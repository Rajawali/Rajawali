package org.rajawali3d.examples

import androidx.test.core.app.takeScreenshot
import androidx.test.core.graphics.writeToTestStorage
import androidx.test.espresso.Espresso
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestName
import org.junit.runner.RunWith
import org.rajawali3d.examples.data.ExamplesDataSet

@RunWith(AndroidJUnit4::class)
class CapabilityTest : BaseExampleTest() {

    @get:Rule
    var nameRule = TestName()

    @Test
    fun clickSystemInfo() {
        onView(withId(R.id.recycler)).check(ViewAssertions.matches(isDisplayed()))
        onView(withId(R.id.action_info)).perform(click())

        takeScreenshot()
            .writeToTestStorage("${javaClass.simpleName}_${nameRule.methodName}-before")
        Espresso.pressBack()

        clickOnExample(2)

        onView(withId(R.id.recycler)).check(ViewAssertions.matches(isDisplayed()))
        onView(withId(R.id.action_info)).perform(click())
        takeScreenshot()
            .writeToTestStorage("${javaClass.simpleName}_${nameRule.methodName}-after")
        Espresso.pressBack()
    }
}
