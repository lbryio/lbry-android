package io.lbry.browser.dialog;

import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.RecyclerView;

import com.google.android.flexbox.FlexboxLayoutManager;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import io.lbry.browser.R;
import io.lbry.browser.adapter.TagListAdapter;
import io.lbry.browser.model.Tag;
import io.lbry.browser.utils.Helper;
import io.lbry.browser.utils.Lbry;
import lombok.Setter;

public class CustomizeTagsDialogFragment extends BottomSheetDialogFragment {
    public static final String TAG = "CustomizeTagsDialog";
    private static final int SUGGESTED_LIMIT = 8;
    private String currentFilter;

    private RecyclerView followedTagsList;
    private RecyclerView suggestedTagsList;
    private TagListAdapter followedTagsAdapter;
    private TagListAdapter suggestedTagsAdapter;
    private View noTagsView;
    private View noResultsView;
    @Setter
    private TagListener listener;

    private void checkNoTags() {
        Helper.setViewVisibility(noTagsView, followedTagsAdapter == null || followedTagsAdapter.getItemCount() == 0 ? View.VISIBLE : View.GONE);
    }
    private void checkNoResults() {
        Helper.setViewVisibility(noResultsView, suggestedTagsAdapter == null || suggestedTagsAdapter.getItemCount() == 0 ? View.VISIBLE : View.GONE);
    }
    public void addTag(Tag tag) {
        if (followedTagsAdapter.getTags().contains(tag)) {
            Snackbar.make(getView(), getString(R.string.tag_already_followed, tag.getName()), Snackbar.LENGTH_LONG).show();
            return;
        }

        tag.setFollowed(true);
        followedTagsAdapter.addTag(tag);
        if (suggestedTagsAdapter != null) {
            suggestedTagsAdapter.removeTag(tag);
        }
        updateKnownTags(currentFilter, SUGGESTED_LIMIT, false);
        if (listener != null) {
            listener.onTagAdded(tag);
        }
        checkNoTags();
        checkNoResults();
    }
    public void removeTag(Tag tag) {
        tag.setFollowed(false);
        followedTagsAdapter.removeTag(tag);
        updateKnownTags(currentFilter, SUGGESTED_LIMIT, false);
        if (listener != null) {
            listener.onTagRemoved(tag);
        }
        checkNoTags();
        checkNoResults();
    }

    public void setFilter(String filter) {
        currentFilter = filter;
        updateKnownTags(currentFilter, SUGGESTED_LIMIT, true);
    }

    public static CustomizeTagsDialogFragment newInstance() {
        return new CustomizeTagsDialogFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_customize_tags, container, false);

        noResultsView = view.findViewById(R.id.customize_no_tag_results);
        noTagsView = view.findViewById(R.id.customize_no_followed_tags);

        followedTagsAdapter = new TagListAdapter(Lbry.followedTags, getContext());
        followedTagsAdapter.setCustomizeMode(TagListAdapter.CUSTOMIZE_MODE_REMOVE);
        followedTagsAdapter.setClickListener(customizeTagClickListener);
        suggestedTagsAdapter = new TagListAdapter(new ArrayList<>(), getContext());
        suggestedTagsAdapter.setCustomizeMode(TagListAdapter.CUSTOMIZE_MODE_ADD);
        suggestedTagsAdapter.setClickListener(customizeTagClickListener);

        FlexboxLayoutManager flm1 = new FlexboxLayoutManager(getContext());
        followedTagsList = view.findViewById(R.id.customize_tags_followed_list);
        followedTagsList.setLayoutManager(flm1);
        followedTagsList.setAdapter(followedTagsAdapter);

        FlexboxLayoutManager flm2 = new FlexboxLayoutManager(getContext());
        suggestedTagsList = view.findViewById(R.id.customize_tags_suggested_list);
        suggestedTagsList.setLayoutManager(flm2);
        suggestedTagsList.setAdapter(suggestedTagsAdapter);

        TextInputEditText filterInput = view.findViewById(R.id.customize_tag_filter_input);
        filterInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String value = Helper.getValue(charSequence);
                setFilter(value);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });


        MaterialButton doneButton = view.findViewById(R.id.customize_done_button);
        doneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });

        checkNoTags();
        return view;
    }

    private TagListAdapter.TagClickListener customizeTagClickListener = new TagListAdapter.TagClickListener() {
        @Override
        public void onTagClicked(Tag tag, int customizeMode) {
            if (customizeMode == TagListAdapter.CUSTOMIZE_MODE_ADD) {
                addTag(tag);
            } else if (customizeMode == TagListAdapter.CUSTOMIZE_MODE_REMOVE) {
                removeTag(tag);
            }
        }
    };

    public void onResume() {
        super.onResume();
        updateKnownTags(null, SUGGESTED_LIMIT, true);
    }

    private void updateKnownTags(String filter, int limit, boolean clearPrevious) {
        (new AsyncTask<Void, Void, List<Tag>>() {
            protected List<Tag> doInBackground(Void... params) {
                List<Tag> tags = new ArrayList<>();
                if (Helper.isNullOrEmpty(filter)) {
                    Random random = new Random();
                    if (suggestedTagsAdapter != null && !clearPrevious) {
                        tags = new ArrayList<>(suggestedTagsAdapter.getTags());
                    }
                    while (tags.size() < limit) {
                        Tag randomTag = Lbry.knownTags.get(random.nextInt(Lbry.knownTags.size()));
                        if (!Lbry.followedTags.contains(randomTag) && (followedTagsAdapter == null || !followedTagsAdapter.getTags().contains(randomTag))) {
                            tags.add(randomTag);
                        }
                    }
                } else {
                    Tag filterTag = new Tag(filter);
                    if (followedTagsAdapter == null || !followedTagsAdapter.getTags().contains(filterTag)) {
                        tags.add(new Tag(filter));
                    }
                    for (int i = 0; i < Lbry.knownTags.size() && tags.size() < SUGGESTED_LIMIT - 1; i++) {
                        Tag knownTag = Lbry.knownTags.get(i);
                        if ((knownTag.getLowercaseName().startsWith(filter) || knownTag.getLowercaseName().matches(filter)) &&
                                (!tags.contains(knownTag) &&
                                        !Lbry.followedTags.contains(knownTag) && (followedTagsAdapter == null || !followedTagsAdapter.getTags().contains(knownTag)))) {
                            tags.add(knownTag);
                        }
                    }
                }
                return tags;
            }
            protected void onPostExecute(List<Tag> tags) {
                if (suggestedTagsAdapter == null) {
                    suggestedTagsAdapter = new TagListAdapter(tags, getContext());
                    suggestedTagsAdapter.setCustomizeMode(TagListAdapter.CUSTOMIZE_MODE_ADD);
                    suggestedTagsAdapter.setClickListener(customizeTagClickListener);
                    if (suggestedTagsList != null) {
                        suggestedTagsList.setAdapter(suggestedTagsAdapter);
                    }
                } else {
                    suggestedTagsAdapter.setTags(tags);
                }
                checkNoResults();
            }
        }).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public interface TagListener {
        void onTagAdded(Tag tag);
        void onTagRemoved(Tag tag);
    }
}
