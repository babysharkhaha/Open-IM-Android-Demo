package io.openim.android.ouiconversation.ui;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;

import com.bumptech.glide.Glide;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import cn.jzvd.Jzvd;
import io.openim.android.ouiconversation.R;
import io.openim.android.ouiconversation.databinding.ActivityPreviewBinding;
import io.openim.android.ouicore.base.BaseActivity;
import io.openim.android.ouicore.base.BaseViewModel;
import io.openim.android.ouicore.net.RXRetrofit.N;
import io.openim.android.ouicore.net.RXRetrofit.NetObserver;
import io.openim.android.ouicore.services.OneselfService;
import io.openim.android.ouicore.utils.Common;
import io.openim.android.ouicore.utils.Constant;
import io.openim.android.ouicore.utils.L;
import io.openim.android.ouicore.utils.MediaFileUtil;
import io.openim.android.ouicore.utils.SinkHelper;
import io.reactivex.Observable;

public class PreviewActivity extends BaseActivity<BaseViewModel, ActivityPreviewBinding> {


    public static final String MEDIA_URL = "media_url";
    public static final String FIRST_FRAME = "first_frame";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bindViewDataBinding(ActivityPreviewBinding.inflate(getLayoutInflater()));
        SinkHelper.get(this).setTranslucentStatus(null);
        initView();
    }

    private void initView() {
        String url = getIntent().getStringExtra(MEDIA_URL);
        String firstFrame = getIntent().getStringExtra(FIRST_FRAME);
        if (TextUtils.isEmpty(url)) return;

        if (MediaFileUtil.isImageType(url)) {
            view.pic.setVisibility(View.VISIBLE);
            view.download.setVisibility(View.VISIBLE);
            Glide.with(this)
                .load(url)
                .into(view.pic);
            view.pic.setOnClickListener(v -> finish());
            view.download.setOnClickListener(v -> {
                toast(getString(io.openim.android.ouicore.R.string.start_download));
                Common.downloadFile(url,null,
                    getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, new ContentValues()))
                    .subscribe(new NetObserver<Boolean>(this) {
                        @Override
                        public void onSuccess(Boolean success) {
                            if (success)
                                toast(getString(io.openim.android.ouicore.R.string.save_succ));
                            else
                                toast(getString(io.openim.android.ouicore.R.string.save_failure));
                        }

                        @Override
                        protected void onFailure(Throwable e) {
                            toast(e.getMessage());
                        }
                    });
            });
        } else if (MediaFileUtil.isVideoType(url)) {
            view.jzVideo.setVisibility(View.VISIBLE);
            view.jzVideo.setUp(url, "");
            Glide.with(this)
                .load(firstFrame)
                .into(view.jzVideo.posterImageView);
        }

    }

    public static ContentValues getImageContentValues(File paramFile, long paramLong) {
        ContentValues localContentValues = new ContentValues();
        localContentValues.put("title", paramFile.getName());
        localContentValues.put("_display_name", paramFile.getName());
        localContentValues.put("mime_type", "image/jpeg");
        localContentValues.put("datetaken", Long.valueOf(paramLong));
        localContentValues.put("date_modified", Long.valueOf(paramLong));
        localContentValues.put("date_added", Long.valueOf(paramLong));
        localContentValues.put("orientation", Integer.valueOf(0));
        localContentValues.put("_data", paramFile.getAbsolutePath());
        localContentValues.put("_size", Long.valueOf(paramFile.length()));
        return localContentValues;
    }

    @Override
    public void onBackPressed() {
        if (Jzvd.backPress()) {
            return;
        }
        super.onBackPressed();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Jzvd.releaseAllVideos();
    }
}
