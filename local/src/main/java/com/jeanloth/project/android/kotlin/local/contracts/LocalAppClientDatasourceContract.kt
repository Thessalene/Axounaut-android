package com.jeanloth.project.android.kotlin.local.contracts

import com.jeanloth.project.android.kotlin.domain_models.entities.AppClient
import com.jeanloth.project.android.kotlin.domain_models.entities.Article
import kotlinx.coroutines.flow.Flow


interface LocalAppClientDatasourceContract {

    fun getAllClients() : List<AppClient>

    fun observeAllClients() : Flow<List<AppClient>>

    fun saveClient(article: AppClient) : Boolean

    fun deleteClient(article: AppClient) : Boolean
}