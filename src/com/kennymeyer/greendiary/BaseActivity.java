package com.kennymeyer.greendiary;

import android.app.Activity;
import com.evernote.client.android.EvernoteSession;
import com.evernote.edam.type.Notebook;

import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: kenny
 * Date: 11/22/13
 * Time: 5:18 PM
 * To change this template use File | Settings | File Templates.
 */
public class BaseActivity extends Activity {
    public static final String CONSUMER_KEY = "kennymeyer-7705";
    public static final String CONSUMER_SECRET = "4ef2aa3a5e4dc2eb";
    public static final EvernoteSession.EvernoteService EVERNOTE_SERVICE = EvernoteSession.EvernoteService.SANDBOX;

    public List<Map<String, String>> notes;

    protected EvernoteSession mEvernoteSession;
    protected Notebook mDiaryNotebook;
    protected NoteReaderDbHelper mDbHelper;

}
