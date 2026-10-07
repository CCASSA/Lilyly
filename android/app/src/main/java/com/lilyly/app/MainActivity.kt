package com.lilyly.app

import android.app.KeyguardManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.os.CancellationSignal
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.SystemBarStyle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat

class MainActivity : ComponentActivity() {
    private lateinit var store:AppStore
    private var unlocked by mutableStateOf(false)
    private var authMessage by mutableStateOf("")
    private var callback:((Boolean)->Unit)?=null
    private var authenticating=false
    private val credential=registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result -> finishAuth(result.resultCode==RESULT_OK) }
    private fun finishAuth(success:Boolean) {
        authenticating=false
        if(success) {unlocked=true;authMessage=""} else authMessage="Lilyly is still locked. Try again when you're ready."
        val completed=callback;callback=null;completed?.invoke(success)
    }
    @Suppress("DEPRECATION")
    fun authenticate(onResult:(Boolean)->Unit) {
        if(authenticating) return
        val keyguard=getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
        if(!keyguard.isDeviceSecure) {authMessage="Set a screen lock in Android Settings first.";onResult(false);return}
        callback=onResult;authenticating=true
        fun screenLock() {
            val intent=keyguard.createConfirmDeviceCredentialIntent("Open Lilyly","Your private world")
            if(intent==null) finishAuth(false) else credential.launch(intent)
        }
        val biometrics = if(Build.VERSION.SDK_INT>=29) {
            (getSystemService(Context.BIOMETRIC_SERVICE) as android.hardware.biometrics.BiometricManager).canAuthenticate()==android.hardware.biometrics.BiometricManager.BIOMETRIC_SUCCESS
        } else if(Build.VERSION.SDK_INT>=28) {
            val fingerprint=getSystemService(Context.FINGERPRINT_SERVICE) as? android.hardware.fingerprint.FingerprintManager
            fingerprint?.isHardwareDetected==true && fingerprint.hasEnrolledFingerprints()
        } else false
        if(Build.VERSION.SDK_INT>=28 && biometrics) {
            val executor=ContextCompat.getMainExecutor(this)
            android.hardware.biometrics.BiometricPrompt.Builder(this).setTitle("Open Lilyly").setSubtitle("Your private world")
                .setNegativeButton("Use screen lock",executor) {_,_->screenLock()}.build()
                .authenticate(CancellationSignal(),executor,object:android.hardware.biometrics.BiometricPrompt.AuthenticationCallback() {
                    override fun onAuthenticationSucceeded(result:android.hardware.biometrics.BiometricPrompt.AuthenticationResult) {finishAuth(true)}
                    override fun onAuthenticationError(code:Int,message:CharSequence) {finishAuth(false);authMessage=message.toString()}
                })
        } else screenLock()
    }
    override fun onStop() {
        if(::store.isInitialized && store.appLockEnabled) unlocked=false
        super.onStop()
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        store=AppStore(this)
        MedicationReminders.schedule(this,store.medications,store.medicationLogs)
        unlocked=!store.appLockEnabled
        setContent {
            DisposableEffect(store.darkTheme,store.appLockEnabled,store.hideScreenshots) {
                val style=if(store.darkTheme) SystemBarStyle.dark(android.graphics.Color.rgb(16,15,22)) else SystemBarStyle.light(android.graphics.Color.rgb(245,235,221),android.graphics.Color.rgb(16,15,22))
                enableEdgeToEdge(statusBarStyle=style,navigationBarStyle=style)
                if(store.appLockEnabled || store.hideScreenshots) window.addFlags(WindowManager.LayoutParams.FLAG_SECURE) else window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
                onDispose { }
            }
            val pages=rememberSaveableStateHolder()
            if(!store.appLockEnabled || unlocked) pages.SaveableStateProvider("lilyly") {LilylyApp(store, intent.getBooleanExtra("medication",false))}
            else LilylyTheme(store.darkTheme) {
                Surface(Modifier.fillMaxSize()) {
                    Column(Modifier.fillMaxSize().padding(30.dp),verticalArrangement=Arrangement.Center,horizontalAlignment=Alignment.CenterHorizontally) {
                        Text("☾",style=MaterialTheme.typography.displayLarge)
                        Text("Your world, kept private",style=MaterialTheme.typography.headlineMedium)
                        Text("Unlock with Android biometrics or your phone's screen lock.",modifier=Modifier.padding(vertical=16.dp))
                        Button(onClick={authenticate {}},modifier=Modifier.fillMaxWidth()) {Text("Open Lilyly")}
                        if(authMessage.isNotBlank()) Text(authMessage,modifier=Modifier.padding(top=14.dp))
                    }
                }
            }
        }
    }
}
