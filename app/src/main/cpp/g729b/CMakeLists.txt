# For more information about using CMake with Android Studio, read the
# documentation: https://d.android.com/studio/projects/add-native-code.html

# Sets the minimum version of CMake required to build the native library.

cmake_minimum_required(VERSION 3.4.1)

# Creates and names a library, sets it as either STATIC
# or SHARED, and provides the relative paths to its source code.
# You can define multiple libraries, and CMake builds them for you.
# Gradle automatically packages shared libraries with your APK.

set(SOURCE_DIR g729AnnexB)

include_directories(${SOURCE_DIR})

add_library( native-g729-enc SHARED
        main-g729-enc.c
        ${SOURCE_DIR}/acelp_co.c
        ${SOURCE_DIR}/basic_op.c
        ${SOURCE_DIR}/bits.c
        ${SOURCE_DIR}/cod_ld8k.c
        ${SOURCE_DIR}/coder.c
        ${SOURCE_DIR}/dspfunc.c
        ${SOURCE_DIR}/filter.c
        ${SOURCE_DIR}/gainpred.c
        ${SOURCE_DIR}/lpc.c
        ${SOURCE_DIR}/lpcfunc.c
        ${SOURCE_DIR}/lspgetq.c
        ${SOURCE_DIR}/oper_32b.c
        ${SOURCE_DIR}/p_parity.c
        ${SOURCE_DIR}/pitch.c
        ${SOURCE_DIR}/pre_proc.c
        ${SOURCE_DIR}/pred_lt3.c
        ${SOURCE_DIR}/pwf.c
        ${SOURCE_DIR}/qua_gain.c
        ${SOURCE_DIR}/qua_lsp.c
        ${SOURCE_DIR}/tab_ld8k.c
        ${SOURCE_DIR}/util.c
        ${SOURCE_DIR}/taming.c
        ${SOURCE_DIR}/vad.c
        ${SOURCE_DIR}/dtx.c
        ${SOURCE_DIR}/qsidgain.c
        ${SOURCE_DIR}/qsidlsf.c
        ${SOURCE_DIR}/calcexc.c
        ${SOURCE_DIR}/tab_dtx.c
        ${SOURCE_DIR}/dec_sid.c
		)

add_library(native-g729-dec SHARED
        main-g729-dec.c
        ${SOURCE_DIR}/basic_op.c
        ${SOURCE_DIR}/bits.c
        ${SOURCE_DIR}/decoder.c
        ${SOURCE_DIR}/de_acelp.c
        ${SOURCE_DIR}/dec_gain.c
        ${SOURCE_DIR}/dec_lag3.c
        ${SOURCE_DIR}/dec_ld8k.c
        ${SOURCE_DIR}/dspfunc.c
        ${SOURCE_DIR}/filter.c
        ${SOURCE_DIR}/gainpred.c
        ${SOURCE_DIR}/lpcfunc.c
        ${SOURCE_DIR}/lspdec.c
        ${SOURCE_DIR}/lspgetq.c
        ${SOURCE_DIR}/oper_32b.c
        ${SOURCE_DIR}/p_parity.c
        ${SOURCE_DIR}/post_pro.c
        ${SOURCE_DIR}/pred_lt3.c
        ${SOURCE_DIR}/pst.c
        ${SOURCE_DIR}/tab_ld8k.c
        ${SOURCE_DIR}/util.c
        ${SOURCE_DIR}/taming.c
        ${SOURCE_DIR}/dec_sid.c
        ${SOURCE_DIR}/qsidgain.c
        ${SOURCE_DIR}/calcexc.c
        ${SOURCE_DIR}/tab_dtx.c
        )

find_library(log-lib log )

target_link_libraries(native-g729-enc ${log-lib} )
target_link_libraries(native-g729-dec ${log-lib} )