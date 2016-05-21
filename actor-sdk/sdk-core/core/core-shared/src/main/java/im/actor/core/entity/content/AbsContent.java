/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.entity.content;

import java.io.IOException;

import im.actor.core.api.ApiDocumentExAnimation;
import im.actor.core.api.ApiDocumentExPhoto;
import im.actor.core.api.ApiDocumentExVideo;
import im.actor.core.api.ApiDocumentExVoice;
import im.actor.core.api.ApiDocumentMessage;
import im.actor.core.api.ApiJsonMessage;
import im.actor.core.api.ApiMessage;
import im.actor.core.api.ApiServiceEx;
import im.actor.core.api.ApiServiceExChangedAvatar;
import im.actor.core.api.ApiServiceExChangedTitle;
import im.actor.core.api.ApiServiceExChangedTopic;
import im.actor.core.api.ApiServiceExChangedAbout;
import im.actor.core.api.ApiServiceExContactRegistered;
import im.actor.core.api.ApiServiceExGroupCreated;
import im.actor.core.api.ApiServiceExPhoneCall;
import im.actor.core.api.ApiServiceExPhoneMissed;
import im.actor.core.api.ApiServiceExUserInvited;
import im.actor.core.api.ApiServiceExUserJoined;
import im.actor.core.api.ApiServiceExUserKicked;
import im.actor.core.api.ApiServiceExUserLeft;
import im.actor.core.api.ApiServiceMessage;
import im.actor.core.api.ApiStickerMessage;
import im.actor.core.api.ApiTextMessage;
import im.actor.core.entity.content.internal.AbsContentContainer;
import im.actor.core.entity.content.internal.AbsLocalContent;
import im.actor.core.entity.content.internal.ContentLocalContainer;
import im.actor.core.entity.content.internal.ContentRemoteContainer;
import im.actor.core.entity.content.internal.LocalAnimation;
import im.actor.core.entity.content.internal.LocalDocument;
import im.actor.core.entity.content.internal.LocalPhoto;
import im.actor.core.entity.content.internal.LocalVideo;
import im.actor.core.entity.content.internal.LocalVoice;
import im.actor.runtime.bser.BserParser;
import im.actor.runtime.bser.BserValues;
import im.actor.runtime.bser.BserWriter;
import im.actor.runtime.bser.DataInput;
import im.actor.runtime.bser.DataOutput;
import im.actor.runtime.json.JSONObject;

// Disabling Bounds checks for speeding up calculations

/*-[
#define J2OBJC_DISABLE_ARRAY_BOUND_CHECKS 1
]-*/

public abstract class AbsContent {

    int updatedCounter = 0;

    public static byte[] serialize(AbsContent content) throws IOException {
        DataOutput dataOutput = new DataOutput();
        BserWriter writer = new BserWriter(dataOutput);
        // Mark new layout
        writer.writeBool(32, true);
        // Write content
        writer.writeBytes(33, content.getContentContainer().buildContainer());
        return dataOutput.toByteArray();
    }

    public static AbsContent fromMessage(ApiMessage message) {
        return convertData(new ContentRemoteContainer(message));
    }

    public static AbsContent parse(byte[] data) throws IOException {
        BserValues reader = new BserValues(BserParser.deserialize(new DataInput(data)));
        AbsContentContainer container;
        // Is New Layout
        if (reader.getBool(32, false)) {
            container = AbsContentContainer.loadContainer(reader.getBytes(33));
        } else {
            throw new RuntimeException("Unsupported obsolete format");
        }
        return convertData(container);
    }

    protected static AbsContent convertData(AbsContentContainer container) {

        if (container instanceof ContentLocalContainer) {
            ContentLocalContainer localContainer = (ContentLocalContainer) container;
            AbsLocalContent content = ((ContentLocalContainer) container).getContent();
            if (content instanceof LocalPhoto) {
                return new PhotoContent(localContainer);
            } else if (content instanceof LocalVideo) {
                return new VideoContent(localContainer);
            } else if (content instanceof LocalVoice) {
                return new VoiceContent(localContainer);
            } else if (content instanceof LocalAnimation) {
                return new AnimationContent(localContainer);
            } else if (content instanceof LocalDocument) {
                return new DocumentContent(localContainer);
            } else {
                throw new RuntimeException("Unknown type");
            }
        } else if (container instanceof ContentRemoteContainer) {
            ContentRemoteContainer remoteContainer = (ContentRemoteContainer) container;
            ApiMessage content = ((ContentRemoteContainer) container).getMessage();
            try {
                if (content instanceof ApiDocumentMessage) {
                    ApiDocumentMessage d = (ApiDocumentMessage) content;
                    if (d.getExt() instanceof ApiDocumentExPhoto) {
                        return new PhotoContent(remoteContainer);
                    } else if (d.getExt() instanceof ApiDocumentExVideo) {
                        return new VideoContent(remoteContainer);
                    } else if (d.getExt() instanceof ApiDocumentExVoice) {
                        return new VoiceContent(remoteContainer);
                    } else if (d.getExt() instanceof ApiDocumentExAnimation) {
                        return new AnimationContent(remoteContainer);
                    } else {
                        return new DocumentContent(remoteContainer);
                    }
                } else if (content instanceof ApiTextMessage) {
                    return new TextContent(remoteContainer);
                } else if (content instanceof ApiServiceMessage) {
                    ApiServiceEx ext = ((ApiServiceMessage) content).getExt();
                    if (ext instanceof ApiServiceExContactRegistered) {
                        return new ServiceUserRegistered(remoteContainer);
                    } else if (ext instanceof ApiServiceExChangedTitle) {
                        return new ServiceGroupTitleChanged(remoteContainer);
                    } else if (ext instanceof ApiServiceExChangedTopic) {
                        return new ServiceGroupTopicChanged(remoteContainer);
                    } else if (ext instanceof ApiServiceExChangedAbout) {
                        return new ServiceGroupAboutChanged(remoteContainer);
                    } else if (ext instanceof ApiServiceExChangedAvatar) {
                        return new ServiceGroupAvatarChanged(remoteContainer);
                    } else if (ext instanceof ApiServiceExGroupCreated) {
                        return new ServiceGroupCreated(remoteContainer);
                    } else if (ext instanceof ApiServiceExUserInvited) {
                        return new ServiceGroupUserInvited(remoteContainer);
                    } else if (ext instanceof ApiServiceExUserKicked) {
                        return new ServiceGroupUserKicked(remoteContainer);
                    } else if (ext instanceof ApiServiceExUserLeft) {
                        return new ServiceGroupUserLeave(remoteContainer);
                    } else if (ext instanceof ApiServiceExUserJoined) {
                        return new ServiceGroupUserJoined(remoteContainer);
                    } else if (ext instanceof ApiServiceExPhoneCall) {
                        return new ServiceCallEnded(remoteContainer);
                    } else if (ext instanceof ApiServiceExPhoneMissed) {
                        return new ServiceCallMissed(remoteContainer);
                    } else {
                        return new ServiceContent(remoteContainer);
                    }
                } else if (content instanceof ApiJsonMessage) {
                    ApiJsonMessage json = (ApiJsonMessage) content;
                    JSONObject object = new JSONObject(json.getRawJson());
                    if (object.getString("dataType").equals("contact")) {
                        return new ContactContent(remoteContainer);
                    } else if (object.getString("dataType").equals("location")) {
                        return new LocationContent(remoteContainer);
                    } else {
                        return new JsonContent(remoteContainer);
                    }
                } else if (content instanceof ApiStickerMessage) {
                    return new StickerContent(remoteContainer);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            // Fallback
            return new UnsupportedContent(remoteContainer);
        } else {
            throw new RuntimeException("Unknown type");
        }
    }

    private AbsContentContainer contentContainer;

    public AbsContent() {
        super();
    }

    public AbsContent(ContentRemoteContainer contentContainer) {
        this.contentContainer = contentContainer;
    }

    public AbsContent(ContentLocalContainer contentContainer) {
        this.contentContainer = contentContainer;
    }

    public AbsContentContainer getContentContainer() {
        return contentContainer;
    }

    protected void setContentContainer(AbsContentContainer contentContainer) {
        this.contentContainer = contentContainer;
    }

    public int getUpdatedCounter() {
        return updatedCounter;
    }

    public AbsContent incrementUpdatedCounter(int oldCounter) {
        updatedCounter = ++oldCounter;
        return this;
    }
}