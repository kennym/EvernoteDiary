package com.kennymeyer.greendiary;

import android.app.Activity;
import android.os.Bundle;
import android.app.Fragment;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.text.Html;
import android.util.Log;
import android.view.*;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.evernote.client.android.OnClientCallback;
import com.evernote.edam.type.Note;
import com.evernote.thrift.transport.TTransportException;


public class NoteFragment extends Fragment {

    protected String noteGuid;
    protected EditText noteContentView;
    protected MainActivity activity;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        setHasOptionsMenu(true);

        LinearLayout ll = (LinearLayout ) inflater.inflate(R.layout.note_fragment, container, false);
        noteContentView = (EditText) ll.findViewById(R.id.note_content);

        noteGuid = getArguments().getString("guid", "");
        String content = getArguments().getString("content", "");

        noteContentView.setText(Html.fromHtml(content));

        // Inflate the layout for this fragment
        return ll;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // handle item selection
        switch (item.getItemId()) {
            case R.id.action_save:
                try {
                    saveNote();
                } catch (TTransportException e) {
                    Log.e("GreenDiary", "Received exception: ");
                    Log.e("GreenDiary", e.toString());
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        // If the nav drawer is open, hide action items related to the content view
        menu.findItem(R.id.action_sync).setVisible(false);
        menu.findItem(R.id.action_save).setVisible(true);
    }

    /* Sync note with Evernote */
    public void saveNote() throws TTransportException {
        final MainActivity activity = ((MainActivity) getActivity());

        String noteBody = noteContentView.getText().toString();

        String nBody = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
        nBody += "<!DOCTYPE en-note SYSTEM \"http://xml.evernote.com/pub/enml2.dtd\">";
        nBody += "<en-note>" + noteBody + "</en-note>";

        Note newNote = new Note();
        newNote.setTitle(activity.getActionBar().getTitle().toString());
        newNote.setContent(nBody);

        if (activity.mDiaryNotebook != null && activity.mDiaryNotebook.isSetGuid()) {
            newNote.setNotebookGuid(activity.mDiaryNotebook.getGuid());
        }

        activity.mEvernoteSession.getClientFactory().createNoteStoreClient().createNote(newNote, new OnClientCallback<Note>() {
            @Override
            public void onSuccess(Note note) {
                activity.showLoadingSpinner();
                activity.addNote(note);
                activity.stopLoadingSpinner();
            }

            @Override
            public void onException(Exception e) {
                Log.e("GreenDiary", "Exception ocurred");
            }
        });

    }
}
