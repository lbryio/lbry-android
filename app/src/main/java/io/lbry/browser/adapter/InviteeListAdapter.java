package io.lbry.browser.adapter;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import io.lbry.browser.R;
import io.lbry.browser.model.lbryinc.Invitee;

public class InviteeListAdapter extends RecyclerView.Adapter<InviteeListAdapter.ViewHolder> {

    private final Context context;
    private final List<Invitee> items;

    public InviteeListAdapter(List<Invitee> invitees, Context context) {
        this.context = context;
        this.items = new ArrayList<>(invitees);
    }

    public void clear() {
        items.clear();
        notifyDataSetChanged();
    }

    public List<Invitee> getItems() {
        return new ArrayList<>(items);
    }

    public void addHeader() {
        Invitee header = new Invitee();
        header.setHeader(true);
        items.add(0, header);
    }

    public void addInvitees(List<Invitee> Invitees) {
        for (Invitee tx : Invitees) {
            if (!items.contains(tx)) {
                items.add(tx);
            }
        }
        notifyDataSetChanged();
    }

    public int getItemCount() {
        return items != null ? items.size() : 0;
    }

    @Override
    public InviteeListAdapter.ViewHolder onCreateViewHolder(ViewGroup root, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.list_item_invitee, root, false);
        return new InviteeListAdapter.ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(InviteeListAdapter.ViewHolder vh, int position) {
        Invitee item = items.get(position);
        vh.emailView.setText(item.isHeader() ? context.getString(R.string.email) : item.getEmail());
        vh.emailView.setTypeface(null, item.isHeader() ? Typeface.BOLD : Typeface.NORMAL);

        String rewardText = context.getString(
                item.isInviteRewardClaimed() ? R.string.claimed :
                        (item.isInviteRewardClaimable() ? R.string.claimable : R.string.unclaimable));
        vh.rewardView.setText(item.isHeader() ? context.getString(R.string.reward) : rewardText);
        vh.rewardView.setTypeface(null, item.isHeader() ? Typeface.BOLD : Typeface.NORMAL);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        protected final TextView emailView;
        protected final TextView rewardView;

        public ViewHolder(View v) {
            super(v);
            emailView = v.findViewById(R.id.invitee_email);
            rewardView = v.findViewById(R.id.invitee_reward);
        }
    }
}
