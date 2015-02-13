package im.actor.messenger.core.actors.profile;

import com.droidkit.actors.concurrency.Future;
import com.droidkit.actors.concurrency.FutureCallback;
import com.droidkit.actors.typed.TypedActor;
import com.droidkit.actors.typed.TypedFuture;

import im.actor.api.scheme.base.SeqUpdate;
import im.actor.api.scheme.rpc.ResponseSeq;
import im.actor.api.scheme.updates.UpdateUserLocalNameChanged;
import im.actor.api.scheme.updates.UpdateUserNameChanged;
import im.actor.messenger.core.actors.api.SequenceActor;
import im.actor.messenger.core.actors.base.TypedActorHolder;
import im.actor.messenger.model.UserModel;

import static im.actor.messenger.core.Core.myUid;
import static im.actor.messenger.core.Core.requests;
import static im.actor.messenger.storage.KeyValueEngines.users;

/**
 * Created by ex3ndr on 25.10.14.
 */
public class EditNameActor extends TypedActor<EditNameInt> implements EditNameInt {

    private static TypedActorHolder<EditNameInt> HOLDER = new TypedActorHolder<EditNameInt>(EditNameInt.class,
            EditNameActor.class, "edit_name");

    public static EditNameInt editName() {
        return HOLDER.get();
    }

    public EditNameActor() {
        super(EditNameInt.class);
    }

    @Override
    public Future<Boolean> editMyName(final String newName) {
        final TypedFuture<Boolean> res = future();
        ask(requests().editName(newName, 15000), new FutureCallback<ResponseSeq>() {
            @Override
            public void onResult(ResponseSeq result) {
                system().actorOf(SequenceActor.sequence())
                        .send(new SeqUpdate(result.getSeq(), result.getState(),
                                UpdateUserLocalNameChanged.HEADER,
                                new UpdateUserNameChanged(myUid(), newName)
                                        .toByteArray()));

                res.doComplete(true);
            }

            @Override
            public void onError(Throwable throwable) {
                res.doError(throwable);
            }
        });
        return res;
    }

    @Override
    public Future<Boolean> editName(final int uid, final String newName) {
        UserModel userModel = users().get(uid);
        final TypedFuture<Boolean> res = future();
        ask(requests().editUserLocalName(uid, userModel.getAccessHash(), newName, 15000), new FutureCallback<ResponseSeq>() {
            @Override
            public void onResult(ResponseSeq result) {
                system().actorOf(SequenceActor.sequence())
                        .send(new SeqUpdate(result.getSeq(), result.getState(),
                                UpdateUserLocalNameChanged.HEADER,
                                new UpdateUserLocalNameChanged(uid, newName)
                                        .toByteArray()));

                res.doComplete(true);
            }

            @Override
            public void onError(Throwable throwable) {
                res.doError(throwable);
            }
        });
        return res;
    }
}
