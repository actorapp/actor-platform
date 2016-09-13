package im.actor.core.viewmodel;

import im.actor.core.entity.ConversationState;
import im.actor.core.viewmodel.generics.BooleanValueModel;
import im.actor.runtime.mvvm.BaseValueModel;
import im.actor.runtime.mvvm.ValueModel;
import im.actor.runtime.mvvm.ValueModelCreator;

public class ConversationVM extends BaseValueModel<ConversationState> {

    public static ValueModelCreator<ConversationState, ConversationVM> CREATOR = new ValueModelCreator<ConversationState, ConversationVM>() {
        @Override
        public ConversationVM create(ConversationState baseValue) {
            return new ConversationVM(baseValue);
        }
    };

    private BooleanValueModel isLoaded;
    private BooleanValueModel isEmpty;
    private ValueModel<Long> ownReadDate;
    private ValueModel<Long> ownSendDate;

    private ValueModel<Long> readDate;
    private ValueModel<Long> receiveDate;

    public ConversationVM(ConversationState rawObj) {
        super(rawObj);

        isLoaded = new BooleanValueModel("chat.is_loaded." + rawObj.getPeer(), rawObj.isLoaded());
        isEmpty = new BooleanValueModel("chat.is_empty." + rawObj.getPeer(), rawObj.isEmpty());
        ownReadDate = new ValueModel<>("chat.own_read_date" + rawObj.getPeer(), rawObj.getInReadDate());
        ownSendDate = new ValueModel<>("chat.own_send_date" + rawObj.getPeer(), rawObj.getOutSendDate());
        readDate = new ValueModel<>("chat.read_date" + rawObj.getPeer(), rawObj.getOutReadDate());
        receiveDate = new ValueModel<>("chat.receive_date" + rawObj.getPeer(), rawObj.getOutReceiveDate());
    }

    public BooleanValueModel getIsLoaded() {
        return isLoaded;
    }

    public BooleanValueModel getIsEmpty() {
        return isEmpty;
    }

    public ValueModel<Long> getOwnReadDate() {
        return ownReadDate;
    }

    public ValueModel<Long> getOwnSendDate() {
        return ownSendDate;
    }

    public ValueModel<Long> getReadDate() {
        return readDate;
    }

    public ValueModel<Long> getReceiveDate() {
        return receiveDate;
    }

    public long getLastReadMessageDate() {
        return Math.max(ownReadDate.get(), ownSendDate.get());
    }

    @Override
    protected void updateValues(ConversationState rawObj) {
        isLoaded.change(rawObj.isLoaded());
        isEmpty.change(rawObj.isEmpty());
        ownReadDate.change(rawObj.getInReadDate());
        ownSendDate.change(rawObj.getOutSendDate());
        readDate.change(rawObj.getOutReadDate());
        receiveDate.change(rawObj.getOutReceiveDate());
    }
}