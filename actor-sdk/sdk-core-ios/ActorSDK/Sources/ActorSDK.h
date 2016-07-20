//
//  Copyright (c) 2014-2016 Actor LLC. <https://actor.im>
//

#import <UIKit/UIKit.h>

//! Project version number for ActorSDK.
FOUNDATION_EXPORT double ActorSDKVersionNumber;

//! Project version string for ActorSDK.
FOUNDATION_EXPORT const unsigned char ActorSDKVersionString[];

// Importing J2ObjC Runtime

@import j2objc;

// Importing Actor Core libraries

#import "ActorCoreUmbrella.h"

// Phone Input

#import "ABPhoneField.h"
#import "RMPhoneFormat.h"

// FMDB include. FMDB Doesn't support frameworks yet, so we included it to app itself

#import "FMDatabase.h"
#import "FMResultSet.h"
#import "FMDatabaseAdditions.h"
#import "FMDatabaseQueue.h"

// GCDAsyncSocket

#import "GCDAsyncSocket.h"

// Ogg record

#import "AAAudioRecorder.h"
#import "AAAudioPlayer.h"
#import "AAModernConversationAudioPlayer.h"

// SLKTextViewController

#import "SLKTextViewController.h"

// NYTPhotos

#import "NYTPhotosViewController.h"
#import "NYTPhoto.h"
#import "NYTPhotoViewController.h"
#import "NYTPhotosViewControllerDataSource.h"
#import "NYTPhotoCaptionViewLayoutWidthHinting.h"

// CLTokenView

#import "CLTokenView.h"
#import "CLTokenInputView.h"

// YYKit

#import "YYText.h"
#import "YYDispatchQueuePool.h"
#import "YYAsyncLayer.h"

// WebRTC

#import "WebRTC.h"