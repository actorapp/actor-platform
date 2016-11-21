//
//  Copyright (c) 2014-2016 Actor LLC. <https://actor.im>
//

import Foundation
import MBProgressHUD

public enum AAExecutionType {
    case normal
    case hidden
    case safe
}

open class AAMenuBuilder {
    
    open var tapClosure: ((_ index: Int) -> ())!
    open var items = [String]()
    open var closures: [(()->())?] = []
    
    public init() {
        
        tapClosure = { (index) -> () in
            if index >= 0 && index <= self.closures.count {
                self.closures[index]?()
            }
        }
    }
    
    open func add(_ title: String, closure: (()->())?) {
        items.append(title)
        closures.append(closure)
    }
}

open class AAExecutions {
    
    open class func execute(_ promise: ARPromise) {
        executePromise(promise)
    }
    
    open class func execute(_ command: ACCommand) {
        execute(command, successBlock: nil, failureBlock: nil)
    }
    
    open class func executePromise(_ promice: ARPromise){
        promice.startUserAction()
    }
    
    open class func executePromise(_ promice: ARPromise, successBlock: ((_ val: Any?) -> Void)?, failureBlock: ((_ val: Any?) -> Void)? ){
        promice.startUserAction()
        promice.then { result in
            successBlock!(result)
        }
    }
    
    open class func execute(_ command: ACCommand, type: AAExecutionType = .normal, ignore: [String] = [], successBlock: ((_ val: Any?) -> Void)?, failureBlock: ((_ val: Any?) -> Void)?) {
        var hud: MBProgressHUD?
        if type != .hidden {
            hud = showProgress()
        }
        
        command.start(with: AACommandCallback(result: { (val:Any?) -> () in
            dispatchOnUi {
                hud?.hide(true)
                successBlock?(val)
            }
            }, error: { (val) -> () in
                dispatchOnUi {
                    hud?.hide(true)
                    
                    if type == .safe {
                        
                        // If unknown error, just try again
                        var tryAgain = true
                        if let exception = val as? ACRpcException {
                            
                            // If is in ignore list, just return to UI
                            if ignore.contains(exception.tag) {
                                failureBlock?(val)
                                return
                            }
                            
                            // Get error from
                            tryAgain = exception.canTryAgain
                        }
                        
                        // Showing alert
                        if tryAgain {
                            errorWithError(val!, rep: { () -> () in
                                AAExecutions.execute(command, type: type, successBlock: successBlock, failureBlock: failureBlock)
                            }, cancel: { () -> () in
                                failureBlock?(val)
                            })
                        } else {
                            errorWithError(val!, cancel: { () -> () in
                                failureBlock?(val)
                            })
                        }
                    } else {
                        failureBlock?(val)
                    }
                }
        }))
    }
    
    open class func errorWithError(_ e: AnyObject, rep:(()->())? = nil, cancel:(()->())? = nil) {
        error(Actor.getFormatter().formatErrorTextWithError(e), rep: rep, cancel: cancel)
    }
    
    open class func errorWithTag(_ tag: String, rep:(()->())? = nil, cancel:(()->())? = nil) {
        error(Actor.getFormatter().formatErrorText(withTag: tag), rep: rep, cancel: cancel)
    }
    
    open class func error(_ message: String, rep:(()->())? = nil, cancel:(()->())? = nil) {
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

    class fileprivate func showProgress() -> MBProgressHUD {
        let window = UIApplication.shared.windows[1]
        let hud = MBProgressHUD(window: window)
        hud.mode = MBProgressHUDMode.indeterminate
        hud.removeFromSuperViewOnHide = true
        window.addSubview(hud)
        window.bringSubview(toFront: hud)
        hud.show(true)
        return hud
    }
}

private var alertViewBlockReference = "_block_reference"

@objc private class UIAlertViewBlock: NSObject, UIAlertViewDelegate {
    
    fileprivate let clickedClosure: ((_ index: Int) -> ())
    
    init(clickedClosure: @escaping ((_ index: Int) -> ())) {
        self.clickedClosure = clickedClosure
    }
    
    @objc fileprivate func alertView(_ alertView: UIAlertView, clickedButtonAt buttonIndex: Int) {
        clickedClosure(buttonIndex)
    }
    
    @objc fileprivate func alertViewCancel(_ alertView: UIAlertView) {
        clickedClosure(-1)
    }
}

public extension UIViewController {
    
    public func execute(_ command: ACCommand) {
        AAExecutions.execute(command)
    }
    
    public func executePromise(_ promise: ARPromise) -> ARPromise {
        AAExecutions.execute(promise)
        return promise
    }
    
    public func executePromise(_ promise: ARPromise, successBlock: ((_ val: Any?) -> Void)?, failureBlock: ((_ val: Any?) -> Void)?) {
        AAExecutions.executePromise(promise, successBlock: successBlock, failureBlock: failureBlock)
    }

    public func execute(_ command: ACCommand, successBlock: ((_ val: Any?) -> Void)?, failureBlock: ((_ val: Any?) -> Void)?) {
        AAExecutions.execute(command, successBlock: successBlock, failureBlock: failureBlock)
    }
    
    public func execute(_ command: ACCommand, successBlock: ((_ val: Any?) -> Void)?) {
        AAExecutions.execute(command, successBlock: successBlock, failureBlock: nil)
    }
    
    public func executeSafe(_ command: ACCommand, ignore: [String] = [], successBlock: ((_ val: Any?) -> Void)? = nil) {
        AAExecutions.execute(command, type: .safe, ignore: ignore, successBlock: successBlock, failureBlock: { (val) -> () in
            successBlock?(nil)
        })
    }
    
    public func executeSafeOnlySuccess(_ command: ACCommand, successBlock: ((_ val: Any?) -> Void)?) {
        AAExecutions.execute(command, type: .safe, ignore: [], successBlock: successBlock, failureBlock: nil)
    }
    
    public func executeHidden(_ command: ACCommand, successBlock: ((_ val: Any?) -> Void)? = nil) {
        AAExecutions.execute(command, type: .hidden, successBlock: successBlock, failureBlock: nil)
    }
}
