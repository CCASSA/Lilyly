package com.lilyly.app

import android.app.*
import android.content.*
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import java.time.LocalDateTime
import java.time.ZoneId

object MedicationReminders {
    const val CHANNEL="lilyly_medication"
    private fun pending(context:Context,at:String=""):PendingIntent = PendingIntent.getBroadcast(context,410,
        Intent(context,MedicationReminderReceiver::class.java).setAction("com.lilyly.MEDICATION").putExtra("scheduled",at),PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
    fun schedule(context:Context,medications:List<Medication>,logs:List<MedicationLog>) {
        val alarm=context.getSystemService(AlarmManager::class.java)
        alarm.cancel(pending(context))
        val next=nextMedicationReminder(medications,logs,LocalDateTime.now()) ?: return
        alarm.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,next.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(),pending(context,next.toString()))
    }
    fun enabled(context:Context):Boolean {
        val manager=context.getSystemService(NotificationManager::class.java)
        return NotificationManagerCompat.from(context).areNotificationsEnabled() && (manager.getNotificationChannel(CHANNEL)?.importance != NotificationManager.IMPORTANCE_NONE)
    }
    fun notify(context:Context) {
        val manager=context.getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(NotificationChannel(CHANNEL,"Medication reminders",NotificationManager.IMPORTANCE_DEFAULT).apply {description="Private reminders to review your recorded schedule";lockscreenVisibility=Notification.VISIBILITY_PRIVATE})
        if(!enabled(context))return
        val open=PendingIntent.getActivity(context,411,Intent(context,MainActivity::class.java).putExtra("medication",true).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP),PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val notification=NotificationCompat.Builder(context,CHANNEL).setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setContentTitle("A moment for your care").setContentText("Open Lilyly to review your medication schedule.")
            .setVisibility(NotificationCompat.VISIBILITY_PRIVATE).setContentIntent(open).setAutoCancel(true).build()
        try {manager.notify(410,notification)} catch(_:SecurityException) { /* Notification permission may have been revoked. */ }
    }
}
class MedicationReminderReceiver:BroadcastReceiver() {
    override fun onReceive(context:Context,intent:Intent) {
        val store=AppStore(context)
        val scheduled=intent.getStringExtra("scheduled")?.let {runCatching {LocalDateTime.parse(it)}.getOrNull()}
        if(intent.action=="com.lilyly.MEDICATION" && scheduled!=null && pendingMedicationReminder(store.medications,store.medicationLogs,scheduled)) MedicationReminders.notify(context)
        MedicationReminders.schedule(context,store.medications,store.medicationLogs)
    }
}
