package com.tec.medxpert.service;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import com.tec.medxpert.MainApplication.MainActivity;
import com.tec.medxpert.R;

public class AppointmentNotificationReceiver extends BroadcastReceiver {
    private static final String CHANNEL_ID = "APPOINTMENT_CHANNEL";
    private static final String CHANNEL_NAME = "Appointment reminder";

    @Override
    public void onReceive(Context context, Intent intent) {
        String patientName = intent.getStringExtra("patient_name");
        String appointmentDate = intent.getStringExtra("appointment_date");
        String appointmentTime = intent.getStringExtra("appointment_time");
        String notificationType = intent.getStringExtra("notification_type");

        createNotificationChannel(context);
        showNotification(context, patientName, appointmentDate, appointmentTime, notificationType);

    }

    private void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription(context.getString(R.string.appointment_channel_description));

            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void showNotification(Context context, String patientName, String date, String time, String type) {
        String title = context.getString(R.string.appointment_reminder_title);
        String message;

        if ("24_hours".equals(type)) {
            message = context.getString(R.string.appointment_tomorrow_message, patientName, time);
        } else {
            message = context.getString(R.string.appointment_in_4_hours_message, patientName, time);
        }

        Intent intent = new Intent(context.getApplicationContext(), MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(title)
                .setContentText(message)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        int notificationId = (int) System.currentTimeMillis();
        notificationManager.notify(notificationId, builder.build());
    }
}
