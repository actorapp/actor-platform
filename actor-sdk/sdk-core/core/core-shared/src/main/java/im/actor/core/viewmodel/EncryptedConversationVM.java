package im.actor.core.viewmodel;

import im.actor.core.entity.EncryptedConversationState;
import im.actor.core.viewmodel.generics.IntValueModel;
import im.actor.runtime.mvvm.BaseValueModel;
import im.actor.runtime.mvvm.ValueModelCreator;

public class EncryptedConversationVM extends BaseValueModel<EncryptedConversationState> {

    public static ValueModelCreator<EncryptedConversationState, EncryptedConversationVM> CREATOR =
            baseValue -> new EncryptedConversationVM(baseValue);

    private int uid;
    private IntValueModel timer;

    public EncryptedConversationVM(EncryptedConversationState rawObj) {
        super(rawObj);
        uid = rawObj.getUid();
        timer = new IntValueModel("encrypted_" + uid + ".timer", rawObj.getTimer());
    }

    public int getUid() {
        return uid;
    }

    public IntValueModel getTimer() {
        return timer;
    }

    @Override
    protected void updateValues(EncryptedConversationState rawObj) {
        timer.change(rawObj.getTimer());
    }
}
