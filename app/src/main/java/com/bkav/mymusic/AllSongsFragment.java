package com.bkav.mymusic;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class AllSongsFragment extends BaseSongListFragment implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final int LOADER_ID = 1;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        getLoaderManager().initLoader(LOADER_ID, null, this);
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
        String[] projection = {MediaStore.Audio.AudioColumns.DATA, MediaStore.Audio.AudioColumns.ALBUM, MediaStore.Audio.ArtistColumns.ARTIST, MediaStore.Audio.Media.TITLE, MediaStore.Audio.Media.DURATION};
        CursorLoader cursorLoader = new CursorLoader(getContext(), MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, projection, null, null, null);
        return cursorLoader;
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor c) {
        ArrayList<Song> listMusic = new ArrayList<>();
        int id = 0;
        if (c != null) {
            c.moveToFirst();
            do{
                String path = c.getString(0);
                String album = c.getString(1);
                String artist = c.getString(2);
                String name = c.getString(3);
                String duration = c.getString(4);
                listMusic.add(new Song(id, name, path, artist, Integer.parseInt(duration)));
                id++;
                Log.d(TAG , " Album :" + album);
                Log.d("Path :" + path, " Artist :" + artist + " Duration " + duration);
            }while (c.moveToNext());
        }
        mAdapter.updateList(listMusic);
        setSong(listMusic);
    }

    private static final String TAG = "AllSongsFragment";

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
       if(mAdapter!=null){
           mAdapter.setSong(new ArrayList<Song>());
       }
    }
}
