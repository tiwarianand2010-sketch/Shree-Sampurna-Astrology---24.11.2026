package com.example.core

data class HelplineContact(
    val labelHi: String,
    val labelEn: String,
    val labelZh: String,
    val phoneDisplay: String,
    val phoneDial: String
)

object ContactConfig {
    val helplines = listOf(
        HelplineContact(
            labelHi = "ज्योतिषी संपर्क मोबाइल नं. 1",
            labelEn = "Astrologer Contact Mobile 1",
            labelZh = "占星师联系电话 1",
            phoneDisplay = "+91 7275547797",
            phoneDial = "+917275547797"
        ),
        HelplineContact(
            labelHi = "ज्योतिषी संपर्क मोबाइल नं. 2",
            labelEn = "Astrologer Contact Mobile 2",
            labelZh = "占星师联系电话 2",
            phoneDisplay = "+91 9451140566",
            phoneDial = "+919451140566"
        )
    )
}
