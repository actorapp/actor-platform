//
//  Copyright (c) 2014-2015 Actor LLC. <https://actor.im>
//

import Foundation

class ACManagedTable {
    
    //------------------------------------------------------------------------//
    
    // Controller of table
    
    let controller: UIViewController
    
    // Table view
    
    let style: ACContentTableStyle
    let tableView: UITableView
    var tableViewDelegate: UITableViewDelegate { get { return baseDelegate } }
    var tableViewDataSource: UITableViewDataSource { get { return baseDelegate } }
    
    // Scrolling closure
    
    var tableScrollClosure: ((tableView: UITableView) -> ())?
    
    // Is fade in/out animated
    
    var fadeShowing = false
    
    // Sections of table
    
    var sections: [ACManagedSection] = [ACManagedSection]()
    
    // Is Table in editing mode
    
    var isEditing: Bool {
        get {
            return tableView.editing
        }
    }
    
    // Is updating sections
    
    private var isUpdating = false
    
    // Reference to table view delegate/data source
    
    private var baseDelegate: AMBaseTableDelegate!
    
    // Search
    private var isSearchInited: Bool = false
    private var isSearchAutoHide: Bool = false
    private var searchDisplayController: UISearchDisplayController!
    private var searchManagedController: AnyObject!
    
    //------------------------------------------------------------------------//
    
    init(style: ACContentTableStyle, tableView: UITableView, controller: UIViewController) {
        self.style = style
        self.controller = controller
        self.tableView = tableView
        self.baseDelegate = tableView.style == .Plain ? AMPlainTableDelegate(data: self) : AMGrouppedTableDelegate(data: self)

        // Init table view
        self.tableView.dataSource = self.baseDelegate
        self.tableView.delegate = self.baseDelegate
    }
    
    //------------------------------------------------------------------------//
    
    // Entry point to adding
    
    func beginUpdates() {
        if isUpdating {
            fatalError("Already updating table")
        }
        isUpdating = true
    }
    
    func addSection(autoSeparator: Bool = false) -> ACManagedSection {
        if !isUpdating {
            fatalError("Table is not in updating mode")
        }
        
        let res = ACManagedSection(table: self, index: sections.count)
        res.autoSeparators = autoSeparator
        sections.append(res)
        return res
    }
    
    func search<C where C: ACBindedSearchCell, C: UITableViewCell>(cell: C.Type, closure: (s: ACManagedSearchConfig<C>) -> ()) {
        
        if !isUpdating {
            fatalError("Table is not in updating mode")
        }
        
        if isSearchInited {
            fatalError("Search already inited")
        }
        
        isSearchInited = true
        
        // Configuring search source
        
        let config = ACManagedSearchConfig<C>()
        
        closure(s: config)
        
        // Creating search source
        
        let searchSource = ACManagedSearchController<C>(config: config, controller: controller, tableView: tableView)
        self.searchDisplayController = searchSource.searchDisplay
        self.searchManagedController = searchSource
        self.isSearchAutoHide = config.isSearchAutoHide
    }
    
    func endUpdates() {
        if !isUpdating {
            fatalError("Table is not in editable mode")
        }
        isUpdating = false
    }
    
    // Reloading table
    
    func reload() {
        self.tableView.reloadData()
    }
    
    func reload(section: Int) {
        self.tableView.reloadSections(NSIndexSet(index: section), withRowAnimation: .Automatic)
    }
    
    // Binding methods
    
    func bind(binder: Binder) {
        for s in sections {
            s.bind(self, binder: binder)
        }
    }
    
    func unbind(binder: Binder) {
        for s in sections {
            s.unbind(self, binder: binder)
        }
    }
    
    // Show/hide table
    
    func showTable() {
        if isUpdating || !fadeShowing {
            self.tableView.alpha = 1
        } else {
            UIView.animateWithDuration(0.3, animations: { () -> Void in
                self.tableView.alpha = 1
            })
        }
    }
    
    func hideTable() {
        if isUpdating || !fadeShowing {
            self.tableView.alpha = 0
        } else {
            UIView.animateWithDuration(0.3, animations: { () -> Void in
                self.tableView.alpha = 0
            })
        }
    }
    
    // Controller callbacks
    
    func controllerViewWillDisappear(animated: Bool) {
        
        // Auto close search on leaving controller
        if isSearchAutoHide {
            //dispatchOnUi { () -> Void in
                searchDisplayController?.setActive(false, animated: false)
            //}
        }
    }
    
    func controllerViewDidDisappear(animated: Bool) {
        
        // Auto close search on leaving controller
//        searchDisplayController?.setActive(false, animated: animated)
    }

    
    func controllerViewWillAppear(animated: Bool) {
        
        // Search bar dissapear fixing

        // Hack No 1
        if searchDisplayController != nil {
            let searchBar = searchDisplayController!.searchBar
            let superView = searchBar.superview
            if !(superView is UITableView) {
                searchBar.removeFromSuperview()
                superView?.addSubview(searchBar)
            }
        }
        
        // Hack No 2
        
        tableView.tableHeaderView?.setNeedsLayout()
        tableView.tableFooterView?.setNeedsLayout()

        
        // Status bar styles
        
        if (searchDisplayController != nil && searchDisplayController!.active) {
            
            // If search is active: apply search status bar style
            MainAppTheme.search.applyStatusBar()
        } else {
            
            // If search is not active: apply main status bar style
            MainAppTheme.navigation.applyStatusBar()
        }
    }
}

// Closure based extension

extension ACManagedTable {
    
    func section(closure: (s: ACManagedSection) -> ()){
        closure(s: addSection(true))
    }
}

// Table view delegates and data sources

private class AMPlainTableDelegate: AMBaseTableDelegate {
    
    @objc func tableView(tableView: UITableView, heightForHeaderInSection section: Int) -> CGFloat {
        return CGFloat(data.sections[section].headerHeight)
    }
    
    @objc func tableView(tableView: UITableView, heightForFooterInSection section: Int) -> CGFloat {
        return CGFloat(data.sections[section].footerHeight)
    }
    
    @objc func tableView(tableView: UITableView, viewForHeaderInSection section: Int) -> UIView? {
        if (data.sections[section].headerText == nil) {
            return UIView()
        } else {
            return nil
        }
    }
    
    @objc func tableView(tableView: UITableView, viewForFooterInSection section: Int) -> UIView? {
        if (data.sections[section].footerText == nil) {
            return UIView()
        } else {
            return nil
        }
    }
    
}

private class AMGrouppedTableDelegate: AMBaseTableDelegate {
    func tableView(tableView: UITableView, willDisplayHeaderView view: UIView, forSection section: Int) {
        let header: UITableViewHeaderFooterView = view as! UITableViewHeaderFooterView
        header.textLabel!.textColor = MainAppTheme.list.sectionColor
    }
    
    func tableView(tableView: UITableView, willDisplayFooterView view: UIView, forSection section: Int) {
        let header: UITableViewHeaderFooterView = view as! UITableViewHeaderFooterView
        header.textLabel!.textColor = MainAppTheme.list.sectionHintColor
    }
}

private class AMBaseTableDelegate: NSObject, UITableViewDelegate, UITableViewDataSource, UIScrollViewDelegate {
    
    unowned private let data: ACManagedTable
    
    init(data: ACManagedTable) {
        self.data = data
    }
    
    @objc func numberOfSectionsInTableView(tableView: UITableView) -> Int {
        return data.sections.count
    }
    
    @objc func tableView(tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        return data.sections[section].numberOfItems(data)
    }
    
    @objc func tableView(tableView: UITableView, cellForRowAtIndexPath indexPath: NSIndexPath) -> UITableViewCell {
        return data.sections[indexPath.section].cellForItem(data, indexPath: indexPath)
    }
    
    @objc func tableView(tableView: UITableView, titleForHeaderInSection section: Int) -> String? {
        let text = data.sections[section].headerText
        if text != nil {
            return localized(text!)
        } else {
            return text
        }
    }
    
    @objc func tableView(tableView: UITableView, titleForFooterInSection section: Int) -> String? {
        let text = data.sections[section].footerText
        if text != nil {
            return localized(text!)
        } else {
            return text
        }
        
    }
    
    @objc func tableView(tableView: UITableView, heightForRowAtIndexPath indexPath: NSIndexPath) -> CGFloat {
        return data.sections[indexPath.section].cellHeightForItem(data, indexPath: indexPath)
    }
    
    @objc func tableView(tableView: UITableView, canEditRowAtIndexPath indexPath: NSIndexPath) -> Bool {
        return data.sections[indexPath.section].canDelete(data, indexPath: indexPath)
    }
    
    @objc func tableView(tableView: UITableView, canMoveRowAtIndexPath indexPath: NSIndexPath) -> Bool {
        return false
    }
    
    @objc func tableView(tableView: UITableView, editingStyleForRowAtIndexPath indexPath: NSIndexPath) -> UITableViewCellEditingStyle {
        return data.sections[indexPath.section].canDelete(data, indexPath: indexPath) ? .Delete : .None
    }
   
    @objc func tableView(tableView: UITableView, commitEditingStyle editingStyle: UITableViewCellEditingStyle, forRowAtIndexPath indexPath: NSIndexPath) {
        return data.sections[indexPath.section].delete(data, indexPath: indexPath)
    }
   
    @objc func tableView(tableView: UITableView, didSelectRowAtIndexPath indexPath: NSIndexPath) {
        let section = data.sections[indexPath.section]
        if section.canSelect(data, indexPath: indexPath) {
            if section.select(data, indexPath: indexPath) {
                tableView.deselectRowAtIndexPath(indexPath, animated: true)
            }
        } else {
            tableView.deselectRowAtIndexPath(indexPath, animated: true)
        }
    }
    
    @objc func tableView(tableView: UITableView, canPerformAction action: Selector, forRowAtIndexPath indexPath: NSIndexPath, withSender sender: AnyObject?) -> Bool {
        
        if action == "copy:" {
            let section = data.sections[indexPath.section]
            return section.canCopy(data, indexPath: indexPath)
        }
        
        return false
    }
    @objc func tableView(tableView: UITableView, shouldShowMenuForRowAtIndexPath indexPath: NSIndexPath) -> Bool {
        let section = data.sections[indexPath.section]
        return section.canCopy(data, indexPath: indexPath)
    }
    
    @objc func tableView(tableView: UITableView, performAction action: Selector, forRowAtIndexPath indexPath: NSIndexPath, withSender sender: AnyObject?) {
        if action == "copy:" {
            let section = data.sections[indexPath.section]
            if section.canCopy(data, indexPath: indexPath) {
                section.copy(data, indexPath: indexPath)
            }
        }
    }
    
    @objc func scrollViewDidScroll(scrollView: UIScrollView) {
        if (data.tableView == scrollView) {
            data.tableScrollClosure?(tableView: data.tableView)
        }
    }
}

class ACManagedSearchConfig<BindCell where BindCell: ACBindedSearchCell, BindCell: UITableViewCell> {
    
    var searchList: ARBindedDisplayList!
    var selectAction: ((BindCell.BindData) -> ())?
    var isSearchAutoHide: Bool = true
    var didBind: ((c: BindCell, d: BindCell.BindData) -> ())?
}

class ACManagedSearchController<BindCell where BindCell: ACBindedSearchCell, BindCell: UITableViewCell>: NSObject, UISearchBarDelegate, UISearchDisplayDelegate, UITableViewDataSource, UITableViewDelegate, ARDisplayList_Listener {
    
    let config: ACManagedSearchConfig<BindCell>
    let displayList: ARBindedDisplayList
    let searchDisplay: UISearchDisplayController
    
    init(config: ACManagedSearchConfig<BindCell>, controller: UIViewController, tableView: UITableView) {
        
        self.config = config
        
        self.displayList = config.searchList
        
        let searchBar = UISearchBar()
        MainAppTheme.search.styleSearchBar(searchBar)
        self.searchDisplay = UISearchDisplayController(searchBar: searchBar, contentsController: controller)
        
        super.init()

        // Creating Search Display Controller
        
        self.searchDisplay.searchBar.delegate = self
        self.searchDisplay.searchResultsDataSource = self
        self.searchDisplay.searchResultsDelegate = self
        self.searchDisplay.delegate = self
        
        // Styling search list
        
        self.searchDisplay.searchResultsTableView.separatorStyle = UITableViewCellSeparatorStyle.None
        self.searchDisplay.searchResultsTableView.backgroundColor = MainAppTheme.list.bgColor
        
        // Adding search to table header
        
        let header = TableViewHeader(frame: CGRectMake(0, 0, 320, 44))
        header.addSubview(self.searchDisplay.searchBar)
        tableView.tableHeaderView = header
        
        // Start receiving events
        
        self.displayList.addListener(self)
    }
    
    // Model
    
    
    func objectAtIndexPath(indexPath: NSIndexPath) -> BindCell.BindData {
        return displayList.itemWithIndex(jint(indexPath.row)) as! BindCell.BindData
    }
    
    func onCollectionChanged() {
        searchDisplay.searchResultsTableView.reloadData()
    }
    
    // Table view data
    
    func tableView(tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        return Int(displayList.size());
    }
    
    func tableView(tableView: UITableView, heightForRowAtIndexPath indexPath: NSIndexPath) -> CGFloat {
        let item = objectAtIndexPath(indexPath)
        return BindCell.self.bindedCellHeight(item)
    }
    
    func tableView(tableView: UITableView, cellForRowAtIndexPath indexPath: NSIndexPath) -> UITableViewCell {
        let item = objectAtIndexPath(indexPath)
        let cell = tableView.dequeueCell(BindCell.self, indexPath: indexPath) as! BindCell
        cell.bind(item, search: nil)
        return cell
    }
    
    func tableView(tableView: UITableView, didSelectRowAtIndexPath indexPath: NSIndexPath) {
        let item = objectAtIndexPath(indexPath)
        config.selectAction!(item)
        MainAppTheme.navigation.applyStatusBar()
    }
    
    // Search updating
    
    func searchBar(searchBar: UISearchBar, textDidChange searchText: String) {
        let normalized = searchText.trim().lowercaseString
        if (normalized.length > 0) {
            displayList.initSearchWithQuery(normalized, withRefresh: false)
        } else {
            displayList.initEmpty()
        }
    }
    
    // Search styling
    
    func searchDisplayControllerWillBeginSearch(controller: UISearchDisplayController) {
        MainAppTheme.search.applyStatusBar()
    }
    
    func searchDisplayControllerWillEndSearch(controller: UISearchDisplayController) {
        MainAppTheme.navigation.applyStatusBar()
    }
    
    func searchDisplayController(controller: UISearchDisplayController, didShowSearchResultsTableView tableView: UITableView) {
        for v in tableView.subviews {
            if (v is UIImageView) {
                (v as! UIImageView).alpha = 0;
            }
        }
    }
}
