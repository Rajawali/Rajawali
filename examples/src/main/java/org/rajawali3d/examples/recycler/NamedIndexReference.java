package org.rajawali3d.examples.recycler;

import android.support.annotation.NonNull;

import org.rajawali3d.examples.data.INamed;

abstract class NamedIndexReference<T extends INamed> extends ReferencedAdapter.IndexReference<T> {

    public NamedIndexReference(@NonNull T value) {
        super(value);
    }

}
