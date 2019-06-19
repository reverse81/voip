
#include <stdlib.h>
#include <stdio.h>
#include <fcntl.h>
#include <unistd.h>
#include <memory.h>
#include <ctype.h>
#include <jni.h>
#include <android/log.h>

#include "typedef.h"
#include "basic_op.h"
#include "ld8k.h"
#include "dtx.h"
#include "octet.h"

#define LOG_TAG "g729" // text for log tag

extern Word16 *new_speech;     /* Pointer to new speech data            */

static struct {
    Word16 prm[PRM_SIZE+1];        /* Analysis parameters.                  */
    Word16 serial[SERIAL_SIZE];    /* Output bitstream buffer               */
    Word16 syn[L_FRAME];           /* Buffer for synthesis speech           */

    Word16 frame;               /* frame counter */

    /* For G.729B */
    Word16 nb_words;
    Word16 vad_enable;
} gUserEnv;

//extern "C"
JNIEXPORT jboolean JNICALL Java_arch3_lge_com_voip_model_codec_audio_AudioG729b_JniG729EncodeInit(JNIEnv *env, jobject obj)
{
    Init_Pre_Process();
    Init_Coder_ld8k();
    for(int i=0; i<PRM_SIZE; i++) {
        gUserEnv.prm[i] = (Word16)0;
    }
    /* for G.729B */
    Init_Cod_cng();

#ifdef SYNC
    /* Read L_NEXT first speech data */

  fread(&new_speech[-L_NEXT], sizeof(Word16), L_NEXT, f_speech);
#ifdef HARDW
    /* set 3 LSB's to zero */
    for(i=0; i < L_NEXT; i++)
      new_speech[-L_NEXT+i] = new_speech[-L_NEXT+i] & 0xFFF8;
#endif
     Pre_Process(&new_speech[-L_NEXT], L_NEXT);
#endif
    gUserEnv.frame =0;
    gUserEnv.vad_enable = 1;
    return (jboolean)1;
}


//extern "C"
JNIEXPORT jbyteArray JNICALL Java_arch3_lge_com_voip_model_codec_audio_AudioG729b_JniG729Encode(JNIEnv *env, jobject obj, jbyteArray data, jint offset, jint size)
{
    jbyteArray result = NULL;

    jsize bufferSize = (*env)->GetArrayLength(env, data);
    if(bufferSize < offset + size ) {
        __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, "overflow buffer size %d, %d, %d", bufferSize, offset, size);
        return result;
    }

    if(bufferSize %  (L_FRAME*sizeof(Word16))) {
        __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, "error buffer size %d, %d, %d", bufferSize, offset, size);
        return result;
    }

    if (gUserEnv.frame == 32767)
        gUserEnv.frame = 256;
    else
        gUserEnv.frame++;

    (*env)->GetByteArrayRegion( env, data, offset, size, new_speech);

#ifdef HARDW
    /* set 3 LSB's to zero */
    for(i=0; i < L_FRAME; i++) new_speech[i] = new_speech[i] & 0xFFF8;
#endif

    //__android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, "start : %d %d %d %d", new_speech[0],new_speech[1], new_speech[2], new_speech[3]);

    Pre_Process(new_speech, L_FRAME);
    Coder_ld8k(gUserEnv.prm, gUserEnv.syn, gUserEnv.frame, gUserEnv.vad_enable);
    prm2bits_ld8k( gUserEnv.prm, gUserEnv.serial);

    gUserEnv.nb_words = add((Word16)gUserEnv.serial[1], 2);

    //__android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, "end :  %d",gUserEnv.nb_words);
    result = (*env)->NewByteArray(env, gUserEnv.nb_words*sizeof(Word16));

    (*env)->SetByteArrayRegion (env, result, 0, gUserEnv.nb_words*sizeof(Word16), gUserEnv.serial);

     return result;
}
