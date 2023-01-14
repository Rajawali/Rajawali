package org.rajawali3d.examples

import android.Manifest
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.rules.activityScenarioRule
import androidx.test.rule.GrantPermissionRule
import org.junit.Rule
import org.rajawali3d.examples.tools.RecyclerViewMatcher

abstract class BaseExampleTest {

    @get:Rule
    val activityScenarioRule = activityScenarioRule<LauncherActivity>()

    @get:Rule
    val grantPermissionRule: GrantPermissionRule = GrantPermissionRule.grant(
        Manifest.permission.CAMERA,
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.READ_EXTERNAL_STORAGE
    )

    protected fun clickOnExample(itemIndex: Int) {
        onView(withId(R.id.recycler)).perform(RecyclerViewActions.scrollToPosition<RecyclerView.ViewHolder>(itemIndex))

        onView(RecyclerViewMatcher(R.id.recycler).atPositionOnView(itemIndex, R.id.textItem))
            .check(matches(isDisplayed()))
            .perform(click())
        //  Intents.intended(hasComponent(ExamplesActivity::class.java.name))

        Espresso.pressBack()
    }
}
