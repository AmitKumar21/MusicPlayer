package com.example.amk52.unimp3;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.PorterDuff.Mode;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class MainActivity extends Activity {
    String LOG_CLASS = "MainActivity";
    CustomAdapter customAdapter = null;
    static TextView playingSong;
    Button btnPlayer;
    static Button btnPause, btnPlay, btnNext, btnPrevious;
    Button btnStop;
    LinearLayout mediaLayout;
    static LinearLayout linearLayoutPlayingSong;
    ListView mediaListView;
    ProgressBar progressBar;
    TextView textBufferDuration, textDuration;
    static ImageView imageViewAlbumArt;
    static Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActionBar().hide();
        setContentView(R.layout.activity_main);
        context = MainActivity.this;
        init();
    }

    private void init() {
        getViews();
        setListeners();
        playingSong.setSelected(true);
        progressBar.getProgressDrawable().setColorFilter(getResources().getColor(R.color.white), Mode.SRC_IN);
        if(PlayerConstants.SONGS_LIST.size() <= 0){
            PlayerConstants.SONGS_LIST = UtilFunctions.listOfSongs(getApplicationContext());
        }
        setListItems();
    }

    private void setListItems() {
        customAdapter = new CustomAdapter(this,R.layout.custom_list, PlayerConstants.SONGS_LIST);
        mediaListView.setAdapter(customAdapter);
        mediaListView.setFastScrollEnabled(true);
    }

    private void getViews() {
        playingSong = (TextView) findViewById(R.id.textNowPlaying);
        btnPlayer = (Button) findViewById(R.id.btnMusicPlayer);
        mediaListView = (ListView) findViewById(R.id.listViewMusic);
        mediaLayout = (LinearLayout) findViewById(R.id.linearLayoutMusicList);
        btnPause = (Button) findViewById(R.id.btnPause);
        btnPlay = (Button) findViewById(R.id.btnPlay);
        linearLayoutPlayingSong = (LinearLayout) findViewById(R.id.linearLayoutPlayingSong);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        btnStop = (Button) findViewById(R.id.btnStop);
        textBufferDuration = (TextView) findViewById(R.id.textBufferDuration);
        textDuration = (TextView) findViewById(R.id.textDuration);
        imageViewAlbumArt = (ImageView) findViewById(R.id.imageViewAlbumArt);
        btnNext = (Button) findViewById(R.id.btnNext);
        btnPrevious = (Button) findViewById(R.id.btnPrevious);
    }

    private void setListeners() {
        mediaListView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View item, int position, long id){
                Log.d("TAG", "TAG Tapped INOUT(IN)");
                PlayerConstants.SONG_PAUSED = false;
                PlayerConstants.SONG_NUMBER = position;
                boolean isServiceRunning = UtilFunctions.isServiceRunning(SongService.class.getName(), getApplicationContext());
                if (!isServiceRunning) {
                    Intent i = new Intent(getApplicationContext(),SongService.class);
                    startService(i);
                } else {
                    PlayerConstants.SONG_CHANGE_HANDLER.sendMessage(PlayerConstants.SONG_CHANGE_HANDLER.obtainMessage());
                }
                updateUI();
                changeButton();
                Log.d("TAG", "TAG Tapped INOUT(OUT)");
            }
        });

        btnPlayer.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this,AudioPlayerActivity.class);
                startActivity(i);
            }
        });
        btnPlay.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Controls.playControl(getApplicationContext());
            }
        });
        btnPause.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Controls.pauseControl(getApplicationContext());
            }
        });
        btnNext.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                Controls.nextControl(getApplicationContext());
            }
        });
        btnPrevious.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                Controls.previousControl(getApplicationContext());
            }
        });
        btnStop.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), SongService.class);
                stopService(i);
                linearLayoutPlayingSong.setVisibility(View.GONE);
            }
        });
        imageViewAlbumArt.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this,AudioPlayerActivity.class);
                startActivity(i);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        try{
            boolean isServiceRunning = UtilFunctions.isServiceRunning(SongService.class.getName(), getApplicationContext());
            if (isServiceRunning) {
                updateUI();
            }else{
                linearLayoutPlayingSong.setVisibility(View.GONE);
            }
            changeButton();
            PlayerConstants.PROGRESSBAR_HANDLER = new Handler(){
                @Override
                public void handleMessage(Message msg){
                    Integer i[] = (Integer[])msg.obj;
                    textBufferDuration.setText(UtilFunctions.getDuration(i[0]));
                    textDuration.setText(UtilFunctions.getDuration(i[1]));
                    progressBar.setProgress(i[2]);
                }
            };
        }catch(Exception e){}
    }

    @SuppressWarnings("deprecation")
    public static void updateUI() {
        try{
            MediaItem data = PlayerConstants.SONGS_LIST.get(PlayerConstants.SONG_NUMBER);
            playingSong.setText(data.getTitle() + " " + data.getArtist() + "-" + data.getAlbum());
            Bitmap albumArt = UtilFunctions.getAlbumart(context, data.getAlbumId());
            if(albumArt != null){
                imageViewAlbumArt.setBackgroundDrawable(new BitmapDrawable(albumArt));
            }else{
                imageViewAlbumArt.setBackgroundDrawable(new BitmapDrawable(UtilFunctions.getDefaultAlbumArt(context)));
            }
            linearLayoutPlayingSong.setVisibility(View.VISIBLE);
        }catch(Exception e){}
    }

    public static void changeButton() {
        if(PlayerConstants.SONG_PAUSED){
            btnPause.setVisibility(View.GONE);
            btnPlay.setVisibility(View.VISIBLE);
        }else{
            btnPause.setVisibility(View.VISIBLE);
            btnPlay.setVisibility(View.GONE);
        }
    }

    public static void changeUI(){
        updateUI();
        changeButton();
    }
}