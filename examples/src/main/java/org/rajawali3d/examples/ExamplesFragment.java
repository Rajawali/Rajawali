package org.rajawali3d.examples;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.SearchView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import org.rajawali3d.examples.data.DataSet;
import org.rajawali3d.examples.recycler.CategoryAdapter;

public class ExamplesFragment extends Fragment implements SearchView.OnQueryTextListener {

    static final String TAG = "ExamplesFragment";

    private CategoryAdapter adapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        adapter = new CategoryAdapter();
        adapter.setCategories(DataSet.getInstance().getCategories());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_examples, container, false);
        RecyclerView recyclerView = view.findViewById(R.id.recycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(adapter);

        return view;
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_search, menu);

        MenuItem menuItemSearch = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) menuItemSearch.getActionView();
        searchView.setOnQueryTextListener(this);
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return adapter.filterDone(requireActivity());
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        adapter.filter(requireActivity(), newText);
        adapter.notifyDataSetChanged();
        return true;
    }

}
