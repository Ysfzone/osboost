package com.osboost

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.app.Activity
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.osboost.audio.SoundEffects
import com.osboost.engine.CleanResult
import com.osboost.engine.EngineMode
import com.osboost.engine.SmartCleaner
import com.osboost.updater.ReleaseInfo
import com.osboost.updater.UpdateChecker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : Activity() {

    private val uiScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private var autoDismissJob: Job? = null
    private var flameAnimator: AnimatorSet? = null
    private var updateGlowAnimator: AnimatorSet? = null
    private var resultsShown = false

    private lateinit var popupCard: LinearLayout
    private lateinit var tvModeLabel: TextView
    private lateinit var rocketStage: FrameLayout
    private lateinit var ivRocket: ImageView
    private lateinit var ivFlame: ImageView
    private lateinit var llCleaningPill: LinearLayout
    private lateinit var llResults: LinearLayout
    private lateinit var tvHeroRam: TextView
    private lateinit var tvDeltaBadge: TextView
    private lateinit var pbRam: ProgressBar
    private lateinit var tvRamUsage: TextView
    private lateinit var btnUpdate: Button
    private lateinit var tvFooterVersion: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        bindViews()
        applyModeLabel(SmartCleaner.peekInitialMode(applicationContext))
        startSequence()
    }

    private fun bindViews() {
        popupCard = findViewById(R.id.popupCard)
        tvModeLabel = findViewById(R.id.tvModeLabel)
        rocketStage = findViewById(R.id.rocketStage)
        ivRocket = findViewById(R.id.ivRocket)
        ivFlame = findViewById(R.id.ivFlame)
        llCleaningPill = findViewById(R.id.llCleaningPill)
        llResults = findViewById(R.id.llResults)
        tvHeroRam = findViewById(R.id.tvHeroRam)
        tvDeltaBadge = findViewById(R.id.tvDeltaBadge)
        pbRam = findViewById(R.id.pbRam)
        tvRamUsage = findViewById(R.id.tvRamUsage)
        btnUpdate = findViewById(R.id.btnUpdate)
        tvFooterVersion = findViewById(R.id.tvFooterVersion)
        tvFooterVersion.text = "v${BuildConfig.VERSION_NAME}"
    }

    private fun applyModeLabel(mode: EngineMode) {
        val modeResId = when (mode) {
            EngineMode.DEEP_ROOT -> R.string.mode_deep_root
            EngineMode.DEEP_SHIZUKU -> R.string.mode_deep_shizuku
            EngineMode.STANDARD_BOOST -> R.string.mode_standard
            EngineMode.SMART_ANALYSIS -> R.string.mode_smart_analysis
        }
        tvModeLabel.setText(modeResId)
    }

    private fun startSequence() {
        popupCard.alpha = 0f
        popupCard.scaleX = 0.9f
        popupCard.scaleY = 0.9f

        val cleanTask = uiScope.async { SmartCleaner.cleanAndAnalyze(applicationContext) }
        val updateTask = uiScope.async { UpdateChecker.checkLatestRelease() }

        uiScope.launch {
            // Phase 1 (0–250ms): Translucent card fade + scale in
            SoundEffects.playLaunchWhoosh(this@MainActivity)
            popupCard.animate()
                .alpha(1f)
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(250L)
                .setInterpolator(DecelerateInterpolator())
                .start()

            delay(250L)

            // Phase 2 (250–1400ms): Full-height vertical rocket launch + flame + scan bar
            startRocketLaunchAnimation(durationMs = 1150L)
            delay(1150L)

            // Phase 3 (1400ms+): Wait for cleaner result, hide Cleaning pill, render results
            val result = cleanTask.await()
            stopFlameAnimation()
            renderResults(result)

            val availableUpdate = updateTask.await()
            if (availableUpdate != null) {
                showUpdateButton(availableUpdate)
            } else {
                scheduleAutoDismiss()
            }
        }
    }

    private fun startRocketLaunchAnimation(durationMs: Long) {
        // Ascend smoothly all the way up to the top inner edge of popupCard
        val distanceToTopEdge = (rocketStage.top + ivRocket.top - popupCard.paddingTop)
            .toFloat()
            .takeIf { it > 0f }
            ?: (72f * resources.displayMetrics.density)
        val apexY = -distanceToTopEdge

        ObjectAnimator.ofFloat(ivRocket, View.TRANSLATION_Y, 0f, apexY).apply {
            duration = durationMs
            interpolator = AccelerateDecelerateInterpolator()
            start()
        }

        ObjectAnimator.ofInt(pbRam, "progress", 0, 100).apply {
            duration = durationMs
            interpolator = AccelerateDecelerateInterpolator()
            start()
        }

        ivFlame.alpha = 1f
        val flamePulseY = ObjectAnimator.ofFloat(ivFlame, View.SCALE_Y, 0.85f, 1.35f).apply {
            repeatMode = ValueAnimator.REVERSE
            repeatCount = ValueAnimator.INFINITE
            duration = 110L
        }
        val flameFollow = ObjectAnimator.ofFloat(ivFlame, View.TRANSLATION_Y, 0f, apexY).apply {
            duration = durationMs
            interpolator = AccelerateDecelerateInterpolator()
        }
        flameAnimator = AnimatorSet().apply {
            playTogether(flamePulseY, flameFollow)
            start()
        }
    }

    private fun stopFlameAnimation() {
        flameAnimator?.cancel()
        ivFlame.animate().alpha(0f).setDuration(180L).start()
    }

    private fun renderResults(result: CleanResult) {
        resultsShown = true
        SoundEffects.playCompleteChime(this)

        applyModeLabel(result.mode)

        // Remove centered Cleaning... pill once results appear
        llCleaningPill.visibility = View.GONE

        llResults.alpha = 0f
        llResults.visibility = View.VISIBLE
        llResults.animate().alpha(1f).setDuration(220L).start()

        tvHeroRam.text = result.stats.availableFormatted

        if (result.stats.deltaAvailableMB > 0) {
            tvDeltaBadge.text = getString(R.string.fmt_freed_mb, result.stats.deltaAvailableMB)
        } else {
            tvDeltaBadge.setText(R.string.label_ram_optimized)
        }
        tvDeltaBadge.setTextColor(ContextCompat.getColor(this, R.color.brand_cyan_electric))

        ObjectAnimator.ofInt(pbRam, "progress", pbRam.progress, result.stats.after.usedPercentage).apply {
            duration = 300L
            interpolator = DecelerateInterpolator()
            start()
        }

        tvRamUsage.text = getString(
            R.string.fmt_ram_usage,
            result.stats.usedFormatted,
            result.stats.totalFormatted
        )
        tvRamUsage.visibility = View.VISIBLE
    }

    private fun showUpdateButton(release: ReleaseInfo) {
        btnUpdate.text = getString(R.string.fmt_update_available, release.versionName)
        btnUpdate.visibility = View.VISIBLE
        btnUpdate.requestFocus()

        // Continuous pulsing glow/alpha animation on Update Available button
        val pulseAlpha = ObjectAnimator.ofFloat(btnUpdate, View.ALPHA, 1f, 0.65f).apply {
            repeatMode = ValueAnimator.REVERSE
            repeatCount = ValueAnimator.INFINITE
            duration = 750L
        }
        val pulseScaleX = ObjectAnimator.ofFloat(btnUpdate, View.SCALE_X, 1f, 1.025f).apply {
            repeatMode = ValueAnimator.REVERSE
            repeatCount = ValueAnimator.INFINITE
            duration = 750L
        }
        val pulseScaleY = ObjectAnimator.ofFloat(btnUpdate, View.SCALE_Y, 1f, 1.025f).apply {
            repeatMode = ValueAnimator.REVERSE
            repeatCount = ValueAnimator.INFINITE
            duration = 750L
        }
        updateGlowAnimator = AnimatorSet().apply {
            playTogether(pulseAlpha, pulseScaleX, pulseScaleY)
            start()
        }

        btnUpdate.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                autoDismissJob?.cancel()
            }
        }

        btnUpdate.setOnClickListener {
            autoDismissJob?.cancel()
            updateGlowAnimator?.cancel()
            btnUpdate.alpha = 1f
            btnUpdate.scaleX = 1f
            btnUpdate.scaleY = 1f
            btnUpdate.isEnabled = false
            btnUpdate.setText(R.string.update_downloading)
            UpdateChecker.downloadAndInstall(this, release)
        }

        scheduleAutoDismiss(delayMs = 6000L)
    }

    private fun scheduleAutoDismiss(delayMs: Long = 2000L) {
        autoDismissJob?.cancel()
        autoDismissJob = uiScope.launch {
            delay(delayMs)
            dismissNow()
        }
    }

    private fun dismissNow() {
        autoDismissJob?.cancel()
        finishAndRemoveTask()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        return when (keyCode) {
            KeyEvent.KEYCODE_BACK -> {
                dismissNow()
                true
            }
            KeyEvent.KEYCODE_DPAD_CENTER, KeyEvent.KEYCODE_ENTER -> {
                if (btnUpdate.visibility == View.VISIBLE && btnUpdate.hasFocus()) {
                    super.onKeyDown(keyCode, event)
                } else if (resultsShown) {
                    dismissNow()
                    true
                } else {
                    super.onKeyDown(keyCode, event)
                }
            }
            else -> super.onKeyDown(keyCode, event)
        }
    }

    override fun onDestroy() {
        flameAnimator?.cancel()
        updateGlowAnimator?.cancel()
        uiScope.cancel()
        super.onDestroy()
    }
}
