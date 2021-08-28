package com.jeanloth.project.android.kotlin.axounaut.ui.commands

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.jeanloth.project.android.kotlin.axounaut.MainActivity
import com.jeanloth.project.android.kotlin.axounaut.R
import com.jeanloth.project.android.kotlin.axounaut.adapters.CheckboxListAdapter
import com.jeanloth.project.android.kotlin.axounaut.extensions.SwipeToCancelCallback
import com.jeanloth.project.android.kotlin.axounaut.extensions.displayDialog
import com.jeanloth.project.android.kotlin.axounaut.extensions.notCanceled
import com.jeanloth.project.android.kotlin.axounaut.viewModels.CommandVM
import com.jeanloth.project.android.kotlin.axounaut.viewModels.MainVM
import com.jeanloth.project.android.kotlin.domain_models.entities.ArticleWrapper
import com.jeanloth.project.android.kotlin.domain_models.entities.ArticleWrapperStatusType
import com.jeanloth.project.android.kotlin.domain_models.entities.CommandStatusType
import com.jeanloth.project.android.kotlin.domain_models.entities.CommandStatusType.Companion.getCommandStatusByCode
import com.jeanloth.project.android.kotlin.domain_models.entities.toNameString
import kotlinx.android.synthetic.main.fragment_command_detail.*
import kotlinx.android.synthetic.main.layout_header.*
import org.koin.android.viewmodel.ext.android.sharedViewModel
import org.koin.android.viewmodel.ext.android.viewModel
import splitties.alertdialog.appcompat.*
import splitties.alertdialog.material.materialAlertDialog
import splitties.views.onClick
import splitties.views.textColorResource

/**
 * A simple [Fragment] subclass.
 * Use the [CommandDetailFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class CommandDetailFragment : Fragment() {

    private val mainVM: MainVM by sharedViewModel()

    val args: CommandDetailFragmentArgs by navArgs()
    private lateinit var checkboxListAdapter: CheckboxListAdapter
    private val commandVM: CommandVM by viewModel()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_command_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupHeader()

        commandVM.currentCommandId = args.commandToDetail.idCommand

        commandVM.observeCurrentCommand()

        checkboxListAdapter = CheckboxListAdapter(args.commandToDetail.articleWrappers.toMutableList()).apply {
            onCheckedItem = { item, isChecked ->
                item.statusCode =
                    if (isChecked) ArticleWrapperStatusType.DONE.code else ArticleWrapperStatusType.IN_PROGRESS.code
                commandVM.saveArticleWrapper(item)
            }

            onSwipeItem = { aw, position ->
                displayCancelOrDeleteArticle(aw, position)
            }
        }

        // Set the recyclerView
        rv_command_articles.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = checkboxListAdapter
        }
        val swipeHandler = object : SwipeToCancelCallback(requireContext()) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                TODO("Not yet implemented")
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                checkboxListAdapter.updateArticleStatus(viewHolder.absoluteAdapterPosition)
            }
        }
        val itemTouchHelper = ItemTouchHelper(swipeHandler)
        itemTouchHelper.attachToRecyclerView(rv_command_articles)

        commandVM.currentCommandLiveData().observe(viewLifecycleOwner) {
            Log.d(
                "[Command details]",
                "current commend observed for id ${args.commandToDetail.idCommand} : $it"
            )
            if (it == null) {// Has been deleted
                goBack()
            } else {
                Log.d(
                    "[Command details]",
                    "current command AWs observed for command id ${args.commandToDetail.idCommand} : ${it.articleWrappers}"
                )
                checkboxListAdapter.setItems(
                    it.articleWrappers,
                    it.statusCode == CommandStatusType.TO_DO.code || it.statusCode == CommandStatusType.IN_PROGRESS.code || it.statusCode == CommandStatusType.DONE.code
                )

                // Update status
                tv_command_status.text = getCommandStatusByCode(it.statusCode).label.toUpperCase()

                // Update total price
                tv_total_price.text = getString(R.string.total_price, it.totalPrice.toString())

                commandVM.updateStatusByStatus(it.statusCode)

                when (it.statusCode) {
                    CommandStatusType.TO_DO.code -> {
                        Log.d("[Command details]", "TODO")
                    }
                    CommandStatusType.IN_PROGRESS.code -> {
                        Log.d("[Command details]", "En cours")
                        bt_delivered.isEnabled = true
                        bt_delivered.visibility = VISIBLE
                    }
                    CommandStatusType.DONE.code -> {
                        Log.d("[Command details]", "Terminé")
                        bt_delivered.visibility = VISIBLE
                        bt_delivered.isEnabled = true

                    }
                    CommandStatusType.DELIVERED.code -> {
                        Log.d("[Command details]", "Livré")
                        bt_delivered.visibility = GONE
                        bt_pay.visibility = VISIBLE
                    }
                    CommandStatusType.PAYED.code -> {
                        Log.d("[Command details]", "Payé")
                        bt_delivered.visibility = GONE
                        bt_pay.visibility = GONE
                    }
                    CommandStatusType.CANCELED.code -> {
                        Log.d("[Command details]", "Cancel")
                        bt_delivered.visibility = GONE
                        bt_pay.visibility = GONE
                    }
                }
            }
        }

        bt_delete_command.onClick {
            displayDeleteDialog()
        }

        bt_delivered.onClick {
            if (commandVM.currentCommand!!.articleWrappers.notCanceled().any { it.statusCode != ArticleWrapperStatusType.DONE.code }) {
                confirmUncompleteDeliveryDialog()
            } else {
                commandVM.updateStatusCommand(CommandStatusType.DELIVERED)
            }
        }

        bt_pay.onClick {
            displayPayCommandFragment()
        }

    }

    private fun setupHeader() {
        mainVM.setHeaderTitle(
            getString(
                R.string.command_number_label,
                args.commandToDetail.idCommand
            ), getString(R.string.command_delivery_date_label, args.commandToDetail.deliveryDate)
        )
        val mainActivity = requireActivity() as MainActivity
        mainActivity.hideOrShowMenuButton(false)
        mainActivity.replaceHeaderLogoByBackButton(true)
        tv_command_client.text = args.commandToDetail.client?.toNameString()
    }

    private fun confirmUncompleteDeliveryDialog() {
        displayDialog(
            context = requireContext(),
            titleRef = R.string.delivery_dialog_title,
            contentRef = R.string.delivery_dialog_content,
            positiveButtonLabelRef = R.string.delivery,
            positiveAction = {
                commandVM.updateStatusCommand(CommandStatusType.DELIVERED)
                Snackbar.make(
                    requireView(), "La commande est bien passée au statut 'Livrée'.",
                    Snackbar.LENGTH_LONG
                ).show()
            },
            negativeButtonLabelRef = R.string.cancel,
            negativeAction = {}
        )
    }

    private fun displayDeleteDialog() {
        displayDialog(
            context = requireContext(),
            titleRef = R.string.delete_dialog_title,
            contentRef = R.string.delete_dialog_content,
            positiveButtonLabelRef = R.string.delete,
            positiveAction = {
                commandVM.removeCommand()
                Snackbar.make(
                    requireView(), "La commande a bien été supprimée.",
                    Snackbar.LENGTH_LONG
                ).show()
                goBack()
            },
            negativeButtonLabelRef = R.string.cancel,
            negativeAction = {}
        )
    }

    private fun goBack() {
        findNavController().popBackStack()
    }

    private fun displayPayCommandFragment() {
        val mainActivity = activity as MainActivity
        mainActivity.displayPayCommandFragment(commandVM.currentCommand!!.idCommand)
    }

    private fun displayCancelOrDeleteArticle(articleWrapper: ArticleWrapper, position: Int){
        displayDialog(
            context = requireContext(),
            titleRef = R.string.cancel_dialog_title,
            contentRef = R.string.cancel_dialog_content,
            positiveButtonLabelRef = R.string.cancel_article,
            positiveAction =  {
                articleWrapper.statusCode = ArticleWrapperStatusType.CANCELED.code
                commandVM.saveArticleWrapper(articleWrapper) },
            negativeButtonLabelRef = R.string.delete_article,
            negativeAction = {
                commandVM.deleteArticleWrapperFromCurrentCommand(articleWrapper)
                checkboxListAdapter.onItemDelete(position)
            })
    }


}