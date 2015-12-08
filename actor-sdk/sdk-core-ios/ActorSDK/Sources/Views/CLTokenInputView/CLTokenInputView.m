//
//  CLTokenInputView.m
//  CLTokenInputView
//
//  Created by Rizwan Sattar on 2/24/14.
//  Copyright (c) 2014 Cluster Labs, Inc. All rights reserved.
//

#import "CLTokenInputView.h"

#import "CLBackspaceDetectingTextField.h"
#import "CLTokenView.h"

static CGFloat const HSPACE = 0.0;
static CGFloat const TEXT_FIELD_HSPACE = 4.0; // Note: Same as CLTokenView.PADDING_X
static CGFloat const VSPACE = 4.0;
static CGFloat const MINIMUM_TEXTFIELD_WIDTH = 56.0;
static CGFloat const PADDING_TOP = 10.0;
static CGFloat const PADDING_BOTTOM = 10.0;
static CGFloat const PADDING_LEFT = 8.0;
static CGFloat const PADDING_RIGHT = 16.0;
static CGFloat const STANDARD_ROW_HEIGHT = 25.0;

static CGFloat const FIELD_MARGIN_X = 4.0; // Note: Same as CLTokenView.PADDING_X

@interface CLTokenInputView () <CLBackspaceDetectingTextFieldDelegate, CLTokenViewDelegate>

@property (strong, nonatomic) CL_GENERIC_MUTABLE_ARRAY(CLToken *) *tokens;
@property (strong, nonatomic) CL_GENERIC_MUTABLE_ARRAY(CLTokenView *) *tokenViews;
@property (strong, nonatomic) CLBackspaceDetectingTextField *textField;
@property (strong, nonatomic) UILabel *fieldLabel;


@property (assign, nonatomic) CGFloat intrinsicContentHeight;
@property (assign, nonatomic) CGFloat additionalTextFieldYOffset;

@end

@implementation CLTokenInputView

- (void)commonInit
{
    self.textField = [[CLBackspaceDetectingTextField alloc] initWithFrame:self.bounds];
    self.textField.backgroundColor = [UIColor clearColor];
    self.textField.keyboardType = UIKeyboardTypeEmailAddress;
    self.textField.autocorrectionType = UITextAutocorrectionTypeNo;
    self.textField.autocapitalizationType = UITextAutocapitalizationTypeNone;
    self.textField.delegate = self;
    self.additionalTextFieldYOffset = 0.0;
    if (![self.textField respondsToSelector:@selector(defaultTextAttributes)]) {
        self.additionalTextFieldYOffset = 1.5;
    }
    [self.textField addTarget:self
                       action:@selector(onTextFieldDidChange:)
             forControlEvents:UIControlEventEditingChanged];
    [self addSubview:self.textField];

    self.tokens = [NSMutableArray arrayWithCapacity:20];
    self.tokenViews = [NSMutableArray arrayWithCapacity:20];

    self.fieldColor = [UIColor lightGrayColor]; 
    self.fieldTextColor = [UIColor blackColor];
    self.textField.textColor = self.fieldTextColor;
    self.fieldLabel = [[UILabel alloc] initWithFrame:CGRectZero];
    self.fieldLabel.font = self.textField.font;
    self.fieldLabel.textColor = self.fieldColor;
    [self addSubview:self.fieldLabel];
    self.fieldLabel.hidden = YES;

    self.intrinsicContentHeight = STANDARD_ROW_HEIGHT;
    [self repositionViews];
}

- (id)initWithFrame:(CGRect)frame
{
    self = [super initWithFrame:frame];
    if (self) {
        [self commonInit];
    }
    return self;
}

- (id)initWithCoder:(NSCoder *)aDecoder
{
    self = [super initWithCoder:aDecoder];
    if (self) {
        [self commonInit];
    }
    return self;
}

- (CGSize)intrinsicContentSize
{
    return CGSizeMake(UIViewNoIntrinsicMetric, MAX(45, self.intrinsicContentHeight));
}


#pragma mark - Tint color


- (void)tintColorDidChange
{
    for (UIView *tokenView in self.tokenViews) {
        tokenView.tintColor = self.tintColor;
    }
}


#pragma mark - Adding / Removing Tokens

- (void)addToken:(CLToken *)token
{
    if ([self.tokens containsObject:token]) {
        return;
    }

    [self.tokens addObject:token];
    CLTokenView *tokenView = [[CLTokenView alloc] initWithToken:token font:self.textField.font];
    if ([self respondsToSelector:@selector(tintColor)]) {
        tokenView.tintColor = self.tintColor;
    }
    tokenView.delegate = self;
    CGSize intrinsicSize = tokenView.intrinsicContentSize;
    tokenView.frame = CGRectMake(0, 0, intrinsicSize.width, intrinsicSize.height);
    [self.tokenViews addObject:tokenView];
    [self addSubview:tokenView];
    self.textField.text = @"";
    if ([self.delegate respondsToSelector:@selector(tokenInputView:didAddToken:)]) {
        [self.delegate tokenInputView:self didAddToken:token];
    }

    // Clearing text programmatically doesn't call this automatically
    [self onTextFieldDidChange:self.textField];

    [self updatePlaceholderTextVisibility];
    [self repositionViews];
}

- (void)removeToken:(CLToken *)token
{
    NSInteger index = [self.tokens indexOfObject:token];
    if (index == NSNotFound) {
        return;
    }
    [self removeTokenAtIndex:index];
}

- (void)removeTokenAtIndex:(NSInteger)index
{
    if (index == NSNotFound) {
        return;
    }
    CLTokenView *tokenView = self.tokenViews[index];
    [tokenView removeFromSuperview];
    [self.tokenViews removeObjectAtIndex:index];
    CLToken *removedToken = self.tokens[index];
    [self.tokens removeObjectAtIndex:index];
    if ([self.delegate respondsToSelector:@selector(tokenInputView:didRemoveToken:)]) {
        [self.delegate tokenInputView:self didRemoveToken:removedToken];
    }
    [self updatePlaceholderTextVisibility];
    [self repositionViews];
}

- (NSArray *)allTokens
{
    return [self.tokens copy];
}

- (CLToken *)tokenizeTextfieldText
{
    CLToken *token = nil;
    NSString *text = self.textField.text;
    if (text.length > 0 &&
        [self.delegate respondsToSelector:@selector(tokenInputView:tokenForText:)]) {
        token = [self.delegate tokenInputView:self tokenForText:text];
        if (token != nil) {
            [self addToken:token];
            self.textField.text = @"";
            [self onTextFieldDidChange:self.textField];
        }
    }
    return token;
}


#pragma mark - Updating/Repositioning Views

- (void)repositionViews
{
    CGRect bounds = self.bounds;
    CGFloat rightBoundary = CGRectGetWidth(bounds) - PADDING_RIGHT;
    CGFloat firstLineRightBoundary = rightBoundary;

    CGFloat curX = PADDING_LEFT;
    CGFloat curY = PADDING_TOP;
    CGFloat totalHeight = STANDARD_ROW_HEIGHT;
    BOOL isOnFirstLine = YES;

    // Position field view (if set)
    if (self.fieldView) {
        CGRect fieldViewRect = self.fieldView.frame;
        fieldViewRect.origin.x = curX + FIELD_MARGIN_X;
        fieldViewRect.origin.y = curY + ((STANDARD_ROW_HEIGHT - CGRectGetHeight(fieldViewRect))/2.0);
        self.fieldView.frame = fieldViewRect;

        curX = CGRectGetMaxX(fieldViewRect) + FIELD_MARGIN_X;
    }

    // Position field label (if field name is set)
    if (!self.fieldLabel.hidden) {
        CGRect fieldLabelRect = self.fieldLabel.frame;
        fieldLabelRect.origin.x = curX + FIELD_MARGIN_X;
        fieldLabelRect.origin.y = curY + ((STANDARD_ROW_HEIGHT-CGRectGetHeight(fieldLabelRect))/2.0);
        self.fieldLabel.frame = fieldLabelRect;

        curX = CGRectGetMaxX(fieldLabelRect) + FIELD_MARGIN_X;
    }

    // Position accessory view (if set)
    if (self.accessoryView) {
        CGRect accessoryRect = self.accessoryView.frame;
        accessoryRect.origin.x = CGRectGetWidth(bounds) - PADDING_RIGHT - CGRectGetWidth(accessoryRect);
        accessoryRect.origin.y = curY;
        self.accessoryView.frame = accessoryRect;

        firstLineRightBoundary = CGRectGetMinX(accessoryRect) - HSPACE;
    }

    // Position token views
    CGRect tokenRect = CGRectNull;
    for (UIView *tokenView in self.tokenViews) {
        tokenRect = tokenView.frame;

        CGFloat tokenBoundary = isOnFirstLine ? firstLineRightBoundary : rightBoundary;
        if (curX + CGRectGetWidth(tokenRect) > tokenBoundary) {
            // Need a new line
            curX = PADDING_LEFT;
            curY += STANDARD_ROW_HEIGHT+VSPACE;
            totalHeight += STANDARD_ROW_HEIGHT;
            isOnFirstLine = NO;
        }

        tokenRect.origin.x = curX;
        // Center our tokenView vertially within STANDARD_ROW_HEIGHT
        tokenRect.origin.y = curY + ((STANDARD_ROW_HEIGHT-CGRectGetHeight(tokenRect))/2.0);
        tokenView.frame = tokenRect;

        curX = CGRectGetMaxX(tokenRect) + HSPACE;
    }

    // Always indent textfield by a little bit
    curX += TEXT_FIELD_HSPACE;
    CGFloat textBoundary = isOnFirstLine ? firstLineRightBoundary : rightBoundary;
    CGFloat availableWidthForTextField = textBoundary - curX;
    if (availableWidthForTextField < MINIMUM_TEXTFIELD_WIDTH) {
        isOnFirstLine = NO;
        curX = PADDING_LEFT + TEXT_FIELD_HSPACE;
        curY += STANDARD_ROW_HEIGHT+VSPACE;
        totalHeight += STANDARD_ROW_HEIGHT;
        // Adjust the width
        availableWidthForTextField = rightBoundary - curX;
    }

    CGRect textFieldRect = self.textField.frame;
    textFieldRect.origin.x = curX;
    textFieldRect.origin.y = curY + self.additionalTextFieldYOffset;
    textFieldRect.size.width = availableWidthForTextField;
    textFieldRect.size.height = STANDARD_ROW_HEIGHT;
    self.textField.frame = textFieldRect;

    CGFloat oldContentHeight = self.intrinsicContentHeight;
    self.intrinsicContentHeight = CGRectGetMaxY(textFieldRect)+PADDING_BOTTOM;
    [self invalidateIntrinsicContentSize];

    if (oldContentHeight != self.intrinsicContentHeight) {
        if ([self.delegate respondsToSelector:@selector(tokenInputView:didChangeHeightTo:)]) {
            [self.delegate tokenInputView:self didChangeHeightTo:self.intrinsicContentSize.height];
        }
    }
    [self setNeedsDisplay];
}

- (void)updatePlaceholderTextVisibility
{
    if (self.tokens.count > 0) {
        self.textField.placeholder = nil;
    } else {
        if (self.placeholderAttributedText != nil) {
            self.textField.attributedPlaceholder = self.placeholderAttributedText;
        } else {
            self.textField.placeholder = self.placeholderText;
        }
    }
}


- (void)layoutSubviews
{
    [super layoutSubviews];
    [self repositionViews];
}


#pragma mark - CLBackspaceDetectingTextFieldDelegate

- (void)textFieldDidDeleteBackwards:(UITextField *)textField
{
    // Delay selecting the next token slightly, so that on iOS 8
    // the deleteBackward on CLTokenView is not called immediately,
    // causing a double-delete
    dispatch_async(dispatch_get_main_queue(), ^{
        if (textField.text.length == 0) {
            CLTokenView *tokenView = self.tokenViews.lastObject;
            if (tokenView) {
                [self selectTokenView:tokenView animated:YES];
                [self.textField resignFirstResponder];
            }
        }
    });
}


#pragma mark - UITextFieldDelegate

- (void)textFieldDidBeginEditing:(UITextField *)textField
{
    if ([self.delegate respondsToSelector:@selector(tokenInputViewDidBeginEditing:)]) {
        [self.delegate tokenInputViewDidBeginEditing:self];
    }
    
    [self unselectAllTokenViewsAnimated:YES];
}

- (void)textFieldDidEndEditing:(UITextField *)textField
{
    if ([self.delegate respondsToSelector:@selector(tokenInputViewDidEndEditing:)]) {
        [self.delegate tokenInputViewDidEndEditing:self];
    }
}

- (BOOL)textFieldShouldReturn:(UITextField *)textField
{
    [self tokenizeTextfieldText];
    return NO;
}


#pragma mark - Text Field Changes

- (void)onTextFieldDidChange:(id)sender
{
    if ([self.delegate respondsToSelector:@selector(tokenInputView:didChangeText:)]) {
        [self.delegate tokenInputView:self didChangeText:self.textField.text];
    }
}


#pragma mark - Text Field Customization

- (void)setKeyboardType:(UIKeyboardType)keyboardType
{
    _keyboardType = keyboardType;
    self.textField.keyboardType = _keyboardType;
}

- (void)setAutocapitalizationType:(UITextAutocapitalizationType)autocapitalizationType
{
    _autocapitalizationType = autocapitalizationType;
    self.textField.autocapitalizationType = _autocapitalizationType;
}

- (void)setAutocorrectionType:(UITextAutocorrectionType)autocorrectionType
{
    _autocorrectionType = autocorrectionType;
    self.textField.autocorrectionType = _autocorrectionType;
}


#pragma mark - Measurements (text field offset, etc.)

- (CGFloat)textFieldDisplayOffset
{
    // Essentially the textfield's y with PADDING_TOP
    return CGRectGetMinY(self.textField.frame) - PADDING_TOP;
}


#pragma mark - Textfield text


- (NSString *)text
{
    return self.textField.text;
}


#pragma mark - CLTokenViewDelegate

- (void)tokenViewDidRequestDelete:(CLTokenView *)tokenView replaceWithText:(NSString *)replacementText
{
    // First, refocus the text field
    [self.textField becomeFirstResponder];
    if (replacementText.length > 0) {
        self.textField.text = replacementText;
    }
    // Then remove the view from our data
    NSInteger index = [self.tokenViews indexOfObject:tokenView];
    if (index == NSNotFound) {
        return;
    }
    [self removeTokenAtIndex:index];
}

- (void)tokenViewDidRequestSelection:(CLTokenView *)tokenView
{
    [self selectTokenView:tokenView animated:YES];
}


#pragma mark - Token selection

- (void)selectTokenView:(CLTokenView *)tokenView animated:(BOOL)animated
{
    [tokenView setSelected:YES animated:animated];
    for (CLTokenView *otherTokenView in self.tokenViews) {
        if (otherTokenView != tokenView) {
            [otherTokenView setSelected:NO animated:animated];
        }
    }
}

- (void)unselectAllTokenViewsAnimated:(BOOL)animated
{
    for (CLTokenView *tokenView in self.tokenViews) {
        [tokenView setSelected:NO animated:animated];
    }
}


#pragma mark - Editing

- (BOOL)isEditing
{
    return self.textField.editing;
}


- (void)beginEditing
{
    [self.textField becomeFirstResponder];
    [self unselectAllTokenViewsAnimated:NO];
}


- (void)endEditing
{
    // NOTE: We used to check if .isFirstResponder
    // and then resign first responder, but sometimes
    // we noticed that it would be the first responder,
    // but still return isFirstResponder=NO. So always
    // attempt to resign without checking.
    [self.textField resignFirstResponder];
}


#pragma mark - (Optional Views)

- (void)setFieldName:(NSString *)fieldName
{
    if (_fieldName == fieldName) {
        return;
    }
    NSString *oldFieldName = _fieldName;
    _fieldName = fieldName;

    self.fieldLabel.text = _fieldName;
    [self.fieldLabel sizeToFit];
    BOOL showField = (_fieldName.length > 0);
    self.fieldLabel.hidden = !showField;
    if (showField && !self.fieldLabel.superview) {
        [self addSubview:self.fieldLabel];
    } else if (!showField && self.fieldLabel.superview) {
        [self.fieldLabel removeFromSuperview];
    }

    if (oldFieldName == nil || ![oldFieldName isEqualToString:fieldName]) {
        [self repositionViews];
    }
}

- (void)setFieldColor:(UIColor *)fieldColor {
    _fieldColor = fieldColor;
    self.fieldLabel.textColor = _fieldColor;
}

- (void)setFieldTextColor:(UIColor *)fieldTextColor {
    _fieldTextColor = fieldTextColor;
    self.textField.textColor = _fieldTextColor;
}

- (void)setFieldView:(UIView *)fieldView
{
    if (_fieldView == fieldView) {
        return;
    }
    [_fieldView removeFromSuperview];
    _fieldView = fieldView;
    if (_fieldView != nil) {
        [self addSubview:_fieldView];
    }
    [self repositionViews];
}

- (void)setPlaceholderText:(NSString *)placeholderText
{
    if (_placeholderText == placeholderText) {
        return;
    }
    _placeholderText = placeholderText;
    [self updatePlaceholderTextVisibility];
}

- (void)setPlaceholderAttributedText:(NSAttributedString *)placeholderAttributedText
{
    if (_placeholderAttributedText == placeholderAttributedText) {
        return;
    }
    _placeholderAttributedText = placeholderAttributedText;
    [self updatePlaceholderTextVisibility];
}

- (void)setAccessoryView:(UIView *)accessoryView
{
    if (_accessoryView == accessoryView) {
        return;
    }
    [_accessoryView removeFromSuperview];
    _accessoryView = accessoryView;

    if (_accessoryView != nil) {
        [self addSubview:_accessoryView];
    }
    [self repositionViews];
}


#pragma mark - Drawing

- (void)setDrawBottomBorder:(BOOL)drawBottomBorder
{
    if (_drawBottomBorder == drawBottomBorder) {
        return;
    }
    _drawBottomBorder = drawBottomBorder;
    [self setNeedsDisplay];
}


// Only override drawRect: if you perform custom drawing.
// An empty implementation adversely affects performance during animation.
- (void)drawRect:(CGRect)rect
{
    [super drawRect:rect];
    if (self.drawBottomBorder) {

        CGContextRef context = UIGraphicsGetCurrentContext();
        CGRect bounds = self.bounds;
        CGContextSetStrokeColorWithColor(context, [UIColor lightGrayColor].CGColor);
        CGContextSetLineWidth(context, 0.5);

        CGContextMoveToPoint(context, 0, bounds.size.height);
        CGContextAddLineToPoint(context, CGRectGetWidth(bounds), bounds.size.height);
        CGContextStrokePath(context);
    }
}

@end
