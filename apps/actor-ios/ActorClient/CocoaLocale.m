//
//  CocoaLocale.m
//  ActorClient
//
//  Created by Антон Буков on 25.02.15.
//  Copyright (c) 2015 Anton Bukov. All rights reserved.
//

#import "java/util/HashMap.h"

#import "CocoaLocale.h"

@implementation CocoaLocale

- (JavaUtilHashMap *)loadLocale
{
    static JavaUtilHashMap *map = nil;
    if (map == nil) {
        map = [[JavaUtilHashMap alloc] init];
        NSError *error = nil;
        NSString *text = [NSString stringWithContentsOfFile:[[NSBundle mainBundle] pathForResource:@"AppText" ofType:@"properties"] encoding:NSUTF8StringEncoding error:&error];
        for (NSString *line in [text componentsSeparatedByString:@"\n"]) {
            NSArray *tokens = [line componentsSeparatedByString:@"="];
            if (tokens.count == 2)
                [map putWithId:tokens[0] withId:tokens[1]];
        }

        text = [NSString stringWithContentsOfFile:[[NSBundle mainBundle] pathForResource:@"Months" ofType:@"properties"] encoding:NSUTF8StringEncoding error:&error];
        for (NSString *line in [text componentsSeparatedByString:@"\n"]) {
            NSArray *tokens = [line componentsSeparatedByString:@"="];
            if (tokens.count == 2)
                [map putWithId:tokens[0] withId:tokens[1]];
        }

    }
    return map;
}

- (jboolean)is24Hours
{
    return true;
}

@end
