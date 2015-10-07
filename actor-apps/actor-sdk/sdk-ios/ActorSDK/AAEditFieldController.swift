//
//  Copyright (c) 2014-2015 Actor LLC. <https://actor.im>
//

import Foundation

public class AAEditFieldControllerConfig {
    
    public var title: String!
    public var actionTitle: String!
    public var hint: String!
    public var initialText: String!
    
    public var fieldReturnKey: UIReturnKeyType = .Default
    public var fieldHint: String!
    public var fieldAutocorrectionType = UITextAutocorrectionType.Default
    public var fieldAutocapitalizationType = UITextAutocapitalizationType.Sentences
    
    public var didDismissTap: ((c: AAEditFieldController)->())?
    public var didDoneTap: ((t: String, c: AAEditFieldController)->())?
    
    func check() {
        if title == nil {
            fatalError("Title is not set")
        }
    }
}

public class AAEditFieldController: ACContentTableController {
    
    public var fieldCell: ACEditRow!
    
    public let config: AAEditFieldControllerConfig
    
    public init(config: AAEditFieldControllerConfig) {
        
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
    
    public required init(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    public override func tableDidLoad() {
        
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
    
    public override func viewWillAppear(animated: Bool) {
        super.viewWillAppear(animated)
        
        if let c = tableView.cellForRowAtIndexPath(NSIndexPath(forRow: 0, inSection: 0)) as? EditCell {
            c.textField.becomeFirstResponder()
        }
    }
    
    public override func viewWillDisappear(animated: Bool) {
        super.viewWillDisappear(animated)
        
        if let c = tableView.cellForRowAtIndexPath(NSIndexPath(forRow: 0, inSection: 0)) as? EditCell {
            c.textField.resignFirstResponder()
        }
    }
}