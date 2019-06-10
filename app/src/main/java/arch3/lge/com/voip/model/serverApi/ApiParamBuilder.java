package arch3.lge.com.voip.model.serverApi;

import android.util.Log;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * 
 * @author mini.lee
 * 
 */
public class ApiParamBuilder {

    private final static String LOGTAG = "AppUp4:ApiParamBuilder";

    protected final JSONObject mMandatoryParam;

    public ApiParamBuilder() {
        mMandatoryParam = new JSONObject();
    }

    protected final static String KEY_EMAIL = "email";
    protected final static String KEY_PASSWORD = "pwd";

    public JSONObject getLogin(String email, String password) {
        try {
            mMandatoryParam.put(KEY_EMAIL, email);
            mMandatoryParam.put(KEY_PASSWORD, password);
            Log.i(LOGTAG, mMandatoryParam.toString());
        } catch (JSONException e) {
            Log.e(LOGTAG, "JSONException on getRetrieveApplistParam...", e);
        }
        return mMandatoryParam;
    }
}
