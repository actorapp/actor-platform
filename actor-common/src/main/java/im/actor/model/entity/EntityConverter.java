package im.actor.model.entity;

/**
 * Created by ex3ndr on 08.02.15.
 */
public class EntityConverter {

    public static Avatar convert(im.actor.model.api.Avatar avatar) {
        if (avatar == null) {
            return null;
        }
        return new Avatar(convert(avatar.getSmallImage()),
                convert(avatar.getLargeImage()),
                convert(avatar.getFullImage()));
    }

    public static AvatarImage convert(im.actor.model.api.AvatarImage avatarImage) {
        if (avatarImage == null) {
            return null;
        }
        return new AvatarImage(avatarImage.getWidth(), avatarImage.getHeight(),
                convert(avatarImage.getFileLocation(), avatarImage.getFileSize()));
    }

    public static FileLocation convert(im.actor.model.api.FileLocation location, int size) {
        return new FileLocation(location.getFileId(), location.getAccessHash(), size);
    }

    public static User convert(im.actor.model.api.User user) {
        return new User(user.getId(), user.getAccessHash(), user.getName(), user.getLocalName(),
                convert(user.getAvatar()));
    }

    public static PeerType convert(im.actor.model.api.PeerType peerType) {
        switch (peerType) {
            case EMAIL:
                return PeerType.EMAIL;
            case GROUP:
                return PeerType.GROUP;
            default:
            case PRIVATE:
                return PeerType.PRIVATE;
        }
    }

    public static Peer convert(im.actor.model.api.Peer peer) {
        return new Peer(convert(peer.getType()), peer.getId());
    }
}
