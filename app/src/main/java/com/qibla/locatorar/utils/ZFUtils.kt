package com.qibla.locatorar.utils

import android.content.Context
import android.webkit.WebView
import android.webkit.WebViewClient
import org.w3c.dom.Document
import java.io.File
import java.io.InputStream
import java.util.*
import javax.xml.parsers.DocumentBuilder
import javax.xml.parsers.DocumentBuilderFactory

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.CancellationSignal
import android.os.ParcelFileDescriptor
import android.print.PageRange
import android.print.PrintAttributes
import android.print.PrintDocumentAdapter
import android.print.PrintDocumentInfo
import android.print.PrintManager
import com.qibla.locatorar.R
import com.qibla.locatorar.data.models.zakat.DropDownItem
import com.qibla.locatorar.data.models.zakat.ZakatCalculationModel
import com.qibla.locatorar.data.models.zakat.ZakatCommonCalculationModel
import com.qibla.locatorar.data.models.zakat.ZakatTypeEnum
import com.qibla.locatorar.data.models.zakat.ZakatValueModel
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import kotlin.collections.ArrayList


object ZFUtils {

    val shareEntryListItem = "                            <tr class=\"rgRow\">\n" +
            "                                 \n" +
            "                                <td><span id=\"ctl00_ctl00_ContentPlaceHolder1_ContentPlaceHolder1_lblCompanyName\">myKey_companyName</span></td>\n" +
            "                                    \n" +
            "                                <td><span id=\"ctl00_ctl00_ContentPlaceHolder1_ContentPlaceHolder1_lblSharesCount\">myKey_sharesCount</span></td>\n" +
            "                                \n" +
            "                                <td><span id=\"ctl00_ctl00_ContentPlaceHolder1_ContentPlaceHolder1_lblMarketPrice\">myKey_MarketPrice</span></td>\n" +
            "                                  <td><span id=\"ctl00_ctl00_ContentPlaceHolder1_ContentPlaceHolder1_lblSharePrice\">myKey_sharePrice</span></td>\n" +
            "                               \n" +
            "                                    <td><span id=\"ctl00_ctl00_ContentPlaceHolder1_ContentPlaceHolder1_lblProfit\">myKey_profit</span></td>\n" +
//            "                                    <td><span id=\"ctl00_ctl00_ContentPlaceHolder1_ContentPlaceHolder1_lblCleaningPercent\">myKey_cleaningPercentage</span></td>\n" +
            "                                    <td><span id=\"ctl00_ctl00_ContentPlaceHolder1_ContentPlaceHolder1_lblZakatBase\">myKey_zakatBase</span></td>\n" +
            "\n" +
            "                            </tr>"

    val zakatOnCompanyEntryListItem = """
        <tr class="rgRow">
            <td><span id="ctl00_ctl00_ContentPlaceHolder1_ContentPlaceHolder1_lblCompany">myKey_Company</span></td>
            <td><span id="ctl00_ctl00_ContentPlaceHolder1_ContentPlaceHolder1_lblItemCategory">myKey_ItemCategory</span></td>
            <td><span id="ctl00_ctl00_ContentPlaceHolder1_ContentPlaceHolder1_lblItemName">myKey_ItemName</span></td>
            <td><span id="ctl00_ctl00_ContentPlaceHolder1_ContentPlaceHolder1_lblZakatValue">myKey_ZakatValue</span></td>
            <td><span id="ctl00_ctl00_ContentPlaceHolder1_ContentPlaceHolder1_lblOriginalValue">myKey_OriginalValue</span></td>
        </tr>
    """
    val zakatOnCompanyTotal = """
        <tr class="rgRow">
            <td colspan="5"><span id="ctl00_ctl00_ContentPlaceHolder1_ContentPlaceHolder1_lblTotalAssets">myKey_Total</span></td>
        </tr>
        <tr class="rgRow">
            <td colspan="5"><span id="ctl00_ctl00_ContentPlaceHolder1_ContentPlaceHolder1_lblTotalZakatAssets">myKey_TotalZakat</span></td>
        </tr>
    """

    val companyManagementUAEID = "3"

    object ZakatBusinessRules {
        const val nesabOfGoldInGrams = 85 // grams
        const val nesabOfSilverInGrams = 595 // grams
        const val nesabOfCropsAndFruitsPerKGs = 653.0 // kgs
        const val nesabOfDatesPerKGs = 653.0 // kgs
        const val hijriZakatPercentage = 2.5
        const val gregorianZakatPercentage = 2.577
        const val withoutCost = 10.0
        const val withCost = 5.0
        const val bothMethodsAlike = 7.5
        const val datesPerKGPrice: Double = 4.0
    }

    fun calculateZakatAlFitr(
        context: Context,
        persons: Int,
        perPersonPrice: Double
    ): ZakatCalculationModel {
        val model = ZakatCalculationModel(ZakatTypeEnum.ZakatAlFitr)
        model.nesabValue = 0.0
        model.totalPersons = persons.toDouble()
        model.perPersonPrice = perPersonPrice
        model.totalAmount = persons * perPersonPrice
        model.zakatValue = persons * perPersonPrice
        model.date = DateTimeUtils.getCurrentDateTime(AppConstants.DATE_FORMAT_ZAKAT_CALCULATION)
        model.calenderType = getZakatCalculationSelectedCalenderTypeString(context)

        val popupModel = ZakatCommonCalculationModel(model.zakatTypeEnum)
        popupModel.zakatList.add(
            ZakatValueModel(
                context.getString(R.string.zakat_Al_Fitr_value_per_person),
                model.perPersonPrice,
                ""
            )
        )
        popupModel.zakatList.add(
            ZakatValueModel(
                context.getString(R.string.total_number_of_persons),
                model.totalPersons.toDouble(),
                ""
            )
        )
        popupModel.totalZakatLabel = context.getString(R.string.total_zakat_al_fitr_value)
        popupModel.totalZakatValue = model.zakatValue
        popupModel.totalZakatSubLabel = context.getString(R.string.aed)
        popupModel.date = model.date
        popupModel.calenderType = model.calenderType
        model.popupModel = popupModel
        return model
    }

    fun calculateZakatOnGold(
        context: Context,
        goldPrice: Double,
        gold24K: Double,
        gold22K: Double,
        gold21K: Double,
        gold18K: Double
    ): ZakatCalculationModel {
        val model = ZakatCalculationModel(ZakatTypeEnum.ZakatOnGold)
        val netWeightOf24kGold = gold24K
        val netWeightOf22kGold = gold22K / 24 * 22
        val netWeightOf21kGold = gold21K / 24 * 21
        val netWeightOf18kGold = gold18K / 24 * 18
        val totalNetWeight =
            netWeightOf24kGold + netWeightOf22kGold + netWeightOf21kGold + netWeightOf18kGold
        model.nesabValue = goldPrice * ZakatBusinessRules.nesabOfGoldInGrams
        model.weight24KGold = gold24K
        model.weight22KGold = gold22K
        model.weight21KGold = gold21K
        model.weight18KGold = gold18K
        model.totalNetWeight = totalNetWeight
        model.totalAmount = totalNetWeight * goldPrice
        model.zakatValue =
            (if (PreferenceHelper.getPreferredCalender() == AppConstants.CalendarType.HIJRI) {
                (model.totalAmount * ZakatBusinessRules.hijriZakatPercentage) / 100
            } else {
                (model.totalAmount * ZakatBusinessRules.gregorianZakatPercentage) / 100
            }).setFractions(0).toDouble()
        model.date = DateTimeUtils.getCurrentDateTime(AppConstants.DATE_FORMAT_ZAKAT_CALCULATION)
        model.calenderType = getZakatCalculationSelectedCalenderTypeString(context)
        return model
    }

    fun calculateZakatOnMoney(
        context: Context,
        goldPrice: Double,
        money: Double
    ): ZakatCalculationModel {
        val totalNesab = goldPrice * ZakatBusinessRules.nesabOfGoldInGrams
        val totalZakat =
            (if (PreferenceHelper.getPreferredCalender() == AppConstants.CalendarType.HIJRI) {
                (money * ZakatBusinessRules.hijriZakatPercentage) / 100
            } else {
                (money * ZakatBusinessRules.gregorianZakatPercentage) / 100
            }).setFractions(0).toDouble()
        val model = ZakatCalculationModel(ZakatTypeEnum.ZakatOnMoney)
        model.nesabValue = totalNesab
        model.totalAmount = money
        model.zakatValue = totalZakat
        model.date = DateTimeUtils.getCurrentDateTime(AppConstants.DATE_FORMAT_ZAKAT_CALCULATION)
        model.calenderType = getZakatCalculationSelectedCalenderTypeString(context)

        val popupModel = ZakatCommonCalculationModel(model.zakatTypeEnum)
        popupModel.zakatList.add(
            ZakatValueModel(
                context.getString(R.string.nesab_value),
                model.nesabValue,
                context.getString(R.string.aed)
            )
        )
        popupModel.zakatList.add(
            ZakatValueModel(
                context.getString(R.string.the_total_amount),
                model.totalAmount,
                context.getString(R.string.aed)
            )
        )
        popupModel.totalZakatLabel = context.getString(R.string.zakat_value)
        popupModel.totalZakatValue = model.zakatValue
        popupModel.totalZakatSubLabel = context.getString(R.string.aed)
        popupModel.date = model.date
        popupModel.calenderType = model.calenderType
        model.popupModel = popupModel
        return model
    }

    fun calculateZakatOnLiveStock(
        context: Context,
        camels: Int,
        cows: Int,
        sheeps: Int
    ): ZakatCalculationModel {
        val model = ZakatCalculationModel(ZakatTypeEnum.ZakatOnLivestock)
        model.camelsDesc = getCamelsDesc(context, camels)
        model.cowsDesc = getCowsDesc(context, cows)
        model.sheepDesc = getSheepsDesc(context, sheeps)
        model.date = DateTimeUtils.getCurrentDateTime(AppConstants.DATE_FORMAT_ZAKAT_CALCULATION)
        return model
    }

    fun calculateZakatOnSilver(
        context: Context,
        silverPrice: Double,
        weight: Double
    ): ZakatCalculationModel {
        val model = ZakatCalculationModel(ZakatTypeEnum.ZakatOnSilver)
        var nesab = silverPrice * ZakatBusinessRules.nesabOfSilverInGrams
        model.totalAmount = silverPrice * weight
        model.nesabValue = ZakatBusinessRules.nesabOfSilverInGrams.toDouble()
        model.weight = weight
        model.zakatValue =
            (if (PreferenceHelper.getPreferredCalender() == AppConstants.CalendarType.HIJRI) {
                (model.totalAmount * ZakatBusinessRules.hijriZakatPercentage) / 100
            } else {
                (model.totalAmount * ZakatBusinessRules.gregorianZakatPercentage) / 100
            }).setFractions(0).toDouble()
        model.date = DateTimeUtils.getCurrentDateTime(AppConstants.DATE_FORMAT_ZAKAT_CALCULATION)
        model.calenderType = getZakatCalculationSelectedCalenderTypeString(context)

        val popupModel = ZakatCommonCalculationModel(model.zakatTypeEnum)
        popupModel.zakatList.add(
            ZakatValueModel(
                context.getString(R.string.nesab_value),
                model.nesabValue,
                context.getString(R.string.grams)
            )
        )
        popupModel.zakatList.add(
            ZakatValueModel(
                context.getString(R.string.number_of_kilograms_you_own),
                model.weight,
                context.getString(R.string.grams)
            )
        )
        popupModel.totalZakatLabel = context.getString(R.string.zakat_value)
        popupModel.totalZakatValue = model.zakatValue
        popupModel.totalZakatSubLabel = context.getString(R.string.aed)
        popupModel.date = model.date
        popupModel.calenderType = model.calenderType
        model.popupModel = popupModel
        return model
    }

    fun calculateZakatOnRevenue(
        context: Context,
        goldPrice: Double,
        rentalRevenues: Double,
        dueRevenues: Double,
        otherRevenues: Double,
        rentExpense: Double,
        wagesExpense: Double,
        taxesExpense: Double,
        otherExpense: Double,
        currentDebts: Double,
        livingExpense: Double
    ): ZakatCalculationModel {
        val model = ZakatCalculationModel(ZakatTypeEnum.ZakatOnRevenue)
        model.annualRevenues = rentalRevenues + dueRevenues + otherRevenues
        model.annualCostsAndExpenses =
            rentExpense + wagesExpense + taxesExpense + otherExpense + currentDebts + livingExpense
        val netRevenues = model.annualRevenues - model.annualCostsAndExpenses
        model.nesabValue = goldPrice * ZakatBusinessRules.nesabOfGoldInGrams
        model.totalAmount = netRevenues
        model.zakatValue =
            (if (PreferenceHelper.getPreferredCalender() == AppConstants.CalendarType.HIJRI) {
                (netRevenues * ZakatBusinessRules.hijriZakatPercentage) / 100
            } else {
                (netRevenues * ZakatBusinessRules.gregorianZakatPercentage) / 100
            }).setFractions(0).toDouble()
        model.date = DateTimeUtils.getCurrentDateTime(AppConstants.DATE_FORMAT_ZAKAT_CALCULATION)
        model.calenderType = getZakatCalculationSelectedCalenderTypeString(context)

        val popupModel = ZakatCommonCalculationModel(model.zakatTypeEnum)
        popupModel.zakatList.add(
            ZakatValueModel(
                context.getString(R.string.annual_revenues),
                model.annualRevenues,
                ""
            )
        )
        popupModel.zakatList.add(
            ZakatValueModel(
                context.getString(R.string.annual_costs_and_expenses),
                model.annualCostsAndExpenses,
                ""
            )
        )
        popupModel.zakatList.add(
            ZakatValueModel(
                context.getString(R.string.net_revenues),
                netRevenues,
                ""
            )
        )
        popupModel.totalZakatLabel = context.getString(R.string.zakat_value)
        popupModel.totalZakatValue = model.zakatValue
        popupModel.totalZakatSubLabel = context.getString(R.string.aed)
        popupModel.date = model.date
        popupModel.calenderType = model.calenderType
        model.popupModel = popupModel
        return model
    }

//    fun calculateZakatOnCropsAndFruits(
//        context: Context,
//        numberOfKGs: Double,
//        pricePerKG: Double,
//        methodOfIrrigationEnum: MethodOfIrrigationEnum
//    ): ZakatCalculationModel {
//        val irrigationPercentage: Double = when (methodOfIrrigationEnum) {
//            MethodOfIrrigationEnum.WithoutCost -> ZakatBusinessRules.withoutCost
//            MethodOfIrrigationEnum.WithCost -> ZakatBusinessRules.withCost
//            else -> ZakatBusinessRules.bothMethodsAlike
//        }
//        val model = ZakatCalculationModel(ZakatTypeEnum.ZakatOnCropsAndFruits)
//        model.weight = numberOfKGs
//        model.zakatValueInKGs = (model.weight * irrigationPercentage) / 100
//        model.nesabValue = pricePerKG * ZakatBusinessRules.nesabOfCropsAndFruitsPerKGs
//        model.totalAmount = model.weight * pricePerKG
//        model.zakatValue = (model.zakatValueInKGs * pricePerKG).setFractions(0).toDouble()
//        model.date = DateTimeUtils.getCurrentDateTime(AppConstants.DATE_FORMAT_ZAKAT_CALCULATION)
//        model.calenderType = getZakatCalculationSelectedCalenderTypeString(context)
//
//        val popupModel = ZakatCommonCalculationModel(model.zakatTypeEnum)
//        popupModel.zakatList.add(
//            ZakatValueModel(
//                context.getString(R.string.total_weight),
//                model.weight,
//                context.getString(R.string.kgs)
//            )
//        )
//        popupModel.zakatList.add(
//            ZakatValueModel(
//                context.getString(R.string.the_value_of_the_financial_zakat),
//                model.zakatValue,
//                ""
//            )
//        )
//        popupModel.zakatList.add(
//            ZakatValueModel(
//                context.getString(R.string.nesab_value),
//                ZakatBusinessRules.nesabOfCropsAndFruitsPerKGs,
//                context.getString(R.string.kgs)
//            )
//        )
//        popupModel.totalZakatLabel = context.getString(R.string.zakat_value_by_weight)
//        popupModel.totalZakatValue = model.zakatValueInKGs
//        popupModel.totalZakatSubLabel = context.getString(R.string.kilo_grams)
//        popupModel.date = model.date
////        popupModel.calenderType = model.calenderType
//        model.popupModel = popupModel
//        return model
//    }

//    fun calculateZakatOnProfessionals(
//        context: Context,
//        goldPrice: Double,
//        totalIncome: Double,
//        expenses: Double,
//        livingExpense: Double,
//        debts: Double
//    ): ZakatCalculationModel {
//        val model = ZakatCalculationModel(ZakatTypeEnum.ZakatOnProfessional)
//        model.annualRevenues = totalIncome
//        model.annualCostsAndExpenses = expenses + livingExpense + debts
//        val netRevenue = model.annualRevenues - model.annualCostsAndExpenses
//        model.nesabValue = goldPrice * ZakatBusinessRules.nesabOfGoldInGrams
//        model.totalAmount = netRevenue
//        model.zakatValue =
//            (if (PreferenceHelper.getPreferredCalender() == AppConstants.CalendarType.HIJRI) {
//                (netRevenue * ZakatBusinessRules.hijriZakatPercentage) / 100
//            } else {
//                (netRevenue * ZakatBusinessRules.gregorianZakatPercentage) / 100
//            }).setFractions(0).toDouble()
//        model.date = DateTimeUtils.getCurrentDateTime(AppConstants.DATE_FORMAT_ZAKAT_CALCULATION)
//        model.calenderType = getZakatCalculationSelectedCalenderTypeString(context)
//
//        val popupModel = ZakatCommonCalculationModel(model.zakatTypeEnum)
//        popupModel.zakatList.add(
//            ZakatValueModel(
//                context.getString(R.string.annual_revenues),
//                totalIncome,
//                ""
//            )
//        )
//        popupModel.zakatList.add(
//            ZakatValueModel(
//                context.getString(R.string.annual_costs_and_expenses),
//                model.annualCostsAndExpenses,
//                ""
//            )
//        )
//        popupModel.zakatList.add(
//            ZakatValueModel(
//                context.getString(R.string.net_revenues),
//                netRevenue,
//                ""
//            )
//        )
//        popupModel.totalZakatLabel = context.getString(R.string.zakat_value)
//        popupModel.totalZakatValue = model.zakatValue
//        popupModel.totalZakatSubLabel = context.getString(R.string.aed)
//        popupModel.date = model.date
//        popupModel.calenderType = model.calenderType
//        model.popupModel = popupModel
//        return model
//    }
//
//    fun calculateZakatOnDates(
//        context: Context,
//        zakatOnDateEntries: ArrayList<ZakatOnDatesCalculationModel>
//    ): ZakatCalculationModel {
//        var totalWeight = 0.0
//        var totalWeightWithMethodOfIrrigation = 0.0
//        for (entry in zakatOnDateEntries) {
//            val entryWeight =
//                getTreeWeight(entry.regionEnum, entry.treeTypeEnum) * entry.numberOfTrees
//            val irrigationPercentage: Double = when (entry.methodOfIrrigationEnum) {
//                MethodOfIrrigationEnum.WithoutCost -> ZakatBusinessRules.withoutCost
//                MethodOfIrrigationEnum.WithCost -> ZakatBusinessRules.withCost
//                else -> ZakatBusinessRules.bothMethodsAlike
//            }
//            val entryWightWithMethodOfIrrigation = (entryWeight * irrigationPercentage) / 100
//            totalWeight += entryWeight
//            totalWeightWithMethodOfIrrigation += entryWightWithMethodOfIrrigation
//        }
//        val model = ZakatCalculationModel(ZakatTypeEnum.ZakatOnDates)
//
//        model.nesabValue = ZakatBusinessRules.nesabOfDatesPerKGs
//        model.totalAmount = totalWeight
//        model.zakatValueInKGs = totalWeightWithMethodOfIrrigation.setFractions(0).toDouble()
//        model.zakatValue = model.zakatValueInKGs * ZakatBusinessRules.datesPerKGPrice
//        model.date = DateTimeUtils.getCurrentDateTime(AppConstants.DATE_FORMAT_ZAKAT_CALCULATION)
//        model.calenderType = getZakatCalculationSelectedCalenderTypeString(context)
//
//        val popupModel = ZakatCommonCalculationModel(model.zakatTypeEnum)
//        popupModel.zakatList.add(
//            ZakatValueModel(
//                context.getString(R.string.nesab_value),
//                model.nesabValue,
//                context.getString(R.string.kgs)
//            )
//        )
//        popupModel.zakatList.add(
//            ZakatValueModel(
//                context.getString(R.string.price_of_one_kg_dates),
//                ZakatBusinessRules.datesPerKGPrice,
//                context.getString(R.string.aed)
//            )
//        )
//        popupModel.zakatList.add(
//            ZakatValueModel(
//                context.getString(R.string.number_of_kilograms_you_own),
//                model.totalAmount,
//                context.getString(R.string.kgs)
//            )
//        )
//        popupModel.totalZakatLabel = context.getString(R.string.zakat_amount)
//        popupModel.totalZakatValue = model.zakatValue
//        popupModel.totalZakatKGs = model.zakatValueInKGs.toInt()
//        popupModel.totalZakatSubLabel = context.getString(R.string.aed)
//        popupModel.date = model.date
////        popupModel.calenderType = model.calenderType
//        model.popupModel = popupModel
//        return model
//    }
    public fun getZakatCalculationSelectedCalenderTypeString(context: Context): String {
        val calenderType =
            if (PreferenceHelper.getPreferredCalender() == AppConstants.CalendarType.HIJRI) {
                context.getString(R.string.hijri)
            } else {
                context.getString(R.string.gregorian)
            }
        return calenderType
    }

    fun getCamelsDesc(context: Context, camels: Int): String {
        if (camels <= 0) {
            return ""
        }
        return when (camels) {
            in 1..4 -> {
                context.getString(R.string.camels_desc_1_4)
            }

            in 5..9 -> {
                String.format(context.getString(R.string.camels_desc_5_9), camels.toString())
            }

            in 10..14 -> {
                String.format(context.getString(R.string.camels_desc_10_14), camels.toString())
            }

            in 15..19 -> {
                String.format(context.getString(R.string.camels_desc_15_19), camels.toString())
            }

            in 20..24 -> {
                String.format(context.getString(R.string.camels_desc_20_24), camels.toString())
            }

            in 20..24 -> {
                String.format(context.getString(R.string.camels_desc_20_24), camels.toString())
            }

            in 25..35 -> {
                String.format(context.getString(R.string.camels_desc_25_35), camels.toString())
            }

            in 36..45 -> {
                String.format(context.getString(R.string.camels_desc_36_45), camels.toString())
            }

            in 46..60 -> {
                String.format(context.getString(R.string.camels_desc_46_60), camels.toString())
            }

            in 61..75 -> {
                String.format(context.getString(R.string.camels_desc_61_75), camels.toString())
            }

            in 76..90 -> {
                String.format(context.getString(R.string.camels_desc_76_90), camels.toString())
            }

            in 91..120 -> {
                String.format(context.getString(R.string.camels_desc_91_120), camels.toString())
            }

            in 121..129 -> {
                String.format(context.getString(R.string.camels_desc_121_129), camels.toString())
            }

            in 130..139 -> {
                String.format(context.getString(R.string.camels_desc_130_139), camels.toString())
            }

            in 140..149 -> {
                String.format(context.getString(R.string.camels_desc_140_149), camels.toString())
            }

            in 150..159 -> {
                String.format(context.getString(R.string.camels_desc_150_159), camels.toString())
            }

            in 160..169 -> {
                String.format(context.getString(R.string.camels_desc_160_169), camels.toString())
            }

            in 170..179 -> {
                String.format(context.getString(R.string.camels_desc_170_179), camels.toString())
            }

            in 180..189 -> {
                String.format(context.getString(R.string.camels_desc_180_189), camels.toString())
            }

            in 190..199 -> {
                String.format(context.getString(R.string.camels_desc_190_199), camels.toString())
            }

            in 200..209 -> {
                String.format(context.getString(R.string.camels_desc_200_209), camels.toString())
            }

            else -> {
                val zakatTwoYears = (camels / 40).toInt()
                val zakatThreeYears = (camels / 50).toInt()
                String.format(context.getString(R.string.camels_desc_210_plus), camels.toString(), zakatTwoYears.toString(), zakatThreeYears.toString())
            }
        }
    }

    fun getCowsDesc(context: Context, cows: Int): String {
        if (cows <= 0) {
            return ""
        }
        return when (cows) {
            in 1..29 -> {
                context.getString(R.string.cows_desc_1_29)
            }

            in 30..39 -> {
                String.format(context.getString(R.string.cows_desc_30_39), cows.toString())
            }

            in 40..59 -> {
                String.format(context.getString(R.string.cows_desc_40_59), cows.toString())
            }

            in 60..69 -> {
                String.format(context.getString(R.string.cows_desc_60_69), cows.toString())
            }

            in 70..79 -> {
                String.format(context.getString(R.string.cows_desc_70_79), cows.toString())
            }

            in 80..89 -> {
                String.format(context.getString(R.string.cows_desc_80_89), cows.toString())
            }

            in 90..99 -> {
                String.format(context.getString(R.string.cows_desc_90_99), cows.toString())
            }

            in 100..109 -> {
                String.format(context.getString(R.string.cows_desc_100_109), cows.toString())
            }

            in 110..119 -> {
                String.format(context.getString(R.string.cows_desc_110_119), cows.toString())
            }

            in 120..129 -> {
                String.format(context.getString(R.string.cows_desc_120_129), cows.toString())
            }

            else -> {
                val zakatOneYears = (cows / 40).toInt()
                val zakatTwoYears = (cows / 30).toInt()
                String.format(context.getString(R.string.cows_desc_130_plus), cows.toString(), zakatOneYears.toString(), zakatTwoYears.toString())
            }
        }
    }

    fun getSheepsDesc(context: Context, sheeps: Int): String {
        if (sheeps <= 0) {
            return ""
        }
        return when (sheeps) {
            in 1..39 -> {
                context.getString(R.string.sheeps_desc_1_39)
            }

            in 40..120 -> {
                String.format(context.getString(R.string.sheeps_desc_40_120), sheeps.toString())
            }

            in 121..200 -> {
                String.format(context.getString(R.string.sheeps_desc_121_200), sheeps.toString())
            }

            in 201..399 -> {
                String.format(context.getString(R.string.sheeps_desc_201_399), sheeps.toString())
            }

            in 400..499 -> {
                String.format(context.getString(R.string.sheeps_desc_400_499), sheeps.toString())
            }

            in 500..599 -> {
                String.format(context.getString(R.string.sheeps_desc_500_599), sheeps.toString())
            }

            else -> {
                val zakatSheep = (sheeps/100).toInt()
                String.format(context.getString(R.string.sheeps_desc_600_plus), sheeps.toString(), zakatSheep.toString())
            }
        }
    }

//    fun getTreeWeight(regionsEnum: RegionsEnum, tree: TreeTypeEnum): Double {
//        return when (tree) {
//            TreeTypeEnum.SHISHI -> {
//                when (regionsEnum) {
//                    RegionsEnum.WESTERN_REGION_AL_DHAFRA -> {
//                        58.0
//                    }
//
//                    RegionsEnum.EASTERN_REGION_AL_AIN_ABU_DHABI -> {
//                        46.0
//                    }
//
//                    else -> {
//                        42.0
//                    }
//                }
//            }
//
//            TreeTypeEnum.ANBRT_ALMADENA -> {
//                when (regionsEnum) {
//                    RegionsEnum.WESTERN_REGION_AL_DHAFRA -> {
//                        44.0
//                    }
//
//                    RegionsEnum.EASTERN_REGION_AL_AIN_ABU_DHABI -> {
//                        67.0
//                    }
//
//                    else -> {
//                        42.0
//                    }
//                }
//            }
//
//            TreeTypeEnum.DABBAS -> {
//                when (regionsEnum) {
//                    RegionsEnum.WESTERN_REGION_AL_DHAFRA -> {
//                        60.0
//                    }
//
//                    RegionsEnum.EASTERN_REGION_AL_AIN_ABU_DHABI -> {
//                        48.0
//                    }
//
//                    else -> {
//                        42.0
//                    }
//                }
//            }
//
//            TreeTypeEnum.RIZIZ -> {
//                when (regionsEnum) {
//                    RegionsEnum.WESTERN_REGION_AL_DHAFRA -> {
//                        69.0
//                    }
//
//                    RegionsEnum.EASTERN_REGION_AL_AIN_ABU_DHABI -> {
//                        51.0
//                    }
//
//                    else -> {
//                        42.0
//                    }
//                }
//            }
//
//            TreeTypeEnum.NAGAL -> {
//                when (regionsEnum) {
//                    RegionsEnum.WESTERN_REGION_AL_DHAFRA -> {
//                        62.0
//                    }
//
//                    RegionsEnum.EASTERN_REGION_AL_AIN_ABU_DHABI -> {
//                        69.0
//                    }
//
//                    else -> {
//                        42.0
//                    }
//                }
//            }
//
//            TreeTypeEnum.BURHI -> {
//                when (regionsEnum) {
//                    RegionsEnum.WESTERN_REGION_AL_DHAFRA -> {
//                        73.0
//                    }
//
//                    RegionsEnum.EASTERN_REGION_AL_AIN_ABU_DHABI -> {
//                        79.0
//                    }
//
//                    else -> {
//                        42.0
//                    }
//                }
//            }
//
//            TreeTypeEnum.BOWMAN -> {
//                when (regionsEnum) {
//                    RegionsEnum.WESTERN_REGION_AL_DHAFRA -> {
//                        51.0
//                    }
//
//                    RegionsEnum.EASTERN_REGION_AL_AIN_ABU_DHABI -> {
//                        69.0
//                    }
//
//                    else -> {
//                        42.0
//                    }
//                }
//            }
//
//            TreeTypeEnum.KHNEZI -> {
//                when (regionsEnum) {
//                    RegionsEnum.WESTERN_REGION_AL_DHAFRA -> {
//                        55.0
//                    }
//
//                    RegionsEnum.EASTERN_REGION_AL_AIN_ABU_DHABI -> {
//                        69.0
//                    }
//
//                    else -> {
//                        42.0
//                    }
//                }
//            }
//
//            TreeTypeEnum.LULU -> {
//                when (regionsEnum) {
//                    RegionsEnum.WESTERN_REGION_AL_DHAFRA -> {
//                        59.0
//                    }
//
//                    RegionsEnum.EASTERN_REGION_AL_AIN_ABU_DHABI -> {
//                        62.0
//                    }
//
//                    else -> {
//                        42.0
//                    }
//                }
//            }
//
//            TreeTypeEnum.FARD -> {
//                when (regionsEnum) {
//                    RegionsEnum.WESTERN_REGION_AL_DHAFRA -> {
//                        46.0
//                    }
//
//                    RegionsEnum.EASTERN_REGION_AL_AIN_ABU_DHABI -> {
//                        74.0
//                    }
//
//                    else -> {
//                        42.0
//                    }
//                }
//            }
//
//            TreeTypeEnum.KHLAS -> {
//                when (regionsEnum) {
//                    RegionsEnum.WESTERN_REGION_AL_DHAFRA -> {
//                        61.0
//                    }
//
//                    RegionsEnum.EASTERN_REGION_AL_AIN_ABU_DHABI -> {
//                        70.0
//                    }
//
//                    else -> {
//                        42.0
//                    }
//                }
//            }
//
//            TreeTypeEnum.ALL_KIND -> {
//                42.0
//            }
//
//            else -> {
//                0.0
//            }
//        }
//    }

//    fun getIrrigationDropDowns(context: Context): ArrayList<DropDownItem> {
//        var list = ArrayList<DropDownItem>()
//        list.add(
//            DropDownItem(
//                MethodOfIrrigationEnum.WithoutCost.toInt().toString(),
//                context.getString(R.string.without_cost)
//            )
//        )
//        list.add(
//            DropDownItem(
//                MethodOfIrrigationEnum.WithCost.toInt().toString(),
//                context.getString(R.string.with_cost)
//            )
//        )
//        list.add(
//            DropDownItem(
//                MethodOfIrrigationEnum.BothMethodsAlike.toInt().toString(),
//                context.getString(R.string.both_mothods_alike)
//            )
//        )
//        return list
//    }

//    fun getDateTreesDropDowns(context: Context)
//            : ArrayList<DropDownItem> {
//        var list = ArrayList<DropDownItem>()
//        list.add(
//            DropDownItem(
//                TreeTypeEnum.SHISHI.toInt().toString(),
//                context.getString(R.string.shishi)
//            )
//        )
//        list.add(
//            DropDownItem(
//                TreeTypeEnum.ANBRT_ALMADENA.toInt().toString(),
//                context.getString(R.string.anbrt_almadena)
//            )
//        )
//        list.add(
//            DropDownItem(
//                TreeTypeEnum.DABBAS.toInt().toString(),
//                context.getString(R.string.dabbas)
//            )
//        )
//        list.add(
//            DropDownItem(
//                TreeTypeEnum.RIZIZ.toInt().toString(),
//                context.getString(R.string.riziz)
//            )
//        )
//        list.add(
//            DropDownItem(
//                TreeTypeEnum.NAGAL.toInt().toString(),
//                context.getString(R.string.nagal)
//            )
//        )
//        list.add(
//            DropDownItem(
//                TreeTypeEnum.BURHI.toInt().toString(),
//                context.getString(R.string.burhi)
//            )
//        )
//        list.add(
//            DropDownItem(
//                TreeTypeEnum.BOWMAN.toInt().toString(),
//                context.getString(R.string.bowman)
//            )
//        )
//        list.add(
//            DropDownItem(
//                TreeTypeEnum.KHNEZI.toInt().toString(),
//                context.getString(R.string.khnezi)
//            )
//        )
//        list.add(
//            DropDownItem(
//                TreeTypeEnum.LULU.toInt().toString(),
//                context.getString(R.string.lulu)
//            )
//        )
//        list.add(
//            DropDownItem(
//                TreeTypeEnum.FARD.toInt().toString(),
//                context.getString(R.string.fard)
//            )
//        )
//        return list
//    }

//    fun getDateTreesAllKindItem(context: Context): ArrayList<DropDownItem>{
//        var list = ArrayList<DropDownItem>()
//        list.add(
//            DropDownItem(
//                TreeTypeEnum.ALL_KIND.toInt().toString(),
//                context.getString(R.string.all_types)
//            )
//        )
//        return list
//    }

//    fun getEmiratesAndRegions(context: Context): ArrayList<DropDownItem> {
//        var list = ArrayList<DropDownItem>()
//
//        var dubaiRegions = ArrayList<DropDownItem>()
//        dubaiRegions.add(
//            DropDownItem(
//                RegionsEnum.DUBAI_REGIONS.toInt().toString(),
//                context.getString(R.string.dubai_regions)
//            )
//        )
//
//        var abuDhabiRegions = ArrayList<DropDownItem>()
//        abuDhabiRegions.add(
//            DropDownItem(
//                RegionsEnum.EASTERN_REGION_AL_AIN_ABU_DHABI.toInt().toString(),
//                context.getString(R.string.eastern_region_al_ain_abu_dhabi)
//            )
//        )
//        abuDhabiRegions.add(
//            DropDownItem(
//                RegionsEnum.WESTERN_REGION_AL_DHAFRA.toInt().toString(),
//                context.getString(R.string.western_region_al_dhafra)
//            )
//        )
//
//        var sharjahRegions = ArrayList<DropDownItem>()
//        sharjahRegions.add(
//            DropDownItem(
//                RegionsEnum.SHARJAH_REGIONS.toInt().toString(),
//                context.getString(R.string.sharjah_regions)
//            )
//        )
//
//        var ajmanRegions = ArrayList<DropDownItem>()
//        ajmanRegions.add(
//            DropDownItem(
//                RegionsEnum.AJMAN_REGIONS.toInt().toString(),
//                context.getString(R.string.ajman_regions)
//            )
//        )
//
//        var ummAlQuwainRegions = ArrayList<DropDownItem>()
//        ummAlQuwainRegions.add(
//            DropDownItem(
//                RegionsEnum.UMM_AL_QUWAIN_REGIONS.toInt().toString(),
//                context.getString(R.string.umm_al_quwain_regions)
//            )
//        )
//
//        var rasAlKhaimahRegions = ArrayList<DropDownItem>()
//        rasAlKhaimahRegions.add(
//            DropDownItem(
//                RegionsEnum.RAS_AL_KHAIMAH_REGIONS.toInt().toString(),
//                context.getString(R.string.ras_al_khaimah_regions)
//            )
//        )
//
//        var fujairahRegions = ArrayList<DropDownItem>()
//        fujairahRegions.add(
//            DropDownItem(
//                RegionsEnum.FUJAIRAH_REGIONS.toInt().toString(),
//                context.getString(R.string.fujairah_regions)
//            )
//        )
//
//        list.add(DropDownItem("1", context.getString(R.string.dubai), dubaiRegions))
//        list.add(DropDownItem("2", context.getString(R.string.abu_dhabi), abuDhabiRegions))
//        list.add(DropDownItem("3", context.getString(R.string.sharjah), sharjahRegions))
//        list.add(DropDownItem("4", context.getString(R.string.ajman), ajmanRegions))
//        list.add(
//            DropDownItem(
//                "5",
//                context.getString(R.string.umm_al_quwain),
//                ummAlQuwainRegions
//            )
//        )
//        list.add(
//            DropDownItem(
//                "6",
//                context.getString(R.string.ras_al_khaimah),
//                rasAlKhaimahRegions
//            )
//        )
//        list.add(DropDownItem("7", context.getString(R.string.fujairah), fujairahRegions))
//        return list
//    }

    fun getZakatTitle(context: Context, zakatTypeEnum: ZakatTypeEnum): String {
        var zakatOn = ""
        if(zakatTypeEnum == ZakatTypeEnum.ZakatAlFitr){
            return context.getString(R.string.zakat_al_fitr)
        }
        when (zakatTypeEnum) {
            ZakatTypeEnum.ZakatOnGold -> {
                zakatOn = "${context.getString(R.string.gold)}"
            }

            ZakatTypeEnum.ZakatOnMoney -> {
                zakatOn = "${context.getString(R.string.money)}"
            }

            ZakatTypeEnum.ZakatOnLivestock -> {
                zakatOn = "${context.getString(R.string.livestock)}"
            }

            ZakatTypeEnum.ZakatOnSilver -> {
                zakatOn = "${context.getString(R.string.silver)}"
            }

            ZakatTypeEnum.ZakatOnRevenue -> {
                zakatOn = "${context.getString(R.string.revenue)}"
            }

            ZakatTypeEnum.ZakatOnCropsAndFruits -> {
                zakatOn = "${context.getString(R.string.crops_and_fruits)}"
            }

            ZakatTypeEnum.ZakatOnProfessional -> {
                zakatOn = "${context.getString(R.string.professional)}"
            }

            ZakatTypeEnum.ZakatOnDates -> {
                zakatOn = "${context.getString(R.string.dates)}"
            }

            ZakatTypeEnum.ZakatOnShare -> {
                zakatOn = "${context.getString(R.string.shares)}"
            }

            ZakatTypeEnum.ZakatOnCompany -> {
                zakatOn = "${context.getString(R.string.companies)}"
            }

            else -> {}
        }
        if (zakatOn.isNotEmpty()) {
            zakatOn = "${context.getString(R.string.zakat_on)} $zakatOn"
        }
        return zakatOn
    }

    fun getZakatTitleEnglish(context: Context, zakatTypeEnum: ZakatTypeEnum): String {
        var zakatOn = ""
        if(zakatTypeEnum == ZakatTypeEnum.ZakatAlFitr){
            return "Zakat-Al-Fitr"
        }
        when (zakatTypeEnum) {
            ZakatTypeEnum.ZakatOnGold -> {
                zakatOn = "Gold"
            }

            ZakatTypeEnum.ZakatOnMoney -> {
                zakatOn = "Money"
            }

            ZakatTypeEnum.ZakatOnLivestock -> {
                zakatOn = "Livestock"
            }

            ZakatTypeEnum.ZakatOnSilver -> {
                zakatOn = "Sliver"
            }

            ZakatTypeEnum.ZakatOnRevenue -> {
                zakatOn = "Revenue"
            }

            ZakatTypeEnum.ZakatOnCropsAndFruits -> {
                zakatOn = "Crops and Fruits"
            }

            ZakatTypeEnum.ZakatOnProfessional -> {
                zakatOn = "Professional"
            }

            ZakatTypeEnum.ZakatOnDates -> {
                zakatOn = "Dates"
            }

            ZakatTypeEnum.ZakatOnShare -> {
                zakatOn = "Shares"
            }

            ZakatTypeEnum.ZakatOnCompany -> {
                zakatOn = "Companies"
            }

            else -> {}
        }
        if (zakatOn.isNotEmpty()) {
            zakatOn = "Zakat on $zakatOn"
        }
        return zakatOn
    }

    fun getZakatTherId(zakatTypeEnum: ZakatTypeEnum): String {
        return when(zakatTypeEnum){
            ZakatTypeEnum.ZakatOnMoney ->
                "4"

            ZakatTypeEnum.ZakatOnGold ->
                "3"

            ZakatTypeEnum.ZakatOnSilver ->
                "5"

            ZakatTypeEnum.ZakatOnLivestock ->
                "8"

            ZakatTypeEnum.ZakatOnCropsAndFruits ->
                "6"

            ZakatTypeEnum.ZakatOnProfessional ->
                "9"

            ZakatTypeEnum.ZakatOnCompany ->
                "1"

            ZakatTypeEnum.ZakatOnShare ->
                "2"
            else -> {
                ""
            }
        }
    }

//    fun getCompanyCountryDropDowns(context: Context): ArrayList<DropDownItem> {
//        var list = ArrayList<DropDownItem>()
//        list.add(
//            DropDownItem(
//                CountryEnum.UAE.toInt().toString(),
//                context.getString(R.string.uae)
//            )
//        )
//        list.add(
//            DropDownItem(
//                CountryEnum.OTHER.toInt().toString(),
//                context.getString(R.string.another_country)
//            )
//        )
//        return list
//    }
//
//    fun getCompanyCurrencyDropDowns(context: Context): ArrayList<DropDownItem> {
//        var list = ArrayList<DropDownItem>()
//        list.add(
//            DropDownItem(
//                CurrencyEnum.AED.toInt().toString(),
//                context.getString(R.string.aed)
//            )
//        )
//        list.add(
//            DropDownItem(
//                CurrencyEnum.OTHER.toInt().toString(),
//                context.getString(R.string.other)
//            )
//        )
//        return list
//    }
//
//    fun getCompanyCalendarDropDowns(context: Context): ArrayList<DropDownItem> {
//        var list = ArrayList<DropDownItem>()
//        list.add(
//            DropDownItem(
//                CalendarTypeEnum.HIJRI.toInt().toString(),
//                context.getString(R.string.hijri)
//            )
//        )
//        list.add(
//            DropDownItem(
//                CalendarTypeEnum.GREGORIAN_IN_COMPANY.toInt().toString(),
//                context.getString(R.string.gregorian)
//            )
//        )
//        return list
//    }
//
//    fun getCompanyItemTypeDropDowns(context: Context): ArrayList<DropDownItem> {
//        var list = ArrayList<DropDownItem>()
//        list.add(
//            DropDownItem(
//                ItemTypeEnum.ASSETS.toInt().toString(),
//                context.getString(R.string.assets)
//            )
//        )
//        list.add(
//            DropDownItem(
//                ItemTypeEnum.LIABILITIES.toInt().toString(),
//                context.getString(R.string.liabilities)
//            )
//        )
//        return list
//    }

    fun getCompanyManagementCompanyTypeDropDowns(context: Context): ArrayList<DropDownItem> {
        var list = ArrayList<DropDownItem>()
        list.add(
            DropDownItem(
                "1",
                context.getString(R.string.commercial)
            )
        )
        list.add(
            DropDownItem(
                "3",
                context.getString(R.string.industrial)
            )
        )
        return list
    }



    fun getCompanyManagementCompanyCurrencyDropDowns(context: Context): ArrayList<DropDownItem> {
        var list = ArrayList<DropDownItem>()
        list.add(
            DropDownItem(
                companyManagementUAEID,
                context.getString(R.string.aed)
            )
        )
        list.add(
            DropDownItem(
                "243",
                context.getString(R.string.other)
            )
        )
        return list
    }

    fun getCompanyManagementCompanyCountryDropDowns(context: Context): ArrayList<DropDownItem> {
        var list = ArrayList<DropDownItem>()
        list.add(
            DropDownItem(
                companyManagementUAEID,
                context.getString(R.string.uae)
            )
        )
        list.add(
            DropDownItem(
                "243",
                context.getString(R.string.another_country)
            )
        )
        return list
    }

    fun getCompanyManagementCompanyCityDropDowns(context: Context): ArrayList<DropDownItem> {
        var list = ArrayList<DropDownItem>()
        list.add(
            DropDownItem(
                "1",
                context.getString(R.string.dubai)
            )
        )
        list.add(
            DropDownItem(
                "2",
                context.getString(R.string.abu_dhabi)
            )
        )
        list.add(
            DropDownItem(
                "3",
                context.getString(R.string.sharjah)
            )
        )
        list.add(
            DropDownItem(
                "4",
                context.getString(R.string.ajman)
            )
        )
        list.add(
            DropDownItem(
                "5",
                context.getString(R.string.umm_al_quwain)
            )
        )
        list.add(
            DropDownItem(
                "6",
                context.getString(R.string.ras_al_khaimah)
            )
        )
        list.add(
            DropDownItem(
                "7",
                context.getString(R.string.fujairah)
            )
        )
        return list
    }

    fun getCompanyManagementCalendarDropDowns(context: Context): ArrayList<DropDownItem> {
        var list = ArrayList<DropDownItem>()
        list.add(
            DropDownItem(
                "1",
                context.getString(R.string.hijri)
            )
        )
        list.add(
            DropDownItem(
                "2",
                context.getString(R.string.gregorian)
            )
        )
        return list
    }

}