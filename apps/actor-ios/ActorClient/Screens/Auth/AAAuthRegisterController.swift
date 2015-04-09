//
//  AAAuthRegisterController.swift
//  ActorClient
//
//  Created by Danil Gontovnik on 3/31/15.
//  Copyright (c) 2015 Actor LLC. All rights reserved.
//

import UIKit

class AAAuthRegisterController: AAViewController {
    
    // MARK: -
    // MARK: Private vars
    
    private var grayBackground: UIView!
    private var titleLabel: UILabel!
    
    private var addPhotoButton: UIButton!
    private var avatarImageView: UIImageView!
    
    private var firstNameField: UITextField!
    private var lastNameField: UITextField!
    
    private var hintLabel: UILabel!
    
    // MARK: -
    
    override func loadView() {
        super.loadView()
        
        view.backgroundColor = UIColor.whiteColor()
        
        let screenSize = UIScreen.mainScreen().bounds.size
        let isWidescreen = screenSize.width > 320 || screenSize.height > 480
        
        grayBackground = UIView(frame: CGRect(x: 0.0, y: 0.0, width: screenSize.width, height: isWidescreen ? 131.0 : 90.0))
        grayBackground.backgroundColor = UIColor.RGB(0xf2f2f2)
        view.addSubview(grayBackground)
        
        titleLabel = UILabel()
        titleLabel.backgroundColor = UIColor.clearColor()
        titleLabel.textColor = UIColor.blackColor()
        titleLabel.textAlignment = NSTextAlignment.Center
        titleLabel.font = UIFont(name: "HelveticaNeue-Light", size: 30.0)
        titleLabel.text = "Your Info" // TODO: Localize
        titleLabel.sizeToFit()
        titleLabel.frame = CGRect(x: (screenSize.width - titleLabel.frame.size.width) / 2.0, y: isWidescreen ? 71.0 : 48.0, width: titleLabel.frame.size.width, height: titleLabel.frame.size.height)
        grayBackground.addSubview(titleLabel)
        
        let separatorHeight: CGFloat = Utils.isRetina() ? 0.5 : 1.0
        var navigationBarSeparator = UIView(frame: CGRect(x: 0.0, y: grayBackground.bounds.size.height, width: screenSize.width, height: separatorHeight))
        navigationBarSeparator.backgroundColor = UIColor.RGB(0xc8c7cc)
        view.addSubview(navigationBarSeparator)
        
        UIGraphicsBeginImageContextWithOptions(CGSize(width: 110, height: 110), false, 0.0);
        var context = UIGraphicsGetCurrentContext();
        CGContextSetFillColorWithColor(context, UIColor.whiteColor().CGColor);
        CGContextFillEllipseInRect(context, CGRectMake(0.0, 0.0, 110.0, 110.0));
        CGContextSetStrokeColorWithColor(context, UIColor.RGB(0xd9d9d9).CGColor);
        CGContextSetLineWidth(context, 1.0);
        CGContextStrokeEllipseInRect(context, CGRectMake(0.5, 0.5, 109.0, 109.0));
        let buttonImage = UIGraphicsGetImageFromCurrentImageContext();
        UIGraphicsEndImageContext();
        
        addPhotoButton = UIButton(frame: CGRect(x: 10, y: navigationBarSeparator.frame.origin.y + 11, width: buttonImage.size.width, height: buttonImage.size.height))
        addPhotoButton.exclusiveTouch = true
        addPhotoButton.setBackgroundImage(buttonImage, forState: UIControlState.Normal)
        addPhotoButton.addTarget(self, action: Selector("askAddPhoto"), forControlEvents: UIControlEvents.TouchUpInside)
        view.addSubview(addPhotoButton)
        
        avatarImageView = UIImageView(frame: CGRect(x: 10, y: navigationBarSeparator.frame.origin.y + 11, width: 110, height: 110))
        avatarImageView.hidden = true;
        avatarImageView.userInteractionEnabled = true
        avatarImageView.layer.cornerRadius = avatarImageView.frame.size.width / 2.0
        avatarImageView.layer.masksToBounds = true
        avatarImageView.layer.shouldRasterize = true
        avatarImageView.layer.rasterizationScale = UIScreen.mainScreen().scale
        avatarImageView.contentMode = UIViewContentMode.Center
        avatarImageView.addGestureRecognizer(UITapGestureRecognizer(target: self, action: Selector("askChangePhoto")))
        view.addSubview(avatarImageView)
        
        var addPhotoLabelFirst = UILabel()
        addPhotoLabelFirst.text = "add"
        addPhotoLabelFirst.font = UIFont.systemFontOfSize(15.0)
        addPhotoLabelFirst.backgroundColor = UIColor.clearColor()
        addPhotoLabelFirst.textColor = UIColor.RGB(0xd9d9d9)
        addPhotoLabelFirst.sizeToFit()
        
        var addPhotoLabelSecond = UILabel()
        addPhotoLabelSecond.text = "photo"
        addPhotoLabelSecond.font = UIFont.systemFontOfSize(15.0)
        addPhotoLabelSecond.backgroundColor = UIColor.clearColor()
        addPhotoLabelSecond.textColor = UIColor.RGB(0xd9d9d9)
        addPhotoLabelSecond.sizeToFit()
        
        addPhotoLabelFirst.frame = CGRectIntegral(CGRectMake((addPhotoButton.frame.size.width - addPhotoLabelFirst.frame.size.width) / 2, 36, addPhotoLabelFirst.frame.size.width, addPhotoLabelFirst.frame.size.height));
        addPhotoLabelSecond.frame = CGRectIntegral(CGRectMake((addPhotoButton.frame.size.width - addPhotoLabelSecond.frame.size.width) / 2, 36 + 22, addPhotoLabelSecond.frame.size.width, addPhotoLabelSecond.frame.size.height));
        
        addPhotoButton.addSubview(addPhotoLabelFirst)
        addPhotoButton.addSubview(addPhotoLabelSecond)
        
        firstNameField = UITextField()
        firstNameField.backgroundColor = UIColor.whiteColor()
        firstNameField.font = UIFont.systemFontOfSize(20)
        firstNameField.keyboardType = UIKeyboardType.Default
        firstNameField.returnKeyType = UIReturnKeyType.Next
        firstNameField.placeholder = "First name" // TODO: Localize
        firstNameField.delegate = self
        firstNameField.contentVerticalAlignment = UIControlContentVerticalAlignment.Center
        firstNameField.frame = CGRectMake(135.0, navigationBarSeparator.frame.origin.y + 8.0, screenSize.width - 134.0 - 8.0, 56.0)
        view.addSubview(firstNameField)
        
        var firstNameFieldSeparator = UIView(frame: CGRect(x: 134.0, y: firstNameField.frame.origin.y + firstNameField.bounds.size.height, width: screenSize.width - 134.0 - 8.0, height: separatorHeight))
        firstNameFieldSeparator.backgroundColor = UIColor.RGB(0xc8c7cc)
        view.addSubview(firstNameFieldSeparator)
        
        lastNameField = UITextField()
        lastNameField.backgroundColor = UIColor.whiteColor()
        lastNameField.font = UIFont.systemFontOfSize(20)
        lastNameField.placeholder = "Last name" // TODO: Localize
        lastNameField.keyboardType = UIKeyboardType.Default
        lastNameField.returnKeyType = UIReturnKeyType.Done
        lastNameField.delegate = self
        lastNameField.contentVerticalAlignment = UIControlContentVerticalAlignment.Center
        lastNameField.frame = CGRectMake(135.0, firstNameFieldSeparator.frame.origin.y + 1, screenSize.width - 134.0 - 8.0, 56.0)
        view.addSubview(lastNameField)
        
        var lastNameFieldSeparator = UIView(frame: CGRect(x: 134.0, y: lastNameField.frame.origin.y + lastNameField.bounds.size.height, width: screenSize.width - 134.0 - 8.0, height: separatorHeight))
        lastNameFieldSeparator.backgroundColor = UIColor.RGB(0xc8c7cc)
        view.addSubview(lastNameFieldSeparator)
        
        if (isWidescreen) {
            hintLabel = UILabel()
            hintLabel.backgroundColor = UIColor.whiteColor()
            hintLabel.font = UIFont.systemFontOfSize(17.0)
            hintLabel.textColor = UIColor.RGB(0x999999)
            hintLabel.text = "Enter your name and add a profile picture."
            hintLabel.lineBreakMode = NSLineBreakMode.ByWordWrapping
            hintLabel.textAlignment = NSTextAlignment.Center
            hintLabel.contentMode = UIViewContentMode.Center
            hintLabel.numberOfLines = 0
            
            let hineLabelSize = hintLabel.sizeThatFits(CGSize(width: 200.0, height: CGFloat.max))
            hintLabel.frame = CGRect(x: (screenSize.width - hineLabelSize.width) / 2.0, y: 274.0, width: hineLabelSize.width, height: hineLabelSize.height)
            view.addSubview(hintLabel)
        }
        
        var nextBarButton = UIBarButtonItem(title: "Next", style: UIBarButtonItemStyle.Done, target: self, action: Selector("nextButtonPressed")) // TODO: Localize
        navigationItem.rightBarButtonItem = nextBarButton
    }
    
    // MARK: -
    // MARK: Layout
    
    override func viewDidLayoutSubviews() {
        super.viewDidLayoutSubviews()
        
        if !firstNameField.isFirstResponder() && !lastNameField.isFirstResponder() {
            firstNameField.becomeFirstResponder()
        }
    }
    
    // MARK: -
    // MARK: Methods
    
    func askChangePhoto() {
        var actionSheet = UIActionSheet(title: nil, delegate: self, cancelButtonTitle: "Cancel", destructiveButtonTitle: "Delete Photo", otherButtonTitles: "Update Photo") // TODO: Localize
        actionSheet.showInView(view)
    }
    
    func askAddPhoto() {
        var actionSheet = UIActionSheet(title: nil, delegate: self, cancelButtonTitle: "Cancel", destructiveButtonTitle: nil, otherButtonTitles: "Take Photo", "Choose Photo") // TODO: Localize
        actionSheet.showInView(view)
    }
    
    func nextButtonPressed() {
        let firstName = firstNameField.text
        
        if count(firstName) == 0 {
            shakeView(firstNameField, originalX: 135.0)
        } else {
            var fullName: String = firstName
            if count(lastNameField.text) > 0 {
                fullName = fullName + " \(lastNameField.text)"
            }
            
            var avatarPath: String = ""
            
            if avatarImageView.image != nil {
                avatarPath = NSTemporaryDirectory().stringByAppendingPathComponent("avatar.jpg")
                UIImageJPEGRepresentation(avatarImageView.image, 0.8).writeToFile(avatarPath, atomically: true)  // TODO: Check smallest 100x100, crop to 800x800
            }
            
            SVProgressHUD.showWithStatus("Saving profile")
            let messenger = CocoaMessenger.messenger().signUpWithNSString(fullName, withNSString: "/tmp/avatar.jpg", withBoolean: true)
            messenger.startWithAMCommandCallback(CocoaCallback(result: { (val: Any?) -> () in
                SVProgressHUD.dismiss()
                self.navigationController!.presentingViewController!.dismissViewControllerAnimated(true, completion: { () -> Void in
                    let application = UIApplication.sharedApplication()
                    if application.respondsToSelector("registerUserNotificationSettings:") {
                        let types: UIUserNotificationType = (.Alert | .Badge | .Sound)
                        let settings: UIUserNotificationSettings = UIUserNotificationSettings(forTypes: types, categories: nil)
                        application.registerUserNotificationSettings(settings)
                        application.registerForRemoteNotifications()
                    } else {
                        application.registerForRemoteNotificationTypes(.Alert | .Badge | .Sound)
                    }
                })
                }, error: { (exception) -> () in
                    SVProgressHUD.showErrorWithStatus(exception.getLocalizedMessage())
            }))
            
        }
    }
    
    private func shakeView(view: UIView, originalX: CGFloat) {
        var r = view.frame
        r.origin.x = originalX
        var originalFrame = r
        var rFirst = r
        rFirst.origin.x = r.origin.x + 4
        r.origin.x = r.origin.x - 4
    
        UIView.animateWithDuration(0.05, delay: 0.0, options: UIViewAnimationOptions.Autoreverse, animations: { () -> Void in
            view.frame = rFirst
        }) { (finished) -> Void in
            if (finished) {
                UIView.animateWithDuration(0.05, delay: 0.0, options: (UIViewAnimationOptions.Repeat | UIViewAnimationOptions.Autoreverse), animations: { () -> Void in
                    UIView.setAnimationRepeatCount(3)
                    view.frame = r
                    }, completion: { (finished) -> Void in
                        view.frame = originalFrame
                })
            } else {
                view.frame = originalFrame
            }
        }
    }


}

// MARK: -
// MARK: UITextField Delegate

extension AAAuthRegisterController: UITextFieldDelegate {
    
    func textFieldShouldReturn(textField: UITextField) -> Bool {
        if textField == firstNameField {
            lastNameField.becomeFirstResponder()
        } else if textField == lastNameField {
            nextButtonPressed()
        }
        
        return false
    }
    
}

// MARK: -
// MARK: UIActionSheet Delegate

extension AAAuthRegisterController: UIActionSheetDelegate {
    
    func actionSheet(actionSheet: UIActionSheet, didDismissWithButtonIndex buttonIndex: Int) {
        let title = actionSheet.buttonTitleAtIndex(buttonIndex)
        
        // TODO: Localize
        if title == "Choose Photo" || title == "Take Photo" {
            let takePhoto = (title == "Take Photo")
            var picker = UIImagePickerController()
            picker.sourceType = (takePhoto ? UIImagePickerControllerSourceType.Camera : UIImagePickerControllerSourceType.PhotoLibrary)
            picker.delegate = self
            self.navigationController!.presentViewController(picker, animated: true, completion: nil)
            
        } else if title == "Delete Photo" {
            avatarImageView.hidden = true
            avatarImageView.image = nil
        } else if title == "Update Photo" {
            askAddPhoto()
        }
    }
    
}

// MARK: -
// MARK: UIImagePickerController Delegate

extension AAAuthRegisterController: UIImagePickerControllerDelegate {
    
    // TODO: Allow to crop rectangle 
    func imagePickerController(picker: UIImagePickerController, didFinishPickingImage image: UIImage!, editingInfo: [NSObject : AnyObject]!) {
        avatarImageView.image = image
        avatarImageView.hidden = false
        navigationController!.dismissViewControllerAnimated(true, completion: nil)
    }
    
    func imagePickerController(picker: UIImagePickerController, didFinishPickingMediaWithInfo info: [NSObject : AnyObject]) {
        let image = info[UIImagePickerControllerOriginalImage] as! UIImage
        avatarImageView.image = image
        avatarImageView.hidden = false
        navigationController!.dismissViewControllerAnimated(true, completion: nil)
    }
    
    func imagePickerControllerDidCancel(picker: UIImagePickerController) {
        self.dismissViewControllerAnimated(true, completion: nil)
    }
    
}

// MARK: -
// MARK: UINavigationController Delegate

extension AAAuthRegisterController: UINavigationControllerDelegate {
    
    
    
}