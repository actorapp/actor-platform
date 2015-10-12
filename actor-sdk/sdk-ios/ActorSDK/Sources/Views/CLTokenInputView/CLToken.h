//
//  CLToken.h
//  CLTokenInputView
//
//  Created by Rizwan Sattar on 2/24/14.
//  Copyright (c) 2014 Cluster Labs, Inc. All rights reserved.
//

#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN

/**
 * This is a high level object that is provided to the
 * CLTokenInputView when tokens should be added/removed
 */
@interface CLToken : NSObject

/** The text to display in the token view */
@property (copy, nonatomic) NSString *displayText;
/** Used for storing anything that would be useful later on */
@property (strong, nonatomic, nullable) NSObject *context;


- (id)initWithDisplayText:(NSString *)displayText context:(nullable NSObject *)context;

@end

NS_ASSUME_NONNULL_END
