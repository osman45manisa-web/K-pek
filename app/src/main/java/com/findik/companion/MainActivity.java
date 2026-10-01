package com.findik.companion;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.*;
import android.provider.Settings;
import android.view.Gravity;
import android.widget.*;

public final class MainActivity extends Activity {

    private static final String PREFS = "findik_prefs";

    private final Handler handler =
        new Handler(Looper.getMainLooper());

    private PetView preview;
    private TextView permissionState;
    private TextView sizeValue;
    private TextView speedValue;
    private TextView activityValue;

    private int frame;

    private final Runnable animate = new Runnable() {
        @Override
        public void run() {

            if (preview != null) {
                preview.frame = frame++ % 8;

                preview.phase =
                    (SystemClock.uptimeMillis() % 2000L)
                    / 2000f;

                preview.invalidate();
            }

            handler.postDelayed(this, 170);
        }
    };

    private int dp(int n) {
        return Math.round(
            n *
            getResources()
                .getDisplayMetrics()
                .density
        );
    }

    private TextView text(
        String value,
        int sp,
        boolean bold
    ) {

        TextView v = new TextView(this);

        v.setText(value);
        v.setTextSize(sp);

        v.setTextColor(
            Color.rgb(
                48,
                41,
                34
            )
        );

        if (bold) {
            v.setTypeface(
                Typeface.DEFAULT,
                Typeface.BOLD
            );
        }

        return v;
    }

    private GradientDrawable bg(
        int color,
        int radiusDp
    ) {

        GradientDrawable d =
            new GradientDrawable();

        d.setColor(color);

        d.setCornerRadius(
            dp(radiusDp)
        );

        return d;
    }

    private LinearLayout card() {

        LinearLayout c =
            new LinearLayout(this);

        c.setOrientation(
            LinearLayout.VERTICAL
        );

        c.setPadding(
            dp(18),
            dp(16),
            dp(18),
            dp(16)
        );

        c.setBackground(
            bg(
                Color.rgb(
                    255,
                    252,
                    247
                ),
                22
            )
        );

        LinearLayout.LayoutParams p =
            new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            );

        p.setMargins(
            0,
            0,
            0,
            dp(14)
        );

        c.setLayoutParams(p);

        return c;
    }

    private Button button(
        String label,
        int color
    ) {

        Button b =
            new Button(this);

        b.setText(label);

        b.setTextAllCaps(false);

        b.setTextSize(16);

        b.setTextColor(
            Color.WHITE
        );

        b.setBackground(
            bg(
                color,
                18
            )
        );

        LinearLayout.LayoutParams p =
            new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(52)
            );

        p.setMargins(
            0,
            dp(7),
            0,
            0
        );

        b.setLayoutParams(p);

        return b;
    }

    @Override
    public void onCreate(
        Bundle savedInstanceState
    ) {

        super.onCreate(
            savedInstanceState
        );

        SharedPreferences prefs =
            getSharedPreferences(
                PREFS,
                MODE_PRIVATE
            );

        LinearLayout page =
            new LinearLayout(this);

        page.setOrientation(
            LinearLayout.VERTICAL
        );

        page.setPadding(
            dp(20),
            dp(24),
            dp(20),
            dp(28)
        );

        page.setBackgroundColor(
            Color.rgb(
                245,
                239,
                229
            )
        );

        TextView title =
            text(
                "Fındık",
                34,
                true
            );

        title.setGravity(
            Gravity.CENTER
        );

        page.addView(title);

        TextView subtitle =
            text(
                "Ekranda yaşayan küçük arkadaşın",
                15,
                false
            );

        subtitle.setGravity(
            Gravity.CENTER
        );

        subtitle.setTextColor(
            Color.rgb(
                104,
                89,
                74
            )
        );

        LinearLayout.LayoutParams subtitleLp =
            new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            );

        subtitleLp.setMargins(
            0,
            dp(2),
            0,
            dp(14)
        );

        subtitle.setLayoutParams(
            subtitleLp
        );

        page.addView(
            subtitle
        );

        preview =
            new PetView(this);

        preview.mood =
            "walk";

        LinearLayout.LayoutParams previewLp =
            new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(205)
            );

        previewLp.setMargins(
            0,
            0,
            0,
            dp(14)
        );

        preview.setLayoutParams(
            previewLp
        );

        preview.setBackground(
            bg(
                Color.rgb(
                    238,
                    229,
                    215
                ),
                24
            )
        );

        page.addView(
            preview
        );

        LinearLayout statusCard =
            card();

        TextView statusTitle =
            text(
                "Durum",
                17,
                true
            );

        permissionState =
            text(
                "",
                14,
                false
            );

        permissionState.setPadding(
            0,
            dp(8),
            0,
            0
        );

        statusCard.addView(
            statusTitle
        );

        statusCard.addView(
            permissionState
        );

        page.addView(
            statusCard
        );

        LinearLayout settingsCard =
            card();

        settingsCard.addView(
            text(
                "Fındık ayarları",
                18,
                true
            )
        );

        int savedSize =
            prefs.getInt(
                "size",
                170
            );

        int savedSpeed =
            prefs.getInt(
                "speed",
                26
            );

        int savedActivity =
            prefs.getInt(
                "activity",
                2
            );

        sizeValue =
            text(
                "",
                14,
                false
            );

        settingsCard.addView(
            sizeValue
        );

        SeekBar size =
            new SeekBar(this);

        size.setMax(120);

        size.setProgress(
            savedSize - 120
        );

        settingsCard.addView(
            size
        );

        speedValue =
            text(
                "",
                14,
                false
            );

        speedValue.setPadding(
            0,
            dp(12),
            0,
            0
        );

        settingsCard.addView(
            speedValue
        );

        SeekBar speed =
            new SeekBar(this);

        speed.setMax(50);

        speed.setProgress(
            savedSpeed - 10
        );

        settingsCard.addView(
            speed
        );

        activityValue =
            text(
                "",
                14,
                false
            );

        activityValue.setPadding(
            0,
            dp(12),
            0,
            0
        );

        settingsCard.addView(
            activityValue
        );

        SeekBar activity =
            new SeekBar(this);

        activity.setMax(2);

        activity.setProgress(
            savedActivity
        );

        settingsCard.addView(
            activity
        );

        page.addView(
            settingsCard
        );

        Runnable updateLabels =
            () -> {

                int currentSize =
                    120 +
                    size.getProgress();

                int currentSpeed =
                    10 +
                    speed.getProgress();

                int currentActivity =
                    activity.getProgress();

                sizeValue.setText(
                    "Boyut: " +
                    currentSize +
                    " dp"
                );

                speedValue.setText(
                    "Yürüme hızı: " +
                    currentSpeed
                );

                String activityText =
                    currentActivity == 0
                        ? "Sakin"
                        : currentActivity == 1
                            ? "Normal"
                            : "Hareketli";

                activityValue.setText(
                    "Davranış sıklığı: " +
                    activityText
                );
            };

        updateLabels.run();

        SeekBar.OnSeekBarChangeListener listener =
            new SeekBar.OnSeekBarChangeListener() {

                @Override
                public void onProgressChanged(
                    SeekBar seekBar,
                    int progress,
                    boolean fromUser
                ) {
                    updateLabels.run();
                }

                @Override
                public void onStartTrackingTouch(
                    SeekBar seekBar
                ) {}

                @Override
                public void onStopTrackingTouch(
                    SeekBar seekBar
                ) {

                    prefs.edit()
                        .putInt(
                            "size",
                            120 +
                            size.getProgress()
                        )
                        .putInt(
                            "speed",
                            10 +
                            speed.getProgress()
                        )
                        .putInt(
                            "activity",
                            activity.getProgress()
                        )
                        .apply();

                    Intent update =
                        new Intent(
                            MainActivity.this,
                            PetService.class
                        );

                    update.setAction(
                        "UPDATE"
                    );

                    startService(
                        update
                    );
                }
            };

        size.setOnSeekBarChangeListener(
            listener
        );

        speed.setOnSeekBarChangeListener(
            listener
        );

        activity.setOnSeekBarChangeListener(
            listener
        );

        LinearLayout actions =
            card();

        actions.addView(
            text(
                "Kontroller",
                18,
                true
            )
        );

        Button permission =
            button(
                "Ekran üzerinde gösterme izni",
                Color.rgb(
                    111,
                    84,
                    65
                )
            );

        permission.setOnClickListener(
            v -> startActivity(
                new Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse(
                        "package:" +
                        getPackageName()
                    )
                )
            )
        );

        actions.addView(
            permission
        );

        Button start =
            button(
                "Fındık'ı başlat",
                Color.rgb(
                    71,
                    126,
                    87
                )
            );

        start.setOnClickListener(
            v -> {

                if (!Settings.canDrawOverlays(this)) {

                    Toast.makeText(
                        this,
                        "Önce ekran üzerinde gösterme iznini aç.",
                        Toast.LENGTH_LONG
                    ).show();

                    return;
                }

                if (
                    Build.VERSION.SDK_INT >= 33 &&
                    checkSelfPermission(
                        Manifest.permission.POST_NOTIFICATIONS
                    ) !=
                    PackageManager.PERMISSION_GRANTED
                ) {

                    requestPermissions(
                        new String[] {
                            Manifest.permission.POST_NOTIFICATIONS
                        },
                        1
                    );
                }

                prefs.edit()
                    .putInt(
                        "size",
                        120 +
                        size.getProgress()
                    )
                    .putInt(
                        "speed",
                        10 +
                        speed.getProgress()
                    )
                    .putInt(
                        "activity",
                        activity.getProgress()
                    )
                    .apply();

                startForegroundService(
                    new Intent(
                        this,
                        PetService.class
                    )
                );

                Toast.makeText(
                    this,
                    "Fındık başladı.",
                    Toast.LENGTH_SHORT
                ).show();
            }
        );

        actions.addView(
            start
        );

        Button stop =
            button(
                "Fındık'ı kapat",
                Color.rgb(
                    155,
                    73,
                    67
                )
            );

        stop.setOnClickListener(
            v -> stopService(
                new Intent(
                    this,
                    PetService.class
                )
            )
        );

        actions.addView(
            stop
        );

        page.addView(
            actions
        );

        TextView hint =
            text(
                "İpucu: Fındık'a dokununca farklı tepkiler verir. " +
                "Sürükleyerek istediğin yere taşıyabilirsin. " +
                "Bir süre dokunmazsan kendi kendine durur, " +
                "bakınır, dinlenir ve yeniden yürür.",
                13,
                false
            );

        hint.setTextColor(
            Color.rgb(
                110,
                98,
                85
            )
        );

        hint.setGravity(
            Gravity.CENTER
        );

        page.addView(
            hint
        );

        ScrollView scroll =
            new ScrollView(this);

        scroll.setFillViewport(
            true
        );

        scroll.addView(
            page
        );

        setContentView(
            scroll
        );
    }

    private void refreshPermissionState() {

        if (permissionState == null)
            return;

        boolean ok =
            Settings.canDrawOverlays(
                this
            );

        permissionState.setText(
            ok
                ? "✓ Ekran üzerinde gösterme izni açık"
                : "○ Ekran üzerinde gösterme izni kapalı"
        );

        permissionState.setTextColor(
            ok
                ? Color.rgb(
                    49,
                    122,
                    72
                )
                : Color.rgb(
                    160,
                    89,
                    55
                )
        );
    }

    @Override
    protected void onResume() {

        super.onResume();

        refreshPermissionState();

        handler.removeCallbacks(
            animate
        );

        handler.post(
            animate
        );
    }

    @Override
    protected void onPause() {

        handler.removeCallbacks(
            animate
        );

        super.onPause();
    }
}
