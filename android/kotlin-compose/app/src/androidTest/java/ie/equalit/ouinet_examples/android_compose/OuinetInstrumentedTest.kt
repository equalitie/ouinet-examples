package ie.equalit.ouinet_examples.android_compose

import android.content.Context
import android.util.Log
import androidx.test.annotation.UiThreadTest
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import ie.equalit.ouinet.OuinetBackground
import ie.equalit.ouinet_examples.android_compose.components.Ouinet
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.InterruptedIOException
import java.util.concurrent.TimeUnit
import org.junit.Assert.*
import org.junit.FixMethodOrder
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.MethodSorters

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
    private fun ouinetBackground(): OuinetBackground {
        val appContext = context()
        val ouinet = Ouinet(appContext)
        ouinet.setBackground(appContext)
        return ouinet.background
    }

    private fun context(): Context {
        return InstrumentationRegistry.getInstrumentation().targetContext
    }

    private fun ouinetStartupAndJoinThread(background: OuinetBackground): Thread {
        Log.i(TAG, "Start ouinet")
        var callback_run = false
        val startupThread =
                background.startup {
                    /* Use callback to wait for ouinet client to stabilize */
                    // there is no guarantee that this will ever be reached
                    callback_run = true

                    val step = 1000 // every second
                    val max_wait = 9000 //
                    for (i in 0 until max_wait/step) {
                        Thread.sleep(step.toLong())
                        val state = background.getState()
                        if (!state.startsWith("Created")){
                            break
                        }
                    }

                    if (background.getState().startsWith("Stopped")){
                        throw Exception("Ouinet state is '${background.getState()}' but we were just waiting for it to start!")
                    }
                    if (!background.getState().startsWith("Start")){
                        throw Exception("Ouinet state is '${background.getState()}' but it should have been Start by now")
                    }
                }

        startupThread.join(10000)
        if (!callback_run) {
            throw Exception("startup callback was not reached -> StartClient() did not return control in 10 seconds")
        }

        if (startupThread.isAlive()) {
            throw Exception("The startup thread has timed out without it having started")
        }
        return startupThread
    }

    private fun ouinetStartAndJoinThread(background: OuinetBackground) {
        Log.i(TAG, "Start ouinet")
        val startThread =
                background.start {
                    /* Use callback to wait for ouinet client to stabilize */
                    Thread.sleep(5000)
                    Log.i(TAG, "Ouinet state: ${background.getState()}")
                    assertTrue(background.getState().startsWith("Start"))
                }
        startThread.join(10000)
    }

    private fun ouinetStopAndJoinThread(background: OuinetBackground) {
        // We have a problem here. This can stop the background before it has
        // ever was started. and it will crash the testing framework entirely.

        if (background.getState().startsWith("Created")){
            throw Exception("Ouinet state is '${background.getState()}' but we attempted to stop it")
        }
        Log.i(TAG, "Stop ouinet")
        val stopThread =
                background.stop {
                    Log.i(TAG, "Ouinet state: ${background.getState()}")
                    assertEquals("Stopped", background.getState())
                }
        stopThread.join(10000)
    }

    private fun ouinetShutdownAndJoinThread(background: OuinetBackground) {
        if (background.getState().startsWith("Created")){
            throw Exception("Ouinet state is '${background.getState()}' but we attempted to shut it down")
        }
        Log.i(TAG, "Shutdown ouinet")
        val shutdownThread =
                background.shutdown(false) {
                    Log.i(TAG, "Ouinet state: ${background.getState()}")
                    assertEquals("Stopped", background.getState())
                }
        shutdownThread.join(10000)
    }

    private fun collect_output(process: Process, seconds: Int): String {
        val output = StringBuilder()
        var readerThread = Thread {
            Log.d(TAG, "thread: start")
            val reader = BufferedReader(InputStreamReader(process.inputStream))
            try {
                reader.use {
                    it.forEachLine { line ->
                        output.appendLine(line)
                        Log.d(TAG, line)
                    }
                }
            } catch (exception: InterruptedIOException) {
                Log.d(TAG, "thread: reading interrupted")
                reader.close()
            }
            Log.d(TAG, "thread: exited")
        }
        readerThread.start()
        val timed_out = !process.waitFor(seconds.toLong(), TimeUnit.SECONDS)
        Log.d(TAG, "interrupting the reader thread")
        readerThread.interrupt()
        readerThread.join(1000)
        Log.d(TAG, "waited for thread")

        val result = output.toString()
        if (!timed_out) {
            throw Exception("premature end of process, output: '$output'")
        }
        if (result.isNullOrEmpty()) {
            throw Exception("i2pd has exited without output")
        }

        return result
    }

    @Test
    fun n00_useAppContext() {
        // Log.d(TAG, "Pid is : $Process.myPid()")
        Log.d(TAG, "checking that app context is what we expect")
        // Context of the app under test.
        val appContext = context()
        assertEquals("ie.equalit.ouinet_examples.android_kotlin", appContext.packageName)
        Log.d(TAG, "app context is what we expect")
    }

    /*
    @Test
    fun n01_i2pd_exists() {
        Log.d(TAG, "checking that i2pd exists")
        val appContext = context()
        require(!Ouinet.findI2PDExe(appContext).toString().isNullOrEmpty())
        Log.d(TAG, "i2pd exists")
    }

    @Test
    fun n02_i2pd_starts() {
        Log.d(TAG, "Starting test of i2pd")
        val process = Ouinet.startI2PD(context())
        val output = collect_output(process, seconds = 2)
        require(!output.isNullOrEmpty())
        // Log.d(TAG, output)
        Log.d(TAG, "destroying the process")
        process.destroyForcibly()
    }
    */

    @Test
    fun n1_testOuinetBackgroundStartup() {
        Log.d(TAG, "Begin testOuinetBackgroundStartup")
        val background = ouinetBackground()
        val thread = ouinetStartupAndJoinThread(background)
    }

    @Test
    fun n2_testOuinetBackgroundStartupShutdown() {
        Log.d(TAG, "Begin testOuinetBackgroundStartupShutdown")
        val background = ouinetBackground()
        ouinetStartupAndJoinThread(background)
        ouinetShutdownAndJoinThread(background)
    }

    @Test
    fun n3_testOuinetBackgroundStartupAndStop() {
        Log.d(TAG, "Begin testOuinetBackgroundStartup")
        val background = ouinetBackground()
        ouinetStartupAndJoinThread(background)
        ouinetStopAndJoinThread(background)
    }

    @Test
    fun n4_testMultiStartupShutdown() {
        Log.d(TAG, "Begin testMultiStartStop")
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
        Log.d(TAG, "Begin testSingleStartStop")
        val background = ouinetBackground()
        ouinetStartupAndJoinThread(background)
        ouinetStartAndJoinThread(background)
        ouinetStopAndJoinThread(background)
    }

    @Test
    fun n6_testMultiStartStop() {
        Log.d(TAG, "Begin testMultiStartStop")
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
        Log.d(TAG, "Begin testMultiStartStop")
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
        Log.d(TAG, "Begin testSingleStartMultiStop")
        val background = ouinetBackground()
        ouinetStartupAndJoinThread(background)
        Log.i(TAG, "Starting Ouinet")
        ouinetStartAndJoinThread(background)
        for (i in 1..3) {
            Log.i(TAG, "Stopping Ouinet, trial $i")
            Thread.sleep(100)
            ouinetStopAndJoinThread(background)
        }
    }

    @Test
    fun n9_testMultiStartSingleStop() {
        Log.d(TAG, "Begin testMultiStartSingleStop")
        val background = ouinetBackground()
        ouinetStartupAndJoinThread(background)
        for (i in 1..3) {
            Log.i(TAG, "Starting Ouinet, trial $i")
            ouinetStartAndJoinThread(background)
        }
        ouinetStopAndJoinThread(background)
    }
}
