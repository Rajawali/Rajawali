package org.rajawali3d.examples

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import org.rajawali3d.examples.data.Example

class ExamplesActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_examples)
        if (savedInstanceState == null) {
            val extras = intent.extras
            extras?.let { processExtras(it) }
        }
    }

    private fun processExtras(extras: Bundle) {
        val example = extras.getParcelable<Example>(EXTRA_EXAMPLE) ?: throw NullPointerException()
        val aClass = example.type
        try {
            val fragment = aClass.newInstance() as Fragment
            supportFragmentManager.beginTransaction()
                .replace(R.id.content_frame, fragment, aClass.name)
                .commit()
        } catch (e: Exception) {
            throw IllegalStateException(e)
        }
    }

    companion object {
        const val EXTRA_EXAMPLE = "EXTRA_EXAMPLE"
    }
}
