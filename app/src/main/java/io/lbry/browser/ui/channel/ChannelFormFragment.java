package io.lbry.browser.ui.channel;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.core.content.ContextCompat;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.flexbox.FlexboxLayoutManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;

import java.io.File;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import io.lbry.browser.BuildConfig;
import io.lbry.browser.MainActivity;
import io.lbry.browser.R;
import io.lbry.browser.adapter.TagListAdapter;
import io.lbry.browser.listener.FilePickerListener;
import io.lbry.browser.listener.StoragePermissionListener;
import io.lbry.browser.listener.WalletBalanceListener;
import io.lbry.browser.model.Claim;
import io.lbry.browser.model.NavMenuItem;
import io.lbry.browser.model.Tag;
import io.lbry.browser.model.WalletBalance;
import io.lbry.browser.tasks.UpdateSuggestedTagsTask;
import io.lbry.browser.tasks.UploadImageTask;
import io.lbry.browser.tasks.claim.ChannelCreateUpdateTask;
import io.lbry.browser.tasks.claim.ClaimResultHandler;
import io.lbry.browser.tasks.lbryinc.LogPublishTask;
import io.lbry.browser.ui.BaseFragment;
import io.lbry.browser.utils.Helper;
import io.lbry.browser.utils.Lbry;
import io.lbry.browser.utils.LbryAnalytics;
import io.lbry.browser.utils.LbryUri;
import lombok.Getter;

public class ChannelFormFragment extends BaseFragment implements
        FilePickerListener, StoragePermissionListener, TagListAdapter.TagClickListener, WalletBalanceListener {

    private static final int SUGGESTED_LIMIT = 8;

    @Getter
    private boolean saveInProgress;
    private boolean uploading;
    private Claim currentClaim;
    private boolean editFieldsLoaded;
    private boolean editMode;
    private View linkCancel;
    private TextView linkShowOptional;
    private MaterialButton buttonSave;

    private NestedScrollView scrollView;
    private View inlineBalanceContainer;
    private TextView inlineBalanceValue;
    private View uploadProgress;
    private View containerOptionalFields;
    private ImageView imageCover;
    private ImageView imageThumbnail;
    private TextInputEditText inputTitle;
    private TextInputEditText inputChannelName;
    private TextInputEditText inputDeposit;
    private TextInputEditText inputDescription;
    private TextInputEditText inputWebsite;
    private TextInputEditText inputEmail;

    private TextInputEditText inputTagFilter;
    private RecyclerView addedTagsList;
    private RecyclerView suggestedTagsList;
    private TagListAdapter addedTagsAdapter;
    private TagListAdapter suggestedTagsAdapter;
    private View noTagsView;
    private View noTagResultsView;

    private View coverEditArea;
    private View iconContainer;
    private View channelSaveProgress;

    private boolean launchCoverSelectPending;
    private boolean launchThumbnailSelectPending;
    private boolean coverFilePickerActive;
    private boolean thumbnailFilePickerActive;

    private String currentFilter;

    private String coverUrl;
    private String thumbnailUrl;
    private String lastSelectedCoverFile;
    private String lastSelectedThumbnailFile;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_channel_form, container, false);

        scrollView = root.findViewById(R.id.channel_form_scroll_view);
        linkCancel = root.findViewById(R.id.channel_form_cancel_link);
        linkShowOptional = root.findViewById(R.id.channel_form_toggle_optional);
        buttonSave = root.findViewById(R.id.channel_form_save_button);

        containerOptionalFields = root.findViewById(R.id.channel_form_optional_fields_container);
        inputTitle = root.findViewById(R.id.channel_form_input_title);
        inputChannelName = root.findViewById(R.id.channel_form_input_channel_name);
        inputDeposit = root.findViewById(R.id.channel_form_input_deposit);
        inputDescription = root.findViewById(R.id.channel_form_input_description);
        inputWebsite = root.findViewById(R.id.channel_form_input_website);
        inputEmail = root.findViewById(R.id.channel_form_input_email);
        inputTagFilter = root.findViewById(R.id.form_tag_filter_input);

        coverEditArea = root.findViewById(R.id.channel_form_cover_edit_area);
        iconContainer = root.findViewById(R.id.channel_form_icon_container);
        imageCover = root.findViewById(R.id.channel_form_cover_image);
        imageThumbnail = root.findViewById(R.id.channel_form_thumbnail);
        inlineBalanceContainer = root.findViewById(R.id.channel_form_inline_balance_container);
        inlineBalanceValue = root.findViewById(R.id.channel_form_inline_balance_value);
        uploadProgress = root.findViewById(R.id.channel_form_upload_progress);
        channelSaveProgress = root.findViewById(R.id.channel_form_save_progress);

        Context context = getContext();
        FlexboxLayoutManager flm1 = new FlexboxLayoutManager(context);
        FlexboxLayoutManager flm2 = new FlexboxLayoutManager(context);
        FlexboxLayoutManager flm3 = new FlexboxLayoutManager(context);
        addedTagsList = root.findViewById(R.id.form_added_tags);
        addedTagsList.setLayoutManager(flm1);
        suggestedTagsList = root.findViewById(R.id.form_suggested_tags);
        suggestedTagsList.setLayoutManager(flm2);

        addedTagsAdapter = new TagListAdapter(new ArrayList<>(), context);
        addedTagsAdapter.setCustomizeMode(TagListAdapter.CUSTOMIZE_MODE_REMOVE);
        addedTagsAdapter.setClickListener(this);
        addedTagsList.setAdapter(addedTagsAdapter);

        suggestedTagsAdapter = new TagListAdapter(new ArrayList<>(), getContext());
        suggestedTagsAdapter.setCustomizeMode(TagListAdapter.CUSTOMIZE_MODE_ADD);
        suggestedTagsAdapter.setClickListener(this);
        suggestedTagsList.setAdapter(suggestedTagsAdapter);

        noTagsView = root.findViewById(R.id.form_no_added_tags);
        noTagResultsView = root.findViewById(R.id.form_no_tag_results);

        buttonSave = root.findViewById(R.id.channel_form_save_button);

        inputDeposit.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                Helper.setViewVisibility(inlineBalanceContainer, hasFocus ? View.VISIBLE : View.INVISIBLE);
            }
        });
        linkShowOptional.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (containerOptionalFields.getVisibility() != View.VISIBLE) {
                    containerOptionalFields.setVisibility(View.VISIBLE);
                    linkShowOptional.setText(R.string.hide_optional_fields);
                    scrollView.post(new Runnable() {
                        @Override
                        public void run() {
                            scrollView.fullScroll(NestedScrollView.FOCUS_DOWN);
                        }
                    });
                } else {
                    containerOptionalFields.setVisibility(View.GONE);
                    linkShowOptional.setText(R.string.show_optional_fields);
                }
            }
        });
        linkCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clearInputFocus();

                Context context = getContext();
                if (context instanceof MainActivity) {
                    ((MainActivity) context).onBackPressed();
                }
            }
        });
        coverEditArea.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (uploading) {
                    Snackbar.make(getView(), R.string.wait_for_upload, Snackbar.LENGTH_LONG).show();
                    return;
                }

                checkPermissionsAndLaunchFilePicker(true);
            }
        });
        iconContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (uploading) {
                    Snackbar.make(view, R.string.wait_for_upload, Snackbar.LENGTH_LONG).show();
                    return;
                }

                checkPermissionsAndLaunchFilePicker(false);
            }
        });

        buttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (uploading) {
                    Snackbar.make(view, R.string.wait_for_upload, Snackbar.LENGTH_LONG).show();
                    return;
                }

                Claim claimToSave = buildChannelClaimToSave();
                validateAndSaveClaim(claimToSave);
            }
        });
        inputTagFilter.addTextChangedListener(new TextWatcher() {
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

        return root;
    }

    private void checkParams() {
        Map<String, Object> params = getParams();
        if (params.containsKey("claim")) {
            Claim claim = (Claim) params.get("claim");
            if (claim != null && !claim.equals(this.currentClaim)) {
                this.currentClaim = claim;
                editFieldsLoaded = false;
            }
        }
    }

    private void updateFieldsFromCurrentClaim() {
        if (currentClaim != null && !editFieldsLoaded) {
            inputTitle.setText(currentClaim.getTitle());
            inputChannelName.setText(currentClaim.getName());
            inputDeposit.setText(currentClaim.getAmount());
            inputEmail.setText(currentClaim.getEmail());
            inputWebsite.setText(currentClaim.getWebsiteUrl());
            inputDescription.setText(currentClaim.getDescription());
            if (currentClaim.getTagObjects() != null) {
                addedTagsAdapter.addTags(currentClaim.getTagObjects());
            }

            Context context = getContext();
            if (context != null) {
                if (!Helper.isNullOrEmpty(currentClaim.getCoverUrl())) {
                    Glide.with(context.getApplicationContext()).load(currentClaim.getCoverUrl()).centerCrop().into(imageCover);
                    coverUrl = currentClaim.getCoverUrl();
                }
                if (!Helper.isNullOrEmpty(currentClaim.getThumbnailUrl())) {
                    Glide.with(context.getApplicationContext()).load(currentClaim.getThumbnailUrl()).apply(RequestOptions.circleCropTransform()).into(imageThumbnail);
                    thumbnailUrl = currentClaim.getThumbnailUrl();
                }
            }

            inputChannelName.setEnabled(false);
            editMode = true;
            editFieldsLoaded = true;
        }
    }

    private void validateAndSaveClaim(Claim claim) {
        if (!editMode && Helper.isNullOrEmpty(claim.getName())) {
            showError(getString(R.string.please_enter_channel_name));
            return;
        }

        String channelName = claim.getName().startsWith("@") ? claim.getName().substring(1) : claim.getName();
        if (!LbryUri.isNameValid(channelName)) {
            showError(getString(R.string.channel_name_invalid_characters));
            return;
        }
        if (!editMode) {
            if (Helper.channelExists(channelName)) {
                showError(getString(R.string.channel_name_already_created));
                return;
            }
        }

        String depositString = Helper.getValue(inputDeposit.getText());
        double depositAmount = 0;
        try {
            depositAmount = Double.valueOf(depositString);
        } catch (NumberFormatException ex) {
            // pass
            showError(getString(R.string.please_enter_valid_deposit));
            return;
        }
        if (depositAmount == 0) {
            String error = getResources().getQuantityString(R.plurals.min_deposit_required, depositAmount == 1 ? 1 : 2, String.valueOf(Helper.MIN_DEPOSIT));
            showError(error);
            return;
        }
        if (Lbry.walletBalance == null || Lbry.walletBalance.getAvailable().doubleValue() < depositAmount) {
            showError(getString(R.string.deposit_more_than_balance));
            return;
        }

        ChannelCreateUpdateTask task = new ChannelCreateUpdateTask(claim, new BigDecimal(depositString), editMode, channelSaveProgress, new ClaimResultHandler() {
            @Override
            public void beforeStart() {
                preSave();
            }

            @Override
            public void onSuccess(Claim claimResult) {
                postSave();

                // Run the logPublish task
                if (!BuildConfig.DEBUG) {
                    LogPublishTask logPublish = new LogPublishTask(claimResult);
                    logPublish.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                }

                if (!editMode) {
                    // channel created
                    Bundle bundle = new Bundle();
                    bundle.putString("claim_id", claimResult.getClaimId());
                    bundle.putString("claim_name", claimResult.getName());
                    LbryAnalytics.logEvent(LbryAnalytics.EVENT_CHANNEL_CREATE, bundle);
                }

                Context context = getContext();
                if (context instanceof MainActivity) {
                    MainActivity activity = (MainActivity) context;
                    activity.showMessage(R.string.channel_save_successful);
                    activity.onBackPressed();
                }
            }

            @Override
            public void onError(Exception error) {
                showError(error != null ? error.getMessage() : getString(R.string.channel_save_failed));
                postSave();
            }
        });
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void showError(String message) {
        Context context = getContext();
        if (context != null) {
            Snackbar.make(getView(), message, Snackbar.LENGTH_LONG).setBackgroundTint(Color.RED).setTextColor(Color.WHITE).show();
        }
    }

    public void checkPermissionsAndLaunchFilePicker(boolean isCover) {
        Context context = getContext();
        if (MainActivity.hasPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, context)) {
            launchCoverSelectPending = false;
            launchThumbnailSelectPending = false;

            coverFilePickerActive = isCover;
            thumbnailFilePickerActive = !isCover;
            launchFilePicker();
        } else {
            launchCoverSelectPending = isCover;
            launchThumbnailSelectPending = !isCover;
            MainActivity.requestPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    MainActivity.REQUEST_STORAGE_PERMISSION,
                    getString(R.string.storage_permission_rationale_images),
                    context,
                    true);
        }
    }

    private void launchFilePicker() {
        Context context = getContext();
        if (context instanceof MainActivity) {
            MainActivity.startingFilePickerActivity = true;
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.setType("image/*");
            ((MainActivity) context).startActivityForResult(
                    Intent.createChooser(intent, getString(coverFilePickerActive ? R.string.select_cover : R.string.select_thumbnail)),
                    MainActivity.REQUEST_FILE_PICKER);
        }
    }

    public void onFilePickerCancelled() {
        coverFilePickerActive = false;
        thumbnailFilePickerActive = false;
    }

    public void onFilePicked(String filePath) {
        if (Helper.isNullOrEmpty(filePath)) {
            Snackbar.make(getView(), R.string.undetermined_image_filepath, Snackbar.LENGTH_LONG).setBackgroundTint(
                    ContextCompat.getColor(getContext(), R.color.red)).show();
            return;
        }

        Context context = getContext();
        if (context != null) {
            Uri fileUri = Uri.fromFile(new File(filePath));
            if (coverFilePickerActive) {
                // cover selected
                if (filePath.equalsIgnoreCase(lastSelectedCoverFile)) {
                    // previous selected cover was uploaded successfully
                    return;
                }
                Glide.with(context.getApplicationContext()).load(fileUri).centerCrop().into(imageCover);
            } else if (thumbnailFilePickerActive) {
                if (filePath.equalsIgnoreCase(lastSelectedThumbnailFile)) {
                    // previous selected thumbnail was uploaded successfully
                    return;
                }
                // thumbnail selected
                Glide.with(context.getApplicationContext()).load(fileUri).apply(RequestOptions.circleCropTransform()).into(imageThumbnail);
            }

            // Upload the image
            uploading = true;
            UploadImageTask task = new UploadImageTask(filePath, uploadProgress, new UploadImageTask.UploadThumbnailHandler() {
                @Override
                public void onSuccess(String url) {
                    if (coverFilePickerActive) {
                        coverUrl = url;
                        lastSelectedCoverFile = filePath;
                    } else if (thumbnailFilePickerActive) {
                        thumbnailUrl = url;
                        lastSelectedThumbnailFile = filePath;
                    }

                    coverFilePickerActive = false;
                    thumbnailFilePickerActive = false;
                    uploading = false;
                }

                @Override
                public void onError(Exception error) {
                    if (getContext() != null) {
                        showError(getString(R.string.image_upload_failed));
                    }
                    if (coverFilePickerActive) {
                        // cover selected
                        imageCover.setImageResource(R.drawable.default_channel_cover);
                    } else if (thumbnailFilePickerActive) {
                        imageThumbnail.setImageDrawable(null);
                    }
                    coverFilePickerActive = false;
                    thumbnailFilePickerActive = false;
                    uploading = false;
                }
            });
            task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } else {
            coverFilePickerActive = false;
            thumbnailFilePickerActive = false;
        }
    }

    public void onStoragePermissionGranted() {
        if (launchCoverSelectPending) {
            checkPermissionsAndLaunchFilePicker(true);
        } else if (launchThumbnailSelectPending) {
            checkPermissionsAndLaunchFilePicker(false);
        }
    }
    public void onStoragePermissionRefused() {
        Snackbar.make(getView(), R.string.storage_permission_rationale_images, Snackbar.LENGTH_LONG).setBackgroundTint(
                ContextCompat.getColor(getContext(), R.color.red)
        ).show();
    }

    @Override
    public void onStart() {
        super.onStart();
        MainActivity activity = (MainActivity) getContext();
        if (activity != null) {
            activity.hideSearchBar();
            activity.showNavigationBackIcon();
            activity.lockDrawer();
            activity.hideFloatingWalletBalance();

            activity.addFilePickerListener(this);
            activity.addWalletBalanceListener(this);

            activity.setActionBarTitle(editMode ? R.string.edit_channel : R.string.create_a_channel);

        }
    }

    @Override
    public void onPause() {
        clearInputFocus();
        super.onPause();
    }

    @Override
    public void onStop() {
        Context context = getContext();
        if (context instanceof MainActivity) {
            MainActivity activity = (MainActivity) getContext();
            activity.removeStoragePermissionListener(this);
            activity.removeWalletBalanceListener(this);
            activity.restoreToggle();
            activity.showFloatingWalletBalance();
            if (!MainActivity.startingFilePickerActivity) {
                activity.removeFilePickerListener(this);
                activity.removeNavFragment(ChannelFormFragment.class, NavMenuItem.ID_ITEM_CHANNELS);
            }
        }
        super.onStop();
    }

    public void onResume() {
        super.onResume();
        checkParams();
        checkRewardsDriver();
        updateFieldsFromCurrentClaim();

        Context context = getContext();
        if (context instanceof MainActivity) {
            MainActivity activity = (MainActivity) context;
            LbryAnalytics.setCurrentScreen(activity, "Channel Form", "ChannelForm");
            activity.addStoragePermissionListener(this);
            if (editMode) {
                activity.setActionBarTitle(R.string.edit_channel);
            }
        }
        String filterText = Helper.getValue(inputTagFilter.getText());
        updateSuggestedTags(filterText, SUGGESTED_LIMIT, true);
    }

    private void checkNoAddedTags() {
        Helper.setViewVisibility(noTagsView, addedTagsAdapter == null || addedTagsAdapter.getItemCount() == 0 ? View.VISIBLE : View.GONE);
    }
    private void checkNoTagResults() {
        Helper.setViewVisibility(noTagResultsView, suggestedTagsAdapter == null || suggestedTagsAdapter.getItemCount() == 0 ? View.VISIBLE : View.GONE);
    }
    public void addTag(Tag tag) {
        if (addedTagsAdapter.getTags().contains(tag)) {
            Snackbar.make(getView(), getString(R.string.tag_already_added, tag.getName()), Snackbar.LENGTH_LONG).show();
            return;
        }
        if (addedTagsAdapter.getItemCount() == 5) {
            Snackbar.make(getView(), R.string.tag_limit_reached, Snackbar.LENGTH_LONG).show();
            return;
        }

        addedTagsAdapter.addTag(tag);
        if (suggestedTagsAdapter != null) {
            suggestedTagsAdapter.removeTag(tag);
        }
        updateSuggestedTags(currentFilter, SUGGESTED_LIMIT, false);

        checkNoAddedTags();
        checkNoTagResults();
    }
    public void removeTag(Tag tag) {
        addedTagsAdapter.removeTag(tag);
        updateSuggestedTags(currentFilter, SUGGESTED_LIMIT, false);
        checkNoAddedTags();
        checkNoTagResults();
    }

    @Override
    public void onWalletBalanceUpdated(WalletBalance walletBalance) {
        if (walletBalance != null && inlineBalanceValue != null) {
            inlineBalanceValue.setText(Helper.shortCurrencyFormat(walletBalance.getAvailable().doubleValue()));
        }
        checkRewardsDriver();
    }

    public void setFilter(String filter) {
        currentFilter = filter;
        updateSuggestedTags(currentFilter, SUGGESTED_LIMIT, true);
    }

    private void updateSuggestedTags(String filter, int limit, boolean clearPrevious) {
        UpdateSuggestedTagsTask task = new UpdateSuggestedTagsTask(
                filter,
                limit,
                addedTagsAdapter,
                suggestedTagsAdapter,
                clearPrevious,
                false,
                new UpdateSuggestedTagsTask.KnownTagsHandler() {
            @Override
            public void onSuccess(List<Tag> tags) {
                if (suggestedTagsAdapter == null) {
                    suggestedTagsAdapter = new TagListAdapter(tags, getContext());
                    suggestedTagsAdapter.setCustomizeMode(TagListAdapter.CUSTOMIZE_MODE_ADD);
                    suggestedTagsAdapter.setClickListener(ChannelFormFragment.this);
                    if (suggestedTagsList != null) {
                        suggestedTagsList.setAdapter(suggestedTagsAdapter);
                    }
                } else {
                    suggestedTagsAdapter.setTags(tags);
                }

                checkNoAddedTags();
                checkNoTagResults();
            }
        });
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private Claim buildChannelClaimToSave() {
        Claim claim = new Claim();
        String name = Helper.getValue(inputChannelName.getText());
        if (!name.startsWith("@")) {
            name = String.format("@%s", name);
        }
        claim.setName(name);

        if (currentClaim != null) {
            claim.setClaimId(currentClaim.getClaimId());
        }

        Claim.ChannelMetadata metadata = new Claim.ChannelMetadata();
        metadata.setTitle(Helper.getValue(inputTitle.getText()));
        metadata.setDescription(Helper.getValue(inputDescription.getText()));
        metadata.setWebsiteUrl(Helper.getValue(inputWebsite.getText()));
        metadata.setEmail(Helper.getValue(inputEmail.getText()));

        Claim.Resource cover = new Claim.Resource();
        cover.setUrl(coverUrl == null ? "" : coverUrl);
        Claim.Resource thumbnail = new Claim.Resource();
        thumbnail.setUrl(thumbnailUrl == null ? "" : thumbnailUrl);
        metadata.setThumbnail(thumbnail);
        metadata.setCover(cover);

        List<Tag> addedTags = addedTagsAdapter != null ? new ArrayList<>(addedTagsAdapter.getTags()) : new ArrayList<>();
        metadata.setTags(Helper.getTagsForTagObjects(addedTags));

        claim.setValue(metadata);
        return claim;
    }

    private void preSave() {
        saveInProgress = true;
        Helper.setViewVisibility(linkShowOptional, View.GONE);
        Helper.setViewEnabled(linkCancel, false);
        Helper.setViewEnabled(buttonSave,  false);
    }

    private void postSave() {
        Helper.setViewVisibility(linkShowOptional, View.VISIBLE);
        Helper.setViewEnabled(linkCancel, true);
        Helper.setViewEnabled(buttonSave,  true);

        clearInputFocus();

        saveInProgress = false;
    }

    public void clearInputFocus() {
        inputChannelName.clearFocus();
        inputDeposit.clearFocus();
        inputWebsite.clearFocus();
        inputEmail.clearFocus();
        inputDescription.clearFocus();
        inputTitle.clearFocus();
        inputTagFilter.clearFocus();
    }

    @Override
    public void onTagClicked(Tag tag, int customizeMode) {
        if (customizeMode == TagListAdapter.CUSTOMIZE_MODE_ADD) {
            addTag(tag);
        } else if (customizeMode == TagListAdapter.CUSTOMIZE_MODE_REMOVE) {
            removeTag(tag);
        }
    }

    private void checkRewardsDriver() {
        Context ctx = getContext();
        if (ctx != null) {
            String rewardsDriverText = String.format("%s\n%s",
                    getString(R.string.channel_creation_requires_credits), getString(R.string.tap_here_to_get_some));
            checkRewardsDriverCard(rewardsDriverText, Helper.MIN_DEPOSIT);
        }
    }
}
