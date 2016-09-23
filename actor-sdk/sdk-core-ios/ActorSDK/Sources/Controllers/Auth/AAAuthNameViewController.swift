//
//  Copyright (c) 2014-2016 Actor LLC. <https://actor.im>
//

import Foundation

open class AAAuthNameViewController: AAAuthViewController {
    
    let transactionHash: String?
    
    let scrollView = UIScrollView()
    
    let welcomeLabel = UILabel()
    let field = UITextField()
    let fieldLine = UIView()
    
    var isFirstAppear = true
    
    public init(transactionHash: String? = nil) {
        self.transactionHash = transactionHash
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
        welcomeLabel.text = AALocalized("AuthNameTitle")
        welcomeLabel.textColor = ActorSDK.sharedActor().style.authTitleColor
        welcomeLabel.textAlignment = .center
        
        field.placeholder = AALocalized("AuthNamePlaceholder")
        field.keyboardType = .default
        field.autocapitalizationType = .words
        field.textColor = ActorSDK.sharedActor().style.authTextColor
        field.addTarget(self, action: #selector(AAAuthNameViewController.fieldDidChanged), for: .editingChanged)
        
        fieldLine.backgroundColor = ActorSDK.sharedActor().style.authSeparatorColor
        fieldLine.isOpaque = false
        
        scrollView.addSubview(welcomeLabel)
        scrollView.addSubview(fieldLine)
        scrollView.addSubview(field)
        
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
    
    func fieldDidChanged() {
//        if field.text!.trim().length > 0 {
//            fieldSuccess.hidden = false
//        } else {
//            fieldSuccess.hidden = true
//        }
    }
    
    open  override func nextDidTap() {
        let name = field.text!.trim()
        if name.length > 0 {
            if transactionHash != nil {
                let promise = Actor.doSignup(withName: name, with: ACSex.unknown(), withTransaction: transactionHash!)
                promise.then { (r: ACAuthRes!) -> () in
                    let promise = Actor.doCompleteAuth(r).startUserAction()
                    promise.then { (r: JavaLangBoolean!) -> () in
                        self.onAuthenticated()
                    }
                }
                promise.startUserAction()
            } else {
                if ActorSDK.sharedActor().authStrategy == .phoneOnly || ActorSDK.sharedActor().authStrategy == .phoneEmail {
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
    
    open override func viewWillAppear(_ animated: Bool) {
        super.viewWillAppear(animated)
        
        if isFirstAppear {
            isFirstAppear = false
            field.becomeFirstResponder()
        }
    }
    
    open override func viewWillDisappear(_ animated: Bool) {
        super.viewWillDisappear(animated)
        
        field.resignFirstResponder()
    }
}
