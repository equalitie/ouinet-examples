package ie.equalit.ouinet_examples.android_kotlin

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.espresso.assertion.ViewAssertions.*
import androidx.test.ext.junit.rules.activityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@LargeTest
class OuinetStressTest {

    /**
     * Use [ActivityScenarioRule] to create and launch the activity under test before each test,
     * and close it after each test. This is a replacement for
     * [androidx.test.rule.ActivityTestRule].
     */
    @get:Rule
    var activityScenarioRule = activityScenarioRule<MainActivity>()

    private fun checkOuinetStarted() {
        /* Wait 10s for ouinet to fully start */
        Thread.sleep(10000)
        onView(withId(R.id.status)).check(matches(withText("State: Started")))
    }

    private fun checkOuinetRestarted() {
        onView(withId(R.id.restart)).perform(click())
        Thread.sleep(15000)
        onView(withId(R.id.status)).check(matches(withText("State: Started")))
    }

    private fun checkOuinetClear() {
        onView(withId(R.id.clear)).perform(click())
        Thread.sleep(5000)
        onView(withId(R.id.cache_size)).check(matches(withText("Cache size: 0 B")))
        onView(withId(R.id.groups)).check(matches(withText("Groups count: 0")))
    }

    private fun requestUrl (url : String) {
        onView(withId(R.id.url))
            .perform(replaceText(url), closeSoftKeyboard())
        onView(withId(R.id.get)).perform(click())
    }

    private fun requestUrlLoop(n : Int) {
        /* loop through list of sites to request */
        for (i in 1..n) {
            requestUrl(BASE_URL + i.toString())
            Thread.sleep(1000)
        }
    }

    @Test
    fun testOuinetStarted() {
        checkOuinetStarted()
    }

    @Test
    fun testOuinetRestart() {
        checkOuinetStarted()
        Thread.sleep(5000)
        checkOuinetRestarted()
    }

    @Test
    fun testRequestTenSitesRestartClear() {
        testOuinetStarted()
        requestUrlLoop(10)
        Thread.sleep(5000)
        checkOuinetRestarted()
        checkOuinetClear()
    }

    @Test
    fun testRequestFiftySitesRestartClear() {
        testOuinetStarted()
        requestUrlLoop(50)
        Thread.sleep(5000)
        checkOuinetRestarted()
        checkOuinetClear()
    }

    @Test
    fun testRequestOneHundredSitesRestartClear() {
        testOuinetStarted()
        requestUrlLoop(100)
        Thread.sleep(5000)
        checkOuinetRestarted()
        checkOuinetClear()
    }

    @Test
    fun testRequestFiveHundredSitesRestartClear() {
        testOuinetStarted()
        requestUrlLoop(500)
        Thread.sleep(5000)
        checkOuinetRestarted()
        checkOuinetClear()
    }

    @Test
    fun testRequestOneThousandSitesRestartClear() {
        testOuinetStarted()
        requestUrlLoop(1000)
        Thread.sleep(5000)
        checkOuinetRestarted()
        checkOuinetClear()
    }

    companion object {
        val BASE_URL = "https://es.wikipedia.org/wiki/"
    }
}
