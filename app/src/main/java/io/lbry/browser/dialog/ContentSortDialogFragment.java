package io.lbry.browser.dialog;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import io.lbry.browser.R;
import lombok.Setter;

public class ContentSortDialogFragment extends BottomSheetDialogFragment {
    public static final String TAG = "ContentSortDialog";
    public static final int ITEM_SORT_BY_TRENDING = 1;
    public static final int ITEM_SORT_BY_NEW = 2;
    public static final int ITEM_SORT_BY_TOP = 3;

    @Setter
    private SortByListener sortByListener;
    private int currentSortByItem;

    public static ContentSortDialogFragment newInstance() {
        return new ContentSortDialogFragment();
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_content_sort, container,false);

        SortByItemClickListener clickListener = new SortByItemClickListener(this, sortByListener);
        view.findViewById(R.id.sort_by_trending_item).setOnClickListener(clickListener);
        view.findViewById(R.id.sort_by_new_item).setOnClickListener(clickListener);
        view.findViewById(R.id.sort_by_top_item).setOnClickListener(clickListener);
        checkSelectedSortByItem(currentSortByItem, view);

        return view;
    }

    public static void checkSelectedSortByItem(int sortByItem, View parent) {
        int checkViewId = -1;
        switch (sortByItem) {
            case ITEM_SORT_BY_TRENDING: checkViewId = R.id.sort_by_trending_item_selected; break;
            case ITEM_SORT_BY_NEW: checkViewId = R.id.sort_by_new_item_selected; break;
            case ITEM_SORT_BY_TOP: checkViewId = R.id.sort_by_top_item_selected; break;
        }
        if (parent != null && checkViewId > -1) {
            parent.findViewById(checkViewId).setVisibility(View.VISIBLE);
        }
    }

    public void setCurrentSortByItem(int sortByItem) {
        this.currentSortByItem = sortByItem;
    }

    private static class SortByItemClickListener implements View.OnClickListener {

        private final int[] checkViewIds = {
                R.id.sort_by_trending_item_selected, R.id.sort_by_new_item_selected, R.id.sort_by_top_item_selected
        };
        private BottomSheetDialogFragment dialog;
        private SortByListener listener;

        public SortByItemClickListener(BottomSheetDialogFragment dialog, SortByListener listener) {
            this.dialog = dialog;
            this.listener = listener;
        }

        public void onClick(View view) {
            int selectedSortByItem = -1;

            if (dialog != null) {
                View dialogView = dialog.getView();
                if (dialogView != null) {
                    for (int id : checkViewIds) {
                        dialogView.findViewById(id).setVisibility(View.GONE);
                    }
                }
            }

            switch (view.getId()) {
                case R.id.sort_by_trending_item: selectedSortByItem = ITEM_SORT_BY_TRENDING; break;
                case R.id.sort_by_new_item: selectedSortByItem = ITEM_SORT_BY_NEW; break;
                case R.id.sort_by_top_item: selectedSortByItem = ITEM_SORT_BY_TOP; break;
            }

            checkSelectedSortByItem(selectedSortByItem, view);
            if (listener != null) {
                listener.onSortByItemSelected(selectedSortByItem);
            }

            if (dialog != null) {
                dialog.dismiss();
            }
        }
    }

    public interface SortByListener {
        void onSortByItemSelected(int sortBy);
    }
}
