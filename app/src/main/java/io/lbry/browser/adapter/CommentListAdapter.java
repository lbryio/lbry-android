package io.lbry.browser.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import androidx.recyclerview.widget.RecyclerView;
import io.lbry.browser.R;
import io.lbry.browser.model.Comment;

public class CommentListAdapter extends RecyclerView.Adapter<CommentListAdapter.ViewHolder> {
    private List<Comment> items;
    private Context context;

    public CommentListAdapter(List<Comment> items, Context context) {
        this.items = items;
        this.context = context;
    }

    @Override
    public int getItemCount() {
        return items != null ? items.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        protected TextView channelName;
        protected TextView commentText;

        public ViewHolder (View v) {
            super(v);
            channelName = v.findViewById(R.id.comment_channel_name);
            commentText = v.findViewById(R.id.comment_text);
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
        holder.channelName.setText(comment.getChannelName());
        holder.commentText.setText(comment.getText());
    }
}