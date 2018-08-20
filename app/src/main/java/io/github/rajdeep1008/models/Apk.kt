package io.github.rajdeep1008.models

import android.content.pm.ApplicationInfo

/**
 * Created by rajdeep1008 on 19/04/18.
 */

data class Apk(val appInfo: ApplicationInfo,
               val appName: String,
               val packageName: String? = "",
               val version: String? = "")