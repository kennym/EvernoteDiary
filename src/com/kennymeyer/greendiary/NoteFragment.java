package com.kennymeyer.greendiary;

import android.os.Bundle;
import android.app.Fragment;
import android.text.Html;
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
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.note_detail_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // handle item selection
        switch (item.getItemId()) {
            case R.id.action_sync:
                syncNote();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /* Sync note with Evernote */
    public void syncNote() {
        // TODO:
    }
}
