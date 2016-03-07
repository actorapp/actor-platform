//
//  Copyright (c) 2014-2016 Actor LLC. <https://actor.im>
//

import Foundation
import YYText

class AAAuthPhoneViewController: AAAuthViewController, AACountryViewControllerDelegate {
    
    let name: String
    
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
        super.viewDidLoad()
        
        view.backgroundColor = UIColor.whiteColor()
        
        welcomeLabel.font = UIFont.lightSystemFontOfSize(23)
        welcomeLabel.textColor = UIColor.alphaBlack(0.87)
        welcomeLabel.text = "What's your phone number?"
        welcomeLabel.numberOfLines = 1
        welcomeLabel.minimumScaleFactor = 0.3
        welcomeLabel.adjustsFontSizeToFitWidth = true
        welcomeLabel.textAlignment = .Center
        
        hintLabel.font = UIFont.systemFontOfSize(14)
        hintLabel.textColor = UIColor.alphaBlack(0.64)
        hintLabel.text = "We need your phone number to grant\nsecurity of your personal information."
        hintLabel.numberOfLines = 2
        hintLabel.textAlignment = .Center
        
        countryButton.setTitle(currentCountry.country, forState: .Normal)
        countryButton.setTitleColor(UIColor.alphaBlack(0.87), forState: .Normal)
        countryButton.titleLabel!.font = UIFont.systemFontOfSize(17)
        countryButton.titleEdgeInsets = UIEdgeInsetsMake(11, 10, 11, 10)
        countryButton.contentHorizontalAlignment = .Left
        countryButton.setBackgroundImage(Imaging.imageWithColor(UIColor.alphaBlack(0.2), size: CGSizeMake(1, 1)), forState: .Highlighted)
        countryButton.addTarget(self, action: "countryDidPressed", forControlEvents: .TouchUpInside)
        
        countryButtonLine.backgroundColor = UIColor.alphaBlack(0.2)
        
        phoneCodeLabel.font = UIFont.systemFontOfSize(17)
        phoneCodeLabel.textColor = UIColor.alphaBlack(0.56)
        phoneCodeLabel.text = "+\(currentCountry.code)"
        phoneCodeLabel.textAlignment = .Center
        
        phoneNumberLabel.currentIso = currentCountry.iso
        
        phoneCodeLabelLine.backgroundColor = UIColor.alphaBlack(0.2)
        
        let hintText = "By singing up, you agree with\nTerms of Service and Privacy Policy."
        let tosRange = NSRange(location: 30, length: 16)
        let privacyRange = NSRange(location: 51, length: 14)

        let attributedTerms = NSMutableAttributedString(string: hintText)
        attributedTerms.yy_color = UIColor.alphaBlack(0.56)
        
        //
        // Terms Of Service
        //
        
        let tosLink = YYTextHighlight()
        tosLink.setColor(UIColor.blueColor().alpha(0.2))
        tosLink.tapAction = { (container, text, range, rect) in
            self.openUrl("https://actor.im/tos")
        }
        attributedTerms.yy_setColor(UIColor.blueColor().alpha(0.56), range: tosRange)
        attributedTerms.yy_setTextHighlight(tosLink, range: tosRange)
        
        
        //
        // Privacy Policy
        //
        
        let privacyLink = YYTextHighlight()
        privacyLink.setColor(UIColor.blueColor().alpha(0.2))
        privacyLink.tapAction = { (container, text, range, rect) in
            self.openUrl("https://actor.im/privacy")
        }
        attributedTerms.yy_setColor(UIColor.blueColor().alpha(0.56), range: privacyRange)
        attributedTerms.yy_setTextHighlight(privacyLink, range: privacyRange)
        
        
        termsLabel.attributedText = attributedTerms
        termsLabel.font = UIFont.systemFontOfSize(14)
        termsLabel.numberOfLines = 2
        termsLabel.textAlignment = .Center
        
        useEmailButton.setTitle("Use email isntead", forState: .Normal)
        useEmailButton.titleLabel?.font = UIFont.systemFontOfSize(14)
        useEmailButton.setTitleColor(UIColor.blueColor().alpha(0.56), forState: .Normal)
        useEmailButton.addTarget(self, action: "useEmailDidPressed", forControlEvents: .TouchUpInside)
        
        view.addSubview(welcomeLabel)
        view.addSubview(hintLabel)
        view.addSubview(countryButton)
        view.addSubview(countryButtonLine)
        view.addSubview(phoneCodeLabel)
        view.addSubview(phoneNumberLabel)
        view.addSubview(phoneCodeLabelLine)
        view.addSubview(termsLabel)
        view.addSubview(useEmailButton)
    }
    
    override func viewDidLayoutSubviews() {
        super.viewDidLayoutSubviews()
        
        welcomeLabel.frame = CGRectMake(20, 90, view.width - 40, 28)
        hintLabel.frame = CGRectMake(20, 127, view.width - 40, 34)
        
        countryButton.frame = CGRectMake(10, 200, view.width - 20, 44)
        countryButtonLine.frame = CGRectMake(10, 244, view.width - 20, 0.5)
        
        termsLabel.frame = CGRectMake(20, 314, view.width - 40, 55)
        
        useEmailButton.frame = CGRectMake(20, 375, view.width - 40, 38)
        
        resizePhoneLabels()
    }
    
    private func resizePhoneLabels() {
        phoneCodeLabel.frame = CGRectMake(10, 244, 80, 44)
        phoneCodeLabel.sizeToFit()
        phoneCodeLabel.frame = CGRectMake(10, 244, phoneCodeLabel.width + 32, 44)
        
        phoneNumberLabel.frame = CGRectMake(phoneCodeLabel.width + 10, 245, view.width - phoneCodeLabel.width, 44)
        phoneCodeLabelLine.frame = CGRectMake(10, 288, view.width - 20, 0.5)
    }
    
    func countriesController(countriesController: AACountryViewController, didChangeCurrentIso currentIso: String) {
        currentCountry = AATelephony.getCountry(currentIso)
        countryButton.setTitle(currentCountry.country, forState: .Normal)
        phoneCodeLabel.text = "+\(currentCountry.code)"
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
}