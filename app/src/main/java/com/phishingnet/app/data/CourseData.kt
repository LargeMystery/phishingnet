package com.phishingnet.app.data

/**
 * All course content lives here. To add a part, append a CourseModule to [modules].
 * To edit text, just change the strings — the UI picks everything up automatically.
 */
object CourseData {

    val modules: List<CourseModule> = listOf(
        // ─────────────────────────────────────────────── PART 1
        CourseModule(
            id = 1,
            title = "What is phishing?",
            summary = "The basics: what attackers want and why phishing works so well.",
            lessons = listOf(
                Lesson(
                    title = "Phishing in one sentence",
                    paragraphs = listOf(
                        "Phishing is a scam where an attacker pretends to be someone you trust — a bank, a delivery company, your boss, a friend — to trick you into doing something that helps them.",
                        "That \"something\" is usually one of three things: giving away information (passwords, card numbers, one-time codes), clicking a link that leads to a fake website, or opening a file that installs malware.",
                        "The name comes from \"fishing\": the attacker throws out bait and waits for someone to bite. The \"ph\" spelling is an old hacker-culture habit.",
                    ),
                    keyPoints = listOf(
                        "Phishing = impersonation + a request.",
                        "The goal is your data, your money, or access to your device.",
                    ),
                ),
                Lesson(
                    title = "Why it works",
                    paragraphs = listOf(
                        "Phishing doesn't break technology — it targets people. Attackers use the same psychological levers again and again:",
                        "Urgency (\"Your account will be closed in 24 hours\"), fear (\"Suspicious login detected\"), authority (\"This is the CEO, I need this done now\"), curiosity (\"See who viewed your profile\"), and greed (\"You've won a prize\").",
                        "All of these push you to act fast and skip the moment where you would normally stop and think. Slowing down is the single most effective defence.",
                    ),
                    keyPoints = listOf(
                        "Urgency, fear, authority, curiosity and greed are the classic triggers.",
                        "If a message makes you feel rushed, that is itself a warning sign.",
                    ),
                ),
                Lesson(
                    title = "Types of phishing",
                    paragraphs = listOf(
                        "Mass phishing: the same generic message sent to millions of people, hoping a small percentage bite.",
                        "Spear phishing: a targeted message written for one person or a small group, often using details from social media or a company website.",
                        "Whaling: spear phishing aimed at executives or people with access to money and sensitive data.",
                        "Business Email Compromise (BEC): the attacker pretends to be (or takes over the mailbox of) a colleague or supplier and asks for payments or bank-detail changes.",
                        "Clone phishing: a real email you received earlier is copied, but the link or attachment is swapped for a malicious one.",
                    ),
                    keyPoints = listOf(
                        "The more personal the message, the more dangerous it is.",
                        "Requests to change payment details are a top BEC red flag.",
                    ),
                ),
                Lesson(
                    title = "What happens after you bite",
                    paragraphs = listOf(
                        "Stolen passwords are tried on many other sites (\"credential stuffing\"), so one leaked password can unlock several accounts if you reuse it.",
                        "A taken-over email account is used to reset passwords for your other services and to phish your contacts — who trust messages coming from you.",
                        "Malware can log your keystrokes, steal browser sessions, or encrypt your files for ransom.",
                        "This is why a single click can matter so much — and why reporting quickly helps limit the damage.",
                    ),
                    keyPoints = listOf(
                        "One stolen password can open many doors if passwords are reused.",
                        "Your email account is the master key to most of your online life.",
                    ),
                ),
            ),
            quiz = listOf(
                Question(
                    "What is the main goal of a phishing message?",
                    listOf(
                        "To advertise a product",
                        "To trick you into giving information, clicking a link, or opening a file",
                        "To test your internet speed",
                        "To update your device automatically",
                    ),
                    1,
                    "Phishing always combines impersonation with a request that benefits the attacker.",
                ),
                Question(
                    "\"Your account will be suspended in 2 hours unless you verify now.\" Which trick is this?",
                    listOf("Curiosity", "Greed", "Urgency", "Humour"),
                    2,
                    "Tight deadlines push you to act before you think. Real companies rarely give you only hours.",
                ),
                Question(
                    "A message mentions your real job title and your manager's name. What type of phishing is this most likely?",
                    listOf("Mass phishing", "Spear phishing", "Spam", "Clone phishing"),
                    1,
                    "Spear phishing is personalised using details the attacker researched about you.",
                ),
                Question(
                    "A 'supplier' emails asking you to send future payments to a new bank account. This is a classic sign of…",
                    listOf("Business Email Compromise", "A software update", "A newsletter", "Whaling the CEO"),
                    0,
                    "Changing payment details by email is the hallmark of BEC. Always verify by phone using a number you already have.",
                ),
                Question(
                    "Why is reusing the same password on many sites dangerous?",
                    listOf(
                        "It makes the password easier to forget",
                        "Websites charge more for reused passwords",
                        "If one site's password is phished, attackers can log into the others",
                        "It isn't dangerous",
                    ),
                    2,
                    "Attackers automatically try stolen credentials on many services (credential stuffing).",
                ),
            ),
        ),

        // ─────────────────────────────────────────────── PART 2
        CourseModule(
            id = 2,
            title = "Spotting a phishing email",
            summary = "Sender addresses, greetings, tone and the details that give a fake away.",
            lessons = listOf(
                Lesson(
                    title = "Check who really sent it",
                    paragraphs = listOf(
                        "Every email has a display name and an address. The display name can be anything — an attacker can call themselves \"PayPal Support\" from any address.",
                        "Tap or hover on the sender to see the full address. Look at the part after the @. Is it the company's real domain, or something like paypal-support-team.com, or a free address like @gmail.com?",
                        "Also be wary if the Reply-To address is different from the From address — replies would go to the attacker, not the company.",
                    ),
                    keyPoints = listOf(
                        "Display names are easy to fake; addresses are what matter.",
                        "Big companies don't send official mail from free webmail accounts.",
                    ),
                    example = "From: PayPal Security <security@paypa1-alerts.com>\nReply-To: verify.team@gmail.com\nSubject: Unusual activity on your account",
                ),
                Lesson(
                    title = "Read the message like a detective",
                    paragraphs = listOf(
                        "Generic greetings (\"Dear Customer\", \"Dear User\") suggest the sender doesn't actually know who you are. Your bank knows your name.",
                        "Spelling and grammar mistakes used to be a strong clue. With AI writing tools, many phishing emails are now well written — so don't treat good grammar as proof of safety.",
                        "Look at what the email asks you to do. Legitimate companies will not ask you to send your password, PIN or one-time code by email or message.",
                    ),
                    keyPoints = listOf(
                        "No legitimate service asks for your password or 2FA code.",
                        "Perfect grammar does NOT mean an email is safe.",
                    ),
                ),
                Lesson(
                    title = "The red flag checklist",
                    paragraphs = listOf(
                        "Before acting on any email that asks for something, run through this list. One flag isn't proof, but several together almost always mean phishing.",
                    ),
                    keyPoints = listOf(
                        "Unexpected — you weren't waiting for it.",
                        "Urgent or threatening tone.",
                        "Sender address doesn't match the company.",
                        "Generic greeting.",
                        "Asks for credentials, payment, gift cards or codes.",
                        "Links that point somewhere different from what they say.",
                        "Unexpected attachment, especially an invoice or 'document'.",
                        "Too good to be true — prizes, refunds, inheritances.",
                    ),
                ),
                Lesson(
                    title = "Example: dissecting a fake",
                    paragraphs = listOf(
                        "Look at the email below and count the red flags before reading on.",
                        "Flags: the domain amaz0n-billing.net uses a zero instead of an 'o' and isn't Amazon's real domain; generic greeting; a 24-hour threat; a request to 'confirm' card details; a link whose real target is an unrelated site.",
                    ),
                    example = "From: Amazon <no-reply@amaz0n-billing.net>\nSubject: Your order has been suspended\n\nDear Customer,\n\nWe could not process payment for your recent order.\nYour account will be locked within 24 hours unless you\nconfirm your card details here:\n\nhttps://amazon.com.account-verify.co/login\n\nAmazon Team",
                ),
            ),
            quiz = listOf(
                Question(
                    "Which part of the sender info is hardest for an attacker to fake?",
                    listOf("The display name", "The domain after the @ (when checked properly)", "The profile picture", "The signature"),
                    1,
                    "Anyone can set any display name or signature. The real domain is what you should check.",
                ),
                Question(
                    "An email from 'Your Bank' starts with 'Dear Valued Customer'. What does that suggest?",
                    listOf(
                        "The bank is being polite",
                        "The sender may not know your name — a possible mass phishing email",
                        "It is definitely real",
                        "Nothing at all",
                    ),
                    1,
                    "Your real bank knows your name. Generic greetings are typical of mass phishing.",
                ),
                Question(
                    "An email is perfectly written with no mistakes. Is it safe?",
                    listOf(
                        "Yes, scammers always make mistakes",
                        "Not necessarily — AI tools let attackers write flawless emails",
                        "Yes, if it has a logo",
                        "Only if it's short",
                    ),
                    1,
                    "Grammar is no longer a reliable signal. Focus on the sender, links and the request.",
                ),
                Question(
                    "The From address is support@yourbank.com but Reply-To is yourbank.help@outlook.com. What's going on?",
                    listOf(
                        "Normal — banks use Outlook",
                        "Replies would go to an outside address controlled by someone else — suspicious",
                        "It's a newsletter",
                        "The bank is changing its domain",
                    ),
                    1,
                    "A mismatched Reply-To sends your answer to the attacker even if the From looks right.",
                ),
                Question(
                    "Which request should NEVER come from a legitimate company by email?",
                    listOf(
                        "Read our new privacy policy",
                        "Rate your recent purchase",
                        "Reply with your password and the code we just texted you",
                        "Your order has shipped",
                    ),
                    2,
                    "No real service will ever ask you to send your password or one-time code.",
                ),
            ),
        ),

        // ─────────────────────────────────────────────── PART 3
        CourseModule(
            id = 3,
            title = "Links & fake websites",
            summary = "How to read a URL and recognise lookalike login pages.",
            lessons = listOf(
                Lesson(
                    title = "Reading a URL",
                    paragraphs = listOf(
                        "A web address has a structure: https://sub.example.com/path?query. The part that tells you who owns the site is the registered domain — the bit immediately before the first single slash, ending with the top-level domain (example.com).",
                        "Read domains right to left, starting from the end. In https://paypal.com.secure-login.io/account the real domain is secure-login.io — 'paypal.com' is just a subdomain the attacker chose.",
                        "Anything after the first single '/' (the path) can say anything and doesn't tell you who owns the site.",
                    ),
                    keyPoints = listOf(
                        "Find the domain right before the first '/' and read it from the right.",
                        "Brand names in subdomains or paths prove nothing.",
                    ),
                    example = "https://www.paypal.com/signin          → owner: paypal.com ✓\nhttps://paypal.com.secure-login.io/    → owner: secure-login.io ✗\nhttps://secure.com/paypal.com/login    → owner: secure.com ✗",
                ),
                Lesson(
                    title = "Lookalike tricks",
                    paragraphs = listOf(
                        "Typosquatting: domains with tiny spelling changes — gooogle.com, micros0ft.com, rnicrosoft.com (r + n looks like m).",
                        "Homoglyphs: letters from other alphabets that look identical, like a Cyrillic 'а' in аpple.com. Browsers often show these as 'xn--…' (punycode) — a strong warning sign.",
                        "Combosquatting: real brand + extra word — netflix-billing.com, apple-id-support.net.",
                        "Odd top-level domains: .zip, .top, .xyz, .click and similar are cheap and heavily abused, although not every site using them is bad.",
                    ),
                    keyPoints = listOf(
                        "xn-- in a domain = it contains non-Latin lookalike characters.",
                        "Brand + extra word in the domain is very often fake.",
                    ),
                ),
                Lesson(
                    title = "Links that lie",
                    paragraphs = listOf(
                        "In an email, the text you see and the place a link goes can be totally different. 'https://mybank.com' can be blue clickable text that actually leads to an attacker's site.",
                        "On a computer, hover over the link and look at the status bar. On a phone, long-press the link to preview the real address — don't tap.",
                        "URL shorteners (bit.ly, tinyurl and similar) hide the destination. Raw IP addresses (http://185.23.44.10/login) instead of a name are a big red flag.",
                        "Also watch for an '@' inside a URL: in http://bank.com@evil.site/ the browser goes to evil.site.",
                    ),
                    keyPoints = listOf(
                        "Hover (desktop) or long-press (phone) before you click.",
                        "IP addresses, shorteners and '@' in links deserve suspicion.",
                    ),
                ),
                Lesson(
                    title = "The padlock myth and safe habits",
                    paragraphs = listOf(
                        "The padlock / https only means the connection is encrypted. It does NOT mean the site is legitimate — most phishing sites now use https too.",
                        "The safest habit: don't log in through links in emails at all. Open the app, or type the address yourself / use a bookmark.",
                        "Password managers help here: they only auto-fill on the real domain. If your manager doesn't offer to fill in your password, stop and check the address.",
                    ),
                    keyPoints = listOf(
                        "Padlock = encrypted, not trustworthy.",
                        "Go to sites directly instead of clicking email links.",
                        "A password manager that won't autofill is a warning.",
                    ),
                ),
            ),
            quiz = listOf(
                Question(
                    "Who owns this site? https://login.microsoft.com.account-check.net/verify",
                    listOf("microsoft.com", "login.microsoft.com", "account-check.net", "verify"),
                    2,
                    "Read from the right, before the first '/': the registered domain is account-check.net.",
                ),
                Question(
                    "Which of these is the real domain of the address https://www.apple.com/support?",
                    listOf("apple.com", "www", "support", "apple.com/support"),
                    0,
                    "The registered domain is apple.com — www is a subdomain and /support is a path.",
                ),
                Question(
                    "A site has a padlock icon and uses https. What does that prove?",
                    listOf(
                        "The site is owned by a real company",
                        "The site is safe to enter passwords on",
                        "Only that the connection is encrypted",
                        "That the government checked it",
                    ),
                    2,
                    "Anyone can get a free certificate. Phishing sites use https all the time.",
                ),
                Question(
                    "You see 'xn--pple-43d.com' in your address bar. What does it mean?",
                    listOf(
                        "A secure Apple server",
                        "The domain contains lookalike characters from another alphabet",
                        "A broken website",
                        "A shortened link",
                    ),
                    1,
                    "xn-- means punycode — the domain uses non-Latin characters that may imitate a brand.",
                ),
                Question(
                    "What's the safest way to log in after getting an email saying 'verify your account'?",
                    listOf(
                        "Click the link but check the padlock",
                        "Reply to ask if it's real",
                        "Open the official app or type the address yourself",
                        "Forward it to friends to check",
                    ),
                    2,
                    "Going to the site independently removes the attacker's link from the equation entirely.",
                ),
            ),
        ),

        // ─────────────────────────────────────────────── PART 4
        CourseModule(
            id = 4,
            title = "Attachments & malware",
            summary = "Dangerous file types, macro tricks and fake invoices.",
            lessons = listOf(
                Lesson(
                    title = "Why attachments are dangerous",
                    paragraphs = listOf(
                        "Some files can run code when you open them. Attackers disguise these as invoices, delivery notes, CVs, scanned documents or voicemail messages.",
                        "Once the code runs it can install malware that steals passwords, gives the attacker remote control, or encrypts your files for ransom (ransomware).",
                    ),
                    keyPoints = listOf(
                        "Unexpected attachment + urgency = stop.",
                        "Invoices and 'scanned documents' are the most common disguises.",
                    ),
                ),
                Lesson(
                    title = "Know the risky file types",
                    paragraphs = listOf(
                        "High risk — programs and scripts: .exe, .scr, .bat, .cmd, .com, .msi, .js, .vbs, .ps1, .jar, .apk (on Android), .lnk shortcuts.",
                        "Containers that hide those programs: .zip, .rar, .7z, .iso, .img — especially password-protected ones, which security scanners can't look inside.",
                        "Office documents with macros: .docm, .xlsm. A document that asks you to 'Enable Content' or 'Enable Editing' to view it is a classic trap.",
                        "HTML attachments (.html, .htm) often open a fake login page right on your device.",
                    ),
                    keyPoints = listOf(
                        "Never click 'Enable Content' on a document from an email you didn't expect.",
                        "Password-protected zips in unexpected emails are a big warning sign.",
                    ),
                ),
                Lesson(
                    title = "Double extensions and disguises",
                    paragraphs = listOf(
                        "Windows and some phones hide file extensions by default. A file called invoice.pdf.exe may show up as just 'invoice.pdf' — but it's a program.",
                        "Attackers also give programs a PDF or Word icon to complete the disguise.",
                        "Turn on 'show file extensions' on your computer, and look at the very last extension — that's the one that counts.",
                    ),
                    keyPoints = listOf(
                        "Only the LAST extension matters: report.pdf.exe is an .exe.",
                    ),
                    example = "Attachments:\n  Invoice_2026_0847.pdf.exe     ← program in disguise\n  Scan_from_printer.html        ← fake login page\n  Payment_details.zip (pw: 1234) ← hides contents from scanners",
                ),
                Lesson(
                    title = "Safe habits for files",
                    paragraphs = listOf(
                        "If you weren't expecting a file, confirm with the sender through a different channel (call them, or message them separately) before opening it.",
                        "Preview documents in your browser or cloud storage rather than downloading them, when possible.",
                        "Keep your phone, computer and apps updated — many malware attacks rely on old, already-fixed vulnerabilities.",
                        "On Android, only install apps from Google Play (or other trusted stores) and never from a link in a message.",
                    ),
                    keyPoints = listOf(
                        "Verify through a second channel.",
                        "Update everything.",
                        "Never install apps from links in messages.",
                    ),
                ),
            ),
            quiz = listOf(
                Question(
                    "What is the real type of the file 'Salary_Review.docx.exe'?",
                    listOf("A Word document", "A program (executable)", "An image", "A zip archive"),
                    1,
                    "Only the last extension counts. This is a program pretending to be a Word file.",
                ),
                Question(
                    "A document says: 'This file is protected. Click Enable Content to view it.' What should you do?",
                    listOf(
                        "Click Enable Content",
                        "Close it — this is a common way to run malicious macros",
                        "Forward it to a colleague",
                        "Print it",
                    ),
                    1,
                    "Enabling content lets macros run. Legitimate documents rarely need this.",
                ),
                Question(
                    "Why do attackers like sending password-protected zip files?",
                    listOf(
                        "They are smaller",
                        "Security scanners can't look inside them",
                        "They open faster",
                        "They look professional",
                    ),
                    1,
                    "Encryption hides the malicious file from email security scanning.",
                ),
                Question(
                    "You receive an unexpected invoice from a known supplier. What's the best first step?",
                    listOf(
                        "Open it to see the amount",
                        "Pay it quickly to avoid late fees",
                        "Contact the supplier using a phone number or address you already have",
                        "Reply to the email asking if it's real",
                    ),
                    2,
                    "Replying goes to the attacker if the account is compromised. Use a channel you already trust.",
                ),
                Question(
                    "A text message asks you to install a 'delivery tracking app' from a link. You should…",
                    listOf(
                        "Install it, it's convenient",
                        "Not install it — only use official app stores",
                        "Install it but deny permissions",
                        "Install it on a friend's phone",
                    ),
                    1,
                    "Apps from message links are a very common way to infect Android phones.",
                ),
            ),
        ),

        // ─────────────────────────────────────────────── PART 5
        CourseModule(
            id = 5,
            title = "Beyond email",
            summary = "SMS, phone calls, social media, QR codes and AI-powered scams.",
            lessons = listOf(
                Lesson(
                    title = "Smishing (SMS phishing)",
                    paragraphs = listOf(
                        "Smishing uses text messages and messaging apps. Common themes: missed parcel deliveries, unpaid road tolls, bank alerts, tax refunds, and 'Hi mum, I lost my phone, this is my new number'.",
                        "Phones show less information than email — short links, no full sender address — which makes smishing especially effective.",
                        "Banks and delivery companies may text you, but they won't ask you to log in or pay through a link in that text.",
                    ),
                    keyPoints = listOf(
                        "'New number' messages from family: call the old number to check.",
                        "Delivery fee texts are almost always scams.",
                    ),
                    example = "SMS from +44 7911 ******\nYour parcel could not be delivered due to an unpaid\nfee of 1.99. Pay now: https://dpd-redelivery.top/x7k",
                ),
                Lesson(
                    title = "Vishing (voice phishing)",
                    paragraphs = listOf(
                        "Vishing happens over the phone. The caller may claim to be your bank's fraud team, tech support, the police or the tax office.",
                        "Caller ID can be spoofed — a call can display your bank's real number and still be fake.",
                        "Typical goal: get you to read out a one-time code, move money to a 'safe account', or install remote-access software. A real bank will never ask for any of these.",
                        "If in doubt, hang up and call back using the number on the back of your card.",
                    ),
                    keyPoints = listOf(
                        "Caller ID can lie.",
                        "There is no such thing as a 'safe account'.",
                        "Hang up and call back on a number you find yourself.",
                    ),
                ),
                Lesson(
                    title = "Social media, QR codes and more",
                    paragraphs = listOf(
                        "Social media: fake support accounts reply to your complaints, fake giveaways ask for your login, and hacked friends' accounts send you links or ask to 'vote' for them.",
                        "Quishing (QR phishing): fake QR codes stuck over real ones on parking meters, restaurant tables or in emails. The phone camera shows the URL — check it before opening.",
                        "MFA fatigue: an attacker who has your password spams you with login approval requests, hoping you'll tap 'Approve' to make them stop. Never approve a login you didn't start.",
                    ),
                    keyPoints = listOf(
                        "Check the URL a QR code opens before visiting.",
                        "Never approve a login request you didn't trigger.",
                    ),
                ),
                Lesson(
                    title = "AI-powered scams",
                    paragraphs = listOf(
                        "Attackers use AI to write convincing messages in any language, and to clone voices from short audio clips found online.",
                        "A call that sounds exactly like a family member or your boss asking for urgent money can be a deepfake.",
                        "Agree on a family 'safe word', and for any money request, verify through a second channel — even if the voice or video looks real.",
                    ),
                    keyPoints = listOf(
                        "A familiar voice is no longer proof of identity.",
                        "Verify money requests through a second channel, always.",
                    ),
                ),
            ),
            quiz = listOf(
                Question(
                    "What is 'smishing'?",
                    listOf("Phishing by SMS / messaging", "Phishing by phone call", "A type of malware", "Phishing on social media only"),
                    0,
                    "Smishing = SMS + phishing.",
                ),
                Question(
                    "Your phone shows your bank's real number calling. The caller asks for the code you just received. You should…",
                    listOf(
                        "Read the code — the number is correct",
                        "Hang up; caller ID can be spoofed and banks never ask for codes",
                        "Give half the code",
                        "Ask them to email you instead",
                    ),
                    1,
                    "Caller ID can be faked, and no bank will ever ask you to read out a one-time code.",
                ),
                Question(
                    "You get 15 login approval prompts in a row that you didn't start. What's happening?",
                    listOf(
                        "An app update",
                        "Someone has your password and is trying MFA fatigue — deny and change your password",
                        "Normal background syncing",
                        "Your phone is broken",
                    ),
                    1,
                    "Deny every prompt, then change your password: the attacker already knows it.",
                ),
                Question(
                    "Before opening a link from a QR code on a parking meter you should…",
                    listOf(
                        "Nothing, QR codes are always safe",
                        "Check the URL preview and ideally use the official parking app instead",
                        "Scan it twice",
                        "Turn off Wi-Fi",
                    ),
                    1,
                    "Fake QR stickers over real ones are a common trick. Check the domain or use the official app.",
                ),
                Question(
                    "A voice message that sounds exactly like your brother asks you to urgently send money. Best response?",
                    listOf(
                        "Send it — it's his voice",
                        "Call him back on his usual number, or ask your family safe word",
                        "Reply with your bank details",
                        "Send half now, half later",
                    ),
                    1,
                    "Voices can be cloned with AI. Always verify money requests through another channel.",
                ),
            ),
        ),

        // ─────────────────────────────────────────────── PART 6
        CourseModule(
            id = 6,
            title = "Protect yourself & respond",
            summary = "Strong defences, and exactly what to do if you clicked.",
            lessons = listOf(
                Lesson(
                    title = "Build your defences",
                    paragraphs = listOf(
                        "Use a password manager so every account gets a unique, random password — a phished password then only exposes one site.",
                        "Turn on multi-factor authentication (MFA) everywhere, starting with email and banking. Passkeys and hardware security keys are the strongest options because they can't be used on a fake site.",
                        "Keep devices and apps updated, and use the built-in spam / phishing protection in your email app and browser.",
                    ),
                    keyPoints = listOf(
                        "Unique passwords + MFA stop most account takeovers.",
                        "Passkeys are phishing-resistant by design.",
                    ),
                ),
                Lesson(
                    title = "The STOP – THINK – CHECK routine",
                    paragraphs = listOf(
                        "STOP: When a message asks you to click, pay, log in, or share something, pause. Urgency is the attacker's best tool.",
                        "THINK: Was I expecting this? Does the request make sense? Would this company really ask this way?",
                        "CHECK: Verify through a channel you already trust — the official app, a typed-in website, a known phone number. Never use contact details from the suspicious message itself.",
                    ),
                    keyPoints = listOf(
                        "STOP → THINK → CHECK, every time money, passwords or codes are involved.",
                    ),
                ),
                Lesson(
                    title = "I clicked — now what?",
                    paragraphs = listOf(
                        "Don't panic, and don't hide it. Acting fast matters more than anything.",
                        "If you entered a password: change it immediately on the real site, plus anywhere you reused it. Turn on MFA and sign out other sessions.",
                        "If you entered card or bank details: call your bank using the number on your card and ask them to block the card.",
                        "If you opened a file or installed something: disconnect from the internet, run a security scan, and at work, contact IT right away.",
                        "If you're at work, always report it — security teams would much rather hear early than discover it later.",
                    ),
                    keyPoints = listOf(
                        "Change passwords → call bank → disconnect → report.",
                        "Reporting early is never 'too much'.",
                    ),
                ),
                Lesson(
                    title = "Report it",
                    paragraphs = listOf(
                        "Use the 'Report phishing' or 'Report spam' button in your email app — it trains filters to protect everyone.",
                        "Forward phishing SMS to your mobile operator's spam reporting number if your country has one (for example 7726 in the UK and the US).",
                        "Report impersonation to the company being faked; most have an abuse or phishing address on their website.",
                        "If you lost money, report it to your bank and to the police or your national cybercrime / consumer protection agency.",
                    ),
                    keyPoints = listOf(
                        "Reporting protects other people too.",
                    ),
                ),
            ),
            quiz = listOf(
                Question(
                    "Why does a password manager help against phishing?",
                    listOf(
                        "It blocks all emails",
                        "It only auto-fills on the real website's domain, and gives each site a unique password",
                        "It makes passwords shorter",
                        "It deletes suspicious messages",
                    ),
                    1,
                    "No autofill on a 'familiar' page is a strong hint you're on a fake site.",
                ),
                Question(
                    "Which login method is most resistant to phishing?",
                    listOf("SMS codes", "Security questions", "Passkeys / hardware security keys", "Using the same strong password everywhere"),
                    2,
                    "Passkeys are bound to the real website, so a fake site can't use them.",
                ),
                Question(
                    "You typed your email password into a fake page. What should you do FIRST?",
                    listOf(
                        "Wait and see if anything happens",
                        "Change the password on the real site (and anywhere you reused it) right away",
                        "Delete the phishing email and forget it",
                        "Reply to the attacker",
                    ),
                    1,
                    "Speed matters — change it before the attacker uses it, then enable MFA.",
                ),
                Question(
                    "In STOP – THINK – CHECK, how should you CHECK a suspicious request?",
                    listOf(
                        "Call the phone number in the message",
                        "Reply to the email",
                        "Use a contact method you already trust, like the official app or a known number",
                        "Ask in a public forum",
                    ),
                    2,
                    "Contact details in the suspicious message are controlled by the attacker.",
                ),
                Question(
                    "At work you clicked a link and now feel unsure. What should you do?",
                    listOf(
                        "Say nothing to avoid embarrassment",
                        "Report it to IT / security immediately",
                        "Restart your computer and forget it",
                        "Send the link to colleagues to test",
                    ),
                    1,
                    "Fast reporting lets the security team contain damage. Good teams thank you for it.",
                ),
            ),
        ),
    )

    /** Percentage needed to "pass" a part's quiz. */
    const val PASS_PERCENT = 80
}
