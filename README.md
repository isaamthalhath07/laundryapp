# Laundry Tally

Count the clothes you hand over for laundry and get the total number of items.

Items tracked: **shirt, t-shirt, blazer, suit, pants, jeans, pyjama, underwear, vest, socks,
towel, bedsheet, pillow cover, blanket** — grouped by upper wear, lower wear, night wear,
innerwear and bath & bedding.

Tap `+` / `−` to count, or tap the number to type it in. The running **total** sits at the
bottom of the screen at all times. Counts (and the optional bag label) are saved on the device,
so closing the app does not lose the tally. **Share list** sends a plain-text summary through
WhatsApp, SMS, email or anything else on the phone:

```
Laundry list - Isaam
27 Sep 2026

Shirt: 4
Jeans: 2
Socks: 3
Towel: 1

Total items: 10
```

There are two builds in this repository, and both do the same thing.

## 1. Web app (easiest — nothing to sideload)

`web/` is an offline-capable PWA. Once it is deployed you open the link on the phone, tap the
browser menu and choose **Add to Home screen** / **Install app**. It then behaves like a normal
app: its own icon, full screen, no address bar, and it works without internet.

To publish it free on GitHub Pages:

1. Merge this branch into the repository's default branch (`main`).
2. In GitHub go to **Settings → Pages** and set **Source** to **GitHub Actions**.
3. The *Deploy web app to GitHub Pages* workflow publishes `web/` and prints the URL
   (`https://isaamthalhath07.github.io/laundryapp/`). Share that link with anyone who needs it.

To try it without deploying anything, open `web/index.html` in any browser.

## 2. Native Android app (APK)

`android/` is a plain Java + Android Views app (no framework to install, minSdk 24 → Android 7.0
and newer). GitHub Actions builds the APK on every push — the container this repository was
written in has no Android SDK, so the APK is produced by CI rather than committed here.

**Getting the APK:**

- *Any push:* open the **Actions** tab → latest *Build Android APK* run → **Artifacts** →
  `laundry-tally-apk`.
- *A proper download link:* a release gives a permanent, one-tap download URL at
  `https://github.com/isaamthalhath07/laundryapp/releases/latest`. Either push a tag:

  ```bash
  git tag v1.0 && git push origin v1.0
  ```

  or run the workflow by hand — **Actions → Build Android APK → Run workflow** — and fill in
  **release_tag** (for example `v1.0`). The workflow then creates that tag and release itself
  and attaches the APK.

**Installing it:** open the downloaded `laundry-tally.apk` on the phone and allow
*Install unknown apps* for your browser or file manager when Android asks. The APK is signed
with the standard debug key, which is fine for installing it yourself; it is not a Play Store
build.

**Building it locally** (needs Android Studio or the Android SDK):

```bash
cd android
./gradlew assembleDebug      # app/build/outputs/apk/debug/app-debug.apk
./gradlew installDebug       # build and install onto a connected phone
```

## Repository layout

```
web/                     offline web app (PWA)
  index.html             the whole app: markup, styles, counting logic
  manifest.webmanifest   name, icons and colours used when installed
  sw.js                  service worker, so it works with no internet
android/                 native Android app
  app/src/main/java/com/isaam/laundrytally/
    Catalog.java         the item list and its groups
    Item.java            one countable item
    MainActivity.java    counting, saving, totalling and sharing
  app/src/main/res/      layouts, colours, strings, launcher icons
.github/workflows/
  android.yml            builds the APK, attaches it to tagged releases
  pages.yml              publishes web/ to GitHub Pages
```

## Changing the item list

Edit one list in each build and everything else (rows, totals, the shared summary) follows:

- Web: the `ITEMS` array at the top of the script in `web/index.html`.
- Android: the `GROUPS` list in `android/app/src/main/java/com/isaam/laundrytally/Catalog.java`.
