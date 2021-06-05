package com.jeanloth.project.android.kotlin.axounaut.adapters

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.core.content.ContextCompat.getColor
import androidx.recyclerview.widget.RecyclerView
import com.jeanloth.project.android.kotlin.axounaut.R
import com.jeanloth.project.android.kotlin.domain.entities.Article
import kotlinx.android.synthetic.main.item_article.view.*

class ArticleAdapter(
    private var articleList : List<Article>,
    private var isEditMode : Boolean = true,
    private val context : Context
) : RecyclerView.Adapter<ArticleAdapter.ArticleHolder>()  {

    var onAddMinusClick : ((List<Article>) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArticleHolder {
        val itemView = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_article, parent, false)
        return ArticleHolder(itemView)
    }

    override fun onBindViewHolder(holder: ArticleHolder, position: Int) {
        val article: Article = articleList[position]
        holder.bind(article, position)

    }

    override fun getItemCount(): Int {
        return articleList.size
    }

    fun setItems(articles : List<Article>, isEditMode : Boolean){
        this.articleList = if(isEditMode) articles else articles.filter { it.count > 0 }
        this.isEditMode = isEditMode
        notifyDataSetChanged()
    }

    inner class ArticleHolder(view: View) : RecyclerView.ViewHolder(view), View.OnClickListener {

        fun bind(article : Article, position : Int){

            Log.d("Article adapter", "In Artcile Holder isEditMode : $isEditMode")
            itemView.tv_name.text= article.name

            val count = article.count

            setupElementVisibility(count.toString(), isEditMode)

            itemView.tv_name.setTextColor(getColor(context, R.color.gray_1))
            itemView.tv_count.setTextColor(getColor(context, R.color.gray_1))

            if(isEditMode){
                itemView.ib_minus.visibility = if(count > 0) VISIBLE else GONE

                itemView.tv_name.setTextColor(getColor(context, if(count > 0) R.color.salamander else R.color.gray_1))
                itemView.tv_count.setTextColor(getColor(context, if(count > 0) R.color.salamander else R.color.gray_1))

                itemView.ib_add.setOnClickListener {
                    article.count = count + 1
                    onAddMinusClick?.invoke(articleList)
                    notifyDataSetChanged()
                }

                itemView.ib_minus.setOnClickListener {
                    if(count > 0) {
                        article.count = count - 1
                    }
                    onAddMinusClick?.invoke(articleList)
                    notifyItemChanged(position)
                }
            } else {
                Log.d("ADAPTER", " : $count")
            }
        }

        private fun setupElementVisibility(count : String, editMode: Boolean) {
            itemView.tv_count.text = if(isEditMode) count else context.resources.getString(R.string.x_count, count)
            itemView.ib_add.visibility = if(isEditMode) VISIBLE else GONE
            if(!isEditMode) itemView.ib_minus.visibility = GONE
        }

        init {
            view.setOnClickListener(this)
        }

        override fun onClick(v: View) {
            Log.d("RecyclerView", "CLICK on !")
        }

    }

}