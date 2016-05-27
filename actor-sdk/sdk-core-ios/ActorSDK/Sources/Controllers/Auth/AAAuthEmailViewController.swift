//
//  Copyright (c) 2014-2016 Actor LLC. <https://actor.im>
//

import Foundation

public class AAAuthEmailViewController: AAAuthViewController {
    
    let name: String
    
    let scrollView = UIScrollView()
    
    let welcomeLabel = UILabel()
    let hintLabel = UILabel()
    
    let emailField = UITextField()
    let emailFieldLine = UIView()
    
    let termsLabel = YYLabel()
    let usePhoneButton = UIButton()
    
    public init(name: String) {
        self.name = name
        super.init(nibName: nil, bundle: nil)
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
        welcomeLabel.textColor = ActorSDK.sharedActor().style.authTitleColor
        welcomeLabel.text = AALocalized("AuthEmailTitle")
        welcomeLabel.numberOfLines = 1
        welcomeLabel.minimumScaleFactor = 0.3
        welcomeLabel.adjustsFontSizeToFitWidth = true
        welcomeLabel.textAlignment = .Center
        
        hintLabel.font = UIFont.systemFontOfSize(14)
        hintLabel.textColor = ActorSDK.sharedActor().style.authHintColor
        hintLabel.text = AALocalized("AuthEmailHint")
        hintLabel.numberOfLines = 1
        hintLabel.textAlignment = .Center
        
        emailField.font = UIFont.systemFontOfSize(17)
        emailField.textColor = ActorSDK.sharedActor().style.authTextColor
        emailField.placeholder = AALocalized("AuthEmailPlaceholder")
        emailField.keyboardType = .EmailAddress
        emailField.autocapitalizationType = .None
        emailField.autocorrectionType = .No
        
        emailFieldLine.backgroundColor = ActorSDK.sharedActor().style.authSeparatorColor
        
        let showTos = ActorSDK.sharedActor().termsOfServiceText != nil || ActorSDK.sharedActor().termsOfServiceUrl != nil
        let showPrivacy = ActorSDK.sharedActor().privacyPolicyText != nil || ActorSDK.sharedActor().privacyPolicyUrl != nil
        
        if showTos || showPrivacy {
            let tosText = AALocalized("AuthDisclaimerToS")
            let privacyText = AALocalized("AuthDisclaimerPrivacy")
            let hintText: String
            if showTos && showPrivacy {
                hintText = AALocalized("AuthDisclaimer")
            } else if showTos {
                hintText = AALocalized("AuthDisclaimerTosOnly")
            } else {
                hintText = AALocalized("AuthDisclaimerProvacyOnly")
            }

            
            
            
            let attributedTerms = NSMutableAttributedString(string: hintText)
            attributedTerms.yy_color = ActorSDK.sharedActor().style.authHintColor
            
            //
            // Terms Of Service
            //
            if showTos {
                let tosRange = NSRange(location: hintText.indexOf(tosText)!, length: tosText.length)
                let tosLink = YYTextHighlight()
                tosLink.setColor(ActorSDK.sharedActor().style.authTintColor.alpha(0.56))
                tosLink.tapAction = { (container, text, range, rect) in
                    self.openUrl(ActorSDK.sharedActor().termsOfServiceUrl!)
                }
                attributedTerms.yy_setColor(ActorSDK.sharedActor().style.authTintColor.alpha(0.56).alpha(0.56), range: tosRange)
                attributedTerms.yy_setTextHighlight(tosLink, range: tosRange)
            }
            
            
            //
            // Privacy Policy
            //
            if showPrivacy {
                let privacyRange = NSRange(location: hintText.indexOf(privacyText)!, length: privacyText.length)
                let privacyLink = YYTextHighlight()
                privacyLink.setColor(ActorSDK.sharedActor().style.authTintColor.alpha(0.56))
                privacyLink.tapAction = { (container, text, range, rect) in
                    self.openUrl(ActorSDK.sharedActor().privacyPolicyUrl!)
                }
                attributedTerms.yy_setColor(ActorSDK.sharedActor().style.authTintColor.alpha(0.56).alpha(0.56), range: privacyRange)
                attributedTerms.yy_setTextHighlight(privacyLink, range: privacyRange)
            }
            
            termsLabel.attributedText = attributedTerms
            termsLabel.font = UIFont.systemFontOfSize(14)
            termsLabel.numberOfLines = 2
            termsLabel.textAlignment = .Center
        } else {
            termsLabel.hidden = true
        }
        
        if ActorSDK.sharedActor().authStrategy == .PhoneOnly || ActorSDK.sharedActor().authStrategy == .PhoneEmail {
            usePhoneButton.setTitle(AALocalized("AuthEmailUsePhone"), forState: .Normal)
            usePhoneButton.titleLabel?.font = UIFont.systemFontOfSize(14)
            usePhoneButton.setTitleColor(ActorSDK.sharedActor().style.authTintColor, forState: .Normal)
            usePhoneButton.setTitleColor(ActorSDK.sharedActor().style.authTintColor.alpha(0.56), forState: .Highlighted)
            usePhoneButton.addTarget(self, action: #selector(AAAuthEmailViewController.usePhoneDidPressed), forControlEvents: .TouchUpInside)
        } else {
            usePhoneButton.hidden = true
        }
        
        scrollView.addSubview(welcomeLabel)
        scrollView.addSubview(hintLabel)
        scrollView.addSubview(emailField)
        scrollView.addSubview(emailFieldLine)
        scrollView.addSubview(usePhoneButton)
        scrollView.addSubview(termsLabel)
        view.addSubview(scrollView)
        
        super.viewDidLoad()
    }
    
    public override func viewDidLayoutSubviews() {
        super.viewDidLayoutSubviews()
        
        welcomeLabel.frame = CGRectMake(20, 90 - 66, view.width - 40, 28)
        hintLabel.frame = CGRectMake(20, 127 - 66, view.width - 40, 18)
        
        emailField.frame = CGRectMake(20, 184 - 66, view.width - 40, 44)
        emailFieldLine.frame = CGRectMake(10, 228 - 66, view.width - 20, 0.5)
        
        termsLabel.frame = CGRectMake(20, 314 - 66, view.width - 40, 55)
        
        usePhoneButton.frame = CGRectMake(20, 375 - 66, view.width - 40, 38)
        
        scrollView.frame = view.bounds
        scrollView.contentSize = CGSizeMake(view.width, 420)
    }
    
    public func usePhoneDidPressed() {
        let controllers = self.navigationController!.viewControllers
        let updatedControllers = Array(controllers[0..<(controllers.count - 1)]) + [AAAuthPhoneViewController(name: name)]
        self.navigationController?.setViewControllers(updatedControllers, animated: false)
    }
    
    public override func nextDidTap() {
        let email = emailField.text!
        
        if !AATools.isValidEmail(email) {
            shakeView(emailField, originalX: view.width / 2)
            shakeView(emailFieldLine, originalX: view.width / 2)
            return
        }
        
        Actor.doStartAuthWithEmail(email).startUserAction().then { (res: ACAuthStartRes!) -> () in
            if res.authMode.toNSEnum() == .OTP {
                self.navigateNext(AAAuthOTPViewController(email: email, name: self.name, transactionHash: res.transactionHash))
            } else {
                self.alertUser(AALocalized("AuthUnsupported").replace("{app_name}", dest: ActorSDK.sharedActor().appName))
            }
        }
    }
    
    public override func keyboardWillAppear(height: CGFloat) {
        scrollView.frame = CGRectMake(0, 0, view.width, view.height - height)
        
        if AADevice.isiPhone4 || AADevice.isiPhone5 {
            let height = scrollView.height - height
            let offset: CGFloat = 184 + 44
            let destOffset = height * 0.8  - offset / 2
        
            scrollView.setContentOffset(CGPoint(x: 0, y: -destOffset), animated: true)
        }
    }
    
    public override func keyboardWillDisappear() {
        scrollView.frame = CGRectMake(0, 0, view.width, view.height)
    }
    
    public override func viewWillDisappear(animated: Bool) {
        super.viewWillDisappear(animated)
        
        emailField.resignFirstResponder()
    }
}