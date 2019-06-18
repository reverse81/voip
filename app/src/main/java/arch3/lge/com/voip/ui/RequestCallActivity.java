package arch3.lge.com.voip.ui;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import java.net.InetAddress;
import java.net.UnknownHostException;

import arch3.lge.com.voip.R;
import arch3.lge.com.voip.controller.CallController;
import arch3.lge.com.voip.model.codec.VoIPAudioIo;
import arch3.lge.com.voip.model.codec.VoIPVideoIo;

public class RequestCallActivity extends BaseCallActivity {
    static final String LOG_TAG = "RequestCallActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request_call);

        String phoneNumber = getIntent().getStringExtra("phoneNumber");
        ImageView self = (ImageView)findViewById(R.id.selfImage);
        //if(testCall("172.20.2.146"))//phoneNumber))
        //    return;
        CallController.requestCall(this, phoneNumber, self);
        //StartReceiveVideoThread();

    }

    private boolean testCall(String phoneNumber)
    {
        String ipName=phoneNumber.replace('*', '.');
        Log.e(LOG_TAG, "address: " + ipName);
        InetAddress address = null;
        try {
            address = InetAddress.getByName(ipName);
        } catch (UnknownHostException e) {
            Log.e(LOG_TAG, "UnknownHostException: " + e);
            return false;
        } catch (Exception e) {
            Log.e(LOG_TAG, "Exception: " + e);
            return false;
        }
        if(address == null)
            return false;

        mVoIPAudioIo = new VoIPAudioIo(getApplicationContext());
     //   mVoIPVideoIo = new VoIPVideoIo();

        if (mVoIPAudioIo.StartAudio(address,0)) {
            Log.e(LOG_TAG, "Audio Already started (Answer Button)");
        }
      //  if (mVoIPVideoIo.StartVideo(address))
        //    Log.e(LOG_TAG, "Video Already started (Answer Button)");
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mVoIPAudioIo !=null) {
            mVoIPAudioIo.EndAudio();
        }
    }
}
