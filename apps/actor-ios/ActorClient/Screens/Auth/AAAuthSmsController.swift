//
//  AAAuthSmsController.swift
//  ActorClient
//
//  Created by Danil Gontovnik on 3/31/15.
//  Copyright (c) 2015 Actor LLC. All rights reserved.
//

import UIKit

class AAAuthSmsController: AAViewController {

    // MARK: -
    // MARK: Private vars
    
    private var grayBackground: UIView!
    private var titleLabel: UILabel!
    
    private var codeTextField: UITextField!
    private var hintLabel: UILabel!
    
    // MARK: -
    // MARK: Public vars
    
    var currentIso: String = ""
    var phoneNumber: String = "" {
        didSet {
            titleLabel.text = phoneNumber
        }
    }
    
    // MARK: -
    // MARK: Constructors
    
    override init() {
        super.init()
        
        view.backgroundColor = UIColor.whiteColor()
        
        let screenSize = UIScreen.mainScreen().bounds.size
        let isWidescreen = screenSize.width > 320 || screenSize.height > 480
        
        grayBackground = UIView(frame: CGRect(x: 0.0, y: 0.0, width: screenSize.width, height: isWidescreen ? 131.0 : 90.0))
        grayBackground.backgroundColor = UIColor.RGB(0xf2f2f2)
        view.addSubview(grayBackground)
        
        titleLabel = UILabel()
        titleLabel.backgroundColor = UIColor.clearColor()
        titleLabel.textColor = UIColor.blackColor()
        titleLabel.textAlignment = NSTextAlignment.Center
        titleLabel.font = UIFont(name: "HelveticaNeue-Light", size: 21.0)
        titleLabel.text = "+0 123 456 789 1011 1213"
        titleLabel.sizeToFit()
        titleLabel.frame = CGRect(x: (screenSize.width - titleLabel.frame.size.width) / 2.0, y: isWidescreen ? 71.0 : 48.0, width: titleLabel.frame.size.width, height: titleLabel.frame.size.height)
        grayBackground.addSubview(titleLabel)
        
        let separatorHeight: CGFloat = Utils.isRetina() ? 0.5 : 1.0
        var navigationBarSeparator = UIView(frame: CGRect(x: 0.0, y: grayBackground.bounds.size.height, width: screenSize.width, height: separatorHeight))
        navigationBarSeparator.backgroundColor = UIColor.RGB(0xc8c7cc)
        view.addSubview(navigationBarSeparator)
        
        codeTextField = UITextField()
        codeTextField.placeholder = "Code"
        codeTextField.font = UIFont.systemFontOfSize(24.0)
        codeTextField.backgroundColor = UIColor.whiteColor()
        codeTextField.contentVerticalAlignment = UIControlContentVerticalAlignment.Center
        codeTextField.textAlignment = NSTextAlignment.Center
        codeTextField.keyboardType = UIKeyboardType.NumberPad
        codeTextField.delegate = self
        codeTextField.frame = CGRect(x: 0.0, y: navigationBarSeparator.frame.origin.y + navigationBarSeparator.bounds.size.height, width: screenSize.width, height: 56.0)
        view.addSubview(codeTextField)
        
        var codeSeparator = UIView(frame: CGRect(x: 22, y: codeTextField.frame.origin.y + codeTextField.bounds.size.height, width: screenSize.width - 44, height: separatorHeight))
        codeSeparator.backgroundColor = UIColor.RGB(0xc8c7cc)
        view.addSubview(codeSeparator)
        
        hintLabel = UILabel()
        hintLabel.font = UIFont.systemFontOfSize(16.0)
        hintLabel.backgroundColor = UIColor.whiteColor()
        hintLabel.textColor = UIColor.blackColor()
        hintLabel.textAlignment = NSTextAlignment.Center
        hintLabel.contentMode = UIViewContentMode.Center
        hintLabel.numberOfLines = 0
        hintLabel.text = "We have sent you an SMS with the code"
        view.addSubview(hintLabel)
        
        let hintLabelSize = hintLabel.sizeThatFits(CGSize(width: 300, height: screenSize.height))
        hintLabel.frame = CGRectIntegral(CGRect(x: (screenSize.width - hintLabelSize.width) / 2, y: navigationBarSeparator.frame.origin.y + (isWidescreen ? 85.0 : 70.0), width: hintLabelSize.width, height: hintLabelSize.height))
        
        var nextBarButton = UIBarButtonItem(title: "Next", style: UIBarButtonItemStyle.Done, target: self, action: Selector("nextButtonPressed")) // TODO: Localize
        navigationItem.rightBarButtonItem = nextBarButton
    }

    required init(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    // MARK: -
    
    override func viewDidAppear(animated: Bool) {
        super.viewDidAppear(animated)
        codeTextField.becomeFirstResponder()
    }
    
    // MARK: -
    // MARK: Methods
    
    func nextButtonPressed() {
        SVProgressHUD.showWithMaskType(SVProgressHUDMaskType.Black)
        
        let messenger = CocoaMessenger.messenger().sendCodeWithInt(jint(codeTextField.text.toInt()!))
        messenger.startWithAMCommandCallback(CocoaCallback(result: { (val: Any?) -> () in
            if let state = val as? AMAuthStateEnum {
                let loggedInState: jint = jint(AMAuthState.LOGGED_IN.rawValue)
                if state.ordinal() == loggedInState {
                    SVProgressHUD.showSuccessWithStatus("Logged in")
                    self.navigationController!.presentingViewController!.dismissViewControllerAnimated(true, completion: nil)
                } else {
                    SVProgressHUD.showSuccessWithStatus(state.description())
                    self.navigateToRegistration()
                }
            }
            SVProgressHUD.dismiss()
            }, error: { (exception) -> () in
                SVProgressHUD.showErrorWithStatus(exception.getLocalizedMessage())
        }))
    }
    
    // MARK: -
    // MARK: Navigate
    
    private func navigateToRegistration() {
        let registerController = AAAuthRegisterController()
        navigationController!.pushViewController(registerController, animated: true)
    }
}

// MARK: -
// MARK: UITextField Delegate

extension AAAuthSmsController: UITextFieldDelegate {

    func textField(textField: UITextField, shouldChangeCharactersInRange range: NSRange, replacementString string: String) -> Bool {

        let newString = (textField.text as NSString).stringByReplacingCharactersInRange(range, withString: string)
        if count(newString) == 6 {
            // TODO: Auto check code correct?
        }
        
        return true
    }
    
}
