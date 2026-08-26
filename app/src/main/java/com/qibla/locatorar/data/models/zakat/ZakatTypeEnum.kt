package com.qibla.locatorar.data.models.zakat

enum class ZakatTypeEnum(i: Int){
    ZakatAlFitr(0),
    ZakatOnGold(1),
    ZakatOnMoney(2),
    ZakatOnLivestock(3),
    ZakatOnSilver(4),
    ZakatOnRevenue(5),
    ZakatOnCropsAndFruits(6),
    ZakatOnProfessional(7),
    ZakatOnDates(8),
    ZakatOnShare(9),
    ZakatOnCompany(10);
    private val value: Int = i
    fun toInt(): Int {
        return value
    }

    companion object {
        fun fromInt(value: Int): ZakatTypeEnum {
            try {
                return values().first { it.value == value }
            } catch (ex: Exception){
                return values().first { it.value == ZakatTypeEnum.ZakatAlFitr.toInt() }
            }
        }
    }

}