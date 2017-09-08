//
//  Copyright (c) 2014-2016 Actor LLC. <https://actor.im>
//

import Foundation

open class AAAuthLogInViewController: AAAuthViewController, UITextFieldDelegate {
    
    let scrollView = UIScrollView()
    
    let welcomeLabel = UILabel()
    let field = UITextField()
    let fieldLine = UIView()
    
    var isFirstAppear = true
    
    public override init() {
        super.init(nibName: nil, bundle: nil)
        
        navigationItem.leftBarButtonItem = UIBarButtonItem(title: AALocalized("NavigationCancel"), style: .plain, target: self, action: #selector(AAViewController.dismissController))
    }

    public required init(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    open override func viewDidLoad() {
     
        view.backgroundColor = UIColor.white
        
        scrollView.keyboardDismissMode = .onDrag
        scrollView.isScrollEnabled = true
        scrollView.alwaysBounceVertical = true
        
        welcomeLabel.font = UIFont.lightSystemFontOfSize(23)
        welcomeLabel.text = AALocalized("AuthLoginTitle").replace("{app_name}", dest: ActorSDK.sharedActor().appName)
        welcomeLabel.textColor = ActorSDK.sharedActor().style.authTitleColor
        welcomeLabel.textAlignment = .center
        
        if ActorSDK.sharedActor().authStrategy == .phoneOnly {
            field.placeholder = AALocalized("AuthLoginPhone")
            field.keyboardType = .phonePad
        } else if ActorSDK.sharedActor().authStrategy == .emailOnly {
            field.placeholder = AALocalized("AuthLoginEmail")
            field.keyboardType = .emailAddress
        } else if ActorSDK.sharedActor().authStrategy == .phoneEmail {
            field.placeholder = AALocalized("AuthLoginPhoneEmail")
            field.keyboardType = .default
        }
        field.autocapitalizationType = .none
        field.autocorrectionType = .no
        field.delegate = self
        
        fieldLine.backgroundColor = ActorSDK.sharedActor().style.authSeparatorColor
        fieldLine.isOpaque = false
        
        scrollView.addSubview(welcomeLabel)
        scrollView.addSubview(field)
        scrollView.addSubview(fieldLine)
        view.addSubview(scrollView)

        super.viewDidLoad()
    }
    
    open override func viewDidLayoutSubviews() {
        super.viewDidLayoutSubviews()
        
        welcomeLabel.frame = CGRect(x: 15, y: 90 - 66, width: view.width - 30, height: 28)
        
        fieldLine.frame = CGRect(x: 10, y: 200 - 66, width: view.width - 20, height: 0.5)
        field.frame = CGRect(x: 20, y: 156 - 66, width: view.width - 40, height: 44)
        
        scrollView.frame = view.bounds
        scrollView.contentSize = CGSize(width: view.width, height: 240 - 66)
    }
    
    open override func nextDidTap() {
        let value = field.text!.trim()
        if value.length == 0 {
            shakeView(field, originalX: 20)
            shakeView(fieldLine, originalX: 10)
            return
        }
        
        if ActorSDK.sharedActor().authStrategy == .emailOnly || ActorSDK.sharedActor().authStrategy == .phoneEmail {
            if (AATools.isValidEmail(value)) {
                Actor.doStartAuth(withEmail: value).startUserAction().then { (res: ACAuthStartRes!) -> () in
                    if res.authMode.toNSEnum() == .OTP {
                        self.navigateNext(AAAuthOTPViewController(email: value, transactionHash: res.transactionHash))
                    } else {
                        self.alertUser(AALocalized("AuthUnsupported").replace("{app_name}", dest: ActorSDK.sharedActor().appName))
                    }
                }
                return
            }
        }
        
        if ActorSDK.sharedActor().authStrategy == .phoneOnly || ActorSDK.sharedActor().authStrategy == .phoneEmail {
            let numbersSet = CharacterSet(charactersIn: "0123456789").inverted
            let stripped = value.strip(numbersSet)
            if let parsed = Int64(stripped) {
                Actor.doStartAuth(withPhone: jlong(parsed)).startUserAction().then { (res: ACAuthStartRes!) -> () in
                    if res.authMode.toNSEnum() == .OTP {
                        let formatted = RMPhoneFormat().format("\(parsed)")!
                        self.navigateNext(AAAuthOTPViewController(phone: formatted, transactionHash: res.transactionHash))
                    } else {
                        self.alertUser(AALocalized("AuthUnsupported").replace("{app_name}", dest: ActorSDK.sharedActor().appName))
                    }
                }
                return
            }
        }
        if ActorSDK.sharedActor().authStrategy == .usernameOnly {
           
                Actor.doStartAuth(withUsername: value).startUserAction().then { (res: ACAuthStartRes!) -> () in
                    if res.authMode.toNSEnum() == .OTP {
                        
                        self.navigateNext(AAAuthOTPViewController(name: value, transactionHash: res.transactionHash))
                    } else {
                        self.alertUser(AALocalized("AuthUnsupported").replace("{app_name}", dest: ActorSDK.sharedActor().appName))
                    }
                }
                return
            }
        
        
        shakeView(field, originalX: 20)
        shakeView(fieldLine, originalX: 10)
    }
    
    public func textFieldDidBeginEditing(_ textField: UITextField) {
        
        
    }
    public func textFieldDidEndEditing(_ textField: UITextField) {

    }
    
    
    override open func keyboardWillAppear(_ height: CGFloat) {
        scrollView.frame = CGRect(x: 0, y: 0, width: view.width, height: view.height - height)
        
        if AADevice.isiPhone4 || AADevice.isiPhone5 {
            
            let height = scrollView.height - height
            let offset: CGFloat = 245 + 44
            let destOffset = height * 0.66  - offset / 2
            
            scrollView.setContentOffset(CGPoint(x: 0, y: -destOffset), animated: true)
        }
    }
    
    override open func keyboardWillDisappear() {
        scrollView.frame = CGRect(x: 0, y: 0, width: view.width, height: view.height)
    }
    open override func viewWillAppear(_ animated: Bool) {
        super.viewWillAppear(animated)
        
        if isFirstAppear {
            isFirstAppear = false
            field.becomeFirstResponder()
        }
    }
}
