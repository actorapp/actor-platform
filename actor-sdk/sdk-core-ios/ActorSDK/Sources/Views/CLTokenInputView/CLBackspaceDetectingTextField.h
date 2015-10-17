//
//  CLBackspaceDetectingTextField.h
//  CLTokenInputView
//
//  Created by Rizwan Sattar on 2/24/14.
//  Copyright (c) 2014 Cluster Labs, Inc. All rights reserved.
//

#import <UIKit/UIKit.h>

NS_ASSUME_NONNULL_BEGIN

@class CLBackspaceDetectingTextField;
@protocol CLBackspaceDetectingTextFieldDelegate <UITextFieldDelegate>

- (void)textFieldDidDeleteBackwards:(UITextField *)textField;

@end

/**
 * CLBackspaceDetectingTextField is a very simple subclass
 * of UITextField that adds an extra delegate method to 
 * notify whenever the backspace key is pressed. Without
 * this delegate method, it is not possible to detect
 * if the backspace key is pressed while the textfield is 
 * empty.
 *
 * @since v1.0
 */
@interface CLBackspaceDetectingTextField : UITextField <UIKeyInput>

@property (weak, nonatomic, nullable) NSObject <CLBackspaceDetectingTextFieldDelegate> *delegate;

@end

NS_ASSUME_NONNULL_END
