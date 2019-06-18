package arch3.lge.com.voip.model.database;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class ContactListDataHelper extends SQLiteOpenHelper {
    public static final int DATABASE_VERSION = 1;

    public ContactListDataHelper(Context context){
        super(context, "contactListDB", null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String tableSql = "create table contact_table (" +
                "name not null," +
                "phone not null)";

        db.execSQL("CREATE TABLE contact_table (name TEXT not null, phone TEXT not null);");
        //db.execSQL("CREATE TABLE MONEYBOOK (_id INTEGER PRIMARY KEY AUTOINCREMENT, item TEXT, price INTEGER, create_at TEXT);");
        //db.execSQL(tableSql);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (newVersion != DATABASE_VERSION){
            db.execSQL("drop table contact_table");
            onCreate(db);
        }
    }

    public void insertContextList(String userName, String phoneNum){
        SQLiteDatabase db = getWritableDatabase();
        //db.execSQL("insert into contact_table (name, phone) values(?,?)", new String[userName, phoneNum]);
        db.execSQL("INSERT INTO contact_table VALUES('"+userName + "', '" + phoneNum +"');");
        db.close();
    }

    public void updateContextList(String userName, String newUserName, String newPhoneNum){
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("update contact_table set userName=" + userName + " where phone='" + newPhoneNum + "';");
        db.execSQL("update contact_table set userName=" + userName + " where name='" + newUserName + "';");
        db.close();
    }

    public void deleteContextList(String userName) {
        SQLiteDatabase db = getWritableDatabase();
        // 입력한 항목과 일치하는 행 삭제
        db.execSQL("delete from contact_table where user='" + userName + "';");
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


}
