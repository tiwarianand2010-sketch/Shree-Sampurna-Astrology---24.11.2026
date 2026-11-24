package com.example.core

enum class AppLanguage(val code: String, val nativeName: String) {
    HI("hi", "हिन्दी"),
    EN("en", "English"),
    BN("bn", "বাংলা"),
    MR("mr", "मराठी"),
    GU("gu", "ગુજરાતી"),
    TA("ta", "தமிழ்"),
    TE("te", "తెలుగు"),
    ML("ml", "മലയാളം"),
    KN("kn", "ಕನ್ನಡ"),
    ZH("zh", "中文 (普通话)");

    companion object {
        fun fromCode(code: String?): AppLanguage =
            entries.firstOrNull { it.code == code } ?: HI
    }
}
