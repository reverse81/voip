package arch3.lge.com.voip.ui;

import android.content.Context;
import android.media.AudioManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;

import arch3.lge.com.voip.R;
import arch3.lge.com.voip.controller.CallController;
import arch3.lge.com.voip.controller.DeviceContorller;
import arch3.lge.com.voip.model.call.PhoneState;
import arch3.lge.com.voip.model.codec.VoIPAudioIo;
import arch3.lge.com.voip.model.codec.VoIPVideoIo;

public class CallingActivity extends BaseCallActivity {

    private static final String LOG_TAG = "VoIP:Calling";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calling);

        Log.e(LOG_TAG, "Start received");
        StartReceiveVideoThread();
        this.attachImageView((ImageView)findViewById(R.id.target));
        Log.e(LOG_TAG, "Start send");


        final ImageButton video = (ImageButton)findViewById(R.id.video_record);
        VoIPVideoIo io = VoIPVideoIo.getInstance();
        io.attachIP(PhoneState.getInstance().getRemoteIP());
        if (!io.isBanned()) {
            io.StartVideo((ImageView) findViewById(R.id.self));
        } else {
            io.attachView((ImageView) findViewById(R.id.self));
            video.setImageResource(R.drawable.video_off);
        }


        Log.e(LOG_TAG, "Start audio");
        mVoIPAudioIo = VoIPAudioIo.getInstance(this);
        if (mVoIPAudioIo.StartAudio(PhoneState.getInstance().getRemoteIP(),0)) {
            Log.e(LOG_TAG, "Audio Already started (Answer Button)");
        }

        ImageButton endCall = (ImageButton)findViewById(R.id.end_call);
        endCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CallController.endCall(CallingActivity.this);
            }
        });

        AudioManager audioManager = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);
        final ImageButton speaker = (ImageButton)findViewById(R.id.speaker);
        final ImageButton bluetooth = (ImageButton)findViewById(R.id.bluetooth);
        final ImageButton mic = (ImageButton)findViewById(R.id.mic);

        if (audioManager.isMicrophoneMute()) {
            mic.setImageResource(R.drawable.mic_off);
        }
        if (audioManager.isBluetoothScoOn()) {
            bluetooth.setImageResource(R.drawable.bluetooth_on);
        }
        if (audioManager.isSpeakerphoneOn()) {
            speaker.setImageResource(R.drawable.speaker);
        }


        video.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!VoIPVideoIo.getInstance().isBanned()) {
                    VoIPVideoIo.getInstance().EndVideo();
                    VoIPVideoIo.getInstance().setBanned(true);
                    video.setImageResource(R.drawable.video_off);

                } else {
                    VoIPVideoIo.getInstance().restartVideo();
                    VoIPVideoIo.getInstance().setBanned(false);
                    video.setImageResource(R.drawable.video_on);
                }
            }
        });

        speaker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean result = DeviceContorller.toggleSpeakerPhone(CallingActivity.this);
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
                boolean result =DeviceContorller.toggleBluetooth(CallingActivity.this);
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
                boolean result =DeviceContorller.toggleMicMute(CallingActivity.this);
                if (result) {
                    mic.setImageResource(R.drawable.mic_off);
                } else {
                    mic.setImageResource(R.drawable.mic_on);
                }
                //CallController.endCall(CallingActivity.this);
            }
        });
    }


}
