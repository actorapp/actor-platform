//
//  Copyright (c) 2014-2016 Actor LLC. <https://actor.im>
//

import Foundation

public class AAAuthLogInViewController: AAAuthViewController {
    
    let scrollView = UIScrollView()
    
    let welcomeLabel = UILabel()
    let field = UITextField()
    let fieldLine = UIView()
    
    var isFirstAppear = true
    
    public override init() {
        super.init(nibName: nil, bundle: nil)
        
        navigationItem.leftBarButtonItem = UIBarButtonItem(title: AALocalized("NavigationCancel"), style: .Plain, target: self, action: #selector(AAViewController.dismiss))
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
        welcomeLabel.text = AALocalized("AuthLoginTitle").replace("{app_name}", dest: ActorSDK.sharedActor().appName)
        welcomeLabel.textColor = ActorSDK.sharedActor().style.authTitleColor
        welcomeLabel.textAlignment = .Center
        
        if ActorSDK.sharedActor().authStrategy == .PhoneOnly {
            field.placeholder = AALocalized("AuthLoginPhone")
            field.keyboardType = .PhonePad
        } else if ActorSDK.sharedActor().authStrategy == .EmailOnly {
            field.placeholder = AALocalized("AuthLoginEmail")
            field.keyboardType = .EmailAddress
        } else if ActorSDK.sharedActor().authStrategy == .PhoneEmail {
            field.placeholder = AALocalized("AuthLoginPhoneEmail")
            field.keyboardType = .Default
        }
        field.autocapitalizationType = .None
        field.autocorrectionType = .No
        
        fieldLine.backgroundColor = ActorSDK.sharedActor().style.authSeparatorColor
        fieldLine.opaque = false
        
        scrollView.addSubview(welcomeLabel)
        scrollView.addSubview(field)
        scrollView.addSubview(fieldLine)
        view.addSubview(scrollView)

        super.viewDidLoad()
    }
    
    public override func viewDidLayoutSubviews() {
        super.viewDidLayoutSubviews()
        
        welcomeLabel.frame = CGRectMake(15, 90 - 66, view.width - 30, 28)
        
        fieldLine.frame = CGRectMake(10, 200 - 66, view.width - 20, 0.5)
        field.frame = CGRectMake(20, 156 - 66, view.width - 40, 44)
        
        scrollView.frame = view.bounds
        scrollView.contentSize = CGSizeMake(view.width, 240 - 66)
    }
    
    public override func nextDidTap() {
        let value = field.text!.trim()
        if value.length == 0 {
            shakeView(field, originalX: 20)
            shakeView(fieldLine, originalX: 10)
            return
        }
        
        if ActorSDK.sharedActor().authStrategy == .EmailOnly || ActorSDK.sharedActor().authStrategy == .PhoneEmail {
            if (AATools.isValidEmail(value)) {
                Actor.doStartAuthWithEmail(value).startUserAction().then { (res: ACAuthStartRes!) -> () in
                    if res.authMode.toNSEnum() == .OTP {
                        self.navigateNext(AAAuthOTPViewController(email: value, transactionHash: res.transactionHash))
                    } else {
                        self.alertUser(AALocalized("AuthUnsupported").replace("{app_name}", dest: ActorSDK.sharedActor().appName))
                    }
                }
                return
            }
        }
        
        if ActorSDK.sharedActor().authStrategy == .PhoneOnly || ActorSDK.sharedActor().authStrategy == .PhoneEmail {
            let numbersSet = NSCharacterSet(charactersInString: "0123456789").invertedSet
            let stripped = value.strip(numbersSet)
            if let parsed = Int64(stripped) {
                Actor.doStartAuthWithPhone(jlong(parsed)).startUserAction().then { (res: ACAuthStartRes!) -> () in
                    if res.authMode.toNSEnum() == .OTP {
                        let formatted = RMPhoneFormat().format("\(parsed)")
                        self.navigateNext(AAAuthOTPViewController(phone: formatted, transactionHash: res.transactionHash))
                    } else {
                        self.alertUser(AALocalized("AuthUnsupported").replace("{app_name}", dest: ActorSDK.sharedActor().appName))
                    }
                }
                return
            }
        }
        
        shakeView(field, originalX: 20)
        shakeView(fieldLine, originalX: 10)
    }
    
    public override func viewWillDisappear(animated: Bool) {
        super.viewWillDisappear(animated)
        
        field.resignFirstResponder()
    }
    
    public override func viewWillAppear(animated: Bool) {
        super.viewWillAppear(animated)
        
        if isFirstAppear {
            isFirstAppear = false
            field.becomeFirstResponder()
        }
    }
}