package wcfb.com.music;

import android.app.ListActivity;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends ListActivity {
    private TextView textView;
    private Button start;
    private Button stop;
    private Button pause;
    private Button next;
    private Button last;
    private SeekBar seekBar;
    private MediaPlayer myMediaPlayer;
    private List<Song> myMusicList=new ArrayList<>();
    private int currentListItem=0;


    PermissionHelper permissionHelper = new PermissionHelper();
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textView = findViewById(R.id.textView);
        start = findViewById(R.id.start);
        stop = findViewById(R.id.stop);
        next = findViewById(R.id.next);
        pause = findViewById(R.id.pause);
        last = findViewById(R.id.last);
        seekBar = findViewById(R.id.seekBar);

        myMediaPlayer = new MediaPlayer();
        asdPermission();
    }

    Handler handler = new Handler();
    Runnable updateThread = new Runnable() {
        public void run() {
            if (myMediaPlayer != null) {
                seekBar.setProgress(myMediaPlayer.getCurrentPosition());
                handler.postDelayed(updateThread, 100);
            }
        }
    };


    /**
     * 加载list
     */
    void musicList(){
        Cursor cursor = getContentResolver()
                .query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                        , null, null, null,
                        MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
        List<String> songName = new ArrayList<>();
        if (cursor != null) {
            while (cursor.moveToNext()) {
                String path = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA));
                String name = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME));
                myMusicList.add(new Song(name, path));
                songName.add(name);
            }
            ArrayAdapter<String> musicList = new ArrayAdapter<>
                    (MainActivity.this,R.layout.musicitme, songName);
            setListAdapter(musicList);
        } else{
            Toast.makeText(MainActivity.this, "没有音乐",Toast.LENGTH_SHORT).show();
        }
        cursor.close();
    }

    void listener(){
        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(myMediaPlayer.isPlaying()){
                    myMediaPlayer.reset();
                    seekBar.setProgress(0);
                    seekBar.setEnabled(false);
                }
                textView.setText("播放");
            }
        });

        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playMusic(myMusicList.get(currentListItem).getPath());
                textView.setText("开始");
            }
        });

        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nextMusic();
                textView.setText("下一首");
            }
        });

        pause.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if(myMediaPlayer.isPlaying()){
                    myMediaPlayer.pause();
                    seekBar.setEnabled(false);
                }else{
                    myMediaPlayer.start();
                    seekBar.setEnabled(true);
                }
                textView.setText("暂停");
            }
        });

        last.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                lastMusic();
                textView.setText("后一首");
            }
        });

    }

    void playMusic(String path){
        try {
            myMediaPlayer.reset();
            myMediaPlayer.setDataSource(path);
            myMediaPlayer.prepare();
            myMediaPlayer.start();
            myMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                public void onPrepared(MediaPlayer mp) {
                    // TODO Auto-generated method stub
                    myMediaPlayer.start();
                    start.setEnabled(false);
                    start.setClickable(false);
                    seekBar.setMax(myMediaPlayer.getDuration());
                    handler.post(updateThread);
                    seekBar.setEnabled(true);
                }
            });
            myMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    nextMusic();
                    seekBar.setProgress(0);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void nextMusic(){
        if(++currentListItem >= myMusicList.size()){
            currentListItem=0;
        }
        else{
            playMusic(myMusicList.get(currentListItem).getPath());
        }
    }

    void lastMusic(){
        if(currentListItem!=0)
        {
            if(--currentListItem>=0){
                currentListItem = myMusicList.size();
            } else{
                playMusic(myMusicList.get(currentListItem).getPath());
            }
        }  else{
            playMusic(myMusicList.get(currentListItem).getPath());
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK){
            myMediaPlayer.stop();
            myMediaPlayer.release();
            this.finish();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        currentListItem = position;
        playMusic(myMusicList.get(currentListItem).getPath());
    }

    public void asdPermission(){
        permissionHelper.checkPermission(this, new PermissionHelper.AskPermissionCallBack() {
            @Override
            public void onSuccess() {
                musicList();
                listener();
            }
            @Override
            public void onFailed() {
            }
        });
    }

    class Song{
        String name;
        String path;

        public Song(String name, String path) {
            this.name = name;
            this.path = path;
        }

        public String getName() {
            return name;
        }

        public String getPath() {
            return path;
        }
    }
}
