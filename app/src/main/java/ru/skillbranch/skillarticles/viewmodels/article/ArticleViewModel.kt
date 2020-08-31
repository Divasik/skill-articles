package ru.skillbranch.skillarticles.viewmodels.article

import androidx.annotation.VisibleForTesting
import androidx.lifecycle.*
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.skillbranch.skillarticles.data.models.CommentItemData
import ru.skillbranch.skillarticles.data.repositories.ArticleRepository
import ru.skillbranch.skillarticles.data.repositories.CommentsDataFactory
import ru.skillbranch.skillarticles.data.repositories.MarkdownElement
import ru.skillbranch.skillarticles.data.repositories.clearContent
import ru.skillbranch.skillarticles.extensions.data.toAppSettings
import ru.skillbranch.skillarticles.extensions.indexesOf
import ru.skillbranch.skillarticles.extensions.shortFormat
import ru.skillbranch.skillarticles.viewmodels.base.BaseViewModel
import ru.skillbranch.skillarticles.viewmodels.base.IViewModelState
import ru.skillbranch.skillarticles.viewmodels.base.NavigationCommand
import ru.skillbranch.skillarticles.viewmodels.base.Notify
import java.util.concurrent.Executors

class ArticleViewModel(
        handle: SavedStateHandle,
        private val articleId: String
) : BaseViewModel<ArticleState>(handle, ArticleState()), IArticleViewModel {

    private val repository = ArticleRepository
    private var clearContent: String? = null
    private val listConfig by lazy {
        PagedList.Config.Builder()
                .setEnablePlaceholders(true)
                .setPageSize(5)
                .build()
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    val listData: LiveData<PagedList<CommentItemData>> =
            Transformations.switchMap(repository.findArticleCommentCount(articleId)) {
                buildPagedList(repository.loadAllComments(articleId, it))
            }

    init {
        // subscribe on mutable data
        subscribeOnDataSource(repository.findArticle(articleId)) { a, state ->
            if(a.content == null) fetchContent()
            state.copy(
                    shareLink = a.shareLink,
                    title = a.title,
                    category = a.category.title,
                    categoryIcon = a.category.icon,
                    date = a.date.shortFormat(),
                    author = a.author,
                    isBookmark = a.isBookmark,
                    isLike = a.isLike,
                    content = a.content ?: emptyList(),
                    source = a.source,
                    tags = a.tags,
                    isLoadingContent = a.content == null
            )
        }

        subscribeOnDataSource(repository.getAppSettings()) { s, state ->
            state.copy(
                    isDarkMode = s.isDarkMode,
                    isBigText = s.isBigText
            )
        }

        subscribeOnDataSource(repository.isAuth()) { auth, state ->
            state.copy(isAuth = auth)
        }
    }

    private fun fetchContent() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.fetchArticleContent(articleId)
        }
    }

    // app settings
    override fun handleNightMode() {
        val settings = currentState.toAppSettings()
        repository.updateSettings(settings.copy(isDarkMode = !settings.isDarkMode))
    }

    override fun handleUpText() {
        repository.updateSettings(currentState.toAppSettings().copy(isBigText = true))
    }

    override fun handleDownText() {
        repository.updateSettings(currentState.toAppSettings().copy(isBigText = false))
    }

    // personal article info
    override fun handleBookmark() {
        val msg = if(!currentState.isBookmark) "Add to bookmarks" else "Remove from bookmarks"
        viewModelScope.launch(Dispatchers.IO) {
            repository.toggleBookmark(articleId)
            withContext(Dispatchers.Main) {
                notify(Notify.TextMessage(msg))
            }
        }
    }

    override fun handleLike() {
        val isLiked = currentState.isLike
        val msg = if(!isLiked) Notify.TextMessage("Mark is liked")
        else {
            Notify.ActionMessage(
                    "Don't like it anymore",
                    "No, still like it"
                    // handler function, if press "No, still like it" on snackbar, then toggle again
            ) { handleLike() }
        }
        viewModelScope.launch(Dispatchers.IO) {
            repository.toggleLike(articleId)
            if(isLiked) repository.decrementLike(articleId)
            else repository.incrementLike(articleId)
            withContext(Dispatchers.Main) {
                notify(msg)
            }
        }
    }

    override fun handleShare() {
        notify(Notify.ErrorMessage(
            "Share is not implemented", "OK", null))
    }

    override fun handleToggleMenu() {
        updateState { it.copy(isShowMenu = !it.isShowMenu) }
    }

    override fun handleSearchMode(isSearch: Boolean) {
        updateState { it.copy(isSearch = isSearch, isShowMenu = false, searchPosition = 0) }
    }

    override fun handleSearch(query: String?) {
        query ?: return
        if(clearContent == null && currentState.content.isNotEmpty()) {
            clearContent = currentState.content.clearContent()
        }
        val result = clearContent
                .indexesOf(query)
                .map { it to it + query.length }
        updateState { it.copy(searchQuery = query, searchResults = result, searchPosition = 0) }
    }

    override fun handleUpResult() {
        updateState { it.copy(searchPosition = it.searchPosition.dec()) }
    }

    override fun handleDownResult() {
        updateState { it.copy(searchPosition = it.searchPosition.inc()) }
    }

    override fun handleCopyCode() {
        notify(Notify.TextMessage("Code copy to clipboard"))
    }

    override fun handleSendComment(comment: String) {
        if(!currentState.isAuth) {
            navigate(NavigationCommand.StartLogin())
            updateState { it.copy(comment = comment) }
            return
        }
        viewModelScope.launch {
            repository.sendMessage(articleId, comment, currentState.answerToSlug)
            withContext(Dispatchers.Main) {
                updateState { it.copy(answerTo = null, answerToSlug = null, comment = null) }
            }
        }
    }

    fun observeList(
            owner: LifecycleOwner,
            onChanged: (PagedList<CommentItemData>) -> Unit
    ) {
        listData.observe(owner, Observer { onChanged(it) })
    }

    fun buildPagedList(
            dataFactory: CommentsDataFactory
    ): LiveData<PagedList<CommentItemData>> {
        return LivePagedListBuilder<String,CommentItemData>(
                dataFactory,
                listConfig
        )
                .setFetchExecutor(Executors.newSingleThreadExecutor())
                .build()
    }

    fun handleCommentFocus(hasFocus: Boolean) {
        updateState { it.copy(showBottomBar = !hasFocus) }
    }

    fun handleClearComment() {
        updateState { it.copy(answerTo = null, answerToSlug = null) }
    }

    fun handleReplyTo(slug: String, name: String) {
        updateState { it.copy(answerTo = "Reply to $name", answerToSlug = slug) }
    }
}


data class ArticleState(
        val isAuth: Boolean = false,
        val isLoadingContent: Boolean = true,
        val isLoadingReviews: Boolean = true,
        val isLike: Boolean = false,
        val isBookmark: Boolean = false,
        val isShowMenu: Boolean = false,
        val isBigText: Boolean = false,
        val isDarkMode: Boolean = false,
        val isSearch: Boolean = false,
        val searchQuery: String? = null,
        val searchResults: List<Pair<Int,Int>> = emptyList(),
        val searchPosition: Int = 0,
        val shareLink: String? = null,
        val title: String? = null,
        val category: String? = null,
        val categoryIcon: Any? = null,
        val date: String? = null,
        val author: Any? = null,
        val poster: String? = null,
        val content: List<MarkdownElement> = emptyList(),
        val source: String? = null,
        val tags: List<String> = emptyList(),
        val reviews: List<Any> = emptyList(),
        val commentsCount: Int = 0,
        val answerTo: String? = null,
        val answerToSlug: String? = null,
        val showBottomBar: Boolean = true,
        val comment: String? = null
): IViewModelState {

    override fun save(outState: SavedStateHandle) {
        // TODO save state
        outState.set("isSearch", isSearch)
        outState.set("searchQuery", searchQuery)
        outState.set("searchResults", searchResults)
        outState.set("searchPosition", searchPosition)
    }

    @Suppress("UNCHECKED_CAST")
    override fun restore(savedState: SavedStateHandle): IViewModelState {
        // TODO restore state
        return copy(
                isSearch = savedState["isSearch"] ?: false,
                searchQuery = savedState["searchQuery"],
                searchResults = savedState["searchResults"] ?: listOf(),
                searchPosition = savedState["searchPosition"] ?: 0
        )
    }

}