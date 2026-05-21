package com.example

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import com.example.ui.StepDashboardScreen
import com.example.ui.StepViewModel
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity(), SensorEventListener {

  private val viewModel: StepViewModel by viewModels()
  private var sensorManager: SensorManager? = null
  private var stepDetectorSensor: Sensor? = null

  private val requestPermissionLauncher = registerForActivityResult(
    ActivityResultContracts.RequestPermission()
  ) { isGranted: Boolean ->
    if (isGranted) {
      registerStepSensor()
    } else {
      Toast.makeText(this, "Stride is running in simulation mode.", Toast.LENGTH_SHORT).show()
      viewModel.setSensorStatus(available = stepDetectorSensor != null, registered = false)
    }
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
    stepDetectorSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR)

    setContent {
      MyApplicationTheme {
        Surface(
          modifier = Modifier.fillMaxSize(),
          color = MaterialTheme.colorScheme.background
        ) {
          StepDashboardScreen(
            viewModel = viewModel,
            onRequestPermission = { checkAndRequestPermission() }
          )
        }
      }
    }

    viewModel.setSensorStatus(available = stepDetectorSensor != null, registered = false)
    checkAndRequestPermission()
  }

  private fun checkAndRequestPermission() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
      when {
        ContextCompat.checkSelfPermission(
          this,
          Manifest.permission.ACTIVITY_RECOGNITION
        ) == PackageManager.PERMISSION_GRANTED -> {
          registerStepSensor()
        }
        else -> {
          requestPermissionLauncher.launch(Manifest.permission.ACTIVITY_RECOGNITION)
        }
      }
    } else {
      registerStepSensor()
    }
  }

  private fun registerStepSensor() {
    val sensor = stepDetectorSensor
    if (sensor != null && sensorManager != null) {
      val registered = sensorManager?.registerListener(this, sensor, SensorManager.SENSOR_DELAY_UI) == true
      viewModel.setSensorStatus(available = true, registered = registered)
    } else {
      viewModel.setSensorStatus(available = false, registered = false)
    }
  }

  override fun onSensorChanged(event: SensorEvent?) {
    if (event?.sensor?.type == Sensor.TYPE_STEP_DETECTOR) {
      viewModel.addSteps(1)
    }
  }

  override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
    // No-op
  }

  override fun onResume() {
    super.onResume()
    registerStepSensor()
  }

  override fun onPause() {
    super.onPause()
    sensorManager?.unregisterListener(this)
    viewModel.setSensorStatus(available = stepDetectorSensor != null, registered = false)
  }
}
