package arch3.lge.com.voip.ui;

import android.os.Bundle;
import android.widget.ListView;

import arch3.lge.com.voip.R;

public class ContactActivity extends MainTabActivity {
    ContactArrayAdapter mAdapter = new ContactArrayAdapter();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact);
        ListView listView = (ListView) findViewById(R.id.contact_list);
        mAdapter.onCreate(this, listView);
    }
}
