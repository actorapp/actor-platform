//
//  Copyright (c) 2014-2015 Actor LLC. <https://actor.im>
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
