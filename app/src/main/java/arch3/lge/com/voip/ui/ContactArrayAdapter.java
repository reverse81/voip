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

        Log.v("dae", "create contact List");

        //Click listener
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Log.v("dae", "Position:");
                // position이 클릭된 위치입니다.
                // 컬렉션에서 적절하게 꺼내서 사용하시면 됩니다.
                //Toast.makeText(activity, itemList.get(position).getSomethingColumn(), Toast.LENGTH_LONG).show();

                // 추가된 부분
                //InfoClass selectedInfoClass = weatherList.get(position);
                //selectedInfoClass.getXXX();
            }
        });


    }

}
