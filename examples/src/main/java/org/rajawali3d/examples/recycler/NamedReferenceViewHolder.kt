package org.rajawali3d.examples.recycler

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import org.rajawali3d.examples.R
import org.rajawali3d.examples.data.INamed
import org.rajawali3d.examples.recycler.ReferencedAdapter.IndexReference

internal class NamedReferenceViewHolder<K : INamed?, T : IndexReference<K>?>(itemView: View) : RecyclerView.ViewHolder(itemView),
    View.OnClickListener {
    private val textViewName: TextView
    private var indexReference: T? = null
    private var referenceClickListener: IndexReferenceClickListener<T>? = null

    init {
        textViewName = itemView.findViewById(R.id.textItem)
        textViewName.setOnClickListener(this)
    }

    override fun onClick(v: View) {
        indexReference?.let {
            referenceClickListener?.onReferenceClicked(v, it)
        }
    }

    fun onBind(indexReference: T) {
        this.indexReference = indexReference
        val k = indexReference!!.get()
        textViewName.setText(k!!.name)
    }

    fun setIndexReferenceClickListener(referenceClickListener: IndexReferenceClickListener<T>) {
        this.referenceClickListener = referenceClickListener
    }

    interface IndexReferenceClickListener<T : IndexReference<*>?> {
        fun onReferenceClicked(v: View?, reference: T)
    }
}