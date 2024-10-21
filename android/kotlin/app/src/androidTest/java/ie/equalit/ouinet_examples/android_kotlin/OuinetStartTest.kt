package ie.equalit.ouinet_examples.android_kotlin

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.Visibility
import androidx.test.ext.junit.rules.activityScenarioRule
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class OuinetStartTest {
    @get:Rule
    var activityScenarioRule = activityScenarioRule<MainActivity>()

    @Before
    fun setup() {
        // Always make sure data is on at the start of test
        enableData()
        Thread.sleep(5000)
    }

    @After
    fun runAfter() {
        // Always make sure data is re-enabled after the test
        enableData()
    }

    @Test
    fun testOuinetStarted() {
        Thread.sleep(5000)
        onView(withId(R.id.start)).perform(click())
        Thread.sleep(15000)
        assertStartedState()
    }

    @Test
    fun testOuinetDegraded() {
        disableData()
        Thread.sleep(5000)
        onView(withId(R.id.start)).perform(click())
        Thread.sleep(15000)
        assertDegradedState()
    }

    @Test
    fun testOuinetStartAfterDegraded() {
        disableData()
        Thread.sleep(5000)
        onView(withId(R.id.start)).perform(click())
        Thread.sleep(15000)
        assertDegradedState()
        enableData()
        Thread.sleep(15000)
        assertStartedState()
    }
}

private fun disableData() {
    InstrumentationRegistry.getInstrumentation().uiAutomation.executeShellCommand("svc wifi disable")
    InstrumentationRegistry.getInstrumentation().uiAutomation.executeShellCommand("svc data disable")
}

private fun enableData() {
    InstrumentationRegistry.getInstrumentation().uiAutomation.executeShellCommand("svc wifi enable")
    InstrumentationRegistry.getInstrumentation().uiAutomation.executeShellCommand("svc data enable")
}

private fun startedState() = onView(withText("State: Started"))
private fun degradedState() = onView(withText("State: Degraded"))

private fun assertStartedState() = startedState().check(matches(
    ViewMatchers.withEffectiveVisibility(Visibility.VISIBLE)
))
private fun assertDegradedState() = degradedState().check(matches(
    ViewMatchers.withEffectiveVisibility(Visibility.VISIBLE)
))
