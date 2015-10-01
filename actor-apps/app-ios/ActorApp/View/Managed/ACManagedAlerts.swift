//
//  Copyright (c) 2014-2015 Actor LLC. <https://actor.im>
//

import Foundation

extension UIViewController {

    func alertSheet(closure: (a: AlertSetting) -> ()) {
        
        let s = AlertSetting()
        
        closure(a: s)
        
        let controller = UIAlertController(title: localized(s.title), message: localized(s.message), preferredStyle: .ActionSheet)
            
        for i in s.actions {
            controller.addAction(UIAlertAction(title: localized(i.title), style: i.isDestructive ? UIAlertActionStyle.Destructive : UIAlertActionStyle.Default, handler: { (c) -> Void in
                i.closure()
            }))
        }
            
        controller.addAction(UIAlertAction(title: localized("AlertCancel"), style: .Cancel, handler: nil))
        
        presentViewController(controller, animated: true, completion: nil)
    }
    
    func confirmDestructive(message: String, action: String, yes: ()->()) {
        let controller = UIAlertController(title: nil, message: message, preferredStyle: .Alert)
        controller.addAction(UIAlertAction(title: action, style: .Destructive, handler: { (act) -> Void in
            yes()
        }))
        controller.addAction(UIAlertAction(title: localized("AlertCancel"), style: .Cancel, handler: nil))        
        presentViewController(controller, animated: true, completion: nil)
    }
}

class AlertSetting {
    
    var cancel: String!
    var title: String!
    var message: String!
    
    private var actions = [AlertActions]()
    
    func action(title: String, closure: ()->()) {
        let a = AlertActions()
        a.title = title
        a.closure = closure
        actions.append(a)
    }
    
    func destructive(title: String, closure: ()->()) {
        let a = AlertActions()
        a.title = title
        a.closure = closure
        a.isDestructive = true
        actions.append(a)
    }
}

class AlertActions {
    var isDestructive = false
    var title: String!
    var closure: (()->())!
}