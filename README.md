# weatherdrive-app

<img width="1024" height="500" alt="Weatherdrive - Feature" src="https://github.com/user-attachments/assets/77c20c14-37d7-4299-8424-34f4876014bb" />

A [Compose Multiplatform](https://www.jetbrains.com/compose-multiplatform/) app for browsing the Weatherdrive archive at [https://www.flightpathestate.com/drvr/#/](https://www.flightpathestate.com/drvr/#/).
> ⚠️ This is early stages and a rebuild of the original iOS app available on TestFlight:
> [https://testflight.apple.com/join/laMnkrEC](https://testflight.apple.com/join/laMnkrEC)

An Android build is also available via Firebase App Distribution:
[https://appdistribution.firebase.dev/i/ea387e6b184333fd](https://appdistribution.firebase.dev/i/ea387e6b184333fd)

<table>
  <tr>
    <td><img width="270" alt="Screenshot_20260813_164022" src="https://github.com/user-attachments/assets/b8006f89-e554-4066-815e-35ba4b95d8b8" /></td>
    <td><img width="270" alt="Screenshot_20260813_164109" src="https://github.com/user-attachments/assets/d629632c-89dc-45d2-9032-ff836939790e" /></td>
    <td><img width="270" alt="Screenshot_20260813_164117" src="https://github.com/user-attachments/assets/7b5c17af-3748-498c-9b4f-faa648c738cb" /></td>
    <td><img width="270" alt="Screenshot_20260813_164122" src="https://github.com/user-attachments/assets/b6e7b289-f4f8-4112-836f-69b1ed4f37f1" /></td>
  </tr>
</table>

## Overview
Because streaming from Google Drive and downloading files above 100 MB requires authentication, playback is made possible through a URL resolver that obtains a direct download link. The audio is then downloaded to disk and played back from there.
## Libraries & Dependencies
- **[Ketch](https://github.com/linroid/Ketch/tree/main/library)** — A great multiplatform download library used to download audio files to disk.
- **[radioplayer-kt](https://github.com/markst/radioplayer-kt)** — Demonstrates using this multiplatform media player repository for audio playback.
- **[expandable-player-kt](https://github.com/markst/expandable-player-kt)** — Used to present an expandable player UI.
