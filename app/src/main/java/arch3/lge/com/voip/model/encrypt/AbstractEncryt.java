package arch3.lge.com.voip.model.encrypt;

public abstract class AbstractEncryt {



    public abstract String encrypt(String strToEncrypt);
    public abstract String decrypt(String strToEncrypt);
    public abstract byte[] encrypt( byte[] strToEncrypt);
    public abstract  byte[] decrypt( byte[] strToEncrypt, int offset, int length);
}
