//
//  Copyright (c) 2014-2015 Actor LLC. <https://actor.im>
//

import Foundation


class ACContentTableController: ACManagedTableController, ACManagedTableControllerDelegate {
    
    private var isInLoad: Bool = false
    
    var autoSections = true
    
    // Controller constructor
    
    override init(tableViewStyle: UITableViewStyle) {
        super.init(tableViewStyle: tableViewStyle)
        
        self.delegate = self
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
    
    func afterTableCreated() {
        if autoSections {
            managedTable.sections.last?.footerHeight = 30
        }
    }
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        navigationItem.backBarButtonItem = UIBarButtonItem(title: nil, style: UIBarButtonItemStyle.Plain, target: nil, action: nil)
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
        tableDidLoad()
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