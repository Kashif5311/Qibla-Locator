package com.qibla.locatorar.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.qibla.locatorar.data.models.zakat.ZakatCalculationModel
import com.qibla.locatorar.ui.components.UnderNisabDialog
import com.qibla.locatorar.ui.components.ZakatResultDialog
import com.qibla.locatorar.utils.AppConstants
import com.qibla.locatorar.utils.PreferenceHelper
import com.qibla.locatorar.utils.ZFUtils
import java.util.Locale

import androidx.compose.ui.res.stringResource
import com.qibla.locatorar.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ZakatCalculatorScreen() {
    val context = LocalContext.current
    var goldPrice by remember { mutableStateOf(PreferenceHelper.getGoldPrice()) }
    var selectedUnit by remember { mutableStateOf(PreferenceHelper.getGoldUnit()) }
    var selectedCurrency by remember { mutableStateOf(PreferenceHelper.getCurrency()) }
    var selectedCalendar by remember { mutableStateOf(PreferenceHelper.getPreferredCalender()) }

    var unitExpanded by remember { mutableStateOf(false) }
    var currencyExpanded by remember { mutableStateOf(false) }
    var calendarExpanded by remember { mutableStateOf(false) }

    val units = listOf(stringResource(R.string.zakat_unit_gram), stringResource(R.string.zakat_unit_tola))
    val currencies = listOf("PKR", "USD", "INR", "SAR", "AED")
    val calendars = listOf(AppConstants.CalendarType.HIJRI, AppConstants.CalendarType.GREGORIAN)

    // Zakat on Gold States
    var isGoldExpanded by remember { mutableStateOf(false) }
    var weight18K by remember { mutableStateOf("") }
    var weight21K by remember { mutableStateOf("") }
    var weight22K by remember { mutableStateOf("") }
    var weight24K by remember { mutableStateOf("") }
    
    // Zakat on Money States
    var isMoneyExpanded by remember { mutableStateOf(false) }
    var moneyAmount by remember { mutableStateOf("") }

    // Zakat on Silver States
    var isSilverExpanded by remember { mutableStateOf(false) }
    var silverPrice by remember { mutableStateOf("") }
    var silverWeight by remember { mutableStateOf("") }
    var silverUnit by remember { mutableStateOf(units.first()) }
    var silverUnitExpanded by remember { mutableStateOf(false) }
    
    var showResultDialog by remember { mutableStateOf(false) }
    var showUnderNisabDialog by remember { mutableStateOf(false) }
    var calculationResult by remember { mutableStateOf<ZakatCalculationModel?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = stringResource(R.string.zakat_calculator_title),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        ZakatInputField(
            value = goldPrice,
            onValueChange = { 
                goldPrice = it
                PreferenceHelper.setGoldPrice(it)
            },
            label = stringResource(R.string.gold_price),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            prefix = { Text("$selectedCurrency ", fontSize = 14.sp) }
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ExposedDropdownMenuBox(
                expanded = unitExpanded,
                onExpandedChange = { unitExpanded = !unitExpanded },
                modifier = Modifier.weight(1f)
            ) {
                ZakatInputField(
                    value = selectedUnit,
                    onValueChange = {},
                    readOnly = true,
                    label = stringResource(R.string.zakat_unit),
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = unitExpanded) },
                    modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable)
                )
                ExposedDropdownMenu(
                    expanded = unitExpanded,
                    onDismissRequest = { unitExpanded = false }
                ) {
                    units.forEach { unit ->
                        DropdownMenuItem(
                            text = { Text(unit, fontSize = 14.sp) },
                            onClick = {
                                selectedUnit = unit
                                PreferenceHelper.setGoldUnit(unit)
                                unitExpanded = false
                            }
                        )
                    }
                }
            }

            ExposedDropdownMenuBox(
                expanded = currencyExpanded,
                onExpandedChange = { currencyExpanded = !currencyExpanded },
                modifier = Modifier.weight(1f)
            ) {
                ZakatInputField(
                    value = selectedCurrency,
                    onValueChange = {},
                    readOnly = true,
                    label = stringResource(R.string.currency),
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = currencyExpanded) },
                    modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable)
                )
                ExposedDropdownMenu(
                    expanded = currencyExpanded,
                    onDismissRequest = { currencyExpanded = false }
                ) {
                    currencies.forEach { currency ->
                        DropdownMenuItem(
                            text = { Text(currency, fontSize = 14.sp) },
                            onClick = {
                                selectedCurrency = currency
                                PreferenceHelper.setCurrency(currency)
                                currencyExpanded = false
                            }
                        )
                    }
                }
            }
        }

        ExposedDropdownMenuBox(
            expanded = calendarExpanded,
            onExpandedChange = { calendarExpanded = !calendarExpanded },
            modifier = Modifier.fillMaxWidth()
        ) {
            ZakatInputField(
                value = selectedCalendar,
                onValueChange = {},
                readOnly = true,
                label = stringResource(R.string.zakat_calendar_type),
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = calendarExpanded) },
                modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth()
            )
            ExposedDropdownMenu(
                expanded = calendarExpanded,
                onDismissRequest = { calendarExpanded = false }
            ) {
                calendars.forEach { cal ->
                    DropdownMenuItem(
                        text = { Text(cal, fontSize = 14.sp) },
                        onClick = {
                            selectedCalendar = cal
                            PreferenceHelper.setPreferredCalender(selectedCalendar)
                            calendarExpanded = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Zakat on Gold Section
        ZakatOnGoldSection(
            isExpanded = isGoldExpanded,
            onToggle = { 
                isGoldExpanded = !isGoldExpanded 
                if (isGoldExpanded) {
                    isMoneyExpanded = false
                    isSilverExpanded = false
                }
            },
            weight18K = weight18K,
            onWeight18KChange = { weight18K = it },
            weight21K = weight21K,
            onWeight21KChange = { weight21K = it },
            weight22K = weight22K,
            onWeight22KChange = { weight22K = it },
            weight24K = weight24K,
            onWeight24KChange = { weight24K = it },
            onCalculate = {
                val price = goldPrice.toDoubleOrNull() ?: 0.0
                val pricePerGram = if (selectedUnit == context.getString(R.string.zakat_unit_tola)) price / 11.664 else price

                val result = ZFUtils.calculateZakatOnGold(
                    context = context,
                    goldPrice = pricePerGram,
                    gold24K = weight24K.toDoubleOrNull() ?: 0.0,
                    gold22K = weight22K.toDoubleOrNull() ?: 0.0,
                    gold21K = weight21K.toDoubleOrNull() ?: 0.0,
                    gold18K = weight18K.toDoubleOrNull() ?: 0.0
                )
                
                calculationResult = result
                if (result.totalNetWeight < ZFUtils.ZakatBusinessRules.nesabOfGoldInGrams) {
                    showUnderNisabDialog = true
                } else {
                    showResultDialog = true
                }
            }
        )

        // Zakat on Money Section
        ZakatOnMoneySection(
            isExpanded = isMoneyExpanded,
            onToggle = { 
                isMoneyExpanded = !isMoneyExpanded 
                if (isMoneyExpanded) {
                    isGoldExpanded = false
                    isSilverExpanded = false
                }
            },
            amount = moneyAmount,
            onAmountChange = { moneyAmount = it },
            currency = selectedCurrency,
            onCalculate = {
                val price = goldPrice.toDoubleOrNull() ?: 0.0
                val pricePerGram = if (selectedUnit == context.getString(R.string.zakat_unit_tola)) price / 11.664 else price
                val amount = moneyAmount.toDoubleOrNull() ?: 0.0

                val result = ZFUtils.calculateZakatOnMoney(
                    context = context,
                    goldPrice = pricePerGram,
                    money = amount
                )
                
                calculationResult = result
                if (result.totalAmount < result.nesabValue) {
                    showUnderNisabDialog = true
                } else {
                    showResultDialog = true
                }
            }
        )

        // Zakat on Silver Section
        ZakatOnSilverSection(
            isExpanded = isSilverExpanded,
            onToggle = { 
                isSilverExpanded = !isSilverExpanded 
                if (isSilverExpanded) {
                    isGoldExpanded = false
                    isMoneyExpanded = false
                }
            },
            price = silverPrice,
            onPriceChange = { silverPrice = it },
            weight = silverWeight,
            onWeightChange = { silverWeight = it },
            unit = silverUnit,
            onUnitChange = { silverUnit = it },
            unitExpanded = silverUnitExpanded,
            onUnitExpandedChange = { silverUnitExpanded = it },
            units = units,
            currency = selectedCurrency,
            onCalculate = {
                val sPrice = silverPrice.toDoubleOrNull() ?: 0.0
                val sPricePerGram = if (silverUnit == context.getString(R.string.zakat_unit_tola)) sPrice / 11.664 else sPrice
                val sWeight = silverWeight.toDoubleOrNull() ?: 0.0

                val result = ZFUtils.calculateZakatOnSilver(
                    context = context,
                    silverPrice = sPricePerGram,
                    weight = sWeight
                )
                
                calculationResult = result
                if (result.weight < result.nesabValue) {
                    showUnderNisabDialog = true
                } else {
                    showResultDialog = true
                }
            }
        )

        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = stringResource(R.string.zakat_calculator_instruction),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }

    if (showUnderNisabDialog) {
        val message = if (calculationResult?.zakatTypeEnum == com.qibla.locatorar.data.models.zakat.ZakatTypeEnum.ZakatOnGold) {
            stringResource(R.string.zakat_under_nisab_gold_msg)
        } else if (calculationResult?.zakatTypeEnum == com.qibla.locatorar.data.models.zakat.ZakatTypeEnum.ZakatOnSilver) {
            stringResource(R.string.zakat_under_nisab_silver_msg)
        } else {
            stringResource(R.string.zakat_under_nisab_money_msg, String.format(Locale.US, "%,.2f", calculationResult?.nesabValue ?: 0.0), selectedCurrency)
        }
        
        UnderNisabDialog(
            message = message,
            onDismiss = { showUnderNisabDialog = false },
            onContinue = {
                showUnderNisabDialog = false
                showResultDialog = true
            }
        )
    }

    if (showResultDialog && calculationResult != null) {
        ZakatResultDialog(
            result = calculationResult!!,
            onDismiss = { showResultDialog = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ZakatOnSilverSection(
    isExpanded: Boolean,
    onToggle: () -> Unit,
    price: String,
    onPriceChange: (String) -> Unit,
    weight: String,
    onWeightChange: (String) -> Unit,
    unit: String,
    onUnitChange: (String) -> Unit,
    unitExpanded: Boolean,
    onUnitExpandedChange: (Boolean) -> Unit,
    units: List<String>,
    currency: String,
    onCalculate: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        shape = MaterialTheme.shapes.medium
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggle() }
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
//                    Text(text = stringResource(R.string.zakat_on), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(text = stringResource(R.string.silver), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }
                Icon(
                    imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = if (isExpanded) stringResource(R.string.zakat_collapse) else stringResource(R.string.zakat_expand),
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            AnimatedVisibility(visible = isExpanded) {
                Column(
                    modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    Text(text = stringResource(R.string.zakat_enter_silver_details), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.secondary)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ZakatInputField(
                            value = price,
                            onValueChange = onPriceChange,
                            label = stringResource(R.string.silver_price),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            prefix = { Text("$currency ", fontSize = 14.sp) },
                            modifier = Modifier.weight(1.5f)
                        )

                        ExposedDropdownMenuBox(
                            expanded = unitExpanded,
                            onExpandedChange = onUnitExpandedChange,
                            modifier = Modifier.weight(1f)
                        ) {
                            ZakatInputField(
                                value = unit,
                                onValueChange = {},
                                readOnly = true,
                                label = stringResource(R.string.zakat_unit),
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = unitExpanded) },
                                modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable)
                            )
                            ExposedDropdownMenu(
                                expanded = unitExpanded,
                                onDismissRequest = { onUnitExpandedChange(false) }
                            ) {
                                units.forEach { u ->
                                    DropdownMenuItem(
                                        text = { Text(u, fontSize = 14.sp) },
                                        onClick = {
                                            onUnitChange(u)
                                            onUnitExpandedChange(false)
                                        }
                                    )
                                }
                            }
                        }
                    }

                    ZakatInputField(
                        value = weight,
                        onValueChange = onWeightChange,
                        label = stringResource(R.string.silver_weight),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        trailingIcon = { Text(stringResource(R.string.grams), fontSize = 12.sp, modifier = Modifier.padding(end = 8.dp)) }
                    )

                    Button(onClick = onCalculate, modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.small) {
                        Text(stringResource(R.string.calculate_zakat))
                    }
                }
            }
        }
    }
}

@Composable
fun ZakatOnMoneySection(
    isExpanded: Boolean,
    onToggle: () -> Unit,
    amount: String,
    onAmountChange: (String) -> Unit,
    currency: String,
    onCalculate: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        shape = MaterialTheme.shapes.medium
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggle() }
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
//                    Text(text = stringResource(R.string.zakat_on), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(text = stringResource(R.string.money), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }
                Icon(
                    imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = if (isExpanded) stringResource(R.string.zakat_collapse) else stringResource(R.string.zakat_expand),
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            AnimatedVisibility(visible = isExpanded) {
                Column(
                    modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    Text(text = stringResource(R.string.zakat_enter_money_amount), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.secondary)

                    ZakatInputField(
                        value = amount,
                        onValueChange = onAmountChange,
                        label = stringResource(R.string.amount_),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        prefix = { Text("$currency ", fontSize = 14.sp) }
                    )

                    Button(onClick = onCalculate, modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.small) {
                        Text(stringResource(R.string.calculate_zakat))
                    }
                }
            }
        }
    }
}

@Composable
fun ZakatOnGoldSection(
    isExpanded: Boolean,
    onToggle: () -> Unit,
    weight18K: String,
    onWeight18KChange: (String) -> Unit,
    weight21K: String,
    onWeight21KChange: (String) -> Unit,
    weight22K: String,
    onWeight22KChange: (String) -> Unit,
    weight24K: String,
    onWeight24KChange: (String) -> Unit,
    onCalculate: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        shape = MaterialTheme.shapes.medium
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggle() }
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
//                    Text(text = stringResource(R.string.zakat_on), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(text = stringResource(R.string.gold), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }
                Icon(
                    imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = if (isExpanded) stringResource(R.string.zakat_collapse) else stringResource(R.string.zakat_expand),
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            AnimatedVisibility(visible = isExpanded) {
                Column(
                    modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    Text(text = stringResource(R.string.zakat_enter_gold_weight_grams), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.secondary)

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ZakatWeightField(label = stringResource(R.string.weight_of_gold_18), value = weight18K, onValueChange = onWeight18KChange, modifier = Modifier.weight(1f))
                        ZakatWeightField(label = stringResource(R.string.weight_of_gold_21), value = weight21K, onValueChange = onWeight21KChange, modifier = Modifier.weight(1f))
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ZakatWeightField(label = stringResource(R.string.weight_of_gold_22), value = weight22K, onValueChange = onWeight22KChange, modifier = Modifier.weight(1f))
                        ZakatWeightField(label = stringResource(R.string.weight_of_gold_24), value = weight24K, onValueChange = onWeight24KChange, modifier = Modifier.weight(1f))
                    }

                    Button(onClick = onCalculate, modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.small) {
                        Text(stringResource(R.string.calculate_zakat))
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ZakatWeightField(label: String, value: String, onValueChange: (String) -> Unit, modifier: Modifier = Modifier) {
    val interactionSource = remember { MutableInteractionSource() }
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.height(42.dp).fillMaxWidth(),
        interactionSource = interactionSource,
        singleLine = true,
        textStyle = TextStyle(fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        decorationBox = @Composable { innerTextField ->
            OutlinedTextFieldDefaults.DecorationBox(
                value = value, visualTransformation = VisualTransformation.None, innerTextField = innerTextField,
                placeholder = null, label = { Text(label, fontSize = 10.sp) }, singleLine = true, enabled = true,
                isError = false, interactionSource = interactionSource, colors = OutlinedTextFieldDefaults.colors(),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                container = {
                    OutlinedTextFieldDefaults.Container(
                        enabled = true, isError = false, interactionSource = interactionSource,
                        colors = OutlinedTextFieldDefaults.colors(), shape = OutlinedTextFieldDefaults.shape
                    )
                }
            )
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ZakatInputField(value: String, onValueChange: (String) -> Unit, label: String, modifier: Modifier = Modifier, readOnly: Boolean = false, prefix: @Composable (() -> Unit)? = null, trailingIcon: @Composable (() -> Unit)? = null, keyboardOptions: KeyboardOptions = KeyboardOptions.Default) {
    val interactionSource = remember { MutableInteractionSource() }
    BasicTextField(
        value = value, onValueChange = onValueChange, modifier = modifier.height(42.dp).fillMaxWidth(),
        interactionSource = interactionSource, readOnly = readOnly, singleLine = true,
        textStyle = TextStyle(fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface),
        keyboardOptions = keyboardOptions,
        decorationBox = @Composable { innerTextField ->
            OutlinedTextFieldDefaults.DecorationBox(
                value = value, visualTransformation = VisualTransformation.None, innerTextField = innerTextField,
                placeholder = null, label = { Text(label, fontSize = 11.sp) }, trailingIcon = trailingIcon,
                prefix = prefix, singleLine = true, enabled = true, isError = false, interactionSource = interactionSource,
                colors = OutlinedTextFieldDefaults.colors(), contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                container = {
                    OutlinedTextFieldDefaults.Container(
                        enabled = true, isError = false, interactionSource = interactionSource,
                        colors = OutlinedTextFieldDefaults.colors(), shape = OutlinedTextFieldDefaults.shape
                    )
                }
            )
        }
    )
}
