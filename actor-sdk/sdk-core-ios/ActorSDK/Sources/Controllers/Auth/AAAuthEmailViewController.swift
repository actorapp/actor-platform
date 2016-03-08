//
//  Copyright (c) 2014-2016 Actor LLC. <https://actor.im>
//

import Foundation
import YYText

public class AAAuthEmailViewController: AAAuthViewController {
    
    let name: String
    
    let scrollView = UIScrollView()
    
    let welcomeLabel = UILabel()
    let hintLabel = UILabel()
    
    let emailField = UITextField()
    let emailFieldLine = UIView()
    
    let termsLabel = YYLabel()
    let usePhoneButton = UIButton()
    
    public init(name: String) {
        self.name = name
        super.init(nibName: nil, bundle: nil)
    }

    public required init(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    public override func viewDidLoad() {
        
        view.backgroundColor = UIColor.whiteColor()
        
        scrollView.keyboardDismissMode = .OnDrag
        scrollView.scrollEnabled = true
        scrollView.alwaysBounceVertical = true
        
        welcomeLabel.font = UIFont.lightSystemFontOfSize(23)
        welcomeLabel.textColor = UIColor.alphaBlack(0.87)
        welcomeLabel.text = "What's your email?"
        welcomeLabel.numberOfLines = 1
        welcomeLabel.minimumScaleFactor = 0.3
        welcomeLabel.adjustsFontSizeToFitWidth = true
        welcomeLabel.textAlignment = .Center
        
        hintLabel.font = UIFont.systemFontOfSize(14)
        hintLabel.textColor = UIColor.alphaBlack(0.64)
        hintLabel.text = "We won't send you spam."
        hintLabel.numberOfLines = 1
        hintLabel.textAlignment = .Center
        
        emailField.font = UIFont.systemFontOfSize(17)
        emailField.textColor = UIColor.alphaBlack(0.64)
        emailField.placeholder = "Your email"
        emailField.keyboardType = .EmailAddress
        emailField.autocapitalizationType = .None
        
        emailFieldLine.backgroundColor = UIColor.alphaBlack(0.2)
        
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
        
        usePhoneButton.setTitle("Use phone isntead", forState: .Normal)
        usePhoneButton.titleLabel?.font = UIFont.systemFontOfSize(14)
        usePhoneButton.setTitleColor(UIColor.blueColor().alpha(0.56), forState: .Normal)
        usePhoneButton.addTarget(self, action: "usePhoneDidPressed", forControlEvents: .TouchUpInside)
        
        scrollView.addSubview(welcomeLabel)
        scrollView.addSubview(hintLabel)
        scrollView.addSubview(emailField)
        scrollView.addSubview(emailFieldLine)
        scrollView.addSubview(usePhoneButton)
        scrollView.addSubview(termsLabel)
        view.addSubview(scrollView)
        
        super.viewDidLoad()
    }
    
    public override func viewDidLayoutSubviews() {
        super.viewDidLayoutSubviews()
        
        welcomeLabel.frame = CGRectMake(20, 90 - 66, view.width - 40, 28)
        hintLabel.frame = CGRectMake(20, 127 - 66, view.width - 40, 18)
        
        emailField.frame = CGRectMake(20, 184 - 66, view.width - 40, 44)
        emailFieldLine.frame = CGRectMake(10, 228 - 66, view.width - 20, 0.5)
        
        termsLabel.frame = CGRectMake(20, 314 - 66, view.width - 40, 55)
        
        usePhoneButton.frame = CGRectMake(20, 375 - 66, view.width - 40, 38)
        
        scrollView.frame = view.bounds
        scrollView.contentSize = CGSizeMake(view.width, 420)
    }
    
    public func usePhoneDidPressed() {
        let controllers = self.navigationController!.viewControllers
        let updatedControllers = Array(controllers[0..<(controllers.count - 1)]) + [AAAuthPhoneViewController(name: name)]
        self.navigationController?.setViewControllers(updatedControllers, animated: false)
    }
    
    public override func nextDidTap() {
        let email = emailField.text!
        
        if !AATools.isValidEmail(email) {
            shakeView(emailField, originalX: view.width / 2)
            shakeView(emailFieldLine, originalX: view.width / 2)
            return
        }
        
        Actor.doStartAuthWithEmail(email).doneLoader().then { (res: ACAuthStartRes!) -> () in
            if res.authMode.toNSEnum() == .OTP {
                self.navigateNext(AAEmailAuthCodeViewController(email: email))
            } else {
                self.alertUser("This account can't be authenticated in this version. Please, update app.")
            }
        }
    }
    
    public override func keyboardWillAppear(height: CGFloat) {
        scrollView.frame = CGRectMake(0, 0, view.width, view.height - height)
        
        let height = scrollView.height - height - 66
        let offset: CGFloat = 184 + 44
        let destOffset = height * 0.8  - offset / 2 + 66
        
        scrollView.setContentOffset(CGPoint(x: 0, y: -destOffset - 66), animated: true)
    }
    
    public override func keyboardWillDisappear() {
        scrollView.frame = CGRectMake(0, 0, view.width, view.height)
    }
    
    public override func viewWillDisappear(animated: Bool) {
        super.viewWillDisappear(animated)
        
        emailField.resignFirstResponder()
    }
}