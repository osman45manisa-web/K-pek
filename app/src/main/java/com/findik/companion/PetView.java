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

    public int frame;

    public boolean facingRight;

    public String mood =
        "walk";

    public float phase;

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

    private String labelForMood() {

        if (
            mood.equals(
                "sniff"
            )
        ) {
            return "Kokluyorum…";
        }

        if (
            mood.equals(
                "hop"
            )
        ) {
            return "Merhaba!";
        }

        if (
            mood.equals(
                "look"
            )
        ) {
            return "Ne var orada?";
        }

        if (
            mood.equals(
                "rest"
            )
        ) {
            return "Biraz dinleneyim…";
        }

        return "";
    }

    @Override
    protected void onDraw(
        Canvas c
    ) {

        super.onDraw(
            c
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
            Math.floorMod(
                frame,
                8
            );

        c.save();

        float breathe =
            1f;

        if (
            mood.equals(
                "walk"
            )
        ) {

            breathe =
                1f +
                (float)
                Math.sin(
                    System
                        .currentTimeMillis() /
                    240.0
                ) *
                0.008f;

            c.scale(
                1f,
                breathe,
                w /
                2f,
                h
            );
        }

        if (
            facingRight
        ) {

            c.translate(
                w,
                0
            );

            c.scale(
                -1,
                1
            );
        }

        float bottom =
            h -
            12;

        float drawH =
            w *
            cellH /
            cellW;

        if (
            mood.equals(
                "hop"
            )
        ) {

            c.translate(
                0,
                -Math.abs(
                    (float)
                    Math.sin(
                        phase *
                        Math.PI *
                        2
                    )
                ) *
                h *
                0.14f
            );
        }

        if (
            mood.equals(
                "sniff"
            )
        ) {

            c.rotate(
                (float)
                Math.sin(
                    phase *
                    Math.PI *
                    5
                ) *
                4,
                w *
                .66f,
                bottom
            );
        }

        if (
            mood.equals(
                "rest"
            )
        ) {

            c.scale(
                1.04f,
                .82f,
                w /
                2f,
                bottom
            );
        }

        if (
            mood.equals(
                "look"
            )
        ) {

            c.rotate
