package arch3.lge.com.voip.ui;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import arch3.lge.com.voip.R;
import arch3.lge.com.voip.controller.CallController;

public class ReceivedCallActivity extends BaseCallActivity {

    private AudioManager audioManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_received_call);

        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        StartReceiveVideoThread();
        this.attachImageView((ImageView)findViewById(R.id.target));
        StartRinger();

        Button accept = (Button) findViewById(R.id.accept);
        accept.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                CallController.acceptCall(ReceivedCallActivity.this);
                Intent intent = new Intent();
                intent.setClass(ReceivedCallActivity.this, CallingActivity.class);
                ReceivedCallActivity.this.startActivity(intent);
                ReceivedCallActivity.this.finish();
            }
        });

        Button reject = (Button) findViewById(R.id.reject);
        reject.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                CallController.rejectCall(ReceivedCallActivity.this);
                ReceivedCallActivity.this.finish();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EndRinger();
    }

    private MediaPlayer ring;
    private Vibrator vibrator;
    private final long[] vibratorPattern = {0, 200, 800};
    private  int previousAudioManagerMode;

    private void StartRinger() {
//        if (PhoneState.getInstance().GetRinger()) {
       if(audioManager.getRingerMode() == AudioManager.RINGER_MODE_NORMAL) {
            if (ring == null) {
        Log.i("RRRRRRRRRRingni", ""+audioManager.getMode());
        previousAudioManagerMode = audioManager.getMode();
                audioManager.setMode(AudioManager.MODE_RINGTONE);
                ring = MediaPlayer.create(getApplicationContext(), R.raw.ring);
                ring.setLooping(true);
                ring.start();
            }
        } else        if(audioManager.getRingerMode() == AudioManager.RINGER_MODE_VIBRATE) {
           vibrator.vibrate(vibratorPattern, 0);
       }
    }

    private void EndRinger() {
        if (ring != null) {
            ring.stop();
            ring.release();
            ring = null;
 audioManager.setMode(previousAudioManagerMode);
        }
        vibrator.cancel();
    }
}
