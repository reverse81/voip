package arch3.lge.com.voip.model.user;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

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
        Log.v("dae", "ori : "+password+" retry : "+ retypedPassword);
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

    public static void saveLogin(Context context, String token, String email, String phoneNumber) {
		SharedPreferences.Editor editor = context.getSharedPreferences("user", Context.MODE_PRIVATE).edit();
        if (token != null)
		    editor.putString("token", token);
        if (email != null)
            editor.putString("email", email);
        if (phoneNumber != null)
            editor.putString("phoneNumber", phoneNumber);
        editor.commit();
    }
    public static String getEmail(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("user", Context.MODE_PRIVATE);
        return sharedPreferences.getString("email","");
    }

    public static String getPhoneNumber(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("user", Context.MODE_PRIVATE);
        return sharedPreferences.getString("phoneNumber","");
    }

    public static String getLogin(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("user", Context.MODE_PRIVATE);
        return sharedPreferences.getString("token","");

    }
    public static void setLogout(Context context) {
        SharedPreferences.Editor editor = context.getSharedPreferences("user", Context.MODE_PRIVATE).edit();
        editor.putString("token", null);
        editor.commit();
    }
}
