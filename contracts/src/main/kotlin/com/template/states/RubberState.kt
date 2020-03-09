package com.template.states

import com.template.contract.RubberContract
import schema.RubberSchema
import schema.RubberSchemaTemplate
import net.corda.core.contracts.*
import net.corda.core.identity.AbstractParty
import net.corda.core.identity.AnonymousParty
import java.util.*


@BelongsToContract(RubberContract::class)
data class RubberState(val invoiceID: UUID,
                       val source: AnonymousParty,
                       val rubberType: String,
                       val volume : Int,
                       val price : Int,
                       val destination: AnonymousParty) : ContractState {
    override val participants: List<AbstractParty> get() = listOfNotNull(destination,source).map { it }
}
//data class RubberState (override val linearId: UniqueIdentifier = UniqueIdentifier(),
//                        val source: Party,
//                        val rubberType: String,
//                        val volume : Int,
//                        val price : Int,
//                        val destination: Party,
//                        val isComplete : Boolean = false) : LinearState, QueryableState {
//    override val participants: List<Party> get() = listOf(source, destination)
//    override fun generateMappedObject(schema: MappedSchema): PersistentState {
//        return when (schema) {
//            is RubberSchemaTemplate -> RubberSchemaTemplate.PersistantSchema(
//                    // Map state field with schema field
//                    linearId = linearId.id,
//                    source = source.name.toString(),
//                    rubberType = rubberType,
//                    volume = volume,
//                    price = price,
//                    destination = destination.name.toString()
//            )
//            else -> throw IllegalArgumentException("Unrecognised schema $schema")
//        }
//    }
//
//    override fun supportedSchemas(): Iterable<MappedSchema> = listOf(RubberSchemaTemplate)
//}