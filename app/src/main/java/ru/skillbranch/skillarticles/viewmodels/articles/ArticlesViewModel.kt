package ru.skillbranch.skillarticles.viewmodels.articles

import android.util.Log
import androidx.lifecycle.*
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.skillbranch.skillarticles.data.models.ArticleItemData
import ru.skillbranch.skillarticles.data.repositories.ArticleStrategy
import ru.skillbranch.skillarticles.data.repositories.ArticlesDataFactory
import ru.skillbranch.skillarticles.data.repositories.ArticlesRepository
import ru.skillbranch.skillarticles.viewmodels.base.BaseViewModel
import ru.skillbranch.skillarticles.viewmodels.base.IViewModelState
import ru.skillbranch.skillarticles.viewmodels.base.Notify
import java.util.concurrent.Executors

class ArticlesViewModel(handle: SavedStateHandle) : BaseViewModel<ArticlesState>(handle, ArticlesState()) {
    private val repository = ArticlesRepository
    private val listConfig by lazy {
        PagedList.Config.Builder()
                .setEnablePlaceholders(false)
                .setPageSize(10)
                .setPrefetchDistance(30)
                .setInitialLoadSizeHint(50)
                .build()
    }

    private var listData = Transformations.switchMap(state) {
        when {
            it.isSearch && !it.searchQuery.isNullOrBlank() -> buildPageList(repository.searchArticles(it.searchQuery))
            else -> buildPageList(repository.allArticles())
        }
    }

    fun observeList(
            owner: LifecycleOwner,
            onChange: (PagedList<ArticleItemData>) -> Unit
    ) {
        listData.observe(owner, Observer { onChange(it) })
    }

    private fun buildPageList(
            dataFactory: ArticlesDataFactory
    ): LiveData<PagedList<ArticleItemData>> {
        val builder = LivePagedListBuilder<Int, ArticleItemData>(
                dataFactory,
                listConfig
        )

        // if all articles
        if (dataFactory.strategy is ArticleStrategy.AllArticles) {
            builder.setBoundaryCallback(
                    ArticlesBoundaryCallback(
                        ::zeroLoadingHandle,
                        ::itemAtEndHandle
                    )
            )
        }

        return builder
                .setFetchExecutor(Executors.newSingleThreadExecutor())
                .build()
    }

    private fun zeroLoadingHandle() {
        notify(Notify.TextMessage("Storage is empty"))
        viewModelScope.launch(Dispatchers.IO) {
            val items = repository.loadArticlesFromNetwork(0, listConfig.initialLoadSizeHint)
            if(items.isNotEmpty()) {
                repository.insertArticlesToDb(items)
                listData.value?.dataSource?.invalidate()
                Log.d("ArticlesVM","zeroLoadingHandle > load initial & invalidate lastKey: ${listData.value!!.lastKey}")
            }
        }
    }

    private fun itemAtEndHandle(lastLoadArticle: ArticleItemData) {
        Log.d("ArticlesVM","itemAtEndHandle > lastKey: ${listData.value!!.lastKey}")
        viewModelScope.launch(Dispatchers.IO) {
            val items = repository.loadArticlesFromNetwork(
                    start = lastLoadArticle.id.toInt().inc(),
                    size = listConfig.pageSize
            )
            if(items.isNotEmpty()) {
                repository.insertArticlesToDb(items)
                listData.value?.dataSource?.invalidate()
                Log.d("ArticlesVM","itemAtEndHandle > load chunk & invalidate lastKey: ${listData.value!!.lastKey}")
            }
            withContext(Dispatchers.Main) {
                notify(Notify.TextMessage(
                        "Load from network articles from ${items.firstOrNull()?.id} " +
                                "to ${items.lastOrNull()?.id}"
                ))
            }
        }
    }

    fun handleSearch(query: String?) {
        query ?: return
        updateState { it.copy(searchQuery = query) }
    }

    fun handleSearchMode(isSearch: Boolean) {
        updateState { it.copy(isSearch = isSearch) }
    }

    fun handleToggleBookmark(id: String, isChecked: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateBookmark(id, !isChecked)
            listData.value?.dataSource?.invalidate()
        }
    }
}

data class ArticlesState(
    val isSearch: Boolean = false,
    val searchQuery: String? = null,
    val isLoading: Boolean = true
): IViewModelState

class ArticlesBoundaryCallback(
        private val zeroLoadingHandle: () -> Unit,
        private val itemAtEndHandle: (ArticleItemData) -> Unit
): PagedList.BoundaryCallback<ArticleItemData>() {
    override fun onZeroItemsLoaded() {
        zeroLoadingHandle()
    }

    override fun onItemAtEndLoaded(itemAtEnd: ArticleItemData) {
        itemAtEndHandle(itemAtEnd)
    }
}