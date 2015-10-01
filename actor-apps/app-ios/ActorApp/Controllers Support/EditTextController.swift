//
//  Copyright (c) 2014-2015 Actor LLC. <https://actor.im>
//

import Foundation

class EditTextControllerConfig {
    
    var title: String!
    var hint: String!
    var actionTitle: String!
    var initialText: String!
    
    var didDismissTap: ((EditTextController) -> ())!
    
    var didCompleteTap: ((String, EditTextController) -> ())!
    
    func check() {
        
        if title == nil {
            fatalError("Title not set")
        }
        if actionTitle == nil {
            fatalError("Action Title not set")
        }
    }
}

class EditTextController: AAViewController {
    
    private let config: EditTextControllerConfig
    
    private var textView =  SZTextView()
    
    init(config: EditTextControllerConfig) {
        
        self.config = config
        
        super.init(nibName: nil, bundle: nil)
        
        self.navigationItem.title = localized(config.title)
        
        if config.actionTitle != nil {
            self.navigationItem.rightBarButtonItem = UIBarButtonItem(title: localized(config.actionTitle), style: UIBarButtonItemStyle.Done, target: self, action: "doSave")
        } else {
            self.navigationItem.rightBarButtonItem = UIBarButtonItem(title: localized("NavigationDone"), style: UIBarButtonItemStyle.Done, target: self, action: "doSave")
        }
        self.navigationItem.leftBarButtonItem = UIBarButtonItem(title: localized("NavigationCancel"), style: UIBarButtonItemStyle.Plain, target: self, action: "doCancel")
        
        self.textView.fadeTime = 0
        if let h = config.hint {
            self.textView.placeholder = localized(h)
        }
        self.textView.text = config.initialText
        self.textView.font = UIFont.systemFontOfSize(18)
        
        self.view.backgroundColor = UIColor.whiteColor()
    }

    required init(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        self.view.addSubview(textView)
    }
    
    override func viewWillAppear(animated: Bool) {
        super.viewWillAppear(animated)
        
        self.textView.becomeFirstResponder()
    }
    
    override func viewWillDisappear(animated: Bool) {
        super.viewWillDisappear(animated)
        
        self.textView.resignFirstResponder()
    }
    
    func doSave() {
        self.config.didCompleteTap?(textView.text, self)
    }
    
    func doCancel() {
        if self.config.didDismissTap != nil {
           self.config.didDismissTap!(self)
        } else {
            dismiss()
        }
    }
    
    override func viewDidLayoutSubviews() {
        super.viewDidLayoutSubviews()
        
        self.textView.frame = CGRectMake(7, 7, self.view.bounds.width - 14, self.view.bounds.height - 14)
    }
}


