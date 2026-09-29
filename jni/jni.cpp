#include <vector>
#include <stdexcept>
#include <jni.h>

#include "api/backup.hpp"

extern "C"
{
    #include "drivers/backup.h"
}

using namespace std;

/*
 * Settings-store access for com.artec.evftoggle.NativeBackup. Adapted from Recipe Lab's jni.cpp
 * (same driver path as OpenMemories-Tweak). Single settings values only; firmware is never touched.
 */

static void throw_native(JNIEnv *env, const char *msg)
{
    jclass c = env->FindClass("com/artec/evftoggle/NativeException");
    if (c) env->ThrowNew(c, msg);
}

static int prop_size(int id)
{
    int res = Backup_get_datasize(id);
    if (res <= 0) throw backup_error("Backup_get_datasize failed");
    return res;
}

extern "C" JNIEXPORT jbyteArray Java_com_artec_evftoggle_NativeBackup_read(JNIEnv *env, jclass clazz, jint id)
{
    jbyteArray arr = NULL;
    try {
        int size = prop_size(id);
        vector<char> v(size);
        int res = Backup_read(id, &v[0]);
        if (res < 0 || res != size) throw backup_error("Backup_read failed");
        arr = env->NewByteArray(size);
        env->SetByteArrayRegion(arr, 0, size, (const jbyte *) &v[0]);
    } catch (const runtime_error &e) {
        throw_native(env, e.what());
    }
    return arr;
}

extern "C" JNIEXPORT void Java_com_artec_evftoggle_NativeBackup_write(JNIEnv *env, jclass clazz, jint id, jbyteArray data)
{
    try {
        jsize n = env->GetArrayLength(data);
        int size = prop_size(id);
        if (n != size) throw backup_error("size mismatch");
        vector<char> v(n);
        env->GetByteArrayRegion(data, 0, n, (jbyte *) &v[0]);
        int res = Backup_write(id >> 16, id, &v[0]);
        if (res == -BACKUP_ERROR_READ_ONLY) throw backup_protected_error();
        if (res < 0 || res != size) throw backup_error("Backup_write failed");
    } catch (const runtime_error &e) {
        throw_native(env, e.what());
    }
}

extern "C" JNIEXPORT void Java_com_artec_evftoggle_NativeBackup_sync(JNIEnv *env, jclass clazz)
{
    Backup_sync_all();
}
