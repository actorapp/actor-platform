//
//  Copyright (c) 2014-2016 Actor LLC. <https://actor.im>
//

import Foundation

// Shorter helper for localized strings

public func AALocalized(_ text: String) -> String {
    
    let appRes = NSLocalizedString(text, comment: "")
    
    if (appRes != text) {
        return appRes
    }
    
    for t in tables {
        let res = NSLocalizedString(text, tableName: t.table, bundle: t.bundle, value: text, comment: "")
        if (res != text) {
            return res
        }
    }
    
    return NSLocalizedString(text, tableName: nil, bundle: Bundle.framework, value: text, comment: "")
}

// Registration localization table

public func AARegisterLocalizedBundle(_ table: String, bundle: Bundle) {
    tables.append(LocTable(table: table, bundle: bundle))
}

private var tables = [LocTable]()

private class LocTable {
    
    let table: String
    let bundle: Bundle
    
    init(table: String, bundle: Bundle) {
        self.table = table
        self.bundle = bundle
    }
}

// Extensions on various UIView

public extension UILabel {
    
    public var textLocalized: String? {
        
        get {
            return self.text
        }
        
        set (value) {
            if value != nil {
                self.text = AALocalized(value!).replace("{appname}", dest: ActorSDK.sharedActor().appName)
            } else {
                self.text = nil
            }
        }
    }
}
