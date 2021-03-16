package io.lbry.browser.views;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.widget.TextView;

import androidx.annotation.Nullable;

import io.lbry.browser.R;

public class CreditsBalanceView extends TextView {
    float textFontSize;
    private float iconSize;
    Rect r;
    Paint p;

    public CreditsBalanceView(Context context) {
        super(context);
    }

    public CreditsBalanceView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        r = new Rect();
        p = new Paint();

        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.CreditsBalanceView,0, 0);

        try {
            textFontSize = a.getDimension(R.styleable.CreditsBalanceView_textSize, 24f);
            iconSize = a.getDimension(R.styleable.CreditsBalanceView_iconSize, 20f);

            this.setTextSize((int) textFontSize);

            DisplayMetrics metrics = Resources.getSystem().getDisplayMetrics();
            float px = 8 * (metrics.densityDpi / 160f);

            setPadding((int) iconSize + Math.round(px), 0, 0, 0);
        } finally {
            a.recycle();
        }

    }

    @Override
    protected void onMeasure (int widthMeasureSpec, int heightMeasureSpec){
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        drawLbryCredits(canvas);
    }

    private void drawLbryCredits(Canvas c) {
        @SuppressLint("UseCompatLoadingForDrawables") Drawable icon = getResources().getDrawable(R.drawable.ic_credits, null);
        float delta = (c.getHeight() - iconSize) / 2;
        icon.setBounds(0, (int) delta, (int) iconSize, (int) (delta + iconSize));
        icon.draw(c);
    }
}
