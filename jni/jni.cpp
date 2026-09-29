#include <vector>
#include <stdexcept>
#include <cstring>
#include <cstddef>
#include <jni.h>
#include <unistd.h>
#include <signal.h>
#include <sys/types.h>
#include <sys/wait.h>
#include <sys/socket.h>
#include <sys/un.h>

#include "api/backup.hpp"

extern "C"
{
    #include "drivers/backup.h"
}

using namespace std;

/*
 * Settings-store access for com.artec.evftoggle.NativeBackup, adapted from Recipe Lab's jni.cpp
 * (same driver path as OpenMemories-Tweak), plus a background watcher that does what evf1.sh did:
 * when the custom button's "Deactivate Monitor" state (0x01070b09) changes, set the active display
 * (0x010708e0) to Viewfinder (01) or Monitor (02).
 */

#define TRIG_ID 0x01070b09
#define DISP_ID 0x010708e0
#define WATCH_LOCK "evftoggle-watcher"

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

/* ---------------------------------------------------------------- watcher (plain C, no exceptions) */

static int read_byte(int id)
{
    int size = Backup_get_datasize(id);
    if (size <= 0 || size > 64) return -1;
    char buf[64];
    if (Backup_read(id, buf) != size) return -1;
    return buf[0] & 0xff;
}

static void write_byte(int id, int value)
{
    if (Backup_get_datasize(id) != 1) return;
    char b = (char) value;
    Backup_write(id >> 16, id, &b);
}

/* An abstract unix socket as a "only one watcher" lock: no files, and it disappears when the watcher dies. */
static int grab_lock()
{
    int fd = socket(AF_UNIX, SOCK_STREAM, 0);
    if (fd < 0) return -1;
    struct sockaddr_un a;
    memset(&a, 0, sizeof(a));
    a.sun_family = AF_UNIX;
    memcpy(a.sun_path + 1, WATCH_LOCK, sizeof(WATCH_LOCK) - 1);  /* sun_path[0] = 0 -> abstract name */
    socklen_t len = (socklen_t) (offsetof(struct sockaddr_un, sun_path) + 1 + sizeof(WATCH_LOCK) - 1);
    if (bind(fd, (struct sockaddr *) &a, len) < 0) { close(fd); return -2; }
    return fd;
}

static void watch_loop()
{
    int last = read_byte(TRIG_ID);
    for (;;) {
        int cur = read_byte(TRIG_ID);
        if (cur >= 0) {
            if (last >= 0 && cur != last) write_byte(DISP_ID, cur == 0x01 ? 0x01 : 0x02);
            last = cur;
        }
        usleep(250000);
    }
}

/* 0 = started, 1 = already running, negative = could not start */
extern "C" JNIEXPORT jint Java_com_artec_evftoggle_NativeBackup_startWatcher(JNIEnv *env, jclass clazz)
{
    int lock = grab_lock();
    if (lock == -2) return 1;
    if (lock < 0) return -1;

    pid_t pid = fork();
    if (pid < 0) { close(lock); return -2; }
    if (pid == 0) {
        /* first child: new session, then fork again so the watcher belongs to init, not to the app */
        setsid();
        signal(SIGHUP, SIG_IGN);
        signal(SIGTERM, SIG_IGN);
        pid_t pid2 = fork();
        if (pid2 != 0) _exit(0);
        watch_loop();
        _exit(0);
    }
    close(lock);                        /* the watcher keeps its own copy of the lock */
    int status;
    waitpid(pid, &status, 0);
    return 0;
}

/* 1 if a watcher holds the lock */
extern "C" JNIEXPORT jboolean Java_com_artec_evftoggle_NativeBackup_watcherRunning(JNIEnv *env, jclass clazz)
{
    int lock = grab_lock();
    if (lock == -2) return JNI_TRUE;
    if (lock >= 0) close(lock);
    return JNI_FALSE;
}
