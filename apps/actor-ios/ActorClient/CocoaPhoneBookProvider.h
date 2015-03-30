//
//  CocoaPhoneBookProvider.h
//  ActorClient
//
//  Created by Антон Буков on 27.02.15.
//  Copyright (c) 2015 Anton Bukov. All rights reserved.
//

#import "im/actor/model/PhoneBookProvider.h"

@interface CocoaPhoneBookProvider : NSObject <AMPhoneBookProvider>

- (void)loadPhoneBookWithAMPhoneBookProvider_Callback:(id<AMPhoneBookProvider_Callback>)callback;

@end
