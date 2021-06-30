package com.jeanloth.project.android.kotlin.domain.usescases.usecases

import com.jeanloth.project.android.kotlin.data.contracts.ArticleContract


class GetAllArticlesUseCase(
    private val articleContract : ArticleContract
) {

    fun invoke() = articleContract.getAllArticles()
}