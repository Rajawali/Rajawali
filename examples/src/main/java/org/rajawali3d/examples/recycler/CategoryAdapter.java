package org.rajawali3d.examples.recycler;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.rajawali3d.examples.ExamplesActivity;
import org.rajawali3d.examples.R;
import org.rajawali3d.examples.data.Category;
import org.rajawali3d.examples.data.Example;
import org.rajawali3d.examples.data.INamed;

import java.util.LinkedList;
import java.util.List;

@SuppressWarnings("unchecked")
public final class CategoryAdapter extends ReferencedAdapter<INamed, NamedReferenceViewHolder, NamedIndexReference<INamed>> implements NamedReferenceViewHolder.IndexReferenceClickListener {

    private static final int INDEX_CATEGORY = 1;
    private static final int INDEX_EXAMPLE = 2;

    @Override
    protected void onBindViewHolder(NamedReferenceViewHolder holder, int position, NamedIndexReference<INamed> namedIndexReference) {
        holder.onBind(namedIndexReference);
    }

    @Override
    public NamedReferenceViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        switch (viewType) {
            case INDEX_CATEGORY: {
                View view = inflater.inflate(R.layout.item_category_view, parent, false);
                return new NamedReferenceViewHolder(view);
            }
            case INDEX_EXAMPLE:
                View view = inflater.inflate(R.layout.item_example_view, parent, false);
                NamedReferenceViewHolder namedReferenceViewHolder = new NamedReferenceViewHolder(view);
                namedReferenceViewHolder.setIndexReferenceClickListener(this);
                return namedReferenceViewHolder;
            default:
                throw new IllegalStateException("Unhandled reference for view type " + viewType);
        }
    }

    @Override
    protected int getIndexViewType(NamedIndexReference indexReference) {
        Class type = indexReference.getType();
        if (Category.class.equals(type)) {
            return INDEX_CATEGORY;
        } else if (Example.class.equals(type)) {
            return INDEX_EXAMPLE;
        } else {
            throw new IllegalStateException("Unhandled reference for " + type);
        }
    }

    public void setCategories(@NonNull List<Category> categories) {
        final List<NamedIndexReference<INamed>> indexReferences = new LinkedList<>();
        for (Category category : categories) {
            indexReferences.add(new CategoryReference(category));
            Example[] examples = category.getExamples();
            for (Example example : examples) {
                indexReferences.add(new ExampleReference(example));
            }
        }

        setReferences(indexReferences);
    }

    @Override
    public void onReferenceClicked(View view, IndexReference reference) {
        Class type = reference.getType();
        if (Example.class.equals(type)) {
            Example example = (Example) reference.get();
            Class aClass = example.getType();
            Context context = view.getContext();

            if (Activity.class.isAssignableFrom(aClass)) {
                context.startActivity(new Intent(context, aClass));
            } else {
                Intent intent = new Intent(context, ExamplesActivity.class);
                intent.putExtra(ExamplesActivity.EXTRA_EXAMPLE, example);
                context.startActivity(intent);
            }
        } else {
            throw new IllegalStateException("Unhandled reference for " + type);
        }
    }

}
