//
//  Copyright (c) 2014-2015 Actor LLC. <https://actor.im>
//

import Foundation

class EditFieldControllerConfig {
    
    var title: String!
    var actionTitle: String!
    var hint: String!
    var initialText: String!
    
    var fieldReturnKey: UIReturnKeyType = .Default
    var fieldHint: String!
    var fieldAutocorrectionType = UITextAutocorrectionType.Default
    var fieldAutocapitalizationType = UITextAutocapitalizationType.Sentences
    
    var didDismissTap: ((c: EditFieldController)->())?
    var didDoneTap: ((t: String, c: EditFieldController)->())?
    
    func check() {
        if title == nil {
            fatalError("Title is not set")
        }
    }
}

class EditFieldController: ACContentTableController {
    
    var fieldCell: ACEditRow!
    
    let config: EditFieldControllerConfig
    
    init(config: EditFieldControllerConfig) {
        
        self.config = config
        
        super.init(style: .SettingsGrouped)
        
        navigationItem.title = localized(config.title)
        navigationItem.leftBarButtonItem = UIBarButtonItem(title: localized("NavigationCancel"), style: .Plain, target: self, action: "doDismiss")
        
        if config.actionTitle != nil {
            navigationItem.rightBarButtonItem = UIBarButtonItem(title: localized(config.actionTitle), style: .Done, target: self, action: "doAction")
        } else {
            navigationItem.rightBarButtonItem = UIBarButtonItem(title: localized("NavigationDone"), style: .Done, target: self, action: "doAction")
        }
    }
    
    required init(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    override func tableDidLoad() {
        
        section { (s) -> () in
            
            if self.config.hint != nil {
                s.footerText = localized(self.config.hint)
            }
            
            self.fieldCell = s.edit { (r) -> () in
                
                if self.config.fieldHint != nil {
                    r.placeholder = localized(self.config.fieldHint)
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
        config.didDoneTap?(t: text, c: self)
    }
    
    func doDismiss() {
        if config.didDismissTap != nil {
            config.didDismissTap!(c: self)
        } else {
            dismiss()
        }
    }
    
    override func viewWillAppear(animated: Bool) {
        super.viewWillAppear(animated)
        
        if let c = tableView.cellForRowAtIndexPath(NSIndexPath(forRow: 0, inSection: 0)) as? EditCell {
            c.textField.becomeFirstResponder()
        }
    }
    
    override func viewWillDisappear(animated: Bool) {
        super.viewWillDisappear(animated)
        
        if let c = tableView.cellForRowAtIndexPath(NSIndexPath(forRow: 0, inSection: 0)) as? EditCell {
            c.textField.resignFirstResponder()
        }
    }
}