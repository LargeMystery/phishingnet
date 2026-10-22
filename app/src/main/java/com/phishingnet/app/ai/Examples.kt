package com.phishingnet.app.ai

/** Ready-made examples for the "Try an example" buttons. */
object Examples {

    data class Email(val label: String, val sender: String, val subject: String, val body: String)
    data class Link(val label: String, val url: String)

    val emails = listOf(
        Email(
            "Fake bank alert",
            "Kaspi Security <security@kaspi-verify.top>",
            "URGENT: Your account has been suspended!!",
            """
                Dear Customer,

                We detected unusual activity on your account. Your account will be closed within 24 hours unless you verify your identity.

                Confirm your card number and the verification code we sent you here:
                http://kaspi.kz.secure-login.top/verify

                Kaspi Security Team
            """.trimIndent(),
        ),
        Email(
            "CEO gift card scam",
            "John Miller CEO <john.miller.ceo@gmail.com>",
            "Quick favour",
            """
                Hi,

                Are you available? I'm in a meeting and can't talk right now. I need you to buy 5 Google Play gift cards for a client, 100 each. Keep this confidential, it's a surprise.

                Send me the codes as soon as possible.

                John
            """.trimIndent(),
        ),
        Email(
            "Fake voicemail",
            "Voicemail Service <no-reply@v-mail-system.net>",
            "You have (3) held messages",
            "You received a new voice message (0:42).\nTo listen to it, open the attached HTML file (voice_message_9841.htm) in your browser.",
        ),
        Email(
            "Russian bank scam",
            "Служба безопасности банка <security@bank-kz-alert.com>",
            "Ваша карта заблокирована",
            """
                Уважаемый клиент, по вашей карте зафиксирована подозрительная операция. Карта заблокирована.
                Срочно подтвердите данные карты: https://bank-kz-alert.com/unlock
                или позвоните по номеру +7 (777) 000-12-34 и сообщите код из SMS.
            """.trimIndent(),
        ),
        Email(
            "Normal email",
            "Aigerim Bekova <aigerim@company-example.kz>",
            "Notes from Tuesday's meeting",
            """
                Hi team,

                Thanks for joining on Tuesday. I've put the notes in our shared folder as we discussed, and I'll send the updated timeline on Friday.

                Have a good week,
                Aigerim
            """.trimIndent(),
        ),
    )

    val links = listOf(
        Link("Fake PayPal", "https://paypal.com.account-verify.top/signin?session=88a1"),
        Link("Look-alike", "https://www.rnicrosoft-login.com/office365/auth"),
        Link("Hidden redirect", "https://www.google.com/url?q=https://kaspi-bonus.click/login"),
        Link("Shortened", "https://bit.ly/3xYzAbC"),
        Link("Real site", "https://kaspi.kz/shop/"),
    )
}
