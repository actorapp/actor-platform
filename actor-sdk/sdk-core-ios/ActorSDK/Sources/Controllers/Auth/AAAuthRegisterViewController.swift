//
//  Copyright (c) 2014-2016 Actor LLC. <https://actor.im>
//

import UIKit
import MobileCoreServices

public class AAAuthRegisterViewController: AAAuthViewController, UIAlertViewDelegate, UITextFieldDelegate, UIActionSheetDelegate {
    
    // MARK: -
    // MARK: Private vars
    
    private var grayBackground: UIView!
    private var titleLabel: UILabel!
    
    private var addPhotoButton: UIButton!
    private var avatarImageView: UIImageView!
    
    private var firstNameField: UITextField!
    
    private var hintLabel: UILabel!
    
    private var navigationBarSeparator: UIView!
    
    private var firstNameFieldSeparator: UIView!
    
    // MARK: -
    
    public override func loadView() {
        super.loadView()
        
        self.content = ACAllEvents_Auth.AUTH_SIGNUP()
        
        view.backgroundColor = UIColor.whiteColor()
        
        grayBackground = UIView()
        grayBackground.backgroundColor = UIColor(rgb: 0xf2f2f2)
        view.addSubview(grayBackground)
        
        titleLabel = UILabel()
        titleLabel.backgroundColor = UIColor.clearColor()
        titleLabel.textColor = UIColor.blackColor()
        titleLabel.textAlignment = NSTextAlignment.Center
        titleLabel.font = AADevice.isiPad
            ? UIFont.thinSystemFontOfSize(50)
            : UIFont.lightSystemFontOfSize(30)
        titleLabel.text = AALocalized("AuthProfileTitle")
        grayBackground.addSubview(titleLabel)
        
        navigationBarSeparator = UIView()
        navigationBarSeparator.backgroundColor = UIColor(rgb: 0xc8c7cc)
        view.addSubview(navigationBarSeparator)
        
        UIGraphicsBeginImageContextWithOptions(CGSize(width: 110, height: 110), false, 0.0);
        let context = UIGraphicsGetCurrentContext();
        CGContextSetFillColorWithColor(context, UIColor.whiteColor().CGColor);
        CGContextFillEllipseInRect(context, CGRectMake(0.0, 0.0, 110.0, 110.0));
        CGContextSetStrokeColorWithColor(context, UIColor(rgb: 0xd9d9d9).CGColor);
        CGContextSetLineWidth(context, 1.0);
        CGContextStrokeEllipseInRect(context, CGRectMake(0.5, 0.5, 109.0, 109.0));
        let buttonImage = UIGraphicsGetImageFromCurrentImageContext();
        UIGraphicsEndImageContext();
        
        addPhotoButton = UIButton()
        addPhotoButton.exclusiveTouch = true
        addPhotoButton.setBackgroundImage(buttonImage, forState: UIControlState.Normal)
        addPhotoButton.addTarget(self, action: Selector("askAddPhoto"), forControlEvents: UIControlEvents.TouchUpInside)
        view.addSubview(addPhotoButton)
        
        avatarImageView = UIImageView()
        avatarImageView.hidden = true;
        avatarImageView.userInteractionEnabled = true
        avatarImageView.layer.cornerRadius = 55
        avatarImageView.layer.masksToBounds = true
        avatarImageView.layer.shouldRasterize = true
        avatarImageView.layer.rasterizationScale = UIScreen.mainScreen().scale
        avatarImageView.contentMode = UIViewContentMode.ScaleAspectFill
        avatarImageView.addGestureRecognizer(UITapGestureRecognizer(target: self, action: Selector("askChangePhoto")))
        view.addSubview(avatarImageView)
        
        let addPhotoLabelFirst = UILabel()
        addPhotoLabelFirst.text = AALocalized("AuthProfileAddPhoto1")
        addPhotoLabelFirst.font = UIFont.systemFontOfSize(15.0)
        addPhotoLabelFirst.backgroundColor = UIColor.clearColor()
        addPhotoLabelFirst.textColor = UIColor(rgb: 0xd9d9d9)
        addPhotoLabelFirst.sizeToFit()
        
        let addPhotoLabelSecond = UILabel()
        addPhotoLabelSecond.text = AALocalized("AuthProfileAddPhoto2")
        addPhotoLabelSecond.font = UIFont.systemFontOfSize(15.0)
        addPhotoLabelSecond.backgroundColor = UIColor.clearColor()
        addPhotoLabelSecond.textColor = UIColor(rgb: 0xd9d9d9)
        addPhotoLabelSecond.sizeToFit()
    
        addPhotoButton.addSubview(addPhotoLabelFirst)
        addPhotoButton.addSubview(addPhotoLabelSecond)
        
        addPhotoLabelFirst.frame = CGRectIntegral(CGRectMake((110 - addPhotoLabelFirst.frame.size.width) / 2, 36, addPhotoLabelFirst.frame.size.width, addPhotoLabelFirst.frame.size.height));
        addPhotoLabelSecond.frame = CGRectIntegral(CGRectMake((110 - addPhotoLabelSecond.frame.size.width) / 2, 36 + 22, addPhotoLabelSecond.frame.size.width, addPhotoLabelSecond.frame.size.height));
        
        firstNameField = UITextField()
        firstNameField.backgroundColor = UIColor.whiteColor()
        firstNameField.font = UIFont.systemFontOfSize(20)
        firstNameField.keyboardType = UIKeyboardType.Default
        firstNameField.returnKeyType = UIReturnKeyType.Next
        firstNameField.placeholder = AALocalized("AuthProfileNameHint")
        firstNameField.delegate = self
        firstNameField.contentVerticalAlignment = UIControlContentVerticalAlignment.Center
        firstNameField.autocapitalizationType = UITextAutocapitalizationType.Words
        view.addSubview(firstNameField)
        
        firstNameFieldSeparator = UIView()
        firstNameFieldSeparator.backgroundColor = UIColor(rgb: 0xc8c7cc)
        view.addSubview(firstNameFieldSeparator)
        
        let screenSize = UIScreen.mainScreen().bounds.size
        let isWidescreen = screenSize.width > 320 || screenSize.height > 480
        
        if (isWidescreen) {
            hintLabel = UILabel()
            hintLabel.backgroundColor = UIColor.whiteColor()
            hintLabel.font = UIFont.systemFontOfSize(17.0)
            hintLabel.textColor = UIColor(rgb: 0x999999)
            hintLabel.text = AALocalized("AuthProfileHint")
            hintLabel.lineBreakMode = NSLineBreakMode.ByWordWrapping
            hintLabel.textAlignment = NSTextAlignment.Center
            hintLabel.contentMode = UIViewContentMode.Center
            hintLabel.numberOfLines = 0
            view.addSubview(hintLabel)
        }
        
        let nextBarButton = UIBarButtonItem(title: AALocalized("NavigationNext"), style: UIBarButtonItemStyle.Done, target: self, action: Selector("nextButtonPressed"))
        navigationItem.rightBarButtonItem = nextBarButton
    }
    
    public override func viewWillAppear(animated: Bool) {
        super.viewWillAppear(animated)
        
        // MainAppTheme.navigation.applyAuthStatusBar()
    }
    
    // MARK: -
    // MARK: Layout
    
    public override func viewDidLayoutSubviews() {
        super.viewDidLayoutSubviews()
        
        if !firstNameField.isFirstResponder() {
            firstNameField.becomeFirstResponder()
        }
        
        let screenSize = UIScreen.mainScreen().bounds.size
        let isWidescreen = screenSize.width > 320 || screenSize.height > 480
        let isPortraint = screenSize.width < screenSize.height
        
        let bgSize : CGFloat = AADevice.isiPad
            ? (isPortraint ? 304.0: 140)
            : (isWidescreen ? 131.0 : 90.0)
        
        grayBackground.frame = CGRect(x: 0.0, y: 0.0, width: screenSize.width, height: bgSize)
        
        let padding = AADevice.isiPad
            ? (isPortraint ? 48 : 20)
            : (20)
        titleLabel.sizeToFit()
        titleLabel.frame = CGRect(x: (screenSize.width - titleLabel.frame.size.width) / 2.0, y: grayBackground.frame.height - titleLabel.frame.size.height - CGFloat(padding), width: titleLabel.frame.size.width, height: titleLabel.frame.size.height)
        
        navigationBarSeparator.frame = CGRect(x: 0.0, y: grayBackground.bounds.size.height, width: screenSize.width, height: 0.5)
        
        let fieldWidth : CGFloat = AADevice.isiPad
            ? (520)
            : (screenSize.width)
        
        addPhotoButton.frame = CGRect(x: (screenSize.width - fieldWidth)/2 + 10, y: navigationBarSeparator.frame.origin.y + 11, width: 110, height: 110)
        
        avatarImageView.frame = CGRect(x: (screenSize.width - fieldWidth)/2 + 10, y: navigationBarSeparator.frame.origin.y + 11, width: 110, height: 110)
    
        firstNameField.frame = CGRectMake((screenSize.width - fieldWidth)/2 + 135.0, navigationBarSeparator.frame.origin.y + 34.0, fieldWidth - 134.0 - 8.0, 56.0)
        
        firstNameFieldSeparator.frame = CGRect(x: (screenSize.width - fieldWidth)/2+134.0, y: firstNameField.frame.origin.y + firstNameField.bounds.size.height, width: fieldWidth - 134.0 - 8.0, height: 0.5)
        
        if (hintLabel != nil) {
            let hintPadding : CGFloat = AADevice.isiPad
                ? (isPortraint ? 460.0 : 274.0)
                : 274.0
            
            let hineLabelSize = hintLabel.sizeThatFits(CGSize(width: 200.0, height: CGFloat.max))
            hintLabel.frame = CGRect(
                x: (screenSize.width - hineLabelSize.width) / 2.0,
                y: hintPadding,
                width: hineLabelSize.width,
                height: hineLabelSize.height)
        }
    }
    
    // MARK: -
    // MARK: Methods
    
    public func askChangePhoto() {
        selectPhoto(true)
    }
    
    public func askAddPhoto() {
        selectPhoto(false)
    }
    
    public func selectPhoto(supportDelete: Bool) {
        let actionSheet = UIActionSheet(title: nil, delegate: self,
            cancelButtonTitle: AALocalized("AlertCancel"),
            destructiveButtonTitle: nil,
            otherButtonTitles: AALocalized("PhotoCamera"), AALocalized("PhotoLibrary"))
        if (avatarImageView.image != nil) {
            actionSheet.addButtonWithTitle(AALocalized("PhotoRemove"))
            actionSheet.destructiveButtonIndex = 3
        }
        actionSheet.showInView(view)
    }
    
    public func nextButtonPressed() {
        let username = firstNameField.text
        
        if username?.length == 0 {
            let screenSize = UIScreen.mainScreen().bounds.size
            let fieldWidth : CGFloat = AADevice.isiPad
                ? (520)
                : (screenSize.width)
            shakeView(firstNameField, originalX: (screenSize.width - fieldWidth)/2+135.0)
        } else {
            var avatarPath: String? = nil
            
            if avatarImageView.image != nil {
                avatarPath = "/tmp/avatar_" + NSUUID().UUIDString + ".jpg"
                let avatarFilePath = CocoaFiles.pathFromDescriptor(avatarPath!)
                let image = avatarImageView.image
                let thumb = image?.resizeSquare(600, maxH: 600);
                UIImageJPEGRepresentation(thumb!, 0.8)!.writeToFile(avatarFilePath, atomically: true)  // TODO: Check smallest 100x100, crop to 800x800
            }
            
            execute(Actor.signUpCommandWithName(username, withSex: ACSex.UNKNOWN(), withAvatar: avatarPath), successBlock: { (val) -> Void in
                self.onAuthenticated()
                }, failureBlock: { (val) -> Void in
                    
                    var message = "Unknwon Error"
                    var tag = "UNKNOWN"
                    
                    if let exception = val as? ACRpcException {
                        tag = exception.getTag()
                        if (tag == "PHONE_CODE_EXPIRED") {
                            message = AALocalized("ErrorCodeExpired")
                        } else if (tag == "NAME_INVALID") {
                            let screenSize = UIScreen.mainScreen().bounds.size
                            let fieldWidth : CGFloat = AADevice.isiPad
                                ? (520)
                                : (screenSize.width)
                            self.shakeView(self.firstNameField, originalX: (screenSize.width - fieldWidth)/2+135.0)
                            return
                        } else {
                            message = exception.getLocalizedMessage()
                        }
                    } else if let exception = val as? JavaLangException {
                        message = exception.getLocalizedMessage()
                    }
                    
                    let alertView = UIAlertView(title: nil, message: message, delegate: self, cancelButtonTitle: AALocalized("AlertOk"))
                    alertView.show()
            })
        }
    }
    
    public func alertView(alertView: UIAlertView, willDismissWithButtonIndex buttonIndex: Int) {
        if (Actor.getAuthState().ordinal() != ACAuthState.SIGN_UP().ordinal()) {
            navigateBack()
        }
    }
    
    public func textFieldShouldReturn(textField: UITextField) -> Bool {
        if textField == firstNameField {
            nextButtonPressed()
        }
        
        return false
    }
    public func actionSheet(actionSheet: UIActionSheet, didDismissWithButtonIndex buttonIndex: Int) {
        if (buttonIndex == 0) {
            return
        }
        
        if (buttonIndex == 1 || buttonIndex == 2) {
            let takePhoto = (buttonIndex == 1)
            pickAvatar(takePhoto, closure: { (image) -> () in
                self.avatarImageView.image = image
                self.avatarImageView.hidden = false
            })
        } else if (buttonIndex == 3) {
            avatarImageView.hidden = true
            avatarImageView.image = nil
        }
    }
    
}