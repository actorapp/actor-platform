//
//  Copyright (c) 2014-2016 Actor LLC. <https://actor.im>
//

import Foundation
import MessageUI

public class AAAuthOTPViewController: AAAuthViewController, MFMailComposeViewControllerDelegate {

    private static let DIAL_SECONDS: Int = 60
    
    let scrollView = UIScrollView()
    let welcomeLabel = UILabel()
    let validateLabel = UILabel()
    let hintLabel = UILabel()
    
    let codeField = UITextField()
    let codeFieldLine = UIView()
    
    let haventReceivedCode = UIButton()
    
    let transactionHash: String
    let name: String!
    let email: String!
    let phone: String!
    
    private var counterTimer: NSTimer!
    private var dialed: Bool = false
    private var counter = AAAuthOTPViewController.DIAL_SECONDS
    
    public init(email: String, transactionHash: String) {
        self.transactionHash = transactionHash
        self.name = nil
        self.email = email
        self.phone = nil
        super.init()
    }
    
    public init(email: String, name: String, transactionHash: String) {
        self.transactionHash = transactionHash
        self.name = name
        self.email = email
        self.phone = nil
        super.init()
    }
    
    public init(phone: String, transactionHash: String) {
        self.transactionHash = transactionHash
        self.name = nil
        self.email = nil
        self.phone = phone
        super.init()
    }
    
    public init(phone: String, name: String, transactionHash: String) {
        self.transactionHash = transactionHash
        self.name = name
        self.email = nil
        self.phone = phone
        super.init()
    }
    
    public required init(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    public override func viewDidLoad() {
        
        view.backgroundColor = UIColor.whiteColor()
        
        scrollView.keyboardDismissMode = .OnDrag
        scrollView.scrollEnabled = true
        scrollView.alwaysBounceVertical = true
        
        welcomeLabel.font = UIFont.lightSystemFontOfSize(23)
        if email != nil {
            welcomeLabel.text = AALocalized("AuthOTPEmailTitle")
        } else {
            welcomeLabel.text = AALocalized("AuthOTPPhoneTitle")
        }
        welcomeLabel.textColor = ActorSDK.sharedActor().style.authTitleColor
        welcomeLabel.textAlignment = .Center
        
        validateLabel.font = UIFont.systemFontOfSize(14)
        if email != nil {
            validateLabel.text = email
        } else {
            validateLabel.text = phone
        }
        validateLabel.textColor = ActorSDK.sharedActor().style.authTintColor
        validateLabel.textAlignment = .Center
        
        hintLabel.font = UIFont.systemFontOfSize(14)
        if email != nil {
            hintLabel.text = AALocalized("AuthOTPEmailHint")
        } else {
            hintLabel.text = AALocalized("AuthOTPPhoneHint")
        }
        hintLabel.textColor = ActorSDK.sharedActor().style.authHintColor
        hintLabel.textAlignment = .Center
        hintLabel.numberOfLines = 2
        hintLabel.lineBreakMode = .ByWordWrapping
        
        codeField.font = UIFont.systemFontOfSize(17)
        codeField.textColor = ActorSDK.sharedActor().style.authTextColor
        codeField.placeholder = AALocalized("AuthOTPPlaceholder")
        codeField.keyboardType = .NumberPad
        codeField.autocapitalizationType = .None
        codeField.autocorrectionType = .No
        
        codeFieldLine.backgroundColor = ActorSDK.sharedActor().style.authSeparatorColor
        
        if ActorSDK.sharedActor().supportEmail != nil {
            haventReceivedCode.setTitle(AALocalized("AuthOTPNoCode"), forState: .Normal)
        } else {
            haventReceivedCode.hidden = true
        }
        haventReceivedCode.titleLabel?.font = UIFont.systemFontOfSize(14)
        haventReceivedCode.setTitleColor(ActorSDK.sharedActor().style.authTintColor, forState: .Normal)
        haventReceivedCode.setTitleColor(ActorSDK.sharedActor().style.authTintColor.alpha(0.64), forState: .Highlighted)
        haventReceivedCode.setTitleColor(ActorSDK.sharedActor().style.authHintColor, forState: .Disabled)
        haventReceivedCode.addTarget(self, action: #selector(AAAuthOTPViewController.haventReceivedCodeDidPressed), forControlEvents: .TouchUpInside)
        
        scrollView.addSubview(welcomeLabel)
        scrollView.addSubview(validateLabel)
        scrollView.addSubview(hintLabel)
        scrollView.addSubview(codeField)
        scrollView.addSubview(codeFieldLine)
        scrollView.addSubview(haventReceivedCode)
        
        view.addSubview(scrollView)
        super.viewDidLoad()
    }
    
    public override func viewDidLayoutSubviews() {
        super.viewDidLayoutSubviews()
        
        welcomeLabel.frame = CGRectMake(15, 90 - 66, view.width - 30, 28)
        validateLabel.frame = CGRectMake(10, 127 - 66, view.width - 20, 17)
        hintLabel.frame = CGRectMake(10, 154 - 66, view.width - 20, 56)
        
        codeField.frame = CGRectMake(20, 228 - 66, view.width - 40, 44)
        codeFieldLine.frame = CGRectMake(10, 228 + 44 - 66, view.width - 20, 0.5)
        
        haventReceivedCode.frame = CGRectMake(20, 297 - 66, view.width - 40, 56)
        
        scrollView.frame = view.bounds
        scrollView.contentSize = CGSizeMake(view.width, 240 - 66)
    }
    
    func haventReceivedCodeDidPressed() {
        if ActorSDK.sharedActor().supportEmail != nil {
            if self.email != nil {
                let emailController = MFMailComposeViewController()
                emailController.setSubject("Activation code problem (\(self.email))")
                emailController.setToRecipients([ActorSDK.sharedActor().supportEmail!])
                emailController.setMessageBody("Hello, Dear Support!\n\nI can't receive any activation codes to the email: \(self.email).\n\nHope, you will answer soon. Thank you!", isHTML: false)
                emailController.delegate = self
                self.presentElegantViewController(emailController)
            } else if self.phone != nil {
                let emailController = MFMailComposeViewController()
                emailController.setSubject("Activation code problem (\(self.phone))")
                emailController.setToRecipients([ActorSDK.sharedActor().supportEmail!])
                emailController.setMessageBody("Hello, Dear Support!\n\nI can't receive any activation codes to the phone: \(self.phone).\n\nHope, you will answer soon. Thank you!", isHTML: false)
                emailController.delegate = self
                self.presentElegantViewController(emailController)
            }
        }
    }
    
    public func mailComposeController(controller: MFMailComposeViewController, didFinishWithResult result: MFMailComposeResult, error: NSError?) {
        controller.dismissViewControllerAnimated(true, completion: nil)
    }
    
    public override func nextDidTap() {
        let code = codeField.text!.trim()
        
        if code.length == 0 {
            shakeField()
            return
        }
        
        let promise = Actor.doValidateCode(code, withTransaction: self.transactionHash)
            .startUserAction(["EMAIL_CODE_INVALID", "PHONE_CODE_INVALID", "EMAIL_CODE_EXPIRED", "PHONE_CODE_EXPIRED"])
        
        promise.then { (r: ACAuthCodeRes!) -> () in
            if r.needToSignup {
                if self.name == nil {
                    self.navigateNext(AAAuthNameViewController(transactionHash: r.transactionHash))
                } else {
                    let promise = Actor.doSignupWithName(self.name, withSex: ACSex.UNKNOWN(), withTransaction: r.transactionHash)
                    promise.then { (r: ACAuthRes!) -> () in
                        Actor.doCompleteAuth(r).startUserAction().then { (r: JavaLangBoolean!) -> () in
                            self.codeField.resignFirstResponder()
                            self.onAuthenticated()
                        }
                    }
                    promise.startUserAction()
                }
            } else {
                Actor.doCompleteAuth(r.result).startUserAction().then { (r: JavaLangBoolean!) -> () in
                    self.codeField.resignFirstResponder()
                    self.onAuthenticated()
                }
            }
        }
        
        promise.failure { (e: JavaLangException!) -> () in
            if let rpc = e as? ACRpcException {
                if rpc.tag == "EMAIL_CODE_INVALID" || rpc.tag == "PHONE_CODE_INVALID" {
                    self.shakeField()
                } else if rpc.tag == "EMAIL_CODE_EXPIRED" || rpc.tag == "PHONE_CODE_EXPIRED" {
                    AAExecutions.errorWithTag(rpc.tag, rep: nil, cancel: { () -> () in
                        self.navigateBack()
                    })
                }
            }
        }
    }
    
    private func shakeField() {
        shakeView(codeField, originalX: 20)
        shakeView(codeFieldLine, originalX: 10)
    }
    
    public override func viewWillAppear(animated: Bool) {
        super.viewWillAppear(animated)
        
        if self.phone != nil {
        
            updateTimerText()
        
            if !dialed {
                counterTimer = NSTimer.scheduledTimerWithTimeInterval(1.0, target: self, selector: #selector(AAAuthOTPViewController.updateTimer), userInfo: nil, repeats: true)
            }
        }
    }
    
    func updateTimer() {
        
        counter -= 1
        
        if counter == 0 {
            dialed = true
            if counterTimer != nil {
                counterTimer.invalidate()
                counterTimer = nil
            }
            
            Actor.doSendCodeViaCall(self.transactionHash)
        }
        
        updateTimerText()
    }
    
    
    func updateTimerText() {
        if dialed {
            if ActorSDK.sharedActor().supportEmail != nil {
                haventReceivedCode.setTitle(AALocalized("AuthOTPNoCode"), forState: .Normal)
                haventReceivedCode.hidden = false
                haventReceivedCode.enabled = true
            } else {
                haventReceivedCode.hidden = true
            }
        } else {
            let min = counter / 60
            let sec = counter % 60
            let minFormatted = min.format("02")
            let secFormatted = sec.format("02")
            let time = "\(minFormatted):\(secFormatted)"
            let text = AALocalized("AuthOTPCallHint")
                .replace("{app_name}", dest: ActorSDK.sharedActor().appName)
                .replace("{time}", dest: time)
            haventReceivedCode.setTitle(text, forState: .Normal)
            haventReceivedCode.enabled = false
            haventReceivedCode.hidden = false
        }
    }
    
    public override func viewWillDisappear(animated: Bool) {
        super.viewWillDisappear(animated)
        
        if counterTimer != nil {
            counterTimer.invalidate()
            counterTimer = nil
        }
        
        self.codeField.resignFirstResponder()
    }
}


