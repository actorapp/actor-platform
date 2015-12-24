/**
 Copyright (c) 2014-present, Facebook, Inc.
 All rights reserved.
 
 This source code is licensed under the BSD-style license found in the
 LICENSE file in the root directory of this source tree. An additional grant
 of patent rights can be found in the PATENTS file in the same directory.
 */

#import <UIKit/UIView.h>

#import "FBShimmering.h"

/**
  @abstract Lightweight, generic shimmering view.
 */
@interface FBShimmeringView : UIView <FBShimmering>

//! @abstract The content view to be shimmered.
@property (strong, nonatomic) UIView *contentView;

@end
