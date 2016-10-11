//
//  Copyright (c) 2014-2016 Actor LLC. <https://actor.im>
//

import Foundation

open class AAManagedTableController: AAViewController {
    
    open let style: AAContentTableStyle
    
    open var managedTableDelegate: AAManagedTableControllerDelegate?
    
    open let binder = AABinder()
    
    open var tableView: UITableView!
    
    open var managedTable: AAManagedTable!
    
    open var unbindOnDissapear: Bool = false
    
    fileprivate var isBinded: Bool = false
    
    public init(style: AAContentTableStyle) {
        self.style = style
        super.init()
    }

    public required init(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    open override func viewDidLoad() {
        super.viewDidLoad()
        
        // Creating tables
        
        let tableViewStyle: UITableViewStyle
        switch(style) {
        case .plain:
            tableViewStyle = .plain
            break
        case .settingsPlain:
            tableViewStyle = .plain
            break
        case .settingsGrouped:
            tableViewStyle = .grouped
            break
        }
        
        tableView = UITableView(frame: view.bounds, style: tableViewStyle)
        
        // Disabling separators as we use manual separators handling
        tableView.separatorStyle = .none
        
        // Setting tableView and view bg color depends on table style
        tableView.backgroundColor = style == .plain ? appStyle.vcBgColor : appStyle.vcBackyardColor
        view.backgroundColor = tableView.backgroundColor
        
        // Useful for making table view with fixed row height
        if let d = managedTableDelegate {
            d.managedTableWillLoad(self)
        }
        
        managedTable = AAManagedTable(style: style, tableView: tableView, controller: self)
        view.addSubview(tableView)
        
        // Invoking table loading
        
        if let d = managedTableDelegate {
            d.managedTableLoad(self, table: managedTable)
        }
        
        // Initial load of Managed Table
        tableView.reloadData()
    }
    
    open override func viewWillAppear(_ animated: Bool) {

        if let t = tableView {
            if let row = t.indexPathForSelectedRow {
                t.deselectRow(at: row, animated: animated)
            }
        }
        
        super.viewWillAppear(animated)
        
        if let m = managedTable {
            
            // Performing data binding
            if let d = managedTableDelegate {
                d.managedTableBind(self, table: m, binder: binder)
            }
            
            if !isBinded {
                // Binding rows
                m.bind(binder)
                isBinded = true
            }
            
            // Passing event to table
            m.controllerViewWillAppear(animated)
            
            // Reloading data
            tableView.reloadData()
        }
    }
    
    open override func viewWillDisappear(_ animated: Bool) {
        super.viewWillDisappear(animated)
        
        // Stopping data binding here
        if let m = managedTable {
            if let d = managedTableDelegate {
                d.managedTableUnbind(self, table: m, binder: binder)
            }
            
            if unbindOnDissapear {
                
                if isBinded {
                    // Unbinding rows
                    m.unbind(binder)
                    isBinded = false
                }
                
                // Removing all bindings
                binder.unbindAll()
            }
            
            // Passing event to table
            m.controllerViewWillDisappear(animated)
        }
    }
    
    open override func viewDidDisappear(_ animated: Bool) {
        super.viewDidDisappear(animated)
        
        if let m = managedTable {
            
            // Passing event to table
            m.controllerViewDidDisappear(animated)
        }
    }
    
    open override func viewWillLayoutSubviews() {
        super.viewWillLayoutSubviews()
        
        tableView.frame = view.bounds
    }
}

public protocol AAManagedTableControllerDelegate {
    func managedTableWillLoad(_ controller: AAManagedTableController)
    func managedTableLoad(_ controller: AAManagedTableController, table: AAManagedTable)
    func managedTableBind(_ controller: AAManagedTableController, table: AAManagedTable, binder: AABinder)
    func managedTableUnbind(_ controller: AAManagedTableController, table: AAManagedTable, binder: AABinder)
}

public extension AAManagedTableControllerDelegate {
    
    public func managedTableLoad(_ controller: AAManagedTableController, table: AAManagedTable) {
        // Do nothing
    }
    
    public func managedTableBind(_ controller: AAManagedTableController, table: AAManagedTable, binder: AABinder) {
        // Do nothing
    }
    
    public func managedTableUnbind(_ controller: AAManagedTableController, table: AAManagedTable, binder: AABinder) {
        // Do nothing
    }
}
