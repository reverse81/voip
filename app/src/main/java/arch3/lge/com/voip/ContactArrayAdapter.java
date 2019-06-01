package arch3.lge.com.voip;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class ContactArrayAdapter {
    ArrayAdapter<String> mAdapter;
    String[] mobileArray = {"Android","IPhone","WindowsMobile","Blackberry",
            "WebOS","Ubuntu","Windows7","Max OS X"};

    public void onCreate(Context context, ListView listView)
    {
        mAdapter = new ArrayAdapter<String>(context, R.layout.activity_contact_list, R.id.textView, mobileArray);
        listView.setAdapter(mAdapter);
    }
}
