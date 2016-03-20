package org.rajawali3d.examples.recycler;

import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import org.rajawali3d.examples.data.INamed;

final class NamedReferenceViewHolder<K extends INamed,
        T extends ReferencedAdapter.IndexReference<K>> extends RecyclerView.ViewHolder implements
        OnClickListener {

    private final TextView textViewName;

    private T indexReference;

    @Nullable
    private IndexReferenceClickListener<T> referenceClickListener;

    public NamedReferenceViewHolder(View itemView) {
        super(itemView);
        textViewName = (TextView) itemView.findViewById(android.R.id.text1);
        textViewName.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (referenceClickListener != null) {
            referenceClickListener.onReferenceClicked(v, indexReference);
        }
    }

    void onBind(T indexReference) {
        this.indexReference = indexReference;
        K k = indexReference.get();
        textViewName.setText(k.getName());
    }

    void setIndexReferenceClickListener(@Nullable IndexReferenceClickListener<T> referenceClickListener) {
        this.referenceClickListener = referenceClickListener;
    }

    public interface IndexReferenceClickListener<T extends ReferencedAdapter.IndexReference> {

        void onReferenceClicked(View v, T reference);

    }

}
