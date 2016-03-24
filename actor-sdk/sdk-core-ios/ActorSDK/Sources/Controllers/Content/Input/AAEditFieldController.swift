//
//  Copyright (c) 2014-2016 Actor LLC. <https://actor.im>
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

public class AAEditFieldController: AAContentTableController {
    
    public var fieldCell: AAEditRow!
    
    public let config: AAEditFieldControllerConfig
    
    public init(config: AAEditFieldControllerConfig) {
        
        self.config = config
        
        super.init(style: .SettingsGrouped)
        
        navigationItem.title = AALocalized(config.title)
        navigationItem.leftBarButtonItem = UIBarButtonItem(title: AALocalized("NavigationCancel"), style: .Plain, target: self, action: #selector(AAEditFieldController.doDismiss))
        
        if config.actionTitle != nil {
            navigationItem.rightBarButtonItem = UIBarButtonItem(title: AALocalized(config.actionTitle), style: .Done, target: self, action: #selector(AAEditFieldController.doAction))
        } else {
            navigationItem.rightBarButtonItem = UIBarButtonItem(title: AALocalized("NavigationDone"), style: .Done, target: self, action: #selector(AAEditFieldController.doAction))
        }
    }
    
    public required init(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    public override func tableDidLoad() {
        
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
        
        if let c = tableView.cellForRowAtIndexPath(NSIndexPath(forRow: 0, inSection: 0)) as? AAEditCell {
            c.textField.becomeFirstResponder()
        }
    }
    
    public override func viewWillDisappear(animated: Bool) {
        super.viewWillDisappear(animated)
        
        if let c = tableView.cellForRowAtIndexPath(NSIndexPath(forRow: 0, inSection: 0)) as? AAEditCell {
            c.textField.resignFirstResponder()
        }
    }
}