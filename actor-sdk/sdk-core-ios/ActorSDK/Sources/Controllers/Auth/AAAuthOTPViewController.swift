//
//  Copyright (c) 2014-2016 Actor LLC. <https://actor.im>
//

import Foundation
import YYText

public class AAAuthOTPViewController: AAAuthViewController {

    let scrollView = UIScrollView()
    let welcomeLabel = UILabel()
    let validateLabel = UILabel()
    let hintLabel = UILabel()
    
    let codeField = UITextField()
    let codeFieldLine = UIView()
    
    let haventReceivedCode = UIButton()
    
    let transactionHash: String
    let name: String!
    let email: String!
    let phone: String!
    
    public init(email: String, transactionHash: String) {
        self.transactionHash = transactionHash
        self.name = nil
        self.email = email
        self.phone = nil
        super.init()
    }
    
    public init(email: String, name: String, transactionHash: String) {
        self.transactionHash = transactionHash
        self.name = name
        self.email = email
        self.phone = nil
        super.init()
    }
    
    public init(phone: String, transactionHash: String) {
        self.transactionHash = transactionHash
        self.name = nil
        self.email = nil
        self.phone = phone
        super.init()
    }
    
    public init(phone: String, name: String, transactionHash: String) {
        self.transactionHash = transactionHash
        self.name = name
        self.email = nil
        self.phone = phone
        super.init()
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
        if email != nil {
            welcomeLabel.text = "Confirm your email"
        } else {
            welcomeLabel.text = "Confirm your phone"
        }
        welcomeLabel.textColor = UIColor.blackColor().alpha(0.87)
        welcomeLabel.textAlignment = .Center
        
        validateLabel.font = UIFont.systemFontOfSize(14)
        validateLabel.text = email
        validateLabel.textColor = UIColor(red: 94, green: 142, blue: 192)
        validateLabel.textAlignment = .Center
        
        hintLabel.font = UIFont.systemFontOfSize(14)
        hintLabel.text = "We’ve sent you an confirmation code.\nPlease enter it below."
        hintLabel.textColor = UIColor.alphaBlack(0.56)
        hintLabel.textAlignment = .Center
        
        codeField.font = UIFont.systemFontOfSize(17)
        codeField.textColor = UIColor.alphaBlack(0.64)
        codeField.placeholder = "Confirmation code"
        codeField.keyboardType = .EmailAddress
        codeField.autocapitalizationType = .None
        
        codeFieldLine.backgroundColor = UIColor.alphaBlack(0.2)
        
        if email != nil {
            haventReceivedCode.setTitle("Haven't received the code?", forState: .Normal)
        } else {
            haventReceivedCode.setTitle("Wrong number?", forState: .Normal)
        }
        haventReceivedCode.titleLabel?.font = UIFont.systemFontOfSize(14)
        haventReceivedCode.setTitleColor(UIColor.blueColor().alpha(0.56), forState: .Normal)
        haventReceivedCode.addTarget(self, action: "haventReceivedCodeDidPressed", forControlEvents: .TouchUpInside)
        
        scrollView.addSubview(welcomeLabel)
        scrollView.addSubview(validateLabel)
        scrollView.addSubview(hintLabel)
        scrollView.addSubview(codeField)
        scrollView.addSubview(codeFieldLine)
        scrollView.addSubview(haventReceivedCode)
        
        view.addSubview(scrollView)
        super.viewDidLoad()
    }
    
    public override func viewDidLayoutSubviews() {
        super.viewDidLayoutSubviews()
        
        welcomeLabel.frame = CGRectMake(15, 90 - 66, view.width - 30, 28)
        validateLabel.frame = CGRectMake(10, 127 - 66, view.width - 20, 17)
        hintLabel.frame = CGRectMake(10, 154 - 66, view.width - 20, 17)
        
        codeField.frame = CGRectMake(20, 228 - 66, view.width - 40, 44)
        codeFieldLine.frame = CGRectMake(10, 228 + 44 - 66, view.width - 20, 0.5)
        
        haventReceivedCode.frame = CGRectMake(20, 297 - 66, view.width - 40, 38)
        
        scrollView.frame = view.bounds
        scrollView.contentSize = CGSizeMake(view.width, 240 - 66)
    }
    
    func haventReceivedCodeDidPressed() {
        
    }
    
    public override func nextDidTap() {
        let code = codeField.text!.trim()
        
        if code.length == 0 {
            shakeField()
            return
        }
        
        let promise = Actor.doValidateCode(code, withTransaction: self.transactionHash)
            .startUserAction(["EMAIL_CODE_INVALID", "PHONE_CODE_INVALID", "EMAIL_CODE_EXPIRED", "PHONE_CODE_EXPIRED"])
        
        promise.then { (r: ACAuthCodeRes!) -> () in
            if r.needToSignup {
                if self.name == nil {
                    self.navigateNext(AAAuthNameViewController(transactionHash: r.transactionHash))
                } else {
                    let promise = Actor.doSignupWithName(self.name, withSex: ACSex.UNKNOWN(), withTransaction: r.transactionHash)
                    promise.then { (r: ACAuthRes!) -> () in
                        Actor.doCompleteAuth(r).startUserAction().then { (r: JavaLangBoolean!) -> () in
                            self.onAuthenticated()
                        }
                    }
                    promise.startUserAction()
                }
            } else {
                Actor.doCompleteAuth(r.result).startUserAction().then { (r: JavaLangBoolean!) -> () in
                    self.onAuthenticated()
                }
            }
        }
        
        promise.failure { (e: JavaLangException!) -> () in
            if let rpc = e as? ACRpcException {
                if rpc.tag == "EMAIL_CODE_INVALID" || rpc.tag == "PHONE_CODE_INVALID" {
                    self.shakeField()
                } else if rpc.tag == "EMAIL_CODE_EXPIRED" || rpc.tag == "PHONE_CODE_EXPIRED" {
                    AAExecutions.errorWithTag(rpc.tag, rep: nil, cancel: { () -> () in
                        self.navigateBack()
                    })
                }
            }
        }
    }
    
    private func shakeField() {
        shakeView(codeField, originalX: 20)
        shakeView(codeFieldLine, originalX: 10)
    }
}


