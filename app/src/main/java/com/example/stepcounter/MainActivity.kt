package com.example.stepcounter

import android.Manifest
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import android.widget.Toast
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), SensorEventListener {

    private var sensorManager: SensorManager? = null

    private var active = false
    private var isStart = true
    private var chronoStart: Boolean = false
    private var totalSteps = 0f
    private var previousTotalSteps = 0f
    private var lastPause: Long = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Dexter.withContext(this)
            .withPermission(Manifest.permission.ACTIVITY_RECOGNITION)
            .withListener(object : PermissionListener {
                override fun onPermissionGranted(p0: PermissionGrantedResponse?) {
                    btn.setOnClickListener {
                        if (isStart) {
                            btn.text = "STOP"
                            active = true
                            sensorManager =
                                getSystemService(Context.SENSOR_SERVICE) as SensorManager
                            val stepSensor =
                                sensorManager?.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
                            if (stepSensor == null) {
                                Toast.makeText(
                                    this@MainActivity,
                                    "No sensor detected on this device",
                                    Toast.LENGTH_LONG
                                ).show()

                            } else {
                                sensorManager?.registerListener(
                                    this@MainActivity,
                                    stepSensor,
                                    SensorManager.SENSOR_DELAY_UI
                                )
                            }
                            if (!chronoStart) {
                                chronometer.start()
                                chronoStart = true
                            }else{
                                chronometer.setBase(chronometer.getBase()
                                        + SystemClock.elapsedRealtime()
                                        - lastPause)
                                chronometer.start()
                            }
                            isStart = false

                        } else if (!isStart) {
                            btn.text = "START"
                            active = false
                            sensorManager?.unregisterListener(this@MainActivity)
                            lastPause = SystemClock.elapsedRealtime()
                            chronometer.stop()
                            isStart = true
                        }

                    }

                    btn.setOnLongClickListener {
                        chronometer.base = lastPause
                        chronometer.text = "00:00"
                        previousTotalSteps = 0f
                        tv2.text = "0"
                        destination_tv.text = "0"
                        kcal_tv.text = "0"

                        return@setOnLongClickListener true

                    }
                }

                override fun onPermissionDenied(p0: PermissionDeniedResponse?) {
                    Toast.makeText(
                        this@MainActivity,
                        "This app will not work properly if you do not allow it",
                        Toast.LENGTH_LONG
                    )
                        .show()
                    this@MainActivity.finish()
                }

                override fun onPermissionRationaleShouldBeShown(
                    p0: PermissionRequest?,
                    p1: PermissionToken?
                ) {
                    p1?.continuePermissionRequest()
                }

            }).check()


    }

    override fun onDestroy() {
        super.onDestroy()
        sensorManager?.unregisterListener(this@MainActivity)
    }

    fun kcalories(step: Float): Int {
        return (step * 0.05).toInt()
    }

    fun distance(step: Float): Int {
        return (step * 0.7).toInt()
    }

    override fun onSensorChanged(p0: SensorEvent?) {

        if (active) {
            if (previousTotalSteps == 0f) {
                previousTotalSteps = p0?.values!![0]
            }
            totalSteps = p0!!.values[0] - previousTotalSteps
            tv2.text = "${totalSteps.toInt()}"

            val kcalories = kcalories(totalSteps)
            kcal_tv.text = "$kcalories"

            val distance = distance(totalSteps)
            destination_tv.text = "$distance"

            progressBar.setProgressWithAnimation(totalSteps.toInt().toFloat())
        }
    }

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {

    }


}