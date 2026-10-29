package com.newswarajya.noswipe.reelshortblocker.service.deviceadmin

import android.app.admin.DeviceAdminReceiver
import android.content.Context
import android.content.Intent
import com.newswarajya.noswipe.reelshortblocker.data.ServiceLocator

/**
 * Device Admin Receiver enabling force-lock block mode and anti-tamper uninstall protection
 * while strict protection or PIN security is active.
 */
class NSDeviceAdminReceiver : DeviceAdminReceiver() {

    override fun onEnabled(context: Context, intent: Intent) {
        super.onEnabled(context, intent)
        ServiceLocator.init(context)
        ServiceLocator.refreshSystemPermissions(context)
    }

    override fun onDisabled(context: Context, intent: Intent) {
        super.onDisabled(context, intent)
        ServiceLocator.init(context)
        ServiceLocator.refreshSystemPermissions(context)
    }

    override fun onDisableRequested(context: Context, intent: Intent): CharSequence {
        return "Disabling NoScroll Anti-Tamper Protection will allow short-form feeds to bypass Lock Screen interception."
    }
}
