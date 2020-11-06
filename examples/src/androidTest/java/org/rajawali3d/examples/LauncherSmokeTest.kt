package org.rajawali3d.examples

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.GrantPermissionRule
import com.moka.utils.Screenshot
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LauncherSmokeTest {

    @get:Rule
    var activityScenarioRule = ActivityScenarioRule(LauncherActivity::class.java)

    @get:Rule
    val grantPermissionRule: GrantPermissionRule = GrantPermissionRule.grant(*LauncherActivity.PERMISSIONS)

    @Test
    fun smokeTestSimplyStart() {
        onView(withId(R.id.recycler)).check(matches(isDisplayed()))
        Screenshot.takeScreenshot("smoke")
    }
}
