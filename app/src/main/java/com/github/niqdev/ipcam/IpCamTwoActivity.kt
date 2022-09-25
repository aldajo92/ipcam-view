package com.github.niqdev.ipcam

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import butterknife.BindView
import butterknife.ButterKnife
import com.github.niqdev.mjpeg.DisplayMode
import com.github.niqdev.mjpeg.MjpegK
import com.github.niqdev.mjpeg.MjpegView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class IpCamTwoActivity : AppCompatActivity() {
    @JvmField
    @BindView(R.id.mjpegViewDefault1)
    var mjpegView1: MjpegView? = null

    @JvmField
    @BindView(R.id.mjpegViewDefault2)
    var mjpegView2: MjpegView? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ipcam_two_camera)
        ButterKnife.bind(this)
    }

    private fun loadIpCam1() {
        CoroutineScope(Dispatchers.IO).launch {
            MjpegK()
                .open("http://50.244.186.65:8081/mjpg/video.mjpg", TIMEOUT)
                .collect { inputStream ->
                    mjpegView1!!.setSource(inputStream)
                    mjpegView1!!.setDisplayMode(DisplayMode.BEST_FIT)
                    mjpegView1!!.showFps(true)
                }
        }
//        { throwable ->
//                Log.e(javaClass.simpleName, "mjpeg error", throwable)
//                Toast.makeText(this, "Error", Toast.LENGTH_LONG).show()
//            }
    }

    private fun loadIpCam2() {
        CoroutineScope(Dispatchers.IO).launch {
            MjpegK()
                .open("http://iris.not.iac.es/axis-cgi/mjpg/video.cgi?resolution=320x240", TIMEOUT)
                .collect { inputStream ->
                    mjpegView2!!.setSource(inputStream)
                    mjpegView2!!.setDisplayMode(DisplayMode.BEST_FIT)
                    mjpegView2!!.showFps(true)
                }
        }
//            ) { throwable ->
//                Log.e(javaClass.simpleName, "mjpeg error", throwable)
//                Toast.makeText(this, "Error", Toast.LENGTH_LONG).show()
//            }
    }

    override fun onResume() {
        super.onResume()
        loadIpCam1()
        loadIpCam2()
    }

    override fun onPause() {
        super.onPause()
        mjpegView1!!.stopPlayback()
        mjpegView2!!.stopPlayback()
    }

    companion object {
        private const val TIMEOUT = 5_000L
    }
}