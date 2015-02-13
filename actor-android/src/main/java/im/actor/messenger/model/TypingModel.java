package im.actor.messenger.model;

import com.droidkit.mvvm.ValueModel;

import java.util.HashMap;

/**
 * Created by ex3ndr on 10.10.14.
 */
public class TypingModel {
    private static final HashMap<Integer, ValueModel<Boolean>> typingModels = new HashMap<Integer, ValueModel<Boolean>>();

    private static final HashMap<Integer, ValueModel<int[]>> groupTypingModels =
            new HashMap<Integer, ValueModel<int[]>>();

    public static ValueModel<Boolean> privateChatTyping(int uid) {
        synchronized (typingModels) {
            if (!typingModels.containsKey(uid)) {
                typingModels.put(uid, new ValueModel<Boolean>("typing." + uid, false));
            }
            return typingModels.get(uid);
        }
    }

    public static ValueModel<int[]> groupChatTyping(int chatId) {
        synchronized (groupTypingModels) {
            if (!groupTypingModels.containsKey(chatId)) {
                groupTypingModels.put(chatId, new ValueModel<int[]>("group_typing." + chatId, new int[0]));
            }
            return groupTypingModels.get(chatId);
        }
    }
}
