# DevxyzMusic

Local-only Android music player ni Jepong. Design lang ang hiniram sa
Square player screenshot — HINDI ito Spotify o YouTube Music client.

## Ano ito, at ano ang hindi

- Galing sa phone ang songs: MediaStore audio + audio files na pipiliin
  mismo ng user (Storage Access Framework).
- Walang internet permission, walang login, walang streaming, walang
  "Switch to video", walang account, walang online catalogue.
- Walang Square code, walang librespot, walang Innertube. Sariling
  Kotlin + Jetpack Compose code ito, at sariling placeholder art lang
  ang ginagamit kapag walang album art ang file (hindi Bruno Mars art).

## Features (v1)

- Library: Songs + Favorites, search sa title/artist/album
- Player screen gaya ng screenshot: top bar, malaking album art,
  title pill (share / queue / heart), seekable progress + oras,
  shuffle / prev / malaking play-pause / next / repeat (off/all/one)
- Bottom pill: bumalik sa library, up-next queue, playback speed
  (0.75x / 1x / 1.25x / 1.5x), detalye ng kanta
- Background playback via foreground service + notification controls
- Favorites na naka-save sa phone (SharedPreferences)
- Pumili ng audio file kahit walang MediaStore permission

## Build

- Package: `com.jepongdevxyz.devxyzmusic` (debug: `.debug` suffix)
- minSdk 29, targetSdk 36, compileSdk 36, AGP 8.9.3, Kotlin 2.0.21
- GitHub Actions: `.github/workflows/build.yml` -> debug APK artifact
- Play Protect OFF muna bago mag-install ng debug APK, gaya ng ibang
  Devxyz debug builds.

## Status

- v1: CI green, APK delivered 2026-10-26. Phone test ni Jepong:
  library screen ay generic purple Material (hindi Square) at
  Songs (0) — tama ang reklamo niya.
- v2 (ito): library + buong app naka-Square beige/taupe palette na
  (walang default purple), status-bar overlap ayos, auto-request ng
  audio permission sa unang bukas, auto-scan pagka-allow at pagbalik
  sa app, I-scan ulit button, at BROAD MediaStore scan (walang
  IS_MUSIC filter) para lumabas ang mga kanta kahit hindi naka-flag
  na music. Player: walang video/streaming button, local lang pa rin.
- v3 (ito): player palette sinukat mismo sa player-2.png (540x1136):
  background #E4D7C7 -> #C7BEB0 -> #A9A49A, title pill at pause
  #948875, translucent circles #7F7566, top circles 44dp, prev/next
  56dp, pause 80dp, bottom pill first icon 40dp. Walang video/
  streaming button pa rin (local-only, ayon sa unang order niya).
- v4 (ito, frame by frame sa player-2.png 540x1136, /1.5 = dp):
  top circles 56px=37dp, top bar 75px=50dp, art square 540x540
  (aspectRatio 1f, hindi stretched), title pill margins 49px=32dp,
  prev/next circles 56px=37dp + icons 30px=20dp, pause circle
  78px=52dp + icon 36px=24dp, bottom pill nasa baba (weight spacer)
  + bottom margin 36px=24dp. Walang Square code na kinopya —
  sukat at kulay lang mula sa screenshot ang ginamit.
- Ang phone test ni Jepong ang final verdict.
