package com.qibla.locatorar.utils



import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import android.graphics.drawable.Drawable
import android.provider.Settings
import android.util.DisplayMetrics
import android.util.Log
import android.util.Patterns
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.core.content.ContextCompat
import org.w3c.dom.Element
import org.w3c.dom.Node
import java.text.NumberFormat
import java.util.*
import kotlin.math.roundToLong


fun ViewGroup.inflate(resId: Int): View {
    return LayoutInflater.from(context).inflate(resId, this, false)
}

/**
 *
 * @param attr
 * @return
 */

fun Context.getThemeColor(attr: Int): Int {
    val typedValue: TypedValue = TypedValue()
    this.theme.resolveAttribute(attr, typedValue, true)
    return typedValue.data
}

/**
 *
 * @param attr
 * @return
 */
fun Context.getThemedDrawable(attr: Int): Drawable {
    val typedValue = TypedValue()
    theme.resolveAttribute(attr, typedValue, true)
    return ContextCompat.getDrawable(this, typedValue.data)!!
}

fun View.dpToPx(dp: Double): Double {
    val scale = context.resources.displayMetrics.density
    return dp * scale + 0.5f
}

/**
 * @param sp
 * @return
 */
fun View.spToPx(sp: Float): Int {
    val px =
        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp, context.resources.displayMetrics)
            .toInt()
    return px
}

/**
 * @param T
 * @param alt
 * @return
 */
fun <T> T?.ifNotNullOr(alt: T): T {
    if (this == null)
        return alt
    else
        return this
}

fun <T> T?.ifNotNullOr(alt: () -> T): T {
    if (this == null)
        return alt()
    else
        return this
}

fun <T> List<T>.plusElementFront(element: T): List<T> {
    val newList = this.toMutableList()
    newList.add(0, element)
    return newList.toList()
}

inline fun <A, B, R> ifNotNull(a: A?, b: B?, code: (A, B) -> R) {
    if (a != null && b != null)
        code(a, b)
}

fun Any.printLog(key: String, message: String) {
    Log.d(key, message)
}

fun Int.isSuccessResponseCode(): Boolean {
    return this == 200

}

fun Double.setFractions(noOfFractions: Int): String {
    var value = this
    if (this == null) {
        return ""
    }
    var strValue = if (noOfFractions <= 0) {
        value.roundToLong().toString()
    } else {
        AppUtils.getNumberValue(value, noOfFractions).toString()
    }
    try{
        val longValue = strValue.toDouble().toLong()
        if(strValue.toDouble() - longValue == 0.0){
            return longValue.toString()
        }
    } catch (ex: Exception){
        ex.printStackTrace()
    }
    return strValue
}

fun String.setFractions(noOfFractions: Int): String {
    var value: Double? = null
    try {
        value = this.toDouble()
    } catch (ex: Exception){

    }
    if (value == null) {
        return ""
    }
    var strValue = if (noOfFractions <= 0) {
        value.roundToLong().toString()
    } else {
        AppUtils.getNumberValue(value, noOfFractions).toString()
    }
    try{
        val longValue = strValue.toDouble().toLong()
        if(strValue.toDouble() - longValue == 0.0){
            return longValue.toString()
        }
    } catch (ex: Exception){
        ex.printStackTrace()
    }
    return strValue
}

fun Double?.removeDecimalPointsIfZero(): String {
    var value = this
    if (this == null) {
        return ""
    }
    try{
        val longValue = this.toDouble().toLong()
        if(this - longValue == 0.0){
            return longValue.toString()
        }
    } catch (ex: Exception){
        ex.printStackTrace()
    }
    return this.toString()
}

fun Element.getMNodeValue(tag: String?): String {
    try {
        val nodeList = this.getElementsByTagName(tag)
        val node = nodeList.item(0)
        if (node != null) {
            if (node.hasChildNodes()) {
                val child = node.firstChild
                while (child != null) {
                    if (child.nodeType == Node.TEXT_NODE) {
                        return child.nodeValue
                    }
                }
            }
        }
    } catch (ex: Exception){
        ex.printStackTrace()
    }
    return ""
}

fun String.getDoubleValue(): Double {
    var value = this
    if (this == null) {
        return 0.0
    }
    try{
        return this.replace(",", "").replace(" ", "").toDouble()
    } catch (ex: Exception){
        ex.printStackTrace()
    }
    return 0.0
}

fun String.getIntValue(): Int {
    var value = this
    if (this == null) {
        return 0
    }
    try{
        return this.toInt()
    } catch (ex: Exception){
        ex.printStackTrace()
    }
    return 0
}

fun String.getLongValue(): Long {
    var value = this
    if (this == null) {
        return 0
    }
    try{
        return this.toLong()
    } catch (ex: Exception){
        ex.printStackTrace()
    }
    return 0
}

fun String.isValidEmail(): Boolean{
    return !this.isNullOrEmpty() && Patterns.EMAIL_ADDRESS.matcher(this).matches()
}

fun String.getFormatedNumber(): String{
    try {
        val value: Double = this.toDouble()
        val formated = NumberFormat.getNumberInstance(Locale.US).format(value)
        return formated
    } catch (ex: Exception){

    }
    return this
}

fun String.isValidUAEPhoneNumber(): Boolean {
//    val uaeNumberRegex = "^(\\+|00)?971\\d{9}$" // Regex pattern for UAE phone number
//    return matches(uaeNumberRegex.toRegex())

    return length == 12
}