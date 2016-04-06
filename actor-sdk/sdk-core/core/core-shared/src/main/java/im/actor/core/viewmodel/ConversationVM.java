package im.actor.core.viewmodel;

import im.actor.core.entity.ConversationState;
import im.actor.core.entity.Peer;
import im.actor.core.modules.ModuleContext;
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
    private ValueModel<Long> ownReadDate;

    public ConversationVM(ConversationState rawObj) {
        super(rawObj);

        isLoaded = new BooleanValueModel("chat.is_loaded." + rawObj.getPeer(), rawObj.isLoaded());
        ownReadDate = new ValueModel<>("chat.read_date" + rawObj.getPeer(), rawObj.getInReadDate());
    }

    public BooleanValueModel getIsLoaded() {
        return isLoaded;
    }

    public ValueModel<Long> getOwnReadDate() {
        return ownReadDate;
    }

    @Override
    protected void updateValues(ConversationState rawObj) {
        isLoaded.change(rawObj.isLoaded());
        ownReadDate.change(rawObj.getInReadDate());
    }
}