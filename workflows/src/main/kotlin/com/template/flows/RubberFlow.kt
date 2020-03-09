package com.template.flows

import co.paralleluniverse.fibers.Suspendable
import com.template.contract.RubberContract
import com.template.states.RubberState
import net.corda.core.contracts.Command
import net.corda.core.contracts.requireThat
import net.corda.core.flows.*
import net.corda.core.identity.Party
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder
import net.corda.core.utilities.ProgressTracker

object RubberFlow {
    @InitiatingFlow
    @StartableByRPC
    class Initiator (val iouRubberType: String,
                    val iouVolume : Int,
                    val iouPrice : Int,
                    val otherParty: Party) : FlowLogic<SignedTransaction>() {
        companion object {
            object GENERATING_TRANSACTION : ProgressTracker.Step("Generating transaction based on new IOU.")
            object VERIFYING_TRANSACTION : ProgressTracker.Step("Verifying contract constraints.")
            object SIGNING_TRANSACTION : ProgressTracker.Step("Signing transaction with our private key.")
            object GATHERING_SIGS : ProgressTracker.Step("Gathering the counterparty's signature.") {
                override fun childProgressTracker() = CollectSignaturesFlow.tracker()
            }

            object FINALISING_TRANSACTION : ProgressTracker.Step("Obtaining notary signature and recording transaction.") {
                override fun childProgressTracker() = FinalityFlow.tracker()
            }

            fun tracker() = ProgressTracker(
                    GENERATING_TRANSACTION,
                    VERIFYING_TRANSACTION,
                    SIGNING_TRANSACTION,
                    GATHERING_SIGS,
                    FINALISING_TRANSACTION
            )
        }

        override val progressTracker = tracker()

        @Suspendable
        override fun call(): SignedTransaction {
            // Select Notary
            val notary = serviceHub.networkMapCache.notaryIdentities[0]

            // Build transaction
            progressTracker.currentStep = GENERATING_TRANSACTION

            val iouState = RubberState(source = serviceHub.myInfo.legalIdentities.first(), rubberType = iouRubberType, volume = iouVolume, price = iouPrice, destination = otherParty)
            val txCommand = Command(RubberContract.Commands.Create(), iouState.participants.map{ it.owningKey})
            val txBuilder = TransactionBuilder(notary = notary)
                    .addOutputState(iouState)
                    .addCommand(txCommand)

            // Verify transaction
            progressTracker.currentStep = VERIFYING_TRANSACTION
            txBuilder.verify(serviceHub)

            // Initial sign transaction
            progressTracker.currentStep = SIGNING_TRANSACTION
            val partSignedTx = serviceHub.signInitialTransaction(txBuilder)

            // Collect signature from other node
            progressTracker.currentStep = GATHERING_SIGS
            val otherPartySession = initiateFlow(otherParty)
            val fullySignedTx = subFlow(CollectSignaturesFlow(partSignedTx, setOf(otherPartySession), GATHERING_SIGS.childProgressTracker()))

            // Finalise flow and commit transaction to vault of each node
            progressTracker.currentStep = FINALISING_TRANSACTION
            return subFlow(FinalityFlow(fullySignedTx, setOf(otherPartySession), FINALISING_TRANSACTION.childProgressTracker()))
        }
    }

    @InitiatedBy(Initiator::class)
    class Acceptor(val otherPartySession: FlowSession) : FlowLogic<SignedTransaction>() {
        @Suspendable
        override fun call(): SignedTransaction {
            val signTransactionFlow = object : SignTransactionFlow(otherPartySession) {
                override fun checkTransaction(stx: SignedTransaction) = requireThat {
                    val output = stx.tx.outputs.single().data
                    "This must be an IOU transaction." using (output is RubberState)
                }
            }
            val txId = subFlow(signTransactionFlow).id
            return subFlow(ReceiveFinalityFlow(otherPartySession, expectedTxId = txId))
        }
    }
}