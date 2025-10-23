# NexusBank-AI — Video Recording Plan (3–5 minute demo)

Objective: Produce a concise, polished demo video for judges showcasing the unique features.

Duration: 4 minutes

Equipment & Software
- Screen recorder (OBS Studio recommended)
- Microphone (USB or headset)
- MySQL Workbench (for DB inspection)
- Java JDK 8+ installed
- Browser for GitHub and README

Recording sequence
1. Intro slide — 8s: Title + tagline
2. Live app: Login screen — 20s
   - Show application window, login as demo user
3. Dashboard & Rewards — 40s
   - Show balance, points, badges area
4. Fraud detection demo — 50s
   - Run `TestFraud` utility and show `fraud_alerts` table in MySQL Workbench
5. Feature highlight overlay — 20s
   - Quick 5 bullet points (Biometrics, Panic PIN, Virtual Cards, Savings Goals, Analytics)
6. Technical deep-dive snapshot — 20s
   - Show code structure (IDE) and DatabaseManager.java auto-create snippet
7. Closing & CTA — 10s
   - Request questions and link to GitHub

Recording tips
- Use two monitors if possible: app on one, recorder controls on the other
- Use keyboard shortcuts to switch windows quickly
- Zoom into the code or DB rows when explaining
- Keep terminal font size large for readability
- Use simulated demo accounts to avoid exposing real data

Rendering & Upload
- Export: MP4 (H.264), 1920x1080, 30fps
- Upload to YouTube (unlisted) and link in your presentation slide deck

Editing
- Trim pauses and filler words
- Add captions for key actions
- Add a small corner webcam feed to personalize demo (optional)

Checklist before recording
- [ ] All code compiled and build directory up-to-date
- [ ] Demo user and data seeded
- [ ] MySQL Workbench connected to `nexusbank_ai`
- [ ] OBS hotkeys configured
- [ ] Microphone tested

---

Commands you'll run (copy-paste)
```powershell
# compile all
javac -source 8 -target 8 -d build -cp "lib/*;src" src/com/nexusbank/**/*.java

# run login screen
java -cp "lib/*;build" com.nexusbank.ui.LoginScreen

# run fraud demo in parallel
java -cp "lib/*;build" com.nexusbank.ai.TestFraud
```

---

Notes
- If UI is not fully implemented, rely on Test utilities and DB snapshots for proofs.
- Keep the voiceover tight — describe what the system does, not how to use every button.

Good luck — let me know if you want me to generate a storyboard or slide images for the video intro.
