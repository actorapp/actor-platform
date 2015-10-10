//
// RMPhoneFormat.m v1.0

// Copyright (c) 2012, Rick Maddy
// All rights reserved.
//
// Redistribution and use in source and binary forms, with or without
// modification, are permitted provided that the following conditions are met:
//
// * Redistributions of source code must retain the above copyright notice, this
//   list of conditions and the following disclaimer.
//
// * Redistributions in binary form must reproduce the above copyright notice,
//   this list of conditions and the following disclaimer in the documentation
//   and/or other materials provided with the distribution.
//
// THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
// ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
// WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
// DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
// FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
// DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS
// OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
// CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
// OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
// OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

#import <ActorSDK/RMPhoneFormat.h>

/*
 * This class depends on a copy of an Apple provided private framework file named Default.phoneformat being copied
 * into the app's resource bundle and named PhoneFormats.dat.
 *
 * The Default.phoneformat file can be located in:
 * /Applications/Xcode.app/Contents/Developer/Platforms/iPhoneSimulator.platform/Developer/SDKs/iPhoneSimulator<version>.sdk/System/Library/PrivateFrameworks/AppSupport.framework
 *    where <version> is the version of the iPhone SDK installed.
 */

/*
 * Add PhoneFormat.h and PhoneFormat.m to your project. This class uses ARC. If your project is not using ARC
 * then add the -fno-objc-arc flag to PhoneFormat.m.
 */

/*
 * Usage:
 * Create an instance of PhoneFormat. For each phone number you need to format simply call the 'format:' method with
 * the entered phone number. The supplied number can be raw phone number digits or an already formatted number.
 *
 * RMPhoneFormat *fmt = [[RMPhoneFormat alloc] init];
 * ...
 * // Call any number of times
 * NSString *formatedNumber = [fmt format:numberString];
 * ...
 * [fmt release]; // if not using ARC
 */

/**********************************************************************************************************************
 Default.phoneformat format details (iOS < 4.3)
 
 32-bit words in LSB format
 16-bit words in LSB format
 
 File Offset 0: 1-word country code count (CodeCount)
 File Offset 4 to (CodeCount * 12 - 1): Country Code 1 - Country Code n (n = CodeCount)
 
 Each Country Code Block is 12 bytes.
 Bytes 0 - 3: ASCII string of country's international prefix (1, 61, 44, etc.)
 Bytes 4 - 7: 2-letter lowercase ASCII string of country code (us, au, uk, etc.)
 Bytes 8 - 11: 1 32-bit word byte offset of phone number format info. Offset is relative to end of Country Code blocks
 
 Phone Number Format section for each country code.
 
 Block 1: Header info including lengths, trunk prefixes, and international access prefixes
 Bytes 0 - 3: Block 1 length in bytes
 Bytes 4 - 7: Block 2 length in bytes
 Bytes 8 - 11: Rule Set Count
 Bytes 12+: NULL terminated list of NULL terminated strings representing the country's trunk prefixes. Followed by NULL terminated list of NULL terminated strings representing the country's international access prefixes.
 
 Block 2: Rule Sets - RuleSetCount Rule Sets
 Rule Set n:
 Bytes 0 - 1: 1 16-bit word number of phone number digits to match.
 Bytes 2 - 3: 1 16-bit word count of rules in set.
 Bytes 4 to (RuleCount * 16 + 3): Rule 1 - Rule n (n = rule set rule count)
 
 Rule n:
 Bytes 0 - 3: Min X of matching prefix for rule
 Bytes 4 - 7: Max X of matching prefix for rule
 Bytes 8 - 9: 1 16-bit word max length of phone number format string.
 Byte 10: Flag (unknown use)
 0: ???
 1: ???
 2: ???
 3: ???
 4: ???
 5: ???
 Byte 11: Flag - Bit mask
 0: Format string does not use trunk prefix or international prefix
 1: Format string should be used if number has trunk prefix
 2: Format string should be used if number has international prefix
 3: Format string should be used if number has trunk prefix or international prefix
 Bytes 12 - 15: 1 32-bit word byte offset to format string. Offset relative to start of format string block
 
 String Block
 NULL terminated strings formats.
 # used for digit, +, #, or *.
 n used for trunk prefix.
 c used for international prefix.
 All other characters used as-is. Includes spaces, dashes, parentheses, and slashes.
 **********************************************************************************************************************/

/**********************************************************************************************************************
 Default.phoneformat format details (iOS >= 4.3)
 
 32-bit words in LSB format
 16-bit words in LSB format
 
 File Offset 0: 1-word country code count (CodeCount)
 File Offset 4 to (CodeCount * 12 - 1): Country Code 1 - Country Code n (n = CodeCount)
 
 Each Country Code Block is 12 bytes.
 Bytes 0 - 3: ASCII string of country's international prefix (1, 61, 44, etc.)
 Bytes 4 - 7: 2-letter lowercase ASCII string of country code (us, au, uk, etc.)
 Bytes 8 - 11: 1 32-bit word byte offset of phone number format info. Offset is relative to end of Country Code blocks
 
 Phone Number Format section for each country code.
 
 Block 1: Header info including lengths, trunk prefixes, and international access prefixes
 Bytes 0 - 1: Block 1 length in bytes
 Bytes 2 - 3: Unknown
 Bytes 4 - 5: Block 2 length in bytes
 Bytes 6 - 7: Unknown
 Bytes 8 - 11: Rule Set Count
 Bytes 12+: NULL terminated list of NULL terminated strings representing the country's trunk prefixes. Followed by NULL terminated list of NULL terminated strings representing the country's international access prefixes.
 
 Block 2: Rule Sets - RuleSetCount Rule Sets
 Rule Set n:
 Bytes 0 - 1: 1 16-bit word number of phone number digits to match.
 Bytes 2 - 3: 1 16-bit word count of rules in set.
 Bytes 4 to (RuleCount * 16 + 3): Rule 1 - Rule n (n = rule set rule count)
 
 Rule n:
 Bytes 0 - 3: Min X of matching prefix for rule
 Bytes 4 - 7: Max X of matching prefix for rule
 Byte 8: Unknown - As of iOS 6.0, the values 2, 3, 5, 6, 7, 8, 9, 10 are used of some rules.
 0: ???
 1: ???
 2: ???
 3: ???
 4: ???
 5: ???
 6: ???
 7: ???
 Byte 9: 1 8-bit byte max length of phone number format string.
 Byte 10: Unknown - As of iOS 6.0, the values 1, 2, 3, 129, 130, 131 are used by some rules.
 0 (  1): ???
 1 (  2): ???
 2 (  4): Not used
 3 (  8): Not used
 4 ( 16): Not used
 5 ( 32): Not used
 6 ( 64): Not used
 7 (128): ???
 Byte 11: Some sort of prefix length
 Byte 12: Flag - Bit mask
 0 (  1): Format string should be used if number has trunk prefix (n)
 1 (  2): Format string should be used if number has international prefix (c)
 2 (  4): Used by seveal rules in various countries
 3 (  8): Used by seveal rules in various countries
 4 ( 16): Used by one rule in 'us'
 5 ( 32): Used by several rules in 'br'
 6 ( 64): Not used (as of iOS 6)
 7 (128): Not used (as of iOS 6)
 Bytes 13: Flag - Bit mask - several different values each used by one rule in many cases, a few rules in some cases.
 0: ???
 1: ???
 2: ???
 3: ???
 4: ???
 5: ???
 6: ???
 7: ???
 Bytes 14 - 15: 1 16-bit word byte offset to format string. Offset relative to start of format string block
 
 String Block
 NULL terminated strings formats.
 # used for digit, +, #, or *.
 n used for trunk prefix.
 c used for international prefix.
 All other characters used as-is. Includes spaces, dashes, parentheses, and slashes.
 **********************************************************************************************************************/

@interface PhoneRule : NSObject

@property (nonatomic, assign) int minVal;
@property (nonatomic, assign) int maxVal;
@property (nonatomic, assign) int byte8;
@property (nonatomic, assign) int maxLen;
@property (nonatomic, assign) int otherFlag;
@property (nonatomic, assign) int prefixLen;
@property (nonatomic, assign) int flag12;
@property (nonatomic, assign) int flag13;
@property (nonatomic) NSString *format;
@property (nonatomic, readonly) BOOL hasIntlPrefix;
@property (nonatomic, readonly) BOOL hasTrunkPrefix;
#ifdef DEBUG
@property (nonatomic) NSSet *countries;
@property (nonatomic) NSString *callingCode;
@property (nonatomic, assign) int matchLen;
#endif

- (NSString *)format:(NSString *)str intlPrefix:(NSString *)intlPrefix trunkPrefix:(NSString *)trunkPrefix;

@end

@implementation PhoneRule

- (BOOL)hasIntlPrefix {
    return (self.flag12 & 0x02);
}

- (BOOL)hasTrunkPrefix {
    return (self.flag12 & 0x01);
}

- (NSString *)format:(NSString *)str intlPrefix:(NSString *)intlPrefix trunkPrefix:(NSString *)trunkPrefix {
    BOOL hadC = NO;
    BOOL hadN = NO;
    BOOL hasOpen = NO;
    int spot = 0;
    NSMutableString *res = [NSMutableString stringWithCapacity:20];
    for (int i = 0; i < [self.format length]; i++) {
        unichar ch = [self.format characterAtIndex:i];
        switch (ch) {
            case 'c':
                // Add international prefix if there is one.
                hadC = YES;
                if (intlPrefix != nil) {
                    [res appendString:intlPrefix];
                }
                break;
            case 'n':
                // Add trunk prefix if there is one.
                hadN = YES;
                if (trunkPrefix != nil) {
                    [res appendString:trunkPrefix];
                }
                break;
            case '#':
                // Add next digit from number. If there aren't enough digits left then do nothing unless we need to
                // space-fill a pair of parenthesis.
                if (spot < [str length]) {
                    [res appendString:[str substringWithRange:NSMakeRange(spot, 1)]];
                    spot++;
                } else if (hasOpen) {
                    [res appendString:@" "];
                }
                break;
            case '(':
                // Flag we found an open paren so it can be space-filled. But only do so if we aren't beyond the
                // end of the number.
                if (spot < [str length]) {
                    hasOpen = YES;
                }
                // fall through
            default: // rest like ) and -
                // Don't show space after n if no trunkPrefix or after c if no intlPrefix
                if (!(ch == ' ' && i > 0 && (([self.format characterAtIndex:i - 1] == 'n' && trunkPrefix == nil) || ([self.format characterAtIndex:i - 1] == 'c' && intlPrefix == nil)))) {
                    // Only show punctuation if not beyond the end of the supplied number.
                    // The only exception is to show a close paren if we had found
                    if (spot < [str length] || (hasOpen && ch == ')')) {
                        [res appendString:[self.format substringWithRange:NSMakeRange(i, 1)]];
                        if (ch == ')') {
                            hasOpen = NO; // close it
                        }
                    }
                }
                break;
        }
    }
    
    // Not all format strings have a 'c' or 'n' in them. If we have an international prefix or a trunk prefix but the
    // format string doesn't explictly say where to put it then simply add it to the beginning.
    if (intlPrefix != nil && !hadC) {
        [res insertString:[NSString stringWithFormat:@"%@ ", intlPrefix] atIndex:0];
    } else if (trunkPrefix != nil && !hadN) {
        [res insertString:trunkPrefix atIndex:0];
    }
    
    return res;
}

- (NSString *)description {
#ifdef DEBUG
    return [NSString stringWithFormat:@"Rule: { countries: %@, calling code: %@, matchlen: %d, minVal: %d, maxVal: %d, byte8: %d, maxLen: %d, nFlag: %d, prefixLen: %d, flag12: %d, flag13: %d, format: %@ }", self.countries, self.callingCode, self.matchLen, self.minVal, self.maxVal, self.byte8, self.maxLen, self.otherFlag, self.prefixLen, self.flag12, self.flag13, self.format];
#else
    return [NSString stringWithFormat:@"Rule: { minVal: %d, maxVal: %d, byte8: %d, maxLen: %d, nFlag: %d, prefixLen: %d, flag12: %d, flag13: %d, format: %@ }", self.minVal, self.maxVal, self.byte8, self.maxLen, self.otherFlag, self.prefixLen, self.flag12, self.flag13, self.format];
#endif
}


@end


@interface RuleSet : NSObject

@property (nonatomic, assign) int matchLen;
@property (nonatomic) NSMutableArray *rules;
@property (nonatomic, assign) BOOL hasRuleWithIntlPrefix;
@property (nonatomic, assign) BOOL hasRuleWithTrunkPrefix;

- (NSString *)format:(NSString *)str intlPrefix:(NSString *)intlPrefix trunkPrefix:(NSString *)trunkPrefix prefixRequired:(BOOL)prefixRequired;

@end

@implementation RuleSet

- (NSString *)format:(NSString *)str intlPrefix:(NSString *)intlPrefix trunkPrefix:(NSString *)trunkPrefix prefixRequired:(BOOL)prefixRequired {
    // First check the number's length against this rule set's match length. If the supplied number is too short then
    // this rule set is ignored.
    if ([str length] >= self.matchLen) {
        // Otherwise we make two passes through the rules in the set. The first pass looks for rules that match the
        // number's prefix and length. It also finds the best rule match based on the prefix flag.
        NSString *begin = [str substringToIndex:self.matchLen];
        int val = [begin intValue];
        for (PhoneRule *rule in self.rules) {
            // Check the rule's range and length against the start of the number
            if (val >= rule.minVal && val <= rule.maxVal && [str length] <= rule.maxLen) {
                if (prefixRequired) {
                    // This pass is trying to find the most restrictive match
                    // A prefix flag of 0 means the format string does not explicitly use the trunk prefix or
                    // international prefix. So only use one of these if the number has no trunk or international prefix.
                    // A prefix flag of 1 means the format string has a reference to the trunk prefix. Only use that
                    // rule if the number has a trunk prefix.
                    // A prefix flag of 2 means the format string has a reference to the international prefix. Only use
                    // that rule if the number has an international prefix.
                    if (((rule.flag12 & 0x03) == 0 && trunkPrefix == nil && intlPrefix == nil) || (trunkPrefix != nil && (rule.flag12 & 0x01)) || (intlPrefix != nil && (rule.flag12 & 0x02))) {
                        return [rule format:str intlPrefix:intlPrefix trunkPrefix:trunkPrefix];
                    }
                } else {
                    // This pass is less restrictive. If this is called it means there was not an exact match based on
                    // prefix flag and any supplied prefix in the number. So now we can use this rule if there is no
                    // prefix regardless of the flag12.
                    if ((trunkPrefix == nil && intlPrefix == nil) || (trunkPrefix != nil && (rule.flag12 & 0x01)) || (intlPrefix != nil && (rule.flag12 & 0x02))) {
                        return [rule format:str intlPrefix:intlPrefix trunkPrefix:trunkPrefix];
                    }
                }
            }
        }
        
        // If we get this far it means the supplied number has either a trunk prefix or an international prefix but
        // none of the rules explictly use that prefix. So now we make one last pass finding a matching rule by totally
        // ignoring the prefix flag.
        if (!prefixRequired) {
            if (intlPrefix != nil) {
                // Strings with intl prefix should use rule with c in it if possible. If not found above then find
                // matching rule with no c.
                for (PhoneRule *rule in self.rules) {
                    if (val >= rule.minVal && val <= rule.maxVal && [str length] <= rule.maxLen) {
                        if (trunkPrefix == nil || (rule.flag12 & 0x01)) {
                            // We found a matching rule.
                            return [rule format:str intlPrefix:intlPrefix trunkPrefix:trunkPrefix];
                        }
                    }
                }
            } else if (trunkPrefix != nil) {
                // Strings with trunk prefix should use rule with n in it if possible. If not found above then find
                // matching rule with no n.
                for (PhoneRule *rule in self.rules) {
                    if (val >= rule.minVal && val <= rule.maxVal && [str length] <= rule.maxLen) {
                        if (intlPrefix == nil || (rule.flag12 & 0x02)) {
                            // We found a matching rule.
                            return [rule format:str intlPrefix:intlPrefix trunkPrefix:trunkPrefix];
                        }
                    }
                }
            }
        }
        
        return nil; // no match found
    } else {
        return nil; // not long enough to compare
    }
}

- (BOOL)isValid:(NSString *)str intlPrefix:(NSString *)intlPrefix trunkPrefix:(NSString *)trunkPrefix prefixRequired:(BOOL)prefixRequired {
    // First check the number's length against this rule set's match length. If the supplied number is the wrong length then
    // this rule set is ignored.
    if ([str length] >= self.matchLen) {
        // Otherwise we make two passes through the rules in the set. The first pass looks for rules that match the
        // number's prefix and length. It also finds the best rule match based on the prefix flag.
        NSString *begin = [str substringToIndex:self.matchLen];
        int val = [begin intValue];
        for (PhoneRule *rule in self.rules) {
            // Check the rule's range and length against the start of the number
            if (val >= rule.minVal && val <= rule.maxVal && [str length] == rule.maxLen) {
                if (prefixRequired) {
                    // This pass is trying to find the most restrictive match
                    // A prefix flag of 0 means the format string does not explicitly use the trunk prefix or
                    // international prefix. So only use one of these if the number has no trunk or international prefix.
                    // A prefix flag of 1 means the format string has a reference to the trunk prefix. Only use that
                    // rule if the number has a trunk prefix.
                    // A prefix flag of 2 means the format string has a reference to the international prefix. Only use
                    // that rule if the number has an international prefix.
                    if (((rule.flag12 & 0x03) == 0 && trunkPrefix == nil && intlPrefix == nil) || (trunkPrefix != nil && (rule.flag12 & 0x01)) || (intlPrefix != nil && (rule.flag12 & 0x02))) {
                        return YES; // full match
                    }
                } else {
                    // This pass is less restrictive. If this is called it means there was not an exact match based on
                    // prefix flag and any supplied prefix in the number. So now we can use this rule if there is no
                    // prefix regardless of the flag12.
                    if ((trunkPrefix == nil && intlPrefix == nil) || (trunkPrefix != nil && (rule.flag12 & 0x01)) || (intlPrefix != nil && (rule.flag12 & 0x02))) {
                        return YES; // full match
                    }
                }
            }
        }
        
        // If we get this far it means the supplied number has either a trunk prefix or an international prefix but
        // none of the rules explictly use that prefix. So now we make one last pass finding a matching rule by totally
        // ignoring the prefix flag.
        if (!prefixRequired) {
            if (intlPrefix != nil && !self.hasRuleWithIntlPrefix) {
                // Strings with intl prefix should use rule with c in it if possible. If not found above then find
                // matching rule with no c.
                for (PhoneRule *rule in self.rules) {
                    if (val >= rule.minVal && val <= rule.maxVal && [str length] == rule.maxLen) {
                        if (trunkPrefix == nil || (rule.flag12 & 0x01)) {
                            // We found a matching rule.
                            return YES;
                        }
                    }
                }
            } else if (trunkPrefix != nil && !self.hasRuleWithTrunkPrefix) {
                // Strings with trunk prefix should use rule with n in it if possible. If not found above then find
                // matching rule with no n.
                for (PhoneRule *rule in self.rules) {
                    if (val >= rule.minVal && val <= rule.maxVal && [str length] == rule.maxLen) {
                        if (intlPrefix == nil || (rule.flag12 & 0x02)) {
                            // We found a matching rule.
                            return YES;
                        }
                    }
                }
            }
        }
        
        return NO; // no match found
    } else {
        return NO; // not the correct length
    }
}

- (NSString *)description {
    NSMutableString *res = [NSMutableString stringWithCapacity:100];
    [res appendFormat:@"RuleSet: { matchLen: %d, rules: %@ }", self.matchLen, self.rules];
    
    return res;
}


@end


@interface CallingCodeInfo : NSObject

@property (nonatomic) NSSet *countries;
@property (nonatomic) NSString *callingCode;
@property (nonatomic) NSMutableArray *trunkPrefixes;
@property (nonatomic) NSMutableArray *intlPrefixes;
@property (nonatomic) NSMutableArray *ruleSets;
@property (nonatomic) NSMutableArray *formatStrings;

- (NSString *)matchingAccessCode:(NSString *)str;
- (NSString *)format:(NSString *)str;

@end

@implementation CallingCodeInfo

- (NSString *)matchingAccessCode:(NSString *)str {
    for (NSString *code in self.intlPrefixes) {
        if ([str hasPrefix:code]) {
            return code;
        }
    }
    
    return nil;
}

- (NSString *)matchingTrunkCode:(NSString *)str {
    for (NSString *code in self.trunkPrefixes) {
        if ([str hasPrefix:code]) {
            return code;
        }
    }
    
    return nil;
}

- (NSString *)format:(NSString *)orig {
    // First see if the number starts with either the country's trunk prefix or international prefix. If so save it
    // off and remove from the number.
    NSString *str = orig;
    NSString *trunkPrefix = nil;
    NSString *intlPrefix = nil;
    if ([str hasPrefix:self.callingCode]) {
        intlPrefix = self.callingCode;
        str = [str substringFromIndex:[intlPrefix length]];
    } else {
        NSString *trunk = [self matchingTrunkCode:str];
        if (trunk) {
            trunkPrefix = trunk;
            str = [str substringFromIndex:[trunkPrefix length]];
        }
    }
    
    // Scan through all sets find best match with no optional prefixes allowed
    for (RuleSet *set in self.ruleSets) {
        NSString *phone = [set format:str intlPrefix:intlPrefix trunkPrefix:trunkPrefix prefixRequired:YES];
        if (phone) {
            return phone;
        }
    }
    
    // No exact matches so now allow for optional prefixes
    for (RuleSet *set in self.ruleSets) {
        NSString *phone = [set format:str intlPrefix:intlPrefix trunkPrefix:trunkPrefix prefixRequired:NO];
        if (phone) {
            return phone;
        }
    }
    
    // No rules matched. If there is an international prefix then display and the rest of the number with a space.
    if (intlPrefix != nil && [str length]) {
        return [NSString stringWithFormat:@"%@ %@", intlPrefix, str];
    }
    
    // Nothing worked so just return the original number as-is.
    return orig;
}

- (BOOL)isValidPhoneNumber:(NSString *)orig {
    // First see if the number starts with either the country's trunk prefix or international prefix. If so save it
    // off and remove from the number.
    NSString *str = orig;
    NSString *trunkPrefix = nil;
    NSString *intlPrefix = nil;
    if ([str hasPrefix:self.callingCode]) {
        intlPrefix = self.callingCode;
        str = [str substringFromIndex:[intlPrefix length]];
    } else {
        NSString *trunk = [self matchingTrunkCode:str];
        if (trunk) {
            trunkPrefix = trunk;
            str = [str substringFromIndex:[trunkPrefix length]];
        }
    }
    
    // Scan through all sets find best match with no optional prefixes allowed
    for (RuleSet *set in self.ruleSets) {
        BOOL valid = [set isValid:str intlPrefix:intlPrefix trunkPrefix:trunkPrefix prefixRequired:YES];
        if (valid) {
            return valid;
        }
    }
    
    // No exact matches so now allow for optional prefixes
    for (RuleSet *set in self.ruleSets) {
        BOOL valid = [set isValid:str intlPrefix:intlPrefix trunkPrefix:trunkPrefix prefixRequired:NO];
        if (valid) {
            return valid;
        }
    }
    
    // The number isn't complete
    return NO;
}

- (NSString *)description {
    NSMutableString *res = [NSMutableString stringWithCapacity:100];
    [res appendFormat:@"CountryInfo { countries: %@, code: %@, trunkPrefixes: %@, intlPrefixes: %@", self.countries, self.callingCode, self.trunkPrefixes, self.intlPrefixes];
    [res appendFormat:@", rule sets: %@ }", self.ruleSets];
    
    return res;
}


@end

static NSCharacterSet *phoneChars = nil;
#ifdef DEBUG
static NSMutableDictionary *extra1CallingCodes = nil;
static NSMutableDictionary *extra2CallingCodes = nil;
static NSMutableDictionary *extra3CallingCodes = nil;
static NSMutableDictionary *flagRules = nil;
#endif

@implementation RMPhoneFormat {
    NSData *_data;
    NSString *_defaultCountry;
    NSString *_defaultCallingCode;
    NSMutableDictionary *_callingCodeOffsets;
    NSMutableDictionary *_callingCodeCountries;
    NSMutableDictionary *_callingCodeData;
    NSMutableDictionary *_countryCallingCode;
}

+ (void)initialize {
    phoneChars = [NSCharacterSet characterSetWithCharactersInString:@"0123456789+*#"];
    
#ifdef DEBUG
    extra1CallingCodes = [[NSMutableDictionary alloc] init];
    extra2CallingCodes = [[NSMutableDictionary alloc] init];
    extra3CallingCodes = [[NSMutableDictionary alloc] init];
    flagRules = [[NSMutableDictionary alloc] init];
#endif
}

+ (NSString *)strip:(NSString *)str {
    NSMutableString *res = [NSMutableString stringWithString:str];
    for (NSInteger i = [res length] - 1; i >= 0; i--) {
        if (![phoneChars characterIsMember:[res characterAtIndex:i]]) {
            [res deleteCharactersInRange:NSMakeRange(i, 1)];
        }
    }
    
    return res;
}

+ (RMPhoneFormat *)instance {
    static RMPhoneFormat *instance = nil;
    static dispatch_once_t predicate = 0;
    
    dispatch_once(&predicate, ^{ instance = [self new]; });
    
    return instance;
}

- (id)init {
    self = [self initWithDefaultCountry:nil];
    
    return self;
}

- (id)initWithDefaultCountry:(NSString *)countryCode {
    if ((self = [super init])) {
        _data = [NSData dataWithContentsOfFile:[[NSBundle bundleWithIdentifier:@"im.actor.ActorSDK"] pathForResource:@"PhoneFormats" ofType:@"dat"]];
        NSAssert(_data, @"The file PhoneFormats.dat is not in the resource bundle. See the README.");
        
        if (countryCode.length) {
            _defaultCountry = countryCode;
        } else {
            NSLocale *loc = [NSLocale currentLocale];
            _defaultCountry = [[loc objectForKey:NSLocaleCountryCode] lowercaseString];
        }
        _callingCodeOffsets = [[NSMutableDictionary alloc] initWithCapacity:255];
        _callingCodeCountries = [[NSMutableDictionary alloc] initWithCapacity:255];
        _callingCodeData = [[NSMutableDictionary alloc] initWithCapacity:10];
        _countryCallingCode = [[NSMutableDictionary alloc] initWithCapacity:255];
        
        [self parseDataHeader];
    }
    
    return self;
}

- (NSString *)defaultCallingCode {
    return [self callingCodeForCountryCode:_defaultCountry];
}

- (NSString *)callingCodeForCountryCode:(NSString *)countryCode {
    return [_countryCallingCode objectForKey:[countryCode lowercaseString]];
}

- (NSSet *)countriesForCallingCode:(NSString *)callingCode {
    if ([callingCode hasPrefix:@"+"]) {
        callingCode = [callingCode substringFromIndex:1];
    }
    
    return [_callingCodeCountries objectForKey:callingCode];
}

- (CallingCodeInfo *)findCallingCodeInfo:(NSString *)str {
    CallingCodeInfo *res = nil;
    for (int i = 0; i < 3; i++) {
        if (i < [str length]) {
            res = [self callingCodeInfo:[str substringToIndex:i + 1]];
            if (res) {
                break;
            }
        } else {
            break;
        }
    }
    
    return res;
}

- (NSString *)format:(NSString *)orig {
    
    if (orig.length == 0)
    {
        return orig;
    }
    // First remove all added punctuation to get just raw phone number characters.
    NSString *str = [RMPhoneFormat strip:orig];
    
    // Phone numbers can be entered by the user in the following formats:
    // 1) +<international prefix><basic number>303
    // 2) <access code><international prefix><basic number>
    // 3) <trunk prefix><basic number>
    // 4) <basic number>
    //
    if ([str hasPrefix:@"+"]) {
        // Handle case 1. Remove the leading '+'.
        NSString *rest = [str substringFromIndex:1];
        // Now find the country that matches the number's international prefix
        CallingCodeInfo *info = [self findCallingCodeInfo:rest];
        if (info) {
            // We found a matching country. Use that info to format the rest of the number.
            NSString *phone = [info format:rest];
            // Put back the leading '+'.
            return [@"+" stringByAppendingString:phone];
        } else {
            // No match so return original number
            return orig;
        }
    } else {
        // Handles cases 2, 3, and 4.
        // Make sure we have info about the user's current region format.
        CallingCodeInfo *info = [self callingCodeInfo:_defaultCallingCode];
        if (info == nil) {
            // No match for the user's locale. No formatting possible.
            return orig;
        }
        
        // See if the entered number begins with an access code valid for the user's region format.
        NSString *accessCode = [info matchingAccessCode:str];
        if (accessCode) {
            // We found a matching access code. This means the rest of the number should be for another country,
            // starting with the other country's international access code.
            // Strip off the access code.
            NSString *rest = [str substringFromIndex:[accessCode length]];
            NSString *phone = rest;
            // Now see if the rest of the number starts with a known international prefix.
            CallingCodeInfo *info2 = [self findCallingCodeInfo:rest];
            if (info2) {
                // We found the other country. Format the number for that country.
                phone = [info2 format:rest];
            }
            
            if ([phone length] == 0) {
                // There is just an access code so far.
                return accessCode;
            } else {
                // We have an access code and a possibly formatted number. Combine with a space between.
                return [NSString stringWithFormat:@"%@ %@", accessCode, phone];
            }
        } else {
            // No access code so we handle cases 3 and 4 and format the number using the user's region format.
            NSString *phone = [info format:str];
            
            return phone;
        }
    }
    
    // All else fails - return the orignal entered number.
    //return orig;
}

- (BOOL)isPhoneNumberValid:(NSString *)phoneNumber {
    if (phoneNumber.length == 0)
    {
        return NO;
    }
    // First remove all added punctuation to get just raw phone number characters.
    NSString *str = [RMPhoneFormat strip:phoneNumber];
    
    // Phone numbers can be entered by the user in the following formats:
    // 1) +<international prefix><basic number>303
    // 2) <access code><international prefix><basic number>
    // 3) <trunk prefix><basic number>
    // 4) <basic number>
    //
    if ([str hasPrefix:@"+"]) {
        // Handle case 1. Remove the leading '+'.
        NSString *rest = [str substringFromIndex:1];
        // Now find the country that matches the number's international prefix
        CallingCodeInfo *info = [self findCallingCodeInfo:rest];
        if (info) {
            // We found a matching country. Use that info to see if the number is complete.
            BOOL valid = [info isValidPhoneNumber:rest];
            
            return valid;
        } else {
            // No matching country code
            return NO;
        }
    } else {
        // Handles cases 2, 3, and 4.
        // Make sure we have info about the user's current region format.
        CallingCodeInfo *info = [self callingCodeInfo:_defaultCallingCode];
        if (info == nil) {
            // No match for the user's locale. No formatting possible.
            return NO;
        }
        
        // See if the entered number begins with an access code valid for the user's region format.
        NSString *accessCode = [info matchingAccessCode:str];
        if (accessCode) {
            // We found a matching access code. This means the rest of the number should be for another country,
            // starting with the other country's international access code.
            // Strip off the access code.
            NSString *rest = [str substringFromIndex:[accessCode length]];
            if (rest.length) {
                // Now see if the rest of the number starts with a know international prefix.
                CallingCodeInfo *info2 = [self findCallingCodeInfo:rest];
                if (info2) {
                    // We found a matching country. Use that info to see if the number is complete.
                    BOOL valid = [info2 isValidPhoneNumber:rest];
                    
                    return valid;
                } else {
                    // No matching country code
                    return NO;
                }
            } else {
                // There is just an access code so far.
                return NO;
            }
        } else {
            // No access code so we handle cases 3 and 4 and validate the number using the user's region format.
            BOOL valid = [info isValidPhoneNumber:str];
            
            return valid;
        }
    }
    
    // All else fails - not a valid phone number.
    return NO;
}

- (uint32_t)value32:(NSUInteger)offset {
    if (offset + 4 <= [_data length]) {
        return OSReadLittleInt32([_data bytes], offset);
    } else {
        return 0;
    }
}

- (uint16_t)value16:(NSUInteger)offset {
    if (offset + 2 <= [_data length]) {
        return OSReadLittleInt16([_data bytes], offset);
    } else {
        return 0;
    }
}

- (int)value16BE:(NSUInteger)offset {
    if (offset + 2 <= [_data length]) {
        return OSReadBigInt16([_data bytes], offset);
    } else {
        return 0;
    }
}

- (CallingCodeInfo *)callingCodeInfo:(NSString *)callingCode {
    CallingCodeInfo *res = [_callingCodeData objectForKey:callingCode];
    if (res == nil) {
        NSNumber *num = [_callingCodeOffsets objectForKey:callingCode];
        if (num) {
            const uint8_t *bytes = [_data bytes];
            NSUInteger start = [num longValue];
            NSUInteger offset = start;
            res = [[CallingCodeInfo alloc] init];
            res.callingCode = callingCode;
            res.countries = [_callingCodeCountries objectForKey:callingCode];
            [_callingCodeData setObject:res forKey:callingCode];
            
            uint16_t block1Len = [self value16:offset];
            offset += 2;
#ifdef DEBUG
            uint16_t extra1 = [self value16:offset];
#endif
            offset += 2;
            uint16_t block2Len = [self value16:offset];
            offset += 2;
#ifdef DEBUG
            uint16_t extra2 = [self value16:offset];
#endif
            offset += 2;
            uint16_t setCnt = [self value16:offset];
            offset += 2;
#ifdef DEBUG
            uint16_t extra3 = [self value16:offset];
#endif
            offset += 2;
            
#ifdef DEBUG
            if (extra1) {
                NSMutableArray *vals = [extra1CallingCodes objectForKey:[NSNumber numberWithInt:extra1]];
                if (!vals) {
                    vals = [[NSMutableArray alloc] init];
                    [extra1CallingCodes setObject:vals forKey:[NSNumber numberWithInt:extra1]];
                }
                [vals addObject:res];
            }
            if (extra2) {
                NSMutableArray *vals = [extra2CallingCodes objectForKey:[NSNumber numberWithInt:extra2]];
                if (!vals) {
                    vals = [[NSMutableArray alloc] init];
                    [extra2CallingCodes setObject:vals forKey:[NSNumber numberWithInt:extra2]];
                }
                [vals addObject:res];
            }
            if (extra3) {
                NSMutableArray *vals = [extra3CallingCodes objectForKey:[NSNumber numberWithInt:extra3]];
                if (!vals) {
                    vals = [[NSMutableArray alloc] init];
                    [extra3CallingCodes setObject:vals forKey:[NSNumber numberWithInt:extra3]];
                }
                [vals addObject:res];
            }
#endif
            
            NSMutableArray *strs = [NSMutableArray arrayWithCapacity:5];
            NSString *str;
            while ([(str = [NSString stringWithCString:(char *)bytes + offset encoding:NSUTF8StringEncoding]) length]) {
                [strs addObject:str];
                offset += [str length] + 1;
            }
            res.trunkPrefixes = strs;
            offset++; // skip NULL
            
            strs = [NSMutableArray arrayWithCapacity:5];
            while ([(str = [NSString stringWithCString:(char *)bytes + offset encoding:NSUTF8StringEncoding]) length]) {
                [strs addObject:str];
                offset += [str length] + 1;
            }
            res.intlPrefixes = strs;
            
            NSMutableArray *ruleSets = [NSMutableArray arrayWithCapacity:setCnt];
            offset = start + block1Len; // Start of rule sets
            for (int s = 0; s < setCnt; s++) {
                RuleSet *ruleSet = [[RuleSet alloc] init];
                int matchCnt = [self value16:offset];
                ruleSet.matchLen = matchCnt;
                offset += 2;
                int ruleCnt = [self value16:offset];
                offset += 2;
                NSMutableArray *rules = [NSMutableArray arrayWithCapacity:ruleCnt];
                for (int r = 0; r < ruleCnt; r++) {
                    PhoneRule *rule = [[PhoneRule alloc] init];
                    rule.minVal = [self value32:offset];
                    offset += 4;
                    rule.maxVal = [self value32:offset];
                    offset += 4;
                    rule.byte8 = (int)bytes[offset++];
                    rule.maxLen = (int)bytes[offset++];
                    rule.otherFlag = (int)bytes[offset++];
                    rule.prefixLen = (int)bytes[offset++];
                    rule.flag12 = (int)bytes[offset++];
                    rule.flag13 = (int)bytes[offset++];
                    uint16_t strOffset = [self value16:offset];
                    offset += 2;
                    rule.format = [NSString stringWithCString:(char *)bytes + start + block1Len + block2Len + strOffset encoding:NSUTF8StringEncoding];
                    // Several formats contain [[9]] or [[8]]. Using the Contacts app as a test, I can find no use
                    // for these. Do they mean "optional"? They don't seem to have any use. This code strips out
                    // anything in [[..]]
                    NSRange openPos = [rule.format rangeOfString:@"[["];
                    if (openPos.location != NSNotFound) {
                        NSRange closePos = [rule.format rangeOfString:@"]]"];
                        rule.format = [NSString stringWithFormat:@"%@%@", [rule.format substringToIndex:openPos.location], [rule.format substringFromIndex:closePos.location + closePos.length]];
                    }
                    
                    [rules addObject:rule];
                    
                    if (rule.hasIntlPrefix) {
                        ruleSet.hasRuleWithIntlPrefix = YES;
                    }
                    if (rule.hasTrunkPrefix) {
                        ruleSet.hasRuleWithTrunkPrefix = YES;
                    }
#ifdef DEBUG
                    rule.countries = res.countries;
                    rule.callingCode = res.callingCode;
                    rule.matchLen = matchCnt;
                    if (rule.byte8) {
                        NSMutableDictionary *data = [flagRules objectForKey:@"byte8"];
                        if (!data) {
                            data = [[NSMutableDictionary alloc] init];
                            [flagRules setObject:data forKey:@"byte8"];
                        }
                        NSMutableArray *list = [data objectForKey:[NSNumber numberWithInt:rule.byte8]];
                        if (!list) {
                            list = [[NSMutableArray alloc] init];
                            [data setObject:list forKey:[NSNumber numberWithInt:rule.byte8]];
                        }
                        
                        [list addObject:rule];
                    }
                    if (rule.prefixLen) {
                        NSMutableDictionary *data = [flagRules objectForKey:@"prefixLen"];
                        if (!data) {
                            data = [[NSMutableDictionary alloc] init];
                            [flagRules setObject:data forKey:@"prefixLen"];
                        }
                        NSMutableArray *list = [data objectForKey:[NSNumber numberWithInt:rule.prefixLen]];
                        if (!list) {
                            list = [[NSMutableArray alloc] init];
                            [data setObject:list forKey:[NSNumber numberWithInt:rule.prefixLen]];
                        }
                        
                        [list addObject:rule];
                    }
                    if (rule.otherFlag) {
                        NSMutableDictionary *data = [flagRules objectForKey:@"otherFlag"];
                        if (!data) {
                            data = [[NSMutableDictionary alloc] init];
                            [flagRules setObject:data forKey:@"otherFlag"];
                        }
                        NSMutableArray *list = [data objectForKey:[NSNumber numberWithInt:rule.otherFlag]];
                        if (!list) {
                            list = [[NSMutableArray alloc] init];
                            [data setObject:list forKey:[NSNumber numberWithInt:rule.otherFlag]];
                        }
                        
                        [list addObject:rule];
                    }
                    if (rule.flag12) {
                        NSMutableDictionary *data = [flagRules objectForKey:@"flag12"];
                        if (!data) {
                            data = [[NSMutableDictionary alloc] init];
                            [flagRules setObject:data forKey:@"flag12"];
                        }
                        NSMutableArray *list = [data objectForKey:[NSNumber numberWithInt:rule.flag12]];
                        if (!list) {
                            list = [[NSMutableArray alloc] init];
                            [data setObject:list forKey:[NSNumber numberWithInt:rule.flag12]];
                        }
                        
                        [list addObject:rule];
                    }
                    if (rule.flag13) {
                        NSMutableDictionary *data = [flagRules objectForKey:@"flag13"];
                        if (!data) {
                            data = [[NSMutableDictionary alloc] init];
                            [flagRules setObject:data forKey:@"flag13"];
                        }
                        NSMutableArray *list = [data objectForKey:[NSNumber numberWithInt:rule.flag13]];
                        if (!list) {
                            list = [[NSMutableArray alloc] init];
                            [data setObject:list forKey:[NSNumber numberWithInt:rule.flag13]];
                        }
                        
                        [list addObject:rule];
                    }
#endif
                }
                ruleSet.rules = rules;
                [ruleSets addObject:ruleSet];
            }
            res.ruleSets = ruleSets;
        }
    }
    
    return res;
}

- (void)parseDataHeader {
    int count = [self value32:0];
    uint32_t base = count * 12 + 4;
    const void *bytes = [_data bytes];
    NSUInteger spot = 4;
    for (int i = 0; i < count; i++) {
        NSString *callingCode = [NSString stringWithCString:bytes + spot encoding:NSUTF8StringEncoding];
        spot += 4;
        NSString *country = [NSString stringWithCString:bytes + spot encoding:NSUTF8StringEncoding];
        spot += 4;
        uint32_t offset = [self value32:spot] + base;
        spot += 4;
        
        if ([country isEqualToString:_defaultCountry]) {
            _defaultCallingCode = callingCode;
        }
        
        [_countryCallingCode setObject:callingCode forKey:country];
        
        [_callingCodeOffsets setObject:[NSNumber numberWithLong:offset] forKey:callingCode];
        NSMutableSet *countries = [_callingCodeCountries objectForKey:callingCode];
        if (!countries) {
            countries = [[NSMutableSet alloc] init];
            [_callingCodeCountries setObject:countries forKey:callingCode];
        }
        [countries addObject:country];
    }
    
    if (_defaultCallingCode) {
        [self callingCodeInfo:_defaultCallingCode];
    }
}

#ifdef DEBUG
- (void)dump {
    NSArray *callingCodes = [[_callingCodeOffsets allKeys] sortedArrayUsingSelector:@selector(compare:)];
    for (NSString *callingCode in callingCodes) {
        CallingCodeInfo *info = [self callingCodeInfo:callingCode];
        NSLog(@"%@", info);
    }
    
    NSLog(@"flagRules: %@", flagRules);
    NSLog(@"extra1 calling codes: %@", extra1CallingCodes);
    NSLog(@"extra2 calling codes: %@", extra2CallingCodes);
    NSLog(@"extra3 calling codes: %@", extra3CallingCodes);
}
#endif


@end