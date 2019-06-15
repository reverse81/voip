package arch3.lge.com.voip.ui;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import arch3.lge.com.voip.R;
import arch3.lge.com.voip.controller.CallController;
import arch3.lge.com.voip.model.codec.VoIPVideoIo;

public class RequestCallActivity extends BaseCallActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request_call);

        String phoneNumber = getIntent().getStringExtra("phoneNumber");
        ImageView self = (ImageView)findViewById(R.id.selfImage);
        CallController.requestCall(this, phoneNumber, self);
        //StartReceiveVideoThread();
    }
}
