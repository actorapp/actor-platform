//
//  Copyright (c) 2014-2016 Actor LLC. <https://actor.im>
//

import Foundation
import RMUniversalAlert

private var pickDocumentClosure = "_pick_document_closure"
private var actionShitReference = "_action_shit"

public extension UIViewController {
    
    public func alertUser(message: String) {
        RMUniversalAlert.showAlertInViewController(self,
            withTitle: nil,
            message: AALocalized(message),
            cancelButtonTitle: AALocalized("AlertOk"),
            destructiveButtonTitle: nil,
            otherButtonTitles: nil,
            tapBlock: nil)
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
    
    public func confirmAlertUser(message: String, action: String, tapYes: ()->(), tapNo: (()->())? = nil) {
        RMUniversalAlert.showAlertInViewController(self,
            withTitle: nil,
            message: AALocalized(message),
            cancelButtonTitle: AALocalized("AlertCancel"),
            destructiveButtonTitle: nil,
            otherButtonTitles: [AALocalized(action)],
            tapBlock: { (alert, buttonIndex) -> Void in
                if (buttonIndex >= alert.firstOtherButtonIndex) {
                    tapYes()
                } else {
                    tapNo?()
                }
        })
    }
    
    public func textInputAlert(message: String, content: String?, action:String, tapYes: (nval: String)->()) {
        let alertView = UIAlertView(
            title: nil,
            message: AALocalized(message),
            delegate: self,
            cancelButtonTitle: AALocalized("AlertCancel"))
        alertView.addButtonWithTitle(AALocalized(action))
        alertView.alertViewStyle = UIAlertViewStyle.PlainTextInput
        alertView.textFieldAtIndex(0)!.autocapitalizationType = UITextAutocapitalizationType.Words
        alertView.textFieldAtIndex(0)!.text = content
        alertView.textFieldAtIndex(0)!.keyboardAppearance = ActorSDK.sharedActor().style.isDarkApp ? UIKeyboardAppearance.Dark : UIKeyboardAppearance.Light
//        alertView.tapBlock = { (alert: UIAlertView, buttonIndex) -> () in
//            if (buttonIndex != alert.cancelButtonIndex) {
//                tapYes(nval: alert.textFieldAtIndex(0)!.text!)
//            }
//        }
        alertView.show()
    }
    
    public func confirmUser(message: String, action: String, cancel: String, sourceView: UIView, sourceRect: CGRect, tapYes: ()->()) {
        RMUniversalAlert.showActionSheetInViewController(
            self,
            withTitle: nil,
            message: AALocalized(message),
            cancelButtonTitle: AALocalized(cancel),
            destructiveButtonTitle: AALocalized(action),
            otherButtonTitles: nil,
            popoverPresentationControllerBlock: { (popover: RMPopoverPresentationController) -> Void in
                popover.sourceView = sourceView
                popover.sourceRect = sourceRect
            },
            tapBlock: { (alert, buttonIndex) -> Void in
                if (buttonIndex == alert.destructiveButtonIndex) {
                    tapYes()
                }
        })
    }
    
    public func showActionSheet(title: String?, buttons: [String], cancelButton: String?, destructButton: String?, sourceView: UIView, sourceRect: CGRect, tapClosure: (index: Int) -> ()) {
        var convertedButtons:[String] = [String]()
        for b in buttons {
            convertedButtons.append(AALocalized(b))
        }
        
        RMUniversalAlert.showActionSheetInViewController(
            self,
            withTitle: nil,
            message: title,
            cancelButtonTitle: cancelButton != nil ? AALocalized(cancelButton!) : nil,
            destructiveButtonTitle: destructButton != nil ? AALocalized(destructButton!) : nil,
            otherButtonTitles: convertedButtons,
            popoverPresentationControllerBlock: { (popover: RMPopoverPresentationController) -> Void in
                popover.sourceView = sourceView
                popover.sourceRect = sourceRect
            },
            tapBlock: { (alert, buttonIndex) -> Void in
                if (buttonIndex == alert.cancelButtonIndex) {
                    tapClosure(index: -1)
                } else if (buttonIndex == alert.destructiveButtonIndex) {
                    tapClosure(index: -2)
                } else if (buttonIndex >= alert.firstOtherButtonIndex) {
                    tapClosure(index: buttonIndex - alert.firstOtherButtonIndex)
                }
        })
        
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

