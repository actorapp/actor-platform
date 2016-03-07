//
//  Copyright (c) 2014-2015 Actor LLC. <https://actor.im>
//

import Foundation

class AAEmailAuthCodeViewController: AAAuthViewController {
    
    // Views
    
    private let grayBackground = UIView()
    private let titleLabel = UILabel()
    
    private let codeTextField = UITextField()
    private let hintLabel = UILabel()
    
    private var navigationBarSeparator = UIView()
    private var codeSeparator = UIView()
    
    private let email: String
    
    init(email: String) {
        self.email = email
        super.init()
    }

    required init(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    // Layouting
    
    override func loadView() {
        super.loadView()
        
        view.backgroundColor = UIColor.whiteColor()
        
        grayBackground.backgroundColor = UIColor(rgb: 0xf2f2f2)
        view.addSubview(grayBackground)
        
        titleLabel.backgroundColor = UIColor.clearColor()
        titleLabel.textColor = UIColor.blackColor()
        titleLabel.font = AADevice.isiPad
            ? UIFont.thinSystemFontOfSize(50.0)
            : UIFont.lightSystemFontOfSize(22)
        
        titleLabel.textLocalized = email
        grayBackground.addSubview(titleLabel)
        
        codeTextField.font = UIFont.systemFontOfSize(20.0)
        codeTextField.backgroundColor = UIColor.whiteColor()
        codeTextField.placeholder = AALocalized("AuthEmailCodeFieldHint")
        codeTextField.keyboardType = UIKeyboardType.NumberPad
        codeTextField.contentVerticalAlignment = UIControlContentVerticalAlignment.Center
        codeTextField.textAlignment = .Center
        view.addSubview(codeTextField)
        
        codeSeparator.backgroundColor = UIColor(rgb: 0xc8c7cc)
        view.addSubview(codeSeparator)
        
        navigationBarSeparator = UIView()
        navigationBarSeparator.backgroundColor = UIColor(rgb: 0xc8c7cc)
        view.addSubview(navigationBarSeparator)
        
        hintLabel.font = UIFont.systemFontOfSize(17.0)
        hintLabel.textColor = UIColor(rgb: 0x999999)
        hintLabel.lineBreakMode = NSLineBreakMode.ByWordWrapping
        hintLabel.backgroundColor = UIColor.whiteColor()
        hintLabel.textAlignment = NSTextAlignment.Center
        hintLabel.contentMode = UIViewContentMode.Center
        hintLabel.numberOfLines = 0
        hintLabel.textLocalized = "AuthEmailCodeHint"
        view.addSubview(hintLabel)
        
        // Configure navigation bar
        navigationItem.rightBarButtonItem = UIBarButtonItem(title: AALocalized("NavigationNext"), style: UIBarButtonItemStyle.Done, target: self, action: "nextButtonPressed")
    }
    
    override func viewWillLayoutSubviews() {
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
        
        codeTextField.frame = CGRect(x: (screenSize.width - fieldWidth) / 2 + 32, y: navigationBarSeparator.top + 16, width: fieldWidth - 10.0 - 64, height: 50 - 2)
        
        codeTextField.frame = CGRect(x: (screenSize.width - fieldWidth)/2, y: navigationBarSeparator.frame.origin.y + navigationBarSeparator.bounds.size.height, width: fieldWidth, height: 56.0)
        
        codeSeparator.frame = CGRect(x: (screenSize.width - fieldWidth)/2 + 22, y: codeTextField.frame.origin.y + codeTextField.height, width: fieldWidth - 44, height: 0.5)
        
        let hintPadding : CGFloat = AADevice.isiPad
            ? (isPortraint ? 460.0: 274.0)
            : (isWidescreen ? 214.0 : 184.0)
        
        let hintLabelSize = hintLabel.sizeThatFits(CGSize(width: 278.0, height: CGFloat.max))
        hintLabel.frame = CGRect(x: (screenSize.width - hintLabelSize.width) / 2.0, y: hintPadding, width: hintLabelSize.width, height: hintLabelSize.height);
    }
    
    // Constoller states
    
    override func viewDidAppear(animated: Bool) {
        super.viewDidAppear(animated)
        codeTextField.becomeFirstResponder()
    }
    
    override func viewWillDisappear(animated: Bool) {
        super.viewWillDisappear(animated)
        codeTextField.resignFirstResponder()
    }
    
    // Actions
    
    func nextButtonPressed() {
        if codeTextField.text == nil || codeTextField.text!.trim().length == 0 {
            let fieldWidth : CGFloat = AADevice.isiPad
                ? (520)
                : (view.width)
            self.shakeView(codeTextField, originalX: (view.width - fieldWidth) / 2)
            return
        }
        
        executeSafe(Actor.validateCodeCommand(codeTextField.text!), ignore: ["PHONE_CODE_INVALID"]) { (val) -> Void in
            
            let state = Actor.getAuthState().ordinal()
            
            if state == ACAuthState.LOGGED_IN().ordinal() {
                self.codeTextField.resignFirstResponder()
                // If logged in state: end authentication
                self.onAuthenticated()
                
            } else if state == ACAuthState.SIGN_UP().ordinal() {
                
                // Is signup go to signup controller
                self.navigateNext(AAAuthRegisterViewController(), removeCurrent: true)
            } else if state != ACAuthState.CODE_VALIDATION_PHONE().ordinal() {
                
                // If state not for the current state: start from scratch
                self.navigateBack()
            } else {
                
                // If same state: something code is incorrect
                self.shakeView(self.codeTextField, originalX: self.codeTextField.frame.origin.x)
            }
        }
    }
}