package com.qibla.locatorar.data.models.zakat

data class ZakatOnCompanyItemNameModel (
    var id: String,
    var itemName: String,
    var itemType: String,
    var itemConcept: String,
    var itemTreatment: String,
    var isIncluded: Boolean
)