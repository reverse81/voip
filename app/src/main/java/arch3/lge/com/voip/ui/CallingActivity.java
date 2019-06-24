package arch3.lge.com.voip.ui;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import arch3.lge.com.voip.R;
import arch3.lge.com.voip.controller.CallController;
import arch3.lge.com.voip.model.call.PhoneState;
import arch3.lge.com.voip.model.codec.VoIPAudioIo;
import arch3.lge.com.voip.model.codec.VoIPVideoIo;

public class CallingActivity extends BaseCallActivity {

    private static final String LOG_TAG = "VoIP:Calling";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calling);

        StartReceiveVideoThread();
        this.attachImageView((ImageView)findViewById(R.id.target));
        VoIPVideoIo io = VoIPVideoIo.getInstance();
        io.StartVideo((ImageView)findViewById(R.id.self));

        mVoIPAudioIo = new VoIPAudioIo(getApplicationContext());
        if (mVoIPAudioIo.StartAudio(PhoneState.getInstance().getRemoteIP(),0)) {
            Log.e(LOG_TAG, "Audio Already started (Answer Button)");
        }

        Button endCall = (Button)findViewById(R.id.end_call);
        endCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CallController.endCall(CallingActivity.this);
            }
        });
    }


}
