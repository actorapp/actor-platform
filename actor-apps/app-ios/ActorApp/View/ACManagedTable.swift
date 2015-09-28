//
//  Copyright (c) 2014-2015 Actor LLC. <https://actor.im>
//

import Foundation

class ACManagedTable {
    
    private var baseDelegate: AMBaseTableDelegate!
    
    var sections: [ACManagedSection] = [ACManagedSection]()
    
    unowned let controller: UIViewController
    unowned let tableView: UITableView
    var delegate: UITableViewDelegate { get { return baseDelegate } }
    var dataSource: UITableViewDataSource { get { return baseDelegate } }
    
    var tableScrollClosure: ((tableView: UITableView) -> ())?
    
    init(tableView: UITableView, controller: UIViewController) {
        self.controller = controller
        self.tableView = tableView
        self.baseDelegate = tableView.style == .Plain ? AMPlainTableDelegate(data: self) : AMGrouppedTableDelegate(data: self)

        // Init table view
        self.tableView.dataSource = self.baseDelegate
        self.tableView.delegate = self.baseDelegate
    }
    
    func reload() {
        self.tableView.reloadData()
    }
    
    func reload(section: Int) {
        self.tableView.reloadSections(NSIndexSet(index: section), withRowAnimation: .Automatic)
    }
    
    func registerClass(cellClass: AnyClass, forCellReuseIdentifier identifier: String) {
        self.tableView.registerClass(cellClass, forCellReuseIdentifier: identifier)
    }
    
    func addSection(autoSeparator: Bool = false) -> ACManagedSection {
        let res = ACManagedSection(table: self, index: sections.count)
        res.autoSeparators = autoSeparator
        sections.append(res)
        return res
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

