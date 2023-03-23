package ie.equalit.ouinet_examples.android_kotlin

import android.util.Log
import androidx.test.annotation.UiThreadTest
import ie.equalit.ouinet_examples.android_kotlin.components.Ouinet

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
    val TAG = "OuinetInstrumentedTest"

    private fun ouinetBackground() : OuinetBackground {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        val ouinet = Ouinet(appContext)
        ouinet.setOnNotificationTapped {
            ouinet.background.shutdown(false)
        }
        ouinet.setOnConfirmTapped {
            ouinet.background.shutdown(true)
        }
        ouinet.setBackground(appContext)
        return ouinet.background
    }

    private fun ouinetStartupAndJoinThread(background : OuinetBackground) {
        Log.i(TAG, "Start ouinet")
        val startupThread = background.startup {
            /* Use callback to wait for ouinet client to stabilize */
            Thread.sleep(5000)
            Log.i(TAG, "Ouinet state: ${background.getState()}")
            assertTrue(background.getState().startsWith("Start"))
        }
        startupThread.join(10000)
    }

    private fun ouinetStartAndJoinThread(background : OuinetBackground) {
        Log.i(TAG, "Start ouinet")
        val startThread = background.start {
            /* Use callback to wait for ouinet client to stabilize */
            Thread.sleep(5000)
            Log.i(TAG, "Ouinet state: ${background.getState()}")
            assertTrue(background.getState().startsWith("Start"))
        }
        startThread.join(10000)
    }

    private fun ouinetStopAndJoinThread(background : OuinetBackground) {
        Log.i(TAG, "Stop ouinet")
        val stopThread = background.stop {
            Log.i(TAG, "Ouinet state: ${background.getState()}")
            assertEquals("Stopped", background.getState());
        }
        stopThread.join(10000)
    }

    private fun ouinetShutdownAndJoinThread(background : OuinetBackground) {
        Log.i(TAG, "Start ouinet")
        val shutdownThread = background.shutdown(false) {
            Log.i(TAG, "Ouinet state: ${background.getState()}")
            assertEquals("Stopped", background.getState());
        }
        shutdownThread.join(10000)
    }

    @Test
    fun useAppContext() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("ie.equalit.ouinet_examples.android_kotlin", appContext.packageName)
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
}
