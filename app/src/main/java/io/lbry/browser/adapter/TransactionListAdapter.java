package io.lbry.browser.adapter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import io.lbry.browser.R;
import io.lbry.browser.model.Transaction;
import io.lbry.browser.utils.Helper;
import io.lbry.browser.utils.LbryUri;
import lombok.Setter;

public class TransactionListAdapter extends RecyclerView.Adapter<TransactionListAdapter.ViewHolder> {

    private static final DecimalFormat TX_LIST_AMOUNT_FORMAT = new DecimalFormat("#,##0.0000");
    private static final SimpleDateFormat TX_LIST_DATE_FORMAT = new SimpleDateFormat("MMM d");

    private Context context;
    private List<Transaction> items;
    @Setter
    private TransactionClickListener listener;

    public TransactionListAdapter(List<Transaction> transactions, Context context) {
        this.context = context;
        this.items = new ArrayList<>(transactions);
    }

    public void clear() {
        items.clear();
        notifyDataSetChanged();
    }

    public List<Transaction> getItems() {
        return new ArrayList<>(items);
    }

    public void addTransactions(List<Transaction> transactions) {
        for (Transaction tx : transactions) {
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
    public TransactionListAdapter.ViewHolder onCreateViewHolder(ViewGroup root, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.list_item_transaction, root, false);
        return new TransactionListAdapter.ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(TransactionListAdapter.ViewHolder vh, int position) {
        Transaction item = items.get(position);
        vh.descView.setText(item.getDescriptionStringId());
        vh.amountView.setText(TX_LIST_AMOUNT_FORMAT.format(item.getValue().doubleValue()));
        vh.claimView.setText(item.getClaim());
        vh.feeView.setText(context.getString(R.string.tx_list_fee, TX_LIST_AMOUNT_FORMAT.format(item.getFee().doubleValue())));
        vh.txidLinkView.setText(item.getTxid().substring(0, 7));
        vh.dateView.setVisibility(item.getConfirmations() > 0 ? View.VISIBLE : View.GONE);
        vh.dateView.setText(item.getConfirmations() > 0 ? TX_LIST_DATE_FORMAT.format(item.getTxDate()) : null);
        vh.pendingView.setVisibility(item.getConfirmations() == 0 ? View.VISIBLE : View.GONE);

        vh.infoFeeContainer.setVisibility(!Helper.isNullOrEmpty(item.getClaim()) || Math.abs(item.getFee().doubleValue()) > 0 ?
                View.VISIBLE : View.GONE);

        vh.claimView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LbryUri claimUrl = item.getClaimUrl();
                if (claimUrl != null && listener != null) {
                    listener.onClaimUrlClicked(claimUrl);
                }
            }
        });

        vh.txidLinkView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (context != null) {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(String.format("%s/%s", Helper.EXPLORER_TX_PREFIX, item.getTxid())));
                    context.startActivity(intent);
                }
            }
        });

        vh.itemView.setOnClickListener(view -> {
            if (listener != null) {
                listener.onTransactionClicked(item);
            }
        });
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        protected TextView descView;
        protected TextView amountView;
        protected TextView claimView;
        protected TextView feeView;
        protected TextView txidLinkView;
        protected TextView dateView;
        protected TextView pendingView;
        protected View infoFeeContainer;

        public ViewHolder(View v) {
            super(v);
            descView = v.findViewById(R.id.transaction_desc);
            amountView = v.findViewById(R.id.transaction_amount);
            claimView = v.findViewById(R.id.transaction_claim);
            feeView = v.findViewById(R.id.transaction_fee);
            txidLinkView = v.findViewById(R.id.transaction_id_link);
            dateView = v.findViewById(R.id.transaction_date);
            pendingView = v.findViewById(R.id.transaction_pending_text);
            infoFeeContainer = v.findViewById(R.id.transaction_info_fee_container);
        }
    }

    public interface TransactionClickListener {
        void onTransactionClicked(Transaction transaction);
        void onClaimUrlClicked(LbryUri uri);
    }
}
