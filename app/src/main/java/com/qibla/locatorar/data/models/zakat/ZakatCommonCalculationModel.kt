package com.qibla.locatorar.data.models.zakat

class ZakatCommonCalculationModel(var zakatTypeEnum: ZakatTypeEnum) {
    var zakatList: ArrayList<ZakatValueModel> = ArrayList()
    var totalZakatLabel = ""
    var totalZakatValue = 0.0
    var totalZakatSubLabel = ""
    var date = ""
    var calenderType = ""

    // ZakatOnDates
    var totalZakatKGs = 0

}