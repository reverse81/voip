package arch3.lge.com.voip.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import arch3.lge.com.voip.R;

public class ContactArrayAdapter {
    ArrayAdapter<String> mAdapter;
    String[] mobileArray = {"Android","IPhone","WindowsMobile","Blackberry",
            "WebOS","Ubuntu","Windows7","Max OS X"};

    public void onCreate(Context context, ListView listView)
    {
        mAdapter = new ArrayAdapter<String>(context, R.layout.contact_list_item, R.id.textView, mobileArray);
        listView.setAdapter(mAdapter);
    }
}
