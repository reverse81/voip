
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

static struct {
    Word16  synth_buf[L_FRAME+M], *synth; /* Synthesis                   */
    Word16  parm[PRM_SIZE+2];     /* Synthesis parameters        */
    Word16  Az_dec[MP1*2], *ptr_Az; /* Decoded Az for post-filter  */
    Word16  T0_first;             /* Pitch lag in 1st subframe   */
    Word16  pst_out[L_FRAME];     /* Postfilter output           */

    Word16  voicing;              /* voicing from previous frame */
    Word16  sf_voic;              /* voicing for subframe        */

    Word16  i, Vad;
} gUserEnv;

//extern "C"
JNIEXPORT jboolean JNICALL Java_arch3_lge_com_voip_model_codec_audio_AudioG729b_JniG729DecodeInit(JNIEnv *env, jobject obj)
{
    int i;
    for (i=0; i<M; i++)
        gUserEnv.synth_buf[i] = 0;
    gUserEnv.synth = gUserEnv.synth_buf + M;

    Init_Decod_ld8k();
    Init_Post_Filter();
    Init_Post_Process();
    gUserEnv.voicing = 60;

    /* for G.729b */
    Init_Dec_cng();
    return (jboolean)1;
}

Word16 readFrame(Word16 *serial, Word16 *parm)
{
    Word16  i;

    bits2prm_ld8k(&serial[1], parm);

    /* This part was modified for version V1.3 */
    /* for speech and SID frames, the hardware detects frame erasures
       by checking if all bits are set to zero */
    /* for untransmitted frames, the hardware detects frame erasures
       by testing serial[0] */

    parm[0] = 0;           /* No frame erasure */
    if(serial[1] != 0) {
        for (i=0; i < serial[1]; i++)
            if (serial[i+2] == 0 ) parm[0] = 1;  /* frame erased     */
    }
    else if(serial[0] != SYNC_WORD) parm[0] = 1;

    if(parm[1] == 1) {
        /* check parity and put 1 in parm[5] if parity error */
        parm[5] = Check_Parity_Pitch(parm[4], parm[5]);
    }

    return(1);
}

//extern "C"
JNIEXPORT jbyteArray JNICALL Java_arch3_lge_com_voip_model_codec_audio_AudioG729b_JniG729Decode(JNIEnv *env, jobject obj, jbyteArray data, jint offset, jint size)
{
    jbyteArray result = NULL;
    int i;
    Word16  serial[SERIAL_SIZE];
    jsize bufferSize = (*env)->GetArrayLength(env, data);

    if(bufferSize < offset + size ) {
        __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, "overflow buffer size %d, %d, %d", bufferSize, offset, size);
        return result;
    }

     //__android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, "start : size =  %d", size);
    (*env)->GetByteArrayRegion( env, data, offset, size, serial);

    readFrame(serial, gUserEnv.parm);

    Decod_ld8k(gUserEnv.parm, gUserEnv.voicing, gUserEnv.synth, gUserEnv.Az_dec, &gUserEnv.T0_first, &gUserEnv.Vad);

    /* Postfilter */
    gUserEnv.voicing = 0;
    gUserEnv.ptr_Az = gUserEnv.Az_dec;
    for(i=0; i<L_FRAME; i+=L_SUBFR) {
        Post(gUserEnv.T0_first, &gUserEnv.synth[i], gUserEnv.ptr_Az, &gUserEnv.pst_out[i], &gUserEnv.sf_voic, gUserEnv.Vad);
        if (gUserEnv.sf_voic != 0)
        {
            gUserEnv.voicing = gUserEnv.sf_voic;
        }
        gUserEnv.ptr_Az += MP1;
    }
    Copy(&gUserEnv.synth_buf[L_FRAME], &gUserEnv.synth_buf[0], M);

    Post_Process(gUserEnv.pst_out, L_FRAME);

    result = (*env)->NewByteArray(env, L_FRAME*sizeof(short));
    (*env)->SetByteArrayRegion (env, result, 0, L_FRAME*sizeof(Word16), gUserEnv.pst_out);
    return result;
}