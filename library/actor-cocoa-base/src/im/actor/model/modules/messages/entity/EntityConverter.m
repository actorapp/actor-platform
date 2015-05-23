//
//  Generated by the J2ObjC translator.  DO NOT EDIT!
//  source: /Users/ex3ndr/Develop/actor-model/library/actor-cocoa-base/build/java/im/actor/model/modules/messages/entity/EntityConverter.java
//


#include "IOSPrimitiveArray.h"
#include "J2ObjC_source.h"
#include "im/actor/model/api/Avatar.h"
#include "im/actor/model/api/DocumentEx.h"
#include "im/actor/model/api/DocumentExPhoto.h"
#include "im/actor/model/api/DocumentExVideo.h"
#include "im/actor/model/api/DocumentMessage.h"
#include "im/actor/model/api/FastThumb.h"
#include "im/actor/model/api/FileLocation.h"
#include "im/actor/model/api/Group.h"
#include "im/actor/model/api/Message.h"
#include "im/actor/model/api/MessageState.h"
#include "im/actor/model/api/Peer.h"
#include "im/actor/model/api/PeerType.h"
#include "im/actor/model/api/ServiceEx.h"
#include "im/actor/model/api/ServiceExChangedAvatar.h"
#include "im/actor/model/api/ServiceExChangedTitle.h"
#include "im/actor/model/api/ServiceExGroupCreated.h"
#include "im/actor/model/api/ServiceExUserAdded.h"
#include "im/actor/model/api/ServiceExUserKicked.h"
#include "im/actor/model/api/ServiceExUserLeft.h"
#include "im/actor/model/api/ServiceMessage.h"
#include "im/actor/model/api/TextMessage.h"
#include "im/actor/model/entity/Avatar.h"
#include "im/actor/model/entity/FileReference.h"
#include "im/actor/model/entity/Group.h"
#include "im/actor/model/entity/MessageState.h"
#include "im/actor/model/entity/Peer.h"
#include "im/actor/model/entity/PeerType.h"
#include "im/actor/model/entity/content/AbsContent.h"
#include "im/actor/model/entity/content/DocumentContent.h"
#include "im/actor/model/entity/content/FastThumb.h"
#include "im/actor/model/entity/content/FileRemoteSource.h"
#include "im/actor/model/entity/content/PhotoContent.h"
#include "im/actor/model/entity/content/ServiceContent.h"
#include "im/actor/model/entity/content/ServiceGroupAvatarChanged.h"
#include "im/actor/model/entity/content/ServiceGroupCreated.h"
#include "im/actor/model/entity/content/ServiceGroupTitleChanged.h"
#include "im/actor/model/entity/content/ServiceGroupUserAdded.h"
#include "im/actor/model/entity/content/ServiceGroupUserKicked.h"
#include "im/actor/model/entity/content/ServiceGroupUserLeave.h"
#include "im/actor/model/entity/content/TextContent.h"
#include "im/actor/model/entity/content/VideoContent.h"
#include "im/actor/model/modules/messages/entity/EntityConverter.h"

@implementation ImActorModelModulesMessagesEntityEntityConverter

+ (AMMessageStateEnum *)convertWithAPMessageStateEnum:(APMessageStateEnum *)state {
  return ImActorModelModulesMessagesEntityEntityConverter_convertWithAPMessageStateEnum_(state);
}

+ (AMGroup *)convertWithAPGroup:(APGroup *)group {
  return ImActorModelModulesMessagesEntityEntityConverter_convertWithAPGroup_(group);
}

+ (AMPeerTypeEnum *)convertWithAPPeerTypeEnum:(APPeerTypeEnum *)peerType {
  return ImActorModelModulesMessagesEntityEntityConverter_convertWithAPPeerTypeEnum_(peerType);
}

+ (AMPeer *)convertWithAPPeer:(APPeer *)peer {
  return ImActorModelModulesMessagesEntityEntityConverter_convertWithAPPeer_(peer);
}

+ (AMAbsContent *)convertWithAPMessage:(APMessage *)content {
  return ImActorModelModulesMessagesEntityEntityConverter_convertWithAPMessage_(content);
}

+ (AMFastThumb *)convertWithAPFastThumb:(APFastThumb *)fastThumb {
  return ImActorModelModulesMessagesEntityEntityConverter_convertWithAPFastThumb_(fastThumb);
}

- (instancetype)init {
  ImActorModelModulesMessagesEntityEntityConverter_init(self);
  return self;
}

@end

AMMessageStateEnum *ImActorModelModulesMessagesEntityEntityConverter_convertWithAPMessageStateEnum_(APMessageStateEnum *state) {
  ImActorModelModulesMessagesEntityEntityConverter_initialize();
  if (state == nil) {
    return AMMessageStateEnum_get_UNKNOWN();
  }
  switch ([state ordinal]) {
    case APMessageState_READ:
    return AMMessageStateEnum_get_READ();
    case APMessageState_RECEIVED:
    return AMMessageStateEnum_get_RECEIVED();
    case APMessageState_SENT:
    return AMMessageStateEnum_get_SENT();
    default:
    return AMMessageStateEnum_get_UNKNOWN();
  }
}

AMGroup *ImActorModelModulesMessagesEntityEntityConverter_convertWithAPGroup_(APGroup *group) {
  ImActorModelModulesMessagesEntityEntityConverter_initialize();
  return new_AMGroup_initWithAPGroup_(group);
}

AMPeerTypeEnum *ImActorModelModulesMessagesEntityEntityConverter_convertWithAPPeerTypeEnum_(APPeerTypeEnum *peerType) {
  ImActorModelModulesMessagesEntityEntityConverter_initialize();
  switch ([peerType ordinal]) {
    case APPeerType_GROUP:
    return AMPeerTypeEnum_get_GROUP();
    default:
    case APPeerType_PRIVATE:
    return AMPeerTypeEnum_get_PRIVATE();
  }
}

AMPeer *ImActorModelModulesMessagesEntityEntityConverter_convertWithAPPeer_(APPeer *peer) {
  ImActorModelModulesMessagesEntityEntityConverter_initialize();
  return new_AMPeer_initWithAMPeerTypeEnum_withInt_(ImActorModelModulesMessagesEntityEntityConverter_convertWithAPPeerTypeEnum_([((APPeer *) nil_chk(peer)) getType]), [peer getId]);
}

AMAbsContent *ImActorModelModulesMessagesEntityEntityConverter_convertWithAPMessage_(APMessage *content) {
  ImActorModelModulesMessagesEntityEntityConverter_initialize();
  if ([content isKindOfClass:[APTextMessage class]]) {
    APTextMessage *message = (APTextMessage *) check_class_cast(content, [APTextMessage class]);
    return new_AMTextContent_initWithNSString_([((APTextMessage *) nil_chk(message)) getText]);
  }
  else if ([content isKindOfClass:[APServiceMessage class]]) {
    APServiceMessage *message = (APServiceMessage *) check_class_cast(content, [APServiceMessage class]);
    APServiceEx *ex = [((APServiceMessage *) nil_chk(message)) getExt];
    if ([ex isKindOfClass:[APServiceExChangedAvatar class]]) {
      APServiceExChangedAvatar *avatar = (APServiceExChangedAvatar *) check_class_cast(ex, [APServiceExChangedAvatar class]);
      return new_AMServiceGroupAvatarChanged_initWithAMAvatar_([((APServiceExChangedAvatar *) nil_chk(avatar)) getAvatar] != nil ? new_AMAvatar_initWithAPAvatar_([avatar getAvatar]) : nil);
    }
    else if ([ex isKindOfClass:[APServiceExChangedTitle class]]) {
      APServiceExChangedTitle *title = (APServiceExChangedTitle *) check_class_cast(ex, [APServiceExChangedTitle class]);
      return new_AMServiceGroupTitleChanged_initWithNSString_([((APServiceExChangedTitle *) nil_chk(title)) getTitle]);
    }
    else if ([ex isKindOfClass:[APServiceExUserAdded class]]) {
      APServiceExUserAdded *userAdded = (APServiceExUserAdded *) check_class_cast(ex, [APServiceExUserAdded class]);
      return new_AMServiceGroupUserAdded_initWithInt_([((APServiceExUserAdded *) nil_chk(userAdded)) getAddedUid]);
    }
    else if ([ex isKindOfClass:[APServiceExUserKicked class]]) {
      APServiceExUserKicked *exUserKicked = (APServiceExUserKicked *) check_class_cast(ex, [APServiceExUserKicked class]);
      return new_AMServiceGroupUserKicked_initWithInt_([((APServiceExUserKicked *) nil_chk(exUserKicked)) getKickedUid]);
    }
    else if ([ex isKindOfClass:[APServiceExUserLeft class]]) {
      return new_AMServiceGroupUserLeave_init();
    }
    else if ([ex isKindOfClass:[APServiceExGroupCreated class]]) {
      return new_AMServiceGroupCreated_initWithNSString_(@"");
    }
    else {
      return new_AMServiceContent_initWithNSString_([message getText]);
    }
  }
  else if ([content isKindOfClass:[APDocumentMessage class]]) {
    APDocumentMessage *documentMessage = (APDocumentMessage *) check_class_cast(content, [APDocumentMessage class]);
    NSString *mimeType = [((APDocumentMessage *) nil_chk(documentMessage)) getMimeType];
    NSString *name = [documentMessage getName];
    AMFastThumb *fastThumb = ImActorModelModulesMessagesEntityEntityConverter_convertWithAPFastThumb_([documentMessage getThumb]);
    AMFileReference *fileReference = new_AMFileReference_initWithAPFileLocation_withNSString_withInt_(new_APFileLocation_initWithLong_withLong_([documentMessage getFileId], [documentMessage getAccessHash]), [documentMessage getName], [documentMessage getFileSize]);
    AMFileRemoteSource *source = new_AMFileRemoteSource_initWithAMFileReference_(fileReference);
    if ([[documentMessage getExt] isKindOfClass:[APDocumentExPhoto class]]) {
      APDocumentExPhoto *photo = (APDocumentExPhoto *) check_class_cast([documentMessage getExt], [APDocumentExPhoto class]);
      return new_AMPhotoContent_initWithAMFileSource_withNSString_withNSString_withAMFastThumb_withInt_withInt_(source, mimeType, name, fastThumb, [((APDocumentExPhoto *) nil_chk(photo)) getW], [photo getH]);
    }
    else if ([[documentMessage getExt] isKindOfClass:[APDocumentExVideo class]]) {
      APDocumentExVideo *video = (APDocumentExVideo *) check_class_cast([documentMessage getExt], [APDocumentExVideo class]);
      return new_AMVideoContent_initWithAMFileSource_withNSString_withNSString_withAMFastThumb_withInt_withInt_withInt_(source, mimeType, name, fastThumb, [((APDocumentExVideo *) nil_chk(video)) getDuration], [video getW], [video getH]);
    }
    else {
      return new_AMDocumentContent_initWithAMFileSource_withNSString_withNSString_withAMFastThumb_(source, mimeType, name, fastThumb);
    }
  }
  return nil;
}

AMFastThumb *ImActorModelModulesMessagesEntityEntityConverter_convertWithAPFastThumb_(APFastThumb *fastThumb) {
  ImActorModelModulesMessagesEntityEntityConverter_initialize();
  if (fastThumb == nil) {
    return nil;
  }
  return new_AMFastThumb_initWithInt_withInt_withByteArray_([((APFastThumb *) nil_chk(fastThumb)) getW], [fastThumb getH], [fastThumb getThumb]);
}

void ImActorModelModulesMessagesEntityEntityConverter_init(ImActorModelModulesMessagesEntityEntityConverter *self) {
  (void) NSObject_init(self);
}

ImActorModelModulesMessagesEntityEntityConverter *new_ImActorModelModulesMessagesEntityEntityConverter_init() {
  ImActorModelModulesMessagesEntityEntityConverter *self = [ImActorModelModulesMessagesEntityEntityConverter alloc];
  ImActorModelModulesMessagesEntityEntityConverter_init(self);
  return self;
}

J2OBJC_CLASS_TYPE_LITERAL_SOURCE(ImActorModelModulesMessagesEntityEntityConverter)
