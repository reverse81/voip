package arch3.lge.com.voip;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

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
