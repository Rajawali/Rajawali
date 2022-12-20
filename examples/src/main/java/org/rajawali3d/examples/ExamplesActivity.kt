package org.rajawali3d.examples

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import org.rajawali3d.examples.data.Example

class ExamplesActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_examples)

        val actionBar: ActionBar? = supportActionBar
        actionBar?.setHomeButtonEnabled(true)
        actionBar?.setDisplayHomeAsUpEnabled(true)

        if (savedInstanceState == null) {
            val extras = intent.extras
            extras?.let { processExtras(it) }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
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
