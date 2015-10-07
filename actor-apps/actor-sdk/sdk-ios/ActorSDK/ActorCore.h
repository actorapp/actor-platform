//
//  Copyright (c) 2014-2015 Actor LLC. <https://actor.im>
//

// J2OBJC

#import <j2objc/j2objc.h>

#import <ActorSDK/im/actor/core/CocoaMessenger.h>
#import <ActorSDK/im/actor/core/Messenger.h>
#import <ActorSDK/im/actor/core/ActorAnalytics.h>
#import <ActorSDK/im/actor/core/ApiConfiguration.h>
#import <ActorSDK/im/actor/core/PlatformType.h>
#import <ActorSDK/im/actor/core/AuthState.h>
#import <ActorSDK/im/actor/core/Configuration.h>
#import <ActorSDK/im/actor/core/ConfigurationBuilder.h>
#import <ActorSDK/im/actor/core/DeviceCategory.h>
#import <ActorSDK/im/actor/core/NotificationProvider.h>
#import <ActorSDK/im/actor/core/PhoneBookProvider.h>

#import <ActorSDK/im/actor/core/entity/Avatar.h>
#import <ActorSDK/im/actor/core/entity/AvatarImage.h>
#import <ActorSDK/im/actor/core/entity/Contact.h>
#import <ActorSDK/im/actor/core/entity/ContentType.h>
#import <ActorSDK/im/actor/core/entity/Dialog.h>
#import <ActorSDK/im/actor/core/entity/SearchEntity.h>
#import <ActorSDK/im/actor/core/entity/FileReference.h>
#import <ActorSDK/im/actor/core/entity/Message.h>
#import <ActorSDK/im/actor/core/entity/MessageState.h>
#import <ActorSDK/im/actor/core/entity/Peer.h>
#import <ActorSDK/im/actor/core/entity/PeerType.h>
#import <ActorSDK/im/actor/core/entity/User.h>
#import <ActorSDK/im/actor/core/entity/Sex.h>
#import <ActorSDK/im/actor/core/entity/GroupMember.h>
#import <ActorSDK/im/actor/core/entity/WebActionDescriptor.h>
#import <ActorSDK/im/actor/core/entity/content/AbsContent.h>
#import <ActorSDK/im/actor/core/entity/content/TextContent.h>
#import <ActorSDK/im/actor/core/entity/content/DocumentContent.h>
#import <ActorSDK/im/actor/core/entity/content/ServiceContent.h>
#import <ActorSDK/im/actor/core/entity/content/PhotoContent.h>
#import <ActorSDK/im/actor/core/entity/content/VideoContent.h>
#import <ActorSDK/im/actor/core/entity/content/FastThumb.h>
#import <ActorSDK/im/actor/core/entity/content/FileSource.h>
#import <ActorSDK/im/actor/core/entity/content/FileLocalSource.h>
#import <ActorSDK/im/actor/core/entity/content/FileRemoteSource.h>
#import <ActorSDK/im/actor/core/entity/content/BannerContent.h>
#import <ActorSDK/im/actor/core/entity/PhoneBookContact.h>
#import <ActorSDK/im/actor/core/entity/PhoneBookPhone.h>
#import <ActorSDK/im/actor/core/entity/PhoneBookEmail.h>
#import <ActorSDK/im/actor/core/entity/ContentType.h>
#import <ActorSDK/im/actor/core/entity/FileReference.h>
#import <ActorSDK/im/actor/core/entity/Notification.h>
#import <ActorSDK/im/actor/core/entity/ContentDescription.h>
#import <ActorSDK/im/actor/core/entity/PublicGroup.h>
#import <ActorSDK/im/actor/core/entity/MentionFilterResult.h>

#import <ActorSDK/im/actor/core/analytics/AllEvents.h>
#import <ActorSDK/im/actor/core/analytics/Event.h>
#import <ActorSDK/im/actor/core/analytics/Page.h>

#import <ActorSDK/im/actor/core/viewmodel/UserVM.h>
#import <ActorSDK/im/actor/core/viewmodel/UserTypingVM.h>
#import <ActorSDK/im/actor/core/viewmodel/GroupVM.h>
#import <ActorSDK/im/actor/core/viewmodel/GroupTypingVM.h>
#import <ActorSDK/im/actor/core/viewmodel/UserPhone.h>
#import <ActorSDK/im/actor/core/viewmodel/UserPresence.h>
#import <ActorSDK/im/actor/core/viewmodel/UploadFileVMCallback.h>
#import <ActorSDK/im/actor/core/viewmodel/UploadFileVM.h>
#import <ActorSDK/im/actor/core/viewmodel/FileCallback.h>
#import <ActorSDK/im/actor/core/viewmodel/UploadFileCallback.h>
#import <ActorSDK/im/actor/core/viewmodel/AppStateVM.h>
#import <ActorSDK/im/actor/core/viewmodel/AvatarUploadState.h>
#import <ActorSDK/im/actor/core/viewmodel/GroupAvatarVM.h>
#import <ActorSDK/im/actor/core/viewmodel/OwnAvatarVM.h>
#import <ActorSDK/im/actor/core/viewmodel/Command.h>
#import <ActorSDK/im/actor/core/viewmodel/CommandCallback.h>

#import <ActorSDK/im/actor/core/viewmodel/generics/StringValueModel.h>
#import <ActorSDK/im/actor/core/viewmodel/generics/AvatarValueModel.h>
#import <ActorSDK/im/actor/core/viewmodel/generics/BooleanValueModel.h>
#import <ActorSDK/im/actor/core/viewmodel/generics/IntValueModel.h>
#import <ActorSDK/im/actor/core/viewmodel/generics/UserPhoneValueModel.h>
#import <ActorSDK/im/actor/core/viewmodel/generics/UserPresenceValueModel.h>
#import <ActorSDK/im/actor/core/viewmodel/generics/ArrayListUserPhone.h>

#import <ActorSDK/im/actor/core/i18n/I18NEngine.h>

#import <ActorSDK/im/actor/core/network/RpcException.h>

#import <ActorSDK/im/actor/core/api/ApiAuthSession.h>
#import <ActorSDK/im/actor/core/api/ApiAuthHolder.h>

#import <ActorSDK/im/actor/core/util/StringMatch.h>

// Core Runtime

#import <ActorSDK/im/actor/runtime/Assets.h>
#import <ActorSDK/im/actor/runtime/Crypto.h>
#import <ActorSDK/im/actor/runtime/HTTP.h>
#import <ActorSDK/im/actor/runtime/Log.h>
#import <ActorSDK/im/actor/runtime/Network.h>
#import <ActorSDK/im/actor/runtime/Runtime.h>
#import <ActorSDK/im/actor/runtime/Storage.h>
#import <ActorSDK/im/actor/runtime/StorageRuntime.h>
#import <ActorSDK/im/actor/runtime/FileSystemRuntime.h>
#import <ActorSDK/im/actor/runtime/HttpRuntime.h>
#import <ActorSDK/im/actor/runtime/NetworkRuntime.h>
#import <ActorSDK/im/actor/runtime/mtproto/ManagedConnection.h>
#import <ActorSDK/im/actor/runtime/mtproto/ManagedNetworkProvider.h>
#import <ActorSDK/im/actor/runtime/mtproto/AsyncConnection.h>
#import <ActorSDK/im/actor/runtime/mtproto/AsyncConnectionFactory.h>
#import <ActorSDK/im/actor/runtime/mtproto/AsyncConnectionInterface.h>
#import <ActorSDK/im/actor/runtime/mtproto/ConnectionEndpoint.h>
#import <ActorSDK/im/actor/runtime/cocoa/CocoaHttpProxyProvider.h>
#import <ActorSDK/im/actor/runtime/cocoa/CocoaStorageProxyProvider.h>
#import <ActorSDK/im/actor/runtime/cocoa/CocoaFileSystemProxyProvider.h>
#import <ActorSDK/im/actor/runtime/cocoa/CocoaNetworkProxyProvider.h>

#import <ActorSDK/im/actor/runtime/http/FileDownloadCallback.h>
#import <ActorSDK/im/actor/runtime/http/FileUploadCallback.h>

#import <ActorSDK/im/actor/runtime/files/FileReadCallback.h>
#import <ActorSDK/im/actor/runtime/files/FileSystemReference.h>
#import <ActorSDK/im/actor/runtime/files/InputFile.h>
#import <ActorSDK/im/actor/runtime/files/OutputFile.h>

#import <ActorSDK/im/actor/runtime/mvvm/AsyncVM.h>
#import <ActorSDK/im/actor/runtime/mvvm/BaseValueModel.h>
#import <ActorSDK/im/actor/runtime/mvvm/ModelChangedListener.h>
#import <ActorSDK/im/actor/runtime/mvvm/MVVMCollection.h>
#import <ActorSDK/im/actor/runtime/mvvm/ValueChangedListener.h>
#import <ActorSDK/im/actor/runtime/mvvm/ValueDoubleChangedListener.h>
#import <ActorSDK/im/actor/runtime/mvvm/ValueModel.h>
#import <ActorSDK/im/actor/runtime/mvvm/ValueModelCreator.h>
#import <ActorSDK/im/actor/runtime/mvvm/ValueTripleChangedListener.h>
#import <ActorSDK/im/actor/runtime/mvvm/Value.h>

#import <ActorSDK/im/actor/runtime/generic/mvvm/ListProcessor.h>
#import <ActorSDK/im/actor/runtime/generic/mvvm/BindedDisplayList.h>
#import <ActorSDK/im/actor/runtime/generic/mvvm/ChangeDescription.h>
#import <ActorSDK/im/actor/runtime/generic/mvvm/DisplayList.h>
#import <ActorSDK/im/actor/runtime/generic/mvvm/DisplayWindow.h>
#import <ActorSDK/im/actor/runtime/generic/mvvm/AppleListUpdate.h>
#import <ActorSDK/im/actor/runtime/generic/mvvm/AndroidListUpdate.h>
#import <ActorSDK/im/actor/runtime/generic/mvvm/alg/Modification.h>
#import <ActorSDK/im/actor/runtime/generic/mvvm/alg/Move.h>

#import <ActorSDK/im/actor/runtime/storage/IndexStorage.h>
#import <ActorSDK/im/actor/runtime/storage/KeyValueEngine.h>
#import <ActorSDK/im/actor/runtime/storage/KeyValueItem.h>
#import <ActorSDK/im/actor/runtime/storage/KeyValueRecord.h>
#import <ActorSDK/im/actor/runtime/storage/KeyValueStorage.h>
#import <ActorSDK/im/actor/runtime/storage/ListEngine.h>
#import <ActorSDK/im/actor/runtime/storage/ListEngineDisplayExt.h>
#import <ActorSDK/im/actor/runtime/storage/ListEngineDisplayListener.h>
#import <ActorSDK/im/actor/runtime/storage/ListEngineDisplayLoadCallback.h>
#import <ActorSDK/im/actor/runtime/storage/ListEngineItem.h>
#import <ActorSDK/im/actor/runtime/storage/ListEngineRecord.h>
#import <ActorSDK/im/actor/runtime/storage/ListStorage.h>
#import <ActorSDK/im/actor/runtime/storage/ListStorageDisplayEx.h>
#import <ActorSDK/im/actor/runtime/storage/PreferencesStorage.h>

#import <ActorSDK/im/actor/runtime/markdown/MarkdownParser.h>
#import <ActorSDK/im/actor/runtime/markdown/MDText.h>
#import <ActorSDK/im/actor/runtime/markdown/MDRawText.h>
#import <ActorSDK/im/actor/runtime/markdown/MDCode.h>
#import <ActorSDK/im/actor/runtime/markdown/MDDocument.h>
#import <ActorSDK/im/actor/runtime/markdown/MDSection.h>
#import <ActorSDK/im/actor/runtime/markdown/MDSpan.h>
#import <ActorSDK/im/actor/runtime/markdown/MDUrl.h>
