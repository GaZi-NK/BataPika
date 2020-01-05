package com.example.redma.batapika

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import androidx.core.content.getSystemService

//センサーを使用するためセンサーインターフェースを実装
class MainActivity : AppCompatActivity(), SensorEventListener {
    private val threshold: Float = 10f  //センサーの閾値(z軸)⇒10以上傾いたら端末が倒れたと判断
    private var oldValue: Float = 0f    //以前の加速度センサーのz軸を記録
    private lateinit var cameraManager: CameraManager   //onCereateで初期化したいためlateinit使用
    private lateinit var  cameraID: String //カメラのID
    private var lightOn: Boolean = false //ライトがonかoffか⇒falseはオフ

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
    }

    override fun onSensorChanged(event: SensorEvent?) {
        //センサーに何らかの値が入っていること確認⇒入っていなかったら返す
        if (event == null) return
        //センサーが加速度センサーの時に実行
        if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            //z軸と保存してあるz軸の差分を出す。abs=絶対値を出す
            val zDiff = Math.abs(event.values[2] - oldValue)
            //zDiffが閾値より高ければLEDライトON
            if (zDiff > threshold) {
                torchOn()
            }
            oldValue = event.values[2] //TODO⇒意味が分かったらコメント追記
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        //カメラマネージャーのインスタンスを取得⇒戻り値がObjecta型なのでキャスト忘れずに
        cameraManager = getSystemService(Context.CAMERA_SERVICE) as CameraManager
        cameraManager.registerTorchCallback(object : CameraManager.TorchCallback() {
            //トーチモードが変更された時の処理
            override fun onTorchModeChanged(cameraId: String, enabled: Boolean) {
                super.onTorchModeChanged(cameraId, enabled)
                cameraID = cameraId //カメラIDを取得
                lightOn = enabled   //現在のライトの状態を取得
            }
        }, Handler())
    }

    override fun onResume() {
        super.onResume()
        //センサーマネージャーインスタンスを取得
        val sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        //センサーオブジェクト取得⇒引数はセンサーのタイプ
        val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        //イベントリスナー登録、引数(リスナーオブジェクト、センサーオブジェクト、センサーからのデータ取得レート)
        // ↑これをしておかないとセンサーが実際に値を検出してもAndroidがイベントとしてアプリケーションに通知してくれない＝onSensorChangedgagaが呼ばれない。
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL)
    }

    override fun onPause() {
        super.onPause()
        //センサーマネージャを取得してリスナーを解除
        val sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        sensorManager.unregisterListener(this)

        //ライトが点滅中だったら消す処理
        if (cameraID != null) {
            try {
                if (lightOn) {
                    cameraManager.setTorchMode(cameraID, false)
                }
            }catch (e: CameraAccessException){
                e.printStackTrace()
            }
        }
    }

    private fun torchOn() {
        //カメラがあるか確認
        if (cameraID != null) {
            try {
                if (!lightOn) {  //ライトが未起動であればライトON
                    cameraManager.setTorchMode(cameraID, true)
                } else {  //ライトが起動中であればライトOFF
                    cameraManager.setTorchMode(cameraID, false)
                }
            } catch (e: CameraAccessException) {
                e.printStackTrace()
            }
        }
    }
}
