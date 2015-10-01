//
//  Copyright (c) 2014-2015 Actor LLC. <https://actor.im>
//

import Foundation

enum ACContentTableStyle {
    
    case SettingsPlain
    case SettingsGrouped
    case Plain
}

class ACContentTableController: ACManagedTableController, ACManagedTableControllerDelegate {
    
    private var isInLoad: Bool = false
    
    var autoSections = true
    
    // Controller constructor
    
    override init(style: ACContentTableStyle) {
        super.init(style: style)
        
        self.managedTableDelegate = self
        
        self.autoSections = style != .Plain
    }

    required init(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    // DSL Implementation
    
    func section(closure: (s: ACManagedSection) -> ()) {
        if !isInLoad {
            fatalError("Unable to change sections not during tableDidLoad method call")
        }
        
        let isFirst = managedTable.sections.count == 0
        let s = managedTable.addSection(true)
        if autoSections {
            s.headerHeight = 15
            s.footerHeight = 15
            if isFirst {
                s.headerHeight = 0
            }
        }
        closure(s: s)
    }
    
    func search<C where C: ACBindedSearchCell, C: UITableViewCell>(cell: C.Type, closure: (s: ACManagedSearchConfig<C>) -> ()) {
        managedTable.search(cell, closure: closure)
    }
    
    func afterTableCreated() {
        if autoSections {
            managedTable.sections.last?.footerHeight = 30
        }
    }
    
    // Implement it in subclass
    
    func tableDidLoad() {
        
    }
    
    func tableWillBind(binder: Binder) {
        
    }
    
    func tableWillUnbind(binder: Binder) {
        
    }
    
    // Delegate implementation
    
    func managedTableLoad(controller: ACManagedTableController, table: ACManagedTable) {
        isInLoad = true
        table.beginUpdates()
        tableDidLoad()
        table.endUpdates()
        afterTableCreated()
        isInLoad = false
    }
    
    func managedTableBind(controller: ACManagedTableController, table: ACManagedTable, binder: Binder) {
        tableWillBind(binder)
    }
    
    func managedTableUnbind(controller: ACManagedTableController, table: ACManagedTable, binder: Binder) {
        tableWillUnbind(binder)
    }
}