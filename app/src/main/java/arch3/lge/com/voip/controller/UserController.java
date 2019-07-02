package arch3.lge.com.voip.controller;

import android.content.Context;
import android.widget.Toast;

import org.json.JSONObject;

import arch3.lge.com.voip.model.serverApi.ApiParamBuilder;
import arch3.lge.com.voip.model.serverApi.ServerApi;

public class UserController {


    private  static ApiParamBuilder param = new ApiParamBuilder();
    private static ServerApi serverApi = new ServerApi();

    static public void register(String phonenumber, Context context) {
        Toast.makeText(context, "Register", Toast.LENGTH_SHORT).show();
        //get IP address

        //connect UDP

    }

    static public void thdraw(String phonenumber) {

    }

//    static public void login(String email, String password,Context context) {
//        JSONObject object = param.getLogin(email, password);
//        serverApi.login(context, object.toString(), email);
//    }

    static public void logout(String phonenumber) {

    }
}
