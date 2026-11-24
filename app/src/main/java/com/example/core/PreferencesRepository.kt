package com.example.core

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.model.ChartStyle
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "sampurna_astro_prefs")

enum class AccountMethod {
    NONE,
    GOOGLE,
    MOBILE
}

data class AstrologerAccount(
    val astrologerName: String = "",
    val mobileNumber: String = "",
    val studioName: String = "Shri Sampurna Astrology",
    val email: String = "",
    val accountMethod: AccountMethod = AccountMethod.NONE
) {
    val isConfigured: Boolean
        get() = astrologerName.isNotBlank() || accountMethod != AccountMethod.NONE

    fun effectiveHeaderName(): String =
        astrologerName.trim().ifBlank { "Acharya (Shri Sampurna Astrology)" }
}

class PreferencesRepository(private val context: Context) {

    private object Keys {
        val LANGUAGE = stringPreferencesKey("app_language")
        val CHART_STYLE = stringPreferencesKey("chart_style")
        val ASTROLOGER_NAME = stringPreferencesKey("astrologer_name")
        val ASTROLOGER_MOBILE = stringPreferencesKey("astrologer_mobile")
        val ASTROLOGER_STUDIO = stringPreferencesKey("astrologer_studio")
        val ASTROLOGER_EMAIL = stringPreferencesKey("astrologer_email")
        val ACCOUNT_METHOD = stringPreferencesKey("account_method")
    }

    val languageFlow: Flow<AppLanguage> = context.dataStore.data.map { prefs ->
        AppLanguage.fromCode(prefs[Keys.LANGUAGE])
    }

    val chartStyleFlow: Flow<ChartStyle> = context.dataStore.data.map { prefs ->
        when (prefs[Keys.CHART_STYLE]) {
            ChartStyle.SOUTH_INDIAN.name -> ChartStyle.SOUTH_INDIAN
            else -> ChartStyle.NORTH_INDIAN
        }
    }

    val astrologerAccountFlow: Flow<AstrologerAccount> = context.dataStore.data.map { prefs ->
        val method = when (prefs[Keys.ACCOUNT_METHOD]) {
            AccountMethod.GOOGLE.name -> AccountMethod.GOOGLE
            AccountMethod.MOBILE.name -> AccountMethod.MOBILE
            else -> AccountMethod.NONE
        }
        AstrologerAccount(
            astrologerName = prefs[Keys.ASTROLOGER_NAME].orEmpty(),
            mobileNumber = prefs[Keys.ASTROLOGER_MOBILE].orEmpty(),
            studioName = prefs[Keys.ASTROLOGER_STUDIO]?.takeIf { it.isNotBlank() } ?: "Shri Sampurna Astrology",
            email = prefs[Keys.ASTROLOGER_EMAIL].orEmpty(),
            accountMethod = method
        )
    }

    suspend fun setLanguage(language: AppLanguage) {
        context.dataStore.edit { prefs ->
            prefs[Keys.LANGUAGE] = language.code
        }
    }

    suspend fun setChartStyle(style: ChartStyle) {
        context.dataStore.edit { prefs ->
            prefs[Keys.CHART_STYLE] = style.name
        }
    }

    suspend fun saveAstrologerAccount(account: AstrologerAccount) {
        context.dataStore.edit { prefs ->
            prefs[Keys.ASTROLOGER_NAME] = account.astrologerName.trim()
            prefs[Keys.ASTROLOGER_MOBILE] = account.mobileNumber.trim()
            prefs[Keys.ASTROLOGER_STUDIO] = account.studioName.trim().ifBlank { "Shri Sampurna Astrology" }
            prefs[Keys.ASTROLOGER_EMAIL] = account.email.trim()
            prefs[Keys.ACCOUNT_METHOD] = account.accountMethod.name
        }
    }

    suspend fun clearAstrologerAccount() {
        context.dataStore.edit { prefs ->
            prefs.remove(Keys.ASTROLOGER_NAME)
            prefs.remove(Keys.ASTROLOGER_MOBILE)
            prefs.remove(Keys.ASTROLOGER_EMAIL)
            prefs[Keys.ACCOUNT_METHOD] = AccountMethod.NONE.name
        }
    }
}
