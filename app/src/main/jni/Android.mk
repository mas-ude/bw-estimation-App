LOCAL_PATH := $(call my-dir)
 
include $(CLEAR_VARS)
 
# Here we give our module name and source file(s)
LOCAL_MODULE    := sniffer
LOCAL_SRC_FILES := sniffer.c
 
include $(BUILD_EXECUTABLE)