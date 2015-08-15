//
//  Copyright (c) 2014-2015 Actor LLC. <https://actor.im>
//

#import <CoreTelephony/CTTelephonyNetworkInfo.h>
#import <CoreTelephony/CTCarrier.h>
#import <RMPhoneFormat/RMPhoneFormat.h>
#import "ABPhoneField.h"

@interface ABPhoneField () <UITextFieldDelegate>

@property (nonatomic, strong) RMPhoneFormat *phoneFormatter;
@property (nonatomic, strong) NSString *localIso;

@end

@implementation ABPhoneField

- (void)setCurrentIso:(NSString *)currentIso
{
    _currentIso = currentIso.lowercaseString;
    self.phoneFormatter = [[RMPhoneFormat alloc] initWithDefaultCountry:_currentIso];
}

#pragma mark - Text Field

- (void)phoneChanged:(UITextField *)textField
{
    if (textField.text.length == 0)
        return;
    if ([textField.text rangeOfString:@"("].location != NSNotFound &&
        [textField.text rangeOfString:@")"].location == NSNotFound)
    {
        while ([textField.text characterAtIndex:textField.text.length-1] == ' ')
            textField.text = [textField.text substringToIndex:textField.text.length-1];
        textField.text = [textField.text substringToIndex:textField.text.length-1];
    }
    
    NSInteger rightCountDigits = [self countDigitsRighterThatCursor:textField];
    NSString *number = [[self phoneNumber] substringFromIndex:self.phoneFormatter.defaultCallingCode.length];
    textField.text = [[self.phoneFormatter format:number] stringByTrimmingCharactersInSet:[NSCharacterSet whitespaceAndNewlineCharacterSet]];
    [self setCursor:textField whereHaveDigitsFromRight:rightCountDigits];
}

- (NSInteger)countDigitsRighterThatCursor:(UITextField *)textField
{
    if (textField.selectedTextRange == nil)
        return 0;
    
    UITextPosition *cursorPos = textField.selectedTextRange.start;
    NSInteger digits = -1;
    for (NSInteger i = 0; i < textField.text.length; i++) {
        unichar ch = [textField.text characterAtIndex:i];
        if (digits == -1 && cursorPos == [textField positionFromPosition:textField.beginningOfDocument offset:i])
            digits = 0;
        if (digits >= 0 && [[NSCharacterSet decimalDigitCharacterSet] characterIsMember:ch])
            digits++;
    }
    
    return MAX(0,digits);
}

- (void)setCursor:(UITextField *)textField whereHaveDigitsFromRight:(NSInteger)digits
{
    NSInteger digitsSkipped = 0;
    for (NSInteger i = 0; i < textField.text.length; i++) {
        unichar ch = [textField.text characterAtIndex:textField.text.length-1-i];
        if (digitsSkipped == digits) {
            UITextPosition *pos = [textField positionFromPosition:textField.endOfDocument inDirection:UITextLayoutDirectionLeft offset:i];
            textField.selectedTextRange = [textField textRangeFromPosition:pos toPosition:pos];
            return;
        }
        if ([[NSCharacterSet decimalDigitCharacterSet] characterIsMember:ch])
            digitsSkipped++;
    }
}

- (NSString *)phoneNumber
{
    NSArray *arr = [[self.phoneFormatter.defaultCallingCode stringByAppendingString:self.text] componentsSeparatedByCharactersInSet:[NSCharacterSet decimalDigitCharacterSet].invertedSet];
    return [arr componentsJoinedByString:@""];
}

- (NSString *)formattedPhoneNumber
{
    return [self.phoneFormatter format:[self phoneNumber]];
}

#pragma mark - View

+ (NSArray *)linesOfResource
{
    static NSArray *lines = nil;
    if (lines == nil) {
        lines = [[NSString stringWithContentsOfFile:[[NSBundle mainBundle] pathForResource:@"ABPhoneFieldCodes" ofType:@"txt"] encoding:NSUTF8StringEncoding error: nil] componentsSeparatedByString:@"\n"];
    }
    return lines;
}

+ (NSDictionary *)callingCodeByCountryCode
{
    static NSMutableDictionary *codes = nil;
    if (codes == nil) {
        codes = [NSMutableDictionary dictionary];
        for (NSString *line in [self linesOfResource]) {
            NSArray *tokens = [line componentsSeparatedByString:@";"];
            NSString *key = [tokens[1] lowercaseString];
            codes[key] = tokens[0];
        }
    }
    return codes;
}

+ (NSDictionary *)countryNameByCountryCode
{
    static NSMutableDictionary *names = nil;
    if (names == nil) {
        names = [NSMutableDictionary dictionary];
        for (NSString *line in [self linesOfResource]) {
            NSArray *tokens = [line componentsSeparatedByString:@";"];
            NSString *key = [tokens[1] lowercaseString];
            names[key] = tokens[2];
        }
    }
    return names;
}

+ (NSDictionary *)phoneMinLengthByCountryCode
{
    static NSMutableDictionary *minLengths = nil;
    if (minLengths == nil) {
        minLengths = [NSMutableDictionary dictionary];
        for (NSString *line in [self linesOfResource]) {
            NSArray *tokens = [line componentsSeparatedByString:@";"];
            NSString *key = [tokens[1] lowercaseString];
            minLengths[key] = (tokens.count >= 5) ? tokens[4] : @"5";
        }
    }
    return minLengths;
}

+ (NSArray *)sortedIsoCodes
{
    static NSMutableArray *isoCodes = nil;
    if (isoCodes == nil) {
        isoCodes = [NSMutableArray array];
        for (NSString *line in [self linesOfResource]) {
            NSArray *tokens = [line componentsSeparatedByString:@";"];
            NSString *key = [tokens[1] lowercaseString];
            [isoCodes addObject:key];
        }
        [isoCodes sortUsingSelector:@selector(compare:)];
    }
    return isoCodes;
}

- (NSString *)isoFromCarrier
{
    if (NSClassFromString(@"CTTelephonyNetworkInfo") == nil)
        return nil;
    CTTelephonyNetworkInfo *networkInfo = [[CTTelephonyNetworkInfo alloc] init];
    CTCarrier *carrier = networkInfo.subscriberCellularProvider;
    return carrier.isoCountryCode.lowercaseString;
}

- (void)awakeFromNib
{
    [super awakeFromNib];
    [self setup];
}

- (id)init {
    self = [super init];
    if (self) {
        [self setup];
    }
    return self;
}

- (id)initWithFrame:(CGRect)frame {
    self = [super initWithFrame:frame];
    if (self) {
        [self setup];
    }
    return self;
}

#pragma mark -
#pragma mark Setup

- (void)setup {
    [self addTarget:self action:@selector(phoneChanged:) forControlEvents:(UIControlEventEditingChanged)];
    
    self.localIso = [self isoFromCarrier];
    if (self.localIso.length == 0)
        self.localIso = [[NSLocale currentLocale] objectForKey:NSLocaleCountryCode];
    if (self.localIso.length == 0)
        self.localIso = @"en8";
    self.currentIso = self.localIso;
}

@end
