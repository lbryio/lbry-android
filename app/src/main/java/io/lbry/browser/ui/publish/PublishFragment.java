package io.lbry.browser.ui.publish;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.CameraX;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
import com.google.common.util.concurrent.ListenableFuture;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;

import io.lbry.browser.MainActivity;
import io.lbry.browser.R;
import io.lbry.browser.adapter.GalleryGridAdapter;
import io.lbry.browser.listener.CameraPermissionListener;
import io.lbry.browser.listener.StoragePermissionListener;
import io.lbry.browser.model.GalleryItem;
import io.lbry.browser.tasks.localdata.LoadGalleryItemsTask;
import io.lbry.browser.ui.BaseFragment;
import io.lbry.browser.utils.Helper;
import io.lbry.browser.utils.LbryAnalytics;

public class PublishFragment extends BaseFragment implements CameraPermissionListener, StoragePermissionListener {

    private boolean loadGalleryItemsPending;
    private PreviewView cameraPreview;
    private RecyclerView galleryGrid;
    private GalleryGridAdapter adapter;
    private TextView noVideosLoaded;
    private View loading;

    private View buttonRecord;
    private View buttonTakePhoto;
    private View buttonUpload;

    private boolean recordPending;
    private boolean takePhotoPending;
    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_publish, container, false);

        noVideosLoaded = root.findViewById(R.id.publish_grid_no_videos);
        loading = root.findViewById(R.id.publish_grid_loading);
        cameraPreview = root.findViewById(R.id.publish_camera_preview);

        Context context = getContext();
        galleryGrid = root.findViewById(R.id.publish_video_grid);
        GridLayoutManager glm = new GridLayoutManager(context, 3);
        galleryGrid.setLayoutManager(glm);
        galleryGrid.addItemDecoration(new GalleryGridAdapter.GalleryGridItemDecoration(
                3, Helper.getScaledValue(3, context.getResources().getDisplayMetrics().density)));

        buttonRecord = root.findViewById(R.id.publish_record_button);
        buttonTakePhoto = root.findViewById(R.id.publish_photo_button);
        buttonUpload = root.findViewById(R.id.publish_upload_button);

        buttonRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkCameraPermissionAndRecord();
            }
        });
        buttonTakePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkCameraPermissionAndTakePhoto();
            }
        });

        return root;
    }

    private boolean cameraAvailable() {
        Context context = getContext();
        return context != null && context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY);
    }

    private void showCameraPreview() {
        buttonRecord.setBackgroundColor(Color.TRANSPARENT);
        buttonTakePhoto.setBackgroundColor(Color.TRANSPARENT);
        displayPreviewWithCameraX();
    }

    private void displayPreviewWithCameraX() {
        Context context = getContext();
        if (context != null) {
            cameraProviderFuture = ProcessCameraProvider.getInstance(context);
            cameraProviderFuture.addListener(new Runnable() {
                @Override
                public void run() {
                    try {
                        ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                        if (cameraProvider != null) {
                            Preview preview = new Preview.Builder().build();
                            CameraSelector cameraSelector = new CameraSelector.Builder()
                                    .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                                    .build();

                            Camera camera = cameraProvider.bindToLifecycle((LifecycleOwner) context, cameraSelector, preview);
                            preview.setSurfaceProvider(cameraPreview.createSurfaceProvider(camera.getCameraInfo()));
                        } else {
                            android.util.Log.d("#HELP", "camera provider future is null?");
                        }
                    } catch (ExecutionException | InterruptedException ex) {
                        // pass
                    }
                }
            }, ContextCompat.getMainExecutor(context));
        }
    }

    private void checkCameraPermissionAndRecord() {
        Context context = getContext();
        if (!MainActivity.hasPermission(Manifest.permission.CAMERA, context)) {
            recordPending = true;
            MainActivity.requestPermission(
                    Manifest.permission.CAMERA,
                    MainActivity.REQUEST_CAMERA_PERMISSION,
                    getString(R.string.camera_permission_rationale_record),
                    context,
                    true);
        } else  {
            // start video record intent
        }
    }

    private void checkCameraPermissionAndTakePhoto() {
        Context context = getContext();
        if (!MainActivity.hasPermission(Manifest.permission.CAMERA, context)) {
            takePhotoPending = true;
            MainActivity.requestPermission(
                    Manifest.permission.CAMERA,
                    MainActivity.REQUEST_CAMERA_PERMISSION,
                    getString(R.string.camera_permission_rationale_photo),
                    context,
                    true);
        } else  {
            // start video record intent
        }
    }

    public void onResume() {
        super.onResume();
        Context context = getContext();
        if (context instanceof MainActivity) {
            MainActivity activity = (MainActivity) context;
            LbryAnalytics.setCurrentScreen(activity, "Publish", "Publish");
            activity.addCameraPermissionListener(this);
            activity.addStoragePermissionListener(this);
            activity.hideFloatingWalletBalance();


            if (cameraAvailable() && MainActivity.hasPermission(Manifest.permission.CAMERA, context)) {
                showCameraPreview();
            }
        }

        checkStoragePermissionAndLoadVideos();
    }

    @SuppressLint("RestrictedApi")
    public void onStop() {
        Context context = getContext();
        if (context instanceof MainActivity) {
            MainActivity activity = (MainActivity) context;
            activity.removeCameraPermissionListener(this);
            activity.removeStoragePermissionListener(this);
            activity.showFloatingWalletBalance();
        }
        CameraX.unbindAll();
        super.onStop();
    }

    private void checkStoragePermissionAndLoadVideos() {
        Context context = getContext();
        if (MainActivity.hasPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, context)) {
            loadGalleryItems();
        } else {
            loadGalleryItemsPending = true;
            MainActivity.requestPermission(
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    MainActivity.REQUEST_STORAGE_PERMISSION,
                    getString(R.string.storage_permission_rationale_download),
                    context,
                    true);
        }
    }

    private void loadGalleryItems() {
        Context context = getContext();
        Helper.setViewVisibility(noVideosLoaded, View.GONE);
        LoadGalleryItemsTask task = new LoadGalleryItemsTask(loading, context, new LoadGalleryItemsTask.LoadGalleryHandler() {
            @Override
            public void onItemLoaded(GalleryItem item) {
                if (context != null) {
                    if (adapter == null) {
                        adapter = new GalleryGridAdapter(Arrays.asList(item), context);
                    } else {
                        adapter.addItem(item);
                    }

                    if (galleryGrid.getAdapter() == null) {
                        galleryGrid.setAdapter(adapter);
                    }
                    Helper.setViewVisibility(loading, adapter == null || adapter.getItemCount() == 0 ? View.VISIBLE : View.GONE);
                }
            }

            @Override
            public void onAllItemsLoaded(List<GalleryItem> items) {
                if (context != null) {
                    if (adapter == null) {
                        adapter = new GalleryGridAdapter(items, context);
                    } else {
                        adapter.addItems(items);
                    }

                    if (galleryGrid.getAdapter() == null) {
                        galleryGrid.setAdapter(adapter);
                    }
                }
                checkNoVideosLoaded();
            }
        });
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public void checkNoVideosLoaded() {
        Helper.setViewVisibility(noVideosLoaded, adapter == null || adapter.getItemCount() == 0 ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onCameraPermissionGranted() {
        if (recordPending) {
            // record video
            recordPending = false;
        } else if (takePhotoPending) {
            // take a photo
            takePhotoPending = false;
        }
    }

    @Override
    public void onCameraPermissionRefused() {
        if (takePhotoPending) {
            takePhotoPending = false;
            Snackbar.make(getView(), R.string.camera_permission_rationale_photo, Toast.LENGTH_LONG).
                    setBackgroundTint(Color.RED).setTextColor(Color.WHITE).show();
            return;
        }

        recordPending = false;
        Snackbar.make(getView(), R.string.camera_permission_rationale_record, Toast.LENGTH_LONG).
                setBackgroundTint(Color.RED).setTextColor(Color.WHITE).show();
    }

    @Override
    public void onRecordAudioPermissionGranted() {

    }

    @Override
    public void onRecordAudioPermissionRefused() {

    }

    @Override
    public void onStoragePermissionGranted() {
        if (loadGalleryItemsPending) {
            loadGalleryItemsPending = false;
            loadGalleryItems();
        }
    }

    @Override
    public void onStoragePermissionRefused() {
        Snackbar.make(getView(), R.string.storage_permission_rationale_videos, Snackbar.LENGTH_LONG).setBackgroundTint(
                ContextCompat.getColor(getContext(), R.color.red)
        ).show();
    }

    @Override
    public boolean shouldHideGlobalPlayer() {
        return true;
    }

    @Override
    public boolean shouldSuspendGlobalPlayer() {
        return true;
    }
}
