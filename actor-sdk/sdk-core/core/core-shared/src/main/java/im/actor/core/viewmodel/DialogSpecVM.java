package im.actor.core.viewmodel;

import com.google.j2objc.annotations.Property;

import im.actor.core.entity.DialogSpec;
import im.actor.core.entity.Peer;
import im.actor.core.viewmodel.generics.BooleanValueModel;
import im.actor.core.viewmodel.generics.IntValueModel;
import im.actor.runtime.mvvm.BaseValueModel;
import im.actor.runtime.mvvm.ValueModelCreator;

public class DialogSpecVM extends BaseValueModel<DialogSpec> {

    public static ValueModelCreator<DialogSpec, DialogSpecVM> CREATOR = new ValueModelCreator<DialogSpec, DialogSpecVM>() {
        @Override
        public DialogSpecVM create(DialogSpec baseValue) {
            return new DialogSpecVM(baseValue);
        }
    };

    @Property("readonly, nonatomic")
    private final Peer peer;
    @Property("readonly, nonatomic")
    private IntValueModel counter;
    @Property("readonly, nonatomic")
    private BooleanValueModel isUnread;

    public DialogSpecVM(DialogSpec rawObj) {
        super(rawObj);

        this.peer = rawObj.getPeer();
        this.counter = new IntValueModel("dialogs.desc.counter", rawObj.getCounter());
        this.isUnread = new BooleanValueModel("dialogs.desc.unread", rawObj.isUnread());
    }

    public Peer getPeer() {
        return peer;
    }

    public IntValueModel getCounter() {
        return counter;
    }

    public BooleanValueModel getIsUnread() {
        return isUnread;
    }

    @Override
    protected void updateValues(DialogSpec rawObj) {
        counter.change(rawObj.getCounter());
        isUnread.change(rawObj.isUnread());
    }
}
