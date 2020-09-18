package ru.skillbranch.skillarticles.data.repositories

import androidx.annotation.VisibleForTesting
import androidx.lifecycle.LiveData
import androidx.paging.DataSource
import androidx.sqlite.db.SimpleSQLiteQuery
import ru.skillbranch.skillarticles.data.local.DbManager.db
import ru.skillbranch.skillarticles.data.local.dao.*
import ru.skillbranch.skillarticles.data.local.entities.ArticleItem
import ru.skillbranch.skillarticles.data.local.entities.ArticleTagXRef
import ru.skillbranch.skillarticles.data.local.entities.CategoryData
import ru.skillbranch.skillarticles.data.local.entities.Tag
import ru.skillbranch.skillarticles.data.remote.NetworkManager
import ru.skillbranch.skillarticles.data.remote.res.ArticleRes
import ru.skillbranch.skillarticles.extensions.data.toArticle
import ru.skillbranch.skillarticles.extensions.data.toArticleContent
import ru.skillbranch.skillarticles.extensions.data.toArticleCounts
import ru.skillbranch.skillarticles.extensions.data.toCategory

interface IArticlesRepository {

    suspend fun loadArticlesFromNetwork(start: String? = null, size: Int = 10): Int
    suspend fun fetchArticleContent(articleId: String)
    suspend fun insertArticlesToDb(articles: List<ArticleRes>)
    suspend fun removeArticleContent(articleId: String)
    suspend fun toggleBookmark(articleId: String): Boolean
    suspend fun findLastArticleId(): String?
    fun findTags(): LiveData<List<String>>
    fun findCategoriesData(): LiveData<List<CategoryData>>
    fun rawQueryArticles(filter: ArticleFilter): DataSource.Factory<Int, ArticleItem>
    suspend fun incrementTagUseCount(tag: String)

}

object ArticlesRepository : IArticlesRepository {

    private val network = NetworkManager.api
    private var articlesDao = db.articlesDao()
    private var articlesContentDao = db.articleContentsDao()
    private var articleCountsDao = db.articleCountsDao()
    private var categoriesDao = db.categoriesDao()
    private var tagsDao = db.tagsDao()
    private var articlePersonalDao = db.articlePersonalInfosDao()

    @VisibleForTesting(otherwise = VisibleForTesting.NONE)
    fun setupTestDao(
            articlesDao: ArticlesDao,
            articleCountsDao: ArticleCountsDao,
            categoriesDao: CategoriesDao,
            tagsDao: TagsDao,
            articlePersonalDao: ArticlePersonalInfosDao,
            articlesContentDao: ArticleContentsDao
    ) {
        this.articlesDao = articlesDao
        this.articleCountsDao = articleCountsDao
        this.categoriesDao = categoriesDao
        this.tagsDao = tagsDao
        this.articlePersonalDao = articlePersonalDao
        this.articlesContentDao = articlesContentDao
    }

    override suspend fun loadArticlesFromNetwork(start: String?, size: Int): Int {
        val items = network.articles(start, size)
        if(items.isNotEmpty()) {
            insertArticlesToDb(items)
        }
        return items.size
    }

    override suspend fun insertArticlesToDb(articles: List<ArticleRes>) {
        articlesDao.upsert(articles.map { it.data.toArticle() })
        articleCountsDao.upsert(articles.map { it.counts.toArticleCounts() })

        val refs = articles.map { it.data }
                .fold(mutableListOf<Pair<String,String>>()) { acc, res ->
                    acc.also { list -> list.addAll(res.tags.map { res.id to it }) }
                }

        val tags = refs.map { it.second }
                .distinct()
                .map { Tag(it) }

        val categories = articles.map { it.data.category.toCategory() }

        categoriesDao.insert(categories)
        tagsDao.insert(tags)
        tagsDao.insertRefs(refs.map { ArticleTagXRef(it.first, it.second) })
    }

    override suspend fun toggleBookmark(articleId: String): Boolean {
        return articlePersonalDao.toggleBookmarkOrInsert(articleId)
    }

    override fun findTags(): LiveData<List<String>> {
        return tagsDao.findTags()
    }

    override fun findCategoriesData(): LiveData<List<CategoryData>> {
        return categoriesDao.findAllCetegoriesData()
    }

    override fun rawQueryArticles(filter: ArticleFilter): DataSource.Factory<Int, ArticleItem> {
        return articlesDao.findArticlesByRaw(SimpleSQLiteQuery(filter.toQuery()))
    }

    override suspend fun incrementTagUseCount(tag: String) {
        tagsDao.incrementTagUseCount(tag)
    }

    override suspend fun findLastArticleId(): String? = articlesDao.findLastArticleId()

    override suspend fun fetchArticleContent(articleId: String) {
        val content = network.loadArticleContent(articleId)
        articlesContentDao.insert(content.toArticleContent())
    }

    override suspend fun removeArticleContent(articleId: String) {
        articlesContentDao.delete(articleId)
    }

    /* fun allArticles(): ArticlesDataFactory =
         ArticlesDataFactory(ArticleStrategy.AllArticles(::findArticlesByRange))

     fun searchArticles(searchQuery: String) =
         ArticlesDataFactory(ArticleStrategy.SearchArticle(::searchArticlesByTitle, searchQuery))

     fun allBookmarked(): ArticlesDataFactory =
         ArticlesDataFactory(ArticleStrategy.BookmarkArticles(::findBookmarkArticles))

     fun searchBookmarkedArticles(searchQuery: String): ArticlesDataFactory =
         ArticlesDataFactory(ArticleStrategy.SearchBookmark(::searchBookmarkArticles, searchQuery))

     private fun findArticlesByRange(start: Int, size: Int) = local.localArticleItems
         .drop(start)
         .take(size)

     private fun findBookmarkArticles(start: Int, size: Int) = local.localArticleItems
         .asSequence()
         .filter { it.isBookmark }
         .drop(start)
         .take(size)
         .toList()

     private fun searchBookmarkArticles(start: Int, size: Int,  query: String) = local.localArticleItems
         .asSequence()
         .filter { it.isBookmark  && it.title.contains(query, true)  }
         .drop(start)
         .take(size)
         .toList()

     private fun searchArticlesByTitle(start: Int, size: Int, queryTitle: String) =
         local.localArticleItems
             .asSequence()
             .filter { it.title.contains(queryTitle, true) }
             .drop(start)
             .take(size)
             .toList()


     fun updateBookmark(id: String, checked: Boolean) {
         val index = local.localArticleItems.indexOfFirst { it.id == id }
         if (index == -1) return
         local.localArticleItems[index] = local.localArticleItems[index].copy(isBookmark = checked)
     }*/


}

/*class ArticlesDataFactory(val strategy: ArticleStrategy) :
    DataSource.Factory<Int, ArticleItemData>() {
    override fun create(): DataSource<Int, ArticleItemData> = ArticleDataSource(strategy)
}


class ArticleDataSource(private val strategy: ArticleStrategy) :
    PositionalDataSource<ArticleItemData>() {
    override fun loadInitial(
        params: LoadInitialParams,
        callback: LoadInitialCallback<ArticleItemData>
    ) {
        val result = strategy.getItems(params.requestedStartPosition, params.requestedLoadSize)
        callback.onResult(result, params.requestedStartPosition)
    }

    override fun loadRange(params: LoadRangeParams, callback: LoadRangeCallback<ArticleItemData>) {
        val result = strategy.getItems(params.startPosition, params.loadSize)
        callback.onResult(result)
    }
}*/

class ArticleFilter(
        val search: String? = null,
        val isBookmark: Boolean = false,
        val categories: List<String> = listOf(),
        val isHashtag: Boolean = false
) {

    fun toQuery(): String {
        val qb = QueryBuilder()

        qb.table("ArticleItem")

        if(search != null && !isHashtag) qb.appendWhere("title LIKE '%$search%'")
        if(search != null && isHashtag) {
            qb.innerJoin("article_tag_x_ref AS refs", "refs.a_id = id")
            qb.appendWhere("refs.t_id = '$search'")
        }
        if(isBookmark) qb.appendWhere("is_bookmark = 1")
        if(categories.isNotEmpty()) qb.appendWhere("category_id IN (${categories.joinToString(",")})")

        qb.orderBy("date")
        return qb.build()
    }

}

class QueryBuilder {
    private var table: String? = null
    private var selectColumns: String = "*"
    private var joinTables: String? = null
    private var whereCondition: String? = null
    private var order: String? = null

    fun table(table: String): QueryBuilder {
        this.table = table
        return this
    }

    fun orderBy(column: String, isDesc: Boolean = true): QueryBuilder {
        order = "ORDER BY $column ${if(isDesc) "DESC" else "ASC"}"
        return this
    }

    fun appendWhere(condition: String, logic: String = "AND"): QueryBuilder {
        if(whereCondition.isNullOrEmpty()) whereCondition = "WHERE $condition "
        else whereCondition += "$logic $condition "
        return this
    }

    fun innerJoin(table: String, on: String): QueryBuilder {
        if(joinTables.isNullOrEmpty()) joinTables = "INNER JOIN $table ON $on "
        else joinTables += "INNER JOIN $table ON $on"
        return this
    }

    fun build(): String {
        check(table != null) { "table must be not null" }

        val sb = StringBuilder("SELECT ")
                .append("$selectColumns ")
                .append("FROM $table ")

        if(joinTables != null) sb.append(joinTables)
        if(whereCondition != null) sb.append(whereCondition)
        if(order != null) sb.append(order)

        return sb.toString()
    }
}

/*
sealed class ArticleStrategy() {
    abstract fun getItems(start: Int, size: Int): List<ArticleItemData>

    class AllArticles(
        private val itemProvider: (Int, Int) -> List<ArticleItemData>
    ) : ArticleStrategy() {
        override fun getItems(start: Int, size: Int): List<ArticleItemData> =
            itemProvider(start, size)
    }

    class SearchArticle(
        private val itemProvider: (Int, Int, String) -> List<ArticleItemData>,
        private val query: String
    ) : ArticleStrategy() {
        override fun getItems(start: Int, size: Int): List<ArticleItemData> =
            itemProvider(start, size, query)
    }

    class SearchBookmark(
        private val itemProvider: (Int, Int, String) -> List<ArticleItemData>,
        private val query: String
    ) : ArticleStrategy() {
        override fun getItems(start: Int, size: Int): List<ArticleItemData> =
            itemProvider(start, size, query)
    }

    class BookmarkArticles(
        private val itemProvider: (Int, Int) -> List<ArticleItemData>
    ) : ArticleStrategy() {
        override fun getItems(start: Int, size: Int): List<ArticleItemData> =
            itemProvider(start, size)
    }
}*/
