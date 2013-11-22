package com.kennymeyer.greendiary;

import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.widget.EditText;
import com.evernote.client.android.OnClientCallback;
import com.evernote.edam.type.Note;
import com.evernote.thrift.transport.TTransportException;

public class NoteDetailActivity extends BaseActivity {

    protected String noteGuid;
    protected EditText noteContentView;
    protected MainActivity activity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.note_detail);

        noteContentView = (EditText) findViewById(R.id.note_content);

        noteGuid = getIntent().getExtras().getString("guid", "");
        String content = getIntent().getExtras().getString("content", "");

        noteContentView.setText(Html.fromHtml(content));
    }

    /* Sync note with Evernote */
    public void saveNote() throws TTransportException {

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
