package com.bkav.mymusic;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

import java.io.IOException;
import java.text.SimpleDateFormat;

public class MediaPlaybackService extends Service {
    private static final String NOTIFICATION_CHANNEL_ID = "1";
    public static final String ACTION_PERVIOUS="xxx.yyy.zzz.ACTION_STOP";
    public static final String ACTION_PLAY="xxx.yyy.zzz.ACTION_PLAY";
    public static final String ACTION_NEXT="xxx.yyy.zzz.ACTION_PAUSE";
    private Binder binder = new MusicBinder();
       MediaPlayer sMediaPlayer = null;// sua
    private Listenner listenner;
    private String link = "";
    private String artist = "";
    private String nameSong = "";
    private int mPosition = 0;
    private int loop;


    @Override
    public void onCreate() {
        super.onCreate();

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (isMusicPlay()) {
            Log.d("getAction", intent.getAction() + "");
            switch (intent.getAction()) {
                case ACTION_PERVIOUS:
                    previousSong();
                    break;
                case ACTION_NEXT:
                    nextSong();
                    break;
                case ACTION_PLAY:
                    if (sMediaPlayer.isPlaying()) {
                        pauseSong();
                    } else {
                        playingSong();
                    }
                    break;
            }
        }
      showNotification(nameSong,artist,link);
        return super.onStartCommand(intent, flags, startId);
    }



    public void getListenner(Listenner listenner) {
        this.listenner=listenner;
    }

    public String getNameSong() {
        return nameSong;
    }

    public String getLink() {
        return link;
    }

    public String getArtist() {
        return artist;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public void setNameSong(String nameSong) {
        this.nameSong = nameSong;
    }

    public void showNotification(String nameSong, String artist,String path) {
        createNotificationChannel();

        Intent notificationIntent = new Intent(this, ActivityMusic.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

        Intent previousIntent = new Intent(ACTION_PERVIOUS);
        PendingIntent previousPendingIntent = null;

        Intent playIntent = new Intent(ACTION_PLAY);
        PendingIntent playPendingIntent = null;

        Intent nextIntent = new Intent(ACTION_NEXT);
        PendingIntent nextPendingIntent = null;

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            previousPendingIntent = PendingIntent.getForegroundService(this, 0, previousIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            playPendingIntent = PendingIntent.getService(getApplicationContext(), 0, playIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            nextPendingIntent = PendingIntent.getForegroundService(this, 0, nextIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        }

        RemoteViews mCustomContentView =new RemoteViews(getPackageName(), R.layout.sub_notification);
        RemoteViews mCustomBigContentView =new RemoteViews(getPackageName(), R.layout.notification);

        NotificationCompat.Builder builder=new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);
        builder.setSmallIcon(R.drawable.ic_music_note_black_24dp);
        builder.setPriority(NotificationCompat.PRIORITY_DEFAULT);
        builder.setStyle(new NotificationCompat.DecoratedCustomViewStyle());
        builder.setCustomContentView(mCustomContentView);
        builder.setCustomBigContentView(mCustomBigContentView);

        mCustomBigContentView.setTextViewText(R.id.textSongName, nameSong);
        mCustomBigContentView.setTextViewText(R.id.textNameSonger, artist);
        mCustomBigContentView.setImageViewResource(R.id.btnPlay, isMusicPlay() ? isPlaying() ? R.drawable.ic_pause : R.drawable.ic_play_arrow_black_24dp : R.drawable.ic_play_arrow_black_24dp);
        mCustomBigContentView.setOnClickPendingIntent(R.id.btnPrevious,previousPendingIntent);
        mCustomBigContentView.setOnClickPendingIntent(R.id.btnPlay,playPendingIntent);
        mCustomBigContentView.setOnClickPendingIntent(R.id.btnPrevious,nextPendingIntent);
        startForeground(1, builder.build());

//        Bitmap largeImage = BitmapFactory.decodeResource(getResources(), R.drawable.icon_disk2);
//        RemoteViews expandedView = new RemoteViews( getPackageName(), R.layout.notification1);
//
//        NotificationCompat.Builder mNotificationCompatBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);
//        mNotificationCompatBuilder .setSmallIcon(R.drawable.ic_menu_send)
//                .setStyle(new NotificationCompat.DecoratedCustomViewStyle())
//                .setCustomContentView(expandedView)
//                .setCustomBigContentView(expandedView)
//                .setContentIntent(pendingIntent) ;
//        NotificationManager mNotificationManager = (NotificationManager) getApplication().getSystemService(Context.NOTIFICATION_SERVICE);
//       // expandedView.setOnClickPendingIntent(R.id.btnPrevious,previousPendingIntent);
//       // expandedView.setOnClickPendingIntent(R.id.btnPause,playPendingIntent);
//      //  expandedView.setOnClickPendingIntent(R.id.btnPrevious,nextPendingIntent);
//        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(this);
//        notificationManagerCompat.notify(1, mNotificationCompatBuilder.build());

        // expandedView.setImageViewBitmap(R.id.img,imageArtist(path));


         //startForeground(1, notification1.build());

    }

    void createNotificationChannel(){
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel musicServiceChannel = new NotificationChannel(
                    NOTIFICATION_CHANNEL_ID,
                    "Music Service Channel",
                    NotificationManager.IMPORTANCE_HIGH
            );
            musicServiceChannel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(musicServiceChannel);
        }
    }

        public boolean isPlaying() {
        if (sMediaPlayer.isPlaying())
            return true;
        else
            return false;
    }

     public  int getDurationSong(){
        return  sMediaPlayer.getDuration();
     }

     public void seekToSong(int getProgress){
        sMediaPlayer.seekTo(getProgress);
     }

    public int getmPosition() {
        return mPosition;
    }

    public void setmPosition(int mPosition) {
        this.mPosition = mPosition;
    }

    public int getLoop() {
        return loop;
    }

    public void setLoop(int loop) {
        this.loop = loop;
    }

    public void playSong(Song songs) {
        Uri content_uri = Uri.parse(songs.getFile());
        sMediaPlayer = new MediaPlayer();
        try {
            sMediaPlayer.setDataSource(getApplicationContext(), content_uri);
            sMediaPlayer.prepare();
            sMediaPlayer.setWakeMode(getApplicationContext(),
                    PowerManager.PARTIAL_WAKE_LOCK);
            sMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

            listenner.onItemListenner();
        } catch (IOException e) {
            e.printStackTrace();
        }
         sMediaPlayer.start();
        link=songs.getFile();
        Log.d("link", link+"//");
      showNotification(songs.getTitle(), songs.getArtist(), link);
    }

    public void playingSong() {
        sMediaPlayer.start();
        if (listenner != null) {
            listenner.onItemListenner();
        }
        showNotification(nameSong, artist,link);
    }

    public void pauseSong() {
        sMediaPlayer.pause();
        if (listenner != null) {
            listenner.onItemListenner();
        }
        showNotification(nameSong, artist,link);
    }

    void previousSong() {
        listenner.actionPrevious();
    }

    public  void nextSong(){
      Log.d("next","next");
        listenner.actionNext();
    }

    public String getDuration() {
        SimpleDateFormat formmatTime = new SimpleDateFormat("mm:ss");
        return formmatTime.format(sMediaPlayer.getDuration());
    }

    public boolean isMusicPlay() {
        if (sMediaPlayer != null)
            return true;
        return false;
    }

    void UpdateTime(){
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                sMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer media) {


                    }
                });
                handler.postDelayed(this, 500);
            }
        }, 100);
    }

    public Bitmap imageArtist(String path){
        Log.d("path", path+"//");
        MediaMetadataRetriever mediaMetadataRetriever=new MediaMetadataRetriever();
        mediaMetadataRetriever.setDataSource(path);
        byte [] data=mediaMetadataRetriever.getEmbeddedPicture();
        if(data!=null){
            return BitmapFactory.decodeByteArray(data, 0 , data.length);
        }
        return null;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    public interface Listenner {
        void onItemListenner();
        void  actionPrevious();
        void actionNext();
    }

    class MusicBinder extends Binder {
        public MediaPlaybackService getMusicBinder()
        {
            return MediaPlaybackService.this;
        }
    }
}