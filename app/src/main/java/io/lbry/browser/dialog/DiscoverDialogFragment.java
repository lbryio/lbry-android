package io.lbry.browser.dialog;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;

import io.lbry.browser.R;
import io.lbry.browser.adapter.SuggestedChannelGridAdapter;
import lombok.Getter;
import lombok.Setter;

public class DiscoverDialogFragment extends BottomSheetDialogFragment {
    public static final String TAG = "DiscoverDialog";

    @Getter
    private SuggestedChannelGridAdapter adapter;
    @Setter
    private DiscoverDialogListener dialogActionsListener;

    public static DiscoverDialogFragment newInstance() {
        return new DiscoverDialogFragment();
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_discover, container, false);

        RecyclerView grid = view.findViewById(R.id.discover_channel_grid);
        GridLayoutManager glm = new GridLayoutManager(getContext(), 3);
        grid.setLayoutManager(glm);
        grid.setAdapter(adapter);
        grid.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                GridLayoutManager lm = (GridLayoutManager) recyclerView.getLayoutManager();
                if (lm != null) {
                    int visibleItemCount = lm.getChildCount();
                    int totalItemCount = lm.getItemCount();
                    int pastVisibleItems = lm.findFirstVisibleItemPosition();
                    if (pastVisibleItems + visibleItemCount >= totalItemCount) {
                        if (dialogActionsListener != null) {
                            dialogActionsListener.onScrollEndReached();
                        }
                    }
                }
            }
        });

        MaterialButton doneButton = view.findViewById(R.id.discover_done_button);
        doneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });

        return view;
    }
    public void setAdapter(SuggestedChannelGridAdapter adapter) {
        this.adapter = adapter;
        if (getView() != null) {
            ((RecyclerView) getView().findViewById(R.id.discover_channel_grid)).setAdapter(adapter);
        }
    }
    public void setLoading(boolean loading) {
        if (getView() != null) {
            getView().findViewById(R.id.discover_loading).setVisibility(loading ? View.VISIBLE : View.GONE);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (dialogActionsListener != null) {
            dialogActionsListener.onResume();
        }
    }
    @Override
    public void onCancel(DialogInterface dialog) {
        super.onDismiss(dialog);
        if (dialogActionsListener != null) {
            dialogActionsListener.onCancel();
        }
    }
    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        if (dialogActionsListener != null) {
            dialogActionsListener.onCancel();
        }
    }

    public interface DiscoverDialogListener {
        void onResume();
        void onCancel();
        void onScrollEndReached();
    }
}
