//
//  Copyright (c) 2014-2015 Actor LLC. <https://actor.im>
//

import Foundation

class ACManagedTable {
    
    static let ReuseCommonCell = "_CommonCell";
    static let ReuseTextCell = "_TextCell";
    static let ReuseTitledCell = "_TitledCell";
    
    private let tableView: UITableView
    private var baseDelegate: AMBaseTableDelegate!
    private var sections: [UASection] = [UASection]()
    
    var delegate: UITableViewDelegate { get { return baseDelegate } }
    var dataSource: UITableViewDataSource { get { return baseDelegate } }
    var tableScrollClosure: ((tableView: UITableView) -> ())?
    
    init(tableView: UITableView) {
        
        self.tableView = tableView
        self.baseDelegate = tableView.style == .Plain ? AMPlainTableDelegate(data: self) : AMGrouppedTableDelegate(data: self)

        // Init table view
        self.tableView.dataSource = self.baseDelegate
        self.tableView.delegate = self.baseDelegate
        
        // Registering classes
        self.tableView.registerClass(CommonCell.self, forCellReuseIdentifier: ACManagedTable.ReuseCommonCell)
        self.tableView.registerClass(TextCell.self, forCellReuseIdentifier: ACManagedTable.ReuseTextCell)
        self.tableView.registerClass(TitledCell.self, forCellReuseIdentifier: ACManagedTable.ReuseTitledCell)
    }
    
    func registerClass(cellClass: AnyClass, forCellReuseIdentifier identifier: String) {
        self.tableView.registerClass(cellClass, forCellReuseIdentifier: identifier)
    }
    
    func addSection(autoSeparator: Bool = false) -> UASection {
        let res = UASection(tableView: tableView, index: sections.count)
        res.autoSeparators = autoSeparator
        sections.append(res)
        return res
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
    
    private let data: ACManagedTable
    
    init(data: ACManagedTable) {
        self.data = data
    }
    
    @objc func numberOfSectionsInTableView(tableView: UITableView) -> Int {
        return data.sections.count
    }
    
    @objc func tableView(tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        return data.sections[section].itemsCount()
    }
    
    @objc func tableView(tableView: UITableView, cellForRowAtIndexPath indexPath: NSIndexPath) -> UITableViewCell {
        return data.sections[indexPath.section].buildCell(tableView, cellForRowAtIndexPath: indexPath)
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
        return CGFloat(data.sections[indexPath.section].cellHeight(tableView, cellForRowAtIndexPath: indexPath))
    }
    
    @objc func tableView(tableView: UITableView, canEditRowAtIndexPath indexPath: NSIndexPath) -> Bool {
        return false
    }
    
    @objc func tableView(tableView: UITableView, canMoveRowAtIndexPath indexPath: NSIndexPath) -> Bool {
        return false
    }
    
    @objc func tableView(tableView: UITableView, editingStyleForRowAtIndexPath indexPath: NSIndexPath) -> UITableViewCellEditingStyle {
        return UITableViewCellEditingStyle.None
    }
    
    @objc func tableView(tableView: UITableView, didSelectRowAtIndexPath indexPath: NSIndexPath) {
        let section = data.sections[indexPath.section]
        if (section.canSelect(tableView, cellForRowAtIndexPath: indexPath)) {
            if section.select(tableView, cellForRowAtIndexPath: indexPath) {
                tableView.deselectRowAtIndexPath(indexPath, animated: true)
            }
        } else {
            tableView.deselectRowAtIndexPath(indexPath, animated: true)
        }
    }
    
    @objc func tableView(tableView: UITableView, canPerformAction action: Selector, forRowAtIndexPath indexPath: NSIndexPath, withSender sender: AnyObject?) -> Bool {
        
        if action == "copy:" {
            let section = data.sections[indexPath.section]
            return section.canCopy(tableView, cellForRowAtIndexPath: indexPath)
        }
        
        return false
    }
    @objc func tableView(tableView: UITableView, shouldShowMenuForRowAtIndexPath indexPath: NSIndexPath) -> Bool {
        let section = data.sections[indexPath.section]
        return section.canCopy(tableView, cellForRowAtIndexPath: indexPath)
    }
    
    @objc func tableView(tableView: UITableView, performAction action: Selector, forRowAtIndexPath indexPath: NSIndexPath, withSender sender: AnyObject?) {
        if action == "copy:" {
            let section = data.sections[indexPath.section]
            if section.canCopy(tableView, cellForRowAtIndexPath: indexPath) {
                section.copy(tableView, cellForRowAtIndexPath: indexPath)
            }
        }
    }
    
    @objc func scrollViewDidScroll(scrollView: UIScrollView) {
        if (data.tableView == scrollView) {
            data.tableScrollClosure?(tableView: data.tableView)
        }
    }
}

// Search result

class RegionSearchResult {
    let region: UARegion
    let index: Int
    
    init(region: UARegion, index: Int) {
        self.region = region
        self.index = index
    }
}

