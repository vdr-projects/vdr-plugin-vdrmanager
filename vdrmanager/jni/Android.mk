LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE    := native_des
LOCAL_SRC_FILES := de_bjusystems_vdrmanager_utils_crypt_NativeDES.c

include $(BUILD_SHARED_LIBRARY)
