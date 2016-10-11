//
//  Copyright (c) 2014-2016 Actor LLC. <https://actor.im>
//

import Foundation

open class AAManagedTable {
    
    //------------------------------------------------------------------------//
    
    // Controller of table
    
    open let controller: UIViewController
    
    // Table view
    
    open let style: AAContentTableStyle
    open let tableView: UITableView
    open var tableViewDelegate: UITableViewDelegate { get { return baseDelegate } }
    open var tableViewDataSource: UITableViewDataSource { get { return baseDelegate } }
    
    // Scrolling closure
    
    open var tableScrollClosure: ((_ tableView: UITableView) -> ())?
    
    // Is fade in/out animated
    
    open var fadeShowing = false
    
    // Sections of table
    
    open var sections: [AAManagedSection] = [AAManagedSection]()
    
    // Fixed Height
    
    open var fixedHeight: CGFloat?
    
    // Can Edit All rows
    
    open var canEditAll: Bool?
    
    // Can Delete All rows
    
    open var canDeleteAll: Bool?
    
    // Is Table in editing mode
    
    open var isEditing: Bool {
        get {
            return tableView.isEditing
        }
    }
    
    // Is updating sections
    
    fileprivate var isUpdating = false
    
    // Reference to table view delegate/data source
    
    fileprivate var baseDelegate: AMBaseTableDelegate!
    
    // Search
    fileprivate var isSearchInited: Bool = false
    fileprivate var isSearchAutoHide: Bool = false
    fileprivate var searchDisplayController: UISearchDisplayController!
    fileprivate var searchManagedController: AnyObject!
    
    //------------------------------------------------------------------------//
    
    public init(style: AAContentTableStyle, tableView: UITableView, controller: UIViewController) {
        self.style = style
        self.controller = controller
        self.tableView = tableView
        
        if style == .settingsGrouped {
            self.baseDelegate = AMGrouppedTableDelegate(data: self)
        } else {
            self.baseDelegate = AMPlainTableDelegate(data: self)
        }
        
        self.tableView.dataSource = self.baseDelegate
        self.tableView.delegate = self.baseDelegate
    }
    
    //------------------------------------------------------------------------//
    
    // Entry point to adding
    
    open func beginUpdates() {
        if isUpdating {
            fatalError("Already updating table")
        }
        isUpdating = true
    }
    
    open func addSection(_ autoSeparator: Bool = false) -> AAManagedSection {
        if !isUpdating {
            fatalError("Table is not in updating mode")
        }
        
        let res = AAManagedSection(table: self, index: sections.count)
        res.autoSeparators = autoSeparator
        sections.append(res)
        return res
    }
    
    open func search<C>(_ cell: C.Type, closure: (_ s: AAManagedSearchConfig<C>) -> ()) where C: AABindedSearchCell, C: UITableViewCell {
        
        if !isUpdating {
            fatalError("Table is not in updating mode")
        }
        
        if isSearchInited {
            fatalError("Search already inited")
        }
        
        isSearchInited = true
        
        // Configuring search source
        
        let config = AAManagedSearchConfig<C>()
        
        closure(config)
        
        // Creating search source
        
        let searchSource = AAManagedSearchController<C>(config: config, controller: controller, tableView: tableView)
        self.searchDisplayController = searchSource.searchDisplay
        self.searchManagedController = searchSource
        self.isSearchAutoHide = config.isSearchAutoHide
    }
    
    open func endUpdates() {
        if !isUpdating {
            fatalError("Table is not in editable mode")
        }
        isUpdating = false
    }
    
    // Reloading table
    
    open func reload() {
        self.tableView.reloadData()
    }
    
    open func reload(_ section: Int) {
        self.tableView.reloadSections(IndexSet(integer: section), with: .automatic)
    }
    
    // Binding methods
    
    open func bind(_ binder: AABinder) {
        for s in sections {
            s.bind(self, binder: binder)
        }
    }
    
    open func unbind(_ binder: AABinder) {
        for s in sections {
            s.unbind(self, binder: binder)
        }
    }
    
    // Show/hide table
    
    open func showTable() {
        if isUpdating || !fadeShowing {
            self.tableView.alpha = 1
        } else {
            UIView.animate(withDuration: 0.3, animations: { () -> Void in
                self.tableView.alpha = 1
            })
        }
    }
    
    open func hideTable() {
        if isUpdating || !fadeShowing {
            self.tableView.alpha = 0
        } else {
            UIView.animate(withDuration: 0.3, animations: { () -> Void in
                self.tableView.alpha = 0
            })
        }
    }
    
    // Controller callbacks
    
    open func controllerViewWillDisappear(_ animated: Bool) {
        
        // Auto close search on leaving controller
        if isSearchAutoHide {
            //dispatchOnUi { () -> Void in
                searchDisplayController?.setActive(false, animated: false)
            //}
        }
    }
    
    open func controllerViewDidDisappear(_ animated: Bool) {
        
        // Auto close search on leaving controller
//        searchDisplayController?.setActive(false, animated: animated)
    }

    
    open func controllerViewWillAppear(_ animated: Bool) {
        
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
        
        if (searchDisplayController != nil && searchDisplayController!.isActive) {
            
            // If search is active: apply search status bar style
            UIApplication.shared.setStatusBarStyle(ActorSDK.sharedActor().style.searchStatusBarStyle, animated: true)
        } else {
            
            // If search is not active: apply main status bar style
            UIApplication.shared.setStatusBarStyle(ActorSDK.sharedActor().style.vcStatusBarStyle, animated: true)
        }
    }
}

// Closure based extension

public extension AAManagedTable {
    
    public func section(_ closure: (_ s: AAManagedSection) -> ()){
        closure(addSection(true))
    }
}

// Table view delegates and data sources

private class AMPlainTableDelegate: AMBaseTableDelegate {
    
    @objc func tableView(_ tableView: UITableView, heightForHeaderInSection section: Int) -> CGFloat {
        return CGFloat(data.sections[section].headerHeight)
    }
    
    @objc func tableView(_ tableView: UITableView, heightForFooterInSection section: Int) -> CGFloat {
        return CGFloat(data.sections[section].footerHeight)
    }
    
    @objc func tableView(_ tableView: UITableView, viewForHeaderInSection section: Int) -> UIView? {
        if (data.sections[section].headerText == nil) {
            return UIView()
        } else {
            return nil
        }
    }
    
    @objc func tableView(_ tableView: UITableView, viewForFooterInSection section: Int) -> UIView? {
        if (data.sections[section].footerText == nil) {
            return UIView()
        } else {
            return nil
        }
    }
    
}

private class AMGrouppedTableDelegate: AMBaseTableDelegate {
    
    @objc func tableView(_ tableView: UITableView, willDisplayHeaderView view: UIView, forSection section: Int) {
        let header: UITableViewHeaderFooterView = view as! UITableViewHeaderFooterView
        header.textLabel!.textColor = ActorSDK.sharedActor().style.cellHeaderColor
    }
    
    @objc func tableView(_ tableView: UITableView, willDisplayFooterView view: UIView, forSection section: Int) {
        let header: UITableViewHeaderFooterView = view as! UITableViewHeaderFooterView
        header.textLabel!.textColor = ActorSDK.sharedActor().style.cellFooterColor
    }
}

private class AMBaseTableDelegate: NSObject, UITableViewDelegate, UITableViewDataSource, UIScrollViewDelegate {
    
    unowned fileprivate let data: AAManagedTable
    
    init(data: AAManagedTable) {
        self.data = data
    }
    
    @objc func numberOfSections(in tableView: UITableView) -> Int {
        return data.sections.count
    }
    
    @objc func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        return data.sections[section].numberOfItems(data)
    }
    
    @objc func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        return data.sections[(indexPath as NSIndexPath).section].cellForItem(data, indexPath: indexPath)
    }
    
    @objc func tableView(_ tableView: UITableView, titleForHeaderInSection section: Int) -> String? {
        let text = data.sections[section].headerText
        if text != nil {
            return AALocalized(text!)
        } else {
            return text
        }
    }
    
    @objc func tableView(_ tableView: UITableView, titleForFooterInSection section: Int) -> String? {
        let text = data.sections[section].footerText
        if text != nil {
            return AALocalized(text!)
        } else {
            return text
        }
        
    }
    
    @objc func tableView(_ tableView: UITableView, heightForRowAt indexPath: IndexPath) -> CGFloat {
        if data.fixedHeight != nil {
            return data.fixedHeight!
        }
        return data.sections[(indexPath as NSIndexPath).section].cellHeightForItem(data, indexPath: indexPath)
    }
    
    @objc func tableView(_ tableView: UITableView, canEditRowAt indexPath: IndexPath) -> Bool {
        if data.canEditAll != nil {
            return data.canEditAll!
        }
        return (data.sections[(indexPath as NSIndexPath).section].numberOfItems(data) > 0 ? data.sections[(indexPath as NSIndexPath).section].canDelete(data, indexPath: indexPath) : false)
    }
    
    @objc func tableView(_ tableView: UITableView, canMoveRowAt indexPath: IndexPath) -> Bool {
        return false
    }
    
    @objc func tableView(_ tableView: UITableView, editingStyleForRowAt indexPath: IndexPath) -> UITableViewCellEditingStyle {
        
        if data.canDeleteAll != nil {
            if data.canDeleteAll! {
                return .delete
            } else {
                return .none
            }
        }
        
        return data.sections[(indexPath as NSIndexPath).section].canDelete(data, indexPath: indexPath) ? .delete : .none
    }
   
    @objc func tableView(_ tableView: UITableView, commit editingStyle: UITableViewCellEditingStyle, forRowAt indexPath: IndexPath) {
        return data.sections[(indexPath as NSIndexPath).section].delete(data, indexPath: indexPath)
    }
   
    @objc func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
        let section = data.sections[(indexPath as NSIndexPath).section]
        if section.canSelect(data, indexPath: indexPath) {
            if section.select(data, indexPath: indexPath) {
                tableView.deselectRow(at: indexPath, animated: true)
            }
        } else {
            tableView.deselectRow(at: indexPath, animated: true)
        }
    }
    
    @objc func tableView(_ tableView: UITableView, canPerformAction action: Selector, forRowAt indexPath: IndexPath, withSender sender: Any?) -> Bool {
        
        if action == #selector(UIResponderStandardEditActions.copy(_:)) {
            let section = data.sections[(indexPath as NSIndexPath).section]
            return section.canCopy(data, indexPath: indexPath)
        }
        
        return false
    }
    @objc func tableView(_ tableView: UITableView, shouldShowMenuForRowAt indexPath: IndexPath) -> Bool {
        let section = data.sections[(indexPath as NSIndexPath).section]
        return section.canCopy(data, indexPath: indexPath)
    }
    
    @objc func tableView(_ tableView: UITableView, performAction action: Selector, forRowAt indexPath: IndexPath, withSender sender: Any?) {
        if action == #selector(UIResponderStandardEditActions.copy(_:)) {
            let section = data.sections[(indexPath as NSIndexPath).section]
            if section.canCopy(data, indexPath: indexPath) {
                section.copy(data, indexPath: indexPath)
            }
        }
    }
    
    @objc func scrollViewDidScroll(_ scrollView: UIScrollView) {
        if (data.tableView == scrollView) {
            data.tableScrollClosure?(data.tableView)
        }
    }
}

open class AAManagedSearchConfig<BindCell> where BindCell: AABindedSearchCell, BindCell: UITableViewCell {
    
    open var searchList: ARBindedDisplayList?
    open var searchModel: ARSearchValueModel?
    open var selectAction: ((BindCell.BindData) -> ())?
    open var isSearchAutoHide: Bool = true
    open var didBind: ((_ c: BindCell, _ d: BindCell.BindData) -> ())?
}

private class AAManagedSearchController<BindCell>: NSObject, UISearchBarDelegate, UISearchDisplayDelegate, UITableViewDataSource, UITableViewDelegate, ARDisplayList_Listener, ARValueChangedListener where BindCell: AABindedSearchCell, BindCell: UITableViewCell {
    
    let config: AAManagedSearchConfig<BindCell>
    let searchList: ARBindedDisplayList?
    let searchModel: ARSearchValueModel?
    let searchDisplay: UISearchDisplayController
    
    init(config: AAManagedSearchConfig<BindCell>, controller: UIViewController, tableView: UITableView) {
        
        self.config = config
        
        self.searchList = config.searchList
        self.searchModel = config.searchModel
        
        let style = ActorSDK.sharedActor().style
        let searchBar = UISearchBar()
        
        // Styling Search bar
        searchBar.searchBarStyle = UISearchBarStyle.default
        searchBar.isTranslucent = false
        searchBar.placeholder = "" // SearchBar placeholder animation fix
        
        // SearchBar background color
        searchBar.barTintColor = style.searchBackgroundColor.forTransparentBar()
        searchBar.setBackgroundImage(Imaging.imageWithColor(style.searchBackgroundColor, size: CGSize(width: 1, height: 1)), for: .any, barMetrics: .default)
        searchBar.backgroundColor = style.searchBackgroundColor
        
        // SearchBar cancel color
        searchBar.tintColor = style.searchCancelColor
        
        // Apply keyboard color
        searchBar.keyboardAppearance = style.isDarkApp ? UIKeyboardAppearance.dark : UIKeyboardAppearance.light

        // SearchBar field color
        let fieldBg = Imaging.imageWithColor(style.searchFieldBgColor, size: CGSize(width: 14,height: 28))
            .roundCorners(14, h: 28, roundSize: 4)
        searchBar.setSearchFieldBackgroundImage(fieldBg.stretchableImage(withLeftCapWidth: 7, topCapHeight: 0), for: UIControlState())
        
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
        
        self.searchDisplay.searchResultsTableView.separatorStyle = UITableViewCellSeparatorStyle.none
        self.searchDisplay.searchResultsTableView.backgroundColor = ActorSDK.sharedActor().style.vcBgColor
        
        // Adding search to table header
        
        let header = AATableViewHeader(frame: CGRect(x: 0, y: 0, width: 320, height: 44))
        header.addSubview(self.searchDisplay.searchBar)
        tableView.tableHeaderView = header
        
        // Start receiving events
        
        if let ds = searchList {
            ds.add(self)
        } else if let sm = searchModel {
            sm.getResults().subscribe(with: self)
        } else {
            fatalError("No search model or search list is set!")
        }
    }
    
    // Model
    
    
    func objectAtIndexPath(_ indexPath: IndexPath) -> BindCell.BindData {
        if let ds = searchList {
            return ds.item(with: jint((indexPath as NSIndexPath).row)) as! BindCell.BindData
        } else if let sm = searchModel {
            let list = sm.getResults().get() as! JavaUtilList
            return list.getWith(jint((indexPath as NSIndexPath).row)) as! BindCell.BindData
        } else {
            fatalError("No search model or search list is set!")
        }
    }
    
    @objc func onCollectionChanged() {
        searchDisplay.searchResultsTableView.reloadData()
    }
    
    @objc func onChanged(_ val: Any!, withModel valueModel: ARValue!) {
        searchDisplay.searchResultsTableView.reloadData()
    }
    
    // Table view data
    
    @objc func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        if let ds = searchList {
            return Int(ds.size())
        } else if let sm = searchModel {
            let list = sm.getResults().get() as! JavaUtilList
            return Int(list.size())
        } else {
            fatalError("No search model or search list is set!")
        }
    }
    
    @objc func tableView(_ tableView: UITableView, heightForRowAt indexPath: IndexPath) -> CGFloat {
        let item = objectAtIndexPath(indexPath)
        return BindCell.self.bindedCellHeight(item)
    }
    
    @objc func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        let item = objectAtIndexPath(indexPath)
        let cell = tableView.dequeueCell(BindCell.self, indexPath: indexPath) as! BindCell
        cell.bind(item, search: nil)
        return cell
    }
    
    @objc func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
        let item = objectAtIndexPath(indexPath)
        config.selectAction!(item)
    }
    
    // Search updating
    
    @objc func searchBar(_ searchBar: UISearchBar, textDidChange searchText: String) {
        if let ds = searchList {
            let normalized = searchText.trim().lowercased()
            if (normalized.length > 0) {
                ds.initSearch(withQuery: normalized, withRefresh: false)
            } else {
                ds.initEmpty()
            }
        } else if let sm = searchModel {
            sm.queryChanged(with: searchText)
        } else {
            fatalError("No search model or search list is set!")
        }
    }
    
    // Search styling
    
    @objc func searchDisplayControllerWillBeginSearch(_ controller: UISearchDisplayController) {
        UIApplication.shared.setStatusBarStyle(ActorSDK.sharedActor().style.searchStatusBarStyle, animated: true)
    }
    
    @objc func searchDisplayControllerWillEndSearch(_ controller: UISearchDisplayController) {
        UIApplication.shared.setStatusBarStyle(ActorSDK.sharedActor().style.vcStatusBarStyle, animated: true)
    }
    
    @objc func searchDisplayController(_ controller: UISearchDisplayController, didShowSearchResultsTableView tableView: UITableView) {
        for v in tableView.subviews {
            if (v is UIImageView) {
                (v as! UIImageView).alpha = 0;
            }
        }
    }
}
