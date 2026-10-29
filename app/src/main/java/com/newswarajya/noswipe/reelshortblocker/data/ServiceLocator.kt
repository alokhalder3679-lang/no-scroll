package com.newswarajya.noswipe.reelshortblocker.data

import android.accessibilityservice.AccessibilityServiceInfo
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.os.PowerManager
import android.provider.Settings
import android.view.accessibility.AccessibilityManager
import com.newswarajya.noswipe.reelshortblocker.data.database.NoScrollDbHelper
import com.newswarajya.noswipe.reelshortblocker.data.datasource.PlatformConfigDataSource
import com.newswarajya.noswipe.reelshortblocker.data.preferences.NoScrollPreferences
import com.newswarajya.noswipe.reelshortblocker.data.repository.AnalyticsRepositoryImpl
import com.newswarajya.noswipe.reelshortblocker.data.repository.NoScrollRepositoryImpl
import com.newswarajya.noswipe.reelshortblocker.data.repository.SettingsRepositoryImpl
import com.newswarajya.noswipe.reelshortblocker.domain.usecase.CheckDailyLimitUseCase
import com.newswarajya.noswipe.reelshortblocker.domain.usecase.DetectShortContentUseCase
import com.newswarajya.noswipe.reelshortblocker.domain.usecase.GetDailyAnalyticsUseCase
import com.newswarajya.noswipe.reelshortblocker.domain.usecase.ManageDetectionPlanUseCase
import com.newswarajya.noswipe.reelshortblocker.domain.usecase.ProcessBlockActionUseCase
import com.newswarajya.noswipe.reelshortblocker.domain.usecase.RecordBlockHistoryUseCase
import com.newswarajya.noswipe.reelshortblocker.service.deviceadmin.NSDeviceAdminReceiver

/**
 * Lightweight Constructor-Injection Service Locator providing shared singleton repositories
 * and domain use cases across Activities, ViewModels, AccessibilityService, and BroadcastReceivers.
 */
object ServiceLocator {

    @Volatile
    private var initialized = false

    lateinit var dbHelper: NoScrollDbHelper
        private set
    lateinit var preferences: NoScrollPreferences
        private set
    lateinit var platformDataSource: PlatformConfigDataSource
        private set
    lateinit var noScrollRepository: NoScrollRepositoryImpl
        private set
    lateinit var analyticsRepository: AnalyticsRepositoryImpl
        private set
    lateinit var settingsRepository: SettingsRepositoryImpl
        private set

    val detectShortContentUseCase = DetectShortContentUseCase()
    val processBlockActionUseCase = ProcessBlockActionUseCase()
    val checkDailyLimitUseCase = CheckDailyLimitUseCase()

    lateinit var recordBlockHistoryUseCase: RecordBlockHistoryUseCase
        private set
    lateinit var getDailyAnalyticsUseCase: GetDailyAnalyticsUseCase
        private set
    lateinit var manageDetectionPlanUseCase: ManageDetectionPlanUseCase
        private set

    fun init(context: Context) {
        if (initialized) return
        synchronized(this) {
            if (initialized) return
            val appContext = context.applicationContext
            dbHelper = NoScrollDbHelper(appContext)
            preferences = NoScrollPreferences(appContext)
            platformDataSource = PlatformConfigDataSource(appContext)
            noScrollRepository = NoScrollRepositoryImpl(
                preferences = preferences,
                platformDataSource = platformDataSource,
                dbHelper = dbHelper
            )
            analyticsRepository = AnalyticsRepositoryImpl(
                dbHelper = dbHelper,
                preferences = preferences,
                onBlockRecordedCallback = {
                    noScrollRepository.reloadPlatformsWithState()
                }
            )
            settingsRepository = SettingsRepositoryImpl(preferences)

            recordBlockHistoryUseCase = RecordBlockHistoryUseCase(analyticsRepository)
            getDailyAnalyticsUseCase = GetDailyAnalyticsUseCase(analyticsRepository)
            manageDetectionPlanUseCase = ManageDetectionPlanUseCase(noScrollRepository)
            refreshSystemPermissions(appContext)
            initialized = true
        }
    }

    fun refreshSystemPermissions(context: Context) {
        val appContext = context.applicationContext
        val am = appContext.getSystemService(Context.ACCESSIBILITY_SERVICE) as? AccessibilityManager
        val enabledServices = am?.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK)
            ?: emptyList()
        val isAccessibilityActive = enabledServices.any {
            it.resolveInfo?.serviceInfo?.packageName == appContext.packageName
        }

        val dpm = appContext.getSystemService(Context.DEVICE_POLICY_SERVICE) as? DevicePolicyManager
        val adminComponent = ComponentName(appContext, NSDeviceAdminReceiver::class.java)
        val isAdminActive = dpm?.isAdminActive(adminComponent) == true

        val isOverlayGranted = Settings.canDrawOverlays(appContext)

        val pm = appContext.getSystemService(Context.POWER_SERVICE) as? PowerManager
        val isBatteryOptIgnored = pm?.isIgnoringBatteryOptimizations(appContext.packageName) == true

        if (::settingsRepository.isInitialized) {
            settingsRepository.updateSystemPermissionStates(
                accessibilityActive = isAccessibilityActive,
                adminActive = isAdminActive,
                overlayGranted = isOverlayGranted,
                batteryOptimizationIgnored = isBatteryOptIgnored
            )
        }
    }
}
