package com.template.contract

import com.template.states.RubberState
import net.corda.core.contracts.*
import net.corda.core.transactions.LedgerTransaction
import java.security.PublicKey

class RubberContract : Contract {
    companion object {
        @JvmStatic
        val ID = "com.template.contract.RubberContract"
    }

    interface Commands : CommandData {
        class Create : Commands
        class Update : Commands
        class Complete : Commands
    }

    override fun verify(tx: LedgerTransaction) {
        val command = tx.commands.requireSingleCommand<Commands>()
        val setOfSigners = command.signers.toSet()

        when (command.value) {
            is Commands.Create -> verifyCreate (tx, setOfSigners)
            is Commands.Update -> verifyUpdate(tx, setOfSigners)
//            is Commands.Complete -> verifyComplete(tx, setOfSigners)
            else -> throw IllegalArgumentException("Unrecognised command.")
        }
    }

    private fun verifyCreate (tx: LedgerTransaction, signers: Set<PublicKey>) = requireThat {
        "No input when create." using (tx.inputStates.isEmpty())
        "Only one output state should be created." using (tx.outputStates.size == 1)
        val out:RubberState = tx.outputStates.first() as RubberState
        "The source and the destination cannot be same entity." using (out.source != out.destination)
        "All of the participants must be signers." using (signers.containsAll(out.participants.map { it.owningKey }))
        "The rubber type must not empty" using (out.rubberType.isNotEmpty())
        "The volume must greater than 0." using (out.volume > 0)
        "The price must greater than 0." using (out.price > 0)
    }

    private fun verifyUpdate (tx: LedgerTransaction, signers: Set<PublicKey>) = requireThat {
        "Must have inputs consumed when updating." using (tx.inputs.isNotEmpty())
        "Must have outputs should be produced when updating." using (tx.outputs.isNotEmpty())
        "Only one input state2 should be updated." using (tx.inputs.size == 1)
        "Only one output state2 should be updated." using (tx.outputs.size == 1)
        val input = tx.inputsOfType<RubberState>().single()
        val out = tx.outputsOfType<RubberState>().single()
        "The source and the destination cannot be same entity." using (out.source != out.destination)
        val allSigners = (input.participants.map { it.owningKey } + out.participants.map { it.owningKey }).toSet()
        "All of the participants must be signers." using (signers.containsAll(allSigners))
        "Rubber type can changed." using (out == input.copy(rubberType = out.rubberType))
        "Volume can changed." using (out == input.copy(volume = out.volume))
        "Price can changed." using (out == input.copy(price = out.price))
        "The rubber type must not empty" using (out.rubberType.isNotEmpty())
        "The volume must greater than 0." using (out.volume > 0)
        "The price must greater than 0." using (out.price > 0)
    }

//    private fun verifyComplete (tx: LedgerTransaction, signers: Set<PublicKey>) = requireThat {
//        "Must have inputs consumed when updating." using (tx.inputs.isNotEmpty())
//        "Must have outputs should be produced when updating." using (tx.outputs.isNotEmpty())
//        "Only one IOUState input state2 should be updated." using (tx.inputsOfType<RubberState>().size == 1)
//        "Only one IOUState output state2 should be updated." using (tx.outputsOfType<RubberState>().size == 1)
//        val input = tx.inputsOfType<RubberState>().single()
//        val out = tx.outputsOfType<RubberState>().single()
//        "The source and the destination cannot be same entity." using (out.source != out.destination)
//        val allSigners = (input.participants.map { it.owningKey } + out.participants.map { it.owningKey }).toSet()
//        "All of the participants must be signers." using (signers.containsAll(allSigners))
//
//        "Only isComplete flag can changed." using (out == input.copy(isComplete = out.isComplete))
//    }
}