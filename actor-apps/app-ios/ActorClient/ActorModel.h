//
//  Copyright (c) 2015 Actor LLC. <https://actor.im>
//

#ifndef ActorClient_ActorModel_h
#define ActorClient_ActorModel_h

#import "J2ObjC_header.h"
#import "J2ObjC_source.h"
#import "IOSClass.h"

// Java Objects

#import "java/lang/Exception.h"
#import "java/lang/Runnable.h"
#import "java/lang/Boolean.h"
#import "java/lang/Long.h"
#import "java/lang/Integer.h"
#import "java/util/List.h"
#import "java/util/ArrayList.h"
#import "java/util/HashSet.h"
#import "java/util/Map.h"
#import "java/util/HashMap.h"
#import "java/math/BigInteger.h"

// Messenger

#import "im/actor/model/Messenger.h"
#import "im/actor/model/BaseMessenger.h"
#import "im/actor/model/Configuration.h"
#import "im/actor/model/ConfigurationBuilder.h"
#import "im/actor/model/ApiConfiguration.h"
#import "im/actor/model/StorageProvider.h"
#import "im/actor/model/CryptoProvider.h"
#import "im/actor/model/FileSystemProvider.h"
#import "im/actor/model/LogProvider.h"
#import "im/actor/model/MainThreadProvider.h"
#import "im/actor/model/NotificationProvider.h"
#import "im/actor/model/NetworkProvider.h"
#import "im/actor/model/DispatcherProvider.h"
#import "im/actor/model/LocaleProvider.h"
#import "im/actor/model/PhoneBookProvider.h"
#import "im/actor/model/HttpProvider.h"
#import "im/actor/model/AnalyticsProvider.h"
#import "im/actor/model/AppCategory.h"
#import "im/actor/model/DeviceCategory.h"
#import "im/actor/model/MessengerEnvironment.h"
#import "im/actor/model/LifecycleProvider.h"

#import "im/actor/model/storage/BaseAsyncStorageProvider.h"
#import "im/actor/model/crypto/bouncycastle/BouncyCastleProvider.h"
#import "im/actor/model/cocoa/CocoaThreadingProvider.h"
#import "im/actor/model/network/Connection.h"
#import "im/actor/model/network/CreateConnectionCallback.h"
#import "im/actor/model/network/ConnectionEndpoint.h"
#import "im/actor/model/network/ConnectionCallback.h"
#import "im/actor/model/network/connection/AsyncConnection.h"
#import "im/actor/model/network/connection/AsyncConnectionFactory.h"
#import "im/actor/model/network/connection/AsyncConnectionInterface.h"
#import "im/actor/model/network/connection/ManagedConnection.h"
#import "im/actor/model/network/connection/ManagedConnectionCreateCallback.h"
#import "im/actor/model/network/connection/ManagedNetworkProvider.h"
#import "im/actor/model/http/FileDownloadCallback.h"
#import "im/actor/model/http/FileUploadCallback.h"

#import "im/actor/model/crypto/CryptoKeyPair.h"
#import "im/actor/model/jvm/JavaRandomProvider.h"
#import "im/actor/model/crypto/bouncycastle/RandomProvider.h"

// API
#import "im/actor/model/network/parser/Request.h"
#import "im/actor/model/network/parser/Response.h"
#import "im/actor/model/api/Interest.h"
#import "im/actor/model/api/rpc/RequestGetAvailableInterests.h"
#import "im/actor/model/api/rpc/ResponseGetAvailableInterests.h"
#import "im/actor/model/api/rpc/RequestEnableInterests.h"
#import "im/actor/model/api/rpc/RequestDisableInterests.h"

// DroidKit Engine

#import "im/actor/model/droidkit/engine/ListEngine.h"
#import "im/actor/model/droidkit/engine/ListEngineItem.h"
#import "im/actor/model/droidkit/engine/ListEngineRecord.h"
#import "im/actor/model/droidkit/engine/ListEngineDisplayLoadCallback.h"
#import "im/actor/model/droidkit/engine/ListEngineDisplayListener.h"
#import "im/actor/model/droidkit/engine/ListStorageDisplayEx.h"
#import "im/actor/model/droidkit/engine/ListStorage.h"
#import "im/actor/model/droidkit/engine/KeyValueRecord.h"
#import "im/actor/model/droidkit/engine/KeyValueEngine.h"
#import "im/actor/model/droidkit/engine/KeyValueStorage.h"
#import "im/actor/model/droidkit/engine/KeyValueRecord.h"
#import "im/actor/model/droidkit/engine/PreferencesStorage.h"

// DroidKit Bser

#import "im/actor/model/droidkit/bser/BSerObject.h"

// DroidKit MVVM

#import "im/actor/model/mvvm/generics/StringValueModel.h"
#import "im/actor/model/mvvm/generics/AvatarValueModel.h"
#import "im/actor/model/mvvm/generics/BooleanValueModel.h"
#import "im/actor/model/mvvm/generics/UserPhoneValueModel.h"
#import "im/actor/model/mvvm/generics/UserPresenceValueModel.h"
#import "im/actor/model/mvvm/generics/ArrayListUserPhone.h"
#import "im/actor/model/mvvm/MVVMCollection.h"
#import "im/actor/model/mvvm/ValueModel.h"
#import "im/actor/model/mvvm/ValueChangedListener.h"
#import "im/actor/model/mvvm/DisplayList.h"
#import "im/actor/model/mvvm/BindedDisplayList.h"
#import "im/actor/model/mvvm/AppleListUpdate.h"
#import "im/actor/model/mvvm/AndroidListUpdate.h"
#import "im/actor/model/mvvm/ChangeDescription.h"
#import "im/actor/model/mvvm/BackgroundProcessor.h"

// I18N

#import "im/actor/model/i18n/I18NEngine.h"

// Files

#import "im/actor/model/files/FileSystemReference.h"
#import "im/actor/model/files/InputFile.h"
#import "im/actor/model/files/OutputFile.h"
#import "im/actor/model/files/FileReadCallback.h"

// Entities

#import "im/actor/model/entity/Avatar.h"
#import "im/actor/model/entity/AvatarImage.h"
#import "im/actor/model/entity/Contact.h"
#import "im/actor/model/entity/ContentType.h"
#import "im/actor/model/entity/Dialog.h"
#import "im/actor/model/entity/SearchEntity.h"
#import "im/actor/model/entity/FileReference.h"
#import "im/actor/model/entity/Message.h"
#import "im/actor/model/entity/MessageState.h"
#import "im/actor/model/entity/Peer.h"
#import "im/actor/model/entity/PeerType.h"
#import "im/actor/model/entity/User.h"
#import "im/actor/model/entity/Sex.h"
#import "im/actor/model/entity/GroupMember.h"
#import "im/actor/model/entity/content/AbsContent.h"
#import "im/actor/model/entity/content/TextContent.h"
#import "im/actor/model/entity/content/DocumentContent.h"
#import "im/actor/model/entity/content/ServiceContent.h"
#import "im/actor/model/entity/content/PhotoContent.h"
#import "im/actor/model/entity/content/VideoContent.h"
#import "im/actor/model/entity/content/FastThumb.h"
#import "im/actor/model/entity/content/FileSource.h"
#import "im/actor/model/entity/content/FileLocalSource.h"
#import "im/actor/model/entity/content/FileRemoteSource.h"
#import "im/actor/model/entity/content/BannerContent.h"
#import "im/actor/model/entity/PhoneBookContact.h"
#import "im/actor/model/entity/PhoneBookPhone.h"
#import "im/actor/model/entity/PhoneBookEmail.h"
#import "im/actor/model/entity/ContentType.h"
#import "im/actor/model/entity/FileReference.h"
#import "im/actor/model/entity/Notification.h"
#import "im/actor/model/entity/ContentDescription.h"
#import "im/actor/model/entity/PublicGroup.h"


// Entities View Model

#import "im/actor/model/viewmodel/UserVM.h"
#import "im/actor/model/viewmodel/UserTypingVM.h"
#import "im/actor/model/viewmodel/GroupVM.h"
#import "im/actor/model/viewmodel/GroupTypingVM.h"
#import "im/actor/model/viewmodel/UserPhone.h"
#import "im/actor/model/viewmodel/UserPresence.h"
#import "im/actor/model/viewmodel/UploadFileVMCallback.h"
#import "im/actor/model/viewmodel/UploadFileVM.h"
#import "im/actor/model/viewmodel/FileCallback.h"
#import "im/actor/model/viewmodel/UploadFileCallback.h"
#import "im/actor/model/viewmodel/AppStateVM.h"
#import "im/actor/model/viewmodel/ConversationVM.h"
#import "im/actor/model/viewmodel/ConversationVMCallback.h"
#import "im/actor/model/viewmodel/AvatarUploadState.h"
#import "im/actor/model/viewmodel/GroupAvatarVM.h"
#import "im/actor/model/viewmodel/OwnAvatarVM.h"

// Misc

#import "im/actor/model/AuthState.h"
#import "im/actor/model/concurrency/Command.h"
#import "im/actor/model/concurrency/CommandCallback.h"

// API

#import "im/actor/model/api/AuthSession.h"
#import "im/actor/model/network/RpcException.h"

#endif
