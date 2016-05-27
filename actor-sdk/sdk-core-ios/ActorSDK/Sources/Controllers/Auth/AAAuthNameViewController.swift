//
//  Copyright (c) 2014-2016 Actor LLC. <https://actor.im>
//

import Foundation

public class AAAuthNameViewController: AAAuthViewController {
    
    let transactionHash: String?
    
    let scrollView = UIScrollView()
    
    let welcomeLabel = UILabel()
    let field = UITextField()
    let fieldLine = UIView()
    
    var isFirstAppear = true
    
    public init(transactionHash: String? = nil) {
        self.transactionHash = transactionHash
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
        welcomeLabel.text = AALocalized("AuthNameTitle")
        welcomeLabel.textColor = ActorSDK.sharedActor().style.authTitleColor
        welcomeLabel.textAlignment = .Center
        
        field.placeholder = AALocalized("AuthNamePlaceholder")
        field.keyboardType = .Default
        field.autocapitalizationType = .Words
        field.textColor = ActorSDK.sharedActor().style.authTextColor
        field.addTarget(self, action: #selector(AAAuthNameViewController.fieldDidChanged), forControlEvents: .EditingChanged)
        
        fieldLine.backgroundColor = ActorSDK.sharedActor().style.authSeparatorColor
        fieldLine.opaque = false
        
        scrollView.addSubview(welcomeLabel)
        scrollView.addSubview(fieldLine)
        scrollView.addSubview(field)
        
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
    
    func fieldDidChanged() {
//        if field.text!.trim().length > 0 {
//            fieldSuccess.hidden = false
//        } else {
//            fieldSuccess.hidden = true
//        }
    }
    
    public  override func nextDidTap() {
        let name = field.text!.trim()
        if name.length > 0 {
            if transactionHash != nil {
                let promise = Actor.doSignupWithName(name, withSex: ACSex.UNKNOWN(), withTransaction: transactionHash!)
                promise.then { (r: ACAuthRes!) -> () in
                    let promise = Actor.doCompleteAuth(r).startUserAction()
                    promise.then { (r: JavaLangBoolean!) -> () in
                        self.onAuthenticated()
                    }
                }
                promise.startUserAction()
            } else {
                if ActorSDK.sharedActor().authStrategy == .PhoneOnly || ActorSDK.sharedActor().authStrategy == .PhoneEmail {
                    navigateNext(AAAuthPhoneViewController(name: name))
                } else {
                    navigateNext(AAAuthEmailViewController(name: name))
                }
            }
        } else {
            shakeView(field, originalX: 20)
            shakeView(fieldLine, originalX: 10)
        }
    }
    
    public override func viewWillAppear(animated: Bool) {
        super.viewWillAppear(animated)
        
        if isFirstAppear {
            isFirstAppear = false
            field.becomeFirstResponder()
        }
    }
    
    public override func viewWillDisappear(animated: Bool) {
        super.viewWillDisappear(animated)
        
        field.resignFirstResponder()
    }
}