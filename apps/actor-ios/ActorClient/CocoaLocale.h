//
//  CocoaLocale.h
//  ActorClient
//
//  Created by Антон Буков on 25.02.15.
//  Copyright (c) 2015 Anton Bukov. All rights reserved.
//

#import "im/actor/model/LocaleProvider.h"

@interface CocoaLocale : NSObject <AMLocaleProvider>

- (JavaUtilHashMap *)loadLocale;

- (jboolean)is24Hours;

@end
