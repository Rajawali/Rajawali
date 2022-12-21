package org.rajawali3d.examples

import android.Manifest
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.takeScreenshot
import androidx.test.core.graphics.writeToTestStorage
import androidx.test.espresso.Espresso
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.espresso.screenshot.captureToBitmap
import androidx.test.ext.junit.rules.activityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.GrantPermissionRule
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestName
import org.junit.runner.RunWith
import org.rajawali3d.examples.data.DataSet
import org.rajawali3d.examples.tools.RecyclerViewMatcher

@RunWith(AndroidJUnit4::class)
class LauncherSmokeTest {

    @get:Rule
    val activityScenarioRule = activityScenarioRule<LauncherActivity>()

    @get:Rule
    var nameRule = TestName()

    @Before
    fun setUp() = Intents.init()

    @After
    fun cleanUp() = Intents.release()

    @get:Rule
    val grantPermissionRule: GrantPermissionRule = GrantPermissionRule.grant(
        Manifest.permission.CAMERA,
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.READ_EXTERNAL_STORAGE
    )

    @Test
    fun smokeTestSimplyStart() {
        Thread.sleep(300)
        takeScreenshot()
            .writeToTestStorage("${javaClass.simpleName}_${nameRule.methodName}-A")

        onView(withId(R.id.recycler)).check(matches(isDisplayed()))
        onView(isRoot())
            .captureToBitmap()
            .writeToTestStorage("${javaClass.simpleName}_${nameRule.methodName}-R")

        var overallIndex = 0
        DataSet.instance?.categories?.forEach {
            it.examples.forEach { _ ->
                overallIndex++
                println("Click on $overallIndex")
                clickOnExample(overallIndex)
            }
            overallIndex++
        }
    }

    private fun clickOnExample(itemIndex: Int) {
        onView(withId(R.id.recycler)).perform(RecyclerViewActions.scrollToPosition<RecyclerView.ViewHolder>(itemIndex))

        onView(RecyclerViewMatcher(R.id.recycler).atPositionOnView(itemIndex, R.id.textItem))
            .check(matches(isDisplayed()))
            .perform(click())
        //  Intents.intended(hasComponent(ExamplesActivity::class.java.name))

        Espresso.pressBack()
    }
}
