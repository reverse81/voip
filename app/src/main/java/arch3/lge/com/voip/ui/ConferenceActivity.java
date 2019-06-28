package arch3.lge.com.voip.ui;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import arch3.lge.com.voip.R;

public class ConferenceActivity extends MainTabActivity {
    ConferenceArrayAdapter mAdapter = new ConferenceArrayAdapter();

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conference);
        this.tapSelect = CONFERENCE_SELECT;

        Log.i("dhtest","ConferenceActivity Create");

        ListView listView = (ListView) findViewById(R.id.conference_list);
        mAdapter.onCreate(this, listView);

        Button registerButton = (Button) findViewById(R.id.conference_new_btn);
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Register menu
                Log.i("dhtest", "New Conference");
                Intent intent = new Intent(ConferenceActivity.this, ConferenceRegisterActivity.class);
                startActivity(intent);
            }
        });

        //Click listener
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Log.i("dhtest", "Position:"+position+" id:"+id);

                //String string = ((TextView)view).getText().toString();
                String string = ((TextView) view.findViewById(R.id.conference_item_phone)).getText().toString();
                Log.i("dhtest", "phone:"+string);

                Intent intent = new Intent(getApplicationContext(), DialpadActivity.class);
                intent.putExtra("phone",string); /*송신*/
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
            }
        });

    }
}