//
//  Copyright (c) 2014-2016 Actor LLC. <https://actor.im>
//

import Foundation
import SZTextView

public class AAEditTextControllerConfig {
    
    public var title: String!
    public var hint: String!
    public var actionTitle: String!
    public var initialText: String!
    
    public var didDismissTap: ((AAEditTextController) -> ())!
    
    public var didCompleteTap: ((String, AAEditTextController) -> ())!
    
    func check() {
        
        if title == nil {
            fatalError("Title not set")
        }
        if actionTitle == nil {
            fatalError("Action Title not set")
        }
    }
}

public class AAEditTextController: AAViewController {
    
    private let config: AAEditTextControllerConfig
    
    private var textView =  SZTextView()
    
    public init(config: AAEditTextControllerConfig) {
        
        self.config = config
        
        super.init(nibName: nil, bundle: nil)
        
        self.navigationItem.title = AALocalized(config.title)
        
        if config.actionTitle != nil {
            self.navigationItem.rightBarButtonItem = UIBarButtonItem(title: AALocalized(config.actionTitle), style: UIBarButtonItemStyle.Done, target: self, action: #selector(AAEditTextController.doSave))
        } else {
            self.navigationItem.rightBarButtonItem = UIBarButtonItem(title: AALocalized("NavigationDone"), style: UIBarButtonItemStyle.Done, target: self, action: #selector(AAEditTextController.doSave))
        }
        self.navigationItem.leftBarButtonItem = UIBarButtonItem(title: AALocalized("NavigationCancel"), style: UIBarButtonItemStyle.Plain, target: self, action: #selector(AAEditTextController.doCancel))
        
        self.textView.fadeTime = 0
        if let h = config.hint {
            self.textView.placeholder = AALocalized(h)
        }
        self.textView.text = config.initialText
        self.textView.font = UIFont.systemFontOfSize(18)
        
        self.view.backgroundColor = UIColor.whiteColor()
    }

    public required init(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    public override func viewDidLoad() {
        super.viewDidLoad()
        
        self.view.addSubview(textView)
    }
    
    public override func viewWillAppear(animated: Bool) {
        super.viewWillAppear(animated)
        
        self.textView.becomeFirstResponder()
    }
    
    public override func viewWillDisappear(animated: Bool) {
        super.viewWillDisappear(animated)
        
        self.textView.resignFirstResponder()
    }
    
    public func doSave() {
        self.config.didCompleteTap?(textView.text, self)
    }
    
    public func doCancel() {
        if self.config.didDismissTap != nil {
           self.config.didDismissTap!(self)
        } else {
            dismiss()
        }
    }
    
    public override func viewDidLayoutSubviews() {
        super.viewDidLayoutSubviews()
        
        self.textView.frame = CGRectMake(7, 7, self.view.bounds.width - 14, self.view.bounds.height - 14)
    }
}


