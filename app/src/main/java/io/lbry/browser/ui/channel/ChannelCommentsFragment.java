package io.lbry.browser.ui.channel;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatSpinner;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import io.lbry.browser.BuildConfig;
import io.lbry.browser.MainActivity;
import io.lbry.browser.R;
import io.lbry.browser.adapter.ClaimListAdapter;
import io.lbry.browser.adapter.CommentListAdapter;
import io.lbry.browser.adapter.InlineChannelSpinnerAdapter;
import io.lbry.browser.listener.SdkStatusListener;
import io.lbry.browser.listener.WalletBalanceListener;
import io.lbry.browser.model.Claim;
import io.lbry.browser.model.Comment;
import io.lbry.browser.model.WalletBalance;
import io.lbry.browser.tasks.CommentCreateTask;
import io.lbry.browser.tasks.CommentListHandler;
import io.lbry.browser.tasks.CommentListTask;
import io.lbry.browser.tasks.claim.ChannelCreateUpdateTask;
import io.lbry.browser.tasks.claim.ClaimListResultHandler;
import io.lbry.browser.tasks.claim.ClaimListTask;
import io.lbry.browser.tasks.claim.ClaimResultHandler;
import io.lbry.browser.tasks.claim.ResolveTask;
import io.lbry.browser.tasks.lbryinc.LogPublishTask;
import io.lbry.browser.utils.Helper;
import io.lbry.browser.utils.Lbry;
import io.lbry.browser.utils.LbryAnalytics;
import io.lbry.browser.utils.LbryUri;
import lombok.Setter;

public class ChannelCommentsFragment extends Fragment implements SdkStatusListener, WalletBalanceListener {

    @Setter
    private Claim claim;
    @Setter
    private String commentHash;
    private CommentListAdapter commentListAdapter;

    private Comment replyToComment;
    private View containerReplyToComment;
    private TextView textReplyingTo;
    private TextView textReplyToBody;
    private View buttonClearReplyToComment;

    private boolean postingComment;
    private boolean fetchingChannels;
    private View progressLoadingChannels;
    private View progressPostComment;
    private InlineChannelSpinnerAdapter commentChannelSpinnerAdapter;
    private AppCompatSpinner commentChannelSpinner;
    private TextInputEditText inputComment;
    private TextView textCommentLimit;
    private MaterialButton buttonPostComment;
    private ImageView commentPostAsThumbnail;
    private View commentPostAsNoThumbnail;
    private TextView commentPostAsAlpha;

    private View inlineChannelCreator;
    private TextInputEditText inlineChannelCreatorInputName;
    private TextInputEditText inlineChannelCreatorInputDeposit;
    private View inlineChannelCreatorInlineBalance;
    private TextView inlineChannelCreatorInlineBalanceValue;
    private View inlineChannelCreatorCancelLink;
    private View inlineChannelCreatorProgress;
    private MaterialButton inlineChannelCreatorCreateButton;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_channel_comments, container, false);

        containerReplyToComment = root.findViewById(R.id.comment_form_reply_to_container);
        textReplyingTo = root.findViewById(R.id.comment_form_replying_to_text);
        textReplyToBody = root.findViewById(R.id.comment_form_reply_to_body);
        buttonClearReplyToComment = root.findViewById(R.id.comment_form_clear_reply_to);

        commentChannelSpinner = root.findViewById(R.id.comment_form_channel_spinner);
        progressLoadingChannels = root.findViewById(R.id.comment_form_channels_loading);
        progressPostComment = root.findViewById(R.id.comment_form_post_progress);
        inputComment = root.findViewById(R.id.comment_form_body);
        textCommentLimit = root.findViewById(R.id.comment_form_text_limit);
        buttonPostComment = root.findViewById(R.id.comment_form_post);
        commentPostAsThumbnail = root.findViewById(R.id.comment_form_thumbnail);
        commentPostAsNoThumbnail = root.findViewById(R.id.comment_form_no_thumbnail);
        commentPostAsAlpha = root.findViewById(R.id.comment_form_thumbnail_alpha);

        inlineChannelCreator = root.findViewById(R.id.container_inline_channel_form_create);
        inlineChannelCreatorInputName = root.findViewById(R.id.inline_channel_form_input_name);
        inlineChannelCreatorInputDeposit = root.findViewById(R.id.inline_channel_form_input_deposit);
        inlineChannelCreatorInlineBalance = root.findViewById(R.id.inline_channel_form_inline_balance_container);
        inlineChannelCreatorInlineBalanceValue = root.findViewById(R.id.inline_channel_form_inline_balance_value);
        inlineChannelCreatorProgress = root.findViewById(R.id.inline_channel_form_create_progress);
        inlineChannelCreatorCancelLink = root.findViewById(R.id.inline_channel_form_cancel_link);
        inlineChannelCreatorCreateButton = root.findViewById(R.id.inline_channel_form_create_button);

        RecyclerView commentList = root.findViewById(R.id.channel_comments_list);
        commentList.setLayoutManager(new LinearLayoutManager(getContext()));

        initCommentForm(root);
        setupInlineChannelCreator(
                inlineChannelCreator,
                inlineChannelCreatorInputName,
                inlineChannelCreatorInputDeposit,
                inlineChannelCreatorInlineBalance,
                inlineChannelCreatorInlineBalanceValue,
                inlineChannelCreatorCancelLink,
                inlineChannelCreatorCreateButton,
                inlineChannelCreatorProgress,
                commentChannelSpinner,
                commentChannelSpinnerAdapter
        );

        return root;
    }

    public void onResume() {
        super.onResume();
        Context context = getContext();
        if (!Lbry.SDK_READY) {
            if (context instanceof MainActivity) {
                ((MainActivity) context).addSdkStatusListener(this);
            }
        } else {
            onSdkReady();
        }

        if (context instanceof MainActivity) {
            ((MainActivity) context).addWalletBalanceListener(this);
        }

        checkAndLoadComments();
    }

    public void onStop() {
        super.onStop();
        Context context = getContext();
        if (context instanceof MainActivity) {
            MainActivity activity = (MainActivity) context;
            activity.removeSdkStatusListener(this);
            activity.removeWalletBalanceListener(this);
        }
    }

    private void checkCommentSdkInitializing() {
        View root = getView();
        if (root != null) {
            TextView commentsSDKInitializing = root.findViewById(R.id.channel_comments_sdk_initializing);
            Helper.setViewVisibility(commentsSDKInitializing, Lbry.SDK_READY ? View.GONE : View.VISIBLE);
        }
    }

    @Override
    public void onSdkReady() {
        fetchChannels();
    }

    private void checkAndLoadComments() {
        View root = getView();
        if (root != null) {
            View commentsDisabledText = root.findViewById(R.id.channel_disabled_comments);
            View commentForm = root.findViewById(R.id.container_comment_form);
            RecyclerView commentsList = root.findViewById(R.id.channel_comments_list);

            if (claim.getTags().contains("disable-comments")) {
                Helper.setViewVisibility(commentsDisabledText, View.VISIBLE);
                Helper.setViewVisibility(commentForm, View.GONE);
                Helper.setViewVisibility(commentsList, View.GONE);
            } else {
                Helper.setViewVisibility(commentsDisabledText, View.GONE);
                Helper.setViewVisibility(commentForm, View.VISIBLE);
                Helper.setViewVisibility(commentsList, View.VISIBLE);
                if (commentsList == null || commentsList.getAdapter() == null || commentsList.getAdapter().getItemCount() == 0) {
                    loadComments();
                }
            }
        }
    }

    private void loadComments() {
        View root = getView();
        ProgressBar relatedLoading = root.findViewById(R.id.channel_comments_progress);
        if (claim != null && root != null) {
            CommentListTask task = new CommentListTask(1, 200, claim.getClaimId(), relatedLoading, new CommentListHandler() {
                @Override
                public void onSuccess(List<Comment> comments, boolean hasReachedEnd) {
                    Context ctx = getContext();
                    View root = getView();
                    if (ctx != null && root != null) {
                        commentListAdapter = new CommentListAdapter(comments, ctx);
                        commentListAdapter.setListener(new ClaimListAdapter.ClaimListItemListener() {
                            @Override
                            public void onClaimClicked(Claim claim) {
                                if (!Helper.isNullOrEmpty(claim.getName()) &&
                                        claim.getName().startsWith("@") &&
                                        ctx instanceof MainActivity) {
                                    ((MainActivity) ctx).openChannelClaim(claim);
                                }
                            }
                        });
                        commentListAdapter.setReplyListener(new CommentListAdapter.ReplyClickListener() {
                            @Override
                            public void onReplyClicked(Comment comment) {
                                setReplyToComment(comment);
                            }
                        });

                        RecyclerView relatedContentList = root.findViewById(R.id.channel_comments_list);
                        relatedContentList.setAdapter(commentListAdapter);
                        commentListAdapter.notifyDataSetChanged();

                        checkNoComments();
                        resolveCommentPosters();
                        scrollToCommentHash();
                    }
                }

                @Override
                public void onError(Exception error) {
                    // pass
                }
            });
            task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }

    private void scrollToCommentHash() {
        View root = getView();
        // check for the position of commentHash if set
        if (root != null && !Helper.isNullOrEmpty(commentHash) && commentListAdapter != null && commentListAdapter.getItemCount() > 0) {
            RecyclerView commentList = root.findViewById(R.id.channel_comments_list);
            int position = commentListAdapter.getPositionForComment(commentHash);
            if (position > -1 && commentList.getLayoutManager() != null) {
                NestedScrollView scrollView = root.findViewById(R.id.channel_comments_area);
                scrollView.requestChildFocus(commentList, commentList);
                commentList.getLayoutManager().scrollToPosition(position);
            }
        }
    }

    private void resolveCommentPosters() {
        if (commentListAdapter != null) {
            List<String> urlsToResolve = new ArrayList<>(commentListAdapter.getClaimUrlsToResolve());
            if (urlsToResolve.size() > 0) {
                ResolveTask task = new ResolveTask(urlsToResolve, Lbry.SDK_CONNECTION_STRING, null, new ClaimListResultHandler() {
                    @Override
                    public void onSuccess(List<Claim> claims) {
                        if (commentListAdapter != null) {
                            for (Claim claim : claims) {
                                if (claim.getClaimId() != null) {
                                    commentListAdapter.updatePosterForComment(claim.getClaimId(), claim);
                                }
                            }
                            commentListAdapter.notifyDataSetChanged();
                        }
                    }

                    @Override
                    public void onError(Exception error) {
                        // pass
                    }
                });
                task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }
        }
    }

    @Override
    public void onWalletBalanceUpdated(WalletBalance walletBalance) {
        if (walletBalance != null && inlineChannelCreatorInlineBalanceValue != null) {
            inlineChannelCreatorInlineBalanceValue.setText(Helper.shortCurrencyFormat(walletBalance.getAvailable().doubleValue()));
        }
    }

    private void fetchChannels() {
        if (Lbry.ownChannels != null && Lbry.ownChannels.size() > 0) {
            updateChannelList(Lbry.ownChannels);
            return;
        }

        fetchingChannels = true;
        disableChannelSpinner();
        ClaimListTask task = new ClaimListTask(Claim.TYPE_CHANNEL, progressLoadingChannels, new ClaimListResultHandler() {
            @Override
            public void onSuccess(List<Claim> claims) {
                Lbry.ownChannels = new ArrayList<>(claims);
                updateChannelList(Lbry.ownChannels);
                enableChannelSpinner();
                fetchingChannels = false;
            }

            @Override
            public void onError(Exception error) {
                enableChannelSpinner();
                fetchingChannels = false;
            }
        });
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }
    private void disableChannelSpinner() {
        Helper.setViewEnabled(commentChannelSpinner, false);
        hideInlineChannelCreator();
    }
    private void enableChannelSpinner() {
        Helper.setViewEnabled(commentChannelSpinner, true);
        if (commentChannelSpinner != null) {
            Claim selectedClaim = (Claim) commentChannelSpinner.getSelectedItem();
            if (selectedClaim != null) {
                if (selectedClaim.isPlaceholder()) {
                    showInlineChannelCreator();
                } else {
                    hideInlineChannelCreator();
                }
            }
        }
    }
    private void showInlineChannelCreator() {
        Helper.setViewVisibility(inlineChannelCreator, View.VISIBLE);
    }
    private void hideInlineChannelCreator() {
        Helper.setViewVisibility(inlineChannelCreator, View.GONE);
    }

    private void updateChannelList(List<Claim> channels) {
        if (commentChannelSpinnerAdapter == null) {
            Context context = getContext();
            if (context != null) {
                commentChannelSpinnerAdapter = new InlineChannelSpinnerAdapter(context, R.layout.spinner_item_channel, new ArrayList<>(channels));
                commentChannelSpinnerAdapter.addPlaceholder(false);
                commentChannelSpinnerAdapter.notifyDataSetChanged();
            }
        } else {
            commentChannelSpinnerAdapter.clear();
            commentChannelSpinnerAdapter.addAll(channels);
            commentChannelSpinnerAdapter.addPlaceholder(false);
            commentChannelSpinnerAdapter.notifyDataSetChanged();
        }

        if (commentChannelSpinner != null) {
            commentChannelSpinner.setAdapter(commentChannelSpinnerAdapter);
        }

        if (commentChannelSpinnerAdapter != null && commentChannelSpinner != null) {
            if (commentChannelSpinnerAdapter.getCount() > 1) {
                commentChannelSpinner.setSelection(1);
            }
        }
    }

    private void initCommentForm(View root) {
        textCommentLimit.setText(String.format("%d / %d", Helper.getValue(inputComment.getText()).length(), Comment.MAX_LENGTH));

        buttonClearReplyToComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clearReplyToComment();
            }
        });

        buttonPostComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!Lbry.SDK_READY) {
                    Snackbar.make(root.findViewById(R.id.channel_comments_area), R.string.sdk_initializing_functionality, Snackbar.LENGTH_LONG).show();
                    return;
                }

                validateAndCheckPostComment();
            }
        });

        inputComment.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                int len = charSequence.length();
                textCommentLimit.setText(String.format("%d / %d", len, Comment.MAX_LENGTH));
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        commentChannelSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                Object item = adapterView.getItemAtPosition(position);
                if (item instanceof Claim) {
                    Claim claim = (Claim) item;
                    if (claim.isPlaceholder()) {
                        if (!fetchingChannels) {
                            showInlineChannelCreator();
                        }
                    } else {
                        hideInlineChannelCreator();
                        updatePostAsChannel(claim);
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    private void validateAndCheckPostComment() {
        String comment = Helper.getValue(inputComment.getText());
        Claim channel = (Claim) commentChannelSpinner.getSelectedItem();

        if (Helper.isNullOrEmpty(comment)) {
            showError(getString(R.string.please_enter_comment));
            return;
        }
        if (channel == null || Helper.isNullOrEmpty(channel.getClaimId())) {
            showError(getString(R.string.please_select_channel));
            return;
        }

        postComment();
    }

    private void updatePostAsChannel(Claim channel) {
        boolean hasThumbnail = !Helper.isNullOrEmpty(channel.getThumbnailUrl());
        Helper.setViewVisibility(commentPostAsThumbnail, hasThumbnail ? View.VISIBLE : View.INVISIBLE);
        Helper.setViewVisibility(commentPostAsNoThumbnail, !hasThumbnail ? View.VISIBLE : View.INVISIBLE);
        Helper.setViewText(commentPostAsAlpha, channel.getName() != null ? channel.getName().substring(1, 2).toUpperCase() : null);

        Context context = getContext();
        int bgColor = Helper.generateRandomColorForValue(channel.getClaimId());
        Helper.setIconViewBackgroundColor(commentPostAsNoThumbnail, bgColor, false, context);

        if (hasThumbnail && context != null) {
            Glide.with(context.getApplicationContext()).
                    asBitmap().
                    load(channel.getThumbnailUrl(commentPostAsThumbnail.getLayoutParams().width, commentPostAsThumbnail.getLayoutParams().height, 85)).
                    apply(RequestOptions.circleCropTransform()).
                    into(commentPostAsThumbnail);
        }
    }

    private void beforePostComment() {
        postingComment = true;
        Helper.setViewEnabled(commentChannelSpinner, false);
        Helper.setViewEnabled(inputComment, false);
        Helper.setViewEnabled(buttonClearReplyToComment, false);
        Helper.setViewEnabled(buttonPostComment, false);
    }

    private void afterPostComment() {
        Helper.setViewEnabled(commentChannelSpinner, true);
        Helper.setViewEnabled(inputComment, true);
        Helper.setViewEnabled(buttonClearReplyToComment, true);
        Helper.setViewEnabled(buttonPostComment, true);
        postingComment = false;
    }

    private Comment buildPostComment() {
        Comment comment = new Comment();
        Claim channel = (Claim) commentChannelSpinner.getSelectedItem();
        comment.setClaimId(claim.getClaimId());
        comment.setChannelId(channel.getClaimId());
        comment.setChannelName(channel.getName());
        comment.setText(Helper.getValue(inputComment.getText()));
        comment.setPoster(channel);
        if (replyToComment != null) {
            comment.setParentId(replyToComment.getId());
        }

        return comment;
    }

    private void setReplyToComment(Comment comment) {
        replyToComment = comment;
        Helper.setViewText(textReplyingTo, getString(R.string.replying_to, comment.getChannelName()));
        Helper.setViewText(textReplyToBody, comment.getText());
        Helper.setViewVisibility(containerReplyToComment, View.VISIBLE);

        inputComment.requestFocus();
        Context context = getContext();
        if (context != null) {
            InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(inputComment, InputMethodManager.SHOW_FORCED);
        }
    }

    private void clearReplyToComment() {
        Helper.setViewText(textReplyingTo, null);
        Helper.setViewText(textReplyToBody, null);
        Helper.setViewVisibility(containerReplyToComment, View.GONE);
        replyToComment = null;
    }

    private void postComment() {
        if (postingComment) {
            return;
        }

        Comment comment = buildPostComment();

        beforePostComment();
        CommentCreateTask task = new CommentCreateTask(comment, progressPostComment, new CommentCreateTask.CommentCreateWithTipHandler() {
            @Override
            public void onSuccess(Comment createdComment) {
                inputComment.setText(null);
                clearReplyToComment();

                if (commentListAdapter != null) {
                    createdComment.setPoster(comment.getPoster());
                    if (!Helper.isNullOrEmpty(createdComment.getParentId())) {
                        commentListAdapter.addReply(createdComment);
                    } else {
                        commentListAdapter.insert(0, createdComment);
                    }
                }
                afterPostComment();
                checkNoComments();

                Bundle bundle = new Bundle();
                bundle.putString("claim_id", claim != null ? claim.getClaimId() : null);
                bundle.putString("claim_name", claim != null ? claim.getName() : null);
                LbryAnalytics.logEvent(LbryAnalytics.EVENT_COMMENT_CREATE, bundle);

                Context context = getContext();
                if (context instanceof MainActivity) {
                    ((MainActivity) context).showMessage(R.string.comment_posted);
                }
            }

            @Override
            public void onError(Exception error) {
                try {
                    showError(error != null ? error.getMessage() : getString(R.string.comment_error));
                } catch (IllegalStateException ex) {
                    // pass
                }
                afterPostComment();
            }
        });
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void showError(String message) {
        Context context = getContext();
        if (context instanceof MainActivity) {
            ((MainActivity) context).showError(message);
        }
    }

    private void checkNoComments() {
        View root = getView();
        if (root != null) {
            Helper.setViewVisibility(root.findViewById(R.id.channel_no_comments),
                    commentListAdapter == null || commentListAdapter.getItemCount() == 0 ? View.VISIBLE : View.GONE);
        }
    }

    private void setupInlineChannelCreator(
            View container,
            TextInputEditText inputChannelName,
            TextInputEditText inputDeposit,
            View inlineBalanceView,
            TextView inlineBalanceValue,
            View linkCancel,
            MaterialButton buttonCreate,
            View progressView,
            AppCompatSpinner channelSpinner,
            InlineChannelSpinnerAdapter channelSpinnerAdapter) {
        inputDeposit.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                Helper.setViewVisibility(inlineBalanceView, hasFocus ? View.VISIBLE : View.INVISIBLE);
            }
        });

        linkCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Helper.setViewText(inputChannelName, null);
                Helper.setViewText(inputDeposit, null);
                Helper.setViewVisibility(container, View.GONE);
            }
        });

        buttonCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // validate deposit and channel name
                String channelNameString = Helper.normalizeChannelName(Helper.getValue(inputChannelName.getText()));
                Claim claimToSave = new Claim();
                claimToSave.setName(channelNameString);
                String channelName = claimToSave.getName().startsWith("@") ? claimToSave.getName().substring(1) : claimToSave.getName();
                String depositString = Helper.getValue(inputDeposit.getText());
                if ("@".equals(channelName) || Helper.isNullOrEmpty(channelName)) {
                    showError(getString(R.string.please_enter_channel_name));
                    return;
                }
                if (!LbryUri.isNameValid(channelName)) {
                    showError(getString(R.string.channel_name_invalid_characters));
                    return;
                }
                if (Helper.channelExists(channelName)) {
                    showError(getString(R.string.channel_name_already_created));
                    return;
                }

                double depositAmount = 0;
                try {
                    depositAmount = Double.valueOf(depositString);
                } catch (NumberFormatException ex) {
                    // pass
                    showError(getString(R.string.please_enter_valid_deposit));
                    return;
                }
                if (depositAmount <= 0.000001) {
                    String error = getResources().getQuantityString(R.plurals.min_deposit_required, Math.abs(depositAmount-1.0) <= 0.000001 ? 1 : 2, String.valueOf(Helper.MIN_DEPOSIT));
                    showError(error);
                    return;
                }
                if (Lbry.walletBalance == null || Lbry.walletBalance.getAvailable().doubleValue() < depositAmount) {
                    showError(getString(R.string.deposit_more_than_balance));
                    return;
                }

                ChannelCreateUpdateTask task =  new ChannelCreateUpdateTask(
                        claimToSave, new BigDecimal(depositString), false, progressView, new ClaimResultHandler() {
                    @Override
                    public void beforeStart() {
                        Helper.setViewEnabled(inputChannelName, false);
                        Helper.setViewEnabled(inputDeposit, false);
                        Helper.setViewEnabled(buttonCreate, false);
                        Helper.setViewEnabled(linkCancel, false);
                    }

                    @Override
                    public void onSuccess(Claim claimResult) {
                        if (!BuildConfig.DEBUG) {
                            LogPublishTask logPublishTask = new LogPublishTask(claimResult);
                            logPublishTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                        }

                        // channel created
                        Bundle bundle = new Bundle();
                        bundle.putString("claim_id", claimResult.getClaimId());
                        bundle.putString("claim_name", claimResult.getName());
                        LbryAnalytics.logEvent(LbryAnalytics.EVENT_CHANNEL_CREATE, bundle);

                        // add the claim to the channel list and set it as the selected item
                        if (channelSpinnerAdapter != null) {
                            channelSpinnerAdapter.add(claimResult);
                        }
                        if (channelSpinner != null && channelSpinnerAdapter != null) {
                            channelSpinner.setSelection(channelSpinnerAdapter.getCount() - 1);
                        }

                        Helper.setViewEnabled(inputChannelName, true);
                        Helper.setViewEnabled(inputDeposit, true);
                        Helper.setViewEnabled(buttonCreate, true);
                        Helper.setViewEnabled(linkCancel, true);
                    }

                    @Override
                    public void onError(Exception error) {
                        Helper.setViewEnabled(inputChannelName, true);
                        Helper.setViewEnabled(inputDeposit, true);
                        Helper.setViewEnabled(buttonCreate, true);
                        Helper.setViewEnabled(linkCancel, true);
                        showError(error.getMessage());
                    }
                });
                task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }
        });

        Helper.setViewText(inlineBalanceValue, Helper.shortCurrencyFormat(Lbry.walletBalance.getAvailable().doubleValue()));
    }
}
