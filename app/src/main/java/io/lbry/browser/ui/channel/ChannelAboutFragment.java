package io.lbry.browser.ui.channel;

import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.text.HtmlCompat;
import androidx.fragment.app.Fragment;

import io.lbry.browser.R;
import io.lbry.browser.utils.Helper;
import lombok.Setter;

public class ChannelAboutFragment extends Fragment {
    private View layoutWebsite;
    private View layoutEmail;
    private View layoutInfoArea;
    private View layoutNoAboutInfo;
    private TextView textWebsite;
    private TextView textEmail;
    private TextView textDescription;

    @Setter
    private String website;
    @Setter
    private String email;
    @Setter
    private String description;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_channel_about, container, false);

        layoutInfoArea = root.findViewById(R.id.channel_about_info_area);
        layoutNoAboutInfo = root.findViewById(R.id.channel_about_no_info_container);
        layoutWebsite = root.findViewById(R.id.channel_about_website_container);
        layoutEmail = root.findViewById(R.id.channel_about_email_container);
        textWebsite = root.findViewById(R.id.channel_about_website);
        textEmail = root.findViewById(R.id.channel_about_email);
        textDescription = root.findViewById(R.id.channel_about_description);

        boolean noInfo = (Helper.isNullOrEmpty(website) && Helper.isNullOrEmpty(email) && Helper.isNullOrEmpty(description));
        layoutNoAboutInfo.setVisibility(noInfo ? View.VISIBLE : View.GONE);
        layoutInfoArea.setVisibility(noInfo ? View.GONE : View.VISIBLE);
        layoutWebsite.setVisibility(!Helper.isNullOrEmpty(website) ? View.VISIBLE : View.GONE);
        layoutEmail.setVisibility(!Helper.isNullOrEmpty(email) ? View.VISIBLE : View.GONE);
        textDescription.setVisibility(!Helper.isNullOrEmpty(description) ? View.VISIBLE : View.GONE);

        textWebsite.setLinksClickable(true);
        textWebsite.setMovementMethod(LinkMovementMethod.getInstance());
        textWebsite.setText(!Helper.isNullOrEmpty(website) ?
                HtmlCompat.fromHtml(String.format("<a href=\"%s\">%s</a>", website, website), HtmlCompat.FROM_HTML_MODE_LEGACY) : null);

        textEmail.setText(email);
        textDescription.setText(description);

        return root;
    }

    public void refresh() {
        textWebsite.setText(!Helper.isNullOrEmpty(website) ?
                HtmlCompat.fromHtml(String.format("<a href=\"%s\">%s</a>", website, website), HtmlCompat.FROM_HTML_MODE_LEGACY) : null);

        textEmail.setText(email);
        textDescription.setText(description);
    }
}
