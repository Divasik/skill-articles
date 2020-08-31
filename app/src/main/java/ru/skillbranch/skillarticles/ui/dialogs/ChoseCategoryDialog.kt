package ru.skillbranch.skillarticles.ui.dialogs

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.inflate
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.dialog_categories_item.*
import ru.skillbranch.skillarticles.R
import ru.skillbranch.skillarticles.data.local.entities.CategoryData
import ru.skillbranch.skillarticles.viewmodels.articles.ArticlesViewModel

class ChoseCategoryDialog : DialogFragment() {

    private val viewModel: ArticlesViewModel by activityViewModels()
    private val args: ChoseCategoryDialogArgs by navArgs()
    lateinit var adapter: CategoriesAdapter

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val categories = args.categories.toList()

        val selIds = when {
            savedInstanceState != null -> savedInstanceState.getStringArray("selectedIds")!!.toList()
            else -> args.selectedCategories.toList()
        }

        adapter = CategoriesAdapter().apply {
            submitList(categories, selIds.toMutableList())
        }

        val customView = inflate(context, R.layout.dialog_categories, null)
        val rv = customView.findViewById<RecyclerView>(R.id.categories_list)
        rv.adapter = adapter

        val adb = AlertDialog.Builder(requireContext())
                .setView(customView)
                .setTitle("Chose category")
                .setPositiveButton("Apply") { _, _ ->
                    viewModel.applyCategories((rv.adapter as CategoriesAdapter).selIds)
                }
                .setNegativeButton("Reset") { _, _ ->
                    viewModel.applyCategories(emptyList())
                }

        return adb.create()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putStringArray("selectedIds", adapter.selIds.toTypedArray())
    }
}

class CategoriesAdapter : RecyclerView.Adapter<CategoriesAdapter.CatVH>() {

    private var items: List<CategoryData> = emptyList()
    var selIds: MutableList<String> = mutableListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CatVH {
        return CatVH(LayoutInflater.from(parent.context).inflate(R.layout.dialog_categories_item, parent, false))
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: CatVH, position: Int) {
        holder.bind(items[position]) { item, isChecked ->
            if(isChecked) selIds.add(item.categoryId)
            else selIds.remove(item.categoryId)
        }
    }

    fun submitList(list: List<CategoryData>, selectedList: MutableList<String>) {
        items = list
        selIds = selectedList
        notifyDataSetChanged()
    }

    inner class CatVH(override val containerView: View) : RecyclerView.ViewHolder(containerView), LayoutContainer {

        fun bind(item: CategoryData, listener: (CategoryData, Boolean) -> Unit) {
            ch_select.isChecked = selIds.contains(item.categoryId)

            Glide.with(itemView)
                    .load(item.icon)
                    .into(iv_icon)

            tv_category.text = item.title

            tv_count.text = item.articlesCount.toString()

            ch_select.setOnClickListener {
                listener(item, ch_select.isChecked)
            }

            itemView.setOnClickListener {
                ch_select.isChecked = !ch_select.isChecked
                listener(item, ch_select.isChecked)
            }
        }

    }
}