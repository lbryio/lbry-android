package io.lbry.browser.adapter;

import android.content.Context;
import android.database.DataSetObserver;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import io.lbry.browser.R;
import io.lbry.browser.model.Claim;

public class InlineChannelSpinnerAdapter extends ArrayAdapter<Claim> {

    private List<Claim> channels;
    private int layoutResourceId;
    private LayoutInflater inflater;

    public InlineChannelSpinnerAdapter(Context context, int resource, List<Claim> channels) {
        super(context, resource, 0, channels);
        inflater = LayoutInflater.from(context);
        layoutResourceId = resource;
        this.channels = new ArrayList<>(channels);
    }
    public void addPlaceholder(boolean includeAnonymous) {
        Claim placeholder = new Claim();
        placeholder.setPlaceholder(true);
        insert(placeholder, 0);
        channels.add(0, placeholder);

        if (includeAnonymous) {
            Claim anonymous = new Claim();
            anonymous.setPlaceholderAnonymous(true);
            insert(anonymous, 1);
            channels.add(1, anonymous);
        }
    }

    public int getItemPosition(Claim item) {
        for (int i = 0; i < channels.size(); i++) {
            Claim channel = channels.get(i);
            if (item.getClaimId().equalsIgnoreCase(channel.getClaimId())) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public View getDropDownView(int position, View view, ViewGroup parent) {
        return createView(position, view, parent);
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        return createView(position, view, parent);
    }
    private View createView(int position, View convertView, ViewGroup parent){
        View view = inflater.inflate(layoutResourceId, parent, false);

        Context context = getContext();
        Claim channel = getItem(position);
        String name = channel.getName();
        if (channel.isPlaceholder()) {
            name = context.getString(R.string.create_a_channel);
        } else if (channel.isPlaceholderAnonymous()) {
            name = context.getString(R.string.anonymous);
        }

        TextView label = view.findViewById(R.id.channel_item_name);
        label.setText(name);

        return view;
    }
}
