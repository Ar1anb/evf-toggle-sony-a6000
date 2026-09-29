LOCAL_PATH := $(call my-dir)

MODE = ANDROID
PLATFORMDIR = platform
include $(LOCAL_PATH)/$(PLATFORMDIR)/vars.mk

# Stub shared libraries needed to link libevftoggle.so. The real ones are already on the camera.
# Newer OpenMemories-Platform keeps these stubs as assembly in stubs/<lib>.S; older versions had
# drivers/<lib>.c. Take whichever exists.
$(foreach lib, $(LIBS), \
    $(eval include $(CLEAR_VARS)) \
    $(eval LOCAL_MODULE := $(lib)) \
    $(eval LOCAL_SRC_FILES := $(wildcard $(addprefix $(LOCAL_PATH)/$(PLATFORMDIR)/$(DRIVERDIR)/$(lib), .c .cpp)) $(wildcard $(LOCAL_PATH)/$(PLATFORMDIR)/$(STUBSDIR)/$(lib).S)) \
    $(eval LOCAL_C_INCLUDES := $(LOCAL_PATH)/$(PLATFORMDIR) $(LOCAL_PATH)/$(PLATFORMDIR)/$(STUBSDIR)) \
    $(eval LOCAL_CONLYFLAGS += -std=c11) \
    $(eval LOCAL_LDFLAGS += $(LFLAGS)) \
    $(eval include $(BUILD_SHARED_LIBRARY)) \
)

# Compile libevftoggle.so (settings-store access)
include $(CLEAR_VARS)
LOCAL_MODULE := evftoggle
LOCAL_SRC_FILES := jni.cpp $(foreach source, $(SOURCES), $(wildcard $(addprefix $(LOCAL_PATH)/$(source), .c .cpp)))
LOCAL_C_INCLUDES := $(LOCAL_PATH)/$(PLATFORMDIR)
LOCAL_CFLAGS += $(DEFS) $(WFLAGS) -fvisibility=hidden
LOCAL_CONLYFLAGS += -std=c11
LOCAL_CPPFLAGS += -std=c++98 -Wno-vla -Wno-variadic-macros -fexceptions
LOCAL_LDFLAGS += -Wl,--gc-sections -Wl,--exclude-libs,ALL -Wl,--no-undefined
LOCAL_LDLIBS := -lgcc
LOCAL_SHARED_LIBRARIES := $(LIBS)
include $(BUILD_SHARED_LIBRARY)
