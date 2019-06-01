package arch3.lge.com.voip;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.widget.ListView;

public class ConferenceActivity extends MainTabActivity {
    ConferenceArrayAdapter mAdapter = new ConferenceArrayAdapter();

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conference);
        ListView listView = (ListView) findViewById(R.id.conference_list);
        mAdapter.onCreate(this, listView);
    }
}