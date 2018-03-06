package com.winterpei.videolivewallpaper;


import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Toast;

import java.util.ArrayList;

import droidninja.filepicker.FilePickerBuilder;
import droidninja.filepicker.FilePickerConst;
import droidninja.filepicker.utils.Orientation;
import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.PermissionUtils;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private int MAX_ATTACHMENT_COUNT = 5;

    private static final String[] PERMISSION_ONPICKPHOTO = new String[]{"android.permission.WRITE_EXTERNAL_STORAGE"};
    private static final int REQUEST_ONPICKPHOTO = 2;

    ArrayList<String> photoPaths = new ArrayList<>();
    ArrayList<String> docPaths = new ArrayList<>();

    private Button mButton;
    private CheckBox mCheckBox;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
        initView();
    }

    private void initView() {

        mCheckBox = (CheckBox) findViewById(R.id.id_cb_voice);
        mButton = (Button) findViewById(R.id.choiceBtn);
        mButton.setOnClickListener(this);
        mCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    //静音
                    VideoLiveWallpaper.voiceSilence(MainActivity.this);
                } else {
                    VideoLiveWallpaper.voiceNormal(MainActivity.this);
                }
            }
        });

    }


    @Override
    public void onClick(View view) {
        int viewId = view.getId();
        switch (viewId) {
            case R.id.choiceBtn:
                if (PermissionUtils.hasSelfPermissions(this, PERMISSION_ONPICKPHOTO)) {
                    this.onPickPhoto();
                } else {
                    ActivityCompat.requestPermissions(this, PERMISSION_ONPICKPHOTO, REQUEST_ONPICKPHOTO);
                }
                break;
        }
    }

    @NeedsPermission({Manifest.permission.WRITE_EXTERNAL_STORAGE})
    private void onPickPhoto() {
        photoPaths.clear();
        int maxCount = MAX_ATTACHMENT_COUNT - docPaths.size();
        if ((docPaths.size() + photoPaths.size()) == MAX_ATTACHMENT_COUNT)
            Toast.makeText(this, "Cannot select more than " + MAX_ATTACHMENT_COUNT + " items", Toast.LENGTH_SHORT).show();
        else
            FilePickerBuilder.getInstance().setMaxCount(1)
                    .setSelectedFiles(photoPaths)
                    .setActivityTheme(R.style.AppTheme)
                    .enableVideoPicker(true)
                    .enableCameraSupport(true)
                    .showGifs(false)
                    .showFolderView(true)
                    .enableImagePicker(false)
                    .setCameraPlaceholder(R.drawable.custom_camera)
                    .withOrientation(Orientation.UNSPECIFIED)
                    .pickPhoto(this);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case FilePickerConst.REQUEST_CODE_PHOTO:
                if (resultCode == Activity.RESULT_OK && data != null) {
                    photoPaths.addAll(data.getStringArrayListExtra(FilePickerConst.KEY_SELECTED_MEDIA));
                    VideoLiveWallpaper.setVideoToWallpaper(this);
                    VideoLiveWallpaper.setVideoPath(getApplicationContext(), photoPaths.get(0));
                }
                break;
            case FilePickerConst.REQUEST_CODE_DOC:
                if (resultCode == Activity.RESULT_OK && data != null) {
                    docPaths.addAll(data.getStringArrayListExtra(FilePickerConst.KEY_SELECTED_DOCS));
                }
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_ONPICKPHOTO:
                if (PermissionUtils.getTargetSdkVersion(this) < 23 && !PermissionUtils.hasSelfPermissions(this, PERMISSION_ONPICKPHOTO)) {
                    return;
                }
                if (PermissionUtils.verifyPermissions(grantResults)) {
                    this.onPickPhoto();
                }
                break;
        }
    }
}
