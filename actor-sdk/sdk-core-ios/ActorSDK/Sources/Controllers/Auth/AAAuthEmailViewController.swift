//
//  Copyright (c) 2014-2016 Actor LLC. <https://actor.im>
//

import Foundation
import YYText

public class AAAuthEmailViewController: AAAuthViewController {
    
    let name: String
    
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
        super.viewDidLoad()
        
        view.backgroundColor = UIColor.whiteColor()
        
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
        
        view.addSubview(welcomeLabel)
        view.addSubview(hintLabel)
        view.addSubview(emailField)
        view.addSubview(emailFieldLine)
        view.addSubview(usePhoneButton)
        view.addSubview(termsLabel)
    }
    
    public override func viewDidLayoutSubviews() {
        super.viewDidLayoutSubviews()
        
        welcomeLabel.frame = CGRectMake(20, 90, view.width - 40, 28)
        hintLabel.frame = CGRectMake(20, 127, view.width - 40, 18)
        
        emailField.frame = CGRectMake(20, 184, view.width - 40, 44)
        emailFieldLine.frame = CGRectMake(10, 228, view.width - 20, 0.5)
        
        termsLabel.frame = CGRectMake(20, 314, view.width - 40, 55)
        
        usePhoneButton.frame = CGRectMake(20, 375, view.width - 40, 38)
    }
    
    public func usePhoneDidPressed() {
        let controllers = self.navigationController!.viewControllers
        let updatedControllers = Array(controllers[0..<(controllers.count - 1)]) + [AAAuthPhoneViewController(name: name)]
        self.navigationController?.setViewControllers(updatedControllers, animated: false)
    }
}