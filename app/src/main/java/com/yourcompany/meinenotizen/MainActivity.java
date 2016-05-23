package com.yourcompany.meinenotizen;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;


import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.stmt.DeleteBuilder;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private ListView listView;
    private ArrayAdapter<String> adapter;

    private DatabaseHelper databaseHelper = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Read the saved notes from Database
        List<Note> notes = Collections.EMPTY_LIST;
        try {
            notes = getDBHelper().getNoteDao().queryForAll();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        //Map the notes to a list of Strings
        List<String> data = new ArrayList<>();
        for (Note note : notes) {
            data.add(note.getContent());
        }

        //Create an adapter to couple the ListView with the data
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, android.R.id.text1, data);

        listView = (ListView) findViewById(R.id.list);
        listView.setAdapter(adapter);

        //set the listener to delete notes on longclick
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                String item = (String) listView.getItemAtPosition(position);

                try {
                    //Get deleteBuilder from NoteDao
                    DeleteBuilder<Note, Integer> deleteBuilder = getDBHelper().getNoteDao().deleteBuilder();
                    //Only delete the matching item
                    deleteBuilder.where().eq("content", item);
                    deleteBuilder.delete();
                } catch (SQLException e) {
                    e.printStackTrace();
                }

                adapter.remove(item);
                return false;
            }
        });
    }

    //invoked when user clicks the Add-Button
    public void onAddNote(View v) {
        //Get text from EditText
        EditText newNote = (EditText) findViewById(R.id.etNewNote);
        String content = newNote.getText().toString();
        newNote.setText("");

        //Create new note
        Note note = new Note();
        note.setContent(content);

        try {
            //Save note in database
            getDBHelper().getNoteDao().create(note);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        //Add text to ListView
        adapter.add(note.getContent());
    }

    private DatabaseHelper getDBHelper() {
        if (databaseHelper == null) {
            databaseHelper = OpenHelperManager.getHelper(this, DatabaseHelper.class);
        }
        return databaseHelper;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (databaseHelper != null) {
            OpenHelperManager.releaseHelper();
            databaseHelper = null;
        }
    }
}
