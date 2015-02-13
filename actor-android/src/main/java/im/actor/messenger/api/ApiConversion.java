package im.actor.messenger.api;

import java.util.ArrayList;
import java.util.List;

import im.actor.api.scheme.Group;
import im.actor.api.scheme.Member;
import im.actor.messenger.storage.scheme.FileLocation;
import im.actor.messenger.storage.scheme.avatar.Avatar;
import im.actor.messenger.storage.scheme.avatar.AvatarImage;
import im.actor.messenger.storage.scheme.groups.GroupInfo;
import im.actor.messenger.storage.scheme.groups.GroupMember;
import im.actor.messenger.storage.scheme.groups.GroupState;
import im.actor.messenger.storage.scheme.messages.PendingUpload;
import im.actor.messenger.storage.scheme.users.Sex;
import im.actor.messenger.storage.scheme.users.User;

public class ApiConversion {

    public static GroupInfo convert(Group group) {
        return new GroupInfo(group.getId(), group.getAccessHash(), group.getTitle(),
                convert(group.getAvatar()), convert(group.getMembers()), group.getAdminUid(),
                group.isMember() ? GroupState.JOINED : GroupState.KICKED);
    }

    public static List<GroupMember> convert(List<Member> members) {
        ArrayList<GroupMember> res = new ArrayList<GroupMember>();
        for (Member m : members) {
            res.add(new GroupMember(m.getUid(), m.getInviterUid(), m.getDate()));
        }
        return res;
    }

    public static User convert(im.actor.api.scheme.User u) {
        return new User(u.getId(), u.getAccessHash(), u.getName(), u.getLocalName(),
                ApiConversion.convert(u.getSex()),
                u.getKeyHashes(), u.getPhone(), ApiConversion.convert(u.getAvatar()));
    }

    public static Sex convert(im.actor.api.scheme.Sex sex) {
        if (sex == null) {
            return Sex.UNKNOWN;
        }
        switch (sex) {
            case FEMALE:
                return Sex.FEMALE;
            case MALE:
                return Sex.MALE;
            default:
            case UNKNOWN:
                return Sex.UNKNOWN;
        }
    }

    public static AvatarImage convert(im.actor.api.scheme.AvatarImage img) {
        if (img == null) {
            return null;
        }
        if (img.getFileLocation().getFileId() == 0) {
            return null;
        }
        return new AvatarImage(new FileLocation(
                img.getFileLocation().getFileId(),
                img.getFileLocation().getAccessHash(),
                img.getFileSize()),
                img.getWidth(), img.getHeight());
    }

    public static Avatar convert(im.actor.api.scheme.Avatar avatar) {

        if (avatar == null) {
            return null;
        }

        AvatarImage smallImage = convert(avatar.getSmallImage());
        AvatarImage largeImage = convert(avatar.getLargeImage());
        AvatarImage fullImage = convert(avatar.getFullImage());

        if (smallImage == null) {
            return null;
        }

        return new Avatar(smallImage, largeImage, fullImage);
    }
}
