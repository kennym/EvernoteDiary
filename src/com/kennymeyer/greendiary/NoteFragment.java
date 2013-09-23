package com.kennymeyer.greendiary;

import android.os.Bundle;
import android.app.Fragment;
import android.text.Html;
import android.view.View;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.evernote.client.android.OnClientCallback;
import com.evernote.edam.type.Note;
import com.evernote.thrift.transport.TTransportException;


public class NoteFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        LinearLayout ll = (LinearLayout )inflater.inflate(R.layout.note_fragment, container, false);
        EditText note_content = (EditText) ll.findViewById(R.id.note_content);

        String content = getArguments().getString("content", "Nothing");

        note_content.setText(Html.fromHtml(content));

        // Inflate the layout for this fragment
        return ll;
    }

}
