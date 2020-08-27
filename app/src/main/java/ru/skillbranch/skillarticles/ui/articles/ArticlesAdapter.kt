package ru.skillbranch.skillarticles.ui.articles

import android.view.View
import android.view.ViewGroup
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.extensions.LayoutContainer
import ru.skillbranch.skillarticles.data.local.entities.ArticleItem
import ru.skillbranch.skillarticles.ui.custom.ArticleItemView

class ArticlesAdapter(
        private val listener: (ArticleItem, Boolean) -> Unit
) : PagedListAdapter<ArticleItem, ArticleVH>(ArticleDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArticleVH {
        val view = ArticleItemView(parent.context)// LayoutInflater.from(parent.context).inflate(R.layout.item_article, parent, false)
        return ArticleVH(view)
    }

    override fun onBindViewHolder(holder: ArticleVH, position: Int) {
        holder.bind(getItem(position), listener)
    }

}

class ArticleDiffCallback: DiffUtil.ItemCallback<ArticleItem>() {
    override fun areItemsTheSame(oldItem: ArticleItem, newItem: ArticleItem): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: ArticleItem, newItem: ArticleItem): Boolean {
        return oldItem == newItem
    }
}

class ArticleVH(override val containerView: View) : RecyclerView.ViewHolder(containerView), LayoutContainer {

    fun bind(item: ArticleItem?,
             listener: (ArticleItem, Boolean) -> Unit) {
        (itemView as ArticleItemView).bind(item!!, listener)
    }

}
