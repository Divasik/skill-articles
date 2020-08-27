package ru.skillbranch.skillarticles.viewmodels.articles

import android.util.Log
import androidx.lifecycle.*
import androidx.paging.DataSource
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.skillbranch.skillarticles.data.local.entities.ArticleItem
import ru.skillbranch.skillarticles.data.local.entities.CategoryData
import ru.skillbranch.skillarticles.data.repositories.ArticleFilter
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
        val filter = it.toArticleFilter()
        return@switchMap buildPageList(repository.rawQueryArticles(filter))
    }

    fun observeList(
            owner: LifecycleOwner,
            isBookmark: Boolean = false,
            onChange: (PagedList<ArticleItem>) -> Unit
    ) {
        updateState { it.copy(isBookmark = isBookmark) }
        listData.observe(owner, Observer { onChange(it) })
    }

    fun observeTags(owner: LifecycleOwner, onChange: (List<String>) -> Unit) {
        repository.findTags().observe(owner, Observer(onChange))
    }

    fun observeCategories(owner: LifecycleOwner, onChange: (List<CategoryData>) -> Unit) {
        repository.findCategoriesData().observe(owner, Observer(onChange))
    }

    private fun buildPageList(
            dataFactory: DataSource.Factory<Int, ArticleItem>
    ): LiveData<PagedList<ArticleItem>> {
        val builder = LivePagedListBuilder<Int, ArticleItem>(
                dataFactory,
                listConfig
        )

        // if all articles
        if (isEmptyFilter()) {
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

    private fun isEmptyFilter(): Boolean = currentState.searchQuery.isNullOrEmpty()
            && !currentState.isBookmark
            && currentState.selectedCategories.isEmpty()
            && !currentState.isHashtagSearch

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

    private fun itemAtEndHandle(lastLoadArticle: ArticleItem) {
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
                        "Load from network articles from ${items.firstOrNull()?.data?.id} " +
                                "to ${items.lastOrNull()?.data?.id}"
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

    fun handleToggleBookmark(articleId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.toggleBookmark(articleId)
        }
    }

    fun handleSuggestion(tag: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.incrementTagUseCount(tag)
        }
    }

    fun applyCategories(selectedCategories: List<String>) {
        updateState { it.copy(selectedCategories = selectedCategories) }
    }
}

private fun ArticlesState.toArticleFilter(): ArticleFilter = ArticleFilter(
        search = searchQuery,
        isBookmark = isBookmark,
        categories = selectedCategories,
        isHashtag = isHashtagSearch
)

data class ArticlesState(
        val isSearch: Boolean = false,
        val searchQuery: String? = null,
        val isLoading: Boolean = true,
        val isBookmark: Boolean = false,
        val selectedCategories: List<String> = emptyList(),
        val isHashtagSearch: Boolean = false
): IViewModelState

class ArticlesBoundaryCallback(
        private val zeroLoadingHandle: () -> Unit,
        private val itemAtEndHandle: (ArticleItem) -> Unit
): PagedList.BoundaryCallback<ArticleItem>() {
    override fun onZeroItemsLoaded() {
        zeroLoadingHandle()
    }

    override fun onItemAtEndLoaded(itemAtEnd: ArticleItem) {
        itemAtEndHandle(itemAtEnd)
    }
}