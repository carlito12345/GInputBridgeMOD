package com.salat.gbinder

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.graphics.Point
import android.graphics.drawable.AnimationDrawable
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.view.Gravity
import android.view.GestureDetector
import android.view.GestureDetector.SimpleOnGestureListener
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.salat.gbinder.components.launchApp
import com.salat.gbinder.datastore.LauncherPrefs
import com.salat.gbinder.datastore.LauncherStorageRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlin.math.abs
import kotlin.math.sqrt
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject
import kotlin.random.Random

@AndroidEntryPoint
class FloatingButtonService : Service() {

    companion object {
        @Volatile
        var isAlive: Boolean = false
        @Volatile
        var isStarting: Boolean = false
        private const val CHANNEL_ID = "floating_button_channel"
        private const val NOTIFICATION_ID = 2008
        private const val SWIPE_THRESHOLD = 50
        private const val SWIPE_VELOCITY_THRESHOLD = 100
        private const val PET_MOVE_INTERVAL = 4000L
        private const val PET_STOP_INTERVAL = 3000L
        private const val PROTECTION_TIME = 10000L
    }

    @Inject
    lateinit var storage: LauncherStorageRepository

    private lateinit var windowManager: WindowManager
    private lateinit var floatingButton: ImageView
    private lateinit var gestureDetector: GestureDetector
    private var params: WindowManager.LayoutParams? = null
    private var initialX = 0
    private var initialY = 0
    private var initialTouchX = 0f
    private var initialTouchY = 0f
    private var isDragging = false
    private var gestureConfig = mapOf("single_click" to "toggle_launcher")
    private var petMode = false
    private var inProtection = false
    private var displayWidth = 0
    private var displayHeight = 0
    private var buttonSize = 100
    private var buttonAlpha = 1.0f
    private val mainHandler = Handler(Looper.getMainLooper())
    private var petRunnable: Runnable? = null
    private var animDrawable: AnimationDrawable? = null

    private val fbScope = kotlinx.coroutines.CoroutineScope(Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        isAlive = true
        isStarting = false
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, buildNotification())
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager

        val display = windowManager.defaultDisplay
        val point = Point()
        display.getSize(point)
        displayWidth = point.x
        displayHeight = point.y

        // Observe all settings continuously
        fbScope.launch {
            combine(
                storage.dataStore.getValueFlow(LauncherPrefs.FLOAT_BUTTON_GESTURES, ""),
                storage.dataStore.getValueFlow(LauncherPrefs.FLOAT_BUTTON_SIZE, 100),
                storage.dataStore.getValueFlow(LauncherPrefs.FLOAT_BUTTON_ALPHA, 1.0f),
                storage.dataStore.getValueFlow(LauncherPrefs.FLOAT_BUTTON_PET_MODE, false)
            ) { gestures, size, alpha, pet ->
                gestureConfig = parseGestureConfig(gestures)
                withContext(Dispatchers.Main) {
                    updateButton(size, alpha, pet)
                }
            }.collect {}
        }

        gestureDetector = GestureDetector(this, object : SimpleOnGestureListener() {
            override fun onDown(e: MotionEvent): Boolean = true
            override fun onLongPress(e: MotionEvent) {
                if (!isDragging) {
                    enterProtectionMode()
                    handleGesture("long_press")
                }
            }
            override fun onFling(e1: MotionEvent?, e2: MotionEvent, vX: Float, vY: Float): Boolean {
                if (e1 == null || isDragging) return false
                val dx = e2.x - e1.x; val dy = e2.y - e1.y
                if (abs(dx) > abs(dy)) {
                    if (abs(dx) > SWIPE_THRESHOLD && abs(vX) > SWIPE_VELOCITY_THRESHOLD)
                        handleGesture(if (dx > 0) "swipe_right" else "swipe_left")
                } else {
                    if (abs(dy) > SWIPE_THRESHOLD && abs(vY) > SWIPE_VELOCITY_THRESHOLD)
                        handleGesture(if (dy > 0) "swipe_down" else "swipe_up")
                }
                return true
            }
        })
        gestureDetector.setOnDoubleTapListener(object : GestureDetector.OnDoubleTapListener {
            override fun onDoubleTap(e: MotionEvent): Boolean {
                enterProtectionMode()
                handleGesture("double_click")
                return true
            }
            override fun onDoubleTapEvent(e: MotionEvent): Boolean = false
            override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
                enterProtectionMode()
                handleGesture("single_click")
                return true
            }
        })
    }

    private fun setupFloatingButton() {
        if (::floatingButton.isInitialized) {
            runCatching { windowManager.removeView(floatingButton) } 
        }
        floatingButton = ImageView(this).apply {
            scaleType = ImageView.ScaleType.FIT_CENTER
            alpha = buttonAlpha
            if (petMode) {
                setImageResource(R.drawable.fb_animation)
                animDrawable = drawable as? AnimationDrawable
                animDrawable?.start()
            } else {
                setImageDrawable(ContextCompat.getDrawable(this@FloatingButtonService, R.drawable.ic_floating_button))
                animDrawable?.stop()
                animDrawable = null
            }
        }

        params = WindowManager.LayoutParams(
            (dpToPx(buttonSize) * 1.5f).toInt(),
            (dpToPx(buttonSize) * 1.5f).toInt(),
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or
                    WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = displayWidth - dpToPx(buttonSize) - 20
            y = displayHeight / 3
        }

        floatingButton.setOnTouchListener { _, event ->
            gestureDetector.onTouchEvent(event)
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    // Stop pet movement on touch
                    stopPetMovement()
                    initialX = params?.x ?: 0
                    initialY = params?.y ?: 0
                    initialTouchX = event.rawX
                    initialTouchY = event.rawY
                    isDragging = false
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    val dx = event.rawX - initialTouchX
                    val dy = event.rawY - initialTouchY
                    if (sqrt(dx * dx + dy * dy) > 10f) {
                        isDragging = true
                        params?.x = (initialX + dx).toInt()
                        params?.y = (initialY + dy).toInt()
                        params?.let { windowManager.updateViewLayout(floatingButton, it) }
                    }
                    true
                }
                MotionEvent.ACTION_UP -> {
                    if (isDragging) {
                        // Dragged - resume pet movement after protection
                        if (petMode && !inProtection) startPetMovement()
                    }
                    true
                }
                else -> false
            }
        }

        runCatching { windowManager.addView(floatingButton, params) }
            .onFailure { Timber.e(it, "Failed to add floating button") }

        // Start pet mode if enabled
        if (petMode) startPetMovement()
    }

    private fun enterProtectionMode() {
        if (petMode) {
            inProtection = true
            stopPetMovement()
            mainHandler.removeCallbacks(protectionRunnable)
            mainHandler.postDelayed(protectionRunnable, PROTECTION_TIME)
        }
    }

    private val protectionRunnable = Runnable {
        inProtection = false
        if (petMode) startPetMovement()
    }

    private fun startPetMovement() {
        stopPetMovement()
        petRunnable = object : Runnable {
            override fun run() {
                if (!petMode || inProtection || !::floatingButton.isInitialized) return
                // Random movement within screen bounds
                val size = dpToPx(buttonSize)
                val maxX = displayWidth - size - 10
                val maxY = displayHeight - size - 10
                val newX = Random.nextInt(10, maxOf(10, maxX))
                val newY = Random.nextInt(10, maxOf(10, maxY))
                params?.x = newX
                params?.y = newY
                runCatching { params?.let { windowManager.updateViewLayout(floatingButton, it) } }
                // Random interval between move and stop
                val isStop = Random.nextBoolean()
                val delay = if (isStop) Random.nextLong(2000, 5000) else Random.nextLong(3000, 7000)
                mainHandler.postDelayed(this, delay)
            }
        }
        mainHandler.postDelayed(petRunnable!!, Random.nextLong(1000, 5000))
    }

    private fun stopPetMovement() {
        petRunnable?.let { mainHandler.removeCallbacks(it) }
        petRunnable = null
    }

    private fun updateButton(size: Int, alpha: Float, pet: Boolean) {
        buttonSize = size
        buttonAlpha = alpha
        val oldPet = petMode
        petMode = pet
        
        if (::floatingButton.isInitialized) {
            // Update alpha
            floatingButton.alpha = alpha
            
            // Ensure no background color
            floatingButton.setBackgroundColor(android.graphics.Color.TRANSPARENT)
            
            // Update animation if pet mode changed
            if (pet != oldPet || !::floatingButton.isInitialized) {
                if (pet) {
                    floatingButton.setImageResource(R.drawable.fb_animation)
                    animDrawable = floatingButton.drawable as? AnimationDrawable
                    animDrawable?.start()
                    if (!inProtection) startPetMovement()
                } else {
                    floatingButton.setImageDrawable(
                        androidx.core.content.ContextCompat.getDrawable(this, R.drawable.ic_floating_button)
                    )
                    animDrawable?.stop()
                    animDrawable = null
                    stopPetMovement()
                }
            } else if (pet) {
                // Already in pet mode, update animation state
                if (!inProtection) startPetMovement()
            }
            
            // Update size - recreate window
            runCatching {
                val oldParams = params
                val newSize = dpToPx(size)
                if (oldParams != null) {
                    oldParams.width = newSize
                    oldParams.height = newSize
                    windowManager.updateViewLayout(floatingButton, oldParams)
                }
            }
        } else {
            // First time - create the button
            setupFloatingButton()
        }
    }

    private fun handleGesture(gesture: String) {
        Timber.d("[FLOAT] Gesture: $gesture")
        val action = gestureConfig[gesture] ?: return
        if (action.isEmpty()) return

        when {
            // Toggle launcher
            action == "toggle_launcher" -> {
                if (isLauncherServiceRunning(this)) stopLauncherOverlay(this)
                else startLauncherOverlay(this)
            }
            // Open GIB settings
            action == "open_gib" || action == "open_keybinds" -> {
                val intent = Intent(this, com.salat.gbinder.MainActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    if (action == "open_keybinds") {
                        putExtra("show_keybinds", true)
                    }
                }
                startActivity(intent)
            }
            // System navigation
            action == "android_back" -> {
                sendBroadcast(Intent("com.salat.gbinder.ANDROID_BACK"))
            }
            action == "android_home" -> {
                sendBroadcast(Intent("com.salat.gbinder.ANDROID_HOME"))
            }
            action == "navigate_to_past_app" -> {
                sendBroadcast(Intent("com.salat.gbinder.NAVIGATE_TO_PAST_APP"))
            }
            // Task manager
            action == "task_manager" -> {
                sendBroadcast(Intent("com.salat.gbinder.TASK_MANAGER"))
            }
            // App carousel
            action == "app_carousel" -> {
                sendBroadcast(Intent("com.salat.gbinder.APP_CAROUSEL"))
            }
            // App launcher
            action == "app_launcher" -> {
                startLauncherOverlay(this)
            }
            // Navigation/Media switch
            action == "navi_media_switch" -> {
                sendBroadcast(Intent("com.salat.gbinder.NAVI_MEDIA_SWITCH"))
            }
            // Audio source carousel
            action == "carousel_audio_source" -> {
                sendBroadcast(Intent("com.salat.gbinder.CAROUSEL_AUDIO_SOURCE"))
            }
            // 360 cameras
            action == "cameras_360" -> {
                sendBroadcast(Intent("com.salat.gbinder.CAMERAS_360"))
            }
            // CarPlay
            action == "carplay_launch" -> {
                sendBroadcast(Intent("com.salat.gbinder.CARPLAY_LAUNCH"))
            }
            // Driving mode toggle
            action == "toggle_dm" -> {
                sendBroadcast(Intent("com.salat.gbinder.TOGGLE_DM"))
            }
            // Driving mode carousel
            action == "carousel_dm" -> {
                sendBroadcast(Intent("com.salat.gbinder.CAROUSEL_DM"))
            }
            // Lamp mode carousel
            action == "carousel_lamp" -> {
                sendBroadcast(Intent("com.salat.gbinder.CAROUSEL_LAMP"))
            }
            // Phone call
            action == "phone_call" -> {
                sendBroadcast(Intent("com.salat.gbinder.PHONE_CALL"))
            }
            // Launch app
            action.startsWith("app:") -> {
                launchApp(action.substring(4), null)
            }
            // Launch activity
            action.startsWith("activity:") -> {
                val parts = action.substring(9).split("/")
                if (parts.size == 2) launchApp(parts[0], parts[1])
            }
        }
    }

    private fun parseGestureConfig(raw: String): Map<String, String> {
        if (raw.isBlank()) return mapOf("single_click" to "toggle_launcher")
        return try {
            val obj = org.json.JSONObject(raw)
            listOf("single_click","double_click","triple_click","long_press",
                "swipe_up","swipe_down","swipe_left","swipe_right").associateWith { obj.optString(it, "") }
        } catch (_: Exception) { mapOf("single_click" to "toggle_launcher") }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            (getSystemService(NOTIFICATION_SERVICE) as NotificationManager)
                .createNotificationChannel(NotificationChannel(CHANNEL_ID, "Floating Button", NotificationManager.IMPORTANCE_LOW))
        }
    }

    private fun buildNotification() = NotificationCompat.Builder(this, CHANNEL_ID)
        .setContentTitle("Floating Button").setContentText("Quick access to Glauncher")
        .setSmallIcon(R.drawable.ic_launcher_logo).setPriority(NotificationCompat.PRIORITY_MIN).setOngoing(true).build()

    fun updateGestureConfig(config: Map<String, String>) { gestureConfig = config }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int = START_STICKY

    override fun onDestroy() {
        isAlive = false; isStarting = false
        stopPetMovement(); mainHandler.removeCallbacks(protectionRunnable)
        animDrawable?.stop()
        if (::floatingButton.isInitialized) runCatching { windowManager.removeView(floatingButton) }
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun dpToPx(dp: Int): Int = (dp * resources.displayMetrics.density).toInt()
}
