package arch3.lge.com.voip.ui;

import android.app.Activity;
import android.database.Cursor;
import android.util.Log;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.SimpleCursorAdapter;

import arch3.lge.com.voip.R;
import arch3.lge.com.voip.model.database.ContactListDataHelper;

public class ContactArrayAdapter {
//    ArrayAdapter<String> mAdapter;
    SimpleAdapter mAdapter;
    ContactListDataHelper mContactDB;

    String[] mobileArray = {"Android","IPhone","WindowsMobile","Blackberry",
            "WebOS","Ubuntu","Windows7","Max OS X"};

    public void onCreate(Activity activity, ListView listView, String contactType)
    {
//        mAdapter = new ArrayAdapter<String>(activity, R.layout.contact_list_item, R.id.list_item_name, mobileArray);

        mContactDB = new ContactListDataHelper(activity);

        Log.i("dhtest","contact1");
        mContactDB.showList();

        //Type Image 변경
        if (contactType.equals("edit")){
            mAdapter = new SimpleAdapter(activity, mContactDB.personList, R.layout.contact_list_edit_item,
                    new String[]{"name", "phone"},
                    new int[]{R.id.list_edit_item_name, R.id.list_edit_item_phone});
        }

        else{
            mAdapter = new SimpleAdapter(activity, mContactDB.personList, R.layout.contact_list_item,
                    new String[]{"name", "phone"},
                    new int[]{R.id.list_item_name, R.id.list_item_phone});
        }


        listView.setAdapter(mAdapter);
    }

    public void onResume(){
        mContactDB.showList();
        mAdapter.notifyDataSetChanged();
    }

}
