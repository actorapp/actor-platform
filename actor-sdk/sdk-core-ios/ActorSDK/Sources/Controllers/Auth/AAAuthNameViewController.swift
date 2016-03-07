//
//  Copyright (c) 2014-2016 Actor LLC. <https://actor.im>
//

import Foundation

public class AAAuthNameViewController: AAAuthViewController {
    
    let welcomeLabel = UILabel()
    let field = UITextField()
    let fieldLine = UIView()
    let fieldSuccess = UILabel()
    
    var isFirstAppear = true
    
    public override init() {
        super.init(nibName: nil, bundle: nil)
        
        navigationItem.leftBarButtonItem = UIBarButtonItem(title: AALocalized("NavigationCancel"), style: .Plain, target: self, action: "dismiss")
    }

    public required init(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    public override func viewDidLoad() {
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
    
    public override func viewDidLayoutSubviews() {
        super.viewDidLayoutSubviews()
        
        welcomeLabel.frame = CGRectMake(15, 90, view.width - 30, 28)
        fieldLine.frame = CGRectMake(10, 200, view.width - 20, 0.5)
        field.frame = CGRectMake(20, 156, view.width - 40, 44)
        fieldSuccess.frame = CGRectMake(20, field.bottom + 15, view.width - 40, 44)
    }
    
    func fieldDidChanged() {
//        if field.text!.trim().length > 0 {
//            fieldSuccess.hidden = false
//        } else {
//            fieldSuccess.hidden = true
//        }
    }
    
    public override func viewWillAppear(animated: Bool) {
        super.viewWillAppear(animated)
        
        if isFirstAppear {
            isFirstAppear = false
            field.becomeFirstResponder()
        }
    }
    
    public  override func viewDidAppear(animated: Bool) {
        super.viewDidAppear(animated)
        
        field.becomeFirstResponder()
    }
    
    public  override func nextDidTap() {
        let name = field.text!.trim()
        if name.length > 0 {
            navigateNext(AAAuthPhoneViewController(name: name))
        } else {
            shakeView(field, originalX: 20)
            shakeView(fieldLine, originalX: 10)
        }
    }
    
    public override func viewWillDisappear(animated: Bool) {
        super.viewWillDisappear(animated)
        
        field.resignFirstResponder()
    }
}