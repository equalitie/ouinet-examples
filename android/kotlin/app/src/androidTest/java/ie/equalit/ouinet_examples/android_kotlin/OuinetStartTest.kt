package ie.equalit.ouinet_examples.android_kotlin

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.rules.activityScenarioRule
import androidx.test.filters.LargeTest
import androidx.test.runner.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@LargeTest
class OuinetStartTest {
    @get:Rule
    var activityScenarioRule = activityScenarioRule<MainActivity>()

    private fun checkOuinetStarted() {
        /* Wait 10s for ouinet to fully start */
        Thread.sleep(10000)
        onView(withId(R.id.status)).check(matches(withText("State: Started")))
    }

    @Test
    fun testOuinetStarted() {
        checkOuinetStarted()
    }
}
