//
//  Copyright (c) 2014-2016 Actor LLC. <https://actor.im>
//

import Foundation

public class AAManagedTable {
    
    //------------------------------------------------------------------------//
    
    // Controller of table
    
    public let controller: UIViewController
    
    // Table view
    
    public let style: AAContentTableStyle
    public let tableView: UITableView
    public var tableViewDelegate: UITableViewDelegate { get { return baseDelegate } }
    public var tableViewDataSource: UITableViewDataSource { get { return baseDelegate } }
    
    // Scrolling closure
    
    public var tableScrollClosure: ((tableView: UITableView) -> ())?
    
    // Is fade in/out animated
    
    public var fadeShowing = false
    
    // Sections of table
    
    public var sections: [AAManagedSection] = [AAManagedSection]()
    
    // Is Table in editing mode
    
    public var isEditing: Bool {
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
    
    public init(style: AAContentTableStyle, tableView: UITableView, controller: UIViewController) {
        self.style = style
        self.controller = controller
        self.tableView = tableView
        
        if style == .SettingsGrouped {
            self.baseDelegate = AMGrouppedTableDelegate(data: self)
        } else {
            self.baseDelegate = AMPlainTableDelegate(data: self)
        }
        
        self.tableView.dataSource = self.baseDelegate
        self.tableView.delegate = self.baseDelegate
    }
    
    //------------------------------------------------------------------------//
    
    // Entry point to adding
    
    public func beginUpdates() {
        if isUpdating {
            fatalError("Already updating table")
        }
        isUpdating = true
    }
    
    public func addSection(autoSeparator: Bool = false) -> AAManagedSection {
        if !isUpdating {
            fatalError("Table is not in updating mode")
        }
        
        let res = AAManagedSection(table: self, index: sections.count)
        res.autoSeparators = autoSeparator
        sections.append(res)
        return res
    }
    
    public func search<C where C: AABindedSearchCell, C: UITableViewCell>(cell: C.Type, @noescape closure: (s: AAManagedSearchConfig<C>) -> ()) {
        
        if !isUpdating {
            fatalError("Table is not in updating mode")
        }
        
        if isSearchInited {
            fatalError("Search already inited")
        }
        
        isSearchInited = true
        
        // Configuring search source
        
        let config = AAManagedSearchConfig<C>()
        
        closure(s: config)
        
        // Creating search source
        
        let searchSource = AAManagedSearchController<C>(config: config, controller: controller, tableView: tableView)
        self.searchDisplayController = searchSource.searchDisplay
        self.searchManagedController = searchSource
        self.isSearchAutoHide = config.isSearchAutoHide
    }
    
    public func endUpdates() {
        if !isUpdating {
            fatalError("Table is not in editable mode")
        }
        isUpdating = false
    }
    
    // Reloading table
    
    public func reload() {
        self.tableView.reloadData()
    }
    
    public func reload(section: Int) {
        self.tableView.reloadSections(NSIndexSet(index: section), withRowAnimation: .Automatic)
    }
    
    // Binding methods
    
    public func bind(binder: AABinder) {
        for s in sections {
            s.bind(self, binder: binder)
        }
    }
    
    public func unbind(binder: AABinder) {
        for s in sections {
            s.unbind(self, binder: binder)
        }
    }
    
    // Show/hide table
    
    public func showTable() {
        if isUpdating || !fadeShowing {
            self.tableView.alpha = 1
        } else {
            UIView.animateWithDuration(0.3, animations: { () -> Void in
                self.tableView.alpha = 1
            })
        }
    }
    
    public func hideTable() {
        if isUpdating || !fadeShowing {
            self.tableView.alpha = 0
        } else {
            UIView.animateWithDuration(0.3, animations: { () -> Void in
                self.tableView.alpha = 0
            })
        }
    }
    
    // Controller callbacks
    
    public func controllerViewWillDisappear(animated: Bool) {
        
        // Auto close search on leaving controller
        if isSearchAutoHide {
            //dispatchOnUi { () -> Void in
                searchDisplayController?.setActive(false, animated: false)
            //}
        }
    }
    
    public func controllerViewDidDisappear(animated: Bool) {
        
        // Auto close search on leaving controller
//        searchDisplayController?.setActive(false, animated: animated)
    }

    
    public func controllerViewWillAppear(animated: Bool) {
        
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
            UIApplication.sharedApplication().setStatusBarStyle(ActorSDK.sharedActor().style.searchStatusBarStyle, animated: true)
        } else {
            
            // If search is not active: apply main status bar style
            UIApplication.sharedApplication().setStatusBarStyle(ActorSDK.sharedActor().style.vcStatusBarStyle, animated: true)
        }
    }
}

// Closure based extension

public extension AAManagedTable {
    
    public func section(closure: (s: AAManagedSection) -> ()){
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
    
    @objc func tableView(tableView: UITableView, willDisplayHeaderView view: UIView, forSection section: Int) {
        let header: UITableViewHeaderFooterView = view as! UITableViewHeaderFooterView
        header.textLabel!.textColor = ActorSDK.sharedActor().style.cellHeaderColor
    }
    
    @objc func tableView(tableView: UITableView, willDisplayFooterView view: UIView, forSection section: Int) {
        let header: UITableViewHeaderFooterView = view as! UITableViewHeaderFooterView
        header.textLabel!.textColor = ActorSDK.sharedActor().style.cellFooterColor
    }
}

private class AMBaseTableDelegate: NSObject, UITableViewDelegate, UITableViewDataSource, UIScrollViewDelegate {
    
    unowned private let data: AAManagedTable
    
    init(data: AAManagedTable) {
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
            return AALocalized(text!)
        } else {
            return text
        }
    }
    
    @objc func tableView(tableView: UITableView, titleForFooterInSection section: Int) -> String? {
        let text = data.sections[section].footerText
        if text != nil {
            return AALocalized(text!)
        } else {
            return text
        }
        
    }
    
    @objc func tableView(tableView: UITableView, heightForRowAtIndexPath indexPath: NSIndexPath) -> CGFloat {
        return data.sections[indexPath.section].cellHeightForItem(data, indexPath: indexPath)
    }
    
    @objc func tableView(tableView: UITableView, canEditRowAtIndexPath indexPath: NSIndexPath) -> Bool {
        return (data.sections[indexPath.section].numberOfItems(data) > 0 ? data.sections[indexPath.section].canDelete(data, indexPath: indexPath) : false)
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

public class AAManagedSearchConfig<BindCell where BindCell: AABindedSearchCell, BindCell: UITableViewCell> {
    
    public var searchList: ARBindedDisplayList!
    public var selectAction: ((BindCell.BindData) -> ())?
    public var isSearchAutoHide: Bool = true
    public var didBind: ((c: BindCell, d: BindCell.BindData) -> ())?
}

private class AAManagedSearchController<BindCell where BindCell: AABindedSearchCell, BindCell: UITableViewCell>: NSObject, UISearchBarDelegate, UISearchDisplayDelegate, UITableViewDataSource, UITableViewDelegate, ARDisplayList_Listener {
    
    let config: AAManagedSearchConfig<BindCell>
    let displayList: ARBindedDisplayList
    let searchDisplay: UISearchDisplayController
    
    init(config: AAManagedSearchConfig<BindCell>, controller: UIViewController, tableView: UITableView) {
        
        self.config = config
        
        self.displayList = config.searchList
        
        let style = ActorSDK.sharedActor().style
        let searchBar = UISearchBar()
        
        // Styling Search bar
        searchBar.searchBarStyle = UISearchBarStyle.Default
        searchBar.translucent = false
        searchBar.placeholder = "" // SearchBar placeholder animation fix
        
        // SearchBar background color
        searchBar.barTintColor = style.searchBackgroundColor.forTransparentBar()
        searchBar.setBackgroundImage(Imaging.imageWithColor(style.searchBackgroundColor, size: CGSize(width: 1, height: 1)), forBarPosition: .Any, barMetrics: .Default)
        searchBar.backgroundColor = style.searchBackgroundColor
        
        // SearchBar cancel color
        searchBar.tintColor = style.searchCancelColor
        
        // Apply keyboard color
        searchBar.keyboardAppearance = style.isDarkApp ? UIKeyboardAppearance.Dark : UIKeyboardAppearance.Light

        // SearchBar field color
        let fieldBg = Imaging.imageWithColor(style.searchFieldBgColor, size: CGSize(width: 14,height: 28))
            .roundCorners(14, h: 28, roundSize: 4)
        searchBar.setSearchFieldBackgroundImage(fieldBg.stretchableImageWithLeftCapWidth(7, topCapHeight: 0), forState: UIControlState.Normal)
        
        // SearchBar field text color
        for subView in searchBar.subviews {
            for secondLevelSubview in subView.subviews {
                if let tf = secondLevelSubview as? UITextField {
                    tf.textColor = style.searchFieldTextColor
                    break
                }
            }
        }

        self.searchDisplay = UISearchDisplayController(searchBar: searchBar, contentsController: controller)
        
        super.init()

        // Creating Search Display Controller
        
        self.searchDisplay.searchBar.delegate = self
        self.searchDisplay.searchResultsDataSource = self
        self.searchDisplay.searchResultsDelegate = self
        self.searchDisplay.delegate = self
        
        // Styling search list
        
        self.searchDisplay.searchResultsTableView.separatorStyle = UITableViewCellSeparatorStyle.None
        self.searchDisplay.searchResultsTableView.backgroundColor = ActorSDK.sharedActor().style.vcBgColor
        
        // Adding search to table header
        
        let header = AATableViewHeader(frame: CGRectMake(0, 0, 320, 44))
        header.addSubview(self.searchDisplay.searchBar)
        tableView.tableHeaderView = header
        
        // Start receiving events
        
        self.displayList.addListener(self)
    }
    
    // Model
    
    
    func objectAtIndexPath(indexPath: NSIndexPath) -> BindCell.BindData {
        return displayList.itemWithIndex(jint(indexPath.row)) as! BindCell.BindData
    }
    
    @objc func onCollectionChanged() {
        searchDisplay.searchResultsTableView.reloadData()
    }
    
    // Table view data
    
    @objc func tableView(tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        return Int(displayList.size());
    }
    
    @objc func tableView(tableView: UITableView, heightForRowAtIndexPath indexPath: NSIndexPath) -> CGFloat {
        let item = objectAtIndexPath(indexPath)
        return BindCell.self.bindedCellHeight(item)
    }
    
    @objc func tableView(tableView: UITableView, cellForRowAtIndexPath indexPath: NSIndexPath) -> UITableViewCell {
        let item = objectAtIndexPath(indexPath)
        let cell = tableView.dequeueCell(BindCell.self, indexPath: indexPath) as! BindCell
        cell.bind(item, search: nil)
        return cell
    }
    
    @objc func tableView(tableView: UITableView, didSelectRowAtIndexPath indexPath: NSIndexPath) {
        let item = objectAtIndexPath(indexPath)
        config.selectAction!(item)
        // MainAppTheme.navigation.applyStatusBar()
    }
    
    // Search updating
    
    @objc func searchBar(searchBar: UISearchBar, textDidChange searchText: String) {
        let normalized = searchText.trim().lowercaseString
        if (normalized.length > 0) {
            displayList.initSearchWithQuery(normalized, withRefresh: false)
        } else {
            displayList.initEmpty()
        }
    }
    
    // Search styling
    
    @objc func searchDisplayControllerWillBeginSearch(controller: UISearchDisplayController) {
        UIApplication.sharedApplication().setStatusBarStyle(ActorSDK.sharedActor().style.searchStatusBarStyle, animated: true)
    }
    
    @objc func searchDisplayControllerWillEndSearch(controller: UISearchDisplayController) {
        UIApplication.sharedApplication().setStatusBarStyle(ActorSDK.sharedActor().style.vcStatusBarStyle, animated: true)
    }
    
    @objc func searchDisplayController(controller: UISearchDisplayController, didShowSearchResultsTableView tableView: UITableView) {
        for v in tableView.subviews {
            if (v is UIImageView) {
                (v as! UIImageView).alpha = 0;
            }
        }
    }
}
