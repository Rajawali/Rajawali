package org.rajawali3d.examples.recycler;

import android.support.annotation.MainThread;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ViewHolder;

import java.util.ArrayList;
import java.util.List;

abstract class ReferencedAdapter<T, V extends ViewHolder, R extends ReferencedAdapter.IndexReference<T>> extends RecyclerView.Adapter<V> {

    private final List<R> referenceList;

    public ReferencedAdapter() {
        referenceList = new ArrayList<>();
    }

    protected abstract void onBindViewHolder(V holder, int position, R indexReference);

    protected abstract int getIndexViewType(R indexReference);

    @Override
    public int getItemViewType(int position) {
        R indexReference = referenceList.get(position);
        return getIndexViewType(indexReference);
    }

    @Override
    public final void onBindViewHolder(V holder, int position) {
        R indexReference = referenceList.get(position);
        onBindViewHolder(holder, position, indexReference);
    }

    @Override
    public int getItemCount() {
        return referenceList.size();
    }

    @MainThread
    public void setReferences(@NonNull List<R> indexReferences) {
        referenceList.clear();
        referenceList.addAll(indexReferences);
    }

    public static abstract class IndexReference<T> {

        final Class<T> type;
        final T value;

        @SuppressWarnings("unchecked")
        public IndexReference(@NonNull T value) {
            this.value = value;
            this.type = (Class<T>) value.getClass();
        }

        @NonNull
        public T get() {
            return value;
        }

        @NonNull
        public Class<T> getType() {
            return type;
        }

    }

}
