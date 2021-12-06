package com.jeanloth.project.android.kotlin.axounaut.viewModels

import android.util.Log
import androidx.lifecycle.*
import com.jeanloth.project.android.kotlin.axounaut.extensions.notCanceled
import com.jeanloth.project.android.kotlin.axounaut.ui.commands.CommandListFragment
import com.jeanloth.project.android.kotlin.domain.usescases.usecases.ObserveCommandByIdUseCase
import com.jeanloth.project.android.kotlin.domain.usescases.usecases.article.ObserveCommandsUseCase
import com.jeanloth.project.android.kotlin.domain.usescases.usecases.command.DeleteArticleWrapperUseCase
import com.jeanloth.project.android.kotlin.domain.usescases.usecases.command.DeleteCommandUseCase
import com.jeanloth.project.android.kotlin.domain.usescases.usecases.command.SaveArticleWrapperUseCase
import com.jeanloth.project.android.kotlin.domain.usescases.usecases.command.SaveCommandUseCase
import com.jeanloth.project.android.kotlin.domain_models.entities.ArticleWrapper
import com.jeanloth.project.android.kotlin.domain_models.entities.ArticleWrapperStatusType
import com.jeanloth.project.android.kotlin.domain_models.entities.Command
import com.jeanloth.project.android.kotlin.domain_models.entities.CommandStatusType
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.lang.Exception
import androidx.lifecycle.MediatorLiveData
import com.jeanloth.project.android.kotlin.axounaut.extensions.toLocalDate
import java.time.LocalDate

class CommandVM (
    private val observeCommandsUseCase: ObserveCommandsUseCase,
    private val observeCommandByIdUseCase: ObserveCommandByIdUseCase,
    private val saveCommandUseCase: SaveCommandUseCase,
    private val deleteCommandUseCase: DeleteCommandUseCase,
    private val deleteArticleWrapperUseCase: DeleteArticleWrapperUseCase,
    private val saveArticleWrapperUseCase: SaveArticleWrapperUseCase,
): ViewModel() {

    var commands : List<Command> = emptyList()
    var currentCommand : Command? = null
    var currentCommandId : Long = 0L

    var allCommandMutableLiveData : MutableLiveData<List<Command>> = MutableLiveData(emptyList())
    fun allCommandsLiveData() : LiveData<List<Command>> = allCommandMutableLiveData

    var currentCommandMutableLiveData : MutableLiveData<Command?> = MutableLiveData()
    fun currentCommandLiveData() : LiveData<Command?> = currentCommandMutableLiveData

    var paymentReceivedMutableLiveData : MutableLiveData<Double> = MutableLiveData()
    fun paymentReceivedLiveData() : LiveData<Double> = paymentReceivedMutableLiveData

    var displayModeMutableLiveData : MutableLiveData<CommandListFragment.CommandDisplayMode> = MutableLiveData(CommandListFragment.CommandDisplayMode.IN_PROGRESS)

    val commandToDisplayMediatorLiveData = MediatorLiveData<List<Command>>()

    init {

        viewModelScope.launch {
            observeCommandsUseCase.invoke().collect {
                allCommandMutableLiveData.postValue(it)
            }
        }

        commandToDisplayMediatorLiveData.addSource(allCommandMutableLiveData) { value: List<Command> ->
            commandToDisplayMediatorLiveData.setValue(value)
        }
        commandToDisplayMediatorLiveData.addSource(displayModeMutableLiveData) { value: CommandListFragment.CommandDisplayMode ->
            var commandToDisplay: List<Command> = emptyList()
            val commands = allCommandMutableLiveData.value ?: emptyList()

            commandToDisplay = commands.filter { it.statusCode in value.statusCode }.apply {
                when (value) {
                    CommandListFragment.CommandDisplayMode.TO_COME -> filter {
                        it.deliveryDate?.toLocalDate()!!.isAfter(LocalDate.now())
                    }
                    else -> this.sortedBy { it.deliveryDate?.toLocalDate() }
                }
            }
            commandToDisplayMediatorLiveData.setValue(commandToDisplay)
        }
        observeCurrentCommand()
    }

    fun setDisplayMode(displayMode: CommandListFragment.CommandDisplayMode) {
        displayModeMutableLiveData.value = displayMode
    }

    fun observeCurrentCommand(){
        viewModelScope.launch {
            if(currentCommandId != 0L) {
                try {
                    observeCommandByIdUseCase.invoke(currentCommandId).collect {
                        Log.d("[CommandVM]", " Current AWs from command Id $currentCommandId observed : $it")
                        currentCommandMutableLiveData.postValue(it)
                        currentCommand = it
                        setPaymentReceived(it!!.totalPrice!!)

                        if(it.articleWrappers.any { it.statusCode == ArticleWrapperStatusType.CANCELED.code }){
                            //it.totalPrice= realTotalPrice
                            saveCommand(it)
                        }
                    }
                } catch (e:Exception){
                    Log.d("[CommandVM]", " Exception AWs from command Id $currentCommandId observed : $e")
                }
            }
        }
    }

    fun saveCommand(command: Command){
        saveCommandUseCase.invoke(command)
    }

    fun removeCommand(){
        deleteCommandUseCase.invoke(currentCommand!!)
    }

    fun saveArticleWrapper(articleWrapper : ArticleWrapper) {
        saveArticleWrapperUseCase.invoke(articleWrapper)
    }

    fun setPaymentReceived(price : Double){
        paymentReceivedMutableLiveData.postValue(price)
    }

    fun updateStatusCommand(status: CommandStatusType) {
        Log.d("[CommandVM]", "Make command $status")
        saveCommandUseCase.updateCommandStatus(currentCommand!!, status)
    }

    fun deleteArticleWrapperFromCurrentCommand(articleWrapper: ArticleWrapper) {
        deleteArticleWrapperUseCase.invoke(currentCommand!!.articleWrappers.find { it == articleWrapper }!!)
    }

    fun updateStatusByStatus(statusCode: Int) {
        val command = currentCommand!!
        when (statusCode) {
            CommandStatusType.TO_DO.code -> {
                Log.d("[Command details]", "TODO")
                if (command.articleWrappers.any { it.statusCode == ArticleWrapperStatusType.DONE.code || it.statusCode == ArticleWrapperStatusType.CANCELED.code }) {
                    updateStatusCommand(CommandStatusType.IN_PROGRESS)
                }
            }
            CommandStatusType.IN_PROGRESS.code -> {
                Log.d("[Command details]", "In progress")
                if (command.articleWrappers.notCanceled().all { it.statusCode == ArticleWrapperStatusType.DONE.code}) {
                    updateStatusCommand(CommandStatusType.DONE)
                } else if (command.articleWrappers.all { it.statusCode == ArticleWrapperStatusType.CANCELED.code }) {
                    // TODO display dialog to canceled or delete command
                }
            }
            CommandStatusType.DONE.code -> {
                Log.d("[Command details]", "Terminé")
                if (!command.articleWrappers.notCanceled().all { it.statusCode == ArticleWrapperStatusType.DONE.code}) {
                    updateStatusCommand(CommandStatusType.IN_PROGRESS)
                }
            }
            CommandStatusType.DELIVERED.code -> {
                Log.d("[Command details]", "Livré")
            }
            CommandStatusType.PAYED.code -> {
                Log.d("[Command details]", "Payé")
            }
            CommandStatusType.CANCELED.code -> {
                Log.d("[Command details]", "Cancel")
            }
        }

    }

}