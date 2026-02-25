package com.weatherdrive.persistence

import java.io.File

actual fun fileExists(path: String): Boolean = File(path).exists()
