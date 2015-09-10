//
//  Copyright (c) 2014-2015 Actor LLC. <https://actor.im>
//

import UIKit

class AuthSmsViewController: AuthViewController, UIAlertViewDelegate {

    // MARK: -
    // MARK: Private vars
    
    private var grayBackground: UIView!
    private var titleLabel: UILabel!
    
    private var codeTextField: UITextField!
    private var hintLabel: UILabel!
    
    private var navigationBarSeparator: UIView!
    private var codeSeparator: UIView!
    
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
        
        grayBackground = UIView()
        grayBackground.backgroundColor = UIColor.RGB(0xf2f2f2)
        view.addSubview(grayBackground)
        
        titleLabel = UILabel()
        titleLabel.backgroundColor = UIColor.clearColor()
        titleLabel.textColor = UIColor.blackColor()
        titleLabel.textAlignment = NSTextAlignment.Center
        titleLabel.font =  isIPad
            ? UIFont(name: "HelveticaNeue-Thin", size: 50.0)
            : UIFont(name: "HelveticaNeue", size: 22.0)
        titleLabel.text = "+0 123 456 789 1011 1213"
        grayBackground.addSubview(titleLabel)
        
        navigationBarSeparator = UIView()
        navigationBarSeparator.backgroundColor = UIColor.RGB(0xc8c7cc)
        view.addSubview(navigationBarSeparator)
        
        codeTextField = UITextField()
        codeTextField.placeholder = NSLocalizedString("AuthCodeFieldHint", comment: "Hint")
        codeTextField.font = UIFont.systemFontOfSize(24.0)
        codeTextField.backgroundColor = UIColor.whiteColor()
        codeTextField.contentVerticalAlignment = UIControlContentVerticalAlignment.Center
        codeTextField.textAlignment = NSTextAlignment.Center
        codeTextField.keyboardType = UIKeyboardType.NumberPad
        codeTextField.delegate = self
        view.addSubview(codeTextField)
        
        codeSeparator = UIView()
        codeSeparator.backgroundColor = UIColor.RGB(0xc8c7cc)
        view.addSubview(codeSeparator)
        
        hintLabel = UILabel()
        hintLabel.font = UIFont.systemFontOfSize(16.0)
        hintLabel.backgroundColor = UIColor.whiteColor()
        hintLabel.textColor = UIColor.blackColor()
        hintLabel.textAlignment = NSTextAlignment.Center
        hintLabel.contentMode = UIViewContentMode.Center
        hintLabel.numberOfLines = 0
        hintLabel.text = NSLocalizedString("AuthCodeHint", comment: "Hint")
        view.addSubview(hintLabel)
        
        let nextBarButton = UIBarButtonItem(title:NSLocalizedString("NavigationNext", comment: "Next"), style: UIBarButtonItemStyle.Done, target: self, action: Selector("nextButtonPressed"))
        navigationItem.rightBarButtonItem = nextBarButton
    }

    required init(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    // MARK: -
    
    override func viewDidAppear(animated: Bool) {
        super.viewDidAppear(animated)
        
        codeTextField.becomeFirstResponder()
        Actor.trackAuthCodeOpen()
    }
    
    override func viewWillAppear(animated: Bool) {
        super.viewWillAppear(animated)
        
        MainAppTheme.navigation.applyAuthStatusBar()
        Actor.trackAuthCodeClosed()
    }
    
    override func viewWillLayoutSubviews() {
        super.viewWillLayoutSubviews()
        
        let screenSize = UIScreen.mainScreen().bounds.size
        let isWidescreen = screenSize.width > 320 || screenSize.height > 480
        let isPortraint = screenSize.width < screenSize.height
        
        let bgSize = isIPad
            ? (isPortraint ? 304.0: 140)
            : (isWidescreen ? 131.0 : 90.0)
        grayBackground.frame = CGRect(x: 0.0, y: 0.0, width: screenSize.width, height: CGFloat(bgSize))
        
        
        let padding = isIPad
            ? (isPortraint ? 48 : 20)
            : (24)
        titleLabel.sizeToFit()
        titleLabel.frame = CGRect(x: (screenSize.width - titleLabel.frame.size.width) / 2.0, y: grayBackground.frame.height - titleLabel.frame.size.height - CGFloat(padding), width: titleLabel.frame.size.width, height: titleLabel.frame.size.height)
        
        let separatorHeight: CGFloat = Utils.isRetina() ? 0.5 : 1.0
        navigationBarSeparator.frame = CGRect(x: 0.0, y: grayBackground.bounds.size.height, width: screenSize.width, height: separatorHeight)
        
        let fieldWidth : CGFloat = isIPad
            ? (520)
            : (screenSize.width)
        
        codeTextField.frame = CGRect(x: (screenSize.width - fieldWidth)/2, y: navigationBarSeparator.frame.origin.y + navigationBarSeparator.bounds.size.height, width: fieldWidth, height: 56.0)
        
        codeSeparator.frame = CGRect(x: (screenSize.width - fieldWidth)/2 + 22, y: codeTextField.frame.origin.y + codeTextField.bounds.size.height, width: fieldWidth - 44, height: separatorHeight)
        
        let hintLabelSize = hintLabel.sizeThatFits(CGSize(width: 300, height: screenSize.height))
        hintLabel.frame = CGRectIntegral(CGRect(x: (screenSize.width - hintLabelSize.width) / 2, y: navigationBarSeparator.frame.origin.y + (isWidescreen ? 85.0 : 70.0), width: hintLabelSize.width, height: hintLabelSize.height))
    }
    
    // MARK: -
    // MARK: Methods
    
    func nextButtonPressed() {
        if codeTextField.text?.length > 0 {
            let action = "Send Code"
            execute(Actor.validateCodeCommandWithCode(codeTextField.text), successBlock: { (val) -> () in
                if let state = val as? ACAuthStateEnum {
                    Actor.trackActionSuccess(action)
                    let loggedInState: jint = jint(ACAuthState.LOGGED_IN.rawValue)
                    if state.ordinal() == loggedInState {
                        self.onAuthenticated()
                    } else {
                        self.navigateNext(AuthRegisterViewController(), removeCurrent: true)
                    }
                }
                }, failureBlock: { (val) -> () in
                    var message = "Unknwon Error"
                    var tag = "UNKNOWN"
                    if let exception = val as? ACRpcException {
                        tag = exception.getTag()
                        if (tag == "PHONE_CODE_EMPTY" || tag == "PHONE_CODE_INVALID") {
                            self.shakeView(self.codeTextField, originalX: self.codeTextField.frame.origin.x)
                            return
                        } else if (tag == "PHONE_CODE_EXPIRED") {
                            message = NSLocalizedString("ErrorCodeExpired", comment: "PHONE_CODE_EXPIRED message")
                        } else {
                            message = exception.getLocalizedMessage()
                        }
                    } else if let exception = val as? JavaLangException {
                        message = exception.getLocalizedMessage()
                    }
                    Actor.trackActionError(action, withTag: tag, withMessage: message)
                    var alertView = UIAlertView(title: nil, message: message, delegate: self, cancelButtonTitle: NSLocalizedString("AlertOk", comment: "Ok"))
                    alertView.show()
            })
        } else {
            shakeView(codeTextField, originalX: codeTextField.frame.origin.x)
        }
    }
    
    func alertView(alertView: UIAlertView, willDismissWithButtonIndex buttonIndex: Int) {
        if (Actor.getAuthState() != ACAuthState.CODE_VALIDATION_PHONE.rawValue) {
            navigateBack()
        }
    }
}

// MARK: -
// MARK: UITextField Delegate

extension AuthSmsViewController: UITextFieldDelegate {

    func textField(textField: UITextField, shouldChangeCharactersInRange range: NSRange, replacementString string: String) -> Bool {
        let newString = (textField.text! as NSString).stringByReplacingCharactersInRange(range, withString: string)
        if newString.length == 6 {
            // TODO: Auto check code correct?
        }
        
        return true
    }
    
}
