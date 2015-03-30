//
//  CocoaPhoneBookProvider.m
//  ActorClient
//
//  Created by Антон Буков on 27.02.15.
//  Copyright (c) 2015 Anton Bukov. All rights reserved.
//

#import <AddressBook/AddressBook.h>
#import "java/util/Vector.h"
#import "java/util/ArrayList.h"
#import "im/actor/model/entity/PhoneBookContact.h"
#import "im/actor/model/entity/PhoneBookPhone.h"
#import "im/actor/model/entity/PhoneBookEmail.h"
#import "CocoaPhoneBookProvider.h"

@implementation CocoaPhoneBookProvider

- (void)loadPhoneBookWithAMPhoneBookProvider_Callback:(id<AMPhoneBookProvider_Callback>)callback
{
    ABAddressBookRef ab = ABAddressBookCreateWithOptions(NULL, NULL);
    ABAddressBookRequestAccessWithCompletion(ab, ^(bool granted, CFErrorRef error) {
        if (!granted) {
            NSLog(@"Access to AddressBook denied");
            return;
        }
        
        NSMutableArray *contacts = [NSMutableArray array];
        
        CFArrayRef allPeople = ABAddressBookCopyArrayOfAllPeople(ab);
        for (int i = 0; i < CFArrayGetCount(allPeople); i++)
        {
            ABRecordRef person = CFArrayGetValueAtIndex(allPeople, i);
            CFStringRef firstNameRef = ABRecordCopyValue(person, kABPersonFirstNameProperty);
            CFStringRef lastNameRef = ABRecordCopyValue(person, kABPersonLastNameProperty);
            CFStringRef midNameRef = ABRecordCopyValue(person, kABPersonMiddleNameProperty);
            NSString *firstName = (NSString *)CFBridgingRelease(firstNameRef);
            NSString *lastName = (NSString *)CFBridgingRelease(lastNameRef);
            NSString *midName = (NSString *)CFBridgingRelease(midNameRef);
            
            NSArray *names = [@[firstName?:@"",lastName?:@"",midName?:@""] filteredArrayUsingPredicate:[NSPredicate predicateWithFormat:@"length != 0"]];
            NSString *name = [names componentsJoinedByString:@" "];
            
            ABMultiValueRef phones = ABRecordCopyValue(person, kABPersonPhoneProperty);
            NSMutableArray *userPhones = [NSMutableArray array];
            for (CFIndex j = 0; j < ABMultiValueGetCount(phones); j++)
            {
                CFStringRef phoneStrRef = ABMultiValueCopyValueAtIndex(phones, j);
                NSString *phoneStr = (NSString *)CFBridgingRelease(phoneStrRef);
                if (phoneStr.length == 11 && [phoneStr hasPrefix:@"8"])
                    phoneStr = [@"7" stringByAppendingString:[phoneStr substringFromIndex:1]];
                [userPhones addObject:phoneStr];
                //[contacts addObject:@{@"name":name,@"phone":phoneStr?:@""}];
            }
            
            ABMultiValueRef mails = ABRecordCopyValue(person, kABPersonEmailProperty);
            NSMutableArray *userMails = [NSMutableArray array];
            for (CFIndex j = 0; j < ABMultiValueGetCount(mails); j++)
            {
                CFStringRef mailStrRef = ABMultiValueCopyValueAtIndex(mails, j);
                NSString *mailStr = (NSString *)CFBridgingRelease(mailStrRef);
                [userMails addObject:mailStr];
                //[contacts addObject:@{@"name":name,@"mail":mailStr?:@""}];
            }
            
            [contacts addObject:@{@"name":name,
                                  @"phones":userPhones,
                                  @"mails":userMails}];
            
            CFRelease(phones);
            CFRelease(mails);
        }
        
        JavaUtilVector *vector = [[JavaUtilVector alloc] init];
        NSInteger index = 1;
        for (id u in contacts) {
            JavaUtilVector *phones = [[JavaUtilVector alloc] init];
            for (NSString *p in u[@"phones"])
                [phones addElementWithId:[[AMPhoneBookPhone alloc] initWithLong:index withLong:p.longLongValue]];
            JavaUtilVector *mails = [[JavaUtilVector alloc] init];
            for (NSString *m in u[@"phones"])
                [mails addElementWithId:[[AMPhoneBookEmail alloc] initWithLong:index withNSString:m]];
            
            [vector addElementWithId:[[AMPhoneBookContact alloc] initWithLong:index withNSString:u[@"name"] withJavaUtilArrayList:[[JavaUtilArrayList alloc] initWithJavaUtilCollection:phones] withJavaUtilArrayList:[[JavaUtilArrayList alloc] initWithJavaUtilCollection:mails]]];
             index++;
        }
        [callback onLoadedWithJavaUtilList:vector];
        
        CFRelease(allPeople);
        CFRelease(ab);
    });
}

@end
