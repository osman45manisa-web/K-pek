package com.findik.companion;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.*;
import android.provider.Settings;
import android.graphics.Color;
import android.view.Gravity;
import android.widget.*;

public final class MainActivity extends Activity {
    private final Handler handler = new Handler(Looper.getMainLooper());
    private PetView preview;
    private int frame;
    private final Runnable animate = new Runnable() { public void run() {
        preview.frame = frame++ % 8; preview.invalidate(); handler.postDelayed(this, 170);
    }};
    private int dp(int n) { return Math.round(n * getResources().getDisplayMetrics().density); }
    @Override public void onCreate(Bundle saved) {
        super.onCreate(saved);
        LinearLayout layout = new LinearLayout(this); layout.setOrientation(LinearLayout.VERTICAL);
        layout.setGravity(Gravity.CENTER); layout.setPadding(dp(24),dp(24),dp(24),dp(24));
        layout.setBackgroundColor(Color.rgb(245,239,229));
        TextView title = new TextView(this); title.setText("Fındık"); title.setTextSize(32); title.setGravity(Gravity.CENTER); layout.addView(title);
        preview = new PetView(this); layout.addView(preview,new LinearLayout.LayoutParams(dp(260),dp(190)));
        TextView info = new TextView(this);
        info.setText("Sakin küçük ekran arkadaşın.\n\nDokun: koklama, zıplama veya dinlenme.\nSürükle: yerini değiştir.\nKendiliğinden yeniden yürümeye başlar.\n\nBaşlatmak için diğer uygulamaların üzerinde gösterme iznini aç.");
        info.setTextSize(16); info.setGravity(Gravity.CENTER); layout.addView(info);
        Button permission = new Button(this); permission.setText("Ekran üzerinde gösterme izni");
        permission.setOnClickListener(v -> startActivity(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,Uri.parse("package:"+getPackageName())))); layout.addView(permission);
        Button start = new Button(this); start.setText("Fındık’ı başlat"); start.setOnClickListener(v -> {
            if (!Settings.canDrawOverlays(this)) { Toast.makeText(this,"Önce ekran üzerinde gösterme iznini aç.",Toast.LENGTH_LONG).show(); return; }
            if (Build.VERSION.SDK_INT >= 33 && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)!=PackageManager.PERMISSION_GRANTED)
                requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, 1);
            startForegroundService(new Intent(this,PetService.class));
            Toast.makeText(this,"Fındık hazır. Ana ekrana dönebilirsin.",Toast.LENGTH_LONG).show();
        }); layout.addView(start);
        Button stop = new Button(this); stop.setText("Fındık’ı kapat"); stop.setOnClickListener(v -> stopService(new Intent(this,PetService.class))); layout.addView(stop);
        ScrollView scroll = new ScrollView(this); scroll.setFillViewport(true); scroll.addView(layout); setContentView(scroll);
    }
    @Override protected void onResume() { super.onResume(); handler.post(animate); }
    @Override protected void onPause() { handler.removeCallbacks(animate); super.onPause(); }
}
