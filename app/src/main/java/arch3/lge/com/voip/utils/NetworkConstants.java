package arch3.lge.com.voip.utils;

public class NetworkConstants {
    public static final int CONTROL_DATA_PORT = 5123;
    public static final int VOIP_AUDIO_UDP_PORT = 5224;
    public static final int VOIP_AUDIO_UDP_PORT_CC1 = VOIP_AUDIO_UDP_PORT+1;
    public static final int VOIP_AUDIO_UDP_PORT_CC2 = VOIP_AUDIO_UDP_PORT+2;
    public static final int VOIP_AUDIO_UDP_PORT_CC3 = VOIP_AUDIO_UDP_PORT+3;
    public static final int VOIP_AUDIO_UDP_PORT_CC4 = VOIP_AUDIO_UDP_PORT+4;

    public static final int VOIP_VIDEO_UDP_PORT = 5325;
    public static final int VOIP_VIDEO_UDP_PORT_CC1 = VOIP_VIDEO_UDP_PORT+1;
    public static final int VOIP_VIDEO_UDP_PORT_CC2 = VOIP_VIDEO_UDP_PORT+2;
    public static final int VOIP_VIDEO_UDP_PORT_CC3 = VOIP_VIDEO_UDP_PORT+3;
    public static final int VOIP_VIDEO_UDP_PORT_CC4 = VOIP_VIDEO_UDP_PORT+4;

    public static final int VIDEO_BUFFER_SIZE = 65507;

    public static final  String serverAddress = "http://128.237.133.85:3000/";
    public static final String ContentsType = "application/json";
}
