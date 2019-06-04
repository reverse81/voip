package arch3.lge.com.voip.ui;

import android.os.Bundle;
import android.widget.ListView;

import arch3.lge.com.voip.R;

public class ConferenceActivity extends MainTabActivity {
    ConferenceArrayAdapter mAdapter = new ConferenceArrayAdapter();

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conference);
        ListView listView = (ListView) findViewById(R.id.conference_list);
        mAdapter.onCreate(this, listView);
    }
}