package io.openim.android.ouiconversation.widget;

import android.content.Context;
import android.text.Selection;
import android.text.style.BackgroundColorSpan;
import android.text.style.CharacterStyle;
import android.text.style.ForegroundColorSpan;
import android.text.style.ImageSpan;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputConnectionWrapper;

import androidx.appcompat.widget.AppCompatEditText;

import java.util.Iterator;
import java.util.List;

import io.openim.android.ouiconversation.vm.ChatVM;
import io.openim.android.ouicore.entity.MsgExpand;
import io.openim.android.sdk.models.Message;

public class TailInputEditText extends AppCompatEditText {
    private ChatVM chatVM;

    public void setChatVM(ChatVM chatVM) {
        this.chatVM = chatVM;
    }

    public TailInputEditText(Context context) {
        this(context, null);
    }

    public TailInputEditText(Context context, AttributeSet attrs) {
        this(context, attrs, android.R.attr.editTextStyle);
    }

    public TailInputEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }


    @Override
    public InputConnection onCreateInputConnection(EditorInfo outAttrs) {
        // 返回自己的实现
        return new BackspaceInputConnection(super.onCreateInputConnection(outAttrs), true);
    }

    private class BackspaceInputConnection extends InputConnectionWrapper {

        public BackspaceInputConnection(InputConnection target, boolean mutable) {
            super(target, mutable);
        }

        /**
         * 当软键盘删除文本之前，会调用这个方法通知输入框，我们可以重写这个方法并判断是否要拦截这个删除事件。
         * 在谷歌输入法上，点击退格键的时候不会调用{@link #sendKeyEvent(KeyEvent event)}，
         * 而是直接回调这个方法，所以也要在这个方法上做拦截；
         */
        @Override
        public boolean deleteSurroundingText(int beforeLength, int afterLength) {
            //监听删除操作，找到最靠近删除的一个Span，然后整体删除
            final int selectionStart = Selection.getSelectionStart(TailInputEditText.this.getText());
            final int selectionEnd = Selection.getSelectionEnd(TailInputEditText.this.getText());

            final CharacterStyle characterStyles[] = TailInputEditText.this.getText().getSpans(selectionStart, selectionEnd, CharacterStyle.class);
            for (CharacterStyle characterStyle : characterStyles) {
                if (characterStyle == null) {
                    continue;
                }
                if (TailInputEditText.this.getText().getSpanEnd(characterStyle) == selectionStart) {
                    int spanStart = TailInputEditText.this.getText().getSpanStart(characterStyle);
                    int spanEnd = TailInputEditText.this.getText().getSpanEnd(characterStyle);
                    spanStart += 1;
                    TailInputEditText.this.getText().delete(spanStart, spanEnd);
                }
                if (characterStyle instanceof ForegroundColorSpan) {
                    //表示@消息
                    List<Message> atMessages = chatVM.atMessages.getValue();
                    Iterator iterator = atMessages.iterator();
                    while (iterator.hasNext()) {
                        Message message = (Message) iterator.next();
                        try {
                            MsgExpand msgExpand = (MsgExpand) message.getExt();
                            if (msgExpand.spanHashCode == characterStyle.hashCode()) {
                                iterator.remove();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
            return super.deleteSurroundingText(beforeLength, afterLength);
        }

    }

}
