//
//  Copyright (c) 2014-2015 Actor LLC. <https://actor.im>
//

import Foundation

class ACManagedTableController: AAViewController {
    
    let style: ACContentTableStyle
    
    var managedTableDelegate: ACManagedTableControllerDelegate?
    
    let binder = Binder()
    
    var tableView: UITableView!
    
    var managedTable: ACManagedTable!
    
    var unbindOnDissapear: Bool = false
    
    private var isBinded: Bool = false
    
    init(style: ACContentTableStyle) {
        self.style = style
        super.init()
    }

    required init(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    override func viewDidLoad() {
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
        tableView.backgroundColor = style == .Plain ? MainAppTheme.list.bgColor : MainAppTheme.list.backyardColor
        view.backgroundColor = tableView.backgroundColor
        
        managedTable = ACManagedTable(style: style, tableView: tableView, controller: self)
        view.addSubview(tableView)
        
        // Invoking table loading
        
        if let d = managedTableDelegate {
            d.managedTableLoad(self, table: managedTable)
        }
        
        // Initial load of Managed Table
        tableView.reloadData()
    }
    
    override func viewWillAppear(animated: Bool) {

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
    
    override func viewWillDisappear(animated: Bool) {
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
    
    override func viewDidDisappear(animated: Bool) {
        super.viewDidDisappear(animated)
        
        if let m = managedTable {
            
            // Passing event to table
            m.controllerViewDidDisappear(animated)
        }
    }
}

protocol ACManagedTableControllerDelegate {
    func managedTableLoad(controller: ACManagedTableController, table: ACManagedTable)
    func managedTableBind(controller: ACManagedTableController, table: ACManagedTable, binder: Binder)
    func managedTableUnbind(controller: ACManagedTableController, table: ACManagedTable, binder: Binder)
}

extension ACManagedTableControllerDelegate {
    
    func managedTableLoad(controller: ACManagedTableController, table: ACManagedTable) {
        // Do nothing
    }
    
    func managedTableBind(controller: ACManagedTableController, table: ACManagedTable, binder: Binder) {
        // Do nothing
    }
    
    func managedTableUnbind(controller: ACManagedTableController, table: ACManagedTable, binder: Binder) {
        // Do nothing
    }
}