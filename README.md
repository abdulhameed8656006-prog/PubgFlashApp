# PUBG Flash Launch

An Android app that counts down to the launch of PUBG Mobile Flash and alerts you when it is out.

## What it does
- Shows days, hours, minutes and seconds left until launch.
- Checks the internet for the latest launch date and notes (when you open the app, and about twice a day in the background).
- Sends a notification at launch time. Tap it to confirm.
- If you have not tapped it by 2:30 PM on launch day, an alarm rings with a voice saying "PUBG Flash is out". It rings until you press Stop.
- Has a button to open the game on Google Play:
  https://play.google.com/store/apps/details?id=com.tencent.igfit
- Has a "Test the alarm now" button so you can check the alarm works.

## How to install
1. Open the Releases section of this repo.
2. Download the newest `PUBG-Flash.apk`.
3. Tap it to install. If Android asks, allow installs from your browser or Files app.
4. Open the app and allow notifications.
5. Go to Settings > Apps > PUBG Flash Launch > Battery and choose Unrestricted, so the alarm is not delayed.

## How to change the launch date
The app reads `launch.json` from the PubgFlash web page repo:
https://abdulhameed8656006-prog.github.io/PubgFlash/launch.json

Edit that file on GitHub and change `launchDate` (format `2026-11-13T00:00:00`, in your phone's local time). Open the app to load the new date and reschedule the alerts.

## How to rebuild
Every time you change a file in this repo, GitHub builds a new APK automatically (see the Actions tab). The finished file appears under Releases.

## Notes
- The launch date is not officially confirmed yet. The app starts with November 13, 2026.
- The alarm and notification are real Android alarms. They work when the app is closed or the phone is locked.
- Phones that restrict background apps may delay the alarm. Keep battery set to Unrestricted for this app.
