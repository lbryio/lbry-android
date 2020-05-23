package io.lbry.browser.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;

import io.lbry.browser.R;
import io.lbry.browser.model.lbryinc.Reward;
import io.lbry.browser.utils.Helper;
import io.lbry.browser.utils.Lbryio;
import lombok.Getter;
import lombok.Setter;

public class RewardListAdapter extends RecyclerView.Adapter<RewardListAdapter.ViewHolder> {

    public static final int DISPLAY_MODE_ALL = 1;
    public static final int DISPLAY_MODE_UNCLAIMED = 2;

    private Context context;
    @Setter
    private List<Reward> all;
    private List<Reward> items;
    @Setter
    private RewardClickListener clickListener;
    @Getter
    private int displayMode;

    public RewardListAdapter(List<Reward> all, Context context) {
        this.all = new ArrayList<>(all);
        this.items = new ArrayList<>(all);
        this.context = context;
        this.displayMode = DISPLAY_MODE_ALL;

        addCustomReward();
    }

    public void setRewards(List<Reward> rewards) {
        this.all = new ArrayList<>(rewards);
        updateItemsForDisplayMode();
        notifyDataSetChanged();
    }

    public void setDisplayMode(int displayMode) {
        this.displayMode = displayMode;
        updateItemsForDisplayMode();
        notifyDataSetChanged();
    }

    private void updateItemsForDisplayMode() {
        if (displayMode == DISPLAY_MODE_ALL) {
            items = new ArrayList<>(all);
        } else if (displayMode == DISPLAY_MODE_UNCLAIMED) {
            items = new ArrayList<>();
            for (Reward reward : all) {
                if (!reward.isClaimed()) {
                    items.add(reward);
                }
            }
        }
        addCustomReward();
    }

    private void addCustomReward() {
        Reward custom = new Reward();
        custom.setCustom(true);
        custom.setRewardTitle(context.getString(R.string.custom_reward_title));
        custom.setRewardDescription(context.getString(R.string.custom_reward_description));
        items.add(custom);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        protected View iconClaimed;
        protected View loading;
        protected View upTo;
        protected TextView textTitle;
        protected TextView textDescription;
        protected TextView textLbcValue;
        protected TextView textUsdValue;
        protected TextView textLinkTransaction;
        protected EditText inputCustomCode;
        protected MaterialButton buttonClaimCustom;
        public ViewHolder(View v) {
            super(v);
            iconClaimed = v.findViewById(R.id.reward_item_claimed_icon);
            upTo = v.findViewById(R.id.reward_item_up_to);
            loading = v.findViewById(R.id.reward_item_loading);
            textTitle = v.findViewById(R.id.reward_item_title);
            textDescription = v.findViewById(R.id.reward_item_description);
            textLbcValue = v.findViewById(R.id.reward_item_lbc_value);
            textLinkTransaction = v.findViewById(R.id.reward_item_tx_link);
            textUsdValue = v.findViewById(R.id.reward_item_usd_value);
            inputCustomCode = v.findViewById(R.id.reward_item_custom_code_input);
            buttonClaimCustom = v.findViewById(R.id.reward_item_custom_claim_button);
        }
    }

    public int getItemCount() {
        return items != null ? items.size() : 0;
    }

    public void addReward(Reward reward) {
        if (!items.contains(reward)) {
            items.add(reward);
        }
        notifyDataSetChanged();
    }
    public List<Reward> getRewards() {
        return new ArrayList<>(items);
    }

    @Override
    public RewardListAdapter.ViewHolder onCreateViewHolder(ViewGroup root, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.list_item_reward, root, false);
        return new RewardListAdapter.ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(RewardListAdapter.ViewHolder vh, int position) {
        Reward reward = items.get(position);
        String displayAmount = reward.getDisplayAmount();
        double rewardAmount = 0;
        if (!"?".equals(displayAmount)) {
            rewardAmount = Double.valueOf(displayAmount);
        }
        boolean hasTransaction = !Helper.isNullOrEmpty(reward.getTransactionId()) && reward.getTransactionId().length() > 7;
        vh.iconClaimed.setVisibility(reward.isClaimed() ? View.VISIBLE : View.INVISIBLE);
        vh.inputCustomCode.setVisibility(reward.isCustom() ? View.VISIBLE : View.GONE);
        vh.buttonClaimCustom.setVisibility(reward.isCustom() ? View.VISIBLE : View.GONE);
        vh.textTitle.setText(reward.getRewardTitle());
        vh.textDescription.setText(reward.getRewardDescription());
        vh.upTo.setVisibility(reward.shouldDisplayRange() ? View.VISIBLE : View.GONE);
        vh.textLbcValue.setText(reward.isCustom() ? "?" : Helper.LBC_CURRENCY_FORMAT.format(Helper.parseDouble(reward.getDisplayAmount(), 0)));
        vh.textLinkTransaction.setVisibility(hasTransaction ? View.VISIBLE : View.GONE);
        vh.textLinkTransaction.setText(hasTransaction ? reward.getTransactionId().substring(0, 7) : null);
        vh.textLinkTransaction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (context != null) {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(String.format("%s/%s", Helper.EXPLORER_TX_PREFIX, reward.getTransactionId())));
                    context.startActivity(intent);
                }
            }
        });

        vh.textUsdValue.setText(reward.isCustom() || Lbryio.LBCUSDRate == 0 ? null :
                String.format("≈$%s", Helper.SIMPLE_CURRENCY_FORMAT.format(rewardAmount * Lbryio.LBCUSDRate)));

        vh.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (reward.isClaimed()) {
                    return;
                }

                if (vh.inputCustomCode != null && !vh.inputCustomCode.hasFocus()) {
                    vh.inputCustomCode.requestFocus();
                }

                if (clickListener != null) {
                    clickListener.onRewardClicked(reward, vh.loading);
                }
            }
        });

        vh.inputCustomCode.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String value = charSequence.toString().trim();
                vh.buttonClaimCustom.setEnabled(value.length() > 0);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        vh.buttonClaimCustom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String claimCode = Helper.getValue(vh.inputCustomCode.getText());
                if (Helper.isNullOrEmpty(claimCode)) {
                    Snackbar.make(view, R.string.please_enter_claim_code, Snackbar.LENGTH_LONG).
                            setBackgroundTint(Color.RED).setTextColor(Color.WHITE).show();
                    return;
                }

                if (clickListener != null) {
                    clickListener.onCustomClaimButtonClicked(claimCode, vh.inputCustomCode, vh.buttonClaimCustom, vh.loading);
                }
            }
        });
    }

    public interface RewardClickListener {
        void onRewardClicked(Reward reward, View loadingView);
        void onCustomClaimButtonClicked(String code, EditText inputCustomCode, MaterialButton buttonClaim, View loadingView);
    }
}
