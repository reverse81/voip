package arch3.lge.com.voip.ui;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;

import arch3.lge.com.voip.R;

public class ConferenceActivity extends MainTabActivity {
    ConferenceArrayAdapter mAdapter = new ConferenceArrayAdapter();

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conference);
        ListView listView = (ListView) findViewById(R.id.conference_list);
        mAdapter.onCreate(this, listView);

        Button registerButton = (Button) findViewById(R.id.conference_new_btn);
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Register menu
                Log.v("dae", "New Conference");
                Intent intent = new Intent(ConferenceActivity.this, ConferenceRegisterActivity.class);
                startActivity(intent);
            }
        });

    }
}