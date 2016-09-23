//
//  Copyright (c) 2014-2016 Actor LLC. <https://actor.im>
//

import Foundation

private var pickDocumentClosure = "_pick_document_closure"
private var actionShitReference = "_action_shit"

public extension UIViewController {
    
    public func alertUser(_ message: String) {
        let controller = UIAlertController(title: nil, message: message, preferredStyle: UIAlertControllerStyle.alert)
        controller.addAction(UIAlertAction(title: AALocalized("AlertOk"), style: UIAlertActionStyle.cancel, handler: nil))
        self.present(controller, animated: true, completion: nil)
    }
    
    public func alertUser(_ message: String, tapYes: @escaping ()->()) {
        let controller = UIAlertController(title: nil, message: message, preferredStyle: UIAlertControllerStyle.alert)
        controller.addAction(UIAlertAction(title: AALocalized("AlertOk"), style: UIAlertActionStyle.cancel, handler: { (alertView) -> () in
            tapYes()
        }))
        self.present(controller, animated: true, completion: nil)
    }
    
    public func confirmAlertUser(_ message: String, action: String, tapYes: @escaping ()->(), tapNo: (()->())? = nil) {
        let controller = UIAlertController(title: nil, message: AALocalized(message), preferredStyle: UIAlertControllerStyle.alert)
        controller.addAction(UIAlertAction(title: AALocalized(action), style: UIAlertActionStyle.default, handler: { (alertView) -> () in
            tapYes()
        }))
        controller.addAction(UIAlertAction(title: AALocalized("AlertCancel"), style: UIAlertActionStyle.cancel, handler: { (alertView) -> () in
            tapNo?()
        }))
        self.present(controller, animated: true, completion: nil)
    }
    
    public func confirmAlertUserDanger(_ message: String, action: String, tapYes: @escaping ()->(), tapNo: (()->())? = nil) {
        let controller = UIAlertController(title: nil, message: AALocalized(message), preferredStyle: UIAlertControllerStyle.alert)
        controller.addAction(UIAlertAction(title: AALocalized(action), style: UIAlertActionStyle.destructive, handler: { (alertView) -> () in
            tapYes()
        }))
        controller.addAction(UIAlertAction(title: AALocalized("AlertCancel"), style: UIAlertActionStyle.cancel, handler: { (alertView) -> () in
            tapNo?()
        }))
        self.present(controller, animated: true, completion: nil)
    }
    
    public func confirmDangerSheetUser(_ action: String, tapYes: @escaping ()->(), tapNo: (()->())?) {
        showActionSheet(nil, buttons: [], cancelButton: "AlertCancel", destructButton: action, sourceView: UIView(), sourceRect: CGRect.zero) { (index) -> () in
            if index == -2 {
                tapYes()
            } else {
                tapNo?()
            }
        }
    }
    
    public func showActionSheet(_ title: String?, buttons: [String], cancelButton: String?, destructButton: String?, sourceView: UIView, sourceRect: CGRect, tapClosure: @escaping (_ index: Int) -> ()) {
        
        let controller = UIAlertController(title: title, message: nil, preferredStyle: UIAlertControllerStyle.actionSheet)
        
        if cancelButton != nil {
            controller.addAction(UIAlertAction(title: AALocalized(cancelButton!), style: UIAlertActionStyle.cancel, handler: { (alertView) -> () in
                tapClosure(-1)
            }))
        }
        
        if destructButton != nil {
            controller.addAction(UIAlertAction(title: AALocalized(destructButton!), style: UIAlertActionStyle.destructive, handler: { (alertView) -> () in
                tapClosure(-2)
            }))
        }
        
        for b in 0..<buttons.count {
            controller.addAction(UIAlertAction(title: AALocalized(buttons[b]), style: UIAlertActionStyle.default, handler: { (alertView) -> () in
                tapClosure(b)
            }))
        }
        
        self.present(controller, animated: true, completion: nil)
    }
    
    func showActionSheet(_ buttons: [String], cancelButton: String?, destructButton: String?, sourceView: UIView, sourceRect: CGRect, tapClosure: @escaping (_ index: Int) -> ()) {
        showActionSheet(nil, buttons:buttons, cancelButton: cancelButton, destructButton: destructButton, sourceView: sourceView, sourceRect:sourceRect,  tapClosure: tapClosure)
    }
    
    func startEditText(_ closure: (AAEditTextControllerConfig) -> ()) {
        let config = AAEditTextControllerConfig()
        closure(config)
        config.check()
        let controller = AANavigationController(rootViewController: AAEditTextController(config: config))
        if (AADevice.isiPad) {
            controller.modalPresentationStyle = .formSheet
        }
        self.present(controller, animated: true, completion: nil)
    }
    
    func startEditField(_ closure: (_ c: AAEditFieldControllerConfig) -> ()) {
        let config = AAEditFieldControllerConfig()
        closure(config)
        config.check()
        let controller = AANavigationController(rootViewController: AAEditFieldController(config: config))
        if (AADevice.isiPad) {
            controller.modalPresentationStyle = .formSheet
        }
        self.present(controller, animated: true, completion: nil)
    }
}

