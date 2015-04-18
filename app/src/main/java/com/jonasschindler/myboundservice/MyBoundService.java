package com.jonasschindler.myboundservice;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

public class MyBoundService extends Service {

    // Initialises the calculation times
    public String time;
    private long startTime;
    private long pauseTime = 0;
    private long resumeTime = 0;

    // de-/activates the loop for the stopWatch thread
    public boolean running = true;

    // id to identify the notification when updating it
    public static final int nId = 1;
    NotificationManager notificationManager;

    private final IBinder binder = new MyBinder();

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    // binder connection makes methods of this service class directly available for the activity
    public class MyBinder extends Binder {

        void start() {
            MyBoundService.this.startTimer();
        }
        void pause() {
            MyBoundService.this.pauseTimer();
        }

        void startNotify() {
            MyBoundService.this.startNotification();
        }

        void reset() {
            MyBoundService.this.resetTimer();
        }

        String getString() {
            return time;
        }
    }

    // called when start button is clicked
    public void startTimer() {
        // differentiates between start and resume
        // updates the time when resume was clicked
        if (!running) {
            resumeTime = resumeTime + System.currentTimeMillis() - pauseTime;
            running = true;
            stopWatch();
        }
        else {
            // saves current time when start is clicked and starts the stopWatch thread
            startTime = System.currentTimeMillis();
            stopWatch();
        }
    }

    // called when pause button is clicked
    public void pauseTimer() {

        // saves the current time when pause is clicked
        pauseTime = System.currentTimeMillis();

        // deactivates the stopWatch Thread
        running = false;

    }

    // called when reset is clicked
    public void resetTimer() {
        pauseTime = 0;
        resumeTime = 0;
        running = true;
        killNotification();
    }

    // Thread that calculates the time from the moment the start button is clicked
    public void stopWatch() {

        new Thread(new Runnable() {
            public void run() {
                while (running) {
                    // calculates the actual elapsed time of the stopWatch
                    long elapsed = System.currentTimeMillis() - startTime - resumeTime;
                    long millis = elapsed % 1000;
                    long secs = elapsed / 1000;
                    secs = secs % 60;
                    long mins = (elapsed / 1000) / 60;
                    mins = mins % 60;
                    time = String.format("%02d:%02d:%03d", mins, secs, millis);

                    // updates the notification with the latest time
                    updateNotification();
                    try {
                        Thread.sleep(1);
                    } catch (InterruptedException e) {
                        return;
                    }
                }
            }
        }).start();
    }

    // called when startService is called in the activity
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        return START_STICKY;
    }

    // creates the notification
    public void startNotification() {
        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setContentTitle(this.time)
                .setSmallIcon(R.drawable.stopwatchnotification)
                .setWhen(0)
                .setContentIntent(PendingIntent.getActivity(this, 0,
                        new Intent(this, MainActivity.class),0));
        notificationManager.notify(nId, builder.build());
    }

    // changes the title of the notification
    public void updateNotification() {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setContentTitle(time)
                .setSmallIcon(R.drawable.stopwatchnotification)
                .setAutoCancel(true)
                .setOngoing(running)
                .setOnlyAlertOnce(true)
                .setWhen(0)
                .setContentIntent(PendingIntent.getActivity(this, 0,
                        new Intent(this, MainActivity.class),0));
        notificationManager.notify(nId, builder.build());
    }

    // kills the notification
    public void killNotification() {
        notificationManager.cancel(nId);
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onRebind(Intent intent) {
        super.onRebind(intent);
    }

    // kills the notification when the service gets unbound
    @Override
    public boolean onUnbind(Intent intent) {
        killNotification();
        return super.onUnbind(intent);

    }

}
