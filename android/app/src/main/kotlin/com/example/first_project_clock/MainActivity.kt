package com.example.first_project_clock

import android.os.CountDownTimer
import android.util.Log
import androidx.annotation.NonNull
import androidx.lifecycle.lifecycleScope
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.MethodChannel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch

class MainActivity : FlutterActivity() {

    private val CHANNEL = "com.example.timer_app/timer"

    override fun configureFlutterEngine(@NonNull flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)
        MethodChannel(
            flutterEngine.dartExecutor.binaryMessenger,
            CHANNEL
        ).setMethodCallHandler { call, result ->
            if (call.method == "startTimer") {
                val timerFlow: Flow<String?> = startTimer()
                CoroutineScope(Dispatchers.Main).launch {
                    timerFlow.collect { state ->
                        Log.e("onTick: ", state.toString())
                        if (state != null) {
                            result.success(state)
                        } else {
                            result.error("Timer is Null", "Timer is Null", null)
                        }
                    }
                }

            } else {
                result.notImplemented()
            }

        }
    }

    private fun startTimer(): Flow<String?> = channelFlow  {
        var state: String? = null
        val timer = object : CountDownTimer(20000, 1000) {
            override fun onTick(p0: Long) {
                Log.e("onTick: ", p0.toString())
            }

            override fun onFinish() {
                Log.e("onTick: ", "Done")
                state = "Done"
                // Emit the state once the timer finishes
                lifecycleScope.launch {
                   send(state)
                    close() 
                }

            }
        }
        timer.start()
        awaitClose{ timer.cancel() }

    }
}
