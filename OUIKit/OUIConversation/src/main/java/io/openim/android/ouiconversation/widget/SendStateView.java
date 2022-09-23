package io.openim.android.ouiconversation.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.wang.avi.AVLoadingIndicatorView;

import io.openim.android.ouiconversation.R;
import io.openim.android.ouiconversation.databinding.LayoutSendStateBinding;
import io.openim.android.ouicore.utils.Constant;

public class SendStateView extends FrameLayout {
    public SendStateView(Context context) {
        super(context);
        init();
    }

    public SendStateView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public SendStateView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }
    private AVLoadingIndicatorView loading;
    private ImageView failedSend;
    private void init() {
        inflate(getContext(), R.layout.layout_send_state, this);
        loading =findViewById(R.id.loading);
        failedSend =findViewById(R.id.failedSend);
    }

    public void setSendState(int state) {
        if (state == Constant.Send_State.SENDING) {
            loading.setVisibility(View.VISIBLE);
            failedSend.setVisibility(View.GONE);
        } else if (state == Constant.Send_State.SEND_FAILED) {
            failedSend.setVisibility(View.VISIBLE);
            loading.setVisibility(View.GONE);
        } else {
            failedSend.setVisibility(View.GONE);
            loading.setVisibility(View.GONE);
        }
    }

}
