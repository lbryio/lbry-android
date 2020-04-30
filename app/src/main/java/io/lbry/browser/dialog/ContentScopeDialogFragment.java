package io.lbry.browser.dialog;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import io.lbry.browser.R;
import lombok.Setter;

public class ContentScopeDialogFragment extends BottomSheetDialogFragment {
    public static final String TAG = "ContentScopeDialog";
    public static final int ITEM_EVERYONE = 1;
    public static final int ITEM_TAGS = 2;

    @Setter
    private ContentScopeListener contentScopeListener;
    private int currentScopeItem;

    public static ContentScopeDialogFragment newInstance() {
        return new ContentScopeDialogFragment();
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_content_scope, container,false);

        ContentScopeItemClickListener clickListener = new ContentScopeItemClickListener(this, contentScopeListener);
        view.findViewById(R.id.content_scope_everyone_item).setOnClickListener(clickListener);
        view.findViewById(R.id.content_scope_tags_item).setOnClickListener(clickListener);
        checkSelectedScopeItem(currentScopeItem, view);

        return view;
    }

    public static void checkSelectedScopeItem(int scope, View parent) {
        int checkViewId = -1;
        switch (scope) {
            case ITEM_EVERYONE: checkViewId = R.id.content_scope_everyone_item_selected; break;
            case ITEM_TAGS: checkViewId = R.id.content_scope_tags_item_selected; break;
        }
        if (parent != null && checkViewId > -1) {
            parent.findViewById(checkViewId).setVisibility(View.VISIBLE);
        }
    }

    public void setCurrentScopeItem(int scopeItem) {
        this.currentScopeItem = scopeItem;
    }

    private static class ContentScopeItemClickListener implements View.OnClickListener {

        private final int[] checkViewIds = {
                R.id.content_scope_everyone_item_selected, R.id.content_scope_tags_item_selected
        };
        private BottomSheetDialogFragment dialog;
        private ContentScopeListener listener;

        public ContentScopeItemClickListener(BottomSheetDialogFragment dialog, ContentScopeListener listener) {
            this.dialog = dialog;
            this.listener = listener;
        }

        public void onClick(View view) {
            int scopeItem = -1;

            if (dialog != null) {
                View dialogView = dialog.getView();
                if (dialogView != null) {
                    for (int id : checkViewIds) {
                        dialogView.findViewById(id).setVisibility(View.GONE);
                    }
                }
            }

            switch (view.getId()) {
                case R.id.content_scope_everyone_item: scopeItem = ITEM_EVERYONE; break;
                case R.id.content_scope_tags_item: scopeItem = ITEM_TAGS; break;
            }

            checkSelectedScopeItem(scopeItem, view);
            if (listener != null) {
                listener.onContentScopeItemSelected(scopeItem);
            }

            if (dialog != null) {
                dialog.dismiss();
            }
        }
    }

    public interface ContentScopeListener {
        void onContentScopeItemSelected(int scopeItem);
    }
}
