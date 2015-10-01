//
//  Copyright (c) 2014-2015 Actor LLC. <https://actor.im>
//

import Foundation

// Shorter helper for localized strings
func localized(text: String?) -> String! {
    if text == nil {
        return nil
    }
    
    return NSLocalizedString(text!, comment: "")
}

// Extensions on various UIView
extension UILabel {
    
    var textLocalized: String? {
        
        get {
            return self.text
        }
        
        set (value) {
            if value != nil {
                self.text = localized(value!)
            } else {
                self.text = nil
            }
        }
    }
}