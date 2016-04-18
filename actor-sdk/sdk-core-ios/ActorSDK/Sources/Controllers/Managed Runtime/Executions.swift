//
//  Copyright (c) 2014-2016 Actor LLC. <https://actor.im>
//

import Foundation
import MBProgressHUD

public enum AAExecutionType {
    case Normal
    case Hidden
    case Safe
}

public class AAMenuBuilder {
    
    public var tapClosure: ((index: Int) -> ())!
    public var items = [String]()
    public var closures: [(()->())?] = []
    
    public init() {
        
        tapClosure = { (index) -> () in
            if index >= 0 && index <= self.closures.count {
                self.closures[index]?()
            }
        }
    }
    
    public func add(title: String, closure: (()->())?) {
        items.append(title)
        closures.append(closure)
    }
}

public class AAExecutions {
    
    public class func execute(promise: ARPromise) {
        executePromise(promise)
    }
    
    public class func execute(command: ACCommand) {
        execute(command, successBlock: nil, failureBlock: nil)
    }
    
    public class func executePromise(promice: ARPromise){
        promice.startUserAction()
    }
    
    public class func executePromise(promice: ARPromise, successBlock: ((val: Any?) -> Void)?, failureBlock: ((val: Any?) -> Void)? ){
        promice.startUserAction()
        promice.then { result in
            successBlock!(val: result)
        }
    }
    
    public class func execute(command: ACCommand, type: AAExecutionType = .Normal, ignore: [String] = [], successBlock: ((val: Any?) -> Void)?, failureBlock: ((val: Any?) -> Void)?) {
        var hud: MBProgressHUD?
        if type != .Hidden {
            hud = showProgress()
        }
        
        command.startWithCallback(AACommandCallback(result: { (val:Any?) -> () in
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
                            if ignore.contains(exception.tag) {
                                failureBlock?(val: val)
                                return
                            }
                            
                            // Get error from
                            tryAgain = exception.canTryAgain
                        }
                        
                        // Showing alert
                        if tryAgain {
                            errorWithError(val, rep: { () -> () in
                                AAExecutions.execute(command, type: type, successBlock: successBlock, failureBlock: failureBlock)
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
    
    public class func errorWithError(e: AnyObject, rep:(()->())? = nil, cancel:(()->())? = nil) {
        error(Actor.getFormatter().formatErrorTextWithError(e), rep: rep, cancel: cancel)
    }
    
    public class func errorWithTag(tag: String, rep:(()->())? = nil, cancel:(()->())? = nil) {
        error(Actor.getFormatter().formatErrorTextWithTag(tag), rep: rep, cancel: cancel)
    }
    
    public class func error(message: String, rep:(()->())? = nil, cancel:(()->())? = nil) {
        if rep != nil {
            let d = UIAlertViewBlock(clickedClosure: { (index) -> () in
                if index > 0 {
                    rep?()
                } else {
                    cancel?()
                }
            })
            let alert = UIAlertView(title: AALocalized("AlertError"),
                message: message,
                delegate: d,
                cancelButtonTitle: AALocalized("AlertCancel"),
                otherButtonTitles: AALocalized("AlertTryAgain"))
            setAssociatedObject(alert, value: d, associativeKey: &alertViewBlockReference)
            alert.show()
        } else {
            let d = UIAlertViewBlock(clickedClosure: { (index) -> () in
                cancel?()
            })
            let alert = UIAlertView(title: nil,
                message: message,
                delegate: d,
                cancelButtonTitle: AALocalized("AlertOk"))
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

public extension UIViewController {
    
    public func execute(command: ACCommand) {
        AAExecutions.execute(command)
    }
    
    public func executePromise(promise: ARPromise) {
        AAExecutions.execute(promise)
    }
    
    public func executePromise(promise: ARPromise, successBlock: ((val: Any?) -> Void)?, failureBlock: ((val: Any?) -> Void)?) {
        AAExecutions.executePromise(promise, successBlock: successBlock, failureBlock: failureBlock)
    }

    public func execute(command: ACCommand, successBlock: ((val: Any?) -> Void)?, failureBlock: ((val: Any?) -> Void)?) {
        AAExecutions.execute(command, successBlock: successBlock, failureBlock: failureBlock)
    }
    
    public func execute(command: ACCommand, successBlock: ((val: Any?) -> Void)?) {
        AAExecutions.execute(command, successBlock: successBlock, failureBlock: nil)
    }
    
    public func executeSafe(command: ACCommand, ignore: [String] = [], successBlock: ((val: Any?) -> Void)? = nil) {
        AAExecutions.execute(command, type: .Safe, ignore: ignore, successBlock: successBlock, failureBlock: { (val) -> () in
            successBlock?(val: nil)
        })
    }
    
    public func executeSafeOnlySuccess(command: ACCommand, successBlock: ((val: Any?) -> Void)?) {
        AAExecutions.execute(command, type: .Safe, ignore: [], successBlock: successBlock, failureBlock: nil)
    }
    
    public func executeHidden(command: ACCommand, successBlock: ((val: Any?) -> Void)? = nil) {
        AAExecutions.execute(command, type: .Hidden, successBlock: successBlock, failureBlock: nil)
    }
}