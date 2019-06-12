package arch3.lge.com.voip.model.encrypt;

public abstract class AbstractEncryt {
    protected   static String source = "KKF2QT4fwpMeJf36POk6yJVHTAEPAPMY";


    public abstract String encrypt(String strToEncrypt);
    public abstract String decrypt(String strToEncrypt);
}
