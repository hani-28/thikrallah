package com.HMSolutions.thikrallah;

import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

//import android.support.v7.app.ActionBarActivity;

/**
 * Created by hani on 6/4/16.
 */
public class MediaBrowser extends AppCompatActivity {
    private static final String TAG = "MediaBrowser";
    private File file;
    private List<String> myList;
    private ListView listView;
    private TextView pathTextView;
    private List<AudioModel> AllAudioFiles;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.media_browser
        );

        listView = (ListView) findViewById(R.id.pathlist);
        pathTextView = (TextView) findViewById(R.id.path);
        myList = new ArrayList<String>();
        AllAudioFiles = getAllAudioFromDevice(this.getApplicationContext());
        for (int i = 0; i < AllAudioFiles.size(); i++) {
            Log.d(TAG, "i=" + i);
            myList.add(AllAudioFiles.get(i).getaName());
        }

        pathTextView.setText("Select Audio File from below:");
        listView.setAdapter(new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, myList));

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int position,
                                    long arg3) {
                    Intent data = new Intent();
//---set the data to pass back---
                data.putExtra("FILE", AllAudioFiles.get(position).getUri().toString());
                setResult(RESULT_OK, data);
//---close the activity---
                    finish();
            }
        });



    }

    private int getAudioFileCount(String dirPath) {

        String selection = MediaStore.Audio.Media.DATA +" like ?";
        String[] projection = {MediaStore.Audio.Media.DATA};
        String[] selectionArgs={dirPath+"%"};
        Cursor cursor = this.managedQuery(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                projection,
                selection,
                selectionArgs,
                null);
        return cursor.getCount();
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }


    public List<AudioModel> getAllAudioFromDevice(final Context context) {

        final List<AudioModel> tempAudioList = new ArrayList<>();

        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String[] projection = {MediaStore.Audio.AudioColumns.DATA, MediaStore.Audio.AudioColumns.ALBUM, MediaStore.Audio.ArtistColumns.ARTIST, MediaStore.Audio.Media._ID};
        Cursor c = context.getContentResolver().query(
                uri,
                projection,
                null,
                null,
                null);

        if (c != null) {
            while (c.moveToNext()) {

                AudioModel audioModel = new AudioModel();
                String path = c.getString(0);
                String album = c.getString(1);
                String artist = c.getString(2);
                long id = c.getLong(3);

                String name = path.substring(path.lastIndexOf("/") + 1);

                audioModel.setaName(name);
                audioModel.setaAlbum(album);
                audioModel.setaArtist(artist);
                audioModel.setaPath(path);
                audioModel.setId(id);
                Uri file_uri = ContentUris.withAppendedId(
                        MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id);
                audioModel.setUri(file_uri);
                Log.e("Name :" + name, " Album :" + album);
                Log.e("Path :" + path, " Artist :" + artist);

                tempAudioList.add(audioModel);
            }
            c.close();
        }

        return tempAudioList;
    }

    private static class AudioModel {

        String aPath;
        String aName;
        String aAlbum;
        String aArtist;
        Uri uri;
        long id;

        public Uri getUri() {
            return uri;
        }

        public long getId() {
            return id;
        }


        public void setUri(Uri uri) {
            this.uri = uri;
        }

        public void setId(long id) {
            this.id = id;
        }


        public String getaPath() {
            return aPath;
        }

        public void setaPath(String aPath) {
            this.aPath = aPath;
        }

        public String getaName() {
            return aName;
        }

        public void setaName(String aName) {
            this.aName = aName;
        }

        public String getaAlbum() {
            return aAlbum;
        }

        public void setaAlbum(String aAlbum) {
            this.aAlbum = aAlbum;
        }

        public String getaArtist() {
            return aArtist;
        }

        public void setaArtist(String aArtist) {
            this.aArtist = aArtist;
        }
    }

}


