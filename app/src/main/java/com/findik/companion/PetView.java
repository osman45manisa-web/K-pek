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

    private final Paint bubblePaint =
        new Paint(
            Paint.ANTI_ALIAS_FLAG
        );

    private final Paint bubbleText =
        new Paint(
            Paint.ANTI_ALIAS_FLAG
        );

    public int frame = 0;
    public boolean facingRight = false;
    public String mood = "walk";
    public float phase = 0f;

    public PetView(
        Context context
    ) {
        super(context);

        BitmapFactory.Options options =
            new BitmapFactory.Options();

        options.inScaled =
            false;

        sheet =
            BitmapFactory.decodeResource(
                getResources(),
                R.drawable.findik_walk,
                options
            );

        bubbleText.setColor(
            Color.rgb(
                70,
                57,
                45
            )
        );

        bubbleText.setTextAlign(
            Paint.Align.CENTER
        );

        bubbleText.setTypeface(
            Typeface.create(
                Typeface.DEFAULT,
                Typeface.BOLD
            )
        );

        setContentDescription(
            "Fındık: dokunarak tepki verdir, sürükleyerek taşı"
        );
    }

    private int moodFrame() {

        if (
            "rest".equals(
                mood
            )
        ) {
            return 6;
        }

        if (
            "look".equals(
                mood
            )
        ) {
            return 5;
        }

        if (
            "sniff".equals(
                mood
            )
        ) {
            return 4;
        }

        if (
            "hop".equals(
                mood
            )
        ) {
            return 7;
        }

        return frame % 4;
    }

    private String moodText() {

        if (
            "sniff".equals(
                mood
            )
        ) {
            return "Kokluyorum…";
        }

        if (
            "hop".equals(
                mood
            )
        ) {
            return "Merhaba!";
        }

        if (
            "look".equals(
                mood
            )
        ) {
            return "Ne var orada?";
        }

        if (
            "rest".equals(
                mood
            )
        ) {
            return "Biraz dinleneyim…";
        }

        return "";
    }

    @Override
    protected void onDraw(
        Canvas canvas
    ) {

        super.onDraw(
            canvas
        );

        if (
            sheet == null
        ) {
            return;
        }

        float w =
            getWidth();

        float h =
            getHeight();

        int cellW =
            sheet.getWidth() /
            4;

        int cellH =
            sheet.getHeight() /
            2;

        int i =
            moodFrame();

        int col =
            i % 4;

        int row =
            i / 4;

        Rect src =
            new Rect(
                col * cellW,
                row * cellH,
                (col + 1) * cellW,
                (row + 1) * cellH
            );

        float bottom =
            h - 8;

        float drawHeight =
            w *
            cellH /
            cellW;

        RectF dst =
            new RectF(
                0,
                bottom -
                drawHeight,
                w,
