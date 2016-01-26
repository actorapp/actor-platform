//
//  Copyright (c) 2014-2016 Actor LLC. <https://actor.im>
//

import Foundation

public enum AAContentTableStyle {
    
    case SettingsPlain
    case SettingsGrouped
    case Plain
}

public class AAContentTableController: AAManagedTableController, AAManagedTableControllerDelegate {
    
    private var isInLoad: Bool = false
    
    public var autoSections = true
    
    // Controller constructor
    
    public override init(style: AAContentTableStyle) {
        super.init(style: style)
        
        self.managedTableDelegate = self
        
        self.autoSections = style != .Plain
    }

    public required init(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    // DSL Implementation
    
    public func section(closure: (s: AAManagedSection) -> ()) {
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
    
    public func search<C where C: AABindedSearchCell, C: UITableViewCell>(cell: C.Type, @noescape closure: (s: AAManagedSearchConfig<C>) -> ()) {
        managedTable.search(cell, closure: closure)
    }
    
    public func afterTableCreated() {
        if autoSections {
            managedTable.sections.last?.footerHeight = 30
        }
    }
    
    // Implement it in subclass
    
    public func tableDidLoad() {
        
    }
    
    public func tableWillBind(binder: AABinder) {
        
    }
    
    public func tableWillUnbind(binder: AABinder) {
        
    }
    
    // Delegate implementation
    
    public func managedTableLoad(controller: AAManagedTableController, table: AAManagedTable) {
        isInLoad = true
        table.beginUpdates()
        tableDidLoad()
        table.endUpdates()
        afterTableCreated()
        isInLoad = false
    }
    
    public func managedTableBind(controller: AAManagedTableController, table: AAManagedTable, binder: AABinder) {
        tableWillBind(binder)
    }
    
    public func managedTableUnbind(controller: AAManagedTableController, table: AAManagedTable, binder: AABinder) {
        tableWillUnbind(binder)
    }
}