package io.lbry.browser.dialog;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import io.lbry.browser.R;
import lombok.Setter;

public class ContentFromDialogFragment extends BottomSheetDialogFragment {
    public static final String TAG = "ContentFromDialog";
    public static final int ITEM_FROM_PAST_24_HOURS = 1;
    public static final int ITEM_FROM_PAST_WEEK = 2;
    public static final int ITEM_FROM_PAST_MONTH = 3;
    public static final int ITEM_FROM_PAST_YEAR = 4;
    public static final int ITEM_FROM_ALL_TIME = 5;

    @Setter
    private ContentFromListener contentFromListener;
    private int currentFromItem;

    public static ContentFromDialogFragment newInstance() {
        return new ContentFromDialogFragment();
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_content_from, container,false);

        ContentFromItemClickListener clickListener = new ContentFromItemClickListener(this, contentFromListener);
        view.findViewById(R.id.content_from_past_24_hours_item).setOnClickListener(clickListener);
        view.findViewById(R.id.content_from_past_week_item).setOnClickListener(clickListener);
        view.findViewById(R.id.content_from_past_month_item).setOnClickListener(clickListener);
        view.findViewById(R.id.content_from_past_year_item).setOnClickListener(clickListener);
        view.findViewById(R.id.content_from_all_time_item).setOnClickListener(clickListener);
        checkSelectedFromItem(currentFromItem, view);

        return view;
    }

    public static void checkSelectedFromItem(int fromItem, View parent) {
        int checkViewId = -1;
        switch (fromItem) {
            case ITEM_FROM_PAST_24_HOURS: checkViewId = R.id.content_from_past_24_hours_item_selected; break;
            case ITEM_FROM_PAST_WEEK: checkViewId = R.id.content_from_past_week_item_selected; break;
            case ITEM_FROM_PAST_MONTH: checkViewId = R.id.content_from_past_month_item_selected; break;
            case ITEM_FROM_PAST_YEAR: checkViewId = R.id.content_from_past_year_item_selected; break;
            case ITEM_FROM_ALL_TIME: checkViewId = R.id.content_from_all_time_item_selected; break;
        }
        if (parent != null && checkViewId > -1) {
            parent.findViewById(checkViewId).setVisibility(View.VISIBLE);
        }
    }

    public void setCurrentFromItem(int fromItem) {
        this.currentFromItem = fromItem;
    }

    private static class ContentFromItemClickListener implements View.OnClickListener {

        private final int[] checkViewIds = {
                R.id.content_from_past_24_hours_item,
                R.id.content_from_past_week_item,
                R.id.content_from_past_month_item,
                R.id.content_from_past_year_item,
                R.id.content_from_all_time_item
        };
        private BottomSheetDialogFragment dialog;
        private ContentFromListener listener;

        public ContentFromItemClickListener(BottomSheetDialogFragment dialog, ContentFromListener listener) {
            this.dialog = dialog;
            this.listener = listener;
        }

        public void onClick(View view) {
            int currentFromItem = -1;

            if (dialog != null) {
                View dialogView = dialog.getView();
                if (dialogView != null) {
                    for (int id : checkViewIds) {
                        dialogView.findViewById(id).setVisibility(View.GONE);
                    }
                }
            }

            switch (view.getId()) {
                case R.id.content_from_past_24_hours_item: currentFromItem = ITEM_FROM_PAST_24_HOURS; break;
                case R.id.content_from_past_week_item: currentFromItem = ITEM_FROM_PAST_WEEK; break;
                case R.id.content_from_past_month_item: currentFromItem = ITEM_FROM_PAST_MONTH; break;
                case R.id.content_from_past_year_item: currentFromItem = ITEM_FROM_PAST_YEAR; break;
                case R.id.content_from_all_time_item: currentFromItem = ITEM_FROM_ALL_TIME; break;
            }

            checkSelectedFromItem(currentFromItem, view);
            if (listener != null) {
                listener.onContentFromItemSelected(currentFromItem);
            }

            if (dialog != null) {
                dialog.dismiss();
            }
        }
    }

    public interface ContentFromListener {
        void onContentFromItemSelected(int contentFromItem);
    }
}
