package com.example.core

/**
 * Multi-language UI helper supporting all 10 app languages:
 * Hindi, English, Bengali, Marathi, Gujarati, Tamil, Telugu, Malayalam, Kannada, and Putonghua (Chinese).
 */
fun tr(
    lang: AppLanguage,
    hi: String,
    en: String,
    zh: String = en,
    bn: String = hi,
    mr: String = hi,
    gu: String = hi,
    ta: String = en,
    te: String = en,
    ml: String = en,
    kn: String = en
): String = when (lang) {
    AppLanguage.HI -> hi
    AppLanguage.EN -> en
    AppLanguage.BN -> bn
    AppLanguage.MR -> mr
    AppLanguage.GU -> gu
    AppLanguage.TA -> ta
    AppLanguage.TE -> te
    AppLanguage.ML -> ml
    AppLanguage.KN -> kn
    AppLanguage.ZH -> zh
}
