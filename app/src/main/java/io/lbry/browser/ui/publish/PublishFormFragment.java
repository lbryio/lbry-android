package io.lbry.browser.ui.publish;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.AppCompatSpinner;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.RecyclerView;

import com.arthenica.mobileffmpeg.Config;
import com.arthenica.mobileffmpeg.FFmpeg;
import com.arthenica.mobileffmpeg.FFprobe;

import com.arthenica.mobileffmpeg.Statistics;
import com.arthenica.mobileffmpeg.StatisticsCallback;
import com.bumptech.glide.Glide;
import com.google.android.flexbox.FlexboxLayoutManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import io.lbry.browser.BuildConfig;
import io.lbry.browser.MainActivity;
import io.lbry.browser.R;
import io.lbry.browser.adapter.InlineChannelSpinnerAdapter;
import io.lbry.browser.adapter.LanguageSpinnerAdapter;
import io.lbry.browser.adapter.LicenseSpinnerAdapter;
import io.lbry.browser.adapter.TagListAdapter;
import io.lbry.browser.listener.FilePickerListener;
import io.lbry.browser.listener.SdkStatusListener;
import io.lbry.browser.listener.StoragePermissionListener;
import io.lbry.browser.listener.WalletBalanceListener;
import io.lbry.browser.model.Claim;
import io.lbry.browser.model.Fee;
import io.lbry.browser.model.GalleryItem;
import io.lbry.browser.model.Language;
import io.lbry.browser.model.License;
import io.lbry.browser.model.NavMenuItem;
import io.lbry.browser.model.Tag;
import io.lbry.browser.model.WalletBalance;
import io.lbry.browser.tasks.claim.ChannelCreateUpdateTask;
import io.lbry.browser.tasks.UpdateSuggestedTagsTask;
import io.lbry.browser.tasks.UploadImageTask;
import io.lbry.browser.tasks.claim.ClaimListResultHandler;
import io.lbry.browser.tasks.claim.ClaimListTask;
import io.lbry.browser.tasks.claim.ClaimResultHandler;
import io.lbry.browser.tasks.claim.PublishClaimTask;
import io.lbry.browser.tasks.lbryinc.LogPublishTask;
import io.lbry.browser.ui.BaseFragment;
import io.lbry.browser.utils.Helper;
import io.lbry.browser.utils.Lbry;
import io.lbry.browser.utils.LbryAnalytics;
import io.lbry.browser.utils.LbryUri;
import io.lbry.browser.utils.Predefined;
import io.lbry.lbrysdk.Utils;
import lombok.Data;
import lombok.Getter;

public class PublishFormFragment extends BaseFragment implements
        FilePickerListener, SdkStatusListener, StoragePermissionListener, TagListAdapter.TagClickListener, WalletBalanceListener {

    private static final String H264_CODEC = "h264";
    private static final int MAX_VIDEO_DIMENSION = 1920;
    private static final int MAX_BITRATE = 5000000; // 5mbps

    private static final int SUGGESTED_LIMIT = 8;

    private boolean editMode;
    @Getter
    private boolean saveInProgress;
    private String currentFilter;
    private boolean publishFileChecked;
    private boolean fetchingChannels;
    private boolean launchPickerPending;
    @Getter
    private boolean transcodeInProgress;
    private long transcodeStartTime;
    private VideoTranscodeTask videoTranscodeTask;

    private TextInputEditText inputTagFilter;
    private RecyclerView addedTagsList;
    private RecyclerView suggestedTagsList;
    private RecyclerView matureTagsList;
    private TagListAdapter addedTagsAdapter;
    private TagListAdapter suggestedTagsAdapter;
    private TagListAdapter matureTagsAdapter;
    private ProgressBar progressPublish;
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
    private ImageView imageThumbnail;
    private TextView linkGenerateAddress;

    private TextInputEditText inputTitle;
    private TextInputEditText inputDescription;
    private TextInputEditText inputPrice;
    private TextInputEditText inputAddress;
    private TextInputEditText inputDeposit;
    private TextInputEditText inputOtherLicenseDescription;
    private TextInputLayout layoutOtherLicenseDescription;
    private View inlineDepositBalanceContainer;
    private TextView inlineDepositBalanceValue;
    private TextView textInlineAddressInvalid;

    private View linkPublishCancel;
    private MaterialButton buttonPublish;

    private View inlineChannelCreator;
    private TextInputEditText inlineChannelCreatorInputName;
    private TextInputEditText inlineChannelCreatorInputDeposit;
    private View inlineChannelCreatorInlineBalance;
    private TextView inlineChannelCreatorInlineBalanceValue;
    private View inlineChannelCreatorCancelLink;
    private View inlineChannelCreatorProgress;
    private MaterialButton inlineChannelCreatorCreateButton;

    private boolean uploading;
    private String lastSelectedThumbnailFile;
    private String uploadedThumbnailUrl;
    private boolean editFieldsLoaded;
    private boolean editChannelSpinnerLoaded;
    private Claim currentClaim;
    private GalleryItem currentGalleryItem;
    private String currentFilePath;
    private String transcodedFilePath;

    private View mediaContainer;
    private View uploadProgress;
    private CardView cardVideoOptimization;
    private ProgressBar optimizationRealProgress;
    private ProgressBar optimizationProgress;
    private TextView textOptimizationProgress;
    private TextView textOptimizationStatus;
    private TextView textOptimizationElapsed;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_publish_form, container, false);

        scrollView = root.findViewById(R.id.publish_form_scroll_view);
        progressLoadingChannels = root.findViewById(R.id.publish_form_loading_channels);
        progressPublish = root.findViewById(R.id.publish_form_publishing);
        channelSpinner = root.findViewById(R.id.publish_form_channel_spinner);
        mediaContainer = root.findViewById(R.id.publish_form_media_container);

        inputTagFilter = root.findViewById(R.id.form_tag_filter_input);
        noTagsView = root.findViewById(R.id.form_no_added_tags);
        noTagResultsView = root.findViewById(R.id.form_no_tag_results);

        textInlineAddressInvalid = root.findViewById(R.id.publish_form_inline_address_invalid);
        inlineDepositBalanceContainer = root.findViewById(R.id.publish_form_inline_balance_container);
        inlineDepositBalanceValue = root.findViewById(R.id.publish_form_inline_balance_value);

        cardVideoOptimization = root.findViewById(R.id.publish_form_video_opt_card);
        optimizationProgress = root.findViewById(R.id.publish_form_video_opt_progress);
        optimizationRealProgress = root.findViewById(R.id.publish_form_video_opt_real_progress);
        textOptimizationProgress = root.findViewById(R.id.publish_form_video_opt_progress_text);
        textOptimizationStatus = root.findViewById(R.id.publish_form_video_opt_status);
        textOptimizationElapsed = root.findViewById(R.id.publish_form_video_opt_elapsed);

        layoutExtraFields = root.findViewById(R.id.publish_form_extra_options_container);
        linkShowExtraFields = root.findViewById(R.id.publish_form_toggle_extra);
        layoutPrice = root.findViewById(R.id.publish_form_price_container);
        textNoPrice = root.findViewById(R.id.publish_form_no_price);
        switchPrice = root.findViewById(R.id.publish_form_price_switch);
        uploadProgress = root.findViewById(R.id.publish_form_thumbnail_upload_progress);
        imageThumbnail = root.findViewById(R.id.publish_form_thumbnail_preview);
        linkGenerateAddress = root.findViewById(R.id.publish_form_generate_address);

        inputTitle = root.findViewById(R.id.publish_form_input_title);
        inputDescription = root.findViewById(R.id.publish_form_input_description);
        inputPrice = root.findViewById(R.id.publish_form_input_price);
        inputAddress = root.findViewById(R.id.publish_form_input_address);
        inputDeposit = root.findViewById(R.id.publish_form_input_deposit);
        inputOtherLicenseDescription = root.findViewById(R.id.publish_form_input_license_other);
        layoutOtherLicenseDescription = root.findViewById(R.id.publish_form_license_other_layout);
        priceCurrencySpinner = root.findViewById(R.id.publish_form_currency_spinner);
        languageSpinner = root.findViewById(R.id.publish_form_language_spinner);
        licenseSpinner = root.findViewById(R.id.publish_form_license_spinner);

        linkPublishCancel = root.findViewById(R.id.publish_form_cancel);
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
        Context context = getContext();
        languageSpinner.setAdapter(new LanguageSpinnerAdapter(context, R.layout.spinner_item_generic));
        licenseSpinner.setAdapter(new LicenseSpinnerAdapter(context, R.layout.spinner_item_generic));

        licenseSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                License license = (License) adapterView.getAdapter().getItem(position);
                boolean otherLicense = Arrays.asList(
                        Predefined.LICENSE_COPYRIGHTED.toLowerCase(),
                        Predefined.LICENSE_OTHER.toLowerCase()).contains(license.getName().toLowerCase());
                Helper.setViewVisibility(layoutOtherLicenseDescription, otherLicense ? View.VISIBLE : View.GONE);
                if (!otherLicense) {
                    inputOtherLicenseDescription.setText(null);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        linkGenerateAddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!editMode) {
                    inputAddress.setText(Helper.generateUrl());
                }
            }
        });

        switchPrice.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                Helper.setViewVisibility(textNoPrice, checked ? View.GONE : View.VISIBLE);
                Helper.setViewVisibility(layoutPrice, checked ? View.VISIBLE : View.GONE);
            }
        });

        inputAddress.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String value = Helper.getValue(charSequence);
                boolean invalid = !Helper.isNullOrEmpty(value) && !LbryUri.isNameValid(value);
                Helper.setViewVisibility(textInlineAddressInvalid, invalid ? View.VISIBLE : View.INVISIBLE);
            }

            @Override
            public void afterTextChanged(Editable editable) {

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

        mediaContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkStoragePermissionAndLaunchFilePicker();
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

        linkPublishCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (transcodeInProgress) {
                    // show alert confirming the user is sure, and then cancel
                    FFmpeg.cancel();
                    transcodeInProgress = false;
                }

                Context context = getContext();
                if (context instanceof MainActivity) {
                    ((MainActivity) context).onBackPressed();
                }
            }
        });

        buttonPublish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (uploading) {
                    Snackbar.make(view, R.string.publish_thumbnail_in_progress, Snackbar.LENGTH_LONG).show();
                    return;
                } else if (Helper.isNullOrEmpty(uploadedThumbnailUrl)) {
                    showError(getString(R.string.publish_no_thumbnail));
                    return;
                }

                if (transcodeInProgress) {
                    Snackbar.make(view, R.string.optimization_in_progress, Snackbar.LENGTH_LONG).show();
                    return;
                }

                // check minimum deposit
                String depositString = Helper.getValue(inputDeposit.getText());
                double depositAmount = 0;
                try {
                    depositAmount = Double.valueOf(depositString);
                } catch (NumberFormatException ex) {
                    // pass
                    showError(getString(R.string.please_enter_valid_deposit));
                    return;
                }
                if (depositAmount < Helper.MIN_DEPOSIT) {
                    String error = getResources().getQuantityString(R.plurals.min_deposit_required, depositAmount == 1 ? 1 : 2, String.valueOf(Helper.MIN_DEPOSIT));
                    showError(error);
                    return;
                }
                if (Lbry.walletBalance == null || Lbry.walletBalance.getAvailable().doubleValue() < depositAmount) {
                    showError(getString(R.string.deposit_more_than_balance));
                    return;
                }

                String priceString = Helper.getValue(inputPrice.getText());
                double priceAmount = Helper.parseDouble(priceString, 0);
                if (switchPrice.isChecked() && priceAmount == 0) {
                    showError(getString(R.string.price_amount_not_set));
                    return;
                }

                Claim claim = buildPublishClaim();
                if (validatePublishClaim(claim)) {
                    publishClaim(claim);
                }
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

            activity.addFilePickerListener(this);
            activity.addWalletBalanceListener(this);

            activity.setActionBarTitle(editMode ? R.string.edit_content : R.string.new_publish);
        }
    }

    @Override
    public void onStop() {
        Context context = getContext();
        if (context instanceof MainActivity) {
            MainActivity activity = (MainActivity) getContext();
            activity.restoreToggle();
            activity.showFloatingWalletBalance();
            if (!MainActivity.startingFilePickerActivity) {
                activity.removeWalletBalanceListener(this);
                activity.removeFilePickerListener(this);
                activity.removeNavFragment(PublishFormFragment.class, NavMenuItem.ID_ITEM_NEW_PUBLISH);
                if (transcodeInProgress) {
                    FFmpeg.cancel();
                }
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
            } else if (params.containsKey("galleryItem")) {
                currentGalleryItem = (GalleryItem) params.get("galleryItem");
            } else if (params.containsKey("directFilePath")) {
                currentFilePath = (String) params.get("directFilePath");
            }

            if (this.currentClaim == null && params.containsKey("suggestedUrl")) {
                String suggestedUrl = (String) params.get("suggestedUrl");
                if (!Helper.isNullOrEmpty(suggestedUrl) && Helper.isNullOrEmpty(Helper.getValue(inputAddress.getText()))) {
                    Helper.setViewText(inputAddress, (String) params.get("suggestedUrl"));
                }
            }
        } else {
            // shouldn't actually happen
            cancelOnFatalCondition(getString(R.string.no_file_found));
        }
    }

    private void checkStoragePermissionAndLaunchFilePicker() {
        Context context = getContext();
        if (MainActivity.hasPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, context)) {
            launchPickerPending = false;
            launchFilePicker();
        } else {
            launchPickerPending = true;
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
                    Intent.createChooser(intent, getString(R.string.select_thumbnail)),
                    MainActivity.REQUEST_FILE_PICKER);
        }
    }

    private void updateFieldsFromCurrentClaim() {
        if (currentClaim != null && !editFieldsLoaded) {
            Context context = getContext();
            Claim.StreamMetadata metadata = (Claim.StreamMetadata) currentClaim.getValue();
            uploadedThumbnailUrl = currentClaim.getThumbnailUrl();
            if (context != null && !Helper.isNullOrEmpty(uploadedThumbnailUrl)) {
                Glide.with(context.getApplicationContext()).load(uploadedThumbnailUrl).centerCrop().into(imageThumbnail);
            }

            inputTitle.setText(currentClaim.getTitle());
            inputDescription.setText(currentClaim.getDescription());
            if (addedTagsAdapter != null && currentClaim.getTagObjects() != null) {
                addedTagsAdapter.addTags(currentClaim.getTagObjects());
                updateSuggestedTags(currentFilter, SUGGESTED_LIMIT, true);
            }

            if (metadata.getFee() != null) {
                Fee fee = metadata.getFee();
                switchPrice.setChecked(true);
                inputPrice.setText(fee.getAmount());
                priceCurrencySpinner.setSelection("lbc".equalsIgnoreCase(fee.getCurrency()) ? 0 : 1);
            }

            inputAddress.setText(currentClaim.getName());
            inputDeposit.setText(currentClaim.getAmount());

            if (metadata.getLanguages() != null && metadata.getLanguages().size() > 0) {
                // get the first language
                String langCode = metadata.getLanguages().get(0);
                int langCodePosition = ((LanguageSpinnerAdapter) languageSpinner.getAdapter()).getItemPosition(langCode);
                if (langCodePosition > -1) {
                    languageSpinner.setSelection(langCodePosition);
                }
            }

            if (!Helper.isNullOrEmpty(metadata.getLicense())) {
                LicenseSpinnerAdapter adapter = (LicenseSpinnerAdapter) licenseSpinner.getAdapter();
                int licPosition = adapter.getItemPosition(metadata.getLicense());
                if (licPosition == -1) {
                    licPosition = adapter.getItemPosition(Predefined.LICENSE_OTHER);
                }
                if (licPosition > -1) {
                    licenseSpinner.setSelection(licPosition);
                }

                License selectedLicense = (License) licenseSpinner.getSelectedItem();
                boolean otherLicense = Arrays.asList(
                        Predefined.LICENSE_COPYRIGHTED.toLowerCase(),
                        Predefined.LICENSE_OTHER.toLowerCase()).contains(selectedLicense.getName().toLowerCase());
                inputOtherLicenseDescription.setText(otherLicense ? metadata.getLicense() : null);
            }

            inputAddress.setEnabled(false);
            editMode = true;
            editFieldsLoaded = true;
        }
    }

    private void checkPublishFile() {
        if (publishFileChecked) {
            return;
        }

        String filePath = "";
        String thumbnailPath = null;
        if (currentGalleryItem != null) {
            // check gallery item type
            filePath = currentGalleryItem.getFilePath();
            thumbnailPath = currentGalleryItem.getThumbnailPath();
        } else if (currentFilePath != null) {
            filePath = currentFilePath;
        }

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

        boolean isVideo = false;
        boolean isImage = !Helper.isNullOrEmpty(type) && type.startsWith("image");
        if (!Helper.isNullOrEmpty(type) && type.startsWith("video")) {
            // ffmpeg video handling
            isVideo = true;
            if (!transcodeInProgress) {
                probeVideo(filePath);
            }
        }

        if (isVideo || isImage) {
            checkAndUploadThumbnail(filePath, thumbnailPath, isVideo ? "video" : "image");
        }

        Helper.setViewVisibility(cardVideoOptimization, isVideo ? View.VISIBLE : View.GONE);

        publishFileChecked = true;
    }

    private void checkAndUploadThumbnail(String filePath, String thumbnailPath, String type) {
        boolean thumbnailValid = false;
        if (!Helper.isNullOrEmpty(thumbnailPath)) {
            File file = new File(thumbnailPath);
            // make sure the file exists and it's not an empty file
            thumbnailValid = file.exists() && file.length() > 0;
        }

        if (!thumbnailValid) {
            createAndUploadThumbnail(filePath, type);
        } else {
            uploadThumbnail(thumbnailPath);
        }
    }

    private void createAndUploadThumbnail(String filePath, String type) {
        Context context = getContext();
        CreateThumbnailTask task = new CreateThumbnailTask(filePath, type, context, new CreateThumbnailTask.CreateThumbnailHandler() {
            @Override
            public void onSuccess(String thumbnailPath) {
                uploadThumbnail(thumbnailPath);
            }

            @Override
            public void onError(Exception error) {
                if (context != null) {
                    showError(getString(R.string.thumbnail_creation_failed));
                }
            }
        });
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void uploadThumbnail(String thumbnailPath) {
        if (uploading) {
            Snackbar.make(getView(), R.string.wait_for_upload, Snackbar.LENGTH_LONG).show();
            return;
        }

        Context context = getContext();
        if (context != null) {
            Glide.with(context.getApplicationContext()).load(thumbnailPath).centerCrop().into(imageThumbnail);
        }

        uploading = true;
        uploadedThumbnailUrl = null;
        UploadImageTask task = new UploadImageTask(thumbnailPath, uploadProgress, new UploadImageTask.UploadThumbnailHandler() {
            @Override
            public void onSuccess(String url) {
                lastSelectedThumbnailFile = thumbnailPath;
                uploadedThumbnailUrl = url;
                uploading = false;
            }

            @Override
            public void onError(Exception error) {
                View view = getView();
                if (context != null && view != null) {
                    showError(getString(R.string.image_upload_failed));
                }
                lastSelectedThumbnailFile = null;
                imageThumbnail.setImageDrawable(null);
                uploading = false;
            }
        });
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void probeVideo(String filePath) {
        VideoProbeTask task = new VideoProbeTask(filePath, new VideoProbeTask.VideoProbeHandler() {
            @Override
            public void onVideoProbed(VideoInformation result) {
                checkAndTranscodeVideo(filePath, result);
            }
        });
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

    }

    private void checkAndTranscodeVideo(String filePath, VideoInformation videoInformation) {
        boolean transcodeRequired = (videoInformation == null ||
                !H264_CODEC.equalsIgnoreCase(videoInformation.getCodecName()) ||
                MAX_VIDEO_DIMENSION < videoInformation.getWidth() || MAX_VIDEO_DIMENSION < videoInformation.getHeight() ||
                MAX_BITRATE < videoInformation.getBitrate());

        String scalePart = "";
        if (videoInformation != null) {
            // check the max dimension that we need to scale
            int videoWidth = videoInformation.getWidth();
            int videoHeight = videoInformation.getHeight();
            // get the highest dimension
            int maxDimension = Math.max(videoWidth, videoHeight);
            if (maxDimension > MAX_VIDEO_DIMENSION) {
                scalePart = maxDimension == videoWidth ? String.format("-vf scale=%d:-2", MAX_VIDEO_DIMENSION) : String.format("-vf scale=-2:%d", MAX_VIDEO_DIMENSION);
            }
        }

        Context context = getContext();
        String outputPath = String.format("%s/videos", Utils.getAppInternalStorageDir(context));
        File dir = new File(outputPath);
        if (!dir.isDirectory()) {
            dir.mkdirs();
        }

        boolean hasFullDuration = videoInformation != null && videoInformation.getDurationSeconds() > 0;
        Helper.setViewVisibility(optimizationRealProgress, hasFullDuration ? View.VISIBLE : View.GONE);
        Helper.setViewVisibility(optimizationProgress, hasFullDuration ? View.GONE : View.VISIBLE);

        File sourceFile = new File(filePath);
        String filename = sourceFile.getName();
        if (!filename.endsWith(".mp4")) {
            int lastDotIndex = filename.lastIndexOf('.');
            filename = String.format("%s.mp4", lastDotIndex > -1 ? filename.substring(0, lastDotIndex) : filename);
        }

        String videoFilePath = String.format("%s/%s", outputPath, filename);
        File targetFile = new File(videoFilePath);
        if (targetFile.exists()) {
            targetFile.delete();
        }

        transcodeInProgress = true;
        videoTranscodeTask = new VideoTranscodeTask(filePath, videoFilePath, scalePart, transcodeRequired, new VideoTranscodeTask.VideoTranscodeHandler() {
            @Override
            public void onProgress(int time) {
                if (context != null) {
                    int currentDuration = Double.valueOf(time / 1000.0).intValue();
                    int fullDuration = videoInformation != null ? videoInformation.getDurationSeconds() : 0;
                    long elapsed = System.currentTimeMillis() - transcodeStartTime;
                    String completedDurationText = Helper.formatDuration(currentDuration);
                    if (fullDuration > 0) {
                        completedDurationText = String.format("%s / %s", completedDurationText, Helper.formatDuration(fullDuration));
                        int percentComplete = Double.valueOf(Math.ceil((double) currentDuration / (double) fullDuration * 100.0)).intValue();
                        optimizationRealProgress.setProgress(percentComplete);
                    }


                    String text = context.getString(R.string.completed_video_duration, completedDurationText);
                    Helper.setViewText(textOptimizationProgress, text);
                    Helper.setViewText(textOptimizationElapsed, Helper.formatDuration(Double.valueOf(elapsed / 1000.0).longValue()));
                }
            }

            @Override
            public void onSuccess(String outputFilePath) {
                transcodedFilePath = outputFilePath;
                transcodeInProgress = false;
                Helper.setViewText(textOptimizationStatus, R.string.video_optimized);
                Helper.setViewVisibility(optimizationRealProgress, View.GONE);
                Helper.setViewVisibility(optimizationProgress, View.GONE);
                Helper.setViewVisibility(textOptimizationProgress, View.GONE);
            }

            @Override
            public void onErrorOrCancelled() {
                transcodeInProgress = false;
                Helper.setViewText(textOptimizationStatus, R.string.video_optimize_failed);
                Helper.setViewVisibility(optimizationRealProgress, View.GONE);
                Helper.setViewVisibility(optimizationProgress, View.GONE);
                Helper.setViewVisibility(textOptimizationProgress, View.GONE);
            }
        });

        transcodeStartTime = System.currentTimeMillis();
        videoTranscodeTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
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
        checkRewardsDriver();
        updateFieldsFromCurrentClaim();

        if (currentClaim == null && (currentGalleryItem != null || !Helper.isNullOrEmpty(currentFilePath))) {
            // load file information
            checkPublishFile();
        }

        Context context = getContext();
        Helper.setWunderbarValue(null, context);
        if (context instanceof MainActivity) {
            MainActivity activity = (MainActivity) context;
            LbryAnalytics.setCurrentScreen(activity, "Channel Form", "ChannelForm");
            activity.addStoragePermissionListener(this);
            if (editMode) {
                activity.setActionBarTitle(R.string.edit_content);
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
            if (editMode && currentClaim.getSigningChannel() != null && !editChannelSpinnerLoaded) {
                int position = channelSpinnerAdapter.getItemPosition(currentClaim.getSigningChannel());
                if (position > -1) {
                    channelSpinner.setSelection(position);
                }
                editChannelSpinnerLoaded = true;
            } else {
                if (channelSpinnerAdapter.getCount() > 2) {
                    // if anonymous displayed, select first channel if available
                    channelSpinner.setSelection(2);
                } else if (channelSpinnerAdapter.getCount() > 1) {
                    // select anonymous
                    channelSpinner.setSelection(1);
                }
            }
        }
    }

    private Claim buildPublishClaim() {
        Claim claim = new Claim();

        claim.setName(Helper.getValue(inputAddress.getText()));
        claim.setAmount(Helper.getValue(inputDeposit.getText()));

        Claim.StreamMetadata metadata = new Claim.StreamMetadata();
        metadata.setTitle(Helper.getValue(inputTitle.getText()));
        metadata.setDescription(Helper.getValue(inputDescription.getText()));
        metadata.setTags(Helper.getTagsForTagObjects(addedTagsAdapter.getTags()));

        Claim selectedChannel = (Claim) channelSpinner.getSelectedItem();
        if (selectedChannel != null && !selectedChannel.isPlaceholder() && !selectedChannel.isPlaceholderAnonymous()) {
            claim.setSigningChannel(selectedChannel);
        }
        if (switchPrice.isChecked()) {
            Fee fee = new Fee();
            fee.setCurrency((String) priceCurrencySpinner.getSelectedItem());
            fee.setAmount(Helper.getValue(inputPrice.getText()));
            metadata.setFee(fee);
        }
        if (!Helper.isNullOrEmpty(uploadedThumbnailUrl)) {
            Claim.Resource thumbnail = new Claim.Resource();
            thumbnail.setUrl(uploadedThumbnailUrl);
            metadata.setThumbnail(thumbnail);
        }

        Language selectedLanguage = (Language) languageSpinner.getSelectedItem();
        if (selectedLanguage != null) {
            metadata.setLanguages(Arrays.asList(selectedLanguage.getCode()));
        }

        License selectedLicense = (License) licenseSpinner.getSelectedItem();
        if (selectedLicense != null) {
            boolean otherLicense = Arrays.asList(
                    Predefined.LICENSE_COPYRIGHTED.toLowerCase(),
                    Predefined.LICENSE_OTHER.toLowerCase()).contains(selectedLicense.getName().toLowerCase());
            metadata.setLicense(otherLicense ? Helper.getValue(inputOtherLicenseDescription.getText()) : selectedLicense.getName());
            metadata.setLicenseUrl(selectedLicense.getUrl());
        }

        claim.setValueType(Claim.TYPE_STREAM);
        claim.setValue(metadata);

        return claim;
    }

    private boolean validatePublishClaim(Claim claim) {
        if (Helper.isNullOrEmpty(claim.getTitle())) {
            showError(getString(R.string.please_provide_title));
            return false;
        }
        if (Helper.isNullOrEmpty(claim.getName())) {
            showError(getString(R.string.please_specify_address));
            return false;
        }
        if (!LbryUri.isNameValid(claim.getName())) {
            showError(getString(R.string.address_invalid_characters));
            return false;
        }
        if (!editMode && Helper.claimNameExists(claim.getName())) {
            showError(getString(R.string.address_already_used));
            return false;
        }

        String publishFilePath = currentGalleryItem != null ? currentGalleryItem.getFilePath() : currentFilePath;
        if (!editMode && Helper.isNullOrEmpty(publishFilePath) && Helper.isNullOrEmpty(transcodedFilePath)) {
            showError(getString(R.string.no_file_selected));
            return false;
        }

        return true;
    }

    private void publishClaim(Claim claim) {
        String finalFilePath = transcodedFilePath;
        if (Helper.isNullOrEmpty(finalFilePath)) {
            finalFilePath = currentGalleryItem != null ? currentGalleryItem.getFilePath() : currentFilePath;
        }
        saveInProgress = true;
        PublishClaimTask task = new PublishClaimTask(claim, finalFilePath, progressPublish, new ClaimResultHandler() {
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

                // publish done
                Bundle bundle = new Bundle();
                bundle.putString("claim_id", claimResult.getClaimId());
                bundle.putString("claim_name", claimResult.getName());
                LbryAnalytics.logEvent(editMode ? LbryAnalytics.EVENT_PUBLISH_UPDATE : LbryAnalytics.EVENT_PUBLISH, bundle);

                Context context = getContext();
                if (context instanceof MainActivity) {
                    MainActivity activity = (MainActivity) context;
                    activity.showMessage(R.string.publish_successful);
                    activity.sendBroadcast(new Intent(MainActivity.ACTION_PUBLISH_SUCCESSFUL));
                }
            }

            @Override
            public void onError(Exception error) {
                showError(error.getMessage());
                postSave();
            }
        });
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void preSave() {
        saveInProgress = true;

        // disable input views
        Helper.setViewEnabled(channelSpinner, false);
        Helper.setViewEnabled(inputTitle, false);
        Helper.setViewEnabled(inputDescription, false);
        Helper.setViewEnabled(inputTagFilter, false);
        Helper.setViewEnabled(inputAddress, false);
        Helper.setViewEnabled(inputDeposit, false);
        Helper.setViewEnabled(inputPrice, false);
        Helper.setViewEnabled(inputOtherLicenseDescription, false);
        Helper.setViewEnabled(switchPrice, false);
        Helper.setViewEnabled(languageSpinner, false);
        Helper.setViewEnabled(licenseSpinner, false);
        Helper.setViewEnabled(priceCurrencySpinner, false);
        Helper.setViewEnabled(linkGenerateAddress, false);

        Helper.setViewEnabled(linkShowExtraFields, false);
        Helper.setViewEnabled(linkPublishCancel, false);
        Helper.setViewEnabled(buttonPublish,  false);
    }

    private void postSave() {
        Helper.setViewEnabled(channelSpinner, true);
        Helper.setViewEnabled(inputTitle, true);
        Helper.setViewEnabled(inputDescription, true);
        Helper.setViewEnabled(inputTagFilter, false);
        Helper.setViewEnabled(inputAddress, editMode ? false : true);
        Helper.setViewEnabled(inputDeposit, true);
        Helper.setViewEnabled(inputPrice, true);
        Helper.setViewEnabled(inputOtherLicenseDescription, true);
        Helper.setViewEnabled(switchPrice, true);
        Helper.setViewEnabled(languageSpinner, true);
        Helper.setViewEnabled(licenseSpinner, true);
        Helper.setViewEnabled(priceCurrencySpinner, true);
        Helper.setViewEnabled(linkGenerateAddress, true);

        Helper.setViewEnabled(linkShowExtraFields, true);
        Helper.setViewEnabled(linkPublishCancel, true);
        Helper.setViewEnabled(buttonPublish,  true);

        saveInProgress = false;
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
        if (saveInProgress) {
            return;
        }

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
        if (saveInProgress) {
            return;
        }
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
        checkRewardsDriver();
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
            Snackbar.make(getView(), message, Snackbar.LENGTH_LONG).
                    setBackgroundTint(Color.RED).setTextColor(Color.WHITE).show();
        }
    }

    private void checkUploadButton() {

    }

    @Override
    public void onStoragePermissionGranted() {
        if (launchPickerPending) {
            launchPickerPending = false;
            launchFilePicker();
        }
    }

    @Override
    public void onStoragePermissionRefused() {
        showError(getString(R.string.storage_permission_rationale_images));
        launchPickerPending = false;
    }

    @Override
    public void onFilePicked(String filePath) {
        if (Helper.isNullOrEmpty(filePath)) {
            Snackbar.make(getView(), R.string.undetermined_image_filepath, Snackbar.LENGTH_LONG).setBackgroundTint(
                    ContextCompat.getColor(getContext(), R.color.red)).show();
            return;
        }

        Context context = getContext();
        if (context != null) {
            if (filePath.equalsIgnoreCase(lastSelectedThumbnailFile)) {
                // previous selected cover was uploaded successfully
                return;
            }

            Uri fileUri = Uri.fromFile(new File(filePath));
            Glide.with(context.getApplicationContext()).load(fileUri).centerCrop().into(imageThumbnail);
            uploadThumbnail(filePath);
        }
    }

    @Override
    public void onFilePickerCancelled() {
        // nothing to do here
        // At some point in the future, allow file picking for publish file?
    }

    private void checkRewardsDriver() {
        Context ctx = getContext();
        if (ctx != null) {
            String rewardsDriverText = String.format("%s\n%s",
                    getString(R.string.publishing_requires_credits), getString(R.string.tap_here_to_get_some));
            checkRewardsDriverCard(rewardsDriverText, Helper.MIN_DEPOSIT);
        }
    }

    private static class VideoProbeTask extends AsyncTask<Void, Void, VideoInformation> {
        private String filePath;
        private VideoProbeHandler handler;
        public VideoProbeTask(String filePath, VideoProbeHandler handler) {
            this.filePath = filePath;
            this.handler = handler;
        }
        protected VideoInformation doInBackground(Void... params) {
            try {
                int code = FFprobe.execute(String.format("-v quiet -show_streams -select_streams v -print_format json -i \"%s\"", filePath));
                if (code == Config.RETURN_CODE_SUCCESS) {
                    String json = Config.getLastCommandOutput();
                    JSONObject result = new JSONObject(json);
                    if (result.has("streams")) {
                        JSONArray streams = result.getJSONArray("streams");
                        if (streams.length() > 0) {
                            JSONObject stream = streams.getJSONObject(0);
                            VideoInformation videoInformation = VideoInformation.fromJSONObject(stream);
                            return videoInformation;
                        }
                    }
                }
            } catch (JSONException ex) {
                // pass
            }
            return null;
        }
        protected void onPostExecute(VideoInformation result) {
            if (handler != null) {
                handler.onVideoProbed(result);
            }
        }

        public interface VideoProbeHandler {
            void onVideoProbed(VideoInformation result);
        }
    }

    private static class VideoTranscodeTask extends AsyncTask<Void, Integer, Boolean> {

        private String filePath;
        private String scaleFlag;
        private String outputFilePath;
        private boolean transcodeRequired;
        private VideoTranscodeHandler handler;

        public VideoTranscodeTask(String filePath, String outputFilePath, String scaleFlag, boolean transcodeRequired, VideoTranscodeHandler handler) {
            this.handler = handler;
            this.filePath = filePath;
            this.outputFilePath = outputFilePath;
            this.scaleFlag = scaleFlag;
            this.transcodeRequired = transcodeRequired;
        }

        protected Boolean doInBackground(Void... params) {
            String movFlagsCommand = String.format("-i \"%s\" -movflags +faststart \"%s\"", filePath, outputFilePath);
            String command = transcodeRequired ? String.format(
                    "-i \"%s\" " +
                    "-c:v libx264 " +
                    "-c:a aac -b:a 128k " +
                    "%s " +
                    "-crf 27 -preset ultrafast " +
                    "-pix_fmt yuv420p " +
                    "-maxrate 5000K -bufsize 5000K " +
                    "-movflags +faststart \"%s\"", filePath, scaleFlag, outputFilePath) : movFlagsCommand;

            Config.enableStatisticsCallback(new StatisticsCallback() {
                @Override
                public void apply(Statistics statistics) {
                    publishProgress(statistics.getTime());
                }
            });
            int code = FFmpeg.execute(command);
            return code == Config.RETURN_CODE_SUCCESS;
        }

        protected void onProgressUpdate(Integer... times) {
            if (handler != null) {
                for (Integer time : times) {
                    handler.onProgress(time);
                }
            }
        }

        protected void onPostExecute(Boolean result) {
            if (handler != null) {
                if (result) {
                    handler.onSuccess(outputFilePath);
                } else {
                    handler.onErrorOrCancelled();
                }
            }
        }

        public interface VideoTranscodeHandler {
            void onProgress(int time);
            void onSuccess(String outputFilePath);
            void onErrorOrCancelled();
        }
    }

    @Data
    private static class VideoInformation {
        private String codecName;
        private int width;
        private int height;
        private int durationSeconds;
        private long bitrate;

        private static int tryParseDuration(JSONObject streamObject) {
            String durationString = Helper.getJSONString("duration", "0", streamObject);
            double parsedDuration = Helper.parseDouble(durationString, 0);
            if (parsedDuration > 0) {
                return Double.valueOf(parsedDuration).intValue();
            }

            try {
                if (streamObject.has("tags") && !streamObject.isNull("tags")) {
                    JSONObject tags = streamObject.getJSONObject("tags");
                    String tagDurationString = Helper.getJSONString("DURATION", null, tags);
                    if (Helper.isNull(tagDurationString)) {
                        tagDurationString = Helper.getJSONString("duration", null, tags);
                    }
                    if (!Helper.isNullOrEmpty(tagDurationString) && tagDurationString.indexOf(':') > -1) {
                        String[] parts = tagDurationString.split(":");
                        if (parts.length == 3) {
                            int hours = Helper.parseInt(parts[0], 0);
                            int minutes = Helper.parseInt(parts[1], 0);
                            int seconds = Helper.parseDouble(parts[2], 0).intValue();
                            return (hours * 60 * 60) + (minutes * 60) + seconds;
                        }
                    }

                }
            } catch (JSONException ex) {
                return 0;
            }

            return 0;
        }

        public static VideoInformation fromJSONObject(JSONObject streamObject) {
            VideoInformation info = new VideoInformation();
            info.setCodecName(Helper.getJSONString("codec_name", null, streamObject));
            info.setWidth(Helper.getJSONInt("width", 0, streamObject));
            info.setHeight(Helper.getJSONInt("height", 0, streamObject));
            info.setBitrate(Helper.getJSONLong("bit_rate", 0, streamObject));
            info.setDurationSeconds(tryParseDuration(streamObject));

            return info;
        }
    }

    private static class CreateThumbnailTask extends AsyncTask<Void, Void, String> {
        private Context context;
        private String filePath;
        private String type;
        private CreateThumbnailHandler handler;
        private Exception error;
        public CreateThumbnailTask(String filePath, String type, Context context, CreateThumbnailHandler handler) {
            this.context = context;
            this.type = type;
            this.filePath = filePath;
            this.handler = handler;
        }
        protected String doInBackground(Void... params) {
            String thumbnailPath = null;
            FileOutputStream os = null;
            Bitmap thumbnail = null;
            try {
                File cacheDir = context.getExternalCacheDir();
                File thumbnailsDir = new File(String.format("%s/thumbnails", cacheDir.getAbsolutePath()));
                if (!thumbnailsDir.isDirectory()) {
                    thumbnailsDir.mkdirs();
                }

                // save the thumbnail to the path
                thumbnailPath = String.format("%s/%s.png", thumbnailsDir.getAbsolutePath(), Helper.makeid(8));
                if ("video".equals(type)) {
                    thumbnail = ThumbnailUtils.createVideoThumbnail(filePath, MediaStore.Video.Thumbnails.MINI_KIND);
                } else {
                    Bitmap source = BitmapFactory.decodeFile(filePath);
                    // MINI_KIND dimensions
                    thumbnail = Bitmap.createScaledBitmap(source, 512, 384, false);
                }

                os = new FileOutputStream(thumbnailPath);
                thumbnail.compress(Bitmap.CompressFormat.PNG, 80, os);
            } catch (Exception ex) {
                error = ex;
                return null;
            } finally {
                Helper.closeCloseable(os);
            }

            return thumbnailPath;
        }
        protected void onPostExecute(String thumbnailPath) {
            if (handler != null) {
                if (!Helper.isNullOrEmpty(thumbnailPath)) {
                    handler.onSuccess(thumbnailPath);
                } else {
                    handler.onError(error);
                }
            }
        }

        public interface CreateThumbnailHandler {
            void onSuccess(String thumbnailPath);
            void onError(Exception error);
        }
    }
}
