package arch3.lge.com.voip.model.database;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;

public class ContactListDataHelper extends SQLiteOpenHelper {
    public static final int DATABASE_VERSION = 1;
    private final String tableName = "contact_table";
    private final String USER = "name";
    private final String PHONE = "phone";
    public ArrayList<HashMap<String, String>> personList;

    public ContactListDataHelper(Context context){
        super(context, "contactListDB", null, DATABASE_VERSION);
        personList = new ArrayList<HashMap<String,String>>();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String tableSql = "create table contact_table (" +
                USER+" not null," +
                PHONE+" not null)";

        db.execSQL("CREATE TABLE "+tableName+" (name TEXT not null, phone TEXT not null);");
        //db.execSQL("CREATE TABLE MONEYBOOK (_id INTEGER PRIMARY KEY AUTOINCREMENT, item TEXT, price INTEGER, create_at TEXT);");
        //db.execSQL(tableSql);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (newVersion != DATABASE_VERSION){
            db.execSQL("drop table "+tableName);
            onCreate(db);
        }
    }

    public void insertContextList(String userName, String phoneNum){
        SQLiteDatabase db = getWritableDatabase();
        //db.execSQL("insert into contact_table (name, phone) values(?,?)", new String[userName, phoneNum]);
        db.execSQL("INSERT INTO "+tableName+" VALUES('"+userName + "', '" + phoneNum +"');");
        db.close();
    }

    public void updateContextList(String userName, String newUserName, String newPhoneNum){
        SQLiteDatabase db = getWritableDatabase();
        Log.i("dhtest", "update Contact DB User : "+userName);
        db.execSQL("update "+tableName+" set phone='" + newPhoneNum + "' where name='" + userName + "';");
        db.execSQL("update "+tableName+" set name='" + newUserName + "' where name='" + userName + "';");
        db.close();
    }

    public void deleteContextList(String userName) {
        SQLiteDatabase db = getWritableDatabase();
        // 입력한 항목과 일치하는 행 삭제
        db.execSQL("delete from "+tableName+" where user='" + userName + "';");
        db.close();
    }

//    public String[] getResult() {
//        // 읽기가 가능하게 DB 열기
//        SQLiteDatabase db = getReadableDatabase();
//        String result[];
//        int i = 0;
//
//        // DB에 있는 데이터를 쉽게 처리하기 위해 Cursor를 사용하여 테이블에 있는 모든 데이터 출력
//        Cursor cursor = db.rawQuery("SELECT * FROM MONEYBOOK", null);
//        while (cursor.moveToNext()) {
//            result += cursor.getString(0);
//            i++;
//        }
//
//        return result;
//    }


    public void showList(){

        try {

            SQLiteDatabase db = getWritableDatabase();


            //SELECT문을 사용하여 테이블에 있는 데이터를 가져옵니다..
            Cursor c = db.rawQuery("SELECT * FROM " + tableName, null);

            if (c != null) {


                if (c.moveToFirst()) {
                    do {

                        //테이블에서 두개의 컬럼값을 가져와서
                        String Name = c.getString(c.getColumnIndex("name"));
                        String Phone = c.getString(c.getColumnIndex("phone"));

                        //HashMap에 넣습니다.
                        HashMap<String,String> persons = new HashMap<String,String>();

                        persons.put(USER,Name);
                        persons.put(PHONE,Phone);

                        //ArrayList에 추가합니다..
                        personList.add(persons);

                    } while (c.moveToNext());
                }
            }

            db.close();

        } catch (SQLiteException se) {
            Log.e("",  se.getMessage());
        }

    }


}
