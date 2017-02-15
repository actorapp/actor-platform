//
//  Copyright (c) 2014-2016 Actor LLC. <https://actor.im>
//

import Foundation
import MessageUI

open class AAAuthOTPViewController: AAAuthViewController, MFMailComposeViewControllerDelegate {

    fileprivate static let DIAL_SECONDS: Int = 60
    
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
    
    fileprivate var counterTimer: Timer!
    fileprivate var dialed: Bool = false
    fileprivate var counter = AAAuthOTPViewController.DIAL_SECONDS
    
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
    
    open override func viewDidLoad() {
        
        view.backgroundColor = UIColor.white
        
        scrollView.keyboardDismissMode = .onDrag
        scrollView.isScrollEnabled = true
        scrollView.alwaysBounceVertical = true
        
        welcomeLabel.font = UIFont.lightSystemFontOfSize(23)
        if email != nil {
            welcomeLabel.text = AALocalized("AuthOTPEmailTitle")
        } else {
            welcomeLabel.text = AALocalized("AuthOTPPhoneTitle")
        }
        welcomeLabel.textColor = ActorSDK.sharedActor().style.authTitleColor
        welcomeLabel.textAlignment = .center
        
        validateLabel.font = UIFont.systemFont(ofSize: 14)
        if email != nil {
            validateLabel.text = email
        } else {
            validateLabel.text = phone
        }
        validateLabel.textColor = ActorSDK.sharedActor().style.authTintColor
        validateLabel.textAlignment = .center
        
        hintLabel.font = UIFont.systemFont(ofSize: 14)
        if email != nil {
            hintLabel.text = AALocalized("AuthOTPEmailHint")
        } else {
            hintLabel.text = AALocalized("AuthOTPPhoneHint")
        }
        hintLabel.textColor = ActorSDK.sharedActor().style.authHintColor
        hintLabel.textAlignment = .center
        hintLabel.numberOfLines = 2
        hintLabel.lineBreakMode = .byWordWrapping
        
        codeField.font = UIFont.systemFont(ofSize: 17)
        codeField.textColor = ActorSDK.sharedActor().style.authTextColor
        codeField.placeholder = AALocalized("AuthOTPPlaceholder")
        codeField.keyboardType = .numberPad
        codeField.autocapitalizationType = .none
        codeField.autocorrectionType = .no
        
        codeFieldLine.backgroundColor = ActorSDK.sharedActor().style.authSeparatorColor
        
        if ActorSDK.sharedActor().supportEmail != nil {
            haventReceivedCode.setTitle(AALocalized("AuthOTPNoCode"), for: UIControlState())
        } else {
            haventReceivedCode.isHidden = true
        }
        haventReceivedCode.titleLabel?.font = UIFont.systemFont(ofSize: 14)
        haventReceivedCode.setTitleColor(ActorSDK.sharedActor().style.authTintColor, for: UIControlState())
        haventReceivedCode.setTitleColor(ActorSDK.sharedActor().style.authTintColor.alpha(0.64), for: .highlighted)
        haventReceivedCode.setTitleColor(ActorSDK.sharedActor().style.authHintColor, for: .disabled)
        haventReceivedCode.addTarget(self, action: #selector(AAAuthOTPViewController.haventReceivedCodeDidPressed), for: .touchUpInside)
        
        scrollView.addSubview(welcomeLabel)
        scrollView.addSubview(validateLabel)
        scrollView.addSubview(hintLabel)
        scrollView.addSubview(codeField)
        scrollView.addSubview(codeFieldLine)
        scrollView.addSubview(haventReceivedCode)
        
        view.addSubview(scrollView)
        super.viewDidLoad()
    }
    
    open override func viewDidLayoutSubviews() {
        super.viewDidLayoutSubviews()
        
        welcomeLabel.frame = CGRect(x: 15, y: 90 - 66, width: view.width - 30, height: 28)
        validateLabel.frame = CGRect(x: 10, y: 127 - 66, width: view.width - 20, height: 17)
        hintLabel.frame = CGRect(x: 10, y: 154 - 66, width: view.width - 20, height: 56)
        
        codeField.frame = CGRect(x: 20, y: 228 - 66, width: view.width - 40, height: 44)
        codeFieldLine.frame = CGRect(x: 10, y: 228 + 44 - 66, width: view.width - 20, height: 0.5)
        
        haventReceivedCode.frame = CGRect(x: 20, y: 297 - 66, width: view.width - 40, height: 56)
        
        scrollView.frame = view.bounds
        scrollView.contentSize = CGSize(width: view.width, height: 240 - 66)
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
    
    open func mailComposeController(_ controller: MFMailComposeViewController, didFinishWith result: MFMailComposeResult, error: Error?) {
        controller.dismiss(animated: true, completion: nil)
    }
    
    open override func nextDidTap() {
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
                    let promise = Actor.doSignup(withName: self.name, with: ACSex.unknown(), withTransaction: r.transactionHash)
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
    
    fileprivate func shakeField() {
        shakeView(codeField, originalX: 20)
        shakeView(codeFieldLine, originalX: 10)
    }
    
    open override func viewWillAppear(_ animated: Bool) {
        super.viewWillAppear(animated)
        
        if self.phone != nil {
        
            updateTimerText()
        
            if !dialed {
                counterTimer = Timer.scheduledTimer(timeInterval: 1.0, target: self, selector: #selector(AAAuthOTPViewController.updateTimer), userInfo: nil, repeats: true)
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
            
            Actor.doSendCode(viaCall: self.transactionHash)
        }
        
        updateTimerText()
    }
    
    
    func updateTimerText() {
        if dialed {
            if ActorSDK.sharedActor().supportEmail != nil {
                haventReceivedCode.setTitle(AALocalized("AuthOTPNoCode"), for: UIControlState())
                haventReceivedCode.isHidden = false
                haventReceivedCode.isEnabled = true
            } else {
                haventReceivedCode.isHidden = true
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
            haventReceivedCode.setTitle(text, for: UIControlState())
            haventReceivedCode.isEnabled = false
            haventReceivedCode.isHidden = false
        }
    }
    
    open override func viewWillDisappear(_ animated: Bool) {
        super.viewWillDisappear(animated)
        
        if counterTimer != nil {
            counterTimer.invalidate()
            counterTimer = nil
        }
        
        self.codeField.resignFirstResponder()
    }
}


