//
//  Copyright (c) 2014-2015 Actor LLC. <https://actor.im>
//

import Foundation

extension UIViewController {
    
    func execute(command: ACCommand) {
        (UIApplication.sharedApplication().delegate as! AppDelegate).execute(command)
    }
    
    func execute(command: ACCommand, successBlock: ((val: Any?) -> Void)?, failureBlock: ((val: Any?) -> Void)?) {
        (UIApplication.sharedApplication().delegate as! AppDelegate).execute(command, successBlock: successBlock, failureBlock: failureBlock)
    }    
}