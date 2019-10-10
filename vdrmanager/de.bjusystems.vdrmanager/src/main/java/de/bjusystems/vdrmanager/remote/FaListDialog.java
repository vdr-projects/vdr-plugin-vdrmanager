package de.bjusystems.vdrmanager.remote;


import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SearchView;

import java.io.Serializable;

import de.bjusystems.vdrmanager.R;

public class FaListDialog extends Dialog implements
        SearchView.OnQueryTextListener, SearchView.OnCloseListener {

    private static final String ITEMS = "items";

    private ArrayAdapter listAdapter;

    private SearchableItem<String> searchableItem;

    public FaListDialog(Context context) {
        super(context);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Getting the layout inflater to inflate the view in an alert dialog.
        LayoutInflater inflater = LayoutInflater.from(getContext());

        // Crash on orientation change #7
        // Change Start
        // Description: As the instance was re initializing to null on rotating the device,
        // getting the instance from the saved instance
        // Change End

        View rootView = inflater.inflate(R.layout.searchable_list_dialog, null);

        final ListView viewById = (ListView) rootView.findViewById(R.id.listItems);
        viewById.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String item = String.valueOf(listAdapter.getItem(position));
                if (searchableItem != null) {
                    searchableItem.onSearchableItemClicked(item, position);
                }
                FaListDialog.this.dismiss();
            }
        });
        ((SearchView) rootView.findViewById(R.id.search)).setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                listAdapter.getFilter().filter(newText);
                return false;
            }
        });
//
//        //create the adapter by passing your ArrayList data
        listAdapter = ArrayAdapter.createFromResource(getContext(), R.array.font, R.layout.simple_list_item_1_fa);
        viewById.setAdapter(listAdapter);
        setContentView(rootView);
    }

    @Override
    public boolean onClose() {
        return false;
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        return false;
    }

    public SearchableItem getSearchableItem() {
        return searchableItem;
    }

    public void setSearchableItem(SearchableItem searchableItem) {
        this.searchableItem = searchableItem;
    }

//    // Crash on orientation change #7
//    // Change Start
//    // Description: Saving the instance of searchable item instance.
//    @Override
//    public void onSaveInstanceState(Bundle outState) {
//        outState.putSerializable("item", _searchableItem);
//        super.onSaveInstanceState(outState);
//    }
//    // Change End
//
//    public void setTitle(String strTitle) {
//        _strTitle = strTitle;
//    }
//
//    public void setPositiveButton(String strPositiveButtonText) {
//        _strPositiveButtonText = strPositiveButtonText;
//    }
//
//    public void setPositiveButton(String strPositiveButtonText, DialogInterface.OnClickListener onClickListener) {
//        _strPositiveButtonText = strPositiveButtonText;
//        _onClickListener = onClickListener;
//    }
//
//    public void setOnSearchableItemClickListener(SearchableItem searchableItem) {
//        this._searchableItem = searchableItem;
//    }
//
//    public void setOnSearchTextChangedListener(OnSearchTextChanged onSearchTextChanged) {
//        this._onSearchTextChanged = onSearchTextChanged;
//    }
//
//    private void setData(View rootView) {
//        SearchManager searchManager = (SearchManager) getActivity().getSystemService(Context
//                .SEARCH_SERVICE);
//
//        _searchView = (SearchView) rootView.findViewById(R.id.search);
//        _searchView.setSearchableInfo(searchManager.getSearchableInfo(getActivity().getComponentName
//                ()));
//        _searchView.setIconifiedByDefault(false);
//        _searchView.setOnQueryTextListener(this);
//        _searchView.setOnCloseListener(this);
//        _searchView.clearFocus();
//        InputMethodManager mgr = (InputMethodManager) getActivity().getSystemService(Context
//                .INPUT_METHOD_SERVICE);
//        mgr.hideSoftInputFromWindow(_searchView.getWindowToken(), 0);
//
//
//        List items = (List) getArguments().getSerializable(ITEMS);
//
//        _listViewItems = (ListView) rootView.findViewById(R.id.listItems);
//
//        //create the adapter by passing your ArrayList data
//        listAdapter = new ArrayAdapter(getActivity(), android.R.layout.simple_list_item_1,
//                items);
//        //attach the adapter to the list
//        _listViewItems.setAdapter(listAdapter);
//
//        _listViewItems.setTextFilterEnabled(true);
//
//        _listViewItems.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                _searchableItem.onSearchableItemClicked(listAdapter.getItem(position), position);
//                getDialog().dismiss();
//            }
//        });
//    }
//
//    @Override
//    public boolean onClose() {
//        return false;
//    }
//
//    @Override
//    public boolean onQueryTextSubmit(String s) {
//        _searchView.clearFocus();
//        return true;
//    }
//
//    @Override
//    public boolean onQueryTextChange(String s) {
////        listAdapter.filterData(s);
//        if (TextUtils.isEmpty(s)) {
////                _listViewItems.clearTextFilter();
//            ((ArrayAdapter) _listViewItems.getAdapter()).getFilter().filter(null);
//        } else {
//            ((ArrayAdapter) _listViewItems.getAdapter()).getFilter().filter(s);
//        }
//        if (null != _onSearchTextChanged) {
//            _onSearchTextChanged.onSearchTextChanged(s);
//        }
//        return true;
//    }

    public interface SearchableItem<T> extends Serializable {
        void onSearchableItemClicked(T item, int position);
    }

    public interface OnSearchTextChanged {
        void onSearchTextChanged(String strText);
    }
}
