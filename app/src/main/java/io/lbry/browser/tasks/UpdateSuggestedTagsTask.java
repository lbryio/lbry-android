package io.lbry.browser.tasks;

import android.os.AsyncTask;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import io.lbry.browser.adapter.TagListAdapter;
import io.lbry.browser.model.Tag;
import io.lbry.browser.utils.Helper;
import io.lbry.browser.utils.Lbry;

public class UpdateSuggestedTagsTask extends AsyncTask<Void, Void, List<Tag>> {

    private boolean clearPrevious;
    private boolean excludeMature;
    private int limit;
    private String filter;
    private TagListAdapter addedTagsAdapter;
    private TagListAdapter suggestedTagsAdapter;
    private KnownTagsHandler handler;

    public UpdateSuggestedTagsTask(
            String filter,
            int limit,
            TagListAdapter addedTagsAdapter,
            TagListAdapter suggestedTagsAdapter,
            boolean clearPrevious,
            boolean excludeMature,
            KnownTagsHandler handler) {
        this.filter = filter;
        this.limit = limit;
        this.addedTagsAdapter = addedTagsAdapter;
        this.suggestedTagsAdapter = suggestedTagsAdapter;
        this.clearPrevious = clearPrevious;
        this.excludeMature = excludeMature;
        this.handler = handler;
    }

    protected List<Tag> doInBackground(Void... params) {
        List<Tag> tags = new ArrayList<>();
        if (Helper.isNullOrEmpty(filter)) {
            Random random = new Random();
            if (suggestedTagsAdapter != null && !clearPrevious) {
                tags = new ArrayList<>(suggestedTagsAdapter.getTags());
            }
            while (tags.size() < limit) {
                Tag randomTag = Lbry.knownTags.get(random.nextInt(Lbry.knownTags.size()));
                if (excludeMature && randomTag.isMature()) {
                    continue;
                }
                if (!Lbry.followedTags.contains(randomTag) && (addedTagsAdapter == null || !addedTagsAdapter.getTags().contains(randomTag))) {
                    tags.add(randomTag);
                }
            }
        } else {
            Tag filterTag = new Tag(filter);
            if (addedTagsAdapter == null || !addedTagsAdapter.getTags().contains(filterTag)) {
                tags.add(new Tag(filter));
            }
            for (int i = 0; i < Lbry.knownTags.size() && tags.size() < limit - 1; i++) {
                Tag knownTag = Lbry.knownTags.get(i);
                if (excludeMature && knownTag.isMature()) {
                    continue;
                }
                if ((knownTag.getLowercaseName().startsWith(filter) || knownTag.getLowercaseName().matches(filter)) &&
                        (!tags.contains(knownTag) &&
                                !Lbry.followedTags.contains(knownTag) && (addedTagsAdapter == null || !addedTagsAdapter.getTags().contains(knownTag)))) {
                    tags.add(knownTag);
                }
            }
        }
        return tags;
    }
    protected void onPostExecute(List<Tag> tags) {
        if (handler != null) {
            handler.onSuccess(tags);
        }
    }

    public interface KnownTagsHandler {
        void onSuccess(List<Tag> tags);
    }
}
