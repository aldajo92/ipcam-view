package com.github.niqdev.ipcam

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import butterknife.BindView
import butterknife.ButterKnife
import com.github.niqdev.mjpeg.DisplayMode
import com.github.niqdev.mjpeg.MjpegK
import com.github.niqdev.mjpeg.MjpegKType
import com.github.niqdev.mjpeg.MjpegView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class IpCamNativeActivity : AppCompatActivity() {
    @JvmField
    @BindView(R.id.mjpegViewNative)
    var mjpegView: MjpegView? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ipcam_native)
        ButterKnife.bind(this)

        // TODO if (mjpegView != null) mjpegView.setResolution(width, height);
        loadIpcam()
    }

    private fun loadIpcam() {
        CoroutineScope(Dispatchers.IO).launch {
            MjpegK(MjpegKType.NATIVE) //.credential("", "")
                .open("http://wmccpinetop.axiscam.net/mjpg/video.mjpg")
                .collect { inputStream ->
                    withContext(Dispatchers.Main) {
                        mjpegView!!.setSource(inputStream)
                        // TODO if (inputStream != null) inputStream.setSkip(1)
                        mjpegView!!.setDisplayMode(DisplayMode.BEST_FIT)
                        mjpegView!!.showFps(true)
                    }
                }
        }
    }

    override fun onResume() {
        super.onResume()

        /* TODO
        if (mjpegView != null) {
            if (suspending) {
                new DoRead().execute(URL);
                suspending = false;
            }
        }
        */
    }

    override fun onPause() {
        super.onPause()
        if (mjpegView != null) {
            if (mjpegView!!.isStreaming) {
                mjpegView!!.stopPlayback()
                //suspending = true;
            }
        }
    }

    override fun onDestroy() {
        if (mjpegView != null) {
            mjpegView!!.freeCameraMemory()
        }
        super.onDestroy()
    }
}