package ru.skillbranch.skillarticles.data.remote

import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.http.*
import ru.skillbranch.skillarticles.data.remote.req.*
import ru.skillbranch.skillarticles.data.remote.res.*

interface RestService {

    @GET("articles")
    suspend fun articles(
        @Query("last") last: String? = null,
        @Query("limit") limit: Int = 10
    ): List<ArticleRes>

    @GET("articles/{article}/content")
    suspend fun loadArticleContent(@Path("article") articleId: String): ArticleContentRes

    @GET("articles/{article}/messages")
    fun loadComments(
            @Path("article") articleId: String,
            @Query("last") last: String? = null,
            @Query("limit") limit: Int = 10
    ): Call<List<CommentRes>>

    @POST("articles/{article}/messages")
    suspend fun sendMessage(
            @Path("article") articleId: String,
            @Body message: MessageReq,
            @Header("Authorization") token: String
    ): MessageRes

    @GET("articles/{article}/counts")
    suspend fun loadArticleCounts(@Path("article") articleId: String): ArticleCountsRes

    @POST("auth/register")
    suspend fun register(@Body registerReq: RegisterReq): AuthRes

    @POST("auth/login")
    suspend fun login(@Body loginReq: LoginReq): AuthRes

    @POST("auth/refresh")
    fun refresh(@Body refreshReq: RefreshReq): Call<RefreshRes>

    @POST("articles/{article}/decrementLikes")
    suspend fun decrementLike(
            @Path("article") articleId: String,
            @Header("Authorization") token: String
    ): LikeRes

    @POST("articles/{article}/incrementLikes")
    suspend fun incrementLike(
            @Path("article") articleId: String,
            @Header("Authorization") token: String
    ): LikeRes

    @POST("articles/{article}/removeBookmark")
    suspend fun removeBookmark(
            @Path("article") articleId: String,
            @Header("Authorization") token: String
    ): BookmarkRes

    @POST("articles/{article}/addBookmark")
    suspend fun addBookmark(
            @Path("article") articleId: String,
            @Header("Authorization") token: String
    ): BookmarkRes

    @Multipart
    @POST("profile/avatar/upload")
    suspend fun upload(
            @Part file: MultipartBody.Part?,
            @Header("Authorization") token: String
    ): UploadRes

    @PUT("profile/avatar/remove")
    suspend fun remove(
            @Header("Authorization") token: String
    ): UploadRes

    @PUT("profile")
    suspend fun profile(
            @Body profileReq: EditProfileReq,
            @Header("Authorization") token: String
    ): ProfileRes

}