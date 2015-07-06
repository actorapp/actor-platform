//
//  Copyright (c) 2015 Actor LLC. <https://actor.im>
//

import Foundation

class UABaseTableData : NSObject, UITableViewDataSource, UITableViewDelegate {
    
    static let ReuseCommonCell = "CommonCell";
    
    private var tableView: UITableView
    private var sections: [UASection] = [UASection]()
    
    var tableScrollClosure: ((tableView: UITableView) -> ())?
    
    init(tableView: UITableView) {
        self.tableView = tableView
        super.init()

        self.tableView.registerClass(CommonCell.self, forCellReuseIdentifier: UABaseTableData.ReuseCommonCell)
        self.tableView.dataSource = self
        self.tableView.delegate = self
    }
    
    func registerClass(cellClass: AnyClass, forCellReuseIdentifier identifier: String) {
        self.tableView.registerClass(cellClass, forCellReuseIdentifier: identifier)
    }
    
    func addSection() -> UASection {
        var res = UASection(tableView: tableView, index: sections.count)
        sections.append(res)
        return res
    }
    
    func numberOfSectionsInTableView(tableView: UITableView) -> Int {
        return sections.count
    }
    
    func tableView(tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        return sections[section].itemsCount()
    }
    
    func tableView(tableView: UITableView, cellForRowAtIndexPath indexPath: NSIndexPath) -> UITableViewCell {
        return sections[indexPath.section].buildCell(tableView, cellForRowAtIndexPath: indexPath)
    }
    
    func tableView(tableView: UITableView, titleForHeaderInSection section: Int) -> String? {
        return sections[section].headerText
    }

    func tableView(tableView: UITableView, titleForFooterInSection section: Int) -> String? {
        return sections[section].footerText
    }
    
    func tableView(tableView: UITableView, heightForRowAtIndexPath indexPath: NSIndexPath) -> CGFloat {
        return CGFloat(sections[indexPath.section].cellHeight(tableView, cellForRowAtIndexPath: indexPath))
    }
    
    func tableView(tableView: UITableView, canEditRowAtIndexPath indexPath: NSIndexPath) -> Bool {
        return false
    }
    
    func tableView(tableView: UITableView, canMoveRowAtIndexPath indexPath: NSIndexPath) -> Bool {
        return false
    }
    
    func tableView(tableView: UITableView, editingStyleForRowAtIndexPath indexPath: NSIndexPath) -> UITableViewCellEditingStyle {
        return UITableViewCellEditingStyle.None
    }
    
    func tableView(tableView: UITableView, didSelectRowAtIndexPath indexPath: NSIndexPath) {
        tableView.deselectRowAtIndexPath(indexPath, animated: true)
        
        var section = sections[indexPath.section]
        if (section.canSelect(tableView, cellForRowAtIndexPath: indexPath)) {
            section.select(tableView, cellForRowAtIndexPath: indexPath)
        }
    }
    
    func scrollViewDidScroll(scrollView: UIScrollView) {
        if (tableView == scrollView) {
            self.tableScrollClosure?(tableView: tableView)
        }
    }
}

class UATableData : UABaseTableData {
    
    func tableView(tableView: UITableView, heightForHeaderInSection section: Int) -> CGFloat {
        return CGFloat(sections[section].headerHeight)
    }

    func tableView(tableView: UITableView, heightForFooterInSection section: Int) -> CGFloat {
        return CGFloat(sections[section].footerHeight)
    }
    
    func tableView(tableView: UITableView, viewForHeaderInSection section: Int) -> UIView? {
        if (sections[section].headerText == nil) {
            return UIView()
        } else {
            return nil
        }
    }
    
    func tableView(tableView: UITableView, viewForFooterInSection section: Int) -> UIView? {
        if (sections[section].footerText == nil) {
            return UIView()
        } else {
            return nil
        }
    }
}

class UAGrouppedTableData : UABaseTableData {
    

    
    func tableView(tableView: UITableView, willDisplayHeaderView view: UIView, forSection section: Int) {
        let header: UITableViewHeaderFooterView = view as! UITableViewHeaderFooterView
        header.textLabel.textColor = MainAppTheme.list.sectionColor
    }
    
    func tableView(tableView: UITableView, willDisplayFooterView view: UIView, forSection section: Int) {
        let header: UITableViewHeaderFooterView = view as! UITableViewHeaderFooterView
        header.textLabel.textColor = MainAppTheme.list.hintColor
    }
}


class UASection {
    
    var headerHeight: Double = 0
    var footerHeight: Double = 0
    
    var headerText: String? = nil
    var footerText: String? = nil
    
    var index: Int    
    
    private var tableView: UITableView
    private var regions: [UARegion] = [UARegion]()
    
    init(tableView: UITableView, index: Int) {
        self.tableView = tableView
        self.index = index
    }
    
    func setFooterText(footerText: String) -> UASection {
        self.footerText = footerText
        return self
    }

    func setHeaderText(headerText: String) -> UASection {
        self.headerText = headerText
        return self
    }
    
    func setFooterHeight(footerHeight: Double) -> UASection {
        self.footerHeight = footerHeight
        return self
    }
    
    func setHeaderHeight(headerHeight: Double) -> UASection {
        self.headerHeight = headerHeight
        return self
    }
    
    func addActionCell(title: String, actionClosure: (() -> ())) -> UACommonCellRegion {
        return addCommonCell()
            .setContent(title)
            .setAction(actionClosure)
            .setStyle(.Blue)
    }
    
    func addNavigationCell(title: String, actionClosure: (() -> ())) -> UACommonCellRegion {
        return addCommonCell()
            .setContent(title)
            .setAction(actionClosure)
            .setStyle(.Navigation)
    }
    
    func addCommonCell(closure: (cell: CommonCell)->()) -> UACommonCellRegion {
        var res = UACommonCellRegion(section: self, closure: closure)
        regions.append(res)
        return res
    }
    
    func addCommonCell() -> UACommonCellRegion {
        var res = UACommonCellRegion(section: self)
        regions.append(res)
        return res
    }
    
    func addCustomCell(closure: (tableView:UITableView, indexPath: NSIndexPath) -> UITableViewCell) -> UACustomCellRegion {
        var res = UACustomCellRegion(section: self, closure: closure)
        regions.append(res)
        return res
    }
    
    func addCustomCells(height: Double,countClosure: () -> Int, closure: (tableView:UITableView, index: Int, indexPath: NSIndexPath) -> UITableViewCell) -> UACustomCellsRegion {
        var res = UACustomCellsRegion(height:height, countClosure: countClosure, closure: closure, section: self)
        regions.append(res)
        return res
    }
    
    func itemsCount() -> Int {
        var res = 0
        for r in regions {
            res += r.itemsCount()
        }
        return res
    }
    
    private func getRegion(indexPath: NSIndexPath) -> RegionSearchResult {
        var prevLength = 0
        for r in regions {
            if (prevLength <= indexPath.row && indexPath.row < prevLength + r.itemsCount()) {
                return RegionSearchResult(region: r, index: indexPath.row - prevLength)
            }
            prevLength += r.itemsCount()
        }
        
        fatalError("Inconsistent cell")
    }
    
    func buildCell(tableView: UITableView, cellForRowAtIndexPath indexPath: NSIndexPath) -> UITableViewCell {
        var r = getRegion(indexPath)
        return r.region.buildCell(tableView, index: r.index, indexPath: indexPath)
    }
    
    func cellHeight(tableView: UITableView, cellForRowAtIndexPath indexPath: NSIndexPath) -> Double {
        var r = getRegion(indexPath)
        return r.region.cellHeight(r.index)
    }
    
    func canSelect(tableView: UITableView, cellForRowAtIndexPath indexPath: NSIndexPath) -> Bool {
        var r = getRegion(indexPath)
        return r.region.canSelect(r.index)
    }
    
    func select(tableView: UITableView, cellForRowAtIndexPath indexPath: NSIndexPath) {
        var r = getRegion(indexPath)
        r.region.select(r.index)
    }
}

class UARegion {
    
    private var section: UASection
    
    init(section: UASection) {
        self.section = section
    }
    
    func itemsCount() -> Int {
        fatalError("Not implemented")
    }
    
    func cellHeight(index: Int) -> Double {
        fatalError("Not implemented")
    }
    
    func buildCell(tableView: UITableView, index: Int, indexPath: NSIndexPath) -> UITableViewCell {
        fatalError("Not implemented")
    }
    
    func canSelect(index: Int) -> Bool {
        fatalError("Not implemented")
    }
    
    func select(index: Int) {
        fatalError("Not implemented")
    }
}

class UACustomCellRegion : UARegion {
    
    private var height: Double = 44.0
    private var closure: (tableView:UITableView, indexPath: NSIndexPath) -> UITableViewCell
    
    init(section: UASection, closure: (tableView:UITableView, indexPath: NSIndexPath) -> UITableViewCell) {
        self.closure = closure
        super.init(section: section)
    }
    
    func setHeight(height: Double) -> UACustomCellRegion {
        self.height = height
        return self
    }
    
    override func itemsCount() -> Int {
        return 1
    }
    
    override func cellHeight(index: Int) -> Double {
        return height
    }
    
    override func canSelect(index: Int) -> Bool {
        return false
    }
    
    override func buildCell(tableView: UITableView, index: Int, indexPath: NSIndexPath) -> UITableViewCell {
        return closure(tableView: tableView, indexPath: indexPath)
    }
}

class UACustomCellsRegion : UARegion {
    
    private var height: Double
    private var countClosure: () -> Int
    private var closure: (tableView:UITableView, index: Int, indexPath: NSIndexPath) -> UITableViewCell
    private var actionClosure: ((index: Int) -> ())?
    
    init(height: Double, countClosure: () -> Int, closure: (tableView:UITableView, index: Int, indexPath: NSIndexPath) -> UITableViewCell, section: UASection) {
        self.height = height
        self.countClosure = countClosure
        self.closure = closure
        super.init(section: section)
    }
    
    func setAction(actionClosure: (index: Int) -> ()) -> UACustomCellsRegion {
        self.actionClosure = actionClosure
        return self
    }
    
    override func itemsCount() -> Int {
        return countClosure()
    }
    
    override func cellHeight(index: Int) -> Double {
        return height
    }
    
    override func canSelect(index: Int) -> Bool {
        return actionClosure != nil
    }
    
    override func select(index: Int) {
        actionClosure!(index: index)
    }
    
    override func buildCell(tableView: UITableView, index: Int, indexPath: NSIndexPath) -> UITableViewCell {
        return closure(tableView: tableView, index: index, indexPath: indexPath)
    }
}

class UACommonCellRegion : UARegion {
    
    private var closure: ((cell: CommonCell) -> ())?
    private var actionClosure: (() -> ())?
    
    private var style: CommonCellStyle? = nil
    
    private var content: String? = nil
    
    private var leftInset: Double = 0.0
    private var bottomSeparatorLeftInset: Double = 0.0
    private var topSeparatorLeftInset: Double = 0.0
    private var bottomSeparator: Bool = true
    private var topSeparator: Bool = true
    
    init(section: UASection, closure: (cell: CommonCell) -> ()) {
        self.closure = closure
        super.init(section: section)
    }
    
    override init(section: UASection) {
        super.init(section: section)
    }
    
    func setModificator(closure: (cell: CommonCell) -> ()) -> UACommonCellRegion {
        self.closure = closure
        return self
    }
    
    func setAction(actionClosure: (() -> ())) -> UACommonCellRegion {
        self.actionClosure = actionClosure
        return self
    }
    
    func setStyle(style: CommonCellStyle) -> UACommonCellRegion {
        self.style = style
        return self
    }
    
    func setContent(content: String) -> UACommonCellRegion {
        self.content = content
        return self
    }
    
    func setLeftInset(leftInset: Double) -> UACommonCellRegion {
        self.leftInset = leftInset
        return self
    }
    
    func showBottomSeparator(inset: Double) -> UACommonCellRegion {
        self.bottomSeparator = true
        self.bottomSeparatorLeftInset = inset
        return self
    }
    
    func hideBottomSeparator() -> UACommonCellRegion {
        self.bottomSeparator = false
        return self
    }
    
    func showTopSeparator(inset: Double) -> UACommonCellRegion {
        self.topSeparator = true
        self.topSeparatorLeftInset = inset
        return self
    }
    
    func hideTopSeparator() -> UACommonCellRegion {
        self.topSeparator = false
        return self
    }
    
    override func itemsCount() -> Int {
        return 1
    }
    
    override func cellHeight(index: Int) -> Double {
        return 44.0
    }
    
    override func canSelect(index: Int) -> Bool {
        return actionClosure != nil
    }
    
    override func select(index: Int) {
        actionClosure!()
    }
    
    override func buildCell(tableView: UITableView, index: Int, indexPath: NSIndexPath) -> UITableViewCell {
        var res = tableView
            .dequeueReusableCellWithIdentifier(
                UABaseTableData.ReuseCommonCell,
                forIndexPath: indexPath)
            as! CommonCell
        
        res.selectionStyle = canSelect(index) ? UITableViewCellSelectionStyle.Blue : UITableViewCellSelectionStyle.None
        res.setLeftInset(CGFloat(leftInset))
        
        if (content != nil) {
            res.setContent(NSLocalizedString(content!, comment: "Cell Title"))
        }
        if (style != nil) {
            res.style = style!
        }

        if (bottomSeparator) {
            res.showBottomSeparator()
            res.setBottomSeparatorLeftInset(CGFloat(bottomSeparatorLeftInset))
        } else {
            res.hideBottomSeparator()
        }
        
        if (topSeparator) {
            res.showTopSeparator()
            res.setTopSeparatorLeftInset(CGFloat(topSeparatorLeftInset))
        } else {
            res.hideTopSeparator()
        }
        
        closure?(cell: res)
        
        return res
    }
}

class RegionSearchResult {
    let region: UARegion
    let index: Int
    
    init(region: UARegion, index: Int) {
        self.region = region
        self.index = index
    }
}

