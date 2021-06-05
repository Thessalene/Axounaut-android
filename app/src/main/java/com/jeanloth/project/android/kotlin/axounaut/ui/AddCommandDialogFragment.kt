package com.jeanloth.project.android.kotlin.axounaut.ui

import android.app.Dialog
import android.os.Bundle
import android.util.Log
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import android.view.LayoutInflater
import android.view.View
import android.view.View.*
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources.getDrawable
import com.jeanloth.project.android.kotlin.axounaut.R
import com.jeanloth.project.android.kotlin.axounaut.adapters.ArticleAdapter
import com.jeanloth.project.android.kotlin.axounaut.extensions.formatDouble
import com.jeanloth.project.android.kotlin.axounaut.mock.DataMock
import com.jeanloth.project.android.kotlin.domain.entities.Article
import kotlinx.android.synthetic.main.fragment_add_command_dialog.*
import java.time.LocalDate

// TODO: Customize parameter argument names
const val ARG_ITEM_COUNT = "item_count"

/**
 *
 * A fragment that shows a list of items as a modal bottom sheet.
 *
 * You can show this modal bottom sheet from your activity like this:
 * <pre>
 *    AddCommandDialogFragment.newInstance(30).show(supportFragmentManager, "dialog")
 * </pre>
 */
class AddCommandDialogFragment : BottomSheetDialogFragment() {

    var articlesActualized = listOf<Article>()
    var isEditMode = true
    private lateinit var adapter: ArticleAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_add_command_dialog, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        setupHeaders()

        adapter = ArticleAdapter(DataMock.articles, true, requireContext()).apply {
            onAddMinusClick = {
                Log.d("ADD COMMAND", "  article : $it")
                bt_next.visibility  = if(it.count { it.count > 0 } > 0) VISIBLE else INVISIBLE
                articlesActualized = it
            }
        }
        rv_articles.adapter = adapter

        bt_next.setOnClickListener {
            // Display previous button
            if(isEditMode)
                changeEditModeDisplay()
            else {
                // Save command
                Log.d("ADD COMMAND", "Save command ")
            }
        }

        bt_previous_or_close.setOnClickListener {
            if (isEditMode)
                dismiss()
            else
                changeEditModeDisplay()
        }
    }

    private fun changeEditModeDisplay() {
        isEditMode = !isEditMode
        setupPreviousCloseButton()
        setupNextButton()
        adapter.setItems(articlesActualized, isEditMode)
        setupElements()
    }

    private fun setupElements() {
        tv_add_command_title.text = getString(if(isEditMode) R.string.add_command_title else R.string.recap)
        et_client.visibility = if(isEditMode) VISIBLE else GONE
        tv_client.visibility = if(isEditMode) GONE else VISIBLE

        Log.d("TAG", "${articlesActualized.filter { it.count > 0 }}")
        Log.d("TAG", "${articlesActualized.filter { it.count > 0 }.map { it.count * it.unitPrice }}")
        Log.d("TAG", "${articlesActualized.filter { it.count > 0 }.map { it.count * it.unitPrice }.sum()}")

        tv_total_price.visibility = if(isEditMode) GONE else VISIBLE
        if(!isEditMode)  tv_total_price.text = getString(R.string.total_price, articlesActualized.filter { it.count > 0 }.map { it.count * it.unitPrice }.sum().formatDouble())
    }

    private fun setupPreviousCloseButton() {
        bt_previous_or_close.background = getDrawable(
            requireContext(),
            if (isEditMode) R.drawable.ic_close else R.drawable.ic_left_arrow
        )
    }

    private fun setupNextButton() {
        bt_next.background = getDrawable(
            requireContext(),
            if (isEditMode) R.drawable.ic_right_arrow else R.drawable.ic_check
        )
    }

    private fun setupSelectedItems(article: Article) {
        if (articlesActualized.map { it.name }.contains(article.name)) {
            if (article.count > 0) {
                //articlesActualized[articlesActualized.map { it.name }.indexOf(article.name)] = article
            }
        } else {
            //articlesActualized.add(article)
        }
        //articlesActualized.removeIf { it.count == 0 }
        Log.d("ADD COMMAND", "Selected items : $articlesActualized")
    }

    private fun setupHeaders() {
        Log.d("TEST", "${LocalDate.now()}")
        tv_delivery_date.text = LocalDate.now().toString()
    }

    companion object {

        fun newInstance() = AddCommandDialogFragment()

    }
}