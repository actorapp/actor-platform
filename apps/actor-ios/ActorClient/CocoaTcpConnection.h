//
//  CocoaTcpConnection.h
//  ActorModel
//
//  Created by Антон Буков on 16.02.15.
//  Copyright (c) 2015 Anton Bukov. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "im/actor/model/network/Connection.h"

@class AMConnectionEndpoint;
@protocol AMConnectionCallback;

@interface CocoaTcpConnection : NSObject <AMConnection>

- (instancetype)initWithConnectionId:(jint)connectionId
                  connectionEndpoint:(AMConnectionEndpoint *)endpoint
                  connectionCallback:(id<AMConnectionCallback>)callback
                      createCallback:(id<AMCreateConnectionCallback>)createCallback;

- (void)post:(IOSByteArray *)data withOffset:(jint)offset withLen:(jint)len;

- (jboolean)isClosed;

- (void)close;

@end
