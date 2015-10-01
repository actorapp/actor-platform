//
//  Copyright (c) 2014-2015 Actor LLC. <https://actor.im>
//

import Foundation

enum ExecutionType {
    case Normal
    case Hidden
    case Safe
}

class MenuBuilder {
    
    var tapClosure: ((index: Int) -> ())!
    var items = [String]()
    var closures: [(()->())?] = []
    
    init() {
        
        tapClosure = { (index) -> () in
            if index >= 0 && index <= self.closures.count {
                self.closures[index]?()
            }
        }
    }
    
    func add(title: String, closure: (()->())?) {
        items.append(title)
        closures.append(closure)
    }
}

class Executions {
    
    class func execute(command: ACCommand) {
        execute(command, successBlock: nil, failureBlock: nil)
    }
    
    class func execute(command: ACCommand, type: ExecutionType = .Normal, ignore: [String] = [], successBlock: ((val: Any?) -> Void)?, failureBlock: ((val: Any?) -> Void)?) {
        
        var hud: MBProgressHUD?
        if type != .Hidden {
            hud = showProgress()
        }
        
        command.startWithCallback(CocoaCallback(result: { (val:Any?) -> () in
            dispatchOnUi {
                hud?.hide(true)
                successBlock?(val: val)
            }
            }, error: { (val) -> () in
                dispatchOnUi {
                    hud?.hide(true)
                    
                    if type == .Safe {
                        
                        // If unknown error, just try again
                        var tryAgain = true
                        if let exception = val as? ACRpcException {
                            
                            // If is in ignore list, just return to UI
                            if ignore.contains(exception.getTag()) {
                                failureBlock?(val: val)
                                return
                            }
                            
                            // Get error from
                            tryAgain = exception.isCanTryAgain()
                        }
                        
                        // Showing alert
                        if tryAgain {
                            errorWithError(val, rep: { () -> () in
                                Executions.execute(command, type: type, successBlock: successBlock, failureBlock: failureBlock)
                            }, cancel: { () -> () in
                                failureBlock?(val: val)
                            })
                        } else {
                            errorWithError(val, cancel: { () -> () in
                                failureBlock?(val: val)
                            })
                        }
                    } else {
                        failureBlock?(val: val)
                    }
                }
        }))
    }
    
    class func errorWithError(e: AnyObject, rep:(()->())? = nil, cancel:(()->())? = nil) {
        error(Actor.getFormatter().formatErrorTextWithError(e), rep: rep, cancel: cancel)
    }
    
    class func errorWithTag(tag: String, rep:(()->())? = nil, cancel:(()->())? = nil) {
        error(Actor.getFormatter().formatErrorTextWithTag(tag), rep: rep, cancel: cancel)
    }
    
    class func error(message: String, rep:(()->())? = nil, cancel:(()->())? = nil) {
        if rep != nil {
            let d = UIAlertViewBlock(clickedClosure: { (index) -> () in
                if index > 0 {
                    rep?()
                } else {
                    cancel?()
                }
            })
            let alert = UIAlertView(title: localized("AlertError"),
                message: message,
                delegate: d,
                cancelButtonTitle: localized("AlertCancel"),
                otherButtonTitles: localized("AlertTryAgain"))
            setAssociatedObject(alert, value: d, associativeKey: &alertViewBlockReference)
            alert.show()
        } else {
            let d = UIAlertViewBlock(clickedClosure: { (index) -> () in
                cancel?()
            })
            let alert = UIAlertView(title: nil,
                message: message,
                delegate: d,
                cancelButtonTitle: localized("AlertOk"))
            setAssociatedObject(alert, value: d, associativeKey: &alertViewBlockReference)
            alert.show()
        }
    }

    class private func showProgress() -> MBProgressHUD {
        let window = UIApplication.sharedApplication().windows[1]
        let hud = MBProgressHUD(window: window)
        hud.mode = MBProgressHUDMode.Indeterminate
        hud.removeFromSuperViewOnHide = true
        window.addSubview(hud)
        window.bringSubviewToFront(hud)
        hud.show(true)
        return hud
    }
}

private var alertViewBlockReference = "_block_reference"

@objc private class UIAlertViewBlock: NSObject, UIAlertViewDelegate {
    
    private let clickedClosure: ((index: Int) -> ())
    
    init(clickedClosure: ((index: Int) -> ())) {
        self.clickedClosure = clickedClosure
    }
    
    @objc private func alertView(alertView: UIAlertView, clickedButtonAtIndex buttonIndex: Int) {
        clickedClosure(index: buttonIndex)
    }
    
    @objc private func alertViewCancel(alertView: UIAlertView) {
        clickedClosure(index: -1)
    }
}

extension UIViewController {
    
    func execute(command: ACCommand) {
        Executions.execute(command)
    }
    
    func execute(command: ACCommand, successBlock: ((val: Any?) -> Void)?, failureBlock: ((val: Any?) -> Void)?) {
        Executions.execute(command, successBlock: successBlock, failureBlock: failureBlock)
    }
    
    func execute(command: ACCommand, successBlock: ((val: Any?) -> Void)?) {
        Executions.execute(command, successBlock: successBlock, failureBlock: nil)
    }
    
    func executeSafe(command: ACCommand, ignore: [String] = [], successBlock: ((val: Any?) -> Void)? = nil) {
        Executions.execute(command, type: .Safe, ignore: ignore, successBlock: successBlock, failureBlock: { (val) -> () in
            successBlock?(val: nil)
        })
    }
    
    func executeSafeOnlySuccess(command: ACCommand, successBlock: ((val: Any?) -> Void)?) {
        Executions.execute(command, type: .Safe, ignore: [], successBlock: successBlock, failureBlock: nil)
    }

    
    func executeHidden(command: ACCommand, successBlock: ((val: Any?) -> Void)? = nil) {
        Executions.execute(command, type: .Hidden, successBlock: successBlock, failureBlock: nil)
    }
}