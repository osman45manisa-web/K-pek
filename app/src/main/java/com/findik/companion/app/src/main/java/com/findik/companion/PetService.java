package com.findik.companion;

import android.app.*;
import android.content.*;
import android.content.pm.ServiceInfo;
import android.graphics.PixelFormat;
import android.os.*;
import android.provider.Settings;
import android.view.*;

public final class PetService extends Service {
    private final Handler handler = new Handler(Looper.getMainLooper());
    private WindowManager wm;
    private WindowManager.LayoutParams params;
    private PetView pet;
    private boolean attached, dragging, right;
    private float position;
    private long last, walkTime, moodStart, moodUntil, nextRest;
    private int interaction;
    private int dp(int n) { return Math.round(n*getResources().getDisplayMetrics().density); }
    private int screenW() { return getResources().getDisplayMetrics().widthPixels; }
    private int screenH() { return getResources().getDisplayMetrics().heightPixels; }

    @Override public void onCreate() {
        super.onCreate();
        wm=(WindowManager)getSystemService(WINDOW_SERVICE);
    }

    @Override public int onStartCommand(Intent intent,int flags,int id) {
        if (intent!=null && "STOP".equals(intent.getAction())) {
            stopSelf();
            return START_NOT_STICKY;
        }

        if (!Settings.canDrawOverlays(this)) {
            stopSelf();
            return START_NOT_STICKY;
        }

        NotificationManager nm=(NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        nm.createNotificationChannel(
            new NotificationChannel(
                "pet",
                "Fındık ekran arkadaşı",
                NotificationManager.IMPORTANCE_LOW
            )
        );

        PendingIntent open=PendingIntent.getActivity(
            this,
            0,
            new Intent(this,MainActivity.class),
            PendingIntent.FLAG_IMMUTABLE|PendingIntent.FLAG_UPDATE_CURRENT
        );

        PendingIntent stop=PendingIntent.getService(
            this,
            1,
            new Intent(this,PetService.class).setAction("STOP"),
            PendingIntent.FLAG_IMMUTABLE|PendingIntent.FLAG_UPDATE_CURRENT
        );

        Notification notification=new Notification.Builder(this,"pet")
            .setSmallIcon(android.R.drawable.ic_menu_compass)
            .setContentTitle("Fındık yanında")
            .setContentText("Dokunarak oyna, sürükleyerek taşı.")
            .setContentIntent(open)
            .addAction(new Notification.Action.Builder(null,"Kapat",stop).build())
            .setOngoing(true)
            .build();

        if (Build.VERSION.SDK_INT>=34)
            startForeground(
                1,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
            );
        else
            startForeground(1,notification);

        if (!attached) attach();
        return START_NOT_STICKY;
    }

    private void attach() {
        pet=new PetView(this);

        params=new WindowManager.LayoutParams(
            dp(170),
            dp(135),
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE|
            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
            PixelFormat.TRANSLUCENT
        );

        params.gravity=Gravity.TOP|Gravity.LEFT;
        params.x=Math.max(0,screenW()-params.width-dp(16));
        params.y=Math.max(0,screenH()-params.height-dp(90));
        position=params.x;

        pet.setOnTouchListener(new View.OnTouchListener() {
            float downX,downY;
            int originalX,originalY;
            boolean moved;

            public boolean onTouch(View v,android.view.MotionEvent e) {
                switch(e.getActionMasked()) {
                    case MotionEvent.ACTION_DOWN:
                        dragging=true;
                        moved=false;
                        downX=e.getRawX();
                        downY=e.getRawY();
                        originalX=params.x;
                        originalY=params.y;
                        return true;

                    case MotionEvent.ACTION_MOVE:
                        float dx=e.getRawX()-downX;
                        float dy=e.getRawY()-downY;

                        if (Math.abs(dx)+Math.abs(dy)>dp(8))
                            moved=true;

                        params.x=Math.max(
                            0,
                            Math.min(
                                screenW()-params.width,
                                originalX+(int)dx
                            )
                        );

                        params.y=Math.max(
                            0,
                            Math.min(
                                screenH()-params.height,
                                originalY+(int)dy
                            )
                        );

                        position=params.x;
                        update();
                        return true;

                    case MotionEvent.ACTION_UP:
                        dragging=false;
                        if (!moved) {
                            pet.performClick();
                            react();
                        }
                        return true;

                    case MotionEvent.ACTION_CANCEL:
                        dragging=false;
                        return true;
                }

                return false;
            }
        });

        try {
            wm.addView(pet,params);
            attached=true;
        } catch (RuntimeException failure) {
            stopSelf();
            return;
        }

        last=SystemClock.uptimeMillis();
        nextRest=last+18000;
        handler.post(tick);
    }

    private void react() {
        String[] moods={"sniff","hop","rest"};
        setMood(
            moods[interaction++%3],
            interaction%3==0 ? 5000 : 2300
        );
    }

    private void setMood(String mood,long duration) {
        pet.mood=mood;
        pet.frame=1;
        moodStart=SystemClock.uptimeMillis();
        moodUntil=moodStart+duration;
    }

    private void update() {
        if (!attached) return;

        try {
            wm.updateViewLayout(pet,params);
        } catch (RuntimeException failure) {
            stopSelf();
        }
    }

    private final Runnable tick=new Runnable() {
        public void run() {
            if (!attached) return;

            long now=SystemClock.uptimeMillis();
            long dt=Math.min(100,now-last);
            last=now;

            if (!Settings.canDrawOverlays(PetService.this)) {
                stopSelf();
                return;
            }

            if (!dragging) {
                if (!pet.mood.equals("walk") && now>=moodUntil) {
                    pet.mood="walk";
                    nextRest=now+18000;
                }

                if (pet.mood.equals("walk") && now>=nextRest)
                    setMood("rest",4000);

                if (pet.mood.equals("walk")) {
                    walkTime+=dt;
                    pet.frame=(int)(walkTime/170)%8;

                    int max=Math.max(0,screenW()-params.width);
                    position+=(right?1:-1)*dp(22)*dt/1000f;

                    if (position<=0) {
                        position=0;
                        right=true;
                    }

                    if (position>=max) {
                        position=max;
                        right=false;
                    }

                    params.x=Math.round(position);
                    pet.facingRight=right;
                } else {
                    pet.phase=(now-moodStart)/(float)Math.max(
                        1,
                        moodUntil-moodStart
                    );
                }
            }

            params.x=Math.max(
                0,
                Math.min(
                    Math.max(0,screenW()-params.width),
                    params.x
                )
            );

            params.y=Math.max(
                0,
                Math.min(
                    Math.max(0,screenH()-params.height),
                    params.y
                )
            );

            update();
            pet.invalidate();
            handler.postDelayed(this,50);
        }
    };

    @Override public void onDestroy() {
        handler.removeCallbacksAndMessages(null);

        if (attached) {
            try {
                wm.removeView(pet);
            } catch (RuntimeException ignored) {}

            attached=false;
        }

        stopForeground(STOP_FOREGROUND_REMOVE);
        super.onDestroy();
    }

    @Override public IBinder onBind(Intent intent) {
        return null;
    }
}
