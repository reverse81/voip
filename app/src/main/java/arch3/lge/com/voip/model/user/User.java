package arch3.lge.com.voip.model.user;

import android.content.Context;
import android.content.SharedPreferences;

public class User {
    private String email;
    private String password;
    private String retypedPassword;

    public User(String email, String password) {
        this.email = email;
        this.password = password;
    }

    public boolean isEmailValid() {
        //TODO: Replace this with your own logic
        return email.contains("@");
    }

    public boolean isPasswordValid() {
        //TODO: Replace this with your own logic
        return password.length() > 4;
    }

    public boolean isSamePassword() {
        //TODO: Replace this with your own logic
        return  password.equals(retypedPassword);
    }

    public String getPassword() {
        return password;
    }


    public String getEmail() {
        return email;
    }

    public void setRetypedPassword(String retypedPassword) {
        this.retypedPassword = retypedPassword;
    }

    public static void saveEmail(Context context, String email){
        SharedPreferences.Editor editor = context.getSharedPreferences("user", Context.MODE_PRIVATE).edit();
        editor.putString("eMail", email);
        editor.commit();
    }

    public static String readEmail(Context context){
        SharedPreferences sharedPreferences = context.getSharedPreferences("user", Context.MODE_PRIVATE);
        return sharedPreferences.getString("email","");
    }

    public static void saveUserName(Context context, String UserName){
        SharedPreferences.Editor editor = context.getSharedPreferences("user", Context.MODE_PRIVATE).edit();
        editor.putString("userName", UserName);
        editor.commit();
    }

    public static String readUserName(Context context){
        SharedPreferences sharedPreferences = context.getSharedPreferences("user", Context.MODE_PRIVATE);
        return sharedPreferences.getString("userName","");
    }

    public static void saveLogin(Context context, String token) {
        SharedPreferences.Editor editor = context.getSharedPreferences("user", Context.MODE_PRIVATE).edit();
        editor.putString("token", token);
        editor.commit();
    }



    public static String getLogin(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("user", Context.MODE_PRIVATE);
        return sharedPreferences.getString("token","eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpZCI6ImRkZEBuYXZlci5jb20iLCJtZXNzYWdlIjoiaXQgbWFrZXMgZnJvbSBtaXlhIiwiaWF0IjoxNTYwMzU5NDgxfQ.a3f9CZ5pdGLbmUWEpsrUamas5LzpM2dtjamdNxtjKz8");

    }
}
