//
//  Copyright (c) 2014-2015 Actor LLC. <https://actor.im>
//

import Foundation

// Shorter helper for localized strings

public func localized(text: String!) -> String! {
    if text == nil {
        return nil
    }
    
    for t in tables {
        let res = NSLocalizedString(text, tableName: t.table, bundle: t.bundle, value: text, comment: "")
        if (res != text) {
            return res
        }
    }
    
    return NSLocalizedString(text, comment: "")
}

// Registration localization table

public func registerLocalizationTable(table: String, bundle: NSBundle) {
    tables.append(LocTable(table: table, bundle: bundle))
}

private var tables = [LocTable]()

private class LocTable {
    
    let table: String
    let bundle: NSBundle
    
    init(table: String, bundle: NSBundle) {
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
                self.text = localized(value!)
            } else {
                self.text = nil
            }
        }
    }
}