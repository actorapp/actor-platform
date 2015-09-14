/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.entity.content;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;

import im.actor.core.api.ApiDocumentExPhoto;
import im.actor.core.api.ApiDocumentExVideo;
import im.actor.core.api.ApiDocumentMessage;
import im.actor.core.api.ApiJsonMessage;
import im.actor.core.api.ApiMessage;
import im.actor.core.api.ApiServiceEx;
import im.actor.core.api.ApiServiceExChangedAvatar;
import im.actor.core.api.ApiServiceExChangedTitle;
import im.actor.core.api.ApiServiceExContactRegistered;
import im.actor.core.api.ApiServiceExGroupCreated;
import im.actor.core.api.ApiServiceExUserInvited;
import im.actor.core.api.ApiServiceExUserJoined;
import im.actor.core.api.ApiServiceExUserKicked;
import im.actor.core.api.ApiServiceExUserLeft;
import im.actor.core.api.ApiServiceMessage;
import im.actor.core.api.ApiTextMessage;
import im.actor.core.entity.content.internal.AbsContentContainer;
import im.actor.core.entity.content.internal.AbsLocalContent;
import im.actor.core.entity.content.internal.ContentLocalContainer;
import im.actor.core.entity.content.internal.ContentRemoteContainer;
import im.actor.core.entity.content.internal.LocalDocument;
import im.actor.core.entity.content.internal.LocalPhoto;
import im.actor.core.entity.content.internal.LocalVideo;
import im.actor.runtime.bser.BserParser;
import im.actor.runtime.bser.BserValues;
import im.actor.runtime.bser.BserWriter;
import im.actor.runtime.bser.DataInput;
import im.actor.runtime.bser.DataOutput;
import im.actor.runtime.json.JSONObject;

public abstract class AbsContent {

    private static ContentConverter[] converters = new ContentConverter[0];

    public static void registerConverter(ContentConverter contentConverter) {
        ContentConverter[] nConverters = new ContentConverter[converters.length + 1];
        for (int i = 0; i < converters.length; i++) {
            nConverters[i] = converters[i];
        }
        nConverters[nConverters.length - 1] = contentConverter;
        converters = nConverters;
    }

    public static byte[] serialize(AbsContent content) throws IOException {
        DataOutput dataOutput = new DataOutput();
        BserWriter writer = new BserWriter(dataOutput);
        // Mark new layout
        writer.writeBool(32, true);
        // Write content
        writer.writeBytes(33, content.getContentContainer().buildContainer());
        return dataOutput.toByteArray();
    }

    public static AbsContent fromMessage(ApiMessage message) throws IOException {
        return convertData(new ContentRemoteContainer(message));
    }

    public static AbsContent parse(byte[] data) throws IOException {
        BserValues reader = new BserValues(BserParser.deserialize(new DataInput(data)));
        AbsContentContainer container;
        // Is New Layout
        if (reader.getBool(32, false)) {
            container = AbsContentContainer.loadContainer(reader.getBytes(33));
        } else {
            throw new IOException("Unsupported obsolete format");
        }
        return convertData(container);
    }

    protected static AbsContent convertData(AbsContentContainer container) throws IOException {

        // Processing extension converters
        for (ContentConverter converter : converters) {
            try {
                AbsContent res = converter.convert(container);
                if (res != null) {
                    return res;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

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
            ApiMessage content = ((ContentRemoteContainer) container).getMessage();
            try {
                if (content instanceof ApiDocumentMessage) {
                    ApiDocumentMessage d = (ApiDocumentMessage) content;
                    if (d.getExt() instanceof ApiDocumentExPhoto) {
                        return new PhotoContent(remoteContainer);
                    } else if (d.getExt() instanceof ApiDocumentExVideo) {
                        return new VideoContent(remoteContainer);
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
                    } else {
                        return new ServiceContent(remoteContainer);
                    }
                } else if (content instanceof ApiJsonMessage) {
                    ApiJsonMessage json = (ApiJsonMessage) content;
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