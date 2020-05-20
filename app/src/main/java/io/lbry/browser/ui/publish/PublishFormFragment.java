package io.lbry.browser.ui.publish;

import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.AppCompatSpinner;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.flexbox.FlexboxLayoutManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;

import java.io.File;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import io.lbry.browser.BuildConfig;
import io.lbry.browser.MainActivity;
import io.lbry.browser.R;
import io.lbry.browser.adapter.InlineChannelSpinnerAdapter;
import io.lbry.browser.adapter.TagListAdapter;
import io.lbry.browser.listener.SdkStatusListener;
import io.lbry.browser.listener.StoragePermissionListener;
import io.lbry.browser.listener.WalletBalanceListener;
import io.lbry.browser.model.Claim;
import io.lbry.browser.model.GalleryItem;
import io.lbry.browser.model.NavMenuItem;
import io.lbry.browser.model.Tag;
import io.lbry.browser.model.WalletBalance;
import io.lbry.browser.tasks.ChannelCreateUpdateTask;
import io.lbry.browser.tasks.UpdateSuggestedTagsTask;
import io.lbry.browser.tasks.claim.ClaimListResultHandler;
import io.lbry.browser.tasks.claim.ClaimListTask;
import io.lbry.browser.tasks.claim.ClaimResultHandler;
import io.lbry.browser.tasks.lbryinc.LogPublishTask;
import io.lbry.browser.ui.BaseFragment;
import io.lbry.browser.utils.Helper;
import io.lbry.browser.utils.Lbry;
import io.lbry.browser.utils.LbryAnalytics;
import io.lbry.browser.utils.LbryUri;
import io.lbry.browser.utils.Predefined;

public class PublishFormFragment extends BaseFragment implements
        SdkStatusListener, StoragePermissionListener, TagListAdapter.TagClickListener, WalletBalanceListener {

    private static final int SUGGESTED_LIMIT = 8;

    private boolean editMode;
    private boolean fetchingChannels;
    private String currentFilter;

    private TextInputEditText inputTagFilter;
    private RecyclerView addedTagsList;
    private RecyclerView suggestedTagsList;
    private RecyclerView matureTagsList;
    private TagListAdapter addedTagsAdapter;
    private TagListAdapter suggestedTagsAdapter;
    private TagListAdapter matureTagsAdapter;
    private ProgressBar progressLoadingChannels;
    private View noTagsView;
    private View noTagResultsView;

    private InlineChannelSpinnerAdapter channelSpinnerAdapter;
    private AppCompatSpinner channelSpinner;
    private AppCompatSpinner priceCurrencySpinner;
    private AppCompatSpinner languageSpinner;
    private AppCompatSpinner licenseSpinner;

    private NestedScrollView scrollView;
    private View layoutExtraFields;
    private TextView linkShowExtraFields;
    private View textNoPrice;
    private View layoutPrice;
    private SwitchMaterial switchPrice;

    private TextInputEditText inputTitle;
    private TextInputEditText inputDescription;
    private TextInputEditText inputPrice;
    private TextInputEditText inputAddress;
    private TextInputEditText inputDeposit;
    private View inlineDepositBalanceContainer;
    private TextView inlineDepositBalanceValue;

    private View linkCancel;
    private MaterialButton buttonPublish;

    private View inlineChannelCreator;
    private TextInputEditText inlineChannelCreatorInputName;
    private TextInputEditText inlineChannelCreatorInputDeposit;
    private View inlineChannelCreatorInlineBalance;
    private TextView inlineChannelCreatorInlineBalanceValue;
    private View inlineChannelCreatorCancelLink;
    private View inlineChannelCreatorProgress;
    private MaterialButton inlineChannelCreatorCreateButton;

    private String uploadedThumbnailUrl;
    private boolean editFieldsLoaded;
    private Claim currentClaim;
    private GalleryItem currentGalleryItem;
    private String currentFilePath;
    private boolean fileLoaded;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_publish_form, container, false);

        scrollView = root.findViewById(R.id.publish_form_scroll_view);
        progressLoadingChannels = root.findViewById(R.id.publish_form_loading_channels);
        channelSpinner = root.findViewById(R.id.publish_form_channel_spinner);

        inputTagFilter = root.findViewById(R.id.form_tag_filter_input);
        noTagsView = root.findViewById(R.id.form_no_added_tags);
        noTagResultsView = root.findViewById(R.id.form_no_tag_results);

        layoutExtraFields = root.findViewById(R.id.publish_form_extra_options_container);
        linkShowExtraFields = root.findViewById(R.id.publish_form_toggle_extra);
        layoutPrice = root.findViewById(R.id.publish_form_price_container);
        textNoPrice = root.findViewById(R.id.publish_form_no_price);
        switchPrice = root.findViewById(R.id.publish_form_price_switch);

        inputTitle = root.findViewById(R.id.publish_form_input_title);
        inputDeposit = root.findViewById(R.id.publish_form_input_description);
        inputPrice = root.findViewById(R.id.publish_form_input_price);
        inputAddress = root.findViewById(R.id.publish_form_input_address);
        inputDeposit = root.findViewById(R.id.publish_form_input_deposit);

        linkCancel = root.findViewById(R.id.publish_form_cancel);
        buttonPublish = root.findViewById(R.id.publish_form_publish_button);

        Context context = getContext();
        FlexboxLayoutManager flm1 = new FlexboxLayoutManager(context);
        FlexboxLayoutManager flm2 = new FlexboxLayoutManager(context);
        FlexboxLayoutManager flm3 = new FlexboxLayoutManager(context);
        addedTagsList = root.findViewById(R.id.form_added_tags);
        addedTagsList.setLayoutManager(flm1);
        suggestedTagsList = root.findViewById(R.id.form_suggested_tags);
        suggestedTagsList.setLayoutManager(flm2);

        root.findViewById(R.id.form_mature_tags_container).setVisibility(View.VISIBLE);
        matureTagsList = root.findViewById(R.id.form_mature_tags);
        matureTagsList.setLayoutManager(flm3);

        addedTagsAdapter = new TagListAdapter(new ArrayList<>(), context);
        addedTagsAdapter.setCustomizeMode(TagListAdapter.CUSTOMIZE_MODE_REMOVE);
        addedTagsAdapter.setClickListener(this);
        addedTagsList.setAdapter(addedTagsAdapter);

        suggestedTagsAdapter = new TagListAdapter(new ArrayList<>(), getContext());
        suggestedTagsAdapter.setCustomizeMode(TagListAdapter.CUSTOMIZE_MODE_ADD);
        suggestedTagsAdapter.setClickListener(this);
        suggestedTagsList.setAdapter(suggestedTagsAdapter);

        matureTagsAdapter = new TagListAdapter(Helper.getTagObjectsForTags(Predefined.MATURE_TAGS), context);
        matureTagsAdapter.setCustomizeMode(TagListAdapter.CUSTOMIZE_MODE_ADD);
        matureTagsAdapter.setClickListener(this);
        matureTagsList.setAdapter(matureTagsAdapter);

        inlineChannelCreator = root.findViewById(R.id.container_inline_channel_form_create);
        inlineChannelCreatorInputName = root.findViewById(R.id.inline_channel_form_input_name);
        inlineChannelCreatorInputDeposit = root.findViewById(R.id.inline_channel_form_input_deposit);
        inlineChannelCreatorInlineBalance = root.findViewById(R.id.inline_channel_form_inline_balance_container);
        inlineChannelCreatorInlineBalanceValue = root.findViewById(R.id.inline_channel_form_inline_balance_value);
        inlineChannelCreatorProgress = root.findViewById(R.id.inline_channel_form_create_progress);
        inlineChannelCreatorCancelLink = root.findViewById(R.id.inline_channel_form_cancel_link);
        inlineChannelCreatorCreateButton = root.findViewById(R.id.inline_channel_form_create_button);

        initUi();

        return root;
    }

    private void initUi() {

        inputAddress.setText(Helper.generateUrl());

        switchPrice.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                Helper.setViewVisibility(textNoPrice, checked ? View.GONE : View.VISIBLE);
                Helper.setViewVisibility(layoutPrice, checked ? View.VISIBLE : View.GONE);
            }
        });

        inputDeposit.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                Helper.setViewVisibility(inlineDepositBalanceContainer, hasFocus ? View.VISIBLE : View.GONE);
            }
        });

        linkShowExtraFields.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (layoutExtraFields.getVisibility() != View.VISIBLE) {
                    layoutExtraFields.setVisibility(View.VISIBLE);
                    linkShowExtraFields.setText(R.string.hide_extra_fields);
                    scrollView.post(new Runnable() {
                        @Override
                        public void run() {
                            scrollView.fullScroll(NestedScrollView.FOCUS_DOWN);
                        }
                    });
                } else {
                    layoutExtraFields.setVisibility(View.GONE);
                    linkShowExtraFields.setText(R.string.show_extra_fields);
                }
            }
        });

        linkCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Context context = getContext();
                if (context instanceof MainActivity) {
                    ((MainActivity) context).onBackPressed();
                }
            }
        });

        channelSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                Object item = adapterView.getItemAtPosition(position);
                if (item instanceof Claim) {
                    Claim claim = (Claim) item;
                    if (claim.isPlaceholder() && !claim.isPlaceholderAnonymous()) {
                        if (!fetchingChannels) {
                            showInlineChannelCreator();
                        }
                    } else {
                        hideInlineChannelCreator();
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

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

        buttonPublish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        setupInlineChannelCreator(
                inlineChannelCreator,
                inlineChannelCreatorInputName,
                inlineChannelCreatorInputDeposit,
                inlineChannelCreatorInlineBalance,
                inlineChannelCreatorInlineBalanceValue,
                inlineChannelCreatorCancelLink,
                inlineChannelCreatorCreateButton,
                inlineChannelCreatorProgress
        );
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
            activity.addWalletBalanceListener(this);

            ActionBar actionBar = activity.getSupportActionBar();
            if (actionBar != null) {
                actionBar.setTitle(editMode ? R.string.edit_content : R.string.new_publish);
            }
        }
    }

    @Override
    public void onStop() {
        Context context = getContext();
        if (context instanceof MainActivity) {
            MainActivity activity = (MainActivity) getContext();
            activity.removeWalletBalanceListener(this);
            activity.restoreToggle();
            activity.showFloatingWalletBalance();
            if (!MainActivity.startingFilePickerActivity) {
                activity.removeNavFragment(PublishFormFragment.class, NavMenuItem.ID_ITEM_NEW_PUBLISH);
            }
        }
        super.onStop();
    }

    private void checkParams() {
        Map<String, Object> params = getParams();
        if (params != null) {
            if (params.containsKey("claim")) {
                Claim claim = (Claim) params.get("claim");
                if (claim != null && !claim.equals(this.currentClaim)) {
                    this.currentClaim = claim;
                    editFieldsLoaded = false;
                }
            }

            if (params.containsKey("galleryItem")) {
                currentGalleryItem = (GalleryItem) params.get("galleryItem");
            } else if (params.containsKey("directFilePath")) {
                currentFilePath = (String) params.get("directFilePath");
            }
        } else {
            // shouldn't actually happen
            cancelOnFatalCondition(getString(R.string.no_file_found));
        }
    }

    private void updateFieldsFromCurrentClaim() {
        if (currentClaim != null && !editFieldsLoaded) {


        }
    }

    private void checkPublishFile() {
        String filePath = "";
        if (currentGalleryItem != null) {
            // check gallery item type
            filePath = currentGalleryItem.getFilePath();
        } else if (currentFilePath != null) {
            filePath = currentFilePath;
        }

        android.util.Log.d("#HELP", "filePath=" + filePath);
        File file = new File(filePath);
        if (!file.exists()) {
            // file doesn't exist. although this shouldn't happen
            cancelOnFatalCondition(getString(R.string.no_file_found));
            return;
        }



        // check content type
        String type = null;
        String extension = MimeTypeMap.getFileExtensionFromUrl(Uri.fromFile(file).toString());
        if (extension != null) {
            type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
        }
        android.util.Log.d("#HELP", "fileType=" + type);

        if (!Helper.isNullOrEmpty(type) && type.startsWith("video")) {
            // ffmpeg video handling
        }
    }

    private void cancelOnFatalCondition(String message) {
        Context context = getContext();
        if (context instanceof MainActivity) {
            MainActivity activity = (MainActivity) context;
            activity.showError(message);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    activity.onBackPressed();
                }
            }, 100);
        }
    }

    public void onResume() {
        super.onResume();
        if (!Lbry.SDK_READY) {
            cancelOnFatalCondition(getString(R.string.sdk_initializing_functionality));
            return;
        }

        checkParams();
        updateFieldsFromCurrentClaim();

        if (currentClaim == null && (currentGalleryItem != null || (Helper.isNullOrEmpty(currentFilePath)))) {
            // load file information
            checkPublishFile();
        }

        Context context = getContext();
        if (context instanceof MainActivity) {
            MainActivity activity = (MainActivity) context;
            LbryAnalytics.setCurrentScreen(activity, "Channel Form", "ChannelForm");
            activity.addStoragePermissionListener(this);
            if (editMode) {
                ActionBar actionBar = activity.getSupportActionBar();
                if (actionBar != null) {
                    actionBar.setTitle(R.string.edit_content);
                }
            }
        }

        if (!Lbry.SDK_READY) {
            if (context instanceof MainActivity) {
                MainActivity activity = (MainActivity) context;
                activity.addSdkStatusListener(this);
            }
        } else {
            onSdkReady();
        }

        String filterText = Helper.getValue(inputTagFilter.getText());
        updateSuggestedTags(filterText, SUGGESTED_LIMIT, true);
    }

    public void onSdkReady() {
        fetchChannels();
        onWalletBalanceUpdated(Lbry.walletBalance);
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
        Helper.setViewEnabled(channelSpinner, false);
        hideInlineChannelCreator();
    }
    private void enableChannelSpinner() {
        Helper.setViewEnabled(channelSpinner, true);
        if (channelSpinner != null) {
            Claim selectedClaim = (Claim) channelSpinner.getSelectedItem();
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
        if (channelSpinnerAdapter == null) {
            Context context = getContext();
            if (context != null) {
                channelSpinnerAdapter = new InlineChannelSpinnerAdapter(context, R.layout.spinner_item_channel, new ArrayList<>(channels));
                channelSpinnerAdapter.addPlaceholder(true);
                channelSpinnerAdapter.notifyDataSetChanged();
            }
        } else {
            channelSpinnerAdapter.clear();
            channelSpinnerAdapter.addAll(channels);
            channelSpinnerAdapter.addPlaceholder(true);
            channelSpinnerAdapter.notifyDataSetChanged();
        }

        if (channelSpinner != null) {
            channelSpinner.setAdapter(channelSpinnerAdapter);
        }

        if (channelSpinnerAdapter != null && channelSpinner != null) {
            if (channelSpinnerAdapter.getCount() > 2) {
                // if anonymous displayed, select first channel if available
                channelSpinner.setSelection(2);
            } else if (channelSpinnerAdapter.getCount() > 1) {
                // select anonymous
                channelSpinner.setSelection(1);
            }
        }
    }

    private Claim buildPublishClaim() {
        Claim claim = new Claim();

        return claim;
    }

    private boolean validatePublishClaim() {
        return false;
    }


    @Override
    public boolean shouldHideGlobalPlayer() {
        return true;
    }

    @Override
    public boolean shouldSuspendGlobalPlayer() {
        return true;
    }

    @Override
    public void onTagClicked(Tag tag, int customizeMode) {
        if (customizeMode == TagListAdapter.CUSTOMIZE_MODE_ADD) {
            addTag(tag);
        } else if (customizeMode == TagListAdapter.CUSTOMIZE_MODE_REMOVE) {
            removeTag(tag);
        }
    }

    public void setFilter(String filter) {
        currentFilter = filter;
        updateSuggestedTags(currentFilter, SUGGESTED_LIMIT, true);
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
    private void updateSuggestedTags(String filter, int limit, boolean clearPrevious) {
        UpdateSuggestedTagsTask task = new UpdateSuggestedTagsTask(
                filter,
                limit,
                addedTagsAdapter,
                suggestedTagsAdapter,
                clearPrevious,
                true, new UpdateSuggestedTagsTask.KnownTagsHandler() {
            @Override
            public void onSuccess(List<Tag> tags) {
                if (suggestedTagsAdapter == null) {
                    suggestedTagsAdapter = new TagListAdapter(tags, getContext());
                    suggestedTagsAdapter.setCustomizeMode(TagListAdapter.CUSTOMIZE_MODE_ADD);
                    suggestedTagsAdapter.setClickListener(PublishFormFragment.this);
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

    @Override
    public void onWalletBalanceUpdated(WalletBalance walletBalance) {
        if (walletBalance != null && inlineDepositBalanceValue != null) {
            inlineDepositBalanceValue.setText(Helper.shortCurrencyFormat(walletBalance.getAvailable().doubleValue()));
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
            View progressView) {
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
                if (depositAmount == 0) {
                    String error = getResources().getQuantityString(R.plurals.min_deposit_required, depositAmount == 1 ? 1 : 2, String.valueOf(Helper.MIN_DEPOSIT));
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
                        channelSpinnerAdapter.add(claimResult);
                        channelSpinner.setSelection(channelSpinnerAdapter.getCount() - 1);

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

    private void showError(String message) {
        Context context = getContext();
        if (context != null) {
            Snackbar.make(getView(), message, Snackbar.LENGTH_LONG).setBackgroundTint(Color.RED).show();
        }
    }

    @Override
    public void onStoragePermissionGranted() {

    }

    @Override
    public void onStoragePermissionRefused() {

    }
}
