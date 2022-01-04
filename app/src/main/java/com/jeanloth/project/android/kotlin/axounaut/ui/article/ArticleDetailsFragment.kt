package com.jeanloth.project.android.kotlin.axounaut.ui.article

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.jeanloth.project.android.kotlin.axounaut.R
import com.jeanloth.project.android.kotlin.axounaut.adapters.CheckboxTextViewAdapter
import com.jeanloth.project.android.kotlin.axounaut.viewModels.ArticleVM
import com.jeanloth.project.android.kotlin.axounaut.viewModels.MainVM
import com.jeanloth.project.android.kotlin.axounaut.viewModels.StockVM
import com.jeanloth.project.android.kotlin.domain_models.entities.Article
import com.jeanloth.project.android.kotlin.domain_models.entities.ArticleCategory
import com.jeanloth.project.android.kotlin.domain_models.entities.ArticleCategory.Companion.getArticleCategoryFromLabel
import kotlinx.android.synthetic.main.fragment_article_details.*
import org.koin.android.viewmodel.ext.android.sharedViewModel
import org.koin.android.viewmodel.ext.android.viewModel

/**
 * A simple [Fragment] subclass.
 * create an instance of this fragment.
 */
class ArticleDetailsFragment : Fragment() {

    private val articleVM : ArticleVM by viewModel()
    private val mainVM : MainVM by sharedViewModel()
    private val stockVM : StockVM by viewModel()

    private lateinit var checkboxTextViewAdapter: CheckboxTextViewAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_article_details, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupHeader()

        checkboxTextViewAdapter = CheckboxTextViewAdapter(mutableListOf())

        // Clear focus on typing done
        et_article_price.setOnEditorActionListener { _, actionId, _ ->
            if(actionId == EditorInfo.IME_ACTION_DONE){
                et_article_price.clearFocus()
            }
            true
        }

        rv_recipe_articles.adapter = checkboxTextViewAdapter

        stockVM.observePWLiveData().observe(viewLifecycleOwner){
            checkboxTextViewAdapter.setItems(it)
        }

        setupSpinner()

        bt_add_article.setOnClickListener {
            addArticle()
        }
    }

    private fun setupHeader() {
        mainVM.setHeaderTitle("Ajouter un article")
    }

    private fun addArticle() {
        val articleName = et_article_name.text.toString()
        val articlePrice = et_article_price.text.toString()
        val articleCategory = getArticleCategoryFromLabel(spinner_categories.selectedItem.toString())

        if( articleName.isEmpty() || articlePrice.isEmpty()) {
            Snackbar.make(requireView(), "Veuillez saisir des valeurs valides.",
                Snackbar.LENGTH_LONG).show()
        } else {
            val articleToAdd = Article(
                label = articleName,
                price = articlePrice.toDouble(),
                category = articleCategory.code
            )
            Log.d("[Article Details Fragment", "Article to add : $articleToAdd")
            articleVM.saveArticle(articleToAdd)
            Snackbar.make(requireView(), "Article ajouté avec succès.",
                Snackbar.LENGTH_SHORT).show()
            findNavController().popBackStack()
        }
    }

    private fun setupSpinner() {

        val adapter: ArrayAdapter<String> = ArrayAdapter<String>(
            requireContext(),
            android.R.layout.simple_spinner_item,
            ArticleCategory.values().map { it.label }
        )

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner_categories.adapter = adapter
    }

}