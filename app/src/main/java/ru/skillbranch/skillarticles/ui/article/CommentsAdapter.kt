package ru.skillbranch.skillarticles.ui.article

import android.view.ViewGroup
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import ru.skillbranch.skillarticles.data.remote.res.CommentRes
import ru.skillbranch.skillarticles.ui.custom.CommentItemView

class CommentsAdapter(private val listener: (CommentRes) -> Unit)
    : PagedListAdapter<CommentRes, CommentVH>(CommentDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentVH {
        return CommentVH(CommentItemView(parent.context), listener)
    }

    override fun onBindViewHolder(holder: CommentVH, position: Int) {
        holder.bind(getItem(position))
    }
}

class CommentVH(private val containerView: CommentItemView, val listener: (CommentRes) -> Unit) : RecyclerView.ViewHolder(containerView) {
    fun bind(item: CommentRes?) {
        containerView.bind(item)
        if(item != null) {
            itemView.setOnClickListener { listener(item) }
        }
    }
}

class CommentDiffCallback : DiffUtil.ItemCallback<CommentRes>() {
    override fun areItemsTheSame(oldItem: CommentRes, newItem: CommentRes) =
            oldItem.id == newItem.id

    override fun areContentsTheSame(oldItem: CommentRes, newItem: CommentRes) =
            oldItem == newItem
}