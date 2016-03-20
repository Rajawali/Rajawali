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
import java.util.Locale;

@SuppressWarnings("unchecked")
public final class CategoryAdapter extends ReferencedAdapter<INamed, NamedReferenceViewHolder, NamedIndexReference<INamed>> implements NamedReferenceViewHolder.IndexReferenceClickListener {

    private static final int INDEX_CATEGORY = 1;
    private static final int INDEX_EXAMPLE = 2;

    private List<Category> categoriesOriginal;
    private List<Category> categoriesDisplayed;

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

    @Override
    public void onReferenceClicked(View view, IndexReference reference) {
        Class type = reference.getType();
        if (Example.class.equals(type)) {
            Example example = (Example) reference.get();
            Context context = view.getContext();
            onExampleSelected(context, example);
        } else {
            throw new IllegalStateException("Unhandled reference for " + type);
        }
    }

    public boolean filterDone(@NonNull Context context) {
        if (categoriesDisplayed.size() == 1) {
            Category category = categoriesDisplayed.get(0);
            Example[] examples = category.getExamples();
            if (examples.length == 1) {
                Example example = examples[0];
                onExampleSelected(context, example);
                return true;
            }
        }

        return false;
    }

    public void filter(@NonNull Context context,
                       @NonNull String query) {

        String queryLC = query.trim()
                .toLowerCase(Locale.ENGLISH);

        List<Category> categories = new LinkedList<>();

        if (queryLC.length() == 0) {
            categories.addAll(categoriesOriginal);
        } else {
            for (Category category : categoriesOriginal) {
                List<Example> filtered = new LinkedList<>();
                Example[] examples = category.getExamples();

                // If the category matches the query, add all examples then move to the next item
                String categoryName = context.getString(category.getName())
                        .toLowerCase(Locale.ENGLISH);

                if (categoryName.contains(queryLC)) {
                    categories.add(category);
                    continue;
                }

                // Iterate the examples adding matches to the filtered list
                for (Example example : examples) {
                    String exampleName = context.getString(example.getName())
                            .toLowerCase(Locale.ENGLISH);

                    if (exampleName.contains(queryLC)) {
                        filtered.add(example);
                    }
                }

                // If the category has any matching examples, keep it and add the matches
                final int size = filtered.size();
                if (size > 0) {
                    Example[] filteredExamples = filtered.toArray(new Example[size]);
                    Category filteredCategory = new Category(category.getName(), filteredExamples);
                    categories.add(filteredCategory);
                }
            }
        }

        setReferencesFromList(categories);
    }

    public void setCategories(@NonNull List<Category> categories) {
        this.categoriesOriginal = categories;
        setReferencesFromList(categories);
    }

    void setReferencesFromList(List<Category> categories) {
        final List<NamedIndexReference<INamed>> indexReferences;
        indexReferences = new LinkedList<>();
        for (Category category : categories) {
            indexReferences.add(new CategoryReference(category));
            Example[] examples = category.getExamples();
            for (Example example : examples) {
                indexReferences.add(new ExampleReference(example));
            }
        }

        categoriesDisplayed = categories;
        setReferences(indexReferences);
    }

    void onExampleSelected(@NonNull Context context,
                           @NonNull Example example) {

        Class aClass = example.getType();
        if (Activity.class.isAssignableFrom(aClass)) {
            context.startActivity(new Intent(context, aClass));
        } else {
            Intent intent = new Intent(context, ExamplesActivity.class);
            intent.putExtra(ExamplesActivity.EXTRA_EXAMPLE, example);
            context.startActivity(intent);
        }
    }

}
