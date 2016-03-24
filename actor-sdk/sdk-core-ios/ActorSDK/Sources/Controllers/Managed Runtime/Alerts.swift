//
//  Copyright (c) 2014-2016 Actor LLC. <https://actor.im>
//

import Foundation

private var pickDocumentClosure = "_pick_document_closure"
private var actionShitReference = "_action_shit"

public extension UIViewController {
    
    public func alertUser(message: String) {
        let controller = UIAlertController(title: nil, message: message, preferredStyle: UIAlertControllerStyle.Alert)
        controller.addAction(UIAlertAction(title: AALocalized("AlertOk"), style: UIAlertActionStyle.Cancel, handler: nil))
        self.presentViewController(controller, animated: true, completion: nil)
    }
    
    public func confirmAlertUser(message: String, action: String, tapYes: ()->(), tapNo: (()->())? = nil) {
        let controller = UIAlertController(title: nil, message: message, preferredStyle: UIAlertControllerStyle.Alert)
        controller.addAction(UIAlertAction(title: AALocalized(message), style: UIAlertActionStyle.Default, handler: { (alertView) -> () in
            tapYes()
        }))
        controller.addAction(UIAlertAction(title: AALocalized("AlertCancel"), style: UIAlertActionStyle.Cancel, handler: { (alertView) -> () in
            tapNo?()
        }))
        self.presentViewController(controller, animated: true, completion: nil)
    }
    
    public func confirmDangerSheetUser(action: String, tapYes: ()->(), tapNo: (()->())?) {
        showActionSheet(nil, buttons: [], cancelButton: "AlertCancel", destructButton: action, sourceView: UIView(), sourceRect: CGRectZero) { (index) -> () in
            if index == -2 {
                tapYes()
            } else {
                tapNo?()
            }
        }
    }
    
    public func showActionSheet(title: String?, buttons: [String], cancelButton: String?, destructButton: String?, sourceView: UIView, sourceRect: CGRect, tapClosure: (index: Int) -> ()) {
        
        let controller = UIAlertController(title: title, message: nil, preferredStyle: UIAlertControllerStyle.ActionSheet)
        
        if cancelButton != nil {
            controller.addAction(UIAlertAction(title: AALocalized(cancelButton), style: UIAlertActionStyle.Cancel, handler: { (alertView) -> () in
                tapClosure(index: -1)
            }))
        }
        
        if destructButton != nil {
            controller.addAction(UIAlertAction(title: AALocalized(destructButton), style: UIAlertActionStyle.Destructive, handler: { (alertView) -> () in
                tapClosure(index: -1)
            }))
        }
        
        for b in 0..<buttons.count {
            controller.addAction(UIAlertAction(title: AALocalized(buttons[b]), style: UIAlertActionStyle.Default, handler: { (alertView) -> () in
                tapClosure(index: b)
            }))
        }
        
        self.presentViewController(controller, animated: true, completion: nil)
    }
    
    func showActionSheet(buttons: [String], cancelButton: String?, destructButton: String?, sourceView: UIView, sourceRect: CGRect, tapClosure: (index: Int) -> ()) {
        showActionSheet(nil, buttons:buttons, cancelButton: cancelButton, destructButton: destructButton, sourceView: sourceView, sourceRect:sourceRect,  tapClosure: tapClosure)
    }
    
    func startEditText(@noescape closure: (AAEditTextControllerConfig) -> ()) {
        let config = AAEditTextControllerConfig()
        closure(config)
        config.check()
        let controller = AANavigationController(rootViewController: AAEditTextController(config: config))
        if (AADevice.isiPad) {
            controller.modalPresentationStyle = .FormSheet
        }
        self.presentViewController(controller, animated: true, completion: nil)
    }
    
    func startEditField(@noescape closure: (c: AAEditFieldControllerConfig) -> ()) {
        let config = AAEditFieldControllerConfig()
        closure(c: config)
        config.check()
        let controller = AANavigationController(rootViewController: AAEditFieldController(config: config))
        if (AADevice.isiPad) {
            controller.modalPresentationStyle = .FormSheet
        }
        self.presentViewController(controller, animated: true, completion: nil)
    }
}

