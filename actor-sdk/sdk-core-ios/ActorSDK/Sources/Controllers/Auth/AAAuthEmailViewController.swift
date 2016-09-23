//
//  Copyright (c) 2014-2016 Actor LLC. <https://actor.im>
//

import Foundation

open class AAAuthEmailViewController: AAAuthViewController {
    
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
    
    open override func viewDidLoad() {
        
        view.backgroundColor = UIColor.white
        
        scrollView.keyboardDismissMode = .onDrag
        scrollView.isScrollEnabled = true
        scrollView.alwaysBounceVertical = true
        
        welcomeLabel.font = UIFont.lightSystemFontOfSize(23)
        welcomeLabel.textColor = ActorSDK.sharedActor().style.authTitleColor
        welcomeLabel.text = AALocalized("AuthEmailTitle")
        welcomeLabel.numberOfLines = 1
        welcomeLabel.minimumScaleFactor = 0.3
        welcomeLabel.adjustsFontSizeToFitWidth = true
        welcomeLabel.textAlignment = .center
        
        hintLabel.font = UIFont.systemFont(ofSize: 14)
        hintLabel.textColor = ActorSDK.sharedActor().style.authHintColor
        hintLabel.text = AALocalized("AuthEmailHint")
        hintLabel.numberOfLines = 1
        hintLabel.textAlignment = .center
        
        emailField.font = UIFont.systemFont(ofSize: 17)
        emailField.textColor = ActorSDK.sharedActor().style.authTextColor
        emailField.placeholder = AALocalized("AuthEmailPlaceholder")
        emailField.keyboardType = .emailAddress
        emailField.autocapitalizationType = .none
        emailField.autocorrectionType = .no
        
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
            termsLabel.font = UIFont.systemFont(ofSize: 14)
            termsLabel.numberOfLines = 2
            termsLabel.textAlignment = .center
        } else {
            termsLabel.isHidden = true
        }
        
        if ActorSDK.sharedActor().authStrategy == .phoneOnly || ActorSDK.sharedActor().authStrategy == .phoneEmail {
            usePhoneButton.setTitle(AALocalized("AuthEmailUsePhone"), for: UIControlState())
            usePhoneButton.titleLabel?.font = UIFont.systemFont(ofSize: 14)
            usePhoneButton.setTitleColor(ActorSDK.sharedActor().style.authTintColor, for: UIControlState())
            usePhoneButton.setTitleColor(ActorSDK.sharedActor().style.authTintColor.alpha(0.56), for: .highlighted)
            usePhoneButton.addTarget(self, action: #selector(AAAuthEmailViewController.usePhoneDidPressed), for: .touchUpInside)
        } else {
            usePhoneButton.isHidden = true
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
    
    open override func viewDidLayoutSubviews() {
        super.viewDidLayoutSubviews()
        
        welcomeLabel.frame = CGRect(x: 20, y: 90 - 66, width: view.width - 40, height: 28)
        hintLabel.frame = CGRect(x: 20, y: 127 - 66, width: view.width - 40, height: 18)
        
        emailField.frame = CGRect(x: 20, y: 184 - 66, width: view.width - 40, height: 44)
        emailFieldLine.frame = CGRect(x: 10, y: 228 - 66, width: view.width - 20, height: 0.5)
        
        termsLabel.frame = CGRect(x: 20, y: 314 - 66, width: view.width - 40, height: 55)
        
        usePhoneButton.frame = CGRect(x: 20, y: 375 - 66, width: view.width - 40, height: 38)
        
        scrollView.frame = view.bounds
        scrollView.contentSize = CGSize(width: view.width, height: 420)
    }
    
    open func usePhoneDidPressed() {
        let controllers = self.navigationController!.viewControllers
        let updatedControllers = Array(controllers[0..<(controllers.count - 1)]) + [AAAuthPhoneViewController(name: name)]
        self.navigationController?.setViewControllers(updatedControllers, animated: false)
    }
    
    open override func nextDidTap() {
        let email = emailField.text!
        
        if !AATools.isValidEmail(email) {
            shakeView(emailField, originalX: view.width / 2)
            shakeView(emailFieldLine, originalX: view.width / 2)
            return
        }
        
        Actor.doStartAuth(withEmail: email).startUserAction().then { (res: ACAuthStartRes!) -> () in
            if res.authMode.toNSEnum() == .OTP {
                self.navigateNext(AAAuthOTPViewController(email: email, name: self.name, transactionHash: res.transactionHash))
            } else {
                self.alertUser(AALocalized("AuthUnsupported").replace("{app_name}", dest: ActorSDK.sharedActor().appName))
            }
        }
    }
    
    open override func keyboardWillAppear(_ height: CGFloat) {
        scrollView.frame = CGRect(x: 0, y: 0, width: view.width, height: view.height - height)
        
        if AADevice.isiPhone4 || AADevice.isiPhone5 {
            let height = scrollView.height - height
            let offset: CGFloat = 184 + 44
            let destOffset = height * 0.8  - offset / 2
        
            scrollView.setContentOffset(CGPoint(x: 0, y: -destOffset), animated: true)
        }
    }
    
    open override func keyboardWillDisappear() {
        scrollView.frame = CGRect(x: 0, y: 0, width: view.width, height: view.height)
    }
    
    open override func viewWillDisappear(_ animated: Bool) {
        super.viewWillDisappear(animated)
        
        emailField.resignFirstResponder()
    }
}
