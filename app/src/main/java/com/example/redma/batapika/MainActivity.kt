package com.example.redma.batapika

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.camera2.CameraManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

//センサーを使用するためセンサーインターフェースを実装
class MainActivity : AppCompatActivity(),SensorEventListener {
    private val threshold: Float = 10f  //センサーの閾値(z軸)⇒10以上傾いたら端末が倒れたと判断
    private var oldValue: Float = 0f    //以前の加速度センサーのz軸を記録
    private lateinit var cameraManager: CameraManager   //onCereateで初期化したいためlateinit使用
    private var cameraID: String? = null //カメラのID
    private var lightOn: Boolean = false //ライトがonかoffか⇒falseはオフ

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onSensorChanged(event: SensorEvent?) {
        //センサーに何らかの値が入っていること確認⇒入っていなかったら返す
        if(event == null) return
        //センサーが加速度センサーの時に実行
        if(event.sensor.type == Sensor.TYPE_ACCELEROMETER){
            //z軸と保存してあるz軸の差分を出す。abs=絶対値を出す
            val zDiff = Math.abs(event.values[2] - oldValue)
            //zDiffが閾値より高ければLEDライトON
            if (zDiff > threshold){
                torchOn()
            }
            oldValue = event.values[2]
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }
}
