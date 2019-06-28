package arch3.lge.com.voip.ui;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;

import arch3.lge.com.voip.R;
import arch3.lge.com.voip.controller.CallController;
import arch3.lge.com.voip.controller.DeviceContorller;
import arch3.lge.com.voip.model.codec.VoIPVideoIo;

public class RequestCallActivity extends BaseCallActivity {
    static final String LOG_TAG = "RequestCallActivity";

    private AudioManager audioManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request_call);

        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        DeviceContorller.initDevice(this);
        StartRinger();

        String phoneNumber = getIntent().getStringExtra("phoneNumber");
        ImageView self = (ImageView)findViewById(R.id.selfImage);
        CallController.requestCall(this, phoneNumber, self);

        ImageButton end_call = (ImageButton) findViewById(R.id.end_call);
        end_call.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                CallController.endCall(RequestCallActivity.this);
                RequestCallActivity.this.finish();
            }
        });

        final ImageButton video = (ImageButton)findViewById(R.id.video_record);
        final ImageButton speaker = (ImageButton)findViewById(R.id.speaker);
        final ImageButton bluetooth = (ImageButton)findViewById(R.id.bluetooth);
        final ImageButton mic = (ImageButton)findViewById(R.id.mic);

        video.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!VoIPVideoIo.getInstance(RequestCallActivity.this).isBanned()) {
                    VoIPVideoIo.getInstance(RequestCallActivity.this).EndVideo();
                    VoIPVideoIo.getInstance(RequestCallActivity.this).setBanned(true);
                    video.setImageResource(R.drawable.video_off);

                } else {
                    VoIPVideoIo.getInstance(RequestCallActivity.this).restartVideo();
                    VoIPVideoIo.getInstance(RequestCallActivity.this).setBanned(false);
                    video.setImageResource(R.drawable.video_on);
                }
            }
        });

        speaker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean result = DeviceContorller.toggleSpeakerPhone(RequestCallActivity.this);
                if (result) {
                    speaker.setImageResource(R.drawable.speaker);
                    bluetooth.setImageResource(R.drawable.bluetooth_disable);
                } else {
                    speaker.setImageResource(R.drawable.speaker_mute);
                }
                //CallController.endCall(CallingActivity.this);
            }
        });


        bluetooth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean result =DeviceContorller.toggleBluetooth(RequestCallActivity.this);
                if (result) {
                    speaker.setImageResource(R.drawable.speaker_mute);
                    bluetooth.setImageResource(R.drawable.bluetooth_on);
                } else {
                    bluetooth.setImageResource(R.drawable.bluetooth_disable);
                }
                //CallController.endCall(CallingActivity.this);
            }
        });

        mic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean result =DeviceContorller.toggleMicMute(RequestCallActivity.this);
                if (result) {
                    mic.setImageResource(R.drawable.mic_off);
                } else {
                    mic.setImageResource(R.drawable.mic_on);
                }
                //CallController.endCall(CallingActivity.this);
            }
        });

    }

    private MediaPlayer ring;
    private  int previousAudioManagerMode;

    private void StartRinger() {
//        if (PhoneState.getInstance().GetRinger()) {
   //     if(audioManager.getRingerMode() == AudioManager.RINGER_MODE_NORMAL) {
            if (ring == null) {
                Log.i("RRRRRRRRRRingni", ""+audioManager.getMode());
                previousAudioManagerMode = audioManager.getMode();
                audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
                ring = MediaPlayer.create(getApplicationContext(), R.raw.signal);
                ring.setLooping(true);
                ring.start();
            }
//        } else        if(audioManager.getRingerMode() == AudioManager.RINGER_MODE_VIBRATE) {
//            vibrator.vibrate(vibratorPattern, 0);
//        }
    }

    public void changeTone() {
        ring.stop();
        ring.release();
        ring = MediaPlayer.create(getApplicationContext(), R.raw.busy);
        ring.setLooping(true);
        ring.start();
    }

    private void EndRinger() {
        if (ring != null) {
            ring.stop();
            ring.release();
            ring = null;
            audioManager.setMode(previousAudioManagerMode);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EndRinger();
    }
}
