package io.lbry.browser.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import io.lbry.browser.R;
import io.lbry.browser.model.Language;
import io.lbry.browser.model.License;
import io.lbry.browser.utils.Predefined;

public class LicenseSpinnerAdapter extends ArrayAdapter<License> {
    private int layoutResourceId;
    private LayoutInflater inflater;

    public LicenseSpinnerAdapter(Context context, int resource) {
        super(context, resource, 0, Predefined.LICENSES);
        inflater = LayoutInflater.from(context);
        layoutResourceId = resource;
    }
    public int getItemPosition(String name) {
        for (int i = 0; i < Predefined.LICENSES.size(); i++) {
            License lic = Predefined.LICENSES.get(i);
            if (lic.getName().equalsIgnoreCase(name)) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public View getDropDownView(int position, View view, @NonNull ViewGroup parent) {
        return createView(position, view, parent);
    }
    @Override
    public View getView(int position, View view, @NonNull ViewGroup parent) {
        return createView(position, view, parent);
    }
    private View createView(int position, View convertView, ViewGroup parent) {
        License item = getItem(position);
        View view = inflater.inflate(layoutResourceId, parent, false);
        TextView label = view.findViewById(R.id.item_display_name);
        label.setText(item != null ? item.getStringResourceId() : 0);

        return view;
    }
}