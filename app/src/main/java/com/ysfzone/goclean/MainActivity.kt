package com.ysfzone.goclean

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
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.ysfzone.goclean.audio.SoundEffects
import com.ysfzone.goclean.engine.CleanResult
import com.ysfzone.goclean.engine.SmartCleaner
import com.ysfzone.goclean.updater.ReleaseInfo
import com.ysfzone.goclean.updater.UpdateChecker
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
    private var resultsShown = false

    private lateinit var popupCard: LinearLayout
    private lateinit var tvStatusBadge: TextView
    private lateinit var ivRocket: ImageView
    private lateinit var ivFlame: ImageView
    private lateinit var tvScanningLabel: TextView
    private lateinit var llResults: LinearLayout
    private lateinit var tvHeroRam: TextView
    private lateinit var tvDeltaBadge: TextView
    private lateinit var pbRam: ProgressBar
    private lateinit var tvRamUsage: TextView
    private lateinit var btnUpdate: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        bindViews()
        startSequence()
    }

    private fun bindViews() {
        popupCard = findViewById(R.id.popupCard)
        tvStatusBadge = findViewById(R.id.tvStatusBadge)
        ivRocket = findViewById(R.id.ivRocket)
        ivFlame = findViewById(R.id.ivFlame)
        tvScanningLabel = findViewById(R.id.tvScanningLabel)
        llResults = findViewById(R.id.llResults)
        tvHeroRam = findViewById(R.id.tvHeroRam)
        tvDeltaBadge = findViewById(R.id.tvDeltaBadge)
        pbRam = findViewById(R.id.pbRam)
        tvRamUsage = findViewById(R.id.tvRamUsage)
        btnUpdate = findViewById(R.id.btnUpdate)
    }

    private fun startSequence() {
        // Initial hidden state for 0-250ms entrance
        popupCard.alpha = 0f
        popupCard.scaleX = 0.9f
        popupCard.scaleY = 0.9f

        val cleanTask = uiScope.async { SmartCleaner.cleanAndAnalyze(applicationContext) }
        val updateTask = uiScope.async { UpdateChecker.checkLatestRelease() }

        uiScope.launch {
            // Phase 1 (0–250ms): Card fade + scale in
            SoundEffects.playLaunchWhoosh(this@MainActivity)
            popupCard.animate()
                .alpha(1f)
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(250L)
                .setInterpolator(DecelerateInterpolator())
                .start()

            delay(250L)

            // Phase 2 (250–1400ms): Rocket launch + flame + scan progress bar
            startRocketLaunchAnimation(durationMs = 1150L)
            delay(1150L)

            // Phase 3 (1400ms+): Wait for cleaner result if still finishing, then render
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
        val liftDp = -28f * resources.displayMetrics.density

        ObjectAnimator.ofFloat(ivRocket, View.TRANSLATION_Y, 0f, liftDp, liftDp * 0.65f).apply {
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
        val flameFollow = ObjectAnimator.ofFloat(ivFlame, View.TRANSLATION_Y, 0f, liftDp, liftDp * 0.65f).apply {
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

        // Status badge
        if (result.isOptimized) {
            tvStatusBadge.setText(R.string.status_optimized)
            tvStatusBadge.setTextColor(ContextCompat.getColor(this, R.color.neon_cyan))
        } else {
            tvStatusBadge.setText(R.string.status_analyzed)
            tvStatusBadge.setTextColor(ContextCompat.getColor(this, R.color.neon_blue))
        }

        // Swap scanning label for hero metrics
        tvScanningLabel.visibility = View.GONE
        llResults.alpha = 0f
        llResults.visibility = View.VISIBLE
        llResults.animate().alpha(1f).setDuration(220L).start()

        tvHeroRam.text = result.stats.availableFormatted

        if (result.stats.deltaAvailableMB > 0) {
            tvDeltaBadge.text = getString(R.string.fmt_freed_mb, result.stats.deltaAvailableMB)
            tvDeltaBadge.setTextColor(ContextCompat.getColor(this, R.color.neon_emerald))
        } else {
            tvDeltaBadge.setText(R.string.label_memory_healthy)
            tvDeltaBadge.setTextColor(ContextCompat.getColor(this, R.color.neon_cyan))
        }

        // Update progress bar to actual used RAM percentage
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

        btnUpdate.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                autoDismissJob?.cancel()
            }
        }

        btnUpdate.setOnClickListener {
            autoDismissJob?.cancel()
            btnUpdate.isEnabled = false
            btnUpdate.setText(R.string.update_downloading)
            UpdateChecker.downloadAndInstall(this, release)
        }

        // If user doesn't interact, still auto-dismiss after 6 seconds
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
        uiScope.cancel()
        super.onDestroy()
    }
}

