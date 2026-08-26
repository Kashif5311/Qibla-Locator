package com.qibla.locatorar.data.models.zakat

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
class ZakatCalculationModel() : Parcelable {
    constructor(zakatTypeEnum: ZakatTypeEnum) : this() {
        this.zakatTypeEnum = zakatTypeEnum
    }

    var zakatTypeEnum = ZakatTypeEnum.ZakatOnGold
    var recordAutoID = -1
    var nesabValue = 0.0
    var totalAmount = 0.0
    var zakatValue = 0.0
    var date = ""
    var calenderType = ""
    var addToCartTimeStamp = ""
    var sadaqa = 0.0
    var zakatPaidOn = ""
    var transactionNumber = ""
    var popupModel = ZakatCommonCalculationModel(zakatTypeEnum)

    // Zakat Al Fitr
    var totalPersons = 0.0
    var perPersonPrice = 0.0

    // Zakat on Gold
    var weight24KGold = 0.0
    var weight22KGold = 0.0
    var weight21KGold = 0.0
    var weight18KGold = 0.0
    var totalNetWeight = 0.0

    // Zakat on Livestock
    var noOfCamels = ""
    var noOfCows = ""
    var noOfSheep = ""
    var camelsDesc = ""
    var cowsDesc = ""
    var sheepDesc = ""

    // Zakat on Silver
    var weight = 0.0

    // Zakat on Revenue and professions
    var annualRevenues = 0.0
    var annualCostsAndExpenses = 0.0

    // Zakat on Crops
    var zakatValueInKGs = 0.0

    // Zakat on Shares
    var zakatOnSharesHijri = 0.0
    var zakatOnSharesGregorian = 0.0
    var sharesNesabDesc = ""
    var sharesZakatDesc = ""
    var shareEntries = ArrayList<ShareEntryModel>()

    // Zakat on Companies
    var companyName = ""
    var companiesZakatMessage = ""
    var companiesEntries = ArrayList<CompanyEntryResponseItem>()
}