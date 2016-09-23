//
//  Copyright (c) 2014-2016 Actor LLC. <https://actor.im>
//

import Foundation

open class AAEditFieldControllerConfig {
    
    open var title: String!
    open var actionTitle: String!
    open var hint: String!
    open var initialText: String!
    
    open var fieldReturnKey: UIReturnKeyType = .default
    open var fieldHint: String!
    open var fieldAutocorrectionType = UITextAutocorrectionType.default
    open var fieldAutocapitalizationType = UITextAutocapitalizationType.sentences
    
    open var didDismissTap: ((_ c: AAEditFieldController)->())?
    open var didDoneTap: ((_ t: String, _ c: AAEditFieldController)->())?
    
    func check() {
        if title == nil {
            fatalError("Title is not set")
        }
    }
}

open class AAEditFieldController: AAContentTableController {
    
    open var fieldCell: AAEditRow!
    
    open let config: AAEditFieldControllerConfig
    
    public init(config: AAEditFieldControllerConfig) {
        
        self.config = config
        
        super.init(style: .settingsGrouped)
        
        navigationItem.title = AALocalized(config.title)
        navigationItem.leftBarButtonItem = UIBarButtonItem(title: AALocalized("NavigationCancel"), style: .plain, target: self, action: #selector(AAEditFieldController.doDismiss))
        
        if config.actionTitle != nil {
            navigationItem.rightBarButtonItem = UIBarButtonItem(title: AALocalized(config.actionTitle), style: .done, target: self, action: #selector(AAEditFieldController.doAction))
        } else {
            navigationItem.rightBarButtonItem = UIBarButtonItem(title: AALocalized("NavigationDone"), style: .done, target: self, action: #selector(AAEditFieldController.doAction))
        }
    }
    
    public required init(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    open override func tableDidLoad() {
        
        section { (s) -> () in
            
            if self.config.hint != nil {
                s.footerText = AALocalized(self.config.hint).replace("{appname}", dest: ActorSDK.sharedActor().appName)
            }
            
            self.fieldCell = s.edit { (r) -> () in
                
                if self.config.fieldHint != nil {
                    r.placeholder = AALocalized(self.config.fieldHint).replace("{appname}", dest: ActorSDK.sharedActor().appName)
                }
                r.autocapitalizationType = self.config.fieldAutocapitalizationType
                r.autocorrectionType = self.config.fieldAutocorrectionType
                r.returnKeyType = self.config.fieldReturnKey
                r.text = self.config.initialText
                
                r.returnAction = { () -> () in
                    self.doAction()
                }
            }
        }
    }
    
    func doAction() {
        if fieldCell.text == nil {
            return
        }
        
        let text = fieldCell.text!.trim()
        config.didDoneTap?(text, self)
    }
    
    func doDismiss() {
        if config.didDismissTap != nil {
            config.didDismissTap!(self)
        } else {
            dismissController()
        }
    }
    
    open override func viewWillAppear(_ animated: Bool) {
        super.viewWillAppear(animated)
        
        if let c = tableView.cellForRow(at: IndexPath(row: 0, section: 0)) as? AAEditCell {
            c.textField.becomeFirstResponder()
        }
    }
    
    open override func viewWillDisappear(_ animated: Bool) {
        super.viewWillDisappear(animated)
        
        if let c = tableView.cellForRow(at: IndexPath(row: 0, section: 0)) as? AAEditCell {
            c.textField.resignFirstResponder()
        }
    }
}
