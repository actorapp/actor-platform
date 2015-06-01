/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.model.entity.content;

import java.io.IOException;

import im.actor.model.api.DocumentExPhoto;
import im.actor.model.api.DocumentExVideo;
import im.actor.model.api.DocumentMessage;
import im.actor.model.api.JsonMessage;
import im.actor.model.api.Message;
import im.actor.model.api.ServiceEx;
import im.actor.model.api.ServiceExChangedAvatar;
import im.actor.model.api.ServiceExChangedTitle;
import im.actor.model.api.ServiceExContactRegistered;
import im.actor.model.api.ServiceExGroupCreated;
import im.actor.model.api.ServiceExUserAdded;
import im.actor.model.api.ServiceExUserJoined;
import im.actor.model.api.ServiceExUserKicked;
import im.actor.model.api.ServiceExUserLeft;
import im.actor.model.api.ServiceMessage;
import im.actor.model.api.TextMessage;
import im.actor.model.droidkit.bser.BserParser;
import im.actor.model.droidkit.bser.BserValues;
import im.actor.model.droidkit.bser.BserWriter;
import im.actor.model.droidkit.bser.DataInput;
import im.actor.model.droidkit.bser.DataOutput;
import im.actor.model.droidkit.json.JSONObject;
import im.actor.model.entity.compat.content.ObsoleteContent;
import im.actor.model.entity.content.internal.AbsContentContainer;
import im.actor.model.entity.content.internal.AbsLocalContent;
import im.actor.model.entity.content.internal.ContentLocalContainer;
import im.actor.model.entity.content.internal.ContentRemoteContainer;
import im.actor.model.entity.content.internal.LocalDocument;
import im.actor.model.entity.content.internal.LocalPhoto;
import im.actor.model.entity.content.internal.LocalVideo;

public abstract class AbsContent {

    public static byte[] serialize(AbsContent content) throws IOException {
        DataOutput dataOutput = new DataOutput();
        BserWriter writer = new BserWriter(dataOutput);
        // Mark new layout
        writer.writeBool(32, true);
        // Write content
        writer.writeBytes(33, content.getContentContainer().buildContainer());
        return dataOutput.toByteArray();
    }

    public static AbsContent fromMessage(Message message) throws IOException {
        return convertData(new ContentRemoteContainer(message));
    }

    public static AbsContent parse(byte[] data) throws IOException {
        BserValues reader = new BserValues(BserParser.deserialize(new DataInput(data)));
        AbsContentContainer container;
        // Is New Layout
        if (reader.getBool(32, false)) {
            container = AbsContentContainer.loadContainer(reader.getBytes(33));
        } else {
            container = ObsoleteContent.contentFromValues(reader);
        }
        return convertData(container);
    }

    protected static AbsContent convertData(AbsContentContainer container) throws IOException {
        if (container instanceof ContentLocalContainer) {
            ContentLocalContainer localContainer = (ContentLocalContainer) container;
            AbsLocalContent content = ((ContentLocalContainer) container).getContent();
            if (content instanceof LocalPhoto) {
                return new PhotoContent(localContainer);
            } else if (content instanceof LocalVideo) {
                return new VideoContent(localContainer);
            } else if (content instanceof LocalDocument) {
                return new DocumentContent(localContainer);
            } else {
                throw new IOException("Unknown type");
            }
        } else if (container instanceof ContentRemoteContainer) {
            ContentRemoteContainer remoteContainer = (ContentRemoteContainer) container;
            Message content = ((ContentRemoteContainer) container).getMessage();
            try {
                if (content instanceof DocumentMessage) {
                    DocumentMessage d = (DocumentMessage) content;
                    if (d.getExt() instanceof DocumentExPhoto) {
                        return new PhotoContent(remoteContainer);
                    } else if (d.getExt() instanceof DocumentExVideo) {
                        return new VideoContent(remoteContainer);
                    } else {
                        return new DocumentContent(remoteContainer);
                    }
                } else if (content instanceof TextMessage) {
                    return new TextContent(remoteContainer);
                } else if (content instanceof ServiceMessage) {
                    ServiceEx ext = ((ServiceMessage) content).getExt();
                    if (ext instanceof ServiceExContactRegistered) {
                        return new ServiceUserRegistered(remoteContainer);
                    } else if (ext instanceof ServiceExChangedTitle) {
                        return new ServiceGroupTitleChanged(remoteContainer);
                    } else if (ext instanceof ServiceExChangedAvatar) {
                        return new ServiceGroupAvatarChanged(remoteContainer);
                    } else if (ext instanceof ServiceExGroupCreated) {
                        return new ServiceGroupCreated(remoteContainer);
                    } else if (ext instanceof ServiceExUserAdded) {
                        return new ServiceGroupUserAdded(remoteContainer);
                    } else if (ext instanceof ServiceExUserKicked) {
                        return new ServiceGroupUserKicked(remoteContainer);
                    } else if (ext instanceof ServiceExUserLeft) {
                        return new ServiceGroupUserLeave(remoteContainer);
                    } else if (ext instanceof ServiceExUserJoined) {
                        return new ServiceGroupUserJoined(remoteContainer);
                    } else {
                        return new ServiceContent(remoteContainer);
                    }
                } else if (content instanceof JsonMessage) {
                    JsonMessage json = (JsonMessage) content;
                    JSONObject object = new JSONObject(json.getRawJson());
                    if (object.getString("dataType").equals("banner")) {
                        return new BannerContent(remoteContainer);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            // Fallback
            return new UnsupportedContent(remoteContainer);
        } else {
            throw new IOException("Unknown type");
        }
    }

    private AbsContentContainer contentContainer;

    public AbsContent(ContentRemoteContainer contentContainer) {
        this.contentContainer = contentContainer;
    }

    public AbsContent(ContentLocalContainer contentContainer) {
        this.contentContainer = contentContainer;
    }

    public AbsContentContainer getContentContainer() {
        return contentContainer;
    }
}