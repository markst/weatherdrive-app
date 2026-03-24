# weatherdrive-app

A [Compose Multiplatform](https://www.jetbrains.com/compose-multiplatform/) app for browsing the Weatherdrive archive at [https://www.flightpathestate.com/drvr/#/](https://www.flightpathestate.com/drvr/#/).

> ⚠️ This is early stages and a rebuild of the original iOS app available on TestFlight:
> [https://testflight.apple.com/join/laMnkrEC](https://testflight.apple.com/join/laMnkrEC)

## Overview

Because streaming from Google Drive and downloading files above 100 MB requires authentication, playback is made possible through a URL resolver that obtains a direct download link. The audio is then downloaded to disk and played back from there.

## Libraries & Dependencies

- **[Ketch](https://github.com/linroid/Ketch/tree/main/library)** — A great multiplatform download library used to download audio files to disk.
- **[radioplayer-kt](https://github.com/markst/radioplayer-kt)** — Demonstrates using this multiplatform media player repository for audio playback.
- **[expandable-player-kt](https://github.com/markst/expandable-player-kt)** — Used to present an expandable player UI.
