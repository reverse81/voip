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

    public static void saveLogin(Context context, String token) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("user", Context.MODE_PRIVATE);
        sharedPreferences.edit().putString("token", token);
        sharedPreferences.edit().commit();
    }

    public static String getLogin(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("user", Context.MODE_PRIVATE);
        return sharedPreferences.getString("token","no");

    }
}
