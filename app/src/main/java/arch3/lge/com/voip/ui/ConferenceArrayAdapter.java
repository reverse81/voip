package arch3.lge.com.voip.ui;

import android.app.Activity;
import android.content.Context;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import arch3.lge.com.voip.R;
import arch3.lge.com.voip.model.database.ConferenceDatabaseHelper;
import arch3.lge.com.voip.model.database.ContactListDataHelper;

public class ConferenceArrayAdapter {
//    ArrayAdapter<String> mAdapter;
    SimpleAdapter mAdapter;
    String[] mobileArray = {"sdfs","IPhone","WindowsMobile","Blackberry",
            "WebOS","Ubuntu","ssddf","Max OS X"};

    public void onCreate(Activity activity, ListView listView)
    {
//        mAdapter = new ArrayAdapter<String>(context, R.layout.activity_conference_list, R.id.list_item_name, mobileArray);
//        listView.setAdapter(mAdapter);

        ConferenceDatabaseHelper ConferenceDB = new ConferenceDatabaseHelper(activity);
        ConferenceDB.showList();
        mAdapter = new SimpleAdapter(activity, ConferenceDB.conferenceList, R.layout.conference_item,
                new String[]{"startTime", "phone"},
                new int[]{R.id.conference_item_time, R.id.conference_item_phone});

        listView.setAdapter(mAdapter);
    }
}
