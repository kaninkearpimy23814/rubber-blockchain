package com.template.contract

import com.template.states.TemplateState
import net.corda.core.contracts.*
import net.corda.core.transactions.LedgerTransaction

class TemplateContract : Contract {
    companion object {
        const val ID = "com.template.contract.TemplateContract"
    }

    override fun verify(tx: LedgerTransaction) {
        val command = tx.commands.requireSingleCommand<Commands.Create>()

        requireThat {

        }
//        when (command.value) {
//            is Commands.Create -> requireThat {

//                "No input when create." using (tx.inputStates.isEmpty())
//                "Only one output state should be created." using (tx.outputStates.size == 1)
//
//                val template:TemplateState = tx.outputStates.first() as TemplateState
//                "The source and the destination cannot be same entity." using (template.source != template.destination)
//                "The volume must greater than 0." using (template.volume > 0)
//                "The price must greater than 0." using (template.price > 0)
//            }
//        }
    }

    interface Commands : CommandData {
        class Create : Commands
    }
}