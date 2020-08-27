package ru.skillbranch.skillarticles.data.local.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import ru.skillbranch.skillarticles.data.local.entities.ArticleCounts

@Dao
interface ArticleCountsDao : BaseDao<ArticleCounts> {

    @Transaction
    fun upsert(list: List<ArticleCounts>) {
        insert(list)
                .mapIndexed { index, res -> if(res == -1L) list[index] else null }
                .filterNotNull()
                .also { if(it.isNotEmpty()) update(it) }
    }

    @Query("""
        SELECT * FROM article_counts
    """)
    fun findArticleCounts(): LiveData<List<ArticleCounts>>

    @Query("""
        SELECT * FROM article_counts
        WHERE article_id = :articleId
    """)
    fun findArticleCounts(articleId: String): LiveData<ArticleCounts>

    @Query("""
        UPDATE article_counts SET likes = likes+1, updated_at = CURRENT_TIMESTAMP
        WHERE article_id = :article_id
    """)
    fun incrementLike(article_id: String): Int

    @Query("""
        UPDATE article_counts SET likes = MAX(0, likes-1), updated_at = CURRENT_TIMESTAMP
        WHERE article_id = :article_id
    """)
    fun decrementLike(article_id: String): Int

    @Query("""
        UPDATE article_counts SET comments = comments+1, updated_at = CURRENT_TIMESTAMP
        WHERE article_id = :article_id
    """)
    fun incrementCommentsCount(article_id: String): Int

    @Query("""
        SELECT comments FROM article_counts
        WHERE article_id = :article_id
    """)
    fun getCommentsCount(article_id: String): LiveData<Int>
}