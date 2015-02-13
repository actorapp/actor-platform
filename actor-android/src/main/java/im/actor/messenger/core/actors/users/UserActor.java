package im.actor.messenger.core.actors.users;

import com.droidkit.actors.ActorRef;
import com.droidkit.actors.concurrency.Future;
import com.droidkit.actors.typed.TypedActor;

import im.actor.messenger.api.ApiConversion;
import im.actor.messenger.core.actors.base.TypedActorHolder;
import im.actor.messenger.core.actors.chat.ConversationActor;
import im.actor.messenger.core.actors.chat.DialogsActor;
import im.actor.messenger.core.actors.contacts.ContactsActor;
import im.actor.messenger.model.DialogType;
import im.actor.messenger.model.UserModel;
import im.actor.messenger.storage.scheme.messages.types.UserAddedDeviceMessage;
import im.actor.messenger.storage.scheme.messages.types.UserRegisteredMessage;
import im.actor.messenger.storage.scheme.users.PublicKey;
import im.actor.messenger.storage.scheme.avatar.Avatar;
import im.actor.messenger.storage.scheme.users.User;
import im.actor.messenger.util.RandomUtil;

import java.util.List;

import static im.actor.messenger.core.Core.myUid;
import static im.actor.messenger.storage.KeyValueEngines.publicKeys;
import static im.actor.messenger.storage.KeyValueEngines.users;

/**
 * Created by ex3ndr on 15.09.14.
 */
public class UserActor extends TypedActor<UserInt> implements UserInt {

    private static TypedActorHolder<UserInt> holder = new TypedActorHolder<UserInt>(
            UserInt.class, UserActor.class, "updates", "sequence/users");

    public static ActorRef userActorRef() {
        return holder.getRef();
    }

    public static UserInt userActor() {
        return holder.get();
    }

    public UserActor() {
        super(UserInt.class);
    }

    @Override
    public Future<Boolean> onUpdateUsers(List<im.actor.api.scheme.User> users) {
        for (im.actor.api.scheme.User u : users) {
            UserModel model = users().get(u.getId());
            User user;
            if (model == null) {
                user = ApiConversion.convert(u);
            } else {
                user = model.getRaw();

                String nName = User.name(u.getLocalName(), u.getName());
                if (!model.getName().equals(nName)) {
                    DialogsActor.dialogs().onUserChangedName(u.getId(), nName);
                    ContactsActor.contactsList().onUserNameChanged(u.getId());
                }

                Avatar avatar = ApiConversion.convert(u.getAvatar());
                if (!areSame(user.getAvatar(), avatar)) {
                    DialogsActor.dialogs().onUserChangedAvatar(u.getId(), avatar);
                    ContactsActor.contactsList().onUserAvatarChanged(u.getId());
                }

                user = user.change(u.getName(), u.getLocalName(), avatar, ApiConversion.convert(u.getSex()), u.getKeyHashes());
            }

            users().put(user.getId(), user);
        }
        return result(true);
    }

    @Override
    public void onLocalNameChanged(int uid, String name) {
        if (name != null && name.trim().length() == 0) {
            name = null;
        }
        UserModel user = users().get(uid);
        if (user != null) {
            if (name == null) {
                if (user.getRaw().getLocalName() == null) {
                    return;
                }
                DialogsActor.dialogs().onUserChangedName(uid, user.getRaw().getServerName());
                ContactsActor.contactsList().onUserNameChanged(uid);
            } else {
                if (user.getRaw().getLocalName() != null && user.getRaw().getLocalName().equals(name)) {
                    return;
                }

                DialogsActor.dialogs().onUserChangedName(uid, name);
                ContactsActor.contactsList().onUserNameChanged(uid);
            }

            users().put(uid, user.getRaw().changeLocalName(name));
        }
    }

    @Override
    public void onAvatarChanged(int uid, im.actor.api.scheme.Avatar avatar) {
        Avatar ava = ApiConversion.convert(avatar);
        UserModel user = users().get(uid);
        if (user != null) {
            if (areSame(user.getRaw().getAvatar(), ava)) {
                return;
            }
            DialogsActor.dialogs().onUserChangedAvatar(uid, ava);
            ContactsActor.contactsList().onUserAvatarChanged(uid);

            users().put(uid, user.getRaw().changeAvatar(ava));
        }
    }

    @Override
    public void onServerNameChanged(int uid, String name) {
        UserModel user = users().get(uid);
        if (user != null) {
            if (!user.getRaw().getServerName().equals(name)) {
                users().put(uid, user.getRaw().changeServerName(name));
                if (user.getRaw().getLocalName() == null) {
                    DialogsActor.dialogs().onUserChangedName(uid, name);
                    ContactsActor.contactsList().onUserNameChanged(uid);
                }
            }
        }
    }

    @Override
    public void onDeviceRemoved(int uid, long keyHash) {
        UserModel user = users().get(uid);
        if (user != null) {
            users().put(uid, user.getRaw().removeKey(keyHash));
        }
    }

    @Override
    public void onDeviceAdded(int uid, long keyHash, byte[] key) {
        UserModel user = users().get(uid);
        if (user != null) {

            if (key != null) {
                publicKeys().putSync(new PublicKey(uid, keyHash, key));
            }

            User user2 = user.getRaw().addKey(keyHash);
            if (user2 != user.getRaw()) {
                users().put(uid, user.getRaw().addKey(keyHash));

                if (uid != myUid()) {
                    ConversationActor.conv(DialogType.TYPE_USER, uid).onInMessage(RandomUtil.randomId(),
                            uid, System.currentTimeMillis(), new UserAddedDeviceMessage());
                } else {
                    // TODO: Add to notification feed
                }
            }
        }
    }

    @Override
    public void onWrongKeys(List<im.actor.api.scheme.UserKey> added, List<im.actor.api.scheme.UserKey> invalid, List<im.actor.api.scheme.UserKey> removed) {
        for (im.actor.api.scheme.UserKey userKey : invalid) {
            onDeviceRemoved(userKey.getUid(), userKey.getKeyHash());
        }
        for (im.actor.api.scheme.UserKey userKey : removed) {
            onDeviceRemoved(userKey.getUid(), userKey.getKeyHash());
        }

        for (im.actor.api.scheme.UserKey userKey : added) {
            onDeviceAdded(userKey.getUid(), userKey.getKeyHash(), null);
        }
    }

    @Override
    public void onUserRegistered(int uid) {
        UserModel user = users().get(uid);
        if (user != null) {
            ConversationActor.conv(DialogType.TYPE_USER, uid).onInMessage(RandomUtil.randomId(),
                    uid, System.currentTimeMillis(), new UserRegisteredMessage());
        }
    }

    private static boolean isEmptyEq(Object a, Object b) {
        if (a == null && b == null) {
            return true;
        }
        if (a != null && b == null) {
            return false;
        }
        if (b != null && a == null) {
            return false;
        }

        return true;
    }

    private static boolean areSame(Avatar a, Avatar b) {
        if (a == b) {
            return true;
        }

        if (!isEmptyEq(a, b))
            return false;

        if (isEmptyEq(a.getSmallImage(), b.getSmallImage())) {
            return false;
        }

        if (a.getSmallImage() != null && b.getSmallImage() != null && a.getSmallImage().getFileLocation().getFileId() != b.getSmallImage().getFileLocation().getFileId()) {
            return false;
        }

        return true;
    }
}
