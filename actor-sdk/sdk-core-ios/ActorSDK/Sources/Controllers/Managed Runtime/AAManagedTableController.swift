//
//  Copyright (c) 2014-2016 Actor LLC. <https://actor.im>
//

import Foundation

public class AAManagedTableController: AAViewController {
    
    public let style: AAContentTableStyle
    
    public var managedTableDelegate: AAManagedTableControllerDelegate?
    
    public let binder = AABinder()
    
    public var tableView: UITableView!
    
    public var managedTable: AAManagedTable!
    
    public var unbindOnDissapear: Bool = false
    
    private var isBinded: Bool = false
    
    public init(style: AAContentTableStyle) {
        self.style = style
        super.init()
    }

    public required init(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    public override func viewDidLoad() {
        super.viewDidLoad()
        
        // Creating tables
        
        let tableViewStyle: UITableViewStyle
        switch(style) {
        case .Plain:
            tableViewStyle = .Plain
            break
        case .SettingsPlain:
            tableViewStyle = .Plain
            break
        case .SettingsGrouped:
            tableViewStyle = .Grouped
            break
        }
        
        tableView = UITableView(frame: view.bounds, style: tableViewStyle)
        
        // Disabling separators as we use manual separators handling
        tableView.separatorStyle = .None
        
        // Setting tableView and view bg color depends on table style
        tableView.backgroundColor = style == .Plain ? appStyle.vcBgColor : appStyle.vcBackyardColor
        view.backgroundColor = tableView.backgroundColor
        
        managedTable = AAManagedTable(style: style, tableView: tableView, controller: self)
        view.addSubview(tableView)
        
        // Invoking table loading
        
        if let d = managedTableDelegate {
            d.managedTableLoad(self, table: managedTable)
        }
        
        // Initial load of Managed Table
        tableView.reloadData()
    }
    
    public override func viewWillAppear(animated: Bool) {

        if let t = tableView {
            if let row = t.indexPathForSelectedRow {
                t.deselectRowAtIndexPath(row, animated: animated)
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
            }
            
            // Passing event to table
            m.controllerViewWillAppear(animated)
        }
    }
    
    public override func viewWillDisappear(animated: Bool) {
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
    
    public override func viewDidDisappear(animated: Bool) {
        super.viewDidDisappear(animated)
        
        if let m = managedTable {
            
            // Passing event to table
            m.controllerViewDidDisappear(animated)
        }
    }
    
    public override func viewWillLayoutSubviews() {
        super.viewWillLayoutSubviews()
        
        tableView.frame = view.bounds
    }
}

public protocol AAManagedTableControllerDelegate {
    func managedTableLoad(controller: AAManagedTableController, table: AAManagedTable)
    func managedTableBind(controller: AAManagedTableController, table: AAManagedTable, binder: AABinder)
    func managedTableUnbind(controller: AAManagedTableController, table: AAManagedTable, binder: AABinder)
}

public extension AAManagedTableControllerDelegate {
    
    public func managedTableLoad(controller: AAManagedTableController, table: AAManagedTable) {
        // Do nothing
    }
    
    public func managedTableBind(controller: AAManagedTableController, table: AAManagedTable, binder: AABinder) {
        // Do nothing
    }
    
    public func managedTableUnbind(controller: AAManagedTableController, table: AAManagedTable, binder: AABinder) {
        // Do nothing
    }
}