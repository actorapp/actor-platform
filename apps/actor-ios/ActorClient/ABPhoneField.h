//
//  ABPhoneField.h
//  ActorClient
//
//  Created by Антон Буков on 20.02.15.
//  Copyright (c) 2015 Anton Bukov. All rights reserved.
//

#import <UIKit/UIKit.h>

@interface ABPhoneField : UITextField

@property (nonatomic, assign) BOOL prefixHidden;

+ (NSDictionary *)callingCodeByCountryCode;
+ (NSDictionary *)countryNameByCountryCode;
+ (NSDictionary *)phoneMinLengthByCountryCode;
+ (NSArray *)sortedIsoCodes;

@property (nonatomic, strong) NSString *currentIso;
@property (nonatomic, readonly) NSString *phoneNumber;
@property (nonatomic, readonly) NSString *formattedPhoneNumber;

@end
