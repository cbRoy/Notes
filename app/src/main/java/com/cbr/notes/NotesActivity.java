package com.cbr.notes;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;


public class NotesActivity extends Activity
        implements ListNotesFragment.ListNotesActionListener,
        NoteFragment.NoteActionListener {

    private NotesDbAdapter mDbHelper;
    private ListNotesFragment mListNoteFragment;

    @Override
    protected void onCreate(Bundle inState) {
        super.onCreate(inState);
        setContentView(R.layout.activity_main);
        if (inState == null) {
            mDbHelper = new NotesDbAdapter(this);
            mDbHelper.open();
            showList();
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                Toast.makeText(this, "yo dawg!", Toast.LENGTH_LONG).show();
                return true;
            default:
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void openNote(long id) {
        Cursor c = mDbHelper.fetchNote(id);
        String title = c.getString(c.getColumnIndex("title"));
        String body = c.getString(c.getColumnIndex("body"));
        Fragment note = NoteFragment.newInstance(id, title, body);
        FragmentManager fm = getFragmentManager();
        fm.beginTransaction()
                .replace(R.id.content_frame, note)
                .addToBackStack(null)
                .commit();
    }

    public void showList() {
        mListNoteFragment = ListNotesFragment.newInstance(getData());
        FragmentManager fm = getFragmentManager();
        fm.beginTransaction()
                .replace(R.id.content_frame, mListNoteFragment)
                .commit();
    }

    private void updateList() {
        mListNoteFragment.updateData(getData());
    }

    private CustomSimpleCursorAdapter getData() {
        Cursor c = mDbHelper.fetchAll();

        String[] from = new String[]{NotesDbAdapter.KEY_TITLE};
        int[] to = new int[]{R.id.textView};
        return new CustomSimpleCursorAdapter(this, R.layout.notes_row, c, from, to);
    }


    @Override
    public void onClickItem(long id) {
        openNote(id);
    }

    @Override
    public void onClickAddNew() {
        Fragment note = NoteFragment.newInstance();
        FragmentManager fm = getFragmentManager();
        fm.beginTransaction()
                .replace(R.id.content_frame, note)
                .addToBackStack(null)
                .commit();

    }

    @Override
    public void onDeleteSelectedItems(long[] ids) {
        for (long id : ids) {
            mDbHelper.deleteNote(id);
        }
        updateList();
    }

    @Override
    public void onNoteSaved(long id, String title, String body) {
        if (id == NoteFragment.NEW_NOTE) {
            mDbHelper.createNote(title, body);
        } else {
            mDbHelper.updateNote(id, title, body);
        }
        updateList();
        InputMethodManager imm = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(getWindow().getDecorView().getWindowToken(), 0);
    }
}
