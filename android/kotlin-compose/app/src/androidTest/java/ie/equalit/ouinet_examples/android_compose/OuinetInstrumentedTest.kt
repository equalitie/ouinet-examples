package ie.equalit.ouinet_examples.android_compose

import android.util.Log
import androidx.test.annotation.UiThreadTest
import ie.equalit.ouinet_examples.android_compose.components.Ouinet

import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import ie.equalit.ouinet.OuinetBackground

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
@UiThreadTest
class OuinetInstrumentedTest {

    private fun ouinetBackground() : OuinetBackground {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        val ouinet = Ouinet(appContext)
        ouinet.setBackground(appContext)
        return ouinet.background
    }

    private fun ouinetWaitForStarted(background: OuinetBackground) {
        var i = 0
        while (i < 30 && background.getState() != "Started") {
            Thread.sleep(1000)
            i++
        }
    }

    private fun ouinetWaitForDegraded(background: OuinetBackground) {
        var i = 0
        while (i < 15 && background.getState() != "Degraded") {
            Thread.sleep(1000)
            i++
        }
    }

    private fun ouinetStartupAndJoinThread(background : OuinetBackground) {
        Log.i(TAG, "Start ouinet")
        val startupThread = background.startup {
            /* Use callback to wait for ouinet client to stabilize */
            ouinetWaitForStarted(background)
            Log.i(TAG, "Ouinet state: ${background.getState()}")
            assertEquals("Started", background.getState())
        }
        startupThread.join()
    }

    private fun ouinetStartAndJoinThread(background : OuinetBackground) {
        Log.i(TAG, "Start ouinet")
        val startThread = background.start {
            /* Use callback to wait for ouinet client to stabilize */
            ouinetWaitForStarted(background)
            Log.i(TAG, "Ouinet state: ${background.getState()}")
            assertEquals("Started", background.getState())
        }
        startThread.join()
    }

    private fun ouinetStartDegradedAndJoinThread(background : OuinetBackground) {
        Log.i(TAG, "Start ouinet")
        val startThread = background.start {
            /* Use callback to wait for ouinet client to stabilize */
            ouinetWaitForDegraded(background)
            Log.i(TAG, "Ouinet state: ${background.getState()}")
            // accept both degraded and started as valid states,
            // since client may get not stay in degraded state long enough
            assertTrue(background.getState() == "Degraded" || background.getState() == "Started")
        }
        startThread.join()
    }

    private fun ouinetStopAndJoinThread(background : OuinetBackground) {
        Log.i(TAG, "Stop ouinet")
        val stopThread = background.stop {
            Log.i(TAG, "Ouinet state: ${background.getState()}")
            assertEquals("Stopped", background.getState());
        }
        stopThread.join()
    }

    private fun ouinetShutdownAndJoinThread(background : OuinetBackground) {
        Log.i(TAG, "Start ouinet")
        val shutdownThread = background.shutdown(false) {
            Log.i(TAG, "Ouinet state: ${background.getState()}")
            assertEquals("Stopped", background.getState());
        }
        shutdownThread.join()
    }

    @Test
    fun useAppContext() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("ie.equalit.ouinet_examples.android_compose", appContext.packageName)
    }

    @Test
    fun testOuinetBackgroundStartup() {
        Log.i(TAG, "Begin testOuinetBackgroundStartup")
        val background = ouinetBackground()
        ouinetStartupAndJoinThread(background)
    }

    @Test
    fun testOuinetBackgroundStartupShutdown() {
        Log.i(TAG, "Begin testOuinetBackgroundStartupShutdown")
        val background = ouinetBackground()
        ouinetStartupAndJoinThread(background)
        ouinetShutdownAndJoinThread(background)
    }

    @Test
    fun testOuinetBackgroundStartupAndStop() {
        Log.i(TAG, "Begin testOuinetBackgroundStartup")
        val background = ouinetBackground()
        ouinetStartupAndJoinThread(background)
        ouinetStopAndJoinThread(background)
    }

    @Test
    fun testMultiStartupShutdown() {
        Log.i(TAG, "Begin testMultiStartStop")
        val background = ouinetBackground()
        for (i in 1..5) {
            Log.i(TAG, "Starting Ouinet, trial $i")
            ouinetStartupAndJoinThread(background)
            Log.i(TAG, "Stopping Ouinet, trial $i")
            ouinetShutdownAndJoinThread(background)
        }
    }

    @Test
    fun testSingleStartStop() {
        Log.i(TAG, "Begin testSingleStartStop")
        val background = ouinetBackground()
        ouinetStartupAndJoinThread(background)
        ouinetStartAndJoinThread(background)
        ouinetStopAndJoinThread(background)
    }

    @Test
    fun testMultiStartStop() {
        Log.i(TAG, "Begin testMultiStartStop")
        val background = ouinetBackground()
        ouinetStartupAndJoinThread(background)
        for (i in 1..5) {
            Log.i(TAG, "Starting Ouinet, trial $i")
            ouinetStartAndJoinThread(background)
            Log.i(TAG, "Stopping Ouinet, trial $i")
            ouinetStopAndJoinThread(background)
        }
    }

    @Test
    fun testMultiStartStopShutdown() {
        Log.i(TAG, "Begin testMultiStartStop")
        val background = ouinetBackground()
        ouinetStartupAndJoinThread(background)
        for (i in 1..5) {
            Log.i(TAG, "Starting Ouinet, trial $i")
            ouinetStartAndJoinThread(background)
            Log.i(TAG, "Stopping Ouinet, trial $i")
            ouinetStopAndJoinThread(background)
        }
        ouinetShutdownAndJoinThread(background)
    }

    @Test
    fun testSingleStartMultiStop() {
        Log.i(TAG, "Begin testSingleStartMultiStop")
        val background = ouinetBackground()
        ouinetStartupAndJoinThread(background)
        Log.i(TAG, "Starting Ouinet")
        ouinetStartAndJoinThread(background)
        for (i in 1..3) {
            Log.i(TAG, "Stopping Ouinet, trial $i")
            Thread.sleep(100);
            ouinetStopAndJoinThread(background)
        }
    }

    @Test
    fun testMultiStartSingleStop() {
        Log.i(TAG, "Begin testMultiStartSingleStop")
        val background = ouinetBackground()
        ouinetStartupAndJoinThread(background)
        for (i in 1..3) {
            Log.i(TAG, "Starting Ouinet, trial $i")
            ouinetStartAndJoinThread(background)
        }
        ouinetStopAndJoinThread(background)
    }

    @Test
    fun testMultiStartDegradedStop() {
        Log.i(TAG, "Begin testMultiStartStop")
        val background = ouinetBackground()
        ouinetStartupAndJoinThread(background)
        for (i in 1..5) {
            Log.i(TAG, "Starting Ouinet, trial $i")
            ouinetStartDegradedAndJoinThread(background)
            Log.i(TAG, "Stopping Ouinet, trial $i")
            ouinetStopAndJoinThread(background)
        }
    }

    companion object {
        private const val TAG = "OuinetInstrumentedTest"
    }
}
