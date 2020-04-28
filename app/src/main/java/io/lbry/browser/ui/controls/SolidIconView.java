package io.lbry.browser.ui.controls;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.Gravity;

import androidx.appcompat.widget.AppCompatTextView;

public class SolidIconView extends AppCompatTextView {
    private Context context;

    public SolidIconView(Context context) {
        super(context);
        this.context = context;
        init();
    }

    public SolidIconView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        init();
    }

    private void init() {
        setGravity(Gravity.CENTER);
        setTypeface(Typeface.createFromAsset(context.getAssets(), "font_awesome_5_free_solid.otf"));
    }
}
