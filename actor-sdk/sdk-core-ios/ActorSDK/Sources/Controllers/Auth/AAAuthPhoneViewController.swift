//
//  Copyright (c) 2014-2015 Actor LLC. <https://actor.im>
//

import UIKit
import TTTAttributedLabel

public class AAAuthPhoneViewController: AAAuthViewController, UITextFieldDelegate, AAAuthCountriesViewControllerDelegate ,
AATapLabelDelegate{
    
    // Views
    
    private var grayBackground: UIView!
    private var titleLabel: UILabel!
    private var countryButton: UIButton!
    private var phoneBackgroundView: UIImageView!
    private var countryCodeLabel: UILabel!
    private var termsofuseLabel : TapLabel!
    private var phoneTextField: ABPhoneField!
    private var hintLabel: UILabel!
    private var navigationBarSeparator: UIView!
    //"AuthTermsOfServiceFull" = "By signing up, you agree to the Terms of Service.";
    
    //"AuthTermsOfService" = "Terms of Service";
    // Constructors
    
    public override init() {
        super.init()
        
        self.content = ACAllEvents_Auth.AUTH_PHONE()
    }
    
    public required init(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    // Layouting
    
    public override func loadView() {
        super.loadView()
        
        view.backgroundColor = UIColor.whiteColor()
        
        grayBackground = UIView()
        grayBackground.backgroundColor = UIColor(rgb: 0xf2f2f2)
        view.addSubview(grayBackground)
        
        titleLabel = UILabel()
        titleLabel.backgroundColor = UIColor.clearColor()
        titleLabel.textColor = UIColor.blackColor()
        titleLabel.font = AADevice.isiPad
            ? UIFont.thinSystemFontOfSize(50.0)
            : UIFont.lightSystemFontOfSize(30)
        
        titleLabel.textLocalized = "AuthPhoneTitle"
        grayBackground.addSubview(titleLabel)
        
        let countryImage = UIImage.bundled("ModernAuthCountryButton")!
        let countryImageHighlighted = UIImage.bundled("ModernAuthCountryButtonHighlighted")!
        
        countryButton = UIButton()
        countryButton.setBackgroundImage(countryImage
            .stretchableImageWithLeftCapWidth(Int(countryImage.size.width / 2), topCapHeight: 0), forState: UIControlState.Normal)
        countryButton.setBackgroundImage(countryImageHighlighted.stretchableImageWithLeftCapWidth(Int(countryImageHighlighted.size.width / 2), topCapHeight: 0), forState: UIControlState.Highlighted)
        countryButton.titleLabel?.font = UIFont.systemFontOfSize(20.0)
        countryButton.titleLabel?.textAlignment = NSTextAlignment.Left
        countryButton.contentHorizontalAlignment = UIControlContentHorizontalAlignment.Left
        countryButton.setTitleColor(UIColor.blackColor(), forState: UIControlState.Normal)
        countryButton.titleEdgeInsets = UIEdgeInsets(top: 0, left: 14, bottom: 9, right: 14)
        countryButton.addTarget(self, action: "showCountriesList", forControlEvents: UIControlEvents.TouchUpInside)
        view.addSubview(countryButton)
        
        let phoneImage = UIImage.bundled("ModernAuthPhoneBackground")!
        phoneBackgroundView = UIImageView(image: phoneImage.stretchableImageWithLeftCapWidth(Int(phoneImage.size.width / 2), topCapHeight: 0))
        view.addSubview(phoneBackgroundView)
        
        countryCodeLabel = UILabel()
        countryCodeLabel.font = UIFont.systemFontOfSize(20.0)
        countryCodeLabel.backgroundColor = UIColor.clearColor()
        countryCodeLabel.textAlignment = NSTextAlignment.Center
        phoneBackgroundView.addSubview(countryCodeLabel)
        
        phoneTextField = ABPhoneField()
        phoneTextField.font = UIFont.systemFontOfSize(20.0)
        phoneTextField.backgroundColor = UIColor.whiteColor()
        phoneTextField.placeholder = AALocalized("AuthPhoneNumberHint")
        phoneTextField.keyboardType = UIKeyboardType.NumberPad;
        phoneTextField.contentVerticalAlignment = UIControlContentVerticalAlignment.Center
        phoneTextField.delegate = self
        view.addSubview(phoneTextField)
        
        navigationBarSeparator = UIView()
        navigationBarSeparator.backgroundColor = UIColor(rgb: 0xc8c7cc)
        view.addSubview(navigationBarSeparator)
        
        hintLabel = UILabel()
        hintLabel.font = UIFont.systemFontOfSize(17.0)
        hintLabel.textColor = UIColor(rgb: 0x999999)
        hintLabel.lineBreakMode = NSLineBreakMode.ByWordWrapping
        hintLabel.backgroundColor = UIColor.whiteColor()
        hintLabel.textAlignment = NSTextAlignment.Center
        hintLabel.contentMode = UIViewContentMode.Center
        hintLabel.numberOfLines = 0
        hintLabel.textLocalized = "AuthPhoneHint"
        view.addSubview(hintLabel)
        
        // terms of use
        termsofuseLabel = TapLabel()

        termsofuseLabel.delegate = self
        termsofuseLabel.numberOfLines = 0
        
        let termsFullString = AALocalized("AuthTermsOfServiceFull")
        let termsString = AALocalized("AuthTermsOfService")
        
        let paragrahStyle = NSMutableParagraphStyle()
        paragrahStyle.alignment = NSTextAlignment.Center
        
        let atrsPolicy = [NSFontAttributeName:UIFont.systemFontOfSize(11),
            NSParagraphStyleAttributeName : paragrahStyle,
            NSForegroundColorAttributeName : UIColor.lightGrayColor() ]
        
        let policyMutText = NSMutableAttributedString(string: termsFullString,
            attributes: atrsPolicy)
        
        termsofuseLabel.attributedText = policyMutText
        
        let termsRangeSwift = termsofuseLabel.text?.rangeOfString(termsString)
        let termsPos = termsofuseLabel.text?.startIndex.distanceTo(termsRangeSwift!.startIndex)

        let termsRange = NSMakeRange(termsPos!, termsString.length)
        
        policyMutText.addAttribute(TapLabel.LinkContentName, value: "openterms", range: termsRange)
        policyMutText.addAttribute(NSForegroundColorAttributeName, value: UIColor.blueColor(), range: termsRange)
        policyMutText.addAttribute(TapLabel.SelectedForegroudColorName, value: UIColor.blueColor(), range: termsRange)
        
        termsofuseLabel.attributedText = policyMutText
        
        self.view.addSubview(termsofuseLabel)
        
        
        // Setting default country
        let defaultIso = phoneTextField.currentIso
        let countryCode = ABPhoneField.callingCodeByCountryCode()[defaultIso] as! String
        let countryTitle = ABPhoneField.countryNameByCountryCode()[defaultIso] as! String
        countryCodeLabel.text = "+\(countryCode)"
        countryButton.setTitle(countryTitle, forState: UIControlState.Normal)
        
        // Configure navigation bar
        navigationItem.rightBarButtonItem = UIBarButtonItem(title: AALocalized("NavigationNext"), style: UIBarButtonItemStyle.Done, target: self, action: "nextButtonPressed")
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
            : (20)
        titleLabel.sizeToFit()
        titleLabel.frame = CGRect(x: (screenSize.width - titleLabel.frame.size.width) / 2.0, y: grayBackground.frame.height - titleLabel.frame.size.height - CGFloat(padding), width: titleLabel.frame.size.width, height: titleLabel.frame.size.height)
        
        navigationBarSeparator.frame = CGRect(x: 0, y: grayBackground.bounds.size.height, width: screenSize.width, height: 0.5)
        
        let fieldWidth : CGFloat = AADevice.isiPad
            ? (520)
            : (screenSize.width)
        
        let countryImage: UIImage! = UIImage.bundled("ModernAuthCountryButton")
        countryButton.frame = CGRect(x: (screenSize.width - fieldWidth) / 2, y: grayBackground.frame.origin.y + grayBackground.bounds.size.height, width: fieldWidth, height: countryImage.size.height)
        
        let phoneImage: UIImage! = UIImage.bundled("ModernAuthPhoneBackground")
        phoneBackgroundView.frame = CGRect(x: (screenSize.width - fieldWidth) / 2, y: countryButton.frame.origin.y + 57, width: fieldWidth, height: phoneImage.size.height)
        
        let countryCodeLabelTopSpacing: CGFloat = 3.0
        countryCodeLabel.frame = CGRect(x: 14, y: countryCodeLabelTopSpacing, width: 68, height: phoneBackgroundView.frame.size.height - countryCodeLabelTopSpacing)
        
        phoneTextField.frame = CGRect(x: (screenSize.width - fieldWidth) / 2 + 96.0, y: phoneBackgroundView.frame.origin.y + 1, width: fieldWidth - 96.0 - 10.0, height: phoneBackgroundView.frame.size.height - 2)
        
        let hintPadding : CGFloat = AADevice.isiPad
            ? (isPortraint ? 460.0: 274.0)
            : (isWidescreen ? 274.0 : 214.0)
        
        let hintLabelSize = hintLabel.sizeThatFits(CGSize(width: 278.0, height: CGFloat.max))
        hintLabel.frame = CGRect(x: (screenSize.width - hintLabelSize.width) / 2.0, y: hintPadding, width: hintLabelSize.width, height: hintLabelSize.height);
        
        let termsLabelSize = termsofuseLabel.sizeThatFits(CGSize(width: 278.0, height: CGFloat.max))
        
        termsofuseLabel.frame = CGRect(x: (screenSize.width - termsLabelSize.width) / 2.0, y: hintLabel.frame.maxY+5, width: termsLabelSize.width, height: termsLabelSize.height);
        
    }
    
    // Constoller states
    
    public override func viewDidAppear(animated: Bool) {
        super.viewDidAppear(animated)
        phoneTextField.becomeFirstResponder()
    }
    
    public override func viewWillDisappear(animated: Bool) {
        super.viewWillDisappear(animated)
        phoneTextField.resignFirstResponder()
    }
    
    // Actions
    
    public func nextButtonPressed() {
        
        let number = phoneTextField.phoneNumber.toJLong()
        let numberLength = phoneTextField.phoneNumber.length
        let numberRequiredLength = Int(ABPhoneField.phoneMinLengthByCountryCode()[self.phoneTextField.currentIso] as! String)!
        
        if (numberLength < numberRequiredLength) {
            
            // Show error about incorrect phone
            AAExecutions.errorWithTag("LOCAL_INCORRECT_PHONE")
        } else {
            
            // Safely executing command for starting activation
            executeSafe(Actor.requestStartAuthCommandWithPhone(number)) { (val) -> Void in
                // Showing code input controller
                let phoneNumber = "+\(self.phoneTextField.formattedPhoneNumber)"
                self.navigateNext(AAAuthCodeViewController(phoneNumber: phoneNumber), removeCurrent: false)
            }
        }
    }
    
    public func showCountriesList() {
        let countriesController = AAAuthCountriesViewController()
        countriesController.delegate = self
        countriesController.currentIso = self.phoneTextField.currentIso
        let navigationController = AANavigationController(rootViewController: countriesController)
        presentViewController(navigationController, animated: true, completion: nil)
    }
    
    // Callback
    
    public func countriesController(countriesController: AAAuthCountriesViewController, didChangeCurrentIso currentIso: String) {
        
        self.phoneTextField.currentIso = currentIso
        
        let countryCode: String = ABPhoneField.callingCodeByCountryCode()[currentIso] as! String
        countryCodeLabel.text = "+\(countryCode)"
        countryButton.setTitle(ABPhoneField.countryNameByCountryCode()[currentIso] as? String, forState: UIControlState.Normal)
    }
    
    // Events
    
    public func textField(textField: UITextField, shouldChangeCharactersInRange range: NSRange, replacementString string: String) -> Bool {
        return true
    }
    
    public func tapLabel(tapLabel: TapLabel, didSelectLink link: String) {
        
        // open terms
        
        self.presentViewController(AATermsController(), animated: true, completion: nil)
        
    }
}