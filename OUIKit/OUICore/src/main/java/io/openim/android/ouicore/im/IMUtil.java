package io.openim.android.ouicore.im;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.ImageSpan;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.alibaba.android.arouter.launcher.ARouter;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import io.openim.android.ouicore.R;
import io.openim.android.ouicore.base.BaseApp;
import io.openim.android.ouicore.entity.AtMsgInfo;
import io.openim.android.ouicore.entity.AtUsersInfo;
import io.openim.android.ouicore.entity.LocationInfo;
import io.openim.android.ouicore.entity.LoginCertificate;
import io.openim.android.ouicore.entity.MsgConversation;
import io.openim.android.ouicore.entity.MsgExpand;
import io.openim.android.ouicore.net.bage.GsonHel;
import io.openim.android.ouicore.services.CallingService;
import io.openim.android.ouicore.utils.Common;
import io.openim.android.ouicore.utils.Constant;
import io.openim.android.ouicore.utils.EmojiUtil;
import io.openim.android.ouicore.utils.L;
import io.openim.android.ouicore.utils.Routes;
import io.openim.android.ouicore.widget.BottomPopDialog;
import io.openim.android.sdk.OpenIMClient;
import io.openim.android.sdk.models.Message;
import io.openim.android.sdk.models.OfflinePushInfo;
import io.openim.android.sdk.models.SignalingInfo;
import io.openim.android.sdk.models.SignalingInvitationInfo;

public class IMUtil {
    //android PlatformID 2
    public static final int PLATFORM_ID = 2;
    private static final String TAG = "IMUtil";

    /**
     * 会话排序比较器
     */
    public static Comparator<MsgConversation> simpleComparator() {
        return (a, b) -> {
            if ((a.conversationInfo.isPinned() && b.conversationInfo.isPinned()) ||
                (!a.conversationInfo.isPinned() && !b.conversationInfo.isPinned())) {
                long aCompare = Math.max(a.conversationInfo.getDraftTextTime(), a.conversationInfo.getLatestMsgSendTime());
                long bCompare = Math.max(b.conversationInfo.getDraftTextTime(), b.conversationInfo.getLatestMsgSendTime());
                return Long.compare(bCompare, aCompare);
            } else if (a.conversationInfo.isPinned() && !b.conversationInfo.isPinned()) {
                return -1;
            } else {
                return 1;
            }
        };
    }


    /**
     * 设置时间显示
     *
     * @param list
     * @return
     */
    public static List<Message> calChatTimeInterval(List<Message> list) {
        Message first = list.get(0);
        long lastShowTimeStamp = first.getSendTime();
        for (int i = 1; i < list.size(); i++) {
            Message message = list.get(i);
            if (lastShowTimeStamp - message.getSendTime() > (1000 * 60 * 5)) {
                lastShowTimeStamp = message.getSendTime();
                MsgExpand msgExpand = (MsgExpand) message.getExt();
                if (null == msgExpand) {
                    msgExpand = new MsgExpand();
                    message.setExt(msgExpand);
                }
                msgExpand.isShowTime = true;
            }
        }
        return list;
    }

    public static Message createMergerMessage(boolean isSingleChat, String otherSideName, List<Message> list) {
        String title = "";
        List<String> summaryList = new ArrayList<>();
        for (Message message : list) {
            summaryList.add(message.getSenderNickname() + ":" + getMsgParse(message));
            if (summaryList.size() >= 2) break;
        }
        if (isSingleChat) {
            title = LoginCertificate.getCache(BaseApp.inst()).nickname
                + BaseApp.inst().getString(R.string.and) + otherSideName
                + BaseApp.inst().getString(R.string.chat_history);
        } else {
            title = BaseApp.inst().getString(R.string.group_chat_history);
        }

        return OpenIMClient.getInstance().messageManager.createMergerMessage(list, title, summaryList);
    }

    /**
     * 解析扩展信息 避免在bindView时解析造成卡顿
     *
     * @param msg
     */
    public static Message buildExpandInfo(Message msg) {
        MsgExpand msgExpand = (MsgExpand) msg.getExt();
        if (null == msgExpand)
            msgExpand = new MsgExpand();
        try {
            if (msg.getContentType() == Constant.MsgType.LOCATION)
                msgExpand.locationInfo = GsonHel.fromJson(msg.getLocationElem().getDescription(), LocationInfo.class);
            if (msg.getContentType() == Constant.MsgType.MENTION) {
                msgExpand.atMsgInfo = GsonHel.fromJson(msg.getContent(), AtMsgInfo.class);
                handleAt(msgExpand);
            }
            handleEmoji(msgExpand, msg);
        } catch (Exception e) {
            e.printStackTrace();
        }
        msg.setExt(msgExpand);
        return msg;
    }

    private static void handleEmoji(MsgExpand expand, Message msg) {
        String content = msg.getContent();
        for (String key : EmojiUtil.emojiFaces.keySet()) {
            int fromIndex = 0;
            if (content.contains(key)) {
                if (null == expand.sequence) {
                    expand.sequence = new SpannableStringBuilder(content);
                }
                while ((fromIndex = content.indexOf(key, fromIndex)) > -1) {
                    int emojiId = Common.getMipmapId(EmojiUtil.emojiFaces.get(key));
                    Drawable drawable = BaseApp.inst().getResources().getDrawable(emojiId, null);
                    drawable.setBounds(0, 0, Common.dp2px(22), Common.dp2px(22));
                    ImageSpan imageSpan = new ImageSpan(drawable);
                    expand.sequence.setSpan(imageSpan, fromIndex, fromIndex + key.length()
                        , Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    fromIndex += 1;//往后继续查
                }
            }
        }
    }

    private static void handleAt(MsgExpand msgExpand) {
        if (null == msgExpand.atMsgInfo) return;
        String atTxt = msgExpand.atMsgInfo.text;
        for (AtUsersInfo atUsersInfo : msgExpand.atMsgInfo.atUsersInfo) {
            atTxt = atTxt.replace("@" + atUsersInfo.atUserID, "@" + atUsersInfo.groupNickname);
        }
        SpannableStringBuilder spannableString = new SpannableStringBuilder(atTxt);
        for (AtUsersInfo atUsersInfo : msgExpand.atMsgInfo.atUsersInfo) {
            String tag = "@" + atUsersInfo.groupNickname;
            ForegroundColorSpan colorSpan = new ForegroundColorSpan(Color.parseColor("#009ad6"));
            ClickableSpan clickableSpan = new ClickableSpan() {
                @Override
                public void onClick(View view) {
                    ARouter.getInstance().build(Routes.Main.PERSON_DETAIL)
                        .withString(Constant.K_ID, atUsersInfo.atUserID).navigation(view.getContext());
                }
            };
            int start = spannableString.toString().indexOf(tag);
            int end = spannableString.toString().indexOf(tag) + tag.length();
            spannableString.setSpan(colorSpan, start, end
                , Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            spannableString.setSpan(clickableSpan, start, end,
                Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
        }
        msgExpand.sequence = spannableString;
    }

    /**
     * 解析消息内容
     *
     * @param msg
     * @return
     */
    public static String getMsgParse(Message msg) {
        String lastMsg = "";
        switch (msg.getContentType()) {
            default:
                lastMsg = msg.getNotificationElem().getDefaultTips();
                break;
            case Constant.MsgType.TXT:
                lastMsg = msg.getContent();
                break;
            case Constant.MsgType.PICTURE:
                lastMsg = "[" + BaseApp.inst().getString(io.openim.android.ouicore.R.string.picture) + "]";
                break;
            case Constant.MsgType.VOICE:
                lastMsg = "[" + BaseApp.inst().getString(io.openim.android.ouicore.R.string.voice) + "]";
                break;
            case Constant.MsgType.VIDEO:
                lastMsg = "[" + BaseApp.inst().getString(io.openim.android.ouicore.R.string.video) + "]";
                break;
            case Constant.MsgType.FILE:
                lastMsg = "[" + BaseApp.inst().getString(io.openim.android.ouicore.R.string.file) + "]";
                break;
            case Constant.MsgType.LOCATION:
                lastMsg = "[" + BaseApp.inst().getString(io.openim.android.ouicore.R.string.location) + "]";
                break;
            case Constant.MsgType.MENTION:
                MsgExpand msgExpand = (MsgExpand) msg.getExt();
                String atTxt = msgExpand.atMsgInfo.text;
                for (AtUsersInfo atUsersInfo : msgExpand.atMsgInfo.atUsersInfo) {
                    atTxt = atTxt.replace("@" + atUsersInfo.atUserID, "@" + atUsersInfo.groupNickname);
                }
                lastMsg = atTxt;
                break;
            case Constant.MsgType.MERGE:
                lastMsg = "[" + BaseApp.inst().getString(io.openim.android.ouicore.R.string.chat_history2) + "]";
                break;
        }
        return lastMsg;
    }

    /**
     * build SignalingInfo
     *
     * @param isVideoCalls   是否视频通话
     * @param isSingleChat   是否单聊
     * @param inviteeUserIDs 邀请ids
     * @param groupID        群id
     * @return
     */
    public static SignalingInfo buildSignalingInfo(boolean isVideoCalls, boolean isSingleChat,
                                                   List<String> inviteeUserIDs, String groupID) {
        SignalingInfo signalingInfo = new SignalingInfo();
        String inId = BaseApp.inst().loginCertificate.userID;
        signalingInfo.setOpUserID(inId);
        SignalingInvitationInfo signalingInvitationInfo = new SignalingInvitationInfo();
        signalingInvitationInfo.setInviterUserID(inId);
        signalingInvitationInfo.setInviteeUserIDList(inviteeUserIDs);
        signalingInvitationInfo.setRoomID(String.valueOf(UUID.randomUUID()));
        signalingInvitationInfo.setTimeout(30);
        signalingInvitationInfo.setMediaType(isVideoCalls ? "video" : "audio");
        signalingInvitationInfo.setPlatformID(IMUtil.PLATFORM_ID);
        signalingInvitationInfo.setSessionType(isSingleChat ? 1 : 2);
        signalingInvitationInfo.setGroupID(groupID);

        signalingInfo.setInvitation(signalingInvitationInfo);
        signalingInfo.setOfflinePushInfo(new OfflinePushInfo());
        return signalingInfo;
    }

    /**
     * 弹出底部菜单选择 音视通话
     */
    public static void showBottomPopMenu(Context context, View.OnKeyListener v) {
        BottomPopDialog dialog = new BottomPopDialog(context);
        dialog.show();
        dialog.getMainView().menu3.setOnClickListener(v1 -> dialog.dismiss());
        dialog.getMainView().menu1.setText(io.openim.android.ouicore.R.string.voice_calls);
        dialog.getMainView().menu2.setText(io.openim.android.ouicore.R.string.video_calls);

        dialog.getMainView().menu1.setOnClickListener(v1 -> {
            v.onKey(v1, 1, null);
            dialog.dismiss();
        });
        dialog.getMainView().menu2.setOnClickListener(v1 -> {
            v.onKey(v1, 2, null);
            dialog.dismiss();
        });
    }

    /**
     * 已登录或登录中
     *
     * @return
     */
    public static boolean isLogged() {
        long status = OpenIMClient.getInstance().getLoginStatus();
        L.e(TAG, "login status-----[" + status + "]");
        return status == 101 || status == 102;
    }

    /**
     * 退出
     *
     * @param from
     * @param to
     */
    public static void logout(AppCompatActivity from, Class<?> to) {
        from.startActivity(new Intent(from, to));
        LoginCertificate.clear();
        CallingService callingService = (CallingService) ARouter.getInstance()
            .build(Routes.Service.CALLING).navigation();
        if (null != callingService)
            callingService.stopAudioVideoService(from);
        from.finish();
    }
}
