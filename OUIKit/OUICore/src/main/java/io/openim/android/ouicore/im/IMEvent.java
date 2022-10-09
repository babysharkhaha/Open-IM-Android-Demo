package io.openim.android.ouicore.im;


import com.alibaba.android.arouter.launcher.ARouter;

import java.util.ArrayList;
import java.util.List;


import io.openim.android.ouicore.base.BaseApp;
import io.openim.android.ouicore.services.CallingService;
import io.openim.android.ouicore.utils.L;
import io.openim.android.ouicore.utils.Routes;
import io.openim.android.sdk.OpenIMClient;
import io.openim.android.sdk.listener.OnAdvanceMsgListener;
import io.openim.android.sdk.listener.OnConnListener;
import io.openim.android.sdk.listener.OnConversationListener;
import io.openim.android.sdk.listener.OnFriendshipListener;
import io.openim.android.sdk.listener.OnGroupListener;
import io.openim.android.sdk.listener.OnSignalingListener;
import io.openim.android.sdk.listener.OnUserListener;
import io.openim.android.sdk.models.BlacklistInfo;
import io.openim.android.sdk.models.ConversationInfo;
import io.openim.android.sdk.models.FriendApplicationInfo;
import io.openim.android.sdk.models.FriendInfo;
import io.openim.android.sdk.models.GroupApplicationInfo;
import io.openim.android.sdk.models.GroupInfo;
import io.openim.android.sdk.models.GroupMembersInfo;
import io.openim.android.sdk.models.Message;
import io.openim.android.sdk.models.ReadReceiptInfo;
import io.openim.android.sdk.models.RevokedInfo;
import io.openim.android.sdk.models.RoomCallingInfo;
import io.openim.android.sdk.models.SignalingInfo;
import io.openim.android.sdk.models.UserInfo;

///im事件 统一处理
public class IMEvent {
    private static IMEvent listener = null;
    private List<OnConnListener> connListeners;
    private List<OnAdvanceMsgListener> advanceMsgListeners;
    private List<OnConversationListener> conversationListeners;
    private List<OnGroupListener> groupListeners;
    private List<OnFriendshipListener> friendshipListeners;

    public void init() {
        connListeners = new ArrayList<>();
        advanceMsgListeners = new ArrayList<>();
        conversationListeners = new ArrayList<>();
        groupListeners = new ArrayList<>();
        friendshipListeners = new ArrayList<>();

        userListener();
        advanceMsgListener();
        friendshipListener();
        conversationListener();
        groupListeners();
//        signalingListener();
    }

    private void signalingListener() {
        OpenIMClient.getInstance().signalingManager.setSignalingListener(new OnSignalingListener() {
            @Override
            public void onInvitationCancelled(SignalingInfo s) {
                // 被邀请者收到：邀请者取消音视频通话
            }

            @Override
            public void onInvitationTimeout(SignalingInfo s) {
                // 邀请者收到：被邀请者超时未接通
            }

            @Override
            public void onInviteeAccepted(SignalingInfo s) {
                // 邀请者收到：被邀请者同意音视频通话
            }

            @Override
            public void onInviteeAcceptedByOtherDevice(SignalingInfo s) {

            }

            @Override
            public void onInviteeRejected(SignalingInfo s) {
                // 邀请者收到：被邀请者拒绝音视频通话
            }

            @Override
            public void onInviteeRejectedByOtherDevice(SignalingInfo s) {

            }

            @Override
            public void onReceiveNewInvitation(SignalingInfo s) {
                // 被邀请者收到：音视频通话邀请
            }

            @Override
            public void onHangup(SignalingInfo s) {

            }

            @Override
            public void onRoomParticipantConnected(RoomCallingInfo s) {

            }

            @Override
            public void onRoomParticipantDisconnected(RoomCallingInfo s) {

            }
        });
    }

    public static synchronized IMEvent getInstance() {
        if (null == listener)
            listener = new IMEvent();
        return listener;
    }

    //连接事件
    public void addConnListener(OnConnListener onConnListener) {
        if (!connListeners.contains(onConnListener)) {
            connListeners.add(onConnListener);
        }
    }

    public void removeConnListener(OnConnListener onConnListener) {
        connListeners.remove(onConnListener);
    }

    // 会话新增或改变监听
    public void addConversationListener(OnConversationListener onConversationListener) {
        if (!conversationListeners.contains(onConversationListener)) {
            conversationListeners.add(onConversationListener);
        }
    }

    public void removeConversationListener(OnConversationListener onConversationListener) {
        conversationListeners.remove(onConversationListener);
    }

    // 收到新消息，已读回执，消息撤回监听。
    public void addAdvanceMsgListener(OnAdvanceMsgListener onAdvanceMsgListener) {
        if (!advanceMsgListeners.contains(onAdvanceMsgListener)) {
            advanceMsgListeners.add(onAdvanceMsgListener);
        }
    }

    public void removeAdvanceMsgListener(OnAdvanceMsgListener onAdvanceMsgListener) {
        advanceMsgListeners.remove(onAdvanceMsgListener);
    }

    // 群组关系发生改变监听
    public void addGroupListener(OnGroupListener onGroupListener) {
        if (!groupListeners.contains(onGroupListener)) {
            groupListeners.add(onGroupListener);
        }
    }

    public void removeGroupListener(OnGroupListener onGroupListener) {
        groupListeners.remove(onGroupListener);
    }

    // 好友关系发生改变监听
    public void addFriendListener(OnFriendshipListener onFriendshipListener) {
        if (!friendshipListeners.contains(onFriendshipListener)) {
            friendshipListeners.add(onFriendshipListener);
        }
    }

    public void removeFriendListener(OnFriendshipListener onFriendshipListener) {
        friendshipListeners.remove(onFriendshipListener);
    }


    //连接事件
    public OnConnListener connListener = new OnConnListener() {

        @Override
        public void onConnectFailed(long code, String error) {
            // 连接服务器失败，可以提示用户当前网络连接不可用
            L.d("连接服务器失败");
            for (OnConnListener onConnListener : connListeners) {
                onConnListener.onConnectFailed(code, error);
            }
        }

        @Override
        public void onConnectSuccess() {
            // 已经成功连接到服务器
            L.d("已经成功连接到服务器");
            for (OnConnListener onConnListener : connListeners) {
                onConnListener.onConnectSuccess();
            }
        }

        @Override
        public void onConnecting() {
            // 正在连接到服务器，适合在 UI 上展示“正在连接”状态。
            L.d("正在连接到服务器");
            for (OnConnListener onConnListener : connListeners) {
                onConnListener.onConnecting();
            }
        }

        @Override
        public void onKickedOffline() {
            // 当前用户被踢下线，此时可以 UI 提示用户“您已经在其他端登录了当前账号，是否重新登录？”
            L.d("当前用户被踢下线");

            CallingService callingService = (CallingService) ARouter.getInstance()
                .build(Routes.Service.CALLING).navigation();
            if (null != callingService)
                callingService.stopAudioVideoService(BaseApp.inst());
        }

        @Override
        public void onUserTokenExpired() {
            // 登录票据已经过期，请使用新签发的 UserSig 进行登录。
            L.d("登录票据已经过期");
        }
    };


    // 群组关系发生改变监听
    private void groupListeners() {
        OpenIMClient.getInstance().groupManager.setOnGroupListener(new OnGroupListener() {
            @Override
            public void onGroupApplicationAccepted(GroupApplicationInfo info) {
                // 发出或收到的组申请被接受
                for (OnGroupListener onGroupListener : groupListeners) {
                    onGroupListener.onGroupApplicationAccepted(info);
                }
            }

            @Override
            public void onGroupApplicationAdded(GroupApplicationInfo info) {
                // 发出或收到的组申请有新增
                for (OnGroupListener onGroupListener : groupListeners) {
                    onGroupListener.onGroupApplicationAdded(info);
                }
            }

            @Override
            public void onGroupApplicationDeleted(GroupApplicationInfo info) {
                // 发出或收到的组申请被删除
                for (OnGroupListener onGroupListener : groupListeners) {
                    onGroupListener.onGroupApplicationDeleted(info);
                }
            }

            @Override
            public void onGroupApplicationRejected(GroupApplicationInfo info) {
                // 发出或收到的组申请被拒绝
                for (OnGroupListener onGroupListener : groupListeners) {
                    onGroupListener.onGroupApplicationRejected(info);
                }
            }

            @Override
            public void onGroupInfoChanged(GroupInfo info) {
                // 组资料变更
                for (OnGroupListener onGroupListener : groupListeners) {
                    onGroupListener.onGroupInfoChanged(info);
                }
            }

            @Override
            public void onGroupMemberAdded(GroupMembersInfo info) {
                // 组成员进入
                for (OnGroupListener onGroupListener : groupListeners) {
                    onGroupListener.onGroupMemberAdded(info);
                }
            }

            @Override
            public void onGroupMemberDeleted(GroupMembersInfo info) {
                // 组成员退出
            }

            @Override
            public void onGroupMemberInfoChanged(GroupMembersInfo info) {
                // 组成员信息发生变化
            }

            @Override
            public void onJoinedGroupAdded(GroupInfo info) {
                // 创建群： 初始成员收到；邀请进群：被邀请者收到
                for (OnGroupListener onGroupListener : groupListeners) {
                    onGroupListener.onJoinedGroupAdded(info);
                }
            }

            @Override
            public void onJoinedGroupDeleted(GroupInfo info) {
                // 退出群：退出者收到；踢出群：被踢者收到
                for (OnGroupListener onGroupListener : groupListeners) {
                    onGroupListener.onJoinedGroupDeleted(info);
                }
            }
        });
    }

    // 会话新增或改变监听
    private void conversationListener() {
        OpenIMClient.getInstance().conversationManager.setOnConversationListener(new OnConversationListener() {
            @Override
            public void onConversationChanged(List<ConversationInfo> list) {
                // 已添加的会话发生改变
                for (OnConversationListener onConversationListener : conversationListeners) {
                    onConversationListener.onConversationChanged(list);
                }
            }

            @Override
            public void onNewConversation(List<ConversationInfo> list) {
                // 新增会话
                for (OnConversationListener onConversationListener : conversationListeners) {
                    onConversationListener.onNewConversation(list);
                }
            }

            @Override
            public void onSyncServerFailed() {

            }

            @Override
            public void onSyncServerFinish() {

            }

            @Override
            public void onSyncServerStart() {

            }

            @Override
            public void onTotalUnreadMessageCountChanged(int i) {
                // 未读消息数发送变化
                L.e("");
            }
        });
    }

    // 好关系发生变化监听
    private void friendshipListener() {
        OpenIMClient.getInstance().friendshipManager.setOnFriendshipListener(new OnFriendshipListener() {
            @Override
            public void onBlacklistAdded(BlacklistInfo u) {
                // 拉入黑名单
            }

            @Override
            public void onBlacklistDeleted(BlacklistInfo u) {
                // 从黑名单删除
            }

            @Override
            public void onFriendApplicationAccepted(FriendApplicationInfo u) {
                // 发出或收到的好友申请已同意
            }

            @Override
            public void onFriendApplicationAdded(FriendApplicationInfo u) {
                // 发出或收到的好友申请被添加
                for (OnFriendshipListener friendshipListener : friendshipListeners) {
                    friendshipListener.onFriendApplicationAdded(u);
                }
            }

            @Override
            public void onFriendApplicationDeleted(FriendApplicationInfo u) {
                // 发出或收到的好友申请被删除
            }

            @Override
            public void onFriendApplicationRejected(FriendApplicationInfo u) {
                // 发出或收到的好友申请被拒绝
            }


            @Override
            public void onFriendInfoChanged(FriendInfo u) {
                // 朋友的资料发生变化
            }

            @Override
            public void onFriendAdded(FriendInfo u) {
                // 好友被添加
            }

            @Override
            public void onFriendDeleted(FriendInfo u) {
                // 好友被删除
            }
        });
    }

    // 收到新消息，已读回执，消息撤回监听。
    private void advanceMsgListener() {
        OpenIMClient.getInstance().messageManager.setAdvancedMsgListener(new OnAdvanceMsgListener() {
            @Override
            public void onRecvNewMessage(Message msg) {
                // 收到新消息，界面添加新消息
                for (OnAdvanceMsgListener onAdvanceMsgListener : advanceMsgListeners) {
                    onAdvanceMsgListener.onRecvNewMessage(msg);
                }
            }

            @Override
            public void onRecvC2CReadReceipt(List<ReadReceiptInfo> list) {
                // 消息被阅读回执，将消息标记为已读
                for (OnAdvanceMsgListener onAdvanceMsgListener : advanceMsgListeners) {
                    onAdvanceMsgListener.onRecvC2CReadReceipt(list);
                }
            }

            @Override
            public void onRecvMessageRevoked(String msgId) {
                // 消息成功撤回，从界面移除消息
                for (OnAdvanceMsgListener onAdvanceMsgListener : advanceMsgListeners) {
                    onAdvanceMsgListener.onRecvMessageRevoked(msgId);
                }
            }

            @Override
            public void onRecvMessageRevokedV2(RevokedInfo info) {

            }

            @Override
            public void onRecvGroupMessageReadReceipt(List<ReadReceiptInfo> list) {
                // 消息被阅读回执，将消息标记为已读
                for (OnAdvanceMsgListener onAdvanceMsgListener : advanceMsgListeners) {
                    onAdvanceMsgListener.onRecvGroupMessageReadReceipt(list);
                }
            }
        });
    }


    // 用户资料变更监听
    private void userListener() {
        OpenIMClient.getInstance().userInfoManager.setOnUserListener(new OnUserListener() {
            @Override
            public void onSelfInfoUpdated(UserInfo info) {
                // 当前登录用户资料变更回调
            }
        });
    }
}


