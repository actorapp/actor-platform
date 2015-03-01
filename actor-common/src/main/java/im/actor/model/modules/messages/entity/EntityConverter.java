package im.actor.model.modules.messages.entity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import im.actor.model.api.FileExPhoto;
import im.actor.model.api.FileExVideo;
import im.actor.model.api.Member;
import im.actor.model.api.ServiceExChangedAvatar;
import im.actor.model.api.ServiceExChangedTitle;
import im.actor.model.api.ServiceExUserAdded;
import im.actor.model.api.ServiceExUserKicked;
import im.actor.model.api.ServiceMessage;
import im.actor.model.droidkit.bser.Bser;
import im.actor.model.entity.Avatar;
import im.actor.model.entity.AvatarImage;
import im.actor.model.entity.ContactRecord;
import im.actor.model.entity.FileLocation;
import im.actor.model.entity.Group;
import im.actor.model.entity.GroupMember;
import im.actor.model.entity.MessageState;
import im.actor.model.entity.Peer;
import im.actor.model.entity.PeerType;
import im.actor.model.entity.Sex;
import im.actor.model.entity.User;
import im.actor.model.entity.content.AbsContent;
import im.actor.model.entity.content.DocumentContent;
import im.actor.model.entity.content.FastThumb;
import im.actor.model.entity.content.FileRemoteSource;
import im.actor.model.entity.content.PhotoContent;
import im.actor.model.entity.content.ServiceContent;
import im.actor.model.entity.content.ServiceGroupAvatarChanged;
import im.actor.model.entity.content.ServiceGroupCreated;
import im.actor.model.entity.content.ServiceGroupTitleChanged;
import im.actor.model.entity.content.ServiceGroupUserAdded;
import im.actor.model.entity.content.ServiceGroupUserKicked;
import im.actor.model.entity.content.ServiceGroupUserLeave;
import im.actor.model.entity.content.TextContent;
import im.actor.model.entity.content.VideoContent;

/**
 * Created by ex3ndr on 08.02.15.
 */
public class EntityConverter {

    public static MessageState convert(im.actor.model.api.MessageState state) {
        if (state == null) {
            return MessageState.UNKNOWN;
        }
        switch (state) {
            case READ:
                return MessageState.READ;
            case RECEIVED:
                return MessageState.RECEIVED;
            case SENT:
                return MessageState.SENT;
            default:
                return MessageState.UNKNOWN;
        }
    }

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
                convert(avatarImage.getFileLocation(), "avatar.jpg", avatarImage.getFileSize()));
    }

    public static FileLocation convert(im.actor.model.api.FileLocation location, String fileName, int size) {
        return new FileLocation(location.getFileId(), location.getAccessHash(), size, fileName);
    }

    public static Sex convert(im.actor.model.api.Sex sex) {
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


    public static User convert(im.actor.model.api.User user) {
        ArrayList<ContactRecord> res = new ArrayList<ContactRecord>();
        res.add(new ContactRecord(0, 0, ContactRecord.TYPE_PHONE, "" + user.getPhone(), "Mobile"));
        return new User(user.getId(), user.getAccessHash(), user.getName(), user.getLocalName(),
                convert(user.getAvatar()), convert(user.getSex()),
                res);
    }

    public static Group convert(im.actor.model.api.Group group) {
        return new Group(group.getId(), group.getAccessHash(), group.getTitle(), convert(group.getAvatar()),
                convert(group.getMembers(), group.getCreatorUid()), group.getCreatorUid(), group.isMember());
    }

    public static ArrayList<GroupMember> convert(List<Member> members, int admin) {
        ArrayList<GroupMember> res = new ArrayList<GroupMember>();
        for (Member m : members) {
            res.add(new GroupMember(m.getUid(), m.getInviterUid(), m.getDate(), m.getUid() == admin));
        }
        return res;
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

    public static AbsContent convert(im.actor.model.api.MessageContent content) {
        try {
            if (content.getType() == 0x01) {
                im.actor.model.api.TextMessage textMessage = Bser.parse(new im.actor.model.api.TextMessage(),
                        content.getContent());
                return new TextContent(textMessage.getText());
            } else if (content.getType() == 0x02) {
                ServiceMessage serviceMessage = Bser.parse(new ServiceMessage(), content.getContent());
                if (serviceMessage.getExtType() == 0x04) {
                    // TODO: pass title
                    return new ServiceGroupCreated("???");
                } else if (serviceMessage.getExtType() == 0x05) {
                    ServiceExChangedTitle title = Bser.parse(new ServiceExChangedTitle(), serviceMessage.getExt());
                    return new ServiceGroupTitleChanged(title.getTitle());
                } else if (serviceMessage.getExtType() == 0x06) {
                    ServiceExChangedAvatar title = Bser.parse(new ServiceExChangedAvatar(), serviceMessage.getExt());
                    return new ServiceGroupAvatarChanged(convert(title.getAvatar()));
                } else if (serviceMessage.getExtType() == 0x01) {
                    ServiceExUserAdded added = Bser.parse(new ServiceExUserAdded(), serviceMessage.getExt());
                    return new ServiceGroupUserAdded(added.getAddedUid());
                } else if (serviceMessage.getExtType() == 0x02) {
                    ServiceExUserKicked added = Bser.parse(new ServiceExUserKicked(), serviceMessage.getExt());
                    return new ServiceGroupUserKicked(added.getKickedUid());
                } else if (serviceMessage.getExtType() == 0x03) {
                    return new ServiceGroupUserLeave();
                } else {
                    return new ServiceContent(serviceMessage.getText());
                }
            } else if (content.getType() == 0x03) {
                im.actor.model.api.FileMessage fileMessage = Bser.parse(new im.actor.model.api.FileMessage(),
                        content.getContent());
                String mimeType = fileMessage.getMimeType();
                String name = fileMessage.getName();
                im.actor.model.entity.content.FastThumb fastThumb = convert(fileMessage.getThumb());
                FileLocation fileLocation = new FileLocation(fileMessage.getFileId(),
                        fileMessage.getAccessHash(),
                        fileMessage.getFileSize(),
                        fileMessage.getName());
                FileRemoteSource source = new FileRemoteSource(fileLocation);

                if (fileMessage.getExtType() == 0x01) {
                    try {
                        FileExPhoto photo = Bser.parse(new FileExPhoto(), fileMessage.getExt());
                        return new PhotoContent(source, mimeType, name, fastThumb, photo.getW(), photo.getH());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else if (fileMessage.getExtType() == 0x02) {
                    try {
                        FileExVideo video = Bser.parse(new FileExVideo(), fileMessage.getExt());
                        return new VideoContent(source, mimeType, name, fastThumb,
                                video.getDuration(), video.getW(), video.getH());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                return new DocumentContent(source, mimeType, name, fastThumb);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static FastThumb convert(im.actor.model.api.FastThumb fastThumb) {
        if (fastThumb == null) {
            return null;
        }
        return new FastThumb(fastThumb.getW(), fastThumb.getH(), fastThumb.getThumb());
    }
}
