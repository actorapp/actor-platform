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
        
        view.backgroundColor = UIColor.white
        
        scrollView.keyboardDismissMode = .onDrag
        scrollView.isScrollEnabled = true
        scrollView.alwaysBounceVertical = true
        
        welcomeLabel.font = UIFont.lightSystemFontOfSize(23)
        welcomeLabel.textColor = ActorSDK.sharedActor().style.authTitleColor
        welcomeLabel.text = AALocalized("AuthPhoneTitle")
        welcomeLabel.numberOfLines = 1
        welcomeLabel.minimumScaleFactor = 0.3
        welcomeLabel.adjustsFontSizeToFitWidth = true
        welcomeLabel.textAlignment = .center
        
        hintLabel.font = UIFont.systemFont(ofSize: 14)
        hintLabel.textColor = ActorSDK.sharedActor().style.authHintColor
        hintLabel.text = AALocalized("AuthPhoneHint")
        hintLabel.numberOfLines = 2
        hintLabel.textAlignment = .center
        
        countryButton.setTitle(currentCountry.country, for: UIControlState())
        countryButton.setTitleColor(ActorSDK.sharedActor().style.authTextColor, for: UIControlState())
        countryButton.titleLabel!.font = UIFont.systemFont(ofSize: 17)
        countryButton.titleEdgeInsets = UIEdgeInsetsMake(11, 10, 11, 10)
        countryButton.contentHorizontalAlignment = .left
        countryButton.setBackgroundImage(Imaging.imageWithColor(UIColor.alphaBlack(0.2), size: CGSize(width: 1, height: 1)), for: .highlighted)
        countryButton.addTarget(self, action: #selector(AAAuthPhoneViewController.countryDidPressed), for: .touchUpInside)
        
        countryButtonLine.backgroundColor = ActorSDK.sharedActor().style.authSeparatorColor
        
        phoneCodeLabel.font = UIFont.systemFont(ofSize: 17)
        phoneCodeLabel.textColor = ActorSDK.sharedActor().style.authHintColor
        phoneCodeLabel.text = "+\(currentCountry.code)"
        phoneCodeLabel.textAlignment = .center
        
        phoneNumberLabel.currentIso = currentCountry.iso
        phoneNumberLabel.keyboardType = .phonePad
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
                        self.present(AABigAlertController(alertTitle: tosText, alertMessage: text), animated: true, completion: nil)
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
                        self.present(AABigAlertController(alertTitle: privacyText, alertMessage: text), animated: true, completion: nil)
                    }
                }
                attributedTerms.yy_setColor(ActorSDK.sharedActor().style.authTintColor, range: privacyRange)
                attributedTerms.yy_setTextHighlight(privacyLink, range: privacyRange)
            }
            
        
            termsLabel.attributedText = attributedTerms
            termsLabel.font = UIFont.systemFont(ofSize: 14)
            termsLabel.numberOfLines = 2
            termsLabel.textAlignment = .center
        } else {
            termsLabel.isHidden = true
        }
        
        
        if ActorSDK.sharedActor().authStrategy == .emailOnly || ActorSDK.sharedActor().authStrategy == .phoneEmail {
            useEmailButton.setTitle(AALocalized("AuthPhoneUseEmail"), for: UIControlState())
            useEmailButton.titleLabel?.font = UIFont.systemFont(ofSize: 14)
            useEmailButton.setTitleColor(ActorSDK.sharedActor().style.authTintColor, for: UIControlState())
            useEmailButton.setTitleColor(ActorSDK.sharedActor().style.authTintColor.alpha(0.56), for: .highlighted)
            useEmailButton.addTarget(self, action: #selector(AAAuthPhoneViewController.useEmailDidPressed), for: .touchUpInside)
        } else {
            useEmailButton.isHidden = true
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
        
        welcomeLabel.frame = CGRect(x: 20, y: 90 - 66, width: view.width - 40, height: 28)
        hintLabel.frame = CGRect(x: 20, y: 127 - 66, width: view.width - 40, height: 34)
        
        countryButton.frame = CGRect(x: 10, y: 200 - 66, width: view.width - 20, height: 44)
        countryButtonLine.frame = CGRect(x: 10, y: 244 - 66, width: view.width - 20, height: 0.5)
        
        termsLabel.frame = CGRect(x: 20, y: 314 - 66, width: view.width - 40, height: 55)
        
        useEmailButton.frame = CGRect(x: 20, y: 375 - 66, width: view.width - 40, height: 38)
        
        resizePhoneLabels()
        
        scrollView.frame = view.bounds
        scrollView.contentSize = CGSize(width: view.width, height: 400)
    }
    
    fileprivate func resizePhoneLabels() {
        phoneCodeLabel.frame = CGRect(x: 10, y: 244 - 66, width: 80, height: 44)
        phoneCodeLabel.sizeToFit()
        phoneCodeLabel.frame = CGRect(x: 10, y: 244 - 66, width: phoneCodeLabel.width + 32, height: 44)
        
        phoneNumberLabel.frame = CGRect(x: phoneCodeLabel.width + 10, y: 245 - 66, width: view.width - phoneCodeLabel.width, height: 44)
        phoneCodeLabelLine.frame = CGRect(x: 10, y: 288 - 66, width: view.width - 20, height: 0.5)
    }
    
    func countriesController(_ countriesController: AACountryViewController, didChangeCurrentIso currentIso: String) {
        currentCountry = AATelephony.getCountry(currentIso)
        countryButton.setTitle(currentCountry.country, for: UIControlState())
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
        
        Actor.doStartAuth(withPhone: number).startUserAction().then { (res: ACAuthStartRes!) -> () in
            if res.authMode.toNSEnum() == .OTP {
                self.navigateNext(AAAuthOTPViewController(phone: numberStr!, name: self.name, transactionHash: res.transactionHash))
            } else {
                self.alertUser(AALocalized("AuthUnsupported").replace("{app_name}", dest: ActorSDK.sharedActor().appName))
            }
        }
    }
    
    override func keyboardWillAppear(_ height: CGFloat) {
        scrollView.frame = CGRect(x: 0, y: 0, width: view.width, height: view.height - height)
        
        if AADevice.isiPhone4 || AADevice.isiPhone5 {
            
            let height = scrollView.height - height
            let offset: CGFloat = 245 + 44
            let destOffset = height * 0.66  - offset / 2
        
            scrollView.setContentOffset(CGPoint(x: 0, y: -destOffset), animated: true)
        }
    }
    
    override func keyboardWillDisappear() {
        scrollView.frame = CGRect(x: 0, y: 0, width: view.width, height: view.height)
    }
    
    override func viewWillDisappear(_ animated: Bool) {
        super.viewWillDisappear(animated)
        
        phoneNumberLabel.resignFirstResponder()

    }
}
