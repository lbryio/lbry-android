package io.lbry.browser.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import io.lbry.browser.R;
import io.lbry.browser.model.StartupStage;

public class StartupStageAdapter extends BaseAdapter {
    private final List<StartupStage> list;
    private final LayoutInflater inflater;
    private final String[] stagesString;

    public StartupStageAdapter(Context ctx, List<StartupStage> rows) {
        this.list = rows;
        this.inflater = LayoutInflater.from(ctx);

        stagesString = new String[7];

        stagesString[0] = ctx.getResources().getString(R.string.installation_id_loaded);
        stagesString[1] = ctx.getResources().getString(R.string.known_tags_loaded);
        stagesString[2] = ctx.getResources().getString(R.string.exchange_rate_loaded);
        stagesString[3] = ctx.getResources().getString(R.string.user_authenticated);
        stagesString[4] = ctx.getResources().getString(R.string.installation_registered);
        stagesString[5] = ctx.getResources().getString(R.string.subscriptions_loaded);
        stagesString[6] = ctx.getResources().getString(R.string.subscriptions_resolved);
    }
    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int i) {
        return list.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        if (view == null) {
            view = inflater.inflate(R.layout.list_item_startupstage, viewGroup, false);

            ImageView iconView = view.findViewById(R.id.startup_stage_icon);
            TextView textView = view.findViewById(R.id.startup_stage_text);

            StartupStage item = (StartupStage) getItem(i);

            iconView.setImageResource(item.stageDone ? R.drawable.ic_check : R.drawable.ic_close);
            iconView.setColorFilter(item.stageDone ? Color.WHITE : Color.RED);

            textView.setText(stagesString[item.stage - 1]);
        }
        return view;
    }
}
