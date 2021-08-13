package io.lbry.browser.adapter;

import android.content.Context;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import io.lbry.browser.R;
import io.lbry.browser.model.Claim;
import io.lbry.browser.model.ClaimCacheKey;
import io.lbry.browser.model.Comment;
import io.lbry.browser.utils.Helper;
import io.lbry.browser.utils.Lbry;
import io.lbry.browser.utils.LbryUri;
import lombok.Setter;

public class CommentListAdapter extends RecyclerView.Adapter<CommentListAdapter.ViewHolder> {
    private final List<Comment> items;
    private final Context context;
    private final boolean nested;
    private float scale;
    @Setter
    private ClaimListAdapter.ClaimListItemListener listener;
    @Setter
    private ReplyClickListener replyListener;

    public CommentListAdapter(List<Comment> items, Context context) {
        this(items, context, false);
    }

    public CommentListAdapter(List<Comment> items, Context context, boolean nested) {
        this.items = new ArrayList<>(items);
        this.context = context;
        this.nested = nested;
        if (context != null) {
            scale = context.getResources().getDisplayMetrics().density;
        }
        for (Comment item : this.items) {
            ClaimCacheKey key = new ClaimCacheKey();
            key.setClaimId(item.getChannelId());
            if (Lbry.claimCache.containsKey(key)) {
                item.setPoster(Lbry.claimCache.get(key));
            }
        }
    }

    public void clearItems() {
        items.clear();
        notifyDataSetChanged();
    }


    public int getPositionForComment(String commentHash) {
        for (int i = 0; i < items.size(); i++) {
            if (commentHash.equalsIgnoreCase(items.get(i).getId())) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public int getItemCount() {
        return items != null ? items.size() : 0;
    }

    public List<String> getClaimUrlsToResolve() {
        List<String> urls = new ArrayList<>();
        for (int i = 0; i < items.size(); i++) {
            Comment item = items.get(i);
            if (item.getPoster() == null) {
                LbryUri url = LbryUri.tryParse(String.format("%s#%s", item.getChannelName(), item.getChannelId()));
                if (url != null && !urls.contains(url.toString())) {
                    urls.add(url.toString());
                }
            }
            if (item.getReplies().size() > 0) {
                for (int j = 0; j < item.getReplies().size(); j++) {
                    Comment reply = item.getReplies().get(j);
                    if (reply.getPoster() == null) {
                        LbryUri url = LbryUri.tryParse(String.format("%s#%s", reply.getChannelName(), reply.getChannelId()));
                        if (url != null && !urls.contains(url.toString())) {
                            urls.add(url.toString());
                        }
                    }
                }
            }
        }
        return urls;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        protected final TextView channelName;
        protected final TextView commentText;
        protected final ImageView thumbnailView;
        protected final View noThumbnailView;
        protected final TextView alphaView;
        protected final TextView commentTimeView;
        protected final View replyLink;
        protected final RecyclerView repliesList;

        public ViewHolder (View v) {
            super(v);
            channelName = v.findViewById(R.id.comment_channel_name);
            commentTimeView = v.findViewById(R.id.comment_time);
            commentText = v.findViewById(R.id.comment_text);
            replyLink = v.findViewById(R.id.comment_reply_link);
            thumbnailView = v.findViewById(R.id.comment_thumbnail);
            noThumbnailView = v.findViewById(R.id.comment_no_thumbnail);
            alphaView = v.findViewById(R.id.comment_thumbnail_alpha);
            repliesList = v.findViewById(R.id.comment_replies);
        }
    }

    public void insert(int index, Comment comment) {
        if (!items.contains(comment)) {
            items.add(index, comment);
            notifyDataSetChanged();
        }
    }

    public void addReply(Comment comment) {
        for (int i = 0; i < items.size(); i++) {
            Comment parent = items.get(i);
            if (parent.getId().equalsIgnoreCase(comment.getParentId())) {
                parent.addReply(comment);
                notifyDataSetChanged();
                break;
            }
        }
    }

    public void updatePosterForComment(String channelId, Claim channel) {
        for (int i = 0 ; i < items.size(); i++) {
            Comment item = items.get(i);
            List<Comment> replies = item.getReplies();
            if (replies != null && replies.size() > 0) {
                for (int j = 0; j < replies.size(); j++) {
                    Comment reply = item.getReplies().get(j);
                    if (channelId.equalsIgnoreCase(reply.getChannelId())) {
                        reply.setPoster(channel);
                        break;
                    }
                }
            }

            if (channelId.equalsIgnoreCase(item.getChannelId())) {
                item.setPoster(channel);
                break;
            }
        }
    }

    @Override
    public  ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.list_item_comment, parent, false);
        return new CommentListAdapter.ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Comment comment = items.get(position);
        holder.itemView.setPadding(
                nested ? Helper.getScaledValue(56, scale) : holder.itemView.getPaddingStart(),
                holder.itemView.getPaddingTop(),
                nested ? 0 : holder.itemView.getPaddingEnd(),
                holder.itemView.getPaddingBottom());

        holder.channelName.setText(comment.getChannelName());
        holder.commentTimeView.setText(DateUtils.getRelativeTimeSpanString(
                (comment.getTimestamp() * 1000), System.currentTimeMillis(), 0, DateUtils.FORMAT_ABBREV_RELATIVE));
        holder.commentText.setText(comment.getText());
        holder.replyLink.setVisibility(!nested ? View.VISIBLE : View.GONE);

        boolean hasThumbnail = comment.getPoster() != null && !Helper.isNullOrEmpty(comment.getPoster().getThumbnailUrl());
        holder.thumbnailView.setVisibility(hasThumbnail ? View.VISIBLE : View.INVISIBLE);
        holder.noThumbnailView.setVisibility(!hasThumbnail ? View.VISIBLE : View.INVISIBLE);
        int bgColor = Helper.generateRandomColorForValue(comment.getChannelId());
        Helper.setIconViewBackgroundColor(holder.noThumbnailView, bgColor, false, context);
        if (hasThumbnail) {
            Glide.with(context.getApplicationContext()).asBitmap().load(comment.getPoster().getThumbnailUrl(holder.thumbnailView.getLayoutParams().width, holder.thumbnailView.getLayoutParams().height, 85)).
                    apply(RequestOptions.circleCropTransform()).into(holder.thumbnailView);
        }
        holder.alphaView.setText(comment.getChannelName() != null ? comment.getChannelName().substring(1, 2).toUpperCase() : null);
        List<Comment> replies = comment.getReplies();
        boolean hasReplies = replies != null && replies.size() > 0;
        if (hasReplies) {
            holder.repliesList.setLayoutManager(new LinearLayoutManager(context));
            holder.repliesList.setAdapter(new CommentListAdapter(replies, context, true));
        } else {
            holder.repliesList.setAdapter(null);
        }

        holder.channelName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (listener != null && comment.getPoster() != null) {
                    listener.onClaimClicked(comment.getPoster());
                }
            }
        });

        holder.replyLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (replyListener != null) {
                    replyListener.onReplyClicked(comment);
                }
            }
        });
    }

    public interface ReplyClickListener {
        void onReplyClicked(Comment comment);
    }
}