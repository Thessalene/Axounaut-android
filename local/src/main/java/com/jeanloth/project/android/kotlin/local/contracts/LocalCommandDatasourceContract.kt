package com.jeanloth.project.android.kotlin.local.contracts

import com.jeanloth.project.android.kotlin.domain_models.entities.Article
import com.jeanloth.project.android.kotlin.domain_models.entities.Command
import kotlinx.coroutines.flow.Flow


interface LocalCommandDatasourceContract {

    fun getAllCommands() : List<Command>

    fun observeAllCommands() : Flow<List<Command>>

    fun saveCommand(command: Command) : Boolean

    fun deleteCommand(command: Command) : Boolean
}