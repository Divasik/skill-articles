package ru.skillbranch.skillarticles.ui.dialogs

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.dialog_categories_item.*
import ru.skillbranch.skillarticles.R
import ru.skillbranch.skillarticles.data.local.entities.CategoryData

class CategoryAdapter : RecyclerView.Adapter<CategoryAdapter.CatVH>() {

    private var items: List<CategoryData> = emptyList()
    var selIds: MutableList<String> = mutableListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CatVH {
        return CatVH(LayoutInflater.from(parent.context).inflate(R.layout.dialog_categories_item, parent, false))
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: CatVH, position: Int) {
        holder.bind(items[position]) { item, isChecked ->
            if(isChecked) selIds.add(item.categoryId)
            else selIds.remove(item.categoryId)
        }
    }

    fun submitList(list: List<CategoryData>, selectedList: MutableList<String>) {
        items = list
        selIds = selectedList
        notifyDataSetChanged()
    }

    inner class CatVH(override val containerView: View) : RecyclerView.ViewHolder(containerView), LayoutContainer {

        fun bind(item: CategoryData, listener: (CategoryData, Boolean) -> Unit) {
            ch_select.isChecked = selIds.contains(item.categoryId)

            Glide.with(itemView)
                    .load(item.icon)
                    .into(iv_icon)

            tv_category.text = item.title

            tv_count.text = item.articlesCount.toString()

            ch_select.setOnClickListener {
                listener(item, ch_select.isChecked)
            }

            itemView.setOnClickListener {
                ch_select.isChecked = !ch_select.isChecked
                listener(item, ch_select.isChecked)
            }
        }

    }
}