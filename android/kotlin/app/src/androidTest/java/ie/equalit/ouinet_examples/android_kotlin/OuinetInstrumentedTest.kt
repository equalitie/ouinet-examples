package ie.equalit.ouinet_examples.android_kotlin

import android.util.Log
import android.content.Context
import androidx.test.annotation.UiThreadTest

import kotlin.io.path.listDirectoryEntries
import ie.equalit.ouinet_examples.android_kotlin.components.Ouinet

import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import ie.equalit.ouinet.OuinetBackground

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*
import org.junit.FixMethodOrder
import org.junit.runners.MethodSorters

import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
@UiThreadTest
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
class OuinetInstrumentedTest {

    val TAG = "OuinetInstrumentedTest"
    private fun ouinetBackground() : OuinetBackground {
        val appContext = context()
        val ouinet = Ouinet(appContext)
        ouinet.setBackground(appContext)
        return ouinet.background
    }


    private fun context() : Context {
         return InstrumentationRegistry.getInstrumentation().targetContext
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
    fun n0_useAppContext() {
        // Context of the app under test.
        val appContext = context()
        assertEquals("ie.equalit.ouinet_examples.android_kotlin", appContext.packageName)
    }

    @Test
    fun n01_i2pd_exists() {
        val appContext = context()
        Ouinet.assertI2PDExe(appContext)
    }

    @Test
    fun n1_testOuinetBackgroundStartup() {
        Log.i(TAG, "Begin testOuinetBackgroundStartup")
        val background = ouinetBackground()
        ouinetStartupAndJoinThread(background)
    }

    @Test
    fun n2_testOuinetBackgroundStartupShutdown() {
        Log.i(TAG, "Begin testOuinetBackgroundStartupShutdown")
        val background = ouinetBackground()
        ouinetStartupAndJoinThread(background)
        ouinetShutdownAndJoinThread(background)
    }

    @Test
    fun n3_testOuinetBackgroundStartupAndStop() {
        Log.i(TAG, "Begin testOuinetBackgroundStartup")
        val background = ouinetBackground()
        ouinetStartupAndJoinThread(background)
        ouinetStopAndJoinThread(background)
    }

    @Test
    fun n4_testMultiStartupShutdown() {
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
    fun n5_testSingleStartStop() {
        Log.i(TAG, "Begin testSingleStartStop")
        val background = ouinetBackground()
        ouinetStartupAndJoinThread(background)
        ouinetStartAndJoinThread(background)
        ouinetStopAndJoinThread(background)
    }

    @Test
    fun n6_testMultiStartStop() {
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
    fun n7_testMultiStartStopShutdown() {
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
    fun n8_testSingleStartMultiStop() {
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
    fun n9_testMultiStartSingleStop() {
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
