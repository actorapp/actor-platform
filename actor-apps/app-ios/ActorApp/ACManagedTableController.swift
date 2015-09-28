//
//  Copyright (c) 2014-2015 Actor LLC. <https://actor.im>
//

import Foundation

class ACManagedTableController: AAViewController {
    
    private let tableViewStyle: UITableViewStyle
    
    var delegate: ACManagedTableControllerDelegate?
    
    let binder = Binder()
    
    var tableView: UITableView!
    
    var managedTable: ACManagedTable!
    
    init(tableViewStyle: UITableViewStyle) {
        self.tableViewStyle = tableViewStyle
        super.init()
    }

    required init(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        // Creating tables
        
        tableView = UITableView(frame: view.bounds, style: tableViewStyle)
        
        // Disabling separators as we use manual separators handling
        tableView.separatorStyle = .None
        
        // Setting tableView and view bg color depends on table style
        tableView.backgroundColor = MainAppTheme.list.backyardColor
        view.backgroundColor = tableView.backgroundColor
        
        managedTable = ACManagedTable(tableView: tableView, controller: self)
        view.addSubview(tableView)
        
        // Invoking table loading
        
        if let d = delegate {
            d.managedTableLoad(self, table: managedTable)
        }
        
        // Initial load of Managed Table
        tableView.reloadData()
    }

    override func viewWillAppear(animated: Bool) {

        if let row = tableView.indexPathForSelectedRow {
            tableView.deselectRowAtIndexPath(row, animated: animated)
        }
        
        super.viewWillAppear(animated)
        
        // Performing data binding
        if let d = delegate {
            d.managedTableBind(self, table: managedTable, binder: binder)
        }
    }
    
    override func viewWillDisappear(animated: Bool) {
        super.viewWillDisappear(animated)
        
        // Stopping data binding here
        if let d = delegate {
            d.managedTableUnbind(self, table: managedTable, binder: binder)
        }
        // Removing all bindings
        binder.unbindAll()
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