package io.lbry.browser.adapter;

import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import java.util.List;

import io.lbry.browser.MainActivity;
import io.lbry.browser.R;
import io.lbry.browser.model.WalletDetailItem;
import io.lbry.browser.utils.Helper;
import io.lbry.browser.views.CreditsBalanceView;

public class WalletDetailAdapter extends BaseAdapter {
    private final List<WalletDetailItem> list;
    private final LayoutInflater inflater;

    public WalletDetailAdapter(Context ctx, List<WalletDetailItem> rows) {
        this.list = rows;
        this.inflater = LayoutInflater.from(ctx);
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
            view = inflater.inflate(R.layout.list_item_boosting_balance, viewGroup, false);

            CreditsBalanceView balanceView = view.findViewById(R.id.wallet_supporting_balance);
            TextView detailTextView = view.findViewById(R.id.detail);
            TextView detailExplanationTextView = view.findViewById(R.id.detail_explanation);

            WalletDetailItem item = (WalletDetailItem) getItem(i);

            detailTextView.setText(item.detail);
            detailExplanationTextView.setText(item.detailDesc);

            Helper.setViewText(balanceView, item.detailAmount);

            ProgressBar progressUnlockTips = view.findViewById(R.id.wallet_unlock_tips_progress);
            progressUnlockTips.setVisibility(item.isInProgress ? View.VISIBLE : View.GONE);

            ImageButton buttonLock = view.findViewById(R.id.lock_button);
            buttonLock.setVisibility((item.isUnlockable && !item.isInProgress) ? View.VISIBLE : View.GONE);

            if (item.isUnlockable) {
                buttonLock.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (view.getContext() != null) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext()).
                                    setTitle(R.string.unlock_tips).
                                    setMessage(R.string.confirm_unlock_tips)
                                    .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            unlockTips(view);
                                        }
                                    }).setNegativeButton(R.string.no, null);
                            builder.show();
                        }
                    }
                });
            }
        }
        return view;
    }

    private void unlockTips(View v) {
        Context ctx = v.getContext();
        if (ctx instanceof MainActivity) {
            v.setVisibility(View.GONE);
            View progress = v.getRootView().findViewById(R.id.wallet_unlock_tips_progress);
            progress.setVisibility(View.VISIBLE);
            ((MainActivity) ctx).unlockTips();
        }
    }
}
