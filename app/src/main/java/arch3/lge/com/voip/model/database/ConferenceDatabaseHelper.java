package arch3.lge.com.voip.model.database;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;

public class ConferenceDatabaseHelper extends SQLiteOpenHelper {
    public static final int DATABASE_VERSION = 1;
    private final String tableName = "conference_table";
    private final String TIME_INFO = "timeInfo";
    private final String PHONE = "phone";
    private final String START_TIME = "startTime";
    private final String END_TIME = "endTime";

    public ArrayList<HashMap<String, String>> conferenceList;

    // DBHelper 생성자로 관리할 DB 이름과 버전 정보를 받음
    public ConferenceDatabaseHelper(Context context) {
        //super(context, name, factory, version);
        super(context, "conferenceDB", null, DATABASE_VERSION);
        conferenceList = new ArrayList<HashMap<String,String>>();
    }

    // DB를 새로 생성할 때 호출되는 함수
    @Override
    public void onCreate(SQLiteDatabase db) {
        // 새로운 테이블 생성
        db.execSQL("CREATE TABLE "+tableName+" (timeInfo TEXT not null, phone TEXT not null, startTime TEXT not null, endTime TEXT not null);");
    }

    // DB 업그레이드를 위해 버전이 변경될 때 호출되는 함수
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (newVersion != DATABASE_VERSION){
            db.execSQL("drop table "+tableName);
            onCreate(db);
        }
    }

    public void insert(String StartTime, String EndTime, String phoneNum) {
        String TimeInfo = (StartTime+" ~ "+EndTime);

        if (isDuplicated(phoneNum)){
            return;
        }

        // 읽고 쓰기가 가능하게 DB 열기
        SQLiteDatabase db = getWritableDatabase();
        //db.execSQL("insert into contact_table (name, phone) values(?,?)", new String[userName, phoneNum]);
        db.execSQL("INSERT INTO "+tableName+" VALUES('"+ TimeInfo +"', '"+ phoneNum +"', '"+ StartTime +"', '" + EndTime +"');");
        db.close();
    }

    public void update(String item, int price) {
        SQLiteDatabase db = getWritableDatabase();
        // 입력한 항목과 일치하는 행의 가격 정보 수정
        db.execSQL("UPDATE MONEYBOOK SET price=" + price + " WHERE item='" + item + "';");
        db.close();
    }

    public void delete(String phone) {
        SQLiteDatabase db = getWritableDatabase();
        // 입력한 항목과 일치하는 행 삭제
        db.execSQL("DELETE FROM MONEYBOOK WHERE phone='"+ phone +"';");
        db.close();
    }

    public String getResult() {
        return null;
    }

    public void showList(){

        try {

            SQLiteDatabase db = getWritableDatabase();

            if (conferenceList != null)
                conferenceList.clear();

            //SELECT문을 사용하여 테이블에 있는 데이터를 가져옵니다..
            Cursor c = db.rawQuery("SELECT * FROM " + tableName, null);

            if (c != null) {


                if (c.moveToFirst()) {
                    do {

                        //테이블에서 두개의 컬럼값을 가져와서
                        String Time = c.getString(c.getColumnIndex("startTime"));
                        String Phone = c.getString(c.getColumnIndex("phone"));

                        //HashMap에 넣습니다.
                        HashMap<String,String> persons = new HashMap<String,String>();

                        persons.put(START_TIME,Time);
                        persons.put(PHONE,Phone);

                        //ArrayList에 추가합니다..
                        conferenceList.add(persons);

                    } while (c.moveToNext());
                }
            }

            db.close();

        } catch (SQLiteException se) {
            Log.e("",  se.getMessage());
        }

    }


    public void deleteListAll(){

        SQLiteDatabase db = getWritableDatabase();
        // DB 모든 정보 삭제
        db.execSQL("DELETE FROM "+ tableName);
        db.close();
    }

    public boolean isDuplicated(String phone){
        boolean duplicatedUser = false;
        try {
            SQLiteDatabase db = getWritableDatabase();

            //SELECT문을 사용하여 테이블에 있는 데이터를 가져옵니다..
            Cursor c = db.rawQuery("SELECT * FROM " + tableName, null);

            if (c != null) {
                if (c.moveToFirst()) {
                    do {
                        //테이블에서 두개의 컬럼값을 가져와서
                        String Phone = c.getString(c.getColumnIndex("phone"));

                        if (Phone.equals(phone)){
                            duplicatedUser = true;
                            break;
                        }
                    } while (c.moveToNext());
                }
            }
            db.close();
        } catch (SQLiteException se) {
            Log.e("",  se.getMessage());
        }

        return duplicatedUser;
    }
}