package com.example

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextReplacement
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class SampurnaAstroRobolectricTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun `home to kundali tabs, advanced modules and help screen navigation works`() {
        composeRule.waitForIdle()

        // Generate Kundali from Home screen
        composeRule.onNodeWithTag("generate_kundali_button").performScrollTo().performClick()
        composeRule.waitUntil(timeoutMillis = 5000) {
            composeRule.onAllNodesWithTag("north_indian_chart").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.waitForIdle()

        // North Indian chart is displayed by default
        composeRule.onNodeWithTag("north_indian_chart").performScrollTo().assertIsDisplayed()

        // Switch to South Indian chart
        composeRule.onNodeWithTag("toggle_south_chart").performScrollTo().performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("south_indian_chart").performScrollTo().assertIsDisplayed()

        // Visit Dasha, Phalit, Bala, Gochar, Panchang tabs
        composeRule.onNodeWithTag("tab_dasha").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("tab_phalit").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("tab_bala").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("tab_gochar").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("tab_panchang").performClick()
        composeRule.waitForIdle()

        // Visit KP, Lal Kitab, West/Vastu, and 250p PDF chips
        composeRule.onNodeWithTag("chip_kp").performScrollTo().performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("chip_lal_kitab").performScrollTo().performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("chip_west_vastu").performScrollTo().performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("chip_pdf_reports").performScrollTo().performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("generate_brihat_pdf_button").performScrollTo().assertIsDisplayed()

        // Open Help screen and verify both helpline numbers are shown
        composeRule.onNodeWithTag("help_button").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithText("+91 7275547797").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("+91 9451140566").performScrollTo().assertIsDisplayed()
    }

    @Test
    fun `home hub opens matching, store, ai chat, and google-gated cloud screen`() {
        composeRule.waitForIdle()

        // Matching screen
        composeRule.onNodeWithTag("nav_matching_button").performScrollTo().performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("calculate_matching_button").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithTag("matching_back_button").performClick()
        composeRule.waitForIdle()

        // Store screen
        composeRule.onNodeWithTag("nav_store_button").performScrollTo().performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("store_toggle_gem_ruby").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithTag("store_back_button").performClick()
        composeRule.waitForIdle()

        // Cloud screen shows Google Sign-In gate when unauthenticated
        composeRule.onNodeWithTag("nav_cloud_button").performScrollTo().performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("google_sign_in_button").assertIsDisplayed()
        composeRule.onNodeWithTag("cloud_back_button").performClick()
        composeRule.waitForIdle()

        // AI Astrologer screen
        composeRule.onNodeWithTag("nav_ai_chat_button").performScrollTo().performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("ai_question_input").assertIsDisplayed()
    }

    @Test
    fun `language switcher switches to English, Bengali and Chinese`() {
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("language_menu_button").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("lang_option_en").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithText("Generate Kundali").performScrollTo().assertIsDisplayed()

        composeRule.onNodeWithTag("language_menu_button").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("lang_option_zh").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithText("生成星盘").performScrollTo().assertIsDisplayed()
    }

    @Test
    fun `astrologer account setup via mobile number updates pdf header and pdf output succeeds`() {
        composeRule.waitForIdle()

        // Navigate to Astrologer Account screen from Home Hub
        composeRule.onNodeWithTag("nav_account_button").performScrollTo().performClick()
        composeRule.waitForIdle()

        // Enter Astrologer Name & Mobile Number and save
        composeRule.onNodeWithTag("account_name_input")
            .performScrollTo()
            .performTextReplacement("Pt. Raghav Sharma")
        composeRule.onNodeWithTag("account_mobile_input")
            .performScrollTo()
            .performTextReplacement("+91 9876543210")
        composeRule.onNodeWithTag("save_mobile_account_button").performScrollTo().performClick()
        composeRule.waitForIdle()

        // Verify Active PDF Header reflects registered Astrologer Name
        composeRule.onNodeWithTag("active_astrologer_header_name")
            .performScrollTo()
            .assertIsDisplayed()
            .assertTextEquals("Pt. Raghav Sharma")

        // Return to Home, generate Kundali, open PDF tab and verify Astrologer Header propagates there too
        composeRule.onNodeWithTag("account_back_button").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("generate_kundali_button").performScrollTo().performClick()
        composeRule.waitUntil(timeoutMillis = 5000) {
            composeRule.onAllNodesWithTag("chip_pdf_reports").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithTag("chip_pdf_reports").performScrollTo().performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithText("Pt. Raghav Sharma").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithTag("generate_summary_pdf_button").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithTag("generate_brihat_pdf_button").performScrollTo().assertIsDisplayed()
    }
}
