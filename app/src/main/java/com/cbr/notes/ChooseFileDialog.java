package com.cbr.notes;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnKeyListener;
import android.os.Bundle;
import android.os.Environment;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


// TODO::
//			not sure - so far seems everything is working fine.
//			rotate on FileChooser causes hidden files to show
//				changing filterExtensions to a static method seems to work
//				and adding !file.isHidden() to the file chooser part does too...

/************************************************************************************
 * Usage Example:																	*
 * 																					*
 * 	ChooseFileDialog.setContext(context);											*
 *	ChooseFileDialog chooserDialog = ChooseFileDialog.newInstance(					*
 *		R.string.TITLE_ID_GOES_HERE,												*
 *		ChooseFileDialog.FILE_CHOOSER, // or ChooseFileDialog.DIRECTORY_CHOOSER		*
 *		new ChooseFileDialog.onFileChosenListener() {								*
 *			@Override																*
 *			public void onFileChosen(String chosenFile) {							*
 *				//here is where you do whatever with the chosen file/dir			*
 *			}																		*
 *		});																			*
 *	chooserDialog.setStartDirectory(SOME_STRING_HERE);		//not required			*
 *	chooserDialog.filterExtensions(new String[] {"db"});	//not required			*
 *	chooserDialog.show(((Activity) context).getFragmentManager(),					*
 *			"ChooseFileDialog.FILE_CHOOSER");										*
 * 																					*
 * 																					*
 * 																					*
 ************************************************************************************/

public class ChooseFileDialog extends DialogFragment {

    public static final int FILE_CHOOSER = 1;
    public static final int DIRECTORY_CHOOSER = 2;

    private static int s_dialogType;
    private static Context s_context;
    private static onFileChosenListener s_listener;

    private TextView m_titleView;

    private String m_sdcardDirectory = Environment.getExternalStorageDirectory().toString();
    private String m_baseDir = m_sdcardDirectory;;

    private List<String> m_dirsQueue = new ArrayList<String>();
    private List<String> m_curdirs = null;

    private static boolean m_checkExtension = false;
    private static String[] m_extensions;


    public static ChooseFileDialog newInstance(int title, int typeOfDialog,
                                               onFileChosenListener listener) {
        ChooseFileDialog frag = new ChooseFileDialog();
        Bundle args = new Bundle();
        args.putInt("title", title);
        frag.setArguments(args);
        s_dialogType = typeOfDialog;
        s_listener = listener;
        return frag;
    }

    public static void setContext(Context c) {
        s_context = c;
    }

    public void setStartDirectory(String directory) {
        if (!directory.isEmpty()){
            m_baseDir = directory;
            File dir = new File(directory); //check to make sure starting
            if(!dir.isDirectory()){			//directory, is not a file.
                m_baseDir = dir.getParent(); 	//if it is, set baseDir to the parent dir of file
            }
        }
    }

    public static void filterExtensions(String[] extensions) {
        m_checkExtension = true;
        m_extensions = extensions;
    }

    public interface onFileChosenListener {
        public void onFileChosen(String chosenFile);
    }

    public void setOnFileChosenListener(onFileChosenListener listener) {
        s_listener = listener;
    }

    @Override
    public void onAttach(Activity activity) {
        if (s_listener == null) {
            try {
                s_listener = (onFileChosenListener) activity;
            } catch (ClassCastException e) {
                throw new ClassCastException(
                        activity.toString()
                                + " must implement onFileChosenListener, or use setOnFileChosenListener");
            }
        }
        super.onAttach(activity);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        m_dirsQueue.clear();

        try {
            final String sdcardCanonicalPath = new File(m_sdcardDirectory)
                    .getCanonicalPath();
            String dirPath = new File(m_baseDir).getCanonicalPath();

            while (!dirPath.equals(sdcardCanonicalPath)) {
                m_dirsQueue.add(0, dirPath);
                dirPath = new File(dirPath).getParent();
            }

            m_dirsQueue.add(0, sdcardCanonicalPath);
            m_curdirs = getDirectories(m_baseDir);
        } catch (IOException ioe) {
        }

        Dialog dialog = null;

        AlertDialog.Builder builder = new AlertDialog.Builder(s_context);

        m_titleView = new TextView(s_context);
        m_titleView.setTextAppearance(s_context,
                android.R.style.TextAppearance_Large);
        m_titleView.setTextColor(s_context.getResources().getColor(
                android.R.color.holo_blue_dark));
        m_titleView.setGravity(Gravity.CENTER_VERTICAL
                | Gravity.CENTER_HORIZONTAL);

        builder.setCustomTitle(m_titleView);
        m_titleView.setText(m_baseDir);

        ArrayAdapter<String> listAdapter = createListAdapter(m_curdirs);

        builder.setSingleChoiceItems(listAdapter, -1,
                new DirectoryOnClickListener());

        if (s_dialogType == DIRECTORY_CHOOSER) { // don't need a positive button
            // when choosing a file
            builder.setPositiveButton("Ok",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (s_listener != null) {
                                String chosenDir = m_dirsQueue.get(m_dirsQueue.size() - 1);
                                s_listener.onFileChosen(chosenDir);
                                dialog.dismiss();
                            }
                        }
                    });
        }
        builder.setNegativeButton("Cancel",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

        dialog = builder.create();

        dialog.setOnKeyListener(new OnKeyListener() {
            @SuppressWarnings("unchecked")
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode,
                                 KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK
                        && event.getAction() == KeyEvent.ACTION_DOWN) {
                    // Back button pressed
                    if (m_dirsQueue.size() == 1) {
                        // The very top level directory, do nothing
                        return false;
                    } else {
                        // Navigate back to an upper directory
                        m_dirsQueue.remove(m_dirsQueue.size() - 1);
                        String newDir = m_dirsQueue.get(m_dirsQueue.size() - 1);

                        m_curdirs.clear();
                        m_curdirs.addAll(getDirectories(newDir));
                        m_titleView.setText(newDir);

                        ((ArrayAdapter<String>) ((AlertDialog) dialog)
                                .getListView().getAdapter())
                                .notifyDataSetChanged();
                    }
                    return true;
                } else {
                    return false;
                }
            }
        });
        dialog.show();
        return dialog;
    }

    private ArrayAdapter<String> createListAdapter(List<String> items) {
        return new ArrayAdapter<String>(s_context,
                android.R.layout.select_dialog_item, android.R.id.text1, items) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View v = super.getView(position, convertView, parent);
                if (v instanceof TextView) {
                    // Enable list item (directory) text wrapping
                    TextView tv = (TextView) v;
                    tv.getLayoutParams().height = LayoutParams.WRAP_CONTENT;
                    tv.setEllipsize(null);
                }
                return v;
            }
        };
    }

    class DirectoryOnClickListener implements DialogInterface.OnClickListener {
        @SuppressWarnings("unchecked")
        public void onClick(DialogInterface dialog, int item) {
            String newDir;
            String chosenItem = (String) ((AlertDialog) dialog).getListView()
                    .getAdapter().getItem(item);
            if (chosenItem.equals("..")) {
                m_dirsQueue.remove(m_dirsQueue.size() - 1);
                newDir = m_dirsQueue.get(m_dirsQueue.size() - 1);
            } else {
                // Navigate into the sub-directory
                newDir = m_dirsQueue.get(m_dirsQueue.size() - 1) + "/"
                        + chosenItem;
                if (!new File(newDir).isDirectory()) {
                    // file chosen, don't move into sub-directory, select file instead
                    if (s_listener != null) {
                        s_listener.onFileChosen(newDir);
                        dialog.dismiss();
                    }else throw new NullPointerException("Must set a listener for ChooseFileDialog.");
                }
                m_dirsQueue.add(newDir);
            }
            List<String> dirs = getDirectories(newDir);
            if (dirs.size() > 1 | (s_dialogType == FILE_CHOOSER)) {
                m_curdirs.clear();
                m_curdirs.addAll(dirs);
                m_titleView.setText(newDir);
                ((ArrayAdapter<String>) ((AlertDialog) dialog).getListView()
                        .getAdapter()).notifyDataSetChanged();
            } else {
                if (s_listener != null) {
                    String chosenDir = m_dirsQueue.get(m_dirsQueue.size() - 1);
                    s_listener.onFileChosen(chosenDir);
                    dialog.dismiss();
                }else throw new NullPointerException("Must set a listener for ChooseFileDialog.");
            }
        }
    }

    private List<String> getDirectories(String dir) {
        List<String> dirs = new ArrayList<String>();
        if (!dir.equalsIgnoreCase(m_sdcardDirectory))
            dirs.add("..");
        try {
            File dirFile = new File(dir);
            if (!dirFile.exists() || !dirFile.isDirectory()) {
                return dirs;
            }
            for (File file : dirFile.listFiles()) {
                if (file.isDirectory() && !file.isHidden()) {
                    dirs.add(file.getName());
                } else if (s_dialogType == FILE_CHOOSER && !file.isHidden()) {
                    int periodIndex = file.toString().lastIndexOf('.');
                    if (periodIndex != -1) {
                        String ext = file.toString().substring(periodIndex + 1);
                        if (m_checkExtension) {
                            if (Arrays.asList(m_extensions).contains(ext)) {
                                dirs.add(file.getName());
                            }
                        } else {
                            dirs.add(file.getName());
                        }
                    }
                }
            }
        } catch (Exception e) {
        }

        Collections.sort(dirs, new Comparator<String>() {
            public int compare(String c1, String c2) {
                return c1.compareToIgnoreCase(c2);
            }
        });
        return dirs;
    }
}