package ie.equalit.ouinet_examples.android_compose

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.rules.activityScenarioRule
import org.junit.Rule
import org.junit.Test

class OuinetStartTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @get:Rule
    var activityScenarioRule = activityScenarioRule<MainActivity>()

    @Test
    fun testOuinetStarted() {
        Thread.sleep(5000)
        composeTestRule.onNodeWithTag("start_button").performClick()
        Thread.sleep(15000)
        composeTestRule.onNodeWithText("State: Started").assertIsDisplayed()
    }
}
