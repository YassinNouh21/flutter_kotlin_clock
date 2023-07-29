package com.example.first_project_clock

import android.os.CountDownTimer
import android.util.Log
import androidx.annotation.NonNull
import androidx.lifecycle.lifecycleScope
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.EventChannel
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
    private val eventChannel = "com.example.timer_app/event_channel/timer"

    override fun configureFlutterEngine(@NonNull flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)
        EventChannel(flutterEngine.dartExecutor.binaryMessenger, eventChannel).setStreamHandler(
            CounterHandler
        )
    }

    object CounterHandler : EventChannel.StreamHandler {

        private var eventSink: EventChannel.EventSink? = null
        override fun onListen(p0: Any?, sink: EventChannel.EventSink) {
            eventSink = sink
            val timer = object : CountDownTimer(5000, 1000) {
                override fun onTick(p0: Long) {
                    // sink in the event channel
                    eventSink?.success((p0 / 1000).toString())
                    Log.e("onTick: ", (p0 / 1000).toString())
                }

                override fun onFinish() {
                    Log.e("onTick: ", "Done")
                    // Emit the state once the timer finishes
                    eventSink?.success("Done")
                }
            }
            timer.start()
        }

        override fun onCancel(p0: Any?) {
            eventSink = null
        }
    }

    private fun startTimer(): Flow<String?> = channelFlow {
        var state: String? = null
        val timer = object : CountDownTimer(5000, 1000) {
            override fun onTick(p0: Long) {
                // sink in the event channel
                Log.e("onTick: ", (p0 / 1000).toString())
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
        awaitClose { timer.cancel() }

    }
}
