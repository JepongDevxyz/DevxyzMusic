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
- Ang phone test ni Jepong ang final verdict.
