package ru.skillbranch.skillarticles.data.repositories

import androidx.paging.DataSource
import androidx.paging.PositionalDataSource
import ru.skillbranch.skillarticles.data.LocalDataHolder
import ru.skillbranch.skillarticles.data.NetworkDataHolder
import ru.skillbranch.skillarticles.data.models.ArticleItemData
import java.lang.Thread.sleep

object ArticlesRepository {

    private val local = LocalDataHolder
    private val network = NetworkDataHolder

    fun allArticles(): ArticlesDataFactory =
            ArticlesDataFactory(ArticleStrategy.AllArticles(::findArticlesByRange))

    fun loadArticlesFromNetwork(start: Int, size: Int): List<ArticleItemData> = network.networkArticleItems
            .drop(start)
            .take(size)
            .apply { sleep(500) }

    fun insertArticlesToDb(articles: List<ArticleItemData>) {
        local.localArticleItems.addAll(articles)
                .apply { sleep(100) }
    }

    fun searchArticles(searchQuery: String): ArticlesDataFactory =
            ArticlesDataFactory(ArticleStrategy.SearchArticles(::searchArticlesByTitle, searchQuery))

    private fun findArticlesByRange(start: Int, size: Int) = local.localArticleItems
            .drop(start)
            .take(size)

    private fun searchArticlesByTitle(start: Int, size: Int, queryTitle: String) = local.localArticleItems
            .asSequence()
            .filter { it.title.contains(queryTitle, true) }
            .drop(start)
            .take(size)
            .toList()

    fun updateBookmark(id: String, isChecked: Boolean) {
        with (local.localArticleItems) {
            val article = first { it.id == id }
            val index = indexOf(article)
            this[index] = article.copy(isBookmark = isChecked)
                    .apply { sleep(100) }
        }
    }

    fun allBookmarks(): ArticlesDataFactory =
            ArticlesDataFactory(ArticleStrategy.BookmarkArticles(::findBookmarksByRange))

    fun searchBookmarks(searchQuery: String): ArticlesDataFactory =
            ArticlesDataFactory(ArticleStrategy.SearchBookmarks(::searchBookmarksByTitle, searchQuery))

    private fun findBookmarksByRange(start: Int, size: Int) = local.localArticleItems
            .filter { it.isBookmark }
            .drop(start)
            .take(size)

    private fun searchBookmarksByTitle(start: Int, size: Int, queryTitle: String) = local.localArticleItems
            .asSequence()
            .filter { it.isBookmark && it.title.contains(queryTitle, true) }
            .drop(start)
            .take(size)
            .toList()
}

class ArticlesDataFactory(val strategy: ArticleStrategy): DataSource.Factory<Int, ArticleItemData>() {

    override fun create(): DataSource<Int, ArticleItemData> = ArticleDataSource(strategy)

}

class ArticleDataSource(private val strategy: ArticleStrategy): PositionalDataSource<ArticleItemData>() {

    override fun loadInitial(params: LoadInitialParams, callback: LoadInitialCallback<ArticleItemData>) {
        val result = strategy.getItems(params.requestedStartPosition, params.requestedLoadSize)
        callback.onResult(result, params.requestedStartPosition)
    }

    override fun loadRange(params: LoadRangeParams, callback: LoadRangeCallback<ArticleItemData>) {
        val result = strategy.getItems(params.startPosition, params.loadSize)
        callback.onResult(result)
    }

}

sealed class ArticleStrategy() {

    abstract fun getItems(start: Int, size: Int): List<ArticleItemData>

    class AllArticles(
            private val itemProvider: (Int,Int) -> List<ArticleItemData>
    ): ArticleStrategy() {
        override fun getItems(start: Int, size: Int): List<ArticleItemData> = itemProvider(start, size)
    }

    class SearchArticles(
            private val itemProvider: (Int,Int,String) -> List<ArticleItemData>,
            private val query: String
    ): ArticleStrategy() {
        override fun getItems(start: Int, size: Int): List<ArticleItemData> = itemProvider(start, size, query)
    }

    class BookmarkArticles(
            private val itemProvider: (Int,Int) -> List<ArticleItemData>
    ): ArticleStrategy() {
        override fun getItems(start: Int, size: Int): List<ArticleItemData> = itemProvider(start, size)
    }

    class SearchBookmarks(
            private val itemProvider: (Int,Int,String) -> List<ArticleItemData>,
            private val query: String
    ): ArticleStrategy() {
        override fun getItems(start: Int, size: Int): List<ArticleItemData> = itemProvider(start, size, query)
    }
}