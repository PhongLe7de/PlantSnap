package com.plantsnap

import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.view.animation.OvershootInterpolator
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.core.animation.doOnEnd
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.plantsnap.ui.navigation.AppNavigation
import com.plantsnap.ui.screens.profile.AuthViewModel
import com.plantsnap.ui.theme.PlantSnapTheme
import dagger.hilt.android.AndroidEntryPoint
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.handleDeeplinks
import javax.inject.Inject
import com.plantsnap.ui.screens.settings.SettingsViewModel
import com.plantsnap.domain.models.AppTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var supabaseClient: SupabaseClient

    private val authViewModel: AuthViewModel by viewModels()

    private val settingsViewModel: SettingsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen().apply {
            setKeepOnScreenCondition {
                authViewModel.uiState.value.hasCompletedOnboarding == null
            }
            setOnExitAnimationListener { splashScreenView ->
                val iconView = try {
                    splashScreenView.iconView // iconView is null when animations are disabled
                } catch (e: NullPointerException) {
                    splashScreenView.remove()
                    return@setOnExitAnimationListener
                }

                val zoomX = ObjectAnimator.ofFloat(iconView, "scaleX", 0.4f, 0.0f)
                zoomX.interpolator = OvershootInterpolator()
                zoomX.duration = 500L
                zoomX.doOnEnd { splashScreenView.remove() }

                val zoomY = ObjectAnimator.ofFloat(iconView, "scaleY", 0.4f, 0.0f)
                zoomY.interpolator = OvershootInterpolator()
                zoomY.duration = 500L

                zoomX.start()
                zoomY.start()
            }
        }
        super.onCreate(savedInstanceState)
        supabaseClient.handleDeeplinks(intent)
        enableEdgeToEdge()
        setContent {
            val settings by settingsViewModel.settings.collectAsStateWithLifecycle()
            PlantSnapTheme(appTheme = settings.theme) {
                AppNavigation()
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        supabaseClient.handleDeeplinks(intent)
    }
}