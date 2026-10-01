package com.findik.companion;

import android.app.*;
import android.content.*;
import android.content.pm.ServiceInfo;
import android.graphics.PixelFormat;
import android.os.*;
import android.provider.Settings;
import android.view.*;

import java.util.Random;

public final class PetService extends Service {

    private static final String PREFS = "findik_prefs";

    private final Handler handler =
        new Handler(Looper.getMainLooper());

    private final Random random =
        new Random();

    private WindowManager wm;
    private WindowManager.LayoutParams params;
    private PetView pet;

    private boolean attached;
    private boolean dragging;
    private boolean right;

    private float position;

    private long last;
    private long walkTime;
    private long moodStart;
    private long moodUntil;
    private long nextBehavior;

    private int interaction;

    private int petSizeDp = 170;
    private int walkSpeed = 26;
    private int activityLevel = 2;

    private int dp(int n) {
        return Math.round(
            n *
            getResources()
                .getDisplayMetrics()
                .density
        );
    }

    private int screenW() {
        return getResources()
            .getDisplayMetrics()
            .widthPixels;
    }

    private int screenH() {
        return getResources()
            .getDisplayMetrics()
            .heightPixels;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        wm =
            (WindowManager)
            getSystemService(
                WINDOW_SERVICE
            );
    }

    private void readSettings() {

        SharedPreferences p =
            getSharedPreferences(
                PREFS,
                MODE_PRIVATE
            );

        petSizeDp =
            p.getInt(
                "size",
                170
            );

        walkSpeed =
            p.getInt(
                "speed",
                26
            );

        activityLevel =
            p.getInt(
                "activity",
                2
            );
    }

    @Override
    public int onStartCommand(
        Intent intent,
        int flags,
        int id
    ) {

        if (
            intent != null &&
            "STOP".equals(
                intent.getAction()
            )
        ) {

            stopSelf();

            return START_NOT_STICKY;
        }

        readSettings();

        if (
            intent != null &&
            "UPDATE".equals(
                intent.getAction()
            )
        ) {

            if (attached) {

                params.width =
                    dp(
                        petSizeDp
                    );

                params.height =
                    dp(
                        Math.round(
                            petSizeDp *
                            0.80f
                        )
                    );

                params.x =
                    Math.max(
                        0,
                        Math.min(
                            screenW() -
                            params.width,
                            params.x
                        )
                    );

                params.y =
                    Math.max(
                        0,
                        Math.min(
                            screenH() -
                            params.height,
                            params.y
                        )
                    );

                update();
            }

            return START_NOT_STICKY;
        }

        if (
            !Settings.canDrawOverlays(
                this
            )
        ) {

            stopSelf();

            return START_NOT_STICKY;
        }

        NotificationManager nm =
            (NotificationManager)
            getSystemService(
                NOTIFICATION_SERVICE
            );

        nm.createNotificationChannel(
            new NotificationChannel(
                "pet",
                "Fındık ekran arkadaşı",
                NotificationManager
                    .IMPORTANCE_LOW
            )
        );

        PendingIntent open =
            PendingIntent.getActivity(
                this,
                0,
                new Intent(
                    this,
                    MainActivity.class
                ),
                PendingIntent
                    .FLAG_IMMUTABLE |
                PendingIntent
                    .FLAG_UPDATE_CURRENT
            );

        PendingIntent stop =
            PendingIntent.getService(
                this,
                1,
                new Intent(
                    this,
                    PetService.class
                ).setAction(
                    "STOP"
                ),
                PendingIntent
                    .FLAG_IMMUTABLE |
                PendingIntent
                    .FLAG_UPDATE_CURRENT
           
