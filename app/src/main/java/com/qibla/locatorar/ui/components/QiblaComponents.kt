package com.qibla.locatorar.ui.components

import androidx.compose.ui.res.stringResource
import com.qibla.locatorar.R
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.qibla.locatorar.utils.AppUtils
import kotlin.math.sin

data class CompassState(val heading: Float, val accuracy: Int)

@Composable
fun rememberCompassState(): CompassState {
    val context = LocalContext.current
    var heading by remember { mutableFloatStateOf(0f) }
    var accuracy by remember { mutableIntStateOf(SensorManager.SENSOR_STATUS_ACCURACY_HIGH) }
    
    DisposableEffect(Unit) {
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        
        val rotationVectorSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
        val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        val magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

        val rotationMatrix = FloatArray(9)
        val orientationAngles = FloatArray(3)
        
        val listener = object : SensorEventListener {
            private var gravity = FloatArray(3)
            private var geomagnetic = FloatArray(3)
            private var hasGravity = false
            private var hasGeomagnetic = false

            override fun onSensorChanged(event: SensorEvent) {
                if (event.sensor.type == Sensor.TYPE_ROTATION_VECTOR) {
                    SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
                    SensorManager.getOrientation(rotationMatrix, orientationAngles)
                    val azimuth = Math.toDegrees(orientationAngles[0].toDouble()).toFloat()
                    heading = (azimuth + 360f) % 360f
                } else {
                    when (event.sensor.type) {
                        Sensor.TYPE_ACCELEROMETER -> {
                            AppUtils.lowPass(event.values, gravity)
                            hasGravity = true
                        }
                        Sensor.TYPE_MAGNETIC_FIELD -> {
                            AppUtils.lowPass(event.values, geomagnetic)
                            hasGeomagnetic = true
                        }
                    }

                    if (hasGravity && hasGeomagnetic) {
                        if (SensorManager.getRotationMatrix(rotationMatrix, null, gravity, geomagnetic)) {
                            SensorManager.getOrientation(rotationMatrix, orientationAngles)
                            val azimuth = Math.toDegrees(orientationAngles[0].toDouble()).toFloat()
                            heading = (azimuth + 360f) % 360f
                        }
                    }
                }
            }

            override fun onAccuracyChanged(sensor: Sensor?, newAccuracy: Int) {
                if (sensor?.type == Sensor.TYPE_MAGNETIC_FIELD || sensor?.type == Sensor.TYPE_ROTATION_VECTOR) {
                    accuracy = newAccuracy
                }
            }
        }

        if (rotationVectorSensor != null) {
            sensorManager.registerListener(listener, rotationVectorSensor, SensorManager.SENSOR_DELAY_UI)
        } else {
            accelerometer?.also { sensorManager.registerListener(listener, it, SensorManager.SENSOR_DELAY_UI) }
            magnetometer?.also { sensorManager.registerListener(listener, it, SensorManager.SENSOR_DELAY_UI) }
        }

        onDispose {
            sensorManager.unregisterListener(listener)
        }
    }
    return CompassState(heading, accuracy)
}

@Composable
fun CalibrationDialog(accuracy: Int, onDismiss: () -> Unit) {
    if (accuracy == SensorManager.SENSOR_STATUS_ACCURACY_LOW || accuracy == SensorManager.SENSOR_STATUS_UNRELIABLE) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = {
                Text(
                    stringResource(R.string.calibrate_compass),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        stringResource(R.string.calibration_instruction),
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    
                    FigureEightAnimation()
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(stringResource(R.string.compass_accuracy), style = MaterialTheme.typography.bodySmall)
                        Text(
                            if (accuracy == SensorManager.SENSOR_STATUS_UNRELIABLE) stringResource(R.string.accuracy_unreliable) else stringResource(R.string.accuracy_low),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = onDismiss) {
                    Text(stringResource(R.string.done_all_caps))
                }
            }
        )
    }
}

@Composable
fun FigureEightAnimation() {
    val infiniteTransition = rememberInfiniteTransition(label = "FigureEightTransition")
    val progress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "FigureEightProgress"
    )

    Canvas(modifier = Modifier.size(200.dp, 120.dp)) {
        val w = size.width
        val h = size.height
        val centerX = w / 2f
        val centerY = h / 2f
        
        val path = Path().apply {
            val a = w * 0.4f
            val b = h * 0.35f
            
            for (t in 0..360) {
                val rad = Math.toRadians(t.toDouble())
                val x = centerX + a * sin(rad).toFloat()
                val y = centerY + b * sin(2 * rad).toFloat()
                if (t == 0) moveTo(x, y) else lineTo(x, y)
            }
            close()
        }

        drawPath(
            path = path,
            color = Color.LightGray.copy(alpha = 0.5f),
            style = Stroke(
                width = 2.dp.toPx(),
                pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
            )
        )

        val pathMeasure = PathMeasure()
        pathMeasure.setPath(path, false)
        val position = pathMeasure.getPosition(progress * pathMeasure.length)
        
        // Draw "Phone" representation
        val phoneW = 40.dp.toPx()
        val phoneH = 20.dp.toPx()
        
        drawRect(
            color = Color.DarkGray,
            topLeft = Offset(position.x - phoneW / 2, position.y - phoneH / 2),
            size = androidx.compose.ui.geometry.Size(phoneW, phoneH),
            style = Stroke(width = 2.dp.toPx())
        )
        drawRect(
            color = Color.Black,
            topLeft = Offset(position.x - phoneW / 2 + 2.dp.toPx(), position.y - phoneH / 2 + 2.dp.toPx()),
            size = androidx.compose.ui.geometry.Size(phoneW - 4.dp.toPx(), phoneH - 4.dp.toPx())
        )
        
        drawCircle(
            color = Color.Cyan,
            radius = 4.dp.toPx(),
            center = position
        )
    }
}
