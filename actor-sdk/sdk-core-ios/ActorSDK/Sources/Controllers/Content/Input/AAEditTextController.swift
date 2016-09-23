//
//  Copyright (c) 2014-2016 Actor LLC. <https://actor.im>
//

import Foundation
import SZTextView

open class AAEditTextControllerConfig {
    
    open var title: String!
    open var hint: String!
    open var actionTitle: String!
    open var initialText: String!
    
    open var didDismissTap: ((AAEditTextController) -> ())!
    
    open var didCompleteTap: ((String, AAEditTextController) -> ())!
    
    func check() {
        
        if title == nil {
            fatalError("Title not set")
        }
        if actionTitle == nil {
            fatalError("Action Title not set")
        }
    }
}

open class AAEditTextController: AAViewController {
    
    fileprivate let config: AAEditTextControllerConfig
    
    fileprivate var textView =  SZTextView()
    
    public init(config: AAEditTextControllerConfig) {
        
        self.config = config
        
        super.init(nibName: nil, bundle: nil)
        
        self.navigationItem.title = AALocalized(config.title)
        
        if config.actionTitle != nil {
            self.navigationItem.rightBarButtonItem = UIBarButtonItem(title: AALocalized(config.actionTitle), style: UIBarButtonItemStyle.done, target: self, action: #selector(AAEditTextController.doSave))
        } else {
            self.navigationItem.rightBarButtonItem = UIBarButtonItem(title: AALocalized("NavigationDone"), style: UIBarButtonItemStyle.done, target: self, action: #selector(AAEditTextController.doSave))
        }
        self.navigationItem.leftBarButtonItem = UIBarButtonItem(title: AALocalized("NavigationCancel"), style: UIBarButtonItemStyle.plain, target: self, action: #selector(AAEditTextController.doCancel))
        
        self.textView.fadeTime = 0
        if let h = config.hint {
            self.textView.placeholder = AALocalized(h)
        }
        self.textView.text = config.initialText
        self.textView.font = UIFont.systemFont(ofSize: 18)
        
        self.view.backgroundColor = UIColor.white
    }

    public required init(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    open override func viewDidLoad() {
        super.viewDidLoad()
        
        self.view.addSubview(textView)
    }
    
    open override func viewWillAppear(_ animated: Bool) {
        super.viewWillAppear(animated)
        
        self.textView.becomeFirstResponder()
    }
    
    open override func viewWillDisappear(_ animated: Bool) {
        super.viewWillDisappear(animated)
        
        self.textView.resignFirstResponder()
    }
    
    open func doSave() {
        self.config.didCompleteTap?(textView.text, self)
    }
    
    open func doCancel() {
        if self.config.didDismissTap != nil {
           self.config.didDismissTap!(self)
        } else {
            dismissController()
        }
    }
    
    open override func viewDidLayoutSubviews() {
        super.viewDidLayoutSubviews()
        
        self.textView.frame = CGRect(x: 7, y: 7, width: self.view.bounds.width - 14, height: self.view.bounds.height - 14)
    }
}


