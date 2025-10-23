# NexusBank-AI — Demo Script (3–5 minutes)

Goal: Show the judges the unique, high-impact features quickly and confidently.

Total length: 4 minutes (recommended)

---

0:00–0:20 — Opening (20s)
- Slide: Title slide (NexusBank-AI — Banking Reimagined)
- One-line pitch: "NexusBank-AI is an AI-powered, gamified ATM simulation that makes banking secure, engaging and intelligent."

0:20–0:40 — Problem & Solution (20s)
- Problem: Traditional ATMs are static, lack engagement and modern security.
- Solution: NexusBank-AI brings AI fraud detection, gamification, virtual cards and emergency security.

0:40–1:30 — Live demo: Signup & Login (50s)
- Show Signup flow (if not implemented, briefly describe and show SignUp placeholder)
- Show Login using card + PIN
- Show daily login bonus popup (demonstrate `RewardsEngine.awardDailyLoginBonus()` result)

1:30–2:20 — Live demo: Dashboard & Quick Actions (50s)
- Show Dashboard: balance, quick actions (Deposit, Withdraw)
- Show Rewards preview: points, badges (if UI not ready, show RewardsPanel placeholder and explain)

2:20–3:00 — Live demo: Fraud Detection (40s)
- Explain the rule-based engine (large withdrawal, rapid transactions, overdraft)
- Run `TestFraud` demo (pre-seeded) and show inserted `fraud_alerts` row (open DB viewer or show console output)
- Emphasize automatic alert & potential lock/notification

3:00–3:30 — Quick walk-through: Features list (30s)
- Biometric authentication (planned/implemented)
- Panic PIN & Emergency SOS
- Virtual cards & spending limits
- Smart savings goals & auto-round-up
- Gamification: badges, leaderboards, redemption

3:30–3:50 — Technical highlights (20s)
- Architecture: MVC, DatabaseManager (auto DB creation), modular AI package
- Security: hashed biometric storage, panic PIN, prepared statements

3:50–4:00 — Closing (10s)
- Call to action: "NexusBank-AI demonstrates a new era of ATM systems — secure, intelligent, and delightful."
- Invite questions

---

Demo tips
- Use full-screen terminal and app windows for visibility
- If the UI is incomplete, fall back to running small utilities (`TestFraud`) and show console output and database table rows in MySQL Workbench
- Keep interactions smooth — prepare keyboard shortcuts to speed up repetitive steps
- Record audio narration separately if needed

Checklist before recording
- [ ] Create demo user in DB
- [ ] Seed demo transactions for fraud test and rewards
- [ ] Ensure `lib/*` jars are in `lib/` and `build/` is compiled
- [ ] Set screen resolution to 1920x1080
- [ ] Disable system notifications

---

Scripts to run during demo
- Compile and run the app (Login)
```powershell
javac -source 8 -target 8 -d build -cp "lib/*;src" src/com/nexusbank/**/*.java
java -cp "lib/*;build" com.nexusbank.ui.LoginScreen
```

- Run fraud demo (in separate terminal)
```powershell
javac -source 8 -target 8 -d build -cp "lib/*;src" src/com/nexusbank/ai/TestFraud.java
java -cp "lib/*;build" com.nexusbank.ai.TestFraud
```

- Quick DB checks (MySQL CLI)
```sql
USE nexusbank_ai;
SELECT * FROM fraud_alerts ORDER BY timestamp DESC LIMIT 5;
SELECT * FROM rewards WHERE user_id = <demo_user_id>;
```

---

Prepared answers to expected questions
- Q: "How does fraud detection scale?"
  A: Rule-based engine provides immediate protection; we can plug in a trained ML model (spending predictor) that scores transactions in real-time and updates thresholds.

- Q: "How secure is biometric data?"
  A: We store biometric hashes, not raw images; we use SHA-256/AES for storage and transmission. Biometric auth is optional and can be revoked.

- Q: "How do you prevent gamification abuse?"
  A: Rewards engines are backed by server-side validation and rate limits; suspicious award patterns generate fraud alerts for review.

- Q: "Can this be deployed in production?"
  A: The architecture supports scaling — DB normalization, indexes, caching, and microservice decomposition when needed.
