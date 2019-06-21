package arch3.lge.com.voip.ui;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;

import arch3.lge.com.voip.R;
import arch3.lge.com.voip.model.codec.VoIPVideoIo;

public class CallingActivity extends BaseCallActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calling);

        StartReceiveVideoThread();
        this.attachImageView((ImageView)findViewById(R.id.target));
        VoIPVideoIo io = VoIPVideoIo.getInstance();
        io.StartVideo((ImageView)findViewById(R.id.self));
    }


}
