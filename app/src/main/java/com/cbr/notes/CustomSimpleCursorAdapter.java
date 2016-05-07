package com.cbr.notes;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Typeface;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

/**
 * Created by chris on 01/08/15.
 */
public class CustomSimpleCursorAdapter extends SimpleCursorAdapter {

    private Context mContext;
    private int mLayout;
    private Cursor mCursor;
    private final LayoutInflater mInflater;




    public CustomSimpleCursorAdapter(Context context, int layout, Cursor cursor, String[] from, int[] to){
        super(context, layout, cursor, from, to, 0);
        this.mContext = context;
        this.mLayout = layout;
        this.mCursor = cursor;
        this.mInflater = LayoutInflater.from(context);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return mInflater.inflate(mLayout,null);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        super.bindView(view, context, cursor);

        TextView txtListItem = (TextView)view.findViewById(R.id.textView);
        txtListItem.setTypeface(Typeface.createFromAsset(mContext.getAssets(), "fonts/JustAnotherHand.ttf"));
        txtListItem.setTextSize(TypedValue.COMPLEX_UNIT_SP, 30);

        int titleColumn = cursor.getColumnIndex(NotesDbAdapter.KEY_TITLE);

        txtListItem.setText(cursor.getString(titleColumn));

    }
}
