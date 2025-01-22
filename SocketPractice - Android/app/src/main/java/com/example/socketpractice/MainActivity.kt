package com.example.socketpractice

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.PrintWriter
import java.net.Socket

class MainActivity : AppCompatActivity(), SensorEventListener {

    private lateinit var messageInput: EditText
    private lateinit var sendButton: Button
    private lateinit var chatHistory: TextView

    private var socket: Socket? = null
    private var writer: PrintWriter? = null

    private var sensorManager: SensorManager? = null
    private var lightSensor: Sensor? = null
    private var currentLight: Float = 0.0f  // 현재 밝기 값

    private val handler = Handler()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        messageInput = findViewById(R.id.messageInput)
        sendButton = findViewById(R.id.sendButton)
        chatHistory = findViewById(R.id.chatHistory)

        // 서버와 연결
        connectToServer()

        // 센서 매니저와 밝기 센서 초기화
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        lightSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_LIGHT)

        // 센서 등록
        sensorManager?.registerListener(this, lightSensor, SensorManager.SENSOR_DELAY_UI)

        sendButton.setOnClickListener {
            val message = messageInput.text.toString()
            if (message.isNotEmpty()) {
                sendMessage(message)
                messageInput.text.clear()
            }
        }

        // 1초마다 밝기 정보를 서버로 전송
        startSendingLightData()
    }

    private fun connectToServer() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // 서버의 IP와 포트를 지정
                socket = Socket("172.17.4.163", 12346)
                writer = PrintWriter(socket!!.getOutputStream(), true)

                runOnUiThread {
                    chatHistory.append("서버에 연결되었습니다.\n")
                }

                // 메시지 수신 대기
                val reader = socket!!.getInputStream().bufferedReader()
                while (true) {
                    val message = reader.readLine() ?: break
                    runOnUiThread {
                        chatHistory.append("서버: $message\n")
                    }
                }
            } catch (e: Exception) {
                Log.e("SocketError", "서버 연결 실패: ${e.message}")
            }
        }
    }

    private fun sendMessage(message: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // writer가 null이 아닌 경우에만 전송
                writer?.println(message)
                runOnUiThread {
                    chatHistory.append("나: $message\n")
                }
            } catch (e: Exception) {
                Log.e("SocketError", "메시지 전송 실패: ${e.message}")
            }
        }
    }

    private fun startSendingLightData() {
        handler.postDelayed(object : Runnable {
            override fun run() {
                // writer가 null이 아니면 메시지 전송
                writer?.let {
                    sendMessage("$currentLight")
                }
                handler.postDelayed(this, 100)  // 1초마다 반복
            }
        }, 100)
    }

    // 센서 값 업데이트
    override fun onSensorChanged(event: SensorEvent?) {
        if (event != null && event.sensor.type == Sensor.TYPE_LIGHT) {
            currentLight = event.values[0]  // 밝기 값 저장
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // 정확도 변화에 대한 처리는 여기서
    }

    override fun onDestroy() {
        super.onDestroy()
        sensorManager?.unregisterListener(this)  // 센서 리스너 해제
        socket?.close()
    }
}
