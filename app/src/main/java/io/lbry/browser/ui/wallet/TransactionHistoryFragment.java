package io.lbry.browser.ui.wallet;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import io.lbry.browser.MainActivity;
import io.lbry.browser.R;
import io.lbry.browser.adapter.TransactionListAdapter;
import io.lbry.browser.model.Transaction;
import io.lbry.browser.tasks.wallet.TransactionListTask;
import io.lbry.browser.ui.BaseFragment;
import io.lbry.browser.utils.Helper;
import io.lbry.browser.utils.LbryAnalytics;
import io.lbry.browser.utils.LbryUri;

public class TransactionHistoryFragment extends BaseFragment implements TransactionListAdapter.TransactionClickListener {

    private static final int TRANSACTION_PAGE_LIMIT = 50;
    private boolean transactionsHaveReachedEnd;
    private boolean transactionsLoading;
    private ProgressBar loading;
    private RecyclerView transactionList;
    private TransactionListAdapter adapter;
    private View noTransactionsView;
    private int currentTransactionPage;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_transaction_history, container, false);

        loading = root.findViewById(R.id.transaction_history_loading);
        transactionList = root.findViewById(R.id.transaction_history_list);
        noTransactionsView = root.findViewById(R.id.transaction_history_no_transactions);

        Context context = getContext();
        LinearLayoutManager llm = new LinearLayoutManager(context);
        transactionList.setLayoutManager(llm);
        DividerItemDecoration itemDecoration = new DividerItemDecoration(context, DividerItemDecoration.VERTICAL);
        itemDecoration.setDrawable(ContextCompat.getDrawable(context, R.drawable.thin_divider));
        transactionList.addItemDecoration(itemDecoration);

        transactionList.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                if (transactionsLoading) {
                    return;
                }
                LinearLayoutManager lm = (LinearLayoutManager) recyclerView.getLayoutManager();
                if (lm != null) {
                    int visibleItemCount = lm.getChildCount();
                    int totalItemCount = lm.getItemCount();
                    int pastVisibleItems = lm.findFirstVisibleItemPosition();
                    if (pastVisibleItems + visibleItemCount >= totalItemCount) {
                        if (!transactionsHaveReachedEnd) {
                            // load more
                            currentTransactionPage++;
                            loadTransactions();
                        }
                    }
                }
            }
        });

        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        Context context = getContext();
        if (context instanceof MainActivity) {
            MainActivity activity = (MainActivity) context;
            LbryAnalytics.setCurrentScreen(activity, "Transaction History", "TransactionHistory");
        }

        if (adapter != null && adapter.getItemCount() > 0 && transactionList != null) {
            transactionList.setAdapter(adapter);
        }
        loadTransactions();
    }

    private void checkNoTransactions() {
        Helper.setViewVisibility(noTransactionsView, adapter == null || adapter.getItemCount() == 0 ? View.VISIBLE : View.GONE);
    }

    private void loadTransactions() {
        currentTransactionPage = currentTransactionPage == 0 ? 1 : currentTransactionPage;
        transactionsLoading = true;
        TransactionListTask task = new TransactionListTask(currentTransactionPage, TRANSACTION_PAGE_LIMIT, loading, new TransactionListTask.TransactionListHandler() {
            @Override
            public void onSuccess(List<Transaction> transactions, boolean hasReachedEnd) {
                transactionsLoading = false;
                transactionsHaveReachedEnd = hasReachedEnd;
                if (adapter == null) {
                    adapter = new TransactionListAdapter(transactions, getContext());
                    adapter.setListener(TransactionHistoryFragment.this);
                    if (transactionList != null) {
                        transactionList.setAdapter(adapter);
                    }
                } else {
                    adapter.addTransactions(transactions);
                }
                checkNoTransactions();
            }

            @Override
            public void onError(Exception error) {
                transactionsLoading = false;
                checkNoTransactions();
            }
        });
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    @Override
    public void onStart() {
        super.onStart();
        MainActivity activity = (MainActivity) getContext();
        if (activity != null) {
            activity.hideSearchBar();
            activity.hideFloatingWalletBalance();
            activity.showNavigationBackIcon();
            activity.lockDrawer();

            activity.setActionBarTitle(R.string.transaction_history);
        }
    }

    @Override
    public void onStop() {
        Context context = getContext();
        if (context instanceof MainActivity) {
            MainActivity activity = (MainActivity) context;
            activity.restoreToggle();
            activity.showFloatingWalletBalance();
        }
        super.onStop();
    }

    public void onTransactionClicked(Transaction transaction) {
        // Don't do anything? Or open the transaction in a browser?
    }
    public void onClaimUrlClicked(LbryUri uri) {
        Context context = getContext();
        if (uri != null && context instanceof MainActivity) {
            MainActivity activity = (MainActivity) context;
            if (uri.isChannel()) {
                activity.openChannelUrl(uri.toString());
            } else {
                activity.openFileUrl(uri.toString());
            }
        }
    }
}
