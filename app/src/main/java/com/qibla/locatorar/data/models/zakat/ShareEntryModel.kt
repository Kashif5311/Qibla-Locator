package com.qibla.locatorar.data.models.zakat

class ShareEntryModel {
    var userId: String = ""
    var sessionID: String = ""
    var id: Int = 0
    var companyName: String = ""
    var noOfShares: Int = 0
    var intention: String = ""
    var purchasePrice: Double = 0.0
    var marketPrice: Double = 0.0
    var profit: Double = 0.0
    var nesabValueAmount: Double = 0.0
    var cleaningPercentage: Double = 0.0
    var zakatTotalPayable: Double = 0.0
    var companyType: String = ""
    var companyId: String = ""
    var selectedActivity: DropDownItem? = null
    var selectedCompany: DropDownItem? = null
}
