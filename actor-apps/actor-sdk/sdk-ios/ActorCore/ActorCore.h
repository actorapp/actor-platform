//
//  Copyright (c) 2014-2015 Actor LLC. <https://actor.im>
//

// J2OBJC

#import <j2objc/j2objc.h>

#import <ActorCore/im/actor/core/CocoaMessenger.h>
#import <ActorCore/im/actor/core/Messenger.h>
#import <ActorCore/im/actor/core/ActorAnalytics.h>
#import <ActorCore/im/actor/core/ApiConfiguration.h>
#import <ActorCore/im/actor/core/PlatformType.h>
#import <ActorCore/im/actor/core/AuthState.h>
#import <ActorCore/im/actor/core/Configuration.h>
#import <ActorCore/im/actor/core/ConfigurationBuilder.h>
#import <ActorCore/im/actor/core/DeviceCategory.h>
#import <ActorCore/im/actor/core/NotificationProvider.h>
#import <ActorCore/im/actor/core/PhoneBookProvider.h>

#import <ActorCore/im/actor/core/entity/Avatar.h>
#import <ActorCore/im/actor/core/entity/AvatarImage.h>
#import <ActorCore/im/actor/core/entity/Contact.h>
#import <ActorCore/im/actor/core/entity/ContentType.h>
#import <ActorCore/im/actor/core/entity/Dialog.h>
#import <ActorCore/im/actor/core/entity/SearchEntity.h>
#import <ActorCore/im/actor/core/entity/FileReference.h>
#import <ActorCore/im/actor/core/entity/Message.h>
#import <ActorCore/im/actor/core/entity/MessageState.h>
#import <ActorCore/im/actor/core/entity/Peer.h>
#import <ActorCore/im/actor/core/entity/PeerType.h>
#import <ActorCore/im/actor/core/entity/User.h>
#import <ActorCore/im/actor/core/entity/Sex.h>
#import <ActorCore/im/actor/core/entity/GroupMember.h>
#import <ActorCore/im/actor/core/entity/WebActionDescriptor.h>
#import <ActorCore/im/actor/core/entity/content/AbsContent.h>
#import <ActorCore/im/actor/core/entity/content/TextContent.h>
#import <ActorCore/im/actor/core/entity/content/DocumentContent.h>
#import <ActorCore/im/actor/core/entity/content/ServiceContent.h>
#import <ActorCore/im/actor/core/entity/content/PhotoContent.h>
#import <ActorCore/im/actor/core/entity/content/VideoContent.h>
#import <ActorCore/im/actor/core/entity/content/FastThumb.h>
#import <ActorCore/im/actor/core/entity/content/FileSource.h>
#import <ActorCore/im/actor/core/entity/content/FileLocalSource.h>
#import <ActorCore/im/actor/core/entity/content/FileRemoteSource.h>
#import <ActorCore/im/actor/core/entity/content/BannerContent.h>
#import <ActorCore/im/actor/core/entity/PhoneBookContact.h>
#import <ActorCore/im/actor/core/entity/PhoneBookPhone.h>
#import <ActorCore/im/actor/core/entity/PhoneBookEmail.h>
#import <ActorCore/im/actor/core/entity/ContentType.h>
#import <ActorCore/im/actor/core/entity/FileReference.h>
#import <ActorCore/im/actor/core/entity/Notification.h>
#import <ActorCore/im/actor/core/entity/ContentDescription.h>
#import <ActorCore/im/actor/core/entity/PublicGroup.h>
#import <ActorCore/im/actor/core/entity/MentionFilterResult.h>

#import <ActorCore/im/actor/core/analytics/AllEvents.h>
#import <ActorCore/im/actor/core/analytics/Event.h>
#import <ActorCore/im/actor/core/analytics/Page.h>

#import <ActorCore/im/actor/core/viewmodel/UserVM.h>
#import <ActorCore/im/actor/core/viewmodel/UserTypingVM.h>
#import <ActorCore/im/actor/core/viewmodel/GroupVM.h>
#import <ActorCore/im/actor/core/viewmodel/GroupTypingVM.h>
#import <ActorCore/im/actor/core/viewmodel/UserPhone.h>
#import <ActorCore/im/actor/core/viewmodel/UserPresence.h>
#import <ActorCore/im/actor/core/viewmodel/UploadFileVMCallback.h>
#import <ActorCore/im/actor/core/viewmodel/UploadFileVM.h>
#import <ActorCore/im/actor/core/viewmodel/FileCallback.h>
#import <ActorCore/im/actor/core/viewmodel/UploadFileCallback.h>
#import <ActorCore/im/actor/core/viewmodel/AppStateVM.h>
#import <ActorCore/im/actor/core/viewmodel/AvatarUploadState.h>
#import <ActorCore/im/actor/core/viewmodel/GroupAvatarVM.h>
#import <ActorCore/im/actor/core/viewmodel/OwnAvatarVM.h>
#import <ActorCore/im/actor/core/viewmodel/Command.h>
#import <ActorCore/im/actor/core/viewmodel/CommandCallback.h>

#import <ActorCore/im/actor/core/viewmodel/generics/StringValueModel.h>
#import <ActorCore/im/actor/core/viewmodel/generics/AvatarValueModel.h>
#import <ActorCore/im/actor/core/viewmodel/generics/BooleanValueModel.h>
#import <ActorCore/im/actor/core/viewmodel/generics/IntValueModel.h>
#import <ActorCore/im/actor/core/viewmodel/generics/UserPhoneValueModel.h>
#import <ActorCore/im/actor/core/viewmodel/generics/UserPresenceValueModel.h>
#import <ActorCore/im/actor/core/viewmodel/generics/ArrayListUserPhone.h>

#import <ActorCore/im/actor/core/i18n/I18NEngine.h>

#import <ActorCore/im/actor/core/network/RpcException.h>

#import <ActorCore/im/actor/core/api/ApiAuthSession.h>
#import <ActorCore/im/actor/core/api/ApiAuthHolder.h>

#import <ActorCore/im/actor/core/util/StringMatch.h>

// Core Runtime

#import <ActorCore/im/actor/runtime/Assets.h>
#import <ActorCore/im/actor/runtime/Crypto.h>
#import <ActorCore/im/actor/runtime/HTTP.h>
#import <ActorCore/im/actor/runtime/Log.h>
#import <ActorCore/im/actor/runtime/Network.h>
#import <ActorCore/im/actor/runtime/Runtime.h>
#import <ActorCore/im/actor/runtime/Storage.h>
#import <ActorCore/im/actor/runtime/StorageRuntime.h>
#import <ActorCore/im/actor/runtime/FileSystemRuntime.h>
#import <ActorCore/im/actor/runtime/HttpRuntime.h>
#import <ActorCore/im/actor/runtime/NetworkRuntime.h>
#import <ActorCore/im/actor/runtime/mtproto/ManagedConnection.h>
#import <ActorCore/im/actor/runtime/mtproto/ManagedNetworkProvider.h>
#import <ActorCore/im/actor/runtime/mtproto/AsyncConnection.h>
#import <ActorCore/im/actor/runtime/mtproto/AsyncConnectionFactory.h>
#import <ActorCore/im/actor/runtime/mtproto/AsyncConnectionInterface.h>
#import <ActorCore/im/actor/runtime/mtproto/ConnectionEndpoint.h>
#import <ActorCore/im/actor/runtime/cocoa/CocoaHttpProxyProvider.h>
#import <ActorCore/im/actor/runtime/cocoa/CocoaStorageProxyProvider.h>
#import <ActorCore/im/actor/runtime/cocoa/CocoaFileSystemProxyProvider.h>
#import <ActorCore/im/actor/runtime/cocoa/CocoaNetworkProxyProvider.h>

#import <ActorCore/im/actor/runtime/http/FileDownloadCallback.h>
#import <ActorCore/im/actor/runtime/http/FileUploadCallback.h>

#import <ActorCore/im/actor/runtime/files/FileReadCallback.h>
#import <ActorCore/im/actor/runtime/files/FileSystemReference.h>
#import <ActorCore/im/actor/runtime/files/InputFile.h>
#import <ActorCore/im/actor/runtime/files/OutputFile.h>

#import <ActorCore/im/actor/runtime/mvvm/AsyncVM.h>
#import <ActorCore/im/actor/runtime/mvvm/BaseValueModel.h>
#import <ActorCore/im/actor/runtime/mvvm/ModelChangedListener.h>
#import <ActorCore/im/actor/runtime/mvvm/MVVMCollection.h>
#import <ActorCore/im/actor/runtime/mvvm/ValueChangedListener.h>
#import <ActorCore/im/actor/runtime/mvvm/ValueDoubleChangedListener.h>
#import <ActorCore/im/actor/runtime/mvvm/ValueModel.h>
#import <ActorCore/im/actor/runtime/mvvm/ValueModelCreator.h>
#import <ActorCore/im/actor/runtime/mvvm/ValueTripleChangedListener.h>
#import <ActorCore/im/actor/runtime/mvvm/Value.h>

#import <ActorCore/im/actor/runtime/generic/mvvm/ListProcessor.h>
#import <ActorCore/im/actor/runtime/generic/mvvm/BindedDisplayList.h>
#import <ActorCore/im/actor/runtime/generic/mvvm/ChangeDescription.h>
#import <ActorCore/im/actor/runtime/generic/mvvm/DisplayList.h>
#import <ActorCore/im/actor/runtime/generic/mvvm/DisplayWindow.h>
#import <ActorCore/im/actor/runtime/generic/mvvm/AppleListUpdate.h>
#import <ActorCore/im/actor/runtime/generic/mvvm/AndroidListUpdate.h>
#import <ActorCore/im/actor/runtime/generic/mvvm/alg/Modification.h>
#import <ActorCore/im/actor/runtime/generic/mvvm/alg/Move.h>

#import <ActorCore/im/actor/runtime/storage/IndexStorage.h>
#import <ActorCore/im/actor/runtime/storage/KeyValueEngine.h>
#import <ActorCore/im/actor/runtime/storage/KeyValueItem.h>
#import <ActorCore/im/actor/runtime/storage/KeyValueRecord.h>
#import <ActorCore/im/actor/runtime/storage/KeyValueStorage.h>
#import <ActorCore/im/actor/runtime/storage/ListEngine.h>
#import <ActorCore/im/actor/runtime/storage/ListEngineDisplayExt.h>
#import <ActorCore/im/actor/runtime/storage/ListEngineDisplayListener.h>
#import <ActorCore/im/actor/runtime/storage/ListEngineDisplayLoadCallback.h>
#import <ActorCore/im/actor/runtime/storage/ListEngineItem.h>
#import <ActorCore/im/actor/runtime/storage/ListEngineRecord.h>
#import <ActorCore/im/actor/runtime/storage/ListStorage.h>
#import <ActorCore/im/actor/runtime/storage/ListStorageDisplayEx.h>
#import <ActorCore/im/actor/runtime/storage/PreferencesStorage.h>

#import <ActorCore/im/actor/runtime/markdown/MarkdownParser.h>
#import <ActorCore/im/actor/runtime/markdown/MDText.h>
#import <ActorCore/im/actor/runtime/markdown/MDRawText.h>
#import <ActorCore/im/actor/runtime/markdown/MDCode.h>
#import <ActorCore/im/actor/runtime/markdown/MDDocument.h>
#import <ActorCore/im/actor/runtime/markdown/MDSection.h>
#import <ActorCore/im/actor/runtime/markdown/MDSpan.h>
#import <ActorCore/im/actor/runtime/markdown/MDUrl.h>
