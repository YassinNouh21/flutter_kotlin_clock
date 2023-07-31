package com.example.first_project_clock

import android.Manifest
import android.os.Build
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
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.tensorflow.lite.task.audio.classifier.AudioClassifier
import java.util.Timer
import kotlin.concurrent.scheduleAtFixedRate

class MainActivity : FlutterActivity() {
    var modelPath = "lite-model_yamnet_classification_tflite_1.tflite"
    var probabilityThreshold: Float = 0.3f

    private val CHANNEL = "com.example.timer_app/method_channel/timer"
    private val eventChannel = "com.example.timer_app/event_channel/timer"

    override fun configureFlutterEngine(@NonNull flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)
        MethodChannel(
            flutterEngine.dartExecutor.binaryMessenger, CHANNEL
        ).setMethodCallHandler { call, result ->
            if (call.method == "startTimer") {
                // startTimer()
//                lifecycleScope.launch {
//                    startTimer().collect {
//                        result.success(it)
//                    }
//                }
                audioClassificationWithInvokedMethod()
            } else {
                result.notImplemented()
            }
        }
//        EventChannel(flutterEngine.dartExecutor.binaryMessenger, eventChannel).setStreamHandler(
//            CounterHandler
//        )

    }

//    fun audioClassification(): Flow<String?> = channelFlow {
//
//        val REQUEST_RECORD_AUDIO = 1337
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            requestPermissions(arrayOf(Manifest.permission.RECORD_AUDIO), REQUEST_RECORD_AUDIO)
//        }
//        // TODO 2.3: Loading the model from the assets folder
//        val classifier = AudioClassifier.createFromFile(this@MainActivity, modelPath)
//        val tensor = classifier.createInputTensorAudio()
//        val format = classifier.requiredTensorAudioFormat
//        val recorderSpecs = "Number Of Channels: ${format.channels}\n" +
//                "Sample Rate: ${format.sampleRate}"
//        Log.e("recorderSpecs: ", recorderSpecs)
//
//        val record = classifier.createAudioRecord()
//        record.startRecording()
//
//        Timer().scheduleAtFixedRate(1, 500) {
//            // TODO 4.1: Classifying audio data
//
//            val numberOfSamples = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//                tensor.load(record)
//            } else {
//                TODO("VERSION.SDK_INT < M")
//            }
//            val output = classifier.classify(tensor)
//
//            // TODO 4.2: Filtering out classifications with low probability
//            val filteredModelOutput = output[0].categories.filter {
//                it.score > probabilityThreshold
//            }
//
//            // TODO 4.3: Creating a multiline string with the filtered results
//            val outputStr =
//                filteredModelOutput.sortedBy { -it.score }
//                    .joinToString(separator = "\n") { "${it.label} -> ${it.score} " }
//
//            lifecycleScope.launch {
//                send(outputStr)
//            }
//
//        }
//
//    }

//
private fun audioClassificationWithInvokedMethod() {

    val REQUEST_RECORD_AUDIO = 1337
    requestPermissions(arrayOf(Manifest.permission.RECORD_AUDIO), REQUEST_RECORD_AUDIO)
    // TODO 2.3: Loading the model from the assets folder
    val classifier = AudioClassifier.createFromFile(this, modelPath)
    val tensor = classifier.createInputTensorAudio()
    val format = classifier.requiredTensorAudioFormat
    val recorderSpecs = "Number Of Channels: ${format.channels}\n" +
            "Sample Rate: ${format.sampleRate}"
    Log.e("recorderSpecs: ", recorderSpecs)

    val record = classifier.createAudioRecord()
    record.startRecording()


    Timer().scheduleAtFixedRate(1, 500) {

        // TODO 4.1: Classifing audio data
        val numberOfSamples = tensor.load(record)

        val output = classifier.classify(tensor)

        // TODO 4.2: Filtering out classifications with low probability
        val filteredModelOutput = output[0].categories.filter {
            it.score > probabilityThreshold
        }

        // TODO 4.3: Creating a multiline string with the filtered results
        val outputStr =
            filteredModelOutput.sortedBy { -it.score }
                .joinToString(separator = "\n") { "${it.label} -> ${it.score} " }
        Log.e("audioClassification", outputStr.toString())
    }

}

    object CounterHandler : EventChannel.StreamHandler  {
        private var eventSink: EventChannel.EventSink? = null
        override fun onListen(p0: Any?, sink: EventChannel.EventSink) {
            eventSink = sink

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
