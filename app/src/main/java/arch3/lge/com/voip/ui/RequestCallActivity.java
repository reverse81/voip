package arch3.lge.com.voip.ui;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import java.net.InetAddress;
import java.net.UnknownHostException;

import arch3.lge.com.voip.R;
import arch3.lge.com.voip.controller.CallController;
import arch3.lge.com.voip.controller.DeviceContorller;
import arch3.lge.com.voip.model.codec.VoIPAudioIo;
import arch3.lge.com.voip.model.codec.VoIPVideoIo;

public class RequestCallActivity extends BaseCallActivity {
    static final String LOG_TAG = "RequestCallActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request_call);

        DeviceContorller.initDevice(this);

        String phoneNumber = getIntent().getStringExtra("phoneNumber");
        ImageView self = (ImageView)findViewById(R.id.selfImage);
        CallController.requestCall(this, phoneNumber, self);

        ImageButton reject = (ImageButton) findViewById(R.id.end_call);
        reject.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                CallController.endCall(RequestCallActivity.this);
                RequestCallActivity.this.finish();
            }
        });

        final ImageButton video = (ImageButton)findViewById(R.id.video_record);


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

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
