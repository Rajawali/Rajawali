package org.rajawali3d.examples

import androidx.test.core.app.takeScreenshot
import androidx.test.core.graphics.writeToTestStorage
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.espresso.screenshot.captureToBitmap
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestName
import org.junit.runner.RunWith
import org.rajawali3d.examples.data.ExamplesDataSet

@RunWith(AndroidJUnit4::class)
class LauncherSmokeTest : BaseExampleTest() {

    @get:Rule
    var nameRule = TestName()

    @Before
    fun setUp() = Intents.init()

    @After
    fun cleanUp() = Intents.release()

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
        ExamplesDataSet.instance?.categories?.forEach {
            it.examples.forEach { _ ->
                overallIndex++
                println("Click on $overallIndex")
                clickOnExample(overallIndex)
            }
            overallIndex++
        }
    }

}
