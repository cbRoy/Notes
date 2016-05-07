package com.cbr.notes;

import android.app.Activity;
import android.app.Fragment;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.melnykov.fab.FloatingActionButton;


public class NoteFragment extends Fragment {
    public static final long NEW_NOTE = -1;

    private static final String ARG_ID    = "id";
    private static final String ARG_TITLE = "title";
    private static final String ARG_BODY  = "body";

    private long   mId = -1;
    private String mTitle;
    private String mBody;


    private EditText txtTitle;
    private LinedEditText txtBody;

    private NoteActionListener mListener;

    public static NoteFragment newInstance(){
        return newInstance(NEW_NOTE,"",""); //new note -1 id, blank title and body
    }

    public static NoteFragment newInstance(long id, String title, String body) {
        NoteFragment fragment = new NoteFragment();
        Bundle args = new Bundle();
        args.putLong(ARG_ID,id);
        args.putString(ARG_TITLE, title);
        args.putString(ARG_BODY, body);
        fragment.setArguments(args);
        return fragment;
    }


    public NoteFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mId = getArguments().getLong(ARG_ID);
            mTitle = getArguments().getString(ARG_TITLE);
            mBody = getArguments().getString(ARG_BODY);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle inState) {
        View v = inflater.inflate(R.layout.fragment_note, container, false);

        txtTitle = (EditText)v.findViewById(R.id.txtTitle);
        txtBody = (LinedEditText)v.findViewById(R.id.txtBody);

        if(inState != null){
            mId = inState.getLong(ARG_ID);
            mTitle = inState.getString(ARG_TITLE);
            mBody = inState.getString(ARG_BODY);
        }

        txtTitle.setText(mTitle);
        txtBody.setText(mBody);
        Typeface font = Typeface.createFromAsset(getActivity().getAssets(), "fonts/JustAnotherHand.ttf");
        txtTitle.setTypeface(font);
        txtTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 40);
        txtBody.setTypeface(font);
        txtBody.setTextSize(TypedValue.COMPLEX_UNIT_SP, 40);


        FloatingActionButton fab = (FloatingActionButton) v.findViewById(R.id.fab);
        fab.show();
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (txtTitle.getText().toString().trim().length() == 0) {
                    Toast.makeText(getActivity(), "Note must have a title", Toast.LENGTH_SHORT).show();
                } else if (txtBody.getText().toString().trim().length() == 0) {
                    Toast.makeText(getActivity(), "Note must have a body", Toast.LENGTH_SHORT).show();
                } else {
                    mListener.onNoteSaved(mId, txtTitle.getText().toString(), txtBody.getText().toString());
                    getActivity().getFragmentManager().popBackStack();
                }
            }
        });
        return v;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (NoteActionListener) activity;
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

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putLong(ARG_ID, mId);
        outState.putString(ARG_TITLE, mTitle);
        outState.putString(ARG_BODY, mBody);
    }

    interface NoteActionListener {
        void onNoteSaved(long id, String title, String body);
    }

}
