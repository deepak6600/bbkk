# CMake का न्यूनतम आवश्यक संस्करण सेट करें।
cmake_minimum_required(VERSION 3.22.1)

# अपनी प्रोजेक्ट का नाम परिभाषित करें।
project("safe_setting_app")

# अपनी नेटिव लाइब्रेरी को परिभाषित करें।
# - native-lib: यह आपकी लाइब्रेरी का नाम है।
# - SHARED: यह बताता है कि यह एक .so फ़ाइल होगी।
# - native-lib.cpp: यह सोर्स फ़ाइल है जिससे लाइब्रेरी बनेगी।
add_library(
        native-lib
        SHARED
        native-lib.cpp)

# एंड्रॉइड लॉगिंग लाइब्रेरी को खोजें और लिंक करें।
find_library(
        log-lib
        log)

# अपनी लाइब्रेरी को लॉगिंग लाइब्रेरी से लिंक करें।
target_link_libraries(
        native-lib
        ${log-lib})