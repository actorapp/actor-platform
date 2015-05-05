//
//  Copyright (c) 2015 Actor LLC. <https://actor.im>
//

import Foundation
class CocoaLocale : NSObject, AMLocaleProvider {

    func loadLocale() -> JavaUtilHashMap! {
        var res = JavaUtilHashMap()
        
        var currentLocale = NSLocalizedString("CurrentLocale",comment: "Current Locale Value")
        var textName = "AppText"
        var monthsName = "Months"
        
        if (currentLocale != "En") {
            textName = "AppText_"+currentLocale
            monthsName = "Months_"+currentLocale
        }
        
        var error:NSError?;
        var text = NSString(contentsOfFile: NSBundle.mainBundle().pathForResource(textName, ofType: "properties")!, encoding: NSUTF8StringEncoding, error: &error)!
        
        for line in text.componentsSeparatedByString("\n") {
            var tokens = line.componentsSeparatedByString("=")
            if (tokens.count == 2) {
                res.putWithId(tokens[0], withId: tokens[1])
            }
        }
        
        text = NSString(contentsOfFile: NSBundle.mainBundle().pathForResource(monthsName, ofType: "properties")!, encoding: NSUTF8StringEncoding, error: &error)!
        
        for line in text.componentsSeparatedByString("\n") {
            var tokens = line.componentsSeparatedByString("=")
            if (tokens.count == 2) {
                res.putWithId(tokens[0], withId: tokens[1])
            }
        }
        
        return res
    }
    
    func is24Hours() -> Bool {
        return true
    }
}