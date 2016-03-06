//
//  Copyright (c) 2014-2016 Actor LLC. <https://actor.im>
//

import Foundation

class AAAuthAskName: AAAuthViewController {
    
    let welcomeLabel = UILabel()
    let field = UITextField()
    let fieldLine = UIView()
    let fieldSuccess = UILabel()
    
    var isFirstAppear = true
    
    override init() {
        super.init(nibName: nil, bundle: nil)
        
        navigationItem.leftBarButtonItem = UIBarButtonItem(title: AALocalized("NavigationCancel"), style: .Plain, target: self, action: "dismiss")
    }

    required init(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        view.backgroundColor = UIColor.whiteColor()
        
        welcomeLabel.font = UIFont.lightSystemFontOfSize(23)
        welcomeLabel.text = "Hi! What's your name?"
        welcomeLabel.textColor = UIColor.blackColor().alpha(0.87)
        welcomeLabel.textAlignment = .Center
        
        fieldSuccess.font = UIFont.systemFontOfSize(18)
        fieldSuccess.text = "Name looks great!"
        fieldSuccess.textColor = UIColor.greenColor()
        fieldSuccess.textAlignment = .Left
        fieldSuccess.hidden = true
        
        field.placeholder = "Your Name"
        field.addTarget(self, action: "fieldDidChanged", forControlEvents: .EditingChanged)
        
        fieldLine.backgroundColor = UIColor.blackColor().alpha(0.2)
        fieldLine.opaque = false
        
        view.addSubview(welcomeLabel)
        view.addSubview(fieldLine)
        view.addSubview(field)
        view.addSubview(fieldSuccess)
    }
    
    override func viewDidLayoutSubviews() {
        super.viewDidLayoutSubviews()
        
        welcomeLabel.frame = CGRectMake(15, 90, view.width - 30, 28)
        fieldLine.frame = CGRectMake(10, 200, view.width - 20, 0.5)
        field.frame = CGRectMake(20, 156, view.width - 40, 44)
        fieldSuccess.frame = CGRectMake(20, field.bottom + 15, view.width - 40, 44)
    }
    
    func fieldDidChanged() {
        if field.text!.trim().length > 0 {
            fieldSuccess.hidden = false
        } else {
            fieldSuccess.hidden = true
        }
    }
    
    override func viewWillAppear(animated: Bool) {
        super.viewWillAppear(animated)
        
        if isFirstAppear {
            isFirstAppear = false
            field.becomeFirstResponder()
        }
    }
    
    override func viewDidAppear(animated: Bool) {
        super.viewDidAppear(animated)
        
        field.becomeFirstResponder()
    }
    
    override func nextDidTap() {
        let name = field.text!.trim()
        if name.length > 0 {
            navigateNext(AAAuthAskPhone(name: name))
        } else {
            shakeView(field, originalX: 20)
            shakeView(fieldLine, originalX: 10)
        }
    }
    
    override func viewWillDisappear(animated: Bool) {
        super.viewWillDisappear(animated)
        
        field.resignFirstResponder()
    }
}