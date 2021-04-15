package ru.skillbranch.sbdelivery.ui.search

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.jakewharton.rxbinding4.appcompat.queryTextChanges
import org.koin.androidx.viewmodel.ext.android.viewModel
import ru.skillbranch.sbdelivery.core.adapter.ProductDelegate
import ru.skillbranch.sbdelivery.core.decor.GridPaddingItemDecoration
import ru.skillbranch.sbdelivery.databinding.FragmentSearchBinding
import ru.skillbranch.sbdelivery.repository.error.EmptyDishesError
import ru.skillbranch.sbdelivery.ui.main.MainState

class SearchFragment : Fragment() {
    companion object {
        fun newInstance() = SearchFragment()
    }

    private val viewModel: SearchViewModel by viewModel()
    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!
    private val adapter by lazy {
        ProductDelegate().createAdapter {
            // TODO handle click
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.initState()
        viewModel.state.observe(viewLifecycleOwner, ::renderState)
        binding.rvProductGrid.adapter = adapter
        binding.rvProductGrid.addItemDecoration(GridPaddingItemDecoration(17))
        binding.btnRetry.setOnClickListener {
            val currentQuery = binding.searchInput.query
            binding.searchInput.setQuery(currentQuery, true)
        }
        val searchEvent = binding.searchInput.queryTextChanges().skipInitialValue().map { it.toString() }
        viewModel.setSearchEvent(searchEvent)
    }

    private fun renderState(searchState: SearchState) {
        binding.progressSearch.isVisible = searchState == SearchState.Loading
        binding.rvProductGrid.isVisible = searchState is SearchState.Result
        binding.tvErrorMessage.isVisible = searchState is SearchState.Error
        binding.btnRetry.isVisible = searchState is SearchState.Error
        when (searchState) {
            is SearchState.Result -> {
                adapter.items = searchState.items
                adapter.notifyDataSetChanged()
            }
            is SearchState.Error -> {
                binding.tvErrorMessage.text = searchState.errorDescription
            }
        }
    }

    override fun onDestroyView() {
        binding.rvProductGrid.adapter = null
        _binding = null
        super.onDestroyView()
    }

}