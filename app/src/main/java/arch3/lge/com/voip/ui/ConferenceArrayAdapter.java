package arch3.lge.com.voip.ui;

import android.app.Activity;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import arch3.lge.com.voip.R;
import arch3.lge.com.voip.model.database.ConferenceDatabaseHelper;

public class ConferenceArrayAdapter {
//    ArrayAdapter<String> mAdapter;
    SimpleAdapter mAdapter;
    ConferenceDatabaseHelper mConferenceDB;
    String[] mobileArray = {"sdfs","IPhone","WindowsMobile","Blackberry",
            "WebOS","Ubuntu","ssddf","Max OS X"};

    public void onCreate(Activity activity, ListView listView)
    {
//        mAdapter = new ArrayAdapter<String>(context, R.layout.activity_conference_list, R.id.list_item_name, mobileArray);
//        listView.setAdapter(mAdapter);

        mConferenceDB = new ConferenceDatabaseHelper(activity);
        mConferenceDB.showList();
        mAdapter = new SimpleAdapter(activity, mConferenceDB.conferenceList, R.layout.conference_item,
                new String[]{"startTime", "phone"},
                new int[]{R.id.conference_item_time, R.id.conference_item_phone});

        listView.setAdapter(mAdapter);
    }

    public void onResume(){
        mConferenceDB.showList();
        mAdapter.notifyDataSetChanged();
    }
}
