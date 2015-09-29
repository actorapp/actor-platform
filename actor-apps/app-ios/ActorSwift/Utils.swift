//
//  Copyright (c) 2014-2015 Actor LLC. <https://actor.im>
//

import UIKit

func dispatchOnUi(closure: () -> Void) {
    dispatch_async(dispatch_get_main_queue(), { () -> Void in
        // NSLog("dispatchOnUi")
        closure()
    })
}

func dispatchBackground(closure: () -> Void) {
    dispatch_async(dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_BACKGROUND, 0), { () -> Void in
        closure()
    })
}

class Utils: NSObject {
    
    class func isRetina() -> Bool {
        return UIScreen.mainScreen().scale > 1
    }
    
    class func retinaPixel() -> CGFloat {
        if Utils.isRetina() {
            return 0.5
        }
        return 1.0
    }
    
}

extension Array {
    func contains<T where T : Equatable>(obj: T) -> Bool {
        return self.filter({$0 as? T == obj}).count > 0
    }
}

extension UIViewController {
    
    func getNavigationBarHeight() -> CGFloat {
        if (navigationController != nil) {
            return navigationController!.navigationBar.frame.height
        } else {
            return CGFloat(44)
        }
    }
    
    func getStatusBarHeight() -> CGFloat {
        let statusBarSize = UIApplication.sharedApplication().statusBarFrame.size
        return min(statusBarSize.width, statusBarSize.height)
    }
}


extension NSTimeInterval {
    var time:String {
        return String(format:"%02d:%02d:%02d.%03d", Int((self/3600.0)%60),Int((self/60.0)%60), Int((self) % 60 ), Int(self*1000 % 1000 ) )
    }
}

func log(text:String) {
    NSLog(text)
}

typealias cancellable_closure = (() -> ())?

func dispatch_after(seconds:Double, queue: dispatch_queue_t = dispatch_get_main_queue(), closure:()->()) -> cancellable_closure {
    var cancelled = false
    let cancel_closure: cancellable_closure = {
        cancelled = true
    }
    
    dispatch_after(
        dispatch_time(DISPATCH_TIME_NOW, Int64(seconds * Double(NSEC_PER_SEC))), queue, {
            if !cancelled {
                closure()
            }
        }
    )
    
    return cancel_closure
}

func cancel_dispatch_after(cancel_closure: cancellable_closure) {
    cancel_closure?()
}

class ObjectPool<T: AnyObject> {
    
    private var objects = NSMutableArray()
    
    func put(item: T) {
        objects.addObject(item)
    }
    
    func get() -> T? {
        if objects.count > 0 {
            let res = objects.objectAtIndex(objects.count - 1) as! T
            objects.removeLastObject()
            return res
        }
        
        return nil
    }
    
    func acquire(var items: [T]) {
        for cached in items {
            put(cached)
        }
        items.removeAll(keepCapacity: true)
    }
}

class Regex {
    let internalExpression: NSRegularExpression
    let pattern: String
    
    init(_ pattern: String) {
        self.pattern = pattern
        do {
            self.internalExpression = try NSRegularExpression(pattern: pattern, options: .CaseInsensitive)
        } catch {
            fatalError("Incorrect regex: \(pattern)")
        }
    }
    
    func test(input: String) -> Bool {
        let matches = self.internalExpression.matchesInString(input, options: NSMatchingOptions(), range:NSMakeRange(0, input.length))
        return matches.count > 0
    }
}
