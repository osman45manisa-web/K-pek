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
    private int petSize = 170;
    private int walkSpeed = 26;
    private int activity = 2;

    private int dp(int n) {
        return Math.round(
            n * getResources()
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
        android.content.SharedPreferences p =
            getSharedPreferences(
                "findik_prefs",
                MODE_PRIVATE
            );

        petSize =
            p.getInt(
                "size",
                170
            );

        walkSpeed =
            p.getInt(
                "speed",
                26
            );

        activity =
            p.getInt(
                "activity",
                2
            );
    }

    @Override
    public int onStartCommand(
        Intent intent,
        int flags,
        int startId
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
            if (
                attached &&
                params != null
            ) {
                params.width =
                    dp(petSize);

                params.height =
                    dp(
                        Math.round(
                            petSize * 0.8f
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

        createNotification();

        if (!attached) {
            attachPet();
        }

        return START_NOT_STICKY;
    }

    private void createNotification() {

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

        PendingIntent stop =
            PendingIntent.getService(
                this,
                1,
                new Intent(
                    this,
                    PetService.class
                ).setAction("STOP"),
                PendingIntent.FLAG_IMMUTABLE |
                PendingIntent.FLAG_UPDATE_CURRENT
            );

        Notification n =
            new Notification.Builder(
                this,
                "pet"
            )
            .setSmallIcon(
                android.R.drawable
                    .ic_menu_compass
            )
            .setContentTitle(
                "Fındık yanında"
            )
            .setContentText(
                "Dokun, sürükle ve oyna."
            )
            .addAction(
                new Notification.Action.Builder(
                    null,
                    "Kapat",
                    stop
                ).build()
            )
            .setOngoing(true)
            .build();

        if (
            Build.VERSION.SDK_INT >= 34
        ) {
            startForeground(
                1,
                n,
                ServiceInfo
                    .FOREGROUND_SERVICE_TYPE_SPECIAL_USE
            );
        } else {
            startForeground(
                1,
                n
            );
        }
    }

    private void attachPet() {

        pet =
            new PetView(this);

        params =
            new WindowManager.LayoutParams(
                dp(petSize),
                dp(
                    Math.round(
                        petSize * 0.8f
                    )
                ),
                WindowManager.LayoutParams
                    .TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams
                    .FLAG_NOT_FOCUSABLE |
                WindowManager.LayoutParams
                    .FLAG_NOT_TOUCH_MODAL,
                PixelFormat.TRANSLUCENT
            );

        params.gravity =
            Gravity.TOP |
            Gravity.LEFT;

        params.x =
            Math.max(
                0,
                screenW() -
                params.width -
                dp(16)
            );

        params.y =
            Math.max(
                0,
                screenH() -
                params.height -
                dp(90)
            );

        position =
            params.x;

        pet.setOnTouchListener(
            new View.OnTouchListener() {

                float downX;
                float downY;

                int startX;
                int startY;

                boolean moved;

                @Override
                public boolean onTouch(
                    View v,
                    MotionEvent e
                ) {

                    switch(
                        e.getActionMasked()
                    ) {

                        case MotionEvent.ACTION_DOWN:

                            dragging =
                                true;

                            moved =
                                false;

                            downX =
                                e.getRawX();

                            downY =
                                e.getRawY();

                            startX =
                                params.x;

                            startY =
                                params.y;

                            return true;

                        case MotionEvent.ACTION_MOVE:

                            float dx =
                                e.getRawX() -
                                downX;

                            float dy =
                                e.getRawY() -
                                downY;

                            if (
                                Math.abs(dx) +
                                Math.abs(dy) >
                                dp(8)
                            ) {
                                moved =
                                    true;
                            }

                            params.x =
                                Math.max(
                                    0,
                                    Math.min(
                                        screenW() -
                                        params.width,
                                        startX +
                                        (int)dx
                                    )
                                );

                            params.y =
                                Math.max(
                                    0,
                                    Math.min(
                                        screenH() -
                                        params.height,
                                        startY +
                                        (int)dy
                                    )
                                );

                            position =
                                params.x;

                            update();

                            return true;

                        case MotionEvent.ACTION_UP:

                            dragging =
                                false;

                            if (!moved) {
                                react();
                            }

                            return true;
                    }

                    return false;
                }
            }
        );

        try {
            wm.addView(
                pet,
                params
            );

            attached =
                true;

        } catch (
            RuntimeException e
        ) {
            stopSelf();
            return;
        }

        last =
            SystemClock
                .uptimeMillis();

        scheduleNext();

        handler.post(
            tick
        );
    }

    private void react() {

        String[] moods = {
            "sniff",
            "hop",
            "look",
            "rest"
        };

        String mood =
            moods[
                interaction %
                moods.length
            ];

        interaction++;

        setMood(
            mood,
            mood.equals("rest")
                ? 4000
                : 2200
        );
    }

    private void setMood(
        String mood,
        long duration
    ) {

        pet.mood =
            mood;

        moodStart =
            SystemClock
                .uptimeMillis();

        moodUntil =
            moodStart +
            duration;
    }

    private void scheduleNext() {

        long now =
            SystemClock
                .uptimeMillis();

        int min;
        int max;

        if (activity == 0) {
            min = 15000;
            max = 25000;
        } else if (activity == 1) {
            min = 10000;
            max = 17000;
        } else {
            min = 6000;
            max = 12000;
        }

        nextBehavior =
            now +
            min +
            random.nextInt(
                max -
                min +
                1
            );
    }

    private void randomBehavior() {

        int n =
            random.nextInt(4);

        if (n == 0) {
            setMood(
                "sniff",
                2200
            );
        } else if (n == 1) {
            setMood(
                "look",
                2200
            );
        } else if (n == 2) {
            setMood(
                "rest",
                3800
            );
        } else {
            setMood(
                "hop",
                1800
            );
        }

        scheduleNext();
    }

    private void update() {

        if (
            !attached ||
            params == null
        ) {
            return;
        }

        try {
            wm.updateViewLayout(
                pet,
                params
            );
        } catch (
            RuntimeException e
        ) {
            stopSelf();
        }
    }

    private final Runnable tick =
        new Runnable() {

            @Override
            public void run() {

                if (!attached) {
                    return;
                }

                long now =
                    SystemClock
                        .uptimeMillis();

                long dt =
                    Math.min(
                        100,
                        now -
                        last
                    );

                last =
                    now;

                if (!dragging) {

                    if (
                        !"walk".equals(
                            pet.mood
                        ) &&
                        now >= moodUntil
                    ) {
                        pet.mood =
                            "walk";

                        scheduleNext();
                    }

                    if (
                        "walk".equals(
                            pet.mood
                        ) &&
                        now >= nextBehavior
                    ) {
                        randomBehavior();
                    }

                    if (
                        "walk".equals(
                            pet.mood
                        )
                    ) {

                        walkTime +=
                            dt;

                        pet.frame =
                            (int)(
                                walkTime /
                                170
                            ) %
                            4;

                        int max =
                            Math.max(
                                0,
                                screenW() -
                                params.width
                            );

                        position +=
                            (
                                right
                                    ? 1
                                    : -1
                            ) *
                            dp(walkSpeed) *
                            dt /
                            1000f;

                        if (
                            position <= 0
                        ) {
                            position =
                                0;

                            right =
                                true;

                            setMood(
                                "look",
                                1000
                            );
                        }

                        if (
                            position >= max
                        ) {
                            position =
                                max;

                            right =
                                false;

                            setMood(
                                "look",
                                1000
                            );
                        }

                        params.x =
                            Math.round(
                                position
                            );

                        pet.facingRight =
                            right;

                    } else {

                        pet.phase =
                            (
                                now -
                                moodStart
                            ) /
                            (float)
                            Math.max(
                                1,
                                moodUntil -
                                moodStart
                            );
                    }
                }

                update();

                pet.invalidate();

                handler.postDelayed(
                    this,
                    50
                );
            }
        };

    @Override
    public void onDestroy() {

        handler.removeCallbacksAndMessages(
            null
        );

        if (
            attached &&
            pet != null
        ) {
            try {
                wm.removeView(
                    pet
                );
            } catch (
                RuntimeException ignored
            ) {}

            attached =
                false;
        }

        stopForeground(
            STOP_FOREGROUND_REMOVE
        );

        super.onDestroy();
    }

    @Override
    public IBinder onBind(
        Intent intent
    ) {
        return null;
    }
}
