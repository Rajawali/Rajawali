package org.rajawali3d.examples.data;

import android.support.annotation.NonNull;
import android.support.annotation.StringRes;

public class Category implements INamed {

    @StringRes
    private final int name;
    private final Example[] examples;

    public Category(@StringRes int name,
             @NonNull Example[] examples) {
        this.name = name;
        this.examples = examples;
    }

    @Override
    @StringRes
    public int getName() {
        return name;
    }

    public Example[] getExamples() {
        return examples;
    }

}
