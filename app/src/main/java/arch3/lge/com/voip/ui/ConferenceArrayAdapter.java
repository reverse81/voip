package arch3.lge.com.voip.ui;

import android.content.Context;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import arch3.lge.com.voip.R;

public class ConferenceArrayAdapter {
    ArrayAdapter<String> mAdapter;
    String[] mobileArray = {"sdfs","IPhone","WindowsMobile","Blackberry",
            "WebOS","Ubuntu","ssddf","Max OS X"};

    public void onCreate(Context context, ListView listView)
    {
        mAdapter = new ArrayAdapter<String>(context, R.layout.activity_conference_list, R.id.textView, mobileArray);
        listView.setAdapter(mAdapter);
    }
}
