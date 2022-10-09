package io.openim.android.ouiconversation.ui;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

import io.openim.android.ouiconversation.R;
import io.openim.android.ouiconversation.databinding.ActivityMediaHistoryBinding;
import io.openim.android.ouiconversation.vm.ChatVM;
import io.openim.android.ouicore.adapter.RecyclerViewAdapter;
import io.openim.android.ouicore.adapter.ViewHol;
import io.openim.android.ouicore.base.BaseActivity;
import io.openim.android.ouicore.utils.Common;
import io.openim.android.ouicore.utils.Constant;
import io.openim.android.ouicore.utils.L;
import io.openim.android.ouicore.utils.TimeUtil;
import io.openim.android.sdk.models.Message;

public class MediaHistoryActivity extends BaseActivity<ChatVM, ActivityMediaHistoryBinding> {

    private boolean isPicture;
    private int page = 1;
    private ExRecyclerViewAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        bindVMByCache(ChatVM.class);
        super.onCreate(savedInstanceState);
        bindViewDataBinding(ActivityMediaHistoryBinding.inflate(getLayoutInflater()));
        sink();
        vm.addSearchMessageItems.getValue().clear();
        isPicture = getIntent().getBooleanExtra(Constant.K_RESULT, false);
        initView();
        listener();

        searchLocalMessages();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (isFinishing())
            vm.addSearchMessageItems.getValue().clear();
    }

    private void searchLocalMessages() {
        vm.searchLocalMessages(null, page, isPicture ? Constant.MsgType.PICTURE : Constant.MsgType.VIDEO);
    }


    private void listener() {
        vm.addSearchMessageItems.observe(this, list -> {
            if (null == list || list.isEmpty()) return;
            adapter.loadMessage(list);
        });

        view.recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                LinearLayoutManager linearLayoutManager = (LinearLayoutManager) view.recyclerView.getLayoutManager();
                int lastVisiblePosition = linearLayoutManager.findLastCompletelyVisibleItemPosition();
                if (lastVisiblePosition == adapter.getItems().size() - 1) {
                    page++;
                    searchLocalMessages();
                }
            }
        });

    }

    private void initView() {
        view.title.setText(isPicture ? io.openim.android.ouicore.R.string.picture :
            io.openim.android.ouicore.R.string.video);
        view.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ExRecyclerViewAdapter();
        adapter.setItems(new ArrayList<>());
        view.recyclerView.setAdapter(adapter);
    }

    public class ExMessage {
        public String sticky;
        public List<Message> messageList = new ArrayList<>();
    }

    private class ExRecyclerViewAdapter extends RecyclerViewAdapter<ExMessage, RecyclerView.ViewHolder> {
        private int TITLE = 1;
        private int CONTENT = 2;

        private String lastSticky = "";


        public void loadMessage(List<Message> items) {
            int startSize = getItems().size();
            if (lastSticky.isEmpty()) {
                lastSticky =
                    TimeUtil.getTimeRules(items.get(0).getSendTime());
                getItems().add(0, getExMessage());
            }
            ExMessage exMessage = null;
            for (int i = 0; i < items.size(); i++) {
                Message message = items.get(i);
                if (message.getSendTime() == 0)
                    continue;
                String timeRules = TimeUtil.getTimeRules(message.getSendTime());
                if (lastSticky.equals(timeRules)) {
                    if (null == exMessage) exMessage = new ExMessage();
                    exMessage.messageList.add(message);
                    if (exMessage.messageList.size() == 4) {
                        getItems().add(exMessage);
                        exMessage = null;
                    }
                } else {
                    if (null != exMessage) {
                        getItems().add(exMessage);
                        exMessage = null;
                    }
                    lastSticky = timeRules;
                    getItems().add(getExMessage());
                }
            }
            if (null != exMessage) getItems().add(exMessage);
            notifyItemRangeChanged(startSize,
                getItems().size());
        }


        @NonNull
        private ExMessage getExMessage() {
            ExMessage message = new ExMessage();
            message.sticky = lastSticky;
            return message;
        }

        @Override
        public int getItemViewType(int position) {
            return getItems().get(position).sticky != null ? TITLE : CONTENT;
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            if (viewType == CONTENT)
                return new ViewHol.RecyclerViewHolder(parent);

            return new ViewHol.StickyViewHo(parent);
        }

        @Override
        public void onBindView(@NonNull RecyclerView.ViewHolder holder, ExMessage data, int position) {
            if (getItemViewType(position) == CONTENT) {
                ViewHol.RecyclerViewHolder recyclerViewHolder = (ViewHol.RecyclerViewHolder) holder;
                RecyclerView recyclerView = recyclerViewHolder.viewBinding.getRoot();
                recyclerView.getLayoutParams().height = ViewGroup.LayoutParams.WRAP_CONTENT;
                recyclerView.setLayoutManager(new GridLayoutManager(MediaHistoryActivity.this, 4));
                RecyclerViewAdapter adapter;
                recyclerView.setAdapter(adapter = new RecyclerViewAdapter<Message, ViewHol.ImageViewHolder>(ViewHol.ImageViewHolder.class) {
                        @Override
                        public void onBindView(@NonNull ViewHol.ImageViewHolder holder, Message data, int position) {
                            String url = isPicture ? data.getPictureElem().getSourcePicture().getUrl() : data.getVideoElem().getVideoUrl();

                            holder.view.getRoot().getLayoutParams().height = Common.dp2px(100);
                            Glide.with(MediaHistoryActivity.this)
                                .load(url)
                                .centerCrop()
                                .into(holder.view.getRoot());
                            holder.view.getRoot().setOnClickListener(v -> {
                                if (isPicture) {
                                    startActivity(
                                        new Intent(MediaHistoryActivity.this, PreviewActivity.class).putExtra(PreviewActivity.MEDIA_URL, url));
                                } else {
                                    startActivity(
                                        new Intent(MediaHistoryActivity.this, PreviewActivity.class)
                                            .putExtra(PreviewActivity.MEDIA_URL, url)
                                            .putExtra(PreviewActivity.FIRST_FRAME, data.getVideoElem().getSnapshotUrl()));
                                }
                            });
                        }
                    }
                );
                adapter.setItems(data.messageList);
                recyclerView.setNestedScrollingEnabled(false);
            } else {
                ViewHol.StickyViewHo stickyViewHo = (ViewHol.StickyViewHo) holder;
                stickyViewHo.view.title.setText(data.sticky);
            }
        }
    }

}
