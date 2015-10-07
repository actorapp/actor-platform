//
//  ActorSDK.h
//  ActorSDK
//
//  Created by Steve Kite on 07.10.15.
//  Copyright © 2015 Steve Kite. All rights reserved.
//

#import <UIKit/UIKit.h>

//! Project version number for ActorSDK.
FOUNDATION_EXPORT double ActorSDKVersionNumber;

//! Project version string for ActorSDK.
FOUNDATION_EXPORT const unsigned char ActorSDKVersionString[];

// Importing J2ObjC Runtime
#import <j2objc/j2objc.h>

// Importing Actor Core libraries
#import <ActorCore/ActorCore.h>

// View extensions

#import <ActorSDK/UIAppearance+Swift.h>
#import <ActorSDK/UIBarAppearance+Swift.h>

// FMDB include. FMDB Doesn't support frameworks yet, so we included it to app itself

#import <ActorSDK/FMDatabase.h>
#import <ActorSDK/FMResultSet.h>
#import <ActorSDK/FMDatabaseAdditions.h>
#import <ActorSDK/FMDatabaseQueue.h>
