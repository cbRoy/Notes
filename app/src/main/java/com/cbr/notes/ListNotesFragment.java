package com.cbr.notes;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import com.melnykov.fab.FloatingActionButton;


/**
 * A placeholder fragment containing a simple view.
 */
public class ListNotesFragment extends Fragment {

    private static ListView listViewNotes;
    private static SimpleCursorAdapter mAdapter;

    private ListNotesActionListener mListener;

    public ListNotesFragment() {
        //empty constructor
    }

    public static ListNotesFragment newInstance(SimpleCursorAdapter adapter){
        ListNotesFragment lnf = new ListNotesFragment();
        mAdapter = adapter;
        return lnf;
    }

    public void updateData(SimpleCursorAdapter adapter){
        mAdapter = adapter;
        listViewNotes.setAdapter(mAdapter);
    }

    public interface ListNotesActionListener{
        void onClickAddNew();
        void onClickItem(long id);
        void onDeleteSelectedItems(long[] ids);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_main, container, false);
        listViewNotes = (ListView)v.findViewById(R.id.listViewNotes);
        listViewNotes.setAdapter(mAdapter);
        listViewNotes.setItemsCanFocus(false);
        listViewNotes.setMultiChoiceModeListener(new ListMultiChoiceModeListener());
        listViewNotes.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mListener.onClickItem(id);
            }
        });
        listViewNotes.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                listViewNotes.setItemChecked(position, true);
                return true;
            }
        });
        FloatingActionButton fab = (FloatingActionButton)v.findViewById(R.id.fab);
        fab.attachToListView(listViewNotes);
        fab.setOnClickListener(new FABClickListener());
        return v;

    }
    private class ListMultiChoiceModeListener implements AbsListView.MultiChoiceModeListener{

        @Override
        public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
            final int checkedCount = listViewNotes.getCheckedItemCount();
            mode.setTitle(checkedCount + " Notes Selected");
        }

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            MenuInflater inflater = getActivity().getMenuInflater();
            inflater.inflate(R.menu.menu_cabactions, menu);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            int id = item.getItemId();
            if(id == R.id.action_delete){
                mListener.onDeleteSelectedItems(listViewNotes.getCheckedItemIds());
                mode.finish();
            }
            return false;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            listViewNotes.clearChoices();
        }
    }
    private class FABClickListener implements View.OnClickListener{
        public void onClick(View v){
            mListener.onClickAddNew();
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (ListNotesActionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

}
