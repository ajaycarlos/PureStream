package com.purestream.app

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

// This simple annotation tells Hilt to wake up and start generating code
// the moment the user taps your app icon on their phone.
@HiltAndroidApp
class PureStreamApp : Application()