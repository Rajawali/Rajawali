package org.rajawali3d.examples

import android.os.Bundle
import android.view.*
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.rajawali3d.examples.data.ExamplesDataSet.Companion.instance
import org.rajawali3d.examples.recycler.CategoryAdapter

class ExamplesFragment : Fragment(), SearchView.OnQueryTextListener {

    private lateinit var adapter: CategoryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        adapter = CategoryAdapter()
        adapter.setCategories(instance!!.categories)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_examples, container, false)
        val recyclerView = view.findViewById<RecyclerView>(R.id.recycler)
        recyclerView.layoutManager = LinearLayoutManager(activity)
        recyclerView.adapter = adapter
        return view
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_search, menu)
        val menuItemSearch = menu.findItem(R.id.action_search)
        val searchView = menuItemSearch.actionView as SearchView?
        searchView!!.setOnQueryTextListener(this)
    }

    override fun onQueryTextSubmit(query: String) = adapter.filterDone(requireActivity())

    override fun onQueryTextChange(newText: String): Boolean {
        adapter.filter(requireActivity(), newText)
        adapter.notifyDataSetChanged()
        return true
    }

    companion object {
        const val TAG = "ExamplesFragment"
    }
}