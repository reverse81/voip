package arch3.lge.com.voip.model.codec;

import arch3.lge.com.voip.model.codec.audio.AudioG729b;
import arch3.lge.com.voip.model.codec.audio.AudioGSM0610;
import arch3.lge.com.voip.model.codec.video.VideoMJPEG;

public class CodecFacotry {
    enum AudioCodecType {
        GSM0610,
        G729B
    };
    enum VideoCodecType{
        MJPEG
    };

    static public AudioCodec createAudio(AudioCodecType type){
        if(type == AudioCodecType.GSM0610)
            return new AudioGSM0610();
        else if(type == AudioCodecType.G729B)
            return new AudioG729b();
        return new AudioGSM0610();
    };

    static public VideoCodec createVideo(VideoCodecType type){
        if(type == VideoCodecType.MJPEG)
            return new VideoMJPEG();

        return new VideoMJPEG();
    };
}
