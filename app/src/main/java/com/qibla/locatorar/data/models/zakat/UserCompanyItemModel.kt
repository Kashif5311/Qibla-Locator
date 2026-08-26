package com.qibla.locatorar.data.models.zakat

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class UserCompanyItemModel (
    var id: String,
    var reviewed: Boolean,
    var approved: Boolean,
    var published: Boolean,
    var companyType: String,
    var companyName: String,
    var companyCountry: String,
    var companyCity: String,
    var currancy: String,
    var zakatYearType: String,
    var companyCountryID: String,
    var companyCityID: String
): Parcelable