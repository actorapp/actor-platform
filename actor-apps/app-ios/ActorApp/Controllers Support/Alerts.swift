//
//  Copyright (c) 2014-2015 Actor LLC. <https://actor.im>
//

import Foundation

private var pickDocumentClosure = "_pick_document_closure"
private var actionShitReference = "_action_shit"

extension UIViewController {
    
    func alertUser(message: String) {
        RMUniversalAlert.showAlertInViewController(self,
            withTitle: nil,
            message: NSLocalizedString(message, comment: "Message"),
            cancelButtonTitle: NSLocalizedString("AlertOk", comment: "Ok"),
            destructiveButtonTitle: nil,
            otherButtonTitles: nil,
            tapBlock: nil)
    }
    
    func confirmDangerSheetUser(action: String, tapYes: ()->(), tapNo: (()->())?) {
        showActionSheet(nil, buttons: [], cancelButton: "AlertCancel", destructButton: action, sourceView: UIView(), sourceRect: CGRectZero) { (index) -> () in
            if index == -2 {
                tapYes()
            } else {
                tapNo?()
            }
        }
    }
    
    func confirmAlertUser(message: String, action: String, tapYes: ()->(), tapNo: (()->())? = nil) {
        RMUniversalAlert.showAlertInViewController(self,
            withTitle: nil,
            message: NSLocalizedString(message, comment: "Message"),
            cancelButtonTitle: NSLocalizedString("AlertCancel", comment: "Cancel"),
            destructiveButtonTitle: nil,
            otherButtonTitles: [NSLocalizedString(action, comment: "Cancel")],
            tapBlock: { (alert, buttonIndex) -> Void in
                if (buttonIndex >= alert.firstOtherButtonIndex) {
                    tapYes()
                } else {
                    tapNo?()
                }
        })
    }
    
    func textInputAlert(message: String, content: String?, action:String, tapYes: (nval: String)->()) {
        let alertView = UIAlertView(
            title: nil,
            message: NSLocalizedString(message, comment: "Title"),
            delegate: self,
            cancelButtonTitle: NSLocalizedString("AlertCancel", comment: "Cancel Title"))
        alertView.addButtonWithTitle(NSLocalizedString(action, comment: "Action Title"))
        alertView.alertViewStyle = UIAlertViewStyle.PlainTextInput
        alertView.textFieldAtIndex(0)!.autocapitalizationType = UITextAutocapitalizationType.Words
        alertView.textFieldAtIndex(0)!.text = content
        alertView.textFieldAtIndex(0)!.keyboardAppearance = MainAppTheme.common.isDarkKeyboard ? UIKeyboardAppearance.Dark : UIKeyboardAppearance.Light
        alertView.tapBlock = { (alert: UIAlertView, buttonIndex) -> () in
            if (buttonIndex != alert.cancelButtonIndex) {
                tapYes(nval: alert.textFieldAtIndex(0)!.text!)
            }
        }
        alertView.show()
    }
    
    func confirmUser(message: String, action: String, cancel: String, sourceView: UIView, sourceRect: CGRect, tapYes: ()->()) {
        RMUniversalAlert.showActionSheetInViewController(
            self,
            withTitle: nil,
            message: NSLocalizedString(message, comment: "Message"),
            cancelButtonTitle: NSLocalizedString(cancel, comment: "Cancel Title"),
            destructiveButtonTitle: NSLocalizedString(action, comment: "Destruct Title"),
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
    
    func showActionSheet(title: String?, buttons: [String], cancelButton: String?, destructButton: String?, sourceView: UIView, sourceRect: CGRect, tapClosure: (index: Int) -> ()) {
        var convertedButtons:[String] = [String]()
        for b in buttons {
            convertedButtons.append(NSLocalizedString(b, comment: "Button Title"))
        }
        
        RMUniversalAlert.showActionSheetInViewController(
            self,
            withTitle: nil,
            message: title,
            cancelButtonTitle: cancelButton != nil ? NSLocalizedString(cancelButton!, comment: "Cancel") : nil,
            destructiveButtonTitle: destructButton != nil ? NSLocalizedString(destructButton!, comment: "Destruct") : nil,
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
    
    func showActionSheetFast(buttons: [String], cancelButton: String, tapClosure: (index: Int) -> ()) {
        let actionShit = ABActionShit()
        
        var convertedButtons:[String] = [String]()
        for b in buttons {
            convertedButtons.append(NSLocalizedString(b, comment: "Button Title"))
        }
        
        actionShit.buttonTitles = convertedButtons
        actionShit.cancelButtonTitle = NSLocalizedString(cancelButton,comment: "Cancel")
        let shitDelegate = ActionShitDelegate(tapClosure: tapClosure)
        actionShit.delegate = shitDelegate
        
        // Convert from weak to strong reference
        setAssociatedObject(actionShit, value: shitDelegate, associativeKey: &actionShitReference, policy: objc_AssociationPolicy.OBJC_ASSOCIATION_RETAIN_NONATOMIC)
        
        actionShit.showWithCompletion(nil)
    }
    
    func startEditText(closure: (EditTextControllerConfig) -> ()) {
        let config = EditTextControllerConfig()
        closure(config)
        config.check()
        self.presentViewController(AANavigationController(rootViewController: EditTextController(config: config)), animated: true, completion: nil)
    }
    
    func startEditField(closure: (c: EditFieldControllerConfig) -> ()) {
        let config = EditFieldControllerConfig()
        closure(c: config)
        config.check()
        self.presentViewController(AANavigationController(rootViewController: EditFieldController(config: config)), animated: true, completion: nil)
    }
}

class ActionShitDelegate: NSObject, ABActionShitDelegate {
    
    let tapClosure: (index: Int) -> ()
    
    init (tapClosure: (index: Int) -> ()) {
        self.tapClosure = tapClosure
    }
    
    func actionShit(actionShit: ABActionShit!, clickedButtonAtIndex buttonIndex: Int) {
        tapClosure(index: buttonIndex)
    }
    
    func actionShitClickedCancelButton(actionShit: ABActionShit!) {
        tapClosure(index: -1)
    }
}


