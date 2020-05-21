package io.lbry.browser.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import io.lbry.browser.R;
import io.lbry.browser.model.Claim;
import io.lbry.browser.model.Language;
import io.lbry.browser.utils.Predefined;

public class LanguageSpinnerAdapter extends ArrayAdapter<Language> {
    private int layoutResourceId;
    private LayoutInflater inflater;

    public LanguageSpinnerAdapter(Context context, int resource) {
        super(context, resource, 0, Predefined.PUBLISH_LANGUAGES);
        inflater = LayoutInflater.from(context);
        layoutResourceId = resource;
    }

    public int getItemPosition(String languageCode) {
        for (int i = 0; i < Predefined.PUBLISH_LANGUAGES.size(); i++) {
            Language lang = Predefined.PUBLISH_LANGUAGES.get(i);
            if (lang.getCode().equalsIgnoreCase(languageCode)) {
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
        Language item = getItem(position);
        View view = inflater.inflate(layoutResourceId, parent, false);
        TextView label = view.findViewById(R.id.item_display_name);
        label.setText(item != null ? item.getStringResourceId() : 0);

        return view;
    }
}