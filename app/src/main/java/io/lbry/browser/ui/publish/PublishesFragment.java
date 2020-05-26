package io.lbry.browser.ui.publish;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.view.ActionMode;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.lbry.browser.MainActivity;
import io.lbry.browser.R;
import io.lbry.browser.adapter.ClaimListAdapter;
import io.lbry.browser.listener.SdkStatusListener;
import io.lbry.browser.listener.SelectionModeListener;
import io.lbry.browser.model.Claim;
import io.lbry.browser.model.NavMenuItem;
import io.lbry.browser.tasks.claim.AbandonHandler;
import io.lbry.browser.tasks.claim.AbandonStreamTask;
import io.lbry.browser.tasks.claim.ClaimListResultHandler;
import io.lbry.browser.tasks.claim.ClaimListTask;
import io.lbry.browser.ui.BaseFragment;
import io.lbry.browser.utils.Helper;
import io.lbry.browser.utils.Lbry;
import io.lbry.browser.utils.LbryAnalytics;

public class PublishesFragment extends BaseFragment implements ActionMode.Callback, SelectionModeListener, SdkStatusListener {

    private Button buttonNewPublish;
    private FloatingActionButton fabNewPublish;
    private ActionMode actionMode;
    private View emptyView;
    private View layoutSdkInitializing;
    private ProgressBar loading;
    private ProgressBar bigLoading;
    private RecyclerView contentList;
    private ClaimListAdapter adapter;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_publishes, container, false);

        buttonNewPublish = root.findViewById(R.id.publishes_create_button);
        fabNewPublish = root.findViewById(R.id.publishes_fab_new_publish);
        buttonNewPublish.setOnClickListener(newPublishClickListener);
        fabNewPublish.setOnClickListener(newPublishClickListener);

        emptyView = root.findViewById(R.id.publishes_empty_container);
        layoutSdkInitializing = root.findViewById(R.id.container_sdk_initializing);
        contentList = root.findViewById(R.id.publishes_list);
        LinearLayoutManager llm = new LinearLayoutManager(getContext());
        contentList.setLayoutManager(llm);
        loading = root.findViewById(R.id.publishes_list_loading);
        bigLoading = root.findViewById(R.id.publishes_list_big_loading);

        layoutSdkInitializing.setVisibility(Lbry.SDK_READY ? View.GONE : View.VISIBLE);

        return root;
    }

    private View.OnClickListener newPublishClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Context context = getContext();
            if (context instanceof MainActivity) {
                ((MainActivity) context).openFragment(PublishFragment.class, true, NavMenuItem.ID_ITEM_NEW_PUBLISH);
            }
        }
    };

    @Override
    public void onStart() {
        super.onStart();
        Context context = getContext();
        if (context != null) {
            MainActivity activity = (MainActivity) context;
            activity.hideFloatingWalletBalance();
        }
    }

    @Override
    public void onStop() {
        Context context = getContext();
        if (context instanceof MainActivity) {
            MainActivity activity = (MainActivity) context;
            activity.showFloatingWalletBalance();
        }
        super.onStop();
    }

    public void onResume() {
        super.onResume();
        Context context = getContext();
        if (context instanceof MainActivity) {
            MainActivity activity = (MainActivity) context;
            activity.setWunderbarValue(null);
            LbryAnalytics.setCurrentScreen(activity, "Publishes", "Publishes");
        }

        if (!Lbry.SDK_READY) {
            if (context instanceof MainActivity) {
                MainActivity activity = (MainActivity) context;
                activity.addSdkStatusListener(this);
            }
        } else {
            onSdkReady();
        }
    }

    public void onSdkReady() {
        Helper.setViewVisibility(layoutSdkInitializing, View.GONE);
        Helper.setViewVisibility(fabNewPublish, View.VISIBLE);
        if (adapter != null && contentList != null) {
            contentList.setAdapter(adapter);
        }
        fetchPublishes();
    }

    public View getLoading() {
        return (adapter == null || adapter.getItemCount() == 0) ? bigLoading : loading;
    }

    private void checkNoPublishes() {
        Helper.setViewVisibility(emptyView, adapter == null || adapter.getItemCount() == 0 ? View.VISIBLE : View.GONE);
    }

    private void fetchPublishes() {
        Helper.setViewVisibility(emptyView, View.GONE);
        ClaimListTask task = new ClaimListTask(Arrays.asList(Claim.TYPE_STREAM, Claim.TYPE_REPOST), getLoading(), new ClaimListResultHandler() {
            @Override
            public void onSuccess(List<Claim> claims) {
                Lbry.ownClaims = Helper.filterDeletedClaims(new ArrayList<>(claims));
                Context context = getContext();
                if (adapter == null) {
                    adapter = new ClaimListAdapter(claims, context);
                    adapter.setCanEnterSelectionMode(true);
                    adapter.setSelectionModeListener(PublishesFragment.this);
                    adapter.setListener(new ClaimListAdapter.ClaimListItemListener() {
                        @Override
                        public void onClaimClicked(Claim claim) {
                            if (context instanceof MainActivity) {
                                MainActivity activity = (MainActivity) context;
                                if (claim.getName().startsWith("@")) {
                                    activity.openChannelClaim(claim);
                                } else {
                                    activity.openFileClaim(claim);
                                }
                            }
                        }
                    });
                    if (contentList != null) {
                        contentList.setAdapter(adapter);
                    }
                } else {
                    adapter.setItems(claims);
                }

                checkNoPublishes();
            }

            @Override
            public void onError(Exception error) {
                checkNoPublishes();
            }
        });
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public void onEnterSelectionMode() {
        Context context = getContext();
        if (context instanceof MainActivity) {
            MainActivity activity = (MainActivity) context;
            activity.startSupportActionMode(this);
        }
    }
    public void onItemSelectionToggled() {
        if (actionMode != null) {
            actionMode.setTitle(String.valueOf(adapter.getSelectedCount()));
            actionMode.invalidate();
        }
    }
    public void onExitSelectionMode() {
        if (actionMode != null) {
            actionMode.finish();
        }
    }

    @Override
    public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
        this.actionMode = actionMode;
        Context context = getContext();
        if (context instanceof MainActivity) {
            MainActivity activity = (MainActivity) context;
            if (!activity.isDarkMode()) {
                activity.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
            }
        }

        actionMode.getMenuInflater().inflate(R.menu.menu_claim_list, menu);
        return true;
    }
    @Override
    public void onDestroyActionMode(ActionMode actionMode) {
        if (adapter != null) {
            adapter.clearSelectedItems();
            adapter.setInSelectionMode(false);
            adapter.notifyDataSetChanged();
        }
        Context context = getContext();
        if (context != null) {
            MainActivity activity = (MainActivity) context;
            if (!activity.isDarkMode()) {
                activity.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            }
        }
        this.actionMode = null;
    }

    @Override
    public boolean onPrepareActionMode(androidx.appcompat.view.ActionMode actionMode, Menu menu) {
        int selectionCount = adapter != null ? adapter.getSelectedCount() : 0;
        menu.findItem(R.id.action_edit).setVisible(selectionCount == 1);
        return true;
    }

    @Override
    public boolean onActionItemClicked(androidx.appcompat.view.ActionMode actionMode, MenuItem menuItem) {
        if (R.id.action_edit == menuItem.getItemId()) {
            if (adapter != null && adapter.getSelectedCount() > 0) {
                Claim claim = adapter.getSelectedItems().get(0);
                // start channel editor with the claim
                Context context = getContext();
                if (context instanceof MainActivity) {
                    ((MainActivity) context).openPublishForm(claim);
                }

                actionMode.finish();
                return true;
            }
        }
        if (R.id.action_delete == menuItem.getItemId()) {
            if (adapter != null && adapter.getSelectedCount() > 0) {
                final List<Claim> selectedClaims = new ArrayList<>(adapter.getSelectedItems());
                String message = getResources().getQuantityString(R.plurals.confirm_delete_publishes, selectedClaims.size());
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext()).
                        setTitle(R.string.delete_selection).
                        setMessage(message)
                        .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                handleDeleteSelectedClaims(selectedClaims);
                            }
                        }).setNegativeButton(R.string.no, null);
                builder.show();
                return true;
            }
        }

        return false;
    }

    private void handleDeleteSelectedClaims(List<Claim> selectedClaims) {
        List<String> claimIds = new ArrayList<>();

        for (Claim claim : selectedClaims) {
            claimIds.add(claim.getClaimId());
        }

        if (actionMode != null) {
            actionMode.finish();
        }

        Helper.setViewVisibility(contentList, View.INVISIBLE);
        Helper.setViewVisibility(fabNewPublish, View.INVISIBLE);
        AbandonStreamTask task = new AbandonStreamTask(claimIds, bigLoading, new AbandonHandler() {
            @Override
            public void onComplete(List<String> successfulClaimIds, List<String> failedClaimIds, List<Exception> errors) {
                View root = getView();
                if (root != null) {
                    if (failedClaimIds.size() > 0) {
                        Snackbar.make(root, R.string.one_or_more_publishes_failed_abandon, Snackbar.LENGTH_LONG).
                                setBackgroundTint(Color.RED).setTextColor(Color.WHITE).show();
                    } else if (successfulClaimIds.size() == claimIds.size()) {
                        try {
                            String message = getResources().getQuantityString(R.plurals.publishes_deleted, successfulClaimIds.size());
                            Snackbar.make(root, message, Snackbar.LENGTH_LONG).show();
                        } catch (IllegalStateException ex) {
                            // pass
                        }
                    }
                }

                Lbry.abandonedClaimIds.addAll(successfulClaimIds);
                if (adapter != null) {
                    adapter.setItems(Helper.filterDeletedClaims(adapter.getItems()));
                }

                Helper.setViewVisibility(contentList, View.VISIBLE);
                Helper.setViewVisibility(fabNewPublish, View.VISIBLE);
                checkNoPublishes();
            }
        });
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }
}
