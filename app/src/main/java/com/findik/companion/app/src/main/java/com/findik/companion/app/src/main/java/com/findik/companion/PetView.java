package com.findik.companion;

import android.content.Context;
import android.graphics.*;
import android.view.View;

/** Draws the supplied eight-frame sheet without changing the original asset. */
public final class PetView extends View {
    private final Bitmap sheet;
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
    public int frame;
    public boolean facingRight;
    public String mood = "walk";
    public float phase;

    public PetView(Context context) {
        super(context);
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inScaled = false;
        sheet = BitmapFactory.decodeResource(getResources(), R.drawable.findik_walk, options);
        setContentDescription("Fındık: dokunarak hareket değiştir, sürükleyerek taşı");
    }

    @Override protected void onDraw(Canvas c) {
        super.onDraw(c);
        if (sheet == null) return;

        float w = getWidth(), h = getHeight();
        int cellW = sheet.getWidth()/4, cellH = sheet.getHeight()/2;
        int i = Math.floorMod(frame, 8);

        c.save();

        if (facingRight) {
            c.translate(w, 0);
            c.scale(-1, 1);
        }

        float bottom = h - 10;
        float drawH = w * cellH / cellW;

        if (mood.equals("hop"))
            c.translate(0, -Math.abs((float)Math.sin(phase * Math.PI * 2))*22);

        if (mood.equals("sniff"))
            c.rotate((float)Math.sin(phase * Math.PI * 4)*3, w*.65f, bottom);

        if (mood.equals("rest"))
            c.scale(1, .90f, w/2, bottom);

        Rect src = new Rect(
            i%4*cellW,
            i/4*cellH,
            (i%4+1)*cellW,
            (i/4+1)*cellH
        );

        c.drawBitmap(
            sheet,
            src,
            new RectF(0, bottom-drawH, w, bottom),
            paint
        );

        c.restore();

        if (!mood.equals("walk")) {
            paint.setColor(Color.rgb(79, 62, 44));
            paint.setTextSize(
                14 * getResources().getDisplayMetrics().scaledDensity
            );
            paint.setTextAlign(Paint.Align.CENTER);

            String label =
                mood.equals("sniff") ? "Kokluyorum…" :
                mood.equals("hop") ? "Merhaba!" :
                "Dinleniyorum…";

            c.drawText(
                label,
                w/2,
                20 * getResources().getDisplayMetrics().density,
                paint
            );
        }
    }
}
