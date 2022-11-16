package io.openim.android.ouicore.widget;

import android.app.Activity;
import android.os.Bundle;

import androidx.fragment.app.FragmentActivity;

import io.openim.android.ouicore.base.BaseActivity;
import io.openim.android.ouicore.entity.LoginCertificate;
import io.openim.android.ouicore.im.IM;
import io.openim.android.ouicore.im.IMEvent;
import io.openim.android.ouicore.utils.L;
import io.openim.android.sdk.OpenIMClient;
import io.openim.android.sdk.listener.OnBase;
import io.openim.android.sdk.listener.OnConnListener;

/**
 * 模块调试时使用
 */
public class DebugActivity extends FragmentActivity implements OnBase<String> {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Override
    public void onError(int code, String error) {
        L.e("登录失败---" + error);

    }

    @Override
    public void onSuccess(String data) {

    }
}
