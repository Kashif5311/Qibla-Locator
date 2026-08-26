package com.qibla.locatorar.data.models.zakat
class DropDownItem {
    var id: String = ""
    var nameEn: String = ""
    var nameAr: String = ""
    var isSelected: Boolean = false
    var isRequiredDocs: Boolean = false
    var subItems = ArrayList<DropDownItem>()
    var zakatOnCompanyItemNameModel: ZakatOnCompanyItemNameModel? = null
    var userComapnyItemModel: UserCompanyItemModel? = null

    constructor(id: String){
        this.id = id
    }

    constructor(id: String, nameEn: String){
        this.id = id
        this.nameEn = nameEn
    }

    constructor(id: String, nameEn: String, nameAr: String){
        this.id = id
        this.nameEn = nameEn
        this.nameAr = nameAr
    }

    constructor(id: String, nameEn: String, subItems: ArrayList<DropDownItem>){
        this.id = id
        this.nameEn = nameEn
        this.subItems = subItems
    }

}
