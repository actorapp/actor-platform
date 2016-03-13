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

#import "CLTokenInputView.h"

#import "CLTokenInputView.h"

// Ogg record

#import "SLKTextViewController.h"

#import "NYTPhotosViewController.h"
#import "NYTPhoto.h"
#import "NYTPhotoViewController.h"
#import "NYTPhotosViewControllerDataSource.h"
#import "NYTPhotoCaptionViewLayoutWidthHinting.h"

#import "AAAudioRecorder.h"
#import "AAAudioPlayer.h"
#import "AAModernConversationAudioPlayer.h"

// CLTokenView

#import "CLTokenView.h"

// YYKit

#import "YYText.h"
#import "YYDispatchQueuePool.h"

// WebRTC

#import "WebRTC.h"