/*
 * Copyright (C) 2009 The Sipdroid Open Source Project
 *
 * This file is part of Sipdroid (http://www.sipdroid.org)
 *
 * Sipdroid is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * This source code is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this source code; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

#include <stdlib.h>
#include <stdio.h>
#include <fcntl.h>
#include <unistd.h>
#include <memory.h>
#include <ctype.h>
#include <jni.h>
#include <android/log.h>

#include "spandsp.h"

/* Define codec specific settings */
#define BLOCK_LEN       160
#define GSM_BYTE_LEN     33

#define LOG_TAG "gsm" // text for log tag

#undef DEBUG_GSM


static int codec_open = 0;


gsm0610_state_t *gsm0610_enc_state;
gsm0610_state_t *gsm0610_dec_state;
/////////////////////////////////////////////////////////////////////////////////////
// JniGsmOpen
/////////////////////////////////////////////////////////////////////////////////////
extern "C"
JNIEXPORT jint JNICALL Java_arch3_lge_com_voip_model_codec_audio_AudioGSM0610_JniGsmOpen
        (JNIEnv *env, jobject obj) {

    if (codec_open != 0) {
        codec_open++;
        return (jint) 0;
    }

    if ((gsm0610_enc_state = gsm0610_init(NULL, GSM0610_PACKING_VOIP)) == NULL)
    {
       return -1;
    }

    if ((gsm0610_dec_state = gsm0610_init(NULL, GSM0610_PACKING_VOIP)) == NULL)
    {
        gsm0610_release(gsm0610_enc_state);
        return -1;
    }
    codec_open++;
    return (jint)0;
}
/////////////////////////////////////////////////////////////////////////////////////
// End JniGsmOpen
/////////////////////////////////////////////////////////////////////////////////////
/////////////////////////////////////////////////////////////////////////////////////
// JniGsmEncode Normal
/////////////////////////////////////////////////////////////////////////////////////
extern "C"
JNIEXPORT jint JNICALL Java_arch3_lge_com_voip_model_codec_audio_AudioGSM0610_JniGsmEncode(JNIEnv *env, jobject obj, jshortArray lin, jbyteArray encoded) {

    jshort pre_amp[BLOCK_LEN];
    jbyte gsm0610_data[GSM_BYTE_LEN];

    int ret;

    if (!codec_open)
        return 0;

#ifdef DEBUG_GSM
    __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG,
            "encoding frame size: %d\toffset: %d\n", size, offset);
#endif


#ifdef DEBUG_GSM
        __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG,
            "encoding frame size: %d\toffset: %d i: %d\n", size, offset, i);
#endif

        env->GetShortArrayRegion(lin, 0,BLOCK_LEN, pre_amp);

        ret=gsm0610_encode(gsm0610_enc_state, (uint8_t *) gsm0610_data, pre_amp, BLOCK_LEN);

#ifdef DEBUG_GSM
        __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG,
				"Enocded Bytes: %d\n", ret);
#endif
        /* Write payload */
        env->SetByteArrayRegion(encoded, 0, ret, gsm0610_data);

#ifdef DEBUG_GSM
    __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG,
        "encoding **END** frame size: %d\toffset: %d i: %d lin_pos: %d\n", size, offset, i, lin_pos);
#endif

    return (jint)ret;
}
/////////////////////////////////////////////////////////////////////////////////////
// End JniGsmEncode Normal
/////////////////////////////////////////////////////////////////////////////////////
/////////////////////////////////////////////////////////////////////////////////////
// All bytes JniGsmEncode
/////////////////////////////////////////////////////////////////////////////////////
extern "C"
JNIEXPORT jbyteArray JNICALL Java_arch3_lge_com_voip_model_codec_audio_AudioGSM0610_JniGsmEncodeB(JNIEnv *env, jobject obj, jbyteArray data, jint offset, jint size) {

    jbyteArray result;
    jbyte pre_amp[BLOCK_LEN*2];
    jbyte gsm0610_data[GSM_BYTE_LEN];
    int ret;

    if (!codec_open)
        return 0;

#ifdef DEBUG_GSM
    __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG,
            "encoding frame size: %d\toffset: %d\n", size, offset);
#endif

#ifdef DEBUG_GSM
        __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG,
            "encoding frame size: %d\toffset: %d i: %d\n", size, offset, i);
#endif

        env->GetByteArrayRegion(data, 0,BLOCK_LEN*2, pre_amp);

        ret=gsm0610_encode(gsm0610_enc_state, (uint8_t *) gsm0610_data, (int16_t *)pre_amp, BLOCK_LEN);

#ifdef DEBUG_GSM
        __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG,
				"Enocded Bytes: %d\n", ret);
#endif
        /* Write payload */
        result = env->NewByteArray(ret);
        env->SetByteArrayRegion(result,0, ret, gsm0610_data);

#ifdef DEBUG_GSM
    __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG,
        "encoding **END** frame size: %d\toffset: %d i: %d lin_pos: %d\n", size, offset, i, lin_pos);
#endif

    return result;
}
/////////////////////////////////////////////////////////////////////////////////////
// End All bytes JniGsmEncode
/////////////////////////////////////////////////////////////////////////////////////
/////////////////////////////////////////////////////////////////////////////////////
// JniGsmDecode Normal
/////////////////////////////////////////////////////////////////////////////////////
extern "C"
JNIEXPORT jint JNICALL Java_arch3_lge_com_voip_model_codec_audio_AudioGSM0610_JniGsmDecode(JNIEnv *env, jobject obj, jbyteArray encoded, jshortArray lin) {

    jshort post_amp[BLOCK_LEN];
    jbyte gsm0610_data[GSM_BYTE_LEN];

    int len;

    if (!codec_open)
        return 0;

#ifdef DEBUG_GSM
    __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG,
        "##### BEGIN DECODE ********  decoding frame size: %d\n", size);
#endif

    env->GetByteArrayRegion(encoded, 0, GSM_BYTE_LEN, gsm0610_data);
    len = gsm0610_decode(gsm0610_dec_state, post_amp,(uint8_t *) gsm0610_data, GSM_BYTE_LEN);

#ifdef DEBUG_GSM
    __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG,
			"##### DECODED length: %d\n", len);
#endif

    env->SetShortArrayRegion(lin, 0, len,post_amp);
    return (jint)len;
}
/////////////////////////////////////////////////////////////////////////////////////
// End JniGsmDecode Normal
/////////////////////////////////////////////////////////////////////////////////////
/////////////////////////////////////////////////////////////////////////////////////
// All Bytes JniGsmDecode
/////////////////////////////////////////////////////////////////////////////////////
extern "C"
JNIEXPORT jbyteArray JNICALL Java_arch3_lge_com_voip_model_codec_audio_AudioGSM0610_JniGsmDecodeB(JNIEnv *env, jobject obj, jbyteArray data, jint offset, jint size) {

    jbyteArray result;
    jbyte post_amp[BLOCK_LEN*2];
    jbyte gsm0610_data[GSM_BYTE_LEN];

    int len;

    if (!codec_open)
        return 0;

#ifdef DEBUG_GSM
    __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG,
        "##### BEGIN DECODE ********  decoding frame size: %d\n", size);
#endif

    env->GetByteArrayRegion(data, 0, GSM_BYTE_LEN, gsm0610_data);
    len = gsm0610_decode(gsm0610_dec_state,(int16_t *) post_amp,(uint8_t *) gsm0610_data,GSM_BYTE_LEN);

#ifdef DEBUG_GSM
    __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG,
			"##### DECODED length: %d\n", len);
#endif

    result = env->NewByteArray(len*2);
    env->SetByteArrayRegion(result, 0, len*2,post_amp);
    return result;
}
/////////////////////////////////////////////////////////////////////////////////////
// End All Bytes JniGsmDecode
/////////////////////////////////////////////////////////////////////////////////////
/////////////////////////////////////////////////////////////////////////////////////
// JniGsmClose
/////////////////////////////////////////////////////////////////////////////////////
extern "C"
JNIEXPORT void JNICALL Java_arch3_lge_com_voip_model_codec_audio_AudioGSM0610_JniGsmClose(JNIEnv *env, jobject obj) {

    if (--codec_open != 0)
        return;

    gsm0610_release(gsm0610_enc_state);
    gsm0610_release(gsm0610_dec_state);

}
/////////////////////////////////////////////////////////////////////////////////////
// End JniGsmClose
/////////////////////////////////////////////////////////////////////////////////////