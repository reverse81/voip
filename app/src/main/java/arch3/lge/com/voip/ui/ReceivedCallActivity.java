package arch3.lge.com.voip.ui;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import arch3.lge.com.voip.R;

public class ReceivedCallActivity extends BaseCallActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_received_call);

        StartReceiveVideoThread();
    }
}
