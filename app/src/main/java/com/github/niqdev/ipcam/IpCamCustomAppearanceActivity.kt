package com.github.niqdev.ipcam

import android.graphics.Color
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.LinearLayout
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

/**
 * Activity to show the possibilities of transparent stream background
 * and the actions which need to be done when hiding and showing the
 * stream with transparent background
 */
class IpCamCustomAppearanceActivity : AppCompatActivity() {
    @JvmField
    @BindView(R.id.mjpegViewCustomAppearance)
    var mjpegView: MjpegView? = null

    @JvmField
    @BindView(R.id.layoutProgressWrapper)
    var progressWrapper: LinearLayout? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ipcam_custom_appearance)
        ButterKnife.bind(this)
    }

    private fun loadIpCam() {
        progressWrapper!!.visibility = View.VISIBLE
        CoroutineScope(Dispatchers.IO).launch {
            MjpegK()
                .open("http://62.176.195.157:80/mjpg/video.mjpg", TIMEOUT)
                .collect { inputStream ->
                    progressWrapper!!.visibility = View.GONE
                    mjpegView!!.setFpsOverlayBackgroundColor(Color.DKGRAY)
                    mjpegView!!.setFpsOverlayTextColor(Color.WHITE)
                    mjpegView!!.setSource(inputStream)
                    mjpegView!!.setDisplayMode(DisplayMode.BEST_FIT)
                    mjpegView!!.showFps(true)
                }
        }

//            ) { throwable ->
//                Log.e(javaClass.simpleName, "mjpeg error", throwable)
//                Toast.makeText(this, "Error", Toast.LENGTH_LONG).show()
//            }
    }

    override fun onResume() {
        super.onResume()
        loadIpCam()
    }

    override fun onPause() {
        super.onPause()
        mjpegView!!.stopPlayback()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_toggleStream -> {
                if ((mjpegView as View?)!!.visibility == View.VISIBLE) {
                    mjpegView!!.stopPlayback()
                    mjpegView!!.clearStream()
                    mjpegView!!.resetTransparentBackground()
                    (mjpegView as View?)!!.visibility = View.GONE
                    item.setIcon(R.drawable.ic_videocam_white_48dp)
                    item.title = getString(R.string.menu_toggleStreamOn)
                } else {
                    mjpegView!!.setTransparentBackground()
                    (mjpegView as View?)!!.visibility = View.VISIBLE
                    item.setIcon(R.drawable.ic_videocam_off_white_48dp)
                    item.title = getString(R.string.menu_toggleStreamOff)
                    loadIpCam()
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_custom_appearance, menu)
        return true
    }

    companion object {
        private const val TIMEOUT = 5_000L
    }
}