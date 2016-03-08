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
        
        navigationItem.leftBarButtonItem = UIBarButtonItem(title: AALocalized("NavigationCancel"), style: .Plain, target: self, action: "dismiss")
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
        welcomeLabel.text = "Log In to Actor"
        welcomeLabel.textColor = UIColor.blackColor().alpha(0.87)
        welcomeLabel.textAlignment = .Center
        
        field.placeholder = "Phone or Email"
        field.keyboardType = .Default
        field.autocapitalizationType = .None
        
        fieldLine.backgroundColor = UIColor.blackColor().alpha(0.2)
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
        if (AATools.isValidEmail(value)) {
            Actor.doStartAuthWithEmail(value).startUserAction().then { (res: ACAuthStartRes!) -> () in
                if res.authMode.toNSEnum() == .OTP {
                    self.navigateNext(AAAuthOTPViewController(email: value, transactionHash: res.transactionHash))
                } else {
                    self.alertUser("This account can't be authenticated in this version. Please, update app.")
                }
            }
        } else {
            let numbersSet = NSCharacterSet(charactersInString: "0123456789").invertedSet
            let stripped = value.strip(numbersSet)
            if let parsed = Int64(stripped) {
                Actor.doStartAuthWithPhone(jlong(parsed)).startUserAction().then { (res: ACAuthStartRes!) -> () in
                    if res.authMode.toNSEnum() == .OTP {
                        self.navigateNext(AAAuthOTPViewController(phone: value, transactionHash: res.transactionHash))
                    } else {
                        self.alertUser("This account can't be authenticated in this version. Please, update app.")
                    }
                }
            } else {
                shakeView(field, originalX: 20)
                shakeView(fieldLine, originalX: 10)
            }
        }
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