package com.template.schema

import net.corda.core.schemas.MappedSchema
import net.corda.core.schemas.PersistentState
import java.util.*
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Table

object RubberSchema

object RubberSchemaTemplate : MappedSchema(
        schemaFamily = RubberSchema.javaClass,
        version = 1,
        mappedTypes = listOf(PersistantSchema::class.java)) {
    @Entity
    @Table(name = "rubber_table")
    class PersistantSchema(
            // Declare field
            @Column (name = "linearId")
            var linearId: UUID,

            @Column (name = "source")
            var source: String,

            @Column (name = "rubberType")
            var rubberType: String,

            @Column (name = "volume")
            var volume: Int,

            @Column (name = "price")
            var price: Int,

            @Column (name = "destination")
            var destination: String
    ) : PersistentState() {
        // Declare default constructor
        constructor() : this(UUID.randomUUID(), "","", 0, 0, "")
    }
}