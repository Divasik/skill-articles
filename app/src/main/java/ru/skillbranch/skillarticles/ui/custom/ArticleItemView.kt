package ru.skillbranch.skillarticles.ui.custom

import android.content.Context
import android.graphics.Typeface
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.setPadding
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import kotlinx.android.synthetic.main.item_article.*
import ru.skillbranch.skillarticles.R
import ru.skillbranch.skillarticles.data.ArticleItemData
import ru.skillbranch.skillarticles.extensions.attrValue
import ru.skillbranch.skillarticles.extensions.dpToIntPx
import ru.skillbranch.skillarticles.extensions.format
import kotlin.math.max

class ArticleItemView(context: Context) : ViewGroup(context) {

    val tv_date: TextView
    val tv_author: TextView
    val tv_title: TextView
    val iv_poster: ImageView
    val iv_category: ImageView
    val tv_description: TextView
    val iv_likes: ImageView
    val tv_likes_count: TextView
    val iv_comments: ImageView
    val tv_comments_count: TextView
    val tv_read_duration: TextView
    val iv_bookmark: ImageView

    private val padding = context.dpToIntPx(16)
    private val iconSize = context.dpToIntPx(16)
    private val authorMarginL = context.dpToIntPx(16)
    private val titleMarginR = context.dpToIntPx(24)
    private val titleMarginVert = context.dpToIntPx(8)
    private val posterMarginT = context.dpToIntPx(4)
    private val posterMarginB = context.dpToIntPx(20)
    private val descrMarginVert = context.dpToIntPx(8)
    private val iconMargin = context.dpToIntPx(8)
    private val posterSize = context.dpToIntPx(64)
    private val categorySize = context.dpToIntPx(40)
    private val categorySize2 = (categorySize / 2f).toInt()
    private val cornerSize = context.dpToIntPx(8)

    init {
        layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        setPadding(padding)

        tv_date = TextView(context).apply {
            setTextColor(context.getColor(R.color.color_gray))
            textSize = 12f
        }
        addView(tv_date)

        tv_author = TextView(context).apply {
            id = R.id.tv_author
            setTextColor(context.attrValue(R.attr.colorPrimary))
            textSize = 12f
        }
        addView(tv_author)

        tv_title = TextView(context).apply {
            id = R.id.tv_title
            setTextColor(context.attrValue(R.attr.colorPrimary))
            textSize = 18f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        addView(tv_title)

        iv_poster = ImageView(context).apply {
            id = R.id.iv_poster
            layoutParams = LayoutParams(context.dpToIntPx(64), context.dpToIntPx(64))
        }
        addView(iv_poster)

        iv_category = ImageView(context).apply {
            layoutParams = LayoutParams(context.dpToIntPx(40), context.dpToIntPx(40))
        }
        addView(iv_category)

        tv_description = TextView(context).apply {
            id = R.id.tv_description
            setTextColor(context.getColor(R.color.color_gray))
            textSize = 14f
        }
        addView(tv_description)

        iv_likes = ImageView(context).apply {
            layoutParams = LayoutParams(context.dpToIntPx(16), context.dpToIntPx(16))
            setImageResource(R.drawable.ic_favorite_black_24dp)
            imageTintList = context.getColorStateList(R.color.color_gray)
        }
        addView(iv_likes)

        tv_likes_count = TextView(context).apply {
            setTextColor(context.getColor(R.color.color_gray))
            textSize = 12f
        }
        addView(tv_likes_count)

        iv_comments = ImageView(context).apply {
            layoutParams = LayoutParams(context.dpToIntPx(16), context.dpToIntPx(16))
            setImageResource(R.drawable.ic_insert_comment_black_24dp)
            imageTintList = context.getColorStateList(R.color.color_gray)
        }
        addView(iv_comments)

        tv_comments_count = TextView(context).apply {
            setTextColor(context.getColor(R.color.color_gray))
            textSize = 12f
        }
        addView(tv_comments_count)

        tv_read_duration = TextView(context).apply {
            id = R.id.tv_read_duration
            setTextColor(context.getColor(R.color.color_gray))
            textSize = 12f
        }
        addView(tv_read_duration)

        iv_bookmark = ImageView(context).apply {
            layoutParams = LayoutParams(context.dpToIntPx(16), context.dpToIntPx(16))
            setImageResource(R.drawable.bookmark_states)
            imageTintList = context.getColorStateList(R.color.color_gray)
        }
        addView(iv_bookmark)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        var usedHeight = paddingTop
        val width = View.getDefaultSize(suggestedMinimumWidth, widthMeasureSpec)

        measureChild(tv_date, widthMeasureSpec, heightMeasureSpec)

        val authorWidth = width - paddingLeft - paddingRight - tv_date.measuredWidth - authorMarginL
        val authorWidthMs = MeasureSpec.makeMeasureSpec(authorWidth, MeasureSpec.EXACTLY)
        tv_author.measure(authorWidthMs, heightMeasureSpec)
        usedHeight += max(tv_date.measuredHeight, tv_author.measuredHeight)

        val titleWidth = width - posterSize - titleMarginR - paddingLeft - paddingRight
        val titleWidthMs = MeasureSpec.makeMeasureSpec(titleWidth, MeasureSpec.EXACTLY)
        tv_title.measure(titleWidthMs, heightMeasureSpec)
        usedHeight += max(tv_title.measuredHeight + 2*titleMarginVert, posterSize + posterMarginT + posterMarginB)

        measureChild(iv_poster, widthMeasureSpec, heightMeasureSpec)
        measureChild(iv_category, widthMeasureSpec, heightMeasureSpec)

        measureChild(tv_description, widthMeasureSpec, heightMeasureSpec)
        usedHeight += tv_description.measuredHeight
        usedHeight += 2*descrMarginVert

        measureChild(tv_likes_count, widthMeasureSpec, heightMeasureSpec)
        measureChild(tv_comments_count, widthMeasureSpec, heightMeasureSpec)

        val durationWidth = width - paddingLeft - paddingRight - iconSize - iconMargin - tv_likes_count.measuredWidth -
                2*iconMargin - iconSize - iconMargin - tv_comments_count.measuredWidth - 4*iconMargin - iconSize
        val durationWidthMs = MeasureSpec.makeMeasureSpec(durationWidth, MeasureSpec.EXACTLY)
        tv_read_duration.measure(durationWidthMs, heightMeasureSpec)

        usedHeight += tv_read_duration.measuredHeight
        usedHeight += paddingBottom

        setMeasuredDimension(width, usedHeight)
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        var usedHeight = paddingTop
        val bodyWidth = right - left - paddingLeft - paddingRight
        val left = paddingLeft
        val right = paddingLeft + bodyWidth

        tv_date.layout(
                left, usedHeight, left + tv_date.measuredWidth,
                usedHeight + tv_date.measuredHeight
        )
        tv_author.layout(
                left + tv_date.measuredWidth + authorMarginL, usedHeight,
                right, usedHeight + tv_author.measuredHeight
        )
        usedHeight += max(tv_date.measuredHeight, tv_author.measuredHeight)

        val rowH = max(tv_title.measuredHeight + 2*titleMarginVert, posterSize + posterMarginT + posterMarginB)
        val titleDy = ((rowH - tv_title.measuredHeight) / 2f).toInt()
        tv_title.layout(
                left, usedHeight + titleDy,
                left + tv_title.measuredWidth, usedHeight + titleDy + tv_title.measuredHeight
        )
        val posterDy = ((rowH - posterSize - posterMarginT - posterMarginB) / 2f).toInt()
        iv_poster.layout(
                right - posterSize, usedHeight + posterMarginT + posterDy,
                right, usedHeight + posterMarginT + posterDy + posterSize
        )
        iv_category.layout(
                right - posterSize - categorySize2, usedHeight + posterMarginT + posterDy + posterSize - categorySize2,
                right - posterSize + categorySize2, usedHeight + posterMarginT + posterDy + posterSize + categorySize2
        )
        usedHeight += rowH
        usedHeight += descrMarginVert

        tv_description.layout(
                left, usedHeight,
                right, usedHeight + tv_description.measuredHeight
        )
        usedHeight += tv_description.measuredHeight
        usedHeight += descrMarginVert

        var rowL = left
        iv_likes.layout(rowL, usedHeight, rowL + iconSize, usedHeight + iconSize)
        rowL += iconSize
        rowL += iconMargin

        tv_likes_count.layout(
                rowL, usedHeight, rowL + tv_likes_count.measuredWidth, usedHeight + tv_likes_count.measuredHeight
        )
        rowL += tv_likes_count.measuredWidth
        rowL += 2 * iconMargin

        iv_comments.layout(
                rowL, usedHeight, rowL + iconSize, usedHeight + iconSize
        )
        rowL += iconSize
        rowL += iconMargin

        tv_comments_count.layout(
                rowL, usedHeight, rowL + tv_comments_count.measuredWidth, usedHeight + tv_comments_count.measuredHeight
        )
        rowL += tv_comments_count.measuredWidth
        rowL += 2*iconMargin

        tv_read_duration.layout(
                rowL, usedHeight, rowL + tv_read_duration.measuredWidth, usedHeight + tv_read_duration.measuredHeight
        )
        rowL += tv_read_duration.measuredWidth
        rowL += 2*iconMargin

        iv_bookmark.layout(
                rowL, usedHeight, rowL + iconSize, usedHeight + iconSize
        )

        /*Log.d("VHVH", "authorY: ${tv_author.top} ${tv_author.bottom} ${tv_author.measuredHeight} " +
                "posterY: ${iv_poster.top} ${iv_poster.bottom} ${iv_poster.measuredHeight}")*/
    }

    fun bind(item: ArticleItemData) {
        Glide.with(context)
                .load(item.poster)
                .transform(CenterCrop(), RoundedCorners(cornerSize))
                .override(posterSize)
                .into(iv_poster)

        Glide.with(context)
                .load(item.categoryIcon)
                .transform(CenterCrop(), RoundedCorners(cornerSize))
                .override(categorySize)
                .into(iv_category)

        tv_date.text = item.date.format()
        tv_author.text = item.author
        tv_title.text = item.title
        tv_description.text = item.description
        tv_likes_count.text = "${item.likeCount}"
        tv_comments_count.text = "${item.commentCount}"
        tv_read_duration.text = "${item.readDuration} min read"
    }
}