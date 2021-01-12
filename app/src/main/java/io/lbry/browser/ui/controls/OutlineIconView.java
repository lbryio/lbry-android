package io.lbry.browser.ui.controls;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.Gravity;

import androidx.appcompat.widget.AppCompatTextView;

public class OutlineIconView extends AppCompatTextView {
    private final Context context;

    public OutlineIconView(Context context) {
        super(context);
        this.context = context;
        init();
    }

    public OutlineIconView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        init();
    }

    private void init() {
        setGravity(Gravity.CENTER);
        setTypeface(Typeface.createFromAsset(context.getAssets(), "font_awesome_5_free_regular.otf"));
    }
}
