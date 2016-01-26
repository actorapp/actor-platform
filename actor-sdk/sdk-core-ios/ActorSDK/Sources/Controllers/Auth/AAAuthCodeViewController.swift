//
//  Copyright (c) 2014-2016 Actor LLC. <https://actor.im>
//

import UIKit
import MessageUI

public class AAAuthCodeViewController: AAAuthViewController, UIAlertViewDelegate, MFMailComposeViewControllerDelegate {

    // Views
    
    private static let DIAL_SECONDS: Int = 60
    
    private let grayBackground = UIView()
    private let titleLabel = UILabel()
    
    private let codeTextField = UITextField()
    private let hintLabel = UILabel()
    private let callHintLabel = UILabel()
    private let callActionLabel = UIButton()
    
    private var navigationBarSeparator = UIView()
    private var codeSeparator = UIView()
    
    private var counterTimer: NSTimer!
    private var dialed: Bool = false
    private var counter = AAAuthCodeViewController.DIAL_SECONDS
    
    private let phoneNumber: String

    public init(phoneNumber: String) {
        
        self.phoneNumber = phoneNumber
        
        super.init()
        
        self.content = ACAllEvents_Auth.AUTH_CODE()
        
        grayBackground.backgroundColor = UIColor(rgb: 0xf2f2f2)
        view.addSubview(grayBackground)
        
        titleLabel.backgroundColor = UIColor.clearColor()
        titleLabel.textColor = UIColor.blackColor()
        titleLabel.textAlignment = NSTextAlignment.Center
        titleLabel.font =  AADevice.isiPad
            ? UIFont.thinSystemFontOfSize(50.0)
            : UIFont.systemFontOfSize(22.0)
        titleLabel.text = phoneNumber
        grayBackground.addSubview(titleLabel)
        
        navigationBarSeparator = UIView()
        navigationBarSeparator.backgroundColor = UIColor(rgb: 0xc8c7cc)
        view.addSubview(navigationBarSeparator)
        
        codeTextField.placeholder = AALocalized("AuthCodeFieldHint")
        codeTextField.font = UIFont.systemFontOfSize(24.0)
        codeTextField.backgroundColor = UIColor.whiteColor()
        codeTextField.contentVerticalAlignment = UIControlContentVerticalAlignment.Center
        codeTextField.textAlignment = NSTextAlignment.Center
        codeTextField.keyboardType = UIKeyboardType.NumberPad
        view.addSubview(codeTextField)
        
        codeSeparator = UIView()
        codeSeparator.backgroundColor = UIColor(rgb: 0xc8c7cc)
        view.addSubview(codeSeparator)
        
        hintLabel.font = UIFont.systemFontOfSize(16.0)
        hintLabel.backgroundColor = UIColor.whiteColor()
        hintLabel.textColor = UIColor.blackColor()
        hintLabel.textAlignment = NSTextAlignment.Center
        hintLabel.contentMode = UIViewContentMode.Center
        hintLabel.numberOfLines = 0
        hintLabel.textLocalized = "AuthCodeHint"
        view.addSubview(hintLabel)
        
        callHintLabel.font = UIFont.systemFontOfSize(16.0)
        callHintLabel.backgroundColor = UIColor.whiteColor()
        callHintLabel.textColor = UIColor.alphaBlack(0.64)
        callHintLabel.textAlignment = NSTextAlignment.Center
        callHintLabel.contentMode = UIViewContentMode.Center
        callHintLabel.numberOfLines = 1
        view.addSubview(callHintLabel)
        
        callActionLabel.titleLabel?.font = UIFont.systemFontOfSize(16)
        callActionLabel.setTitleColor(UIColor(rgb: 0x5085CB), forState: .Normal)
        callActionLabel.setTitle(AALocalized("AuthNoCodeHint"), forState: .Normal)
        callActionLabel.hidden = true
        callActionLabel.addTarget(self, action: "noCodeDidPressed", forControlEvents: .TouchUpInside)
        view.addSubview(callActionLabel)
        
        view.backgroundColor = UIColor.whiteColor()
        
        navigationItem.rightBarButtonItem = UIBarButtonItem(title: AALocalized("NavigationNext"), style: UIBarButtonItemStyle.Done, target: self, action: "nextButtonPressed")
    }

    public required init(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    // MARK: -
    
    public override func viewDidAppear(animated: Bool) {
        super.viewDidAppear(animated)
        
        codeTextField.becomeFirstResponder()
        
        updateTimerText()
        
        if !dialed {
            counterTimer = NSTimer.scheduledTimerWithTimeInterval(1.0, target: self, selector: "updateTimer", userInfo: nil, repeats: true)
        }
    }
    
    public override func viewWillDisappear(animated: Bool) {
        super.viewWillDisappear(animated)
        
        if counterTimer != nil {
            counterTimer.invalidate()
            counterTimer = nil
        }
    }
    
    public override func viewWillLayoutSubviews() {
        super.viewWillLayoutSubviews()
        
        let screenSize = UIScreen.mainScreen().bounds.size
        let isWidescreen = screenSize.width > 320 || screenSize.height > 480
        let isPortraint = screenSize.width < screenSize.height
        
        let bgSize = AADevice.isiPad
            ? (isPortraint ? 304.0: 140)
            : (isWidescreen ? 131.0 : 90.0)
        grayBackground.frame = CGRect(x: 0.0, y: 0.0, width: screenSize.width, height: CGFloat(bgSize))
        
        
        let padding = AADevice.isiPad
            ? (isPortraint ? 48 : 20)
            : (24)
        titleLabel.sizeToFit()
        titleLabel.frame = CGRect(x: (screenSize.width - titleLabel.frame.size.width) / 2.0, y: grayBackground.frame.height - titleLabel.frame.size.height - CGFloat(padding), width: titleLabel.frame.size.width, height: titleLabel.frame.size.height)
        
        let separatorHeight: CGFloat = 0.5
        navigationBarSeparator.frame = CGRect(x: 0.0, y: grayBackground.bounds.size.height, width: screenSize.width, height: separatorHeight)
        
        let fieldWidth : CGFloat = AADevice.isiPad
            ? (520)
            : (screenSize.width)
        
        codeTextField.frame = CGRect(x: (screenSize.width - fieldWidth)/2, y: navigationBarSeparator.frame.origin.y + navigationBarSeparator.bounds.size.height, width: fieldWidth, height: 56.0)
        
        codeSeparator.frame = CGRect(x: (screenSize.width - fieldWidth)/2 + 22, y: codeTextField.frame.origin.y + codeTextField.bounds.size.height, width: fieldWidth - 44, height: separatorHeight)
        
        let hintLabelSize = hintLabel.sizeThatFits(CGSize(width: 300, height: screenSize.height))
        hintLabel.frame = CGRectIntegral(CGRect(x: (screenSize.width - hintLabelSize.width) / 2, y: navigationBarSeparator.frame.origin.y + (isWidescreen ? 85.0 : 70.0), width: hintLabelSize.width, height: hintLabelSize.height))
        
        callHintLabel.frame = CGRectIntegral(CGRect(x: (screenSize.width - hintLabelSize.width) / 2, y: hintLabel.bottom + 11, width: hintLabelSize.width, height: 22))
        
        callActionLabel.frame = CGRectIntegral(CGRect(x: (screenSize.width - hintLabelSize.width) / 2, y: callHintLabel.bottom + 5, width: hintLabelSize.width, height: 22))
    }
    
    func updateTimer() {
        
        counter--
        
        if counter == 0 {
            dialed = true
            if counterTimer != nil {
                counterTimer.invalidate()
                counterTimer = nil
            }
            
            executeHidden(Actor.requestPhoneCall())
        }
     
        updateTimerText()
    }
    
    func supportEnabled() -> Bool {
        return ActorSDK.sharedActor().supportEmail != nil
    }

    func updateTimerText() {
        if dialed {
            callHintLabel.textLocalized = "AuthCallDoneHint"
            if self.supportEnabled() {
                callActionLabel.hidden = false
            }
        } else {
            let min = counter / 60
            let sec = counter % 60
            let minFormatted = min.format("02")
            let secFormatted = sec.format("02")
            let time = "\(minFormatted):\(secFormatted)"
            callHintLabel.text = AALocalized("AuthCallHint")
                .replace("{time}", dest: time).replace("{appname}", dest: ActorSDK.sharedActor().appNameInLocStrings)
            callActionLabel.hidden = true
        }
    }
    
    func nextButtonPressed() {
        
        let code = codeTextField.text!
        
        if code.length > 0 {
            
            executeSafe(Actor.validateCodeCommand(code), ignore: ["PHONE_CODE_INVALID"]) { (val) -> Void in
                
                let state = Actor.getAuthState().ordinal()
                
                if state == ACAuthState.LOGGED_IN().ordinal() {
                    
                    // If logged in state: end authentication
                    self.onAuthenticated()
                } else if state == ACAuthState.SIGN_UP().ordinal() {
                    
                    // Is signup go to signup controller
                    self.navigateNext(AAAuthRegisterViewController(), removeCurrent: true)
                } else if state != ACAuthState.CODE_VALIDATION_PHONE().ordinal() {
                    
                    // If state not for the current state: start from scratch
                    self.navigateBack()
                } else {
                    
                    // If same state: something code is incorrect
                    self.shakeView(self.codeTextField, originalX: self.codeTextField.frame.origin.x)
                }
            }
            
        } else {
            shakeView(codeTextField, originalX: codeTextField.frame.origin.x)
        }
    }
    
    public func noCodeDidPressed() {
        if self.supportEnabled() {
            let emailController = MFMailComposeViewController()
            emailController.setSubject("Activation code problem (\(phoneNumber))")
            emailController.setToRecipients([ActorSDK.sharedActor().supportEmail!])
            emailController.setMessageBody("Hello, Dear Support!\n\nI can't receive any activation codes to the phone: \(phoneNumber).\n\nHope, you will answer soon. Thank you!", isHTML: false)
            emailController.delegate = self
            presentViewController(emailController, animated: true, completion: nil)
        }
    }
    
    public func mailComposeController(controller: MFMailComposeViewController, didFinishWithResult result: MFMailComposeResult, error: NSError?) {
        controller.dismissViewControllerAnimated(false, completion: nil)
    }
}
