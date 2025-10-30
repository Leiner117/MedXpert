package com.tec.medxpert.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class AppointmentNotificationScheduler {
    public static void scheduleAppointmentNotifications(Context context,
                                                        String patientName, String appointmentDate,
                                                        String appointmentTime, String appointmentId) {

        try {
            long appointmentTimestamp = convertToTimestamp(appointmentDate, appointmentTime);

            int appointmentIdHash = appointmentId.hashCode();

            if (appointmentIdHash < 0) {
                appointmentIdHash = Math.abs(appointmentIdHash);
            }

            if (appointmentTimestamp > System.currentTimeMillis()) {
                long notification24h = appointmentTimestamp - (24 * 60 * 60 * 1000);
                if (notification24h > System.currentTimeMillis()) {
                    scheduleNotification(context, patientName, appointmentDate,
                            appointmentTime, notification24h, "24_hours",
                            appointmentIdHash * 10 + 1, appointmentId);
                }

                long notification4h = appointmentTimestamp - (4 * 60 * 60 * 1000);
                if (notification4h > System.currentTimeMillis()) {
                    scheduleNotification(context, patientName, appointmentDate,
                            appointmentTime, notification4h, "4_hours",
                            appointmentIdHash * 10 + 2, appointmentId);
                }
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    private static long convertToTimestamp(String date, String time) throws ParseException {

        Log.d("NotiDebug", "Fecha original: " + date + " " + time);

        String dateTimeString = date + " " + time;
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.getDefault());
        Date dateTime = sdf.parse(dateTimeString);
        return dateTime != null ? dateTime.getTime() : 0;
    }

    private static void scheduleNotification(Context context, String patientName,
                                             String date, String time, long triggerTime,
                                             String type, int requestCode, String appointmentId) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        Intent intent = new Intent(context, AppointmentNotificationReceiver.class);
        intent.putExtra("patient_name", patientName);
        intent.putExtra("appointment_date", date);
        intent.putExtra("appointment_time", time);
        intent.putExtra("notification_type", type);
        intent.putExtra("appointment_id", appointmentId);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context, requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent);
        } else {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent);
        }

        Log.d("NotiDebug", "Notificación programada para: " + new Date(triggerTime));

    }

    public static void cancelAppointmentNotifications(Context context, String appointmentId) {
        int appointmentIdHash = appointmentId.hashCode();
        if (appointmentIdHash < 0) {
            appointmentIdHash = Math.abs(appointmentIdHash);
        }

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        Intent intent24h = new Intent(context, AppointmentNotificationReceiver.class);
        PendingIntent pendingIntent24h = PendingIntent.getBroadcast(
                context, appointmentIdHash * 10 + 1, intent24h,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        alarmManager.cancel(pendingIntent24h);

        Intent intent4h = new Intent(context, AppointmentNotificationReceiver.class);
        PendingIntent pendingIntent4h = PendingIntent.getBroadcast(
                context, appointmentIdHash * 10 + 2, intent4h,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        alarmManager.cancel(pendingIntent4h);
    }
}
