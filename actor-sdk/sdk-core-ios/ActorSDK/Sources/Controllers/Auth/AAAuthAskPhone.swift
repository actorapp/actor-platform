//
//  Copyright (c) 2014-2016 Actor LLC. <https://actor.im>
//

import Foundation
import YYKit

class AAAuthAskPhone: AAAuthViewController, AAAuthCountriesViewControllerDelegate {
    
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
        
        let attributedTerms = NSMutableAttributedString(string: "By singing up, you agree with\nTerms of Service and Privacy Policy.")
        let tosLink = YYTextHighlight()
        tosLink.tapAction = { (container, text, range, rect) in
            
        }
        
        termsLabel.text = "By singing up, you agree with\nTerms of Service and Privacy Policy."
        termsLabel.font = UIFont.systemFontOfSize(14)
        termsLabel.textColor = UIColor.alphaBlack(0.56)
        termsLabel.numberOfLines = 2
        termsLabel.textAlignment = .Center
        
        view.addSubview(welcomeLabel)
        view.addSubview(hintLabel)
        view.addSubview(countryButton)
        view.addSubview(countryButtonLine)
        view.addSubview(phoneCodeLabel)
        view.addSubview(phoneNumberLabel)
        view.addSubview(phoneCodeLabelLine)
        view.addSubview(termsLabel)
    }
    
    override func viewDidLayoutSubviews() {
        super.viewDidLayoutSubviews()
        
        welcomeLabel.frame = CGRectMake(20, 90, view.width - 40, 28)
        hintLabel.frame = CGRectMake(20, 127, view.width - 40, 34)
        
        countryButton.frame = CGRectMake(10, 200, view.width - 20, 44)
        countryButtonLine.frame = CGRectMake(10, 244, view.width - 20, 0.5)
        
        termsLabel.frame = CGRectMake(20, 314, view.width - 40, 55)
        
        resizePhoneLabels()
    }
    
    private func resizePhoneLabels() {
        phoneCodeLabel.frame = CGRectMake(10, 244, 80, 44)
        phoneCodeLabel.sizeToFit()
        phoneCodeLabel.frame = CGRectMake(10, 244, phoneCodeLabel.width + 32, 44)
        
        phoneNumberLabel.frame = CGRectMake(phoneCodeLabel.width + 10, 245, view.width - phoneCodeLabel.width, 44)
        phoneCodeLabelLine.frame = CGRectMake(10, 288, view.width - 20, 0.5)
    }
    
    func countriesController(countriesController: AAAuthCountriesViewController, didChangeCurrentIso currentIso: String) {
        currentCountry = AATelephony.getCountry(currentIso)
        countryButton.setTitle(currentCountry.country, forState: .Normal)
        phoneCodeLabel.text = "+\(currentCountry.code)"
        resizePhoneLabels()
    }
    
    func countryDidPressed() {
        let countriesController = AAAuthCountriesViewController()
        countriesController.delegate = self
        presentElegantViewController(AANavigationController(rootViewController: countriesController))
    }
}