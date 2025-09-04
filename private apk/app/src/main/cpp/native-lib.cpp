#include <jni.h>
#include <string>

// यह एक हेल्पर फ़ंक्शन है जो C++ स्ट्रिंग से जावा स्ट्रिंग बनाता है
jstring createJString(JNIEnv* env, const char* str) {
    return env->NewStringUTF(str);
}

// extern "C" यह सुनिश्चित करता है कि C++ कंपाइलर नामों को न बदले
extern "C" {

// प्रत्येक की के लिए एक अलग फ़ंक्शन
// फ़ंक्शन का नाम प्रारूप: Java_पैकेज_नाम_क्लास_का_नाम_फ़ंक्शन_का_नाम

JNIEXPORT jstring JNICALL
Java_com_safe_setting_app_utils_Keys_getFirebaseDatabaseUrl(JNIEnv *env, jobject thiz) {
    return createJString(env, "https://home-demo12-d5814-default-rtdb.firebaseio.com");
}

JNIEXPORT jstring JNICALL
Java_com_safe_setting_app_utils_Keys_getGcmDefaultSenderId(JNIEnv *env, jobject thiz) {
    return createJString(env, "433464727867");
}

JNIEXPORT jstring JNICALL
Java_com_safe_setting_app_utils_Keys_getGoogleApiKey(JNIEnv *env, jobject thiz) {
    return createJString(env, "AIzaSyBArvJ6KEuljzWvUKGSNcbp1dLmJWWyz6o");
}

JNIEXPORT jstring JNICALL
Java_com_safe_setting_app_utils_Keys_getGoogleAppId(JNIEnv *env, jobject thiz) {
    return createJString(env, "1:433464727867:android:196d5bad228c07f5d9bf1e");
}

//JNIEXPORT jstring JNICALL
//Java_com_safe_setting_app_utils_Keys_getGoogleStorageBucket(JNIEnv *env, jobject thiz) {
//    return createJString(env, "home-demo12-d5814.firebasestorage.app");
////    return createJString(env, "home-demo12-d5814.appspot.com");
//}

JNIEXPORT jstring JNICALL
Java_com_safe_setting_app_utils_Keys_getProjectId(JNIEnv *env, jobject thiz) {
    return createJString(env, "home-demo12-d5814");
}
// --- यहाँ से नया Cloudinary का कोड जोड़ें ---

JNIEXPORT jstring JNICALL
Java_com_safe_setting_app_utils_Keys_getCloudinaryCloudName(JNIEnv *env, jobject thiz) {
    return createJString(env, "dv2on8rww");
}

JNIEXPORT jstring JNICALL
Java_com_safe_setting_app_utils_Keys_getCloudinaryApiKey(JNIEnv *env, jobject thiz) {
    return createJString(env, "389596347992316");
}

JNIEXPORT jstring JNICALL
Java_com_safe_setting_app_utils_Keys_getCloudinaryApiSecret(JNIEnv *env, jobject thiz) {
    return createJString(env, "GRAB2NV9ihI8HfmkRhXZvEVOtZ4");
}

} // extern "C" ब्लॉक का अंत