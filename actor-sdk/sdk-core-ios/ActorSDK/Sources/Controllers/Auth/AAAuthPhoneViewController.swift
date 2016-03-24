//
//  Copyright (c) 2014-2016 Actor LLC. <https://actor.im>
//

import Foundation

class AAAuthPhoneViewController: AAAuthViewController, AACountryViewControllerDelegate {
    
    let name: String
    
    let scrollView = UIScrollView()
    
    let welcomeLabel = UILabel()
    let hintLabel = UILabel()
    
    let countryButton = UIButton()
    let countryButtonLine = UIView()
    var currentCountry: CountryDesc
    
    let phoneCodeLabel = UILabel()
    let phoneNumberLabel = ABPhoneField()
    let phoneCodeLabelLine = UIView()
    
    let termsLabel = YYLabel()
    let useEmailButton = UIButton()
    
    init(name: String) {
        self.name = name
        self.currentCountry = AATelephony.getCountry(AATelephony.loadDefaultISOCountry())
        super.init()
    }
    
    required init(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }

    override func viewDidLoad() {
        
        view.backgroundColor = UIColor.whiteColor()
        
        scrollView.keyboardDismissMode = .OnDrag
        scrollView.scrollEnabled = true
        scrollView.alwaysBounceVertical = true
        
        welcomeLabel.font = UIFont.lightSystemFontOfSize(23)
        welcomeLabel.textColor = ActorSDK.sharedActor().style.authTitleColor
        welcomeLabel.text = AALocalized("AuthPhoneTitle")
        welcomeLabel.numberOfLines = 1
        welcomeLabel.minimumScaleFactor = 0.3
        welcomeLabel.adjustsFontSizeToFitWidth = true
        welcomeLabel.textAlignment = .Center
        
        hintLabel.font = UIFont.systemFontOfSize(14)
        hintLabel.textColor = ActorSDK.sharedActor().style.authHintColor
        hintLabel.text = AALocalized("AuthPhoneHint")
        hintLabel.numberOfLines = 2
        hintLabel.textAlignment = .Center
        
        countryButton.setTitle(currentCountry.country, forState: .Normal)
        countryButton.setTitleColor(ActorSDK.sharedActor().style.authTextColor, forState: .Normal)
        countryButton.titleLabel!.font = UIFont.systemFontOfSize(17)
        countryButton.titleEdgeInsets = UIEdgeInsetsMake(11, 10, 11, 10)
        countryButton.contentHorizontalAlignment = .Left
        countryButton.setBackgroundImage(Imaging.imageWithColor(UIColor.alphaBlack(0.2), size: CGSizeMake(1, 1)), forState: .Highlighted)
        countryButton.addTarget(self, action: #selector(AAAuthPhoneViewController.countryDidPressed), forControlEvents: .TouchUpInside)
        
        countryButtonLine.backgroundColor = ActorSDK.sharedActor().style.authSeparatorColor
        
        phoneCodeLabel.font = UIFont.systemFontOfSize(17)
        phoneCodeLabel.textColor = ActorSDK.sharedActor().style.authHintColor
        phoneCodeLabel.text = "+\(currentCountry.code)"
        phoneCodeLabel.textAlignment = .Center
        
        phoneNumberLabel.currentIso = currentCountry.iso
        phoneNumberLabel.keyboardType = .PhonePad
        phoneNumberLabel.placeholder = AALocalized("AuthPhonePlaceholder")
        phoneNumberLabel.textColor = ActorSDK.sharedActor().style.authTextColor
        
        phoneCodeLabelLine.backgroundColor = ActorSDK.sharedActor().style.authSeparatorColor
        
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
                let tosLink = YYTextHighlight()
                let tosRange = NSRange(location: hintText.indexOf(tosText)!, length: tosText.length)
                tosLink.setColor(ActorSDK.sharedActor().style.authTintColor.alpha(0.56))
                tosLink.tapAction = { (container, text, range, rect) in
                    if let url = ActorSDK.sharedActor().termsOfServiceUrl {
                        self.openUrl(url)
                    } else if let text = ActorSDK.sharedActor().termsOfServiceText {
                        self.presentViewController(AABigAlertController(alertTitle: tosText, alertMessage: text), animated: true, completion: nil)
                    }
                }
                attributedTerms.yy_setColor(ActorSDK.sharedActor().style.authTintColor, range: tosRange)
                attributedTerms.yy_setTextHighlight(tosLink, range: tosRange)
            }
        
            
            //
            // Privacy Policy
            //
            if showPrivacy {
                let privacyLink = YYTextHighlight()
                let privacyRange = NSRange(location: hintText.indexOf(privacyText)!, length: privacyText.length)
                privacyLink.setColor(ActorSDK.sharedActor().style.authTintColor.alpha(0.56))
                privacyLink.tapAction = { (container, text, range, rect) in
                    if let url = ActorSDK.sharedActor().privacyPolicyUrl {
                        self.openUrl(url)
                    } else if let text = ActorSDK.sharedActor().privacyPolicyText {
                        self.presentViewController(AABigAlertController(alertTitle: privacyText, alertMessage: text), animated: true, completion: nil)
                    }
                }
                attributedTerms.yy_setColor(ActorSDK.sharedActor().style.authTintColor, range: privacyRange)
                attributedTerms.yy_setTextHighlight(privacyLink, range: privacyRange)
            }
            
        
            termsLabel.attributedText = attributedTerms
            termsLabel.font = UIFont.systemFontOfSize(14)
            termsLabel.numberOfLines = 2
            termsLabel.textAlignment = .Center
        } else {
            termsLabel.hidden = true
        }
        
        
        if ActorSDK.sharedActor().authStrategy == .EmailOnly || ActorSDK.sharedActor().authStrategy == .PhoneEmail {
            useEmailButton.setTitle(AALocalized("AuthPhoneUseEmail"), forState: .Normal)
            useEmailButton.titleLabel?.font = UIFont.systemFontOfSize(14)
            useEmailButton.setTitleColor(ActorSDK.sharedActor().style.authTintColor, forState: .Normal)
            useEmailButton.setTitleColor(ActorSDK.sharedActor().style.authTintColor.alpha(0.56), forState: .Highlighted)
            useEmailButton.addTarget(self, action: #selector(AAAuthPhoneViewController.useEmailDidPressed), forControlEvents: .TouchUpInside)
        } else {
            useEmailButton.hidden = true
        }
        
        
        scrollView.addSubview(welcomeLabel)
        scrollView.addSubview(hintLabel)
        scrollView.addSubview(countryButton)
        scrollView.addSubview(countryButtonLine)
        scrollView.addSubview(phoneCodeLabel)
        scrollView.addSubview(phoneNumberLabel)
        scrollView.addSubview(phoneCodeLabelLine)
        scrollView.addSubview(termsLabel)
        scrollView.addSubview(useEmailButton)
        view.addSubview(scrollView)
        
        super.viewDidLoad()
    }
    

    
    override func viewDidLayoutSubviews() {
        super.viewDidLayoutSubviews()
        
        welcomeLabel.frame = CGRectMake(20, 90 - 66, view.width - 40, 28)
        hintLabel.frame = CGRectMake(20, 127 - 66, view.width - 40, 34)
        
        countryButton.frame = CGRectMake(10, 200 - 66, view.width - 20, 44)
        countryButtonLine.frame = CGRectMake(10, 244 - 66, view.width - 20, 0.5)
        
        termsLabel.frame = CGRectMake(20, 314 - 66, view.width - 40, 55)
        
        useEmailButton.frame = CGRectMake(20, 375 - 66, view.width - 40, 38)
        
        resizePhoneLabels()
        
        scrollView.frame = view.bounds
        scrollView.contentSize = CGSizeMake(view.width, 400)
    }
    
    private func resizePhoneLabels() {
        phoneCodeLabel.frame = CGRectMake(10, 244 - 66, 80, 44)
        phoneCodeLabel.sizeToFit()
        phoneCodeLabel.frame = CGRectMake(10, 244 - 66, phoneCodeLabel.width + 32, 44)
        
        phoneNumberLabel.frame = CGRectMake(phoneCodeLabel.width + 10, 245 - 66, view.width - phoneCodeLabel.width, 44)
        phoneCodeLabelLine.frame = CGRectMake(10, 288 - 66, view.width - 20, 0.5)
    }
    
    func countriesController(countriesController: AACountryViewController, didChangeCurrentIso currentIso: String) {
        currentCountry = AATelephony.getCountry(currentIso)
        countryButton.setTitle(currentCountry.country, forState: .Normal)
        phoneCodeLabel.text = "+\(currentCountry.code)"
        phoneNumberLabel.currentIso = currentIso
        resizePhoneLabels()
    }
    
    func countryDidPressed() {
        let countriesController = AACountryViewController()
        countriesController.delegate = self
        presentElegantViewController(AANavigationController(rootViewController: countriesController))
    }
    
    func useEmailDidPressed() {
        let controllers = self.navigationController!.viewControllers
        let updatedControllers = Array(controllers[0..<(controllers.count - 1)]) + [AAAuthEmailViewController(name: name)]
        self.navigationController?.setViewControllers(updatedControllers, animated: false)
    }
    
    override func nextDidTap() {
        let numberStr = phoneNumberLabel.phoneNumber
        let number = phoneNumberLabel.phoneNumber.toJLong()
        
        Actor.doStartAuthWithPhone(number).startUserAction().then { (res: ACAuthStartRes!) -> () in
            if res.authMode.toNSEnum() == .OTP {
                self.navigateNext(AAAuthOTPViewController(phone: numberStr, name: self.name, transactionHash: res.transactionHash))
            } else {
                self.alertUser(AALocalized("AuthUnsupported").replace("{app_name}", dest: ActorSDK.sharedActor().appName))
            }
        }
    }
    
    override func keyboardWillAppear(height: CGFloat) {
        scrollView.frame = CGRectMake(0, 0, view.width, view.height - height)
        
        if AADevice.isiPhone4 || AADevice.isiPhone5 {
            
            let height = scrollView.height - height
            let offset: CGFloat = 245 + 44
            let destOffset = height * 0.66  - offset / 2
        
            scrollView.setContentOffset(CGPoint(x: 0, y: -destOffset), animated: true)
        }
    }
    
    override func keyboardWillDisappear() {
        scrollView.frame = CGRectMake(0, 0, view.width, view.height)
    }
    
    override func viewWillDisappear(animated: Bool) {
        super.viewWillDisappear(animated)
        
        phoneNumberLabel.resignFirstResponder()

    }
}