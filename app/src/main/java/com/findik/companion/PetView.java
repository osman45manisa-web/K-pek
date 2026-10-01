package com.findik.companion;

import android.content.Context;
import android.graphics.*;
import android.view.View;

public final class PetView extends View {

    private final Bitmap sheet;
    private final Paint paint =
        new Paint(
            Paint.ANTI_ALIAS_FLAG |
            Paint.FILTER_BITMAP_FLAG
        );

    public int frame = 0;
    public boolean facingRight = false;
    public String mood = "walk";
    public float phase = 0f;

    public PetView(Context context) {
        super(context);

        BitmapFactory.Options o =
            new BitmapFactory.Options();

        o.inScaled = false;

        sheet =
            BitmapFactory.decodeResource(
                getResources(),
                R.drawable.findik_walk,
                o
            );
    }

    private int currentFrame() {

        if ("sniff".equals(mood))
            return 4;

        if ("look".equals(mood))
            return 5;

        if ("rest".equals(mood))
            return 6;

        if ("hop".equals(mood))
            return 7;

        return frame % 4;
    }

    @Override
    protected void onDraw(Canvas c) {

        super.onDraw(c);

        if (sheet == null)
            return;

        float w = getWidth();
        float h = getHeight();

        int cellW =
            sheet.getWidth() / 4;

        int cellH =
            sheet.getHeight() / 2;

        int i =
            currentFrame();

        int col = i % 4;
        int row = i / 4;

        Rect src =
            new Rect(
                col * cellW,
                row * cellH,
                (col + 1) * cellW,
                (row + 1) * cellH
            );

        float bottom =
            h - 8;

        float drawH =
            w * cellH / cellW;

        RectF dst =
            new RectF(
                0,
                bottom - drawH,
                w,
                bottom
            );

        c.save();

        if (facingRight) {

            c.translate(
                w,
                0
            );

            c.scale(
                -1f,
                1f
            );
        }

        if ("hop".equals(mood)) {

            float jump =
                Math.abs(
                    (float)
                    Math.sin(
                        phase *
                        Math.PI
                    )
                ) *
                h *
                0.15f;

            c.translate(
                0,
                -jump
            );
        }

        if ("sniff".equals(mood)) {

            c.rotate(
                (float)
                Math.sin(
                    phase *
                    Math.PI *
                    4
                ) *
                3f,
                w / 2f,
                bottom
            );
        }

        if ("look".equals(mood)) {

            c.rotate(
                (float)
                Math.sin(
                    phase *
                    Math.PI *
                    3
                ) *
                4f,
                w / 2f,
                bottom
            );
        }

        if ("rest".equals(mood)) {

            c.scale(
                1.02f,
                0.94f,
                w / 2f,
                bottom
            );
        }

        c.drawBitmap(
            sheet,
            src,
            dst,
            paint
        );

        c.restore();
    }
}
