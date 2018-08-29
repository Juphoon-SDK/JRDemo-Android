package com.juphoon.jrsdk;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.cmcc.sso.sdk.auth.AuthnConstants;
import com.cmcc.sso.sdk.auth.TokenListener;
import com.cmcc.sso.sdk.util.SsoSdkConstants;
import com.juphoon.jrsdk.manager.CmccTokenManager;
import com.juphoon.jrsdk.utils.CommonUtils;
import com.juphoon.jrsdk.utils.CommonValue;
import com.juphoon.rcs.jrsdk.JRProfile;
import com.juphoon.rcs.jrsdk.JRProfileCallback;
import com.juphoon.rcs.jrsdk.JRProfileConstants;

import org.json.JSONObject;

import java.io.File;

public class ProfileActivity extends AppCompatActivity implements JRProfileCallback {
    private String mTakenFilePath;
    private ImageView mIcon;
    private EditText mInput;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        JRProfile.getInstance().addCallback(this);
        mIcon = findViewById(R.id.profile_icon);
        mInput = findViewById(R.id.edit_input);
    }

    public void onLoadSelf(View view) {
        CmccTokenManager.getInstance(ProfileActivity.this).getAppToken(null, new TokenListener() {
            @Override
            public void onGetTokenComplete(JSONObject jsonObject) {
                if (jsonObject == null) {
                    return;
                }
                int resultCode = jsonObject.optInt(SsoSdkConstants.VALUES_KEY_RESULT_CODE, -1);
                if (resultCode == AuthnConstants.CLIENT_CODE_SUCCESS) {
                    String token = jsonObject.optString(SsoSdkConstants.VALUES_KEY_TOKEN);
                    JRProfile.getInstance().loadSelfIcon(JRProfileConstants.IconSolution.Solution120, token);
                } else {
                    Toast.makeText(ProfileActivity.this, "Token获取失败，请重试", Toast.LENGTH_SHORT).show();
                }

            }
        });
    }

    public void onLoadUser(View view) {
        if (TextUtils.isEmpty(mInput.getText().toString())) {
            Toast.makeText(this, "号码不能为空", Toast.LENGTH_SHORT).show();
            return;
        }
        CmccTokenManager.getInstance(ProfileActivity.this).getAppToken(null, new TokenListener() {
            @Override
            public void onGetTokenComplete(JSONObject jsonObject) {
                if (jsonObject == null) {
                    return;
                }
                int resultCode = jsonObject.optInt(SsoSdkConstants.VALUES_KEY_RESULT_CODE, -1);
                if (resultCode == AuthnConstants.CLIENT_CODE_SUCCESS) {
                    String token = jsonObject.optString(SsoSdkConstants.VALUES_KEY_TOKEN);
                    JRProfile.getInstance().loadUserIcon(mInput.getText().toString(), JRProfileConstants.IconSolution.Solution120, token);
                } else {
                    Toast.makeText(ProfileActivity.this, "Token获取失败，请重试", Toast.LENGTH_SHORT).show();
                }

            }
        });
    }

    public void onUploadSelf(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        String[] cameraItems = {"拍摄照片", "选择照片"};
        builder.setItems(cameraItems, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (i == 0) {
                    capturePicture();
                } else {
                    choosePicture();
                }
            }
        });
        builder.create().show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != RESULT_OK) {
            return;
        }

        switch (requestCode) {
            case CommonValue.REQUEST_CAMERA_PICTURE:
                CmccTokenManager.getInstance(ProfileActivity.this).getAppToken(null, new TokenListener() {
                    @Override
                    public void onGetTokenComplete(JSONObject jsonObject) {
                        if (jsonObject == null) {
                            return;
                        }
                        int resultCode = jsonObject.optInt(SsoSdkConstants.VALUES_KEY_RESULT_CODE, -1);
                        if (resultCode == AuthnConstants.CLIENT_CODE_SUCCESS) {
                            String token = jsonObject.optString(SsoSdkConstants.VALUES_KEY_TOKEN);
                            JRProfile.getInstance().updateSelfIcon(mTakenFilePath, token);
                        } else {
                            Toast.makeText(ProfileActivity.this, "Token获取失败，请重试", Toast.LENGTH_SHORT).show();
                        }

                    }
                });
                break;
            case CommonValue.REQUEST_CHOOSE_PICTURE:
                CmccTokenManager.getInstance(ProfileActivity.this).getAppToken(null, new TokenListener() {
                    @Override
                    public void onGetTokenComplete(JSONObject jsonObject) {
                        if (jsonObject == null) {
                            return;
                        }
                        int resultCode = jsonObject.optInt(SsoSdkConstants.VALUES_KEY_RESULT_CODE, -1);
                        if (resultCode == AuthnConstants.CLIENT_CODE_SUCCESS) {
                            String token = jsonObject.optString(SsoSdkConstants.VALUES_KEY_TOKEN);
                            String srcPath = CommonUtils.getPath(ProfileActivity.this, data.getData());
                            JRProfile.getInstance().updateSelfIcon(srcPath, token);
                        } else {
                            Toast.makeText(ProfileActivity.this, "Token获取失败，请重试", Toast.LENGTH_SHORT).show();
                        }

                    }
                });

                break;
        }
    }

    private void capturePicture() {
        mTakenFilePath = CommonUtils.getSdcardPath(this) + "/" + CommonUtils.getAppName(this) + "/temp/" + System.currentTimeMillis() + ".png";
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File file = new File(mTakenFilePath);
        if (file.exists()) {
            file.delete();
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            Uri contentUri = FileProvider.getUriForFile(this, "com.juphoon.rcs.juphoonrcsandroid.fileProvider", file);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, contentUri);
        } else {
            intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(file));
        }
        startActivityForResult(intent, CommonValue.REQUEST_CAMERA_PICTURE);
    }

    private void choosePicture() {
        Intent chooseImageIntent = CommonUtils
                .getMediaByTypeIntent("image/*", false);
        if (chooseImageIntent != null) {
            startActivityForResult(chooseImageIntent,
                    CommonValue.REQUEST_CHOOSE_PICTURE);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    @Override
    public void onUpdateIconResult(boolean b) {
        if (b) {
            Toast.makeText(this, "upload success", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "upload failed", Toast.LENGTH_SHORT).show();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    @Override
    public void onLoadSelfIconResult(boolean b, String s) {
        if (b) {
            Toast.makeText(this, "下载成功 路径为 " + s, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "下载失败", Toast.LENGTH_SHORT).show();
        }
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Glide.with(getApplicationContext())
                        .load(s)
                        .centerCrop()
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .into(mIcon);
            }
        }, 1000);
    }

    @Override
    public void onLoadUserIconResult(boolean b, String s, String s1) {
        if (b) {
            Toast.makeText(this, "下载成功 路径为 " + s, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "下载失败", Toast.LENGTH_SHORT).show();
        }
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Glide.with(getApplicationContext())
                        .load(s)
                        .centerCrop()
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .into(mIcon);
            }
        }, 1000);
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    @Override
    protected void onDestroy() {
        JRProfile.getInstance().removeCallback(this);
        Glide.with(getApplicationContext()).pauseRequests();
        super.onDestroy();
    }
}
