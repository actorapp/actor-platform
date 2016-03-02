//
//  Copyright (c) 2014-2015 Actor LLC. <https://actor.im>
//

import Foundation

class AAEmailAuthViewController: AAAuthViewController {
    
    // Views
    
    private var grayBackground: UIView!
    private var titleLabel: UILabel!
    private var emailTextField: UITextField!
    private var emailTextSeparator = UIView()
    private var hintLabel: UILabel!
    private var navigationBarSeparator: UIView!
    
    // Constructors
    
    override init() {
        super.init()
        
        self.content = ACAllEvents_Auth.AUTH_PHONE()
    }
    
    required init(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    // Layouting
    
    override func loadView() {
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
        
        titleLabel.textLocalized = "AuthEmailTitle"
        grayBackground.addSubview(titleLabel)
        
        emailTextField = UITextField()
        emailTextField.font = UIFont.systemFontOfSize(20.0)
        emailTextField.backgroundColor = UIColor.whiteColor()
        emailTextField.placeholder = AALocalized("AuthEmailFieldHint")
        emailTextField.keyboardType = UIKeyboardType.EmailAddress
        emailTextField.contentVerticalAlignment = UIControlContentVerticalAlignment.Center
        emailTextField.textAlignment = .Center
        emailTextField.autocapitalizationType = .None
        // phoneTextField.delegate = self
        view.addSubview(emailTextField)
        
        emailTextSeparator = UIView()
        emailTextSeparator.backgroundColor = UIColor(rgb: 0xc8c7cc)
        view.addSubview(emailTextSeparator)
        
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
        hintLabel.textLocalized = "AuthEmailHint"
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
        
        emailTextField.frame = CGRect(x: (screenSize.width - fieldWidth) / 2 + 32, y: navigationBarSeparator.top + 16, width: fieldWidth - 10.0 - 64, height: 50 - 2)
        
        emailTextField.frame = CGRect(x: (screenSize.width - fieldWidth)/2, y: navigationBarSeparator.frame.origin.y + navigationBarSeparator.bounds.size.height, width: fieldWidth, height: 56.0)
        
        emailTextSeparator.frame = CGRect(x: (screenSize.width - fieldWidth)/2 + 22, y: emailTextField.frame.origin.y + emailTextField.height, width: fieldWidth - 44, height: 0.5)
        
        let hintPadding : CGFloat = AADevice.isiPad
            ? (isPortraint ? 460.0: 274.0)
            : (isWidescreen ? 214.0 : 184.0)
        
        let hintLabelSize = hintLabel.sizeThatFits(CGSize(width: 278.0, height: CGFloat.max))
        hintLabel.frame = CGRect(x: (screenSize.width - hintLabelSize.width) / 2.0, y: hintPadding, width: hintLabelSize.width, height: hintLabelSize.height);
    }
    
    // Constoller states
    
    override func viewDidAppear(animated: Bool) {
        super.viewDidAppear(animated)
        emailTextField.becomeFirstResponder()
    }
    
    // Actions
    
    func nextButtonPressed() {
        
        if emailTextField.text == nil || !isValidEmail(emailTextField.text!) {
            let fieldWidth : CGFloat = AADevice.isiPad
                ? (520)
                : (view.width)
            self.shakeView(emailTextField, originalX: (view.width - fieldWidth) / 2)
            return
        }
        
        let email = emailTextField.text!
        executeSafe(Actor.requestStartAuthCommandWithEmail(email)) { (val) -> Void in
            self.navigateNext(AAEmailAuthCodeViewController(email: email), removeCurrent: false)
        }
    }
    
    func isValidEmail(testStr:String) -> Bool {
        
        let emailRegEx = "^[A-Z0-9a-z._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}"
        
        let emailTest = NSPredicate(format:"SELF MATCHES %@", emailRegEx)
        return emailTest.evaluateWithObject(testStr)
    }
    
    // Events
    
    func textField(textField: UITextField, shouldChangeCharactersInRange range: NSRange, replacementString string: String) -> Bool {
        return true
    }
}