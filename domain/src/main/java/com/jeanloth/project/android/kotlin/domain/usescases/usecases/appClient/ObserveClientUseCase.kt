package com.jeanloth.project.android.kotlin.domain.usescases.usecases.appClient

import com.jeanloth.project.android.kotlin.data.contracts.AppClientContract
import com.jeanloth.project.android.kotlin.data.contracts.ArticleContract
import com.jeanloth.project.android.kotlin.domain_models.entities.AppClient
import com.jeanloth.project.android.kotlin.domain_models.entities.Article

class ObserveClientUseCase(
    private val appClientContract: AppClientContract
) {

    fun invoke() = appClientContract.observeClients()

}