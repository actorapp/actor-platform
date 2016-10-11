//
//  Copyright (c) 2014-2016 Actor LLC. <https://actor.im>
//

import Foundation

public extension UIViewController {

    public func alertSheet(_ closure: (_ a: AAAlertSetting) -> ()) {
        
        let s = AAAlertSetting()
        
        closure(s)
        
        let controller = UIAlertController(title: AALocalized(s.title), message: AALocalized(s.message), preferredStyle: .actionSheet)
            
        for i in s.actions {
            controller.addAction(UIAlertAction(title: AALocalized(i.title), style: i.isDestructive ? UIAlertActionStyle.destructive : UIAlertActionStyle.default, handler: { (c) -> Void in
                i.closure()
            }))
        }
            
        controller.addAction(UIAlertAction(title: AALocalized("AlertCancel"), style: .cancel, handler: nil))
        
        present(controller, animated: true, completion: nil)
    }
    
    public func confirmDestructive(_ message: String, action: String, yes: @escaping ()->()) {
        let controller = UIAlertController(title: nil, message: message, preferredStyle: .alert)
        controller.addAction(UIAlertAction(title: action, style: .destructive, handler: { (act) -> Void in
            yes()
        }))
        controller.addAction(UIAlertAction(title: AALocalized("AlertCancel"), style: .cancel, handler: nil))
        present(controller, animated: true, completion: nil)
    }
}

open class AAAlertSetting {
    
    open var cancel: String!
    open var title: String!
    open var message: String!
    
    fileprivate var actions = [AlertActions]()
    
    open func action(_ title: String, closure: @escaping ()->()) {
        let a = AlertActions()
        a.title = title
        a.closure = closure
        actions.append(a)
    }
    
    open func destructive(_ title: String, closure: @escaping ()->()) {
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
