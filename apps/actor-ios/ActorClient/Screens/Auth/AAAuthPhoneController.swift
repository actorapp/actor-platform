//
//  AAAuthPhoneController.swift
//  ActorClient
//
//  Created by Danil Gontovnik on 3/31/15.
//  Copyright (c) 2015 Actor LLC. All rights reserved.
//

import UIKit

class AAAuthPhoneController: AAViewController {
    
    // MARK: - 
    // MARK: Private vars
    
    private var grayBackground: UIView!
    private var titleLabel: UILabel!
    
    private var countryButton: UIButton!
    
    private var phoneBackgroundView: UIImageView!
    private var countryCodeLabel: UILabel!
    private var phoneTextField: ABPhoneField!
    private var hintLabel: UILabel!
    
    // MARK: - 
    // MARK: Public vars
    
    var currentIso: String = "" {
        didSet {
            phoneTextField.currentIso = currentIso
            
            let countryCode: String = ABPhoneField.callingCodeByCountryCode()[currentIso] as! String
            countryCodeLabel.text = "+\(countryCode)"
            countryButton.setTitle(ABPhoneField.countryNameByCountryCode()[currentIso] as? String, forState: UIControlState.Normal)
        }
    }
    
    // MARK: -
    // MARK: Constructors
    
    override init() {
        super.init()
    }
    
    required init(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    // MARK: -
    
    override func loadView() {
        super.loadView()
        
        view.backgroundColor = UIColor.whiteColor()
        
        let screenSize = UIScreen.mainScreen().bounds.size
        let isWidescreen = screenSize.width > 320 || screenSize.height > 480
        
        grayBackground = UIView(frame: CGRect(x: 0.0, y: 0.0, width: screenSize.width, height: isWidescreen ? 131.0 : 90.0))
        grayBackground.backgroundColor = UIColor.RGB(0xf2f2f2)
        view.addSubview(grayBackground)
        
        titleLabel = UILabel()
        titleLabel.backgroundColor = UIColor.clearColor()
        titleLabel.textColor = UIColor.blackColor()
        titleLabel.font = UIFont(name: "HelveticaNeue-Light", size: 30.0)
        titleLabel.text = "Your Phone" // TODO: Localize
        titleLabel.sizeToFit()
        titleLabel.frame = CGRect(x: (screenSize.width - titleLabel.frame.size.width) / 2.0, y: isWidescreen ? 71.0 : 48.0, width: titleLabel.frame.size.width, height: titleLabel.frame.size.height)
        grayBackground.addSubview(titleLabel)
        
        let countryImage: UIImage! = UIImage(named: "ModernAuthCountryButton")
        let countryImageHighlighted: UIImage! = UIImage(named: "ModernAuthCountryButtonHighlighted")
        
        countryButton = UIButton(frame: CGRect(x: 0.0, y: grayBackground.frame.origin.y + grayBackground.bounds.size.height, width: screenSize.width, height: countryImage.size.height))
        countryButton.setBackgroundImage(countryImage.stretchableImageWithLeftCapWidth(Int(countryImage.size.width / 2), topCapHeight: 0), forState: UIControlState.Normal)
        countryButton.setBackgroundImage(countryImageHighlighted.stretchableImageWithLeftCapWidth(Int(countryImageHighlighted.size.width / 2), topCapHeight: 0), forState: UIControlState.Highlighted)
        countryButton.titleLabel?.font = UIFont.systemFontOfSize(20.0)
        countryButton.titleLabel?.textAlignment = NSTextAlignment.Left
        countryButton.contentHorizontalAlignment = UIControlContentHorizontalAlignment.Left
        countryButton.setTitleColor(UIColor.blackColor(), forState: UIControlState.Normal)
        countryButton.titleEdgeInsets = UIEdgeInsets(top: 0, left: 14, bottom: 9, right: 14)
        countryButton.addTarget(self, action: Selector("showCountriesList"), forControlEvents: UIControlEvents.TouchUpInside)
        view.addSubview(countryButton)
        
        let phoneImage: UIImage! = UIImage(named: "ModernAuthPhoneBackground")
        phoneBackgroundView = UIImageView(image: phoneImage.stretchableImageWithLeftCapWidth(Int(phoneImage.size.width / 2), topCapHeight: 0))
        phoneBackgroundView.frame = CGRect(x: 0, y: countryButton.frame.origin.y + 57, width: screenSize.width, height: phoneImage.size.height)
        view.addSubview(phoneBackgroundView)
        
        let countryCodeLabelTopSpacing: CGFloat = 3.0
        countryCodeLabel = UILabel(frame: CGRect(x: 14, y: countryCodeLabelTopSpacing, width: 68, height: phoneBackgroundView.frame.size.height - countryCodeLabelTopSpacing))
        countryCodeLabel.font = UIFont.systemFontOfSize(20.0)
        countryCodeLabel.backgroundColor = UIColor.clearColor()
        countryCodeLabel.textAlignment = NSTextAlignment.Center
        phoneBackgroundView.addSubview(countryCodeLabel)
        
        phoneTextField = ABPhoneField(frame: CGRect(x: 96.0, y: phoneBackgroundView.frame.origin.y + 1, width: screenSize.width - 96.0 - 10.0, height: phoneBackgroundView.frame.size.height - 2))
        phoneTextField.font = UIFont.systemFontOfSize(20.0)
        phoneTextField.backgroundColor = UIColor.whiteColor()
        phoneTextField.placeholder = "Your phone number" // TODO: Localize
        phoneTextField.keyboardType = UIKeyboardType.NumberPad;
        phoneTextField.contentVerticalAlignment = UIControlContentVerticalAlignment.Center
        view.addSubview(phoneTextField)
        
        let separatorHeight: CGFloat = Utils.isRetina() ? 0.5 : 1.0;
        var navigationBarSeparator = UIView(frame: CGRect(x: 0.0, y: grayBackground.bounds.size.height, width: screenSize.width, height: separatorHeight))
        navigationBarSeparator.backgroundColor = UIColor.RGB(0xc8c7cc)
        view.addSubview(navigationBarSeparator)
        
        hintLabel = UILabel()
        hintLabel.font = UIFont.systemFontOfSize(17.0)
        hintLabel.textColor = UIColor.RGB(0x999999)
        hintLabel.lineBreakMode = NSLineBreakMode.ByWordWrapping
        hintLabel.backgroundColor = UIColor.whiteColor()
        hintLabel.textAlignment = NSTextAlignment.Center
        hintLabel.contentMode = UIViewContentMode.Center
        hintLabel.numberOfLines = 0
        hintLabel.text = "Please confirm your country code and enter your phone number." // TODO: Localize
        let hintLabelSize = hintLabel.sizeThatFits(CGSize(width: 278.0, height: CGFloat.max))
        hintLabel.frame = CGRect(x: (screenSize.width - hintLabelSize.width) / 2.0, y: CGFloat(isWidescreen ? 274.0 : 214.0), width: hintLabelSize.width, height: hintLabelSize.height);
        view.addSubview(hintLabel)
        
        var nextBarButton = UIBarButtonItem(title: "Next", style: UIBarButtonItemStyle.Done, target: self, action: Selector("nextButtonPressed")) // TODO: Localize
        navigationItem.rightBarButtonItem = nextBarButton
        
        currentIso = phoneTextField.currentIso
    }
    
    override func viewDidAppear(animated: Bool) {
        super.viewDidAppear(animated)
        phoneTextField.becomeFirstResponder()
    }
    
    // MARK: -
    // MARK: Methods
    
    func nextButtonPressed() {
        let numberLength = count(phoneTextField.phoneNumber) as Int
        let numberRequiredLength: Int = (ABPhoneField.phoneMinLengthByCountryCode()[currentIso] as! String).toInt()!
        if (numberLength != numberRequiredLength) {
            SVProgressHUD.showErrorWithStatus("Wrong phone length")
        } else {
            SVProgressHUD.showWithMaskType(SVProgressHUDMaskType.Black)
            
            let messenger = CocoaMessenger.messenger().requestSmsWithLong(jlong((phoneTextField.phoneNumber as NSString).longLongValue))
            messenger.startWithAMCommandCallback(CocoaCallback(result: { (val: Any?) -> () in
                self.navigateToSms()
                SVProgressHUD.dismiss()
                }, error: { (exception) -> () in
                    SVProgressHUD.showErrorWithStatus(exception.getLocalizedMessage())
            }))

        }
    }
    
    // MARK: -
    // MARK: Navigation
    
    func showCountriesList() {
        var countriesController = AAAuthCountriesController()
        countriesController.delegate = self
        countriesController.currentIso = currentIso
        var navigationController = AANavigationController(rootViewController: countriesController)
        presentViewController(navigationController, animated: true, completion: nil)
    }
    
    func navigateToSms() {
        var smsController = AAAuthSmsController()
        smsController.phoneNumber = "+\(phoneTextField.formattedPhoneNumber)"
        navigationController!.pushViewController(smsController, animated: true)
    }
    
}

// MARK: -
// MARK: AAAuthCountriesController Delegate

extension AAAuthPhoneController: AAAuthCountriesControllerDelegate {
    
    func countriesController(countriesController: AAAuthCountriesController, didChangeCurrentIso currentIso: String) {
        self.currentIso = currentIso
    }
    
}