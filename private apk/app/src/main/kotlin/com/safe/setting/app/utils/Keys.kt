package com.safe.setting.app.utils

object Keys {

    // यह सिस्टम को बताता है कि "native-lib" नाम की लाइब्रेरी को लोड करना है।
    // यह नाम CMakeLists.txt में दिए गए नाम से मेल खाना चाहिए।
    init {
        System.loadLibrary("native-lib")
    }

    // यह native-lib.cpp में परिभाषित हमारे C++ फ़ंक्शंस को घोषित करता है।
    external fun getFirebaseDatabaseUrl(): String
    external fun getGcmDefaultSenderId(): String
    external fun getGoogleApiKey(): String
    external fun getGoogleAppId(): String
//    external fun getGoogleStorageBucket(): String
    external fun getProjectId(): String

    // --- यहाँ Cloudinary के लिए नए फंक्शन्स जोड़ें ---
    external fun getCloudinaryCloudName(): String
    external fun getCloudinaryApiKey(): String
    external fun getCloudinaryApiSecret(): String
}