package arch3.lge.com.voip.ui;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import arch3.lge.com.voip.R;
import arch3.lge.com.voip.model.database.ContactListDataHelper;

public class ContactArrayAdapter {
//    ArrayAdapter<String> mAdapter;
    SimpleAdapter mAdapter;

    String[] mobileArray = {"Android","IPhone","WindowsMobile","Blackberry",
            "WebOS","Ubuntu","Windows7","Max OS X"};

    public void onCreate(Activity activity, ListView listView)
    {
//        mAdapter = new ArrayAdapter<String>(activity, R.layout.contact_list_item, R.id.list_item_name, mobileArray);

        ContactListDataHelper ContactDB = new ContactListDataHelper(activity);
        ContactDB.showList();
        mAdapter = new SimpleAdapter(activity, ContactDB.personList, R.layout.contact_list_item,
                                    new String[]{"name", "phone"},
                                    new int[]{R.id.list_item_name, R.id.list_item_phone});

        listView.setAdapter(mAdapter);



    }

}
