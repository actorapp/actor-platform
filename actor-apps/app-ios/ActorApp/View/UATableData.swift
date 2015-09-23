//
//  Copyright (c) 2014-2015 Actor LLC. <https://actor.im>
//

import Foundation

class UABaseTableData : NSObject, UITableViewDataSource, UITableViewDelegate {
    
    static let ReuseCommonCell = "_CommonCell";
    static let ReuseTextCell = "_TextCell";
    static let ReuseTitledCell = "_TitledCell";
    
    private var tableView: UITableView
    private var sections: [UASection] = [UASection]()
    
    var tableScrollClosure: ((tableView: UITableView) -> ())?
    
    init(tableView: UITableView) {
        self.tableView = tableView
        super.init()

        self.tableView.registerClass(CommonCell.self, forCellReuseIdentifier: UABaseTableData.ReuseCommonCell)
        self.tableView.registerClass(TextCell.self, forCellReuseIdentifier: UABaseTableData.ReuseTextCell)
        self.tableView.registerClass(TitledCell.self, forCellReuseIdentifier: UABaseTableData.ReuseTitledCell)
        self.tableView.dataSource = self
        self.tableView.delegate = self
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
        let text = sections[section].headerText
        if text != nil {
            return localized(text!)
        } else {
            return text
        }
    }

    func tableView(tableView: UITableView, titleForFooterInSection section: Int) -> String? {
        let text = sections[section].footerText
        if text != nil {
            return localized(text!)
        } else {
            return text
        }

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
        let section = sections[indexPath.section]
        if (section.canSelect(tableView, cellForRowAtIndexPath: indexPath)) {
            if section.select(tableView, cellForRowAtIndexPath: indexPath) {
                tableView.deselectRowAtIndexPath(indexPath, animated: true)
            }
        } else {
            tableView.deselectRowAtIndexPath(indexPath, animated: true)
        }
    }
    
    func tableView(tableView: UITableView, canPerformAction action: Selector, forRowAtIndexPath indexPath: NSIndexPath, withSender sender: AnyObject?) -> Bool {
        
        if action == "copy:" {
            let section = sections[indexPath.section]
            return section.canCopy(tableView, cellForRowAtIndexPath: indexPath)
        }
        
        return false
    }
    func tableView(tableView: UITableView, shouldShowMenuForRowAtIndexPath indexPath: NSIndexPath) -> Bool {
        let section = sections[indexPath.section]
        return section.canCopy(tableView, cellForRowAtIndexPath: indexPath)
    }
    
    func tableView(tableView: UITableView, performAction action: Selector, forRowAtIndexPath indexPath: NSIndexPath, withSender sender: AnyObject?) {
        if action == "copy:" {
            let section = sections[indexPath.section]
            if section.canCopy(tableView, cellForRowAtIndexPath: indexPath) {
               section.copy(tableView, cellForRowAtIndexPath: indexPath)
            }
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
        header.textLabel!.textColor = MainAppTheme.list.sectionColor
    }
    
    func tableView(tableView: UITableView, willDisplayFooterView view: UIView, forSection section: Int) {
        let header: UITableViewHeaderFooterView = view as! UITableViewHeaderFooterView
        header.textLabel!.textColor = MainAppTheme.list.hintColor
    }
}

class UASection {
    
    var headerHeight: Double = 0
    var footerHeight: Double = 0
    
    var headerText: String? = nil
    var footerText: String? = nil
    
    var index: Int
    
    var autoSeparatorTopOffset: Int = 0
    var autoSeparatorBottomOffset: Int = 0
    var autoSeparators: Bool = false
    var autoSeparatorsInset: CGFloat = 15.0
    
    private var tableView: UITableView
    private var regions: [UARegion] = [UARegion]()
    
    init(tableView: UITableView, index: Int) {
        self.tableView = tableView
        self.index = index
    }
    
    func setSeparatorsTopOffset(offset: Int) -> UASection {
        self.autoSeparatorTopOffset = offset
        return self
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
    
    func addActionCell(title: String, actionClosure: (() -> Bool)) -> UACommonCellRegion {
        return addCommonCell()
            .setContent(title)
            .setAction(actionClosure)
            .setStyle(.Action)
    }
    
    func addDangerCell(title: String, actionClosure: (() -> Bool)) -> UACommonCellRegion {
        return addCommonCell()
            .setContent(title)
            .setAction(actionClosure)
            .setStyle(.Destructive)
    }
    
    func addNavigationCell(title: String, actionClosure: (() -> Bool)) -> UACommonCellRegion {
        return addCommonCell()
            .setContent(title)
            .setAction(actionClosure)
            .setStyle(.Navigation)
    }
    
    func addCommonCell(closure: (cell: CommonCell)->()) -> UACommonCellRegion {
        let res = UACommonCellRegion(section: self, closure: closure)
        regions.append(res)
        return res
    }
    
    func addCommonCell() -> UACommonCellRegion {
        let res = UACommonCellRegion(section: self)
        regions.append(res)
        return res
    }
    
    func addTextCell(title: String, text: String) -> UATextCellRegion {
        let res = UATextCellRegion(title: title, text: text, section: self)
        regions.append(res)
        return res
    }
    
    func addTitledCell(title: String, text: String) -> UATitledCellRegion {
        let res = UATitledCellRegion(title: title, text: text, section: self)
        regions.append(res)
        return res
    }
    
    func addCustomCell(closure: (tableView:UITableView, indexPath: NSIndexPath) -> UITableViewCell) -> UACustomCellRegion {
        let res = UACustomCellRegion(section: self, closure: closure)
        regions.append(res)
        return res
    }
    
    func addCustomCells(height: CGFloat,countClosure: () -> Int, closure: (tableView:UITableView, index: Int, indexPath: NSIndexPath) -> UITableViewCell) -> UACustomCellsRegion {
        let res = UACustomCellsRegion(height:height, countClosure: countClosure, closure: closure, section: self)
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
        let r = getRegion(indexPath)
        let res = r.region.buildCell(tableView, index: r.index, indexPath: indexPath)
        if autoSeparators {
            if let cell = res as? UATableViewCell {
                
                // Top separator
                // Showing only for top cell
                cell.topSeparatorLeftInset = 0
                cell.topSeparatorVisible = indexPath.row == autoSeparatorTopOffset
                
                if indexPath.row >= autoSeparatorTopOffset {
                    cell.bottomSeparatorVisible = true
                    if indexPath.row == itemsCount() - 1 {
                        cell.bottomSeparatorLeftInset = 0
                    } else {
                        cell.bottomSeparatorLeftInset = autoSeparatorsInset
                    }
                } else {
                    // too high
                    cell.bottomSeparatorVisible = false
                }
                
            }
        }
        return res
    }
    
    func cellHeight(tableView: UITableView, cellForRowAtIndexPath indexPath: NSIndexPath) -> CGFloat {
        let r = getRegion(indexPath)
        return r.region.cellHeight(r.index, width: tableView.bounds.width)
    }
    
    func canSelect(tableView: UITableView, cellForRowAtIndexPath indexPath: NSIndexPath) -> Bool {
        let r = getRegion(indexPath)
        return r.region.canSelect(r.index)
    }
    
    func select(tableView: UITableView, cellForRowAtIndexPath indexPath: NSIndexPath) -> Bool {
        let r = getRegion(indexPath)
        return r.region.select(r.index)
    }
    
    func canCopy(tableView: UITableView, cellForRowAtIndexPath indexPath: NSIndexPath) -> Bool {
        let r = getRegion(indexPath)
        return r.region.canCopy(r.index)
    }
    
    func copy(tableView: UITableView, cellForRowAtIndexPath indexPath: NSIndexPath) {
        let r = getRegion(indexPath)
        r.region.copy(r.index)
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
    
    func cellHeight(index: Int, width: CGFloat) -> CGFloat {
        fatalError("Not implemented")
    }
    
    func buildCell(tableView: UITableView, index: Int, indexPath: NSIndexPath) -> UITableViewCell {
        fatalError("Not implemented")
    }
    
    func canSelect(index: Int) -> Bool {
        return false
    }
    
    func select(index: Int) -> Bool {
        fatalError("Not implemented")
    }
    
    func canCopy(index: Int) -> Bool {
        return false
    }
    
    func copy(index: Int) {
        fatalError("Not implemented")
    }
}

class UASingleCellRegion : UARegion {
    
    private var copyData: String?
    private var height: CGFloat = 44.0
    private var actionClosure: (() -> Bool)?
    private var longActionClosure: (() -> ())?
    
    func setAction(actionClosure: () -> Bool) -> UASingleCellRegion {
        self.actionClosure = actionClosure
        return self
    }
    
    func setCopy(copyData: String) -> UASingleCellRegion {
        self.copyData = copyData
        return self
    }
    
    override func itemsCount() -> Int {
        return 1
    }
    
    func setHeight(height: CGFloat) -> UASingleCellRegion {
        self.height = height
        return self
    }
    
    override func canSelect(index: Int) -> Bool {
        return actionClosure != nil
    }
    
    override func select(index: Int) -> Bool {
        return actionClosure!()
    }
    
    override func cellHeight(index: Int, width: CGFloat) -> CGFloat {
        return height
    }
    
    override func canCopy(index: Int) -> Bool {
        return copyData != nil
    }
    
    override func copy(index: Int) {
        UIPasteboard.generalPasteboard().string = copyData
    }
}

class UACustomCellRegion : UASingleCellRegion {
    
    private var closure: (tableView:UITableView, indexPath: NSIndexPath) -> UITableViewCell
    
    init(section: UASection, closure: (tableView:UITableView, indexPath: NSIndexPath) -> UITableViewCell) {
        self.closure = closure
        super.init(section: section)
    }
    
    override func buildCell(tableView: UITableView, index: Int, indexPath: NSIndexPath) -> UITableViewCell {
        return closure(tableView: tableView, indexPath: indexPath)
    }
}

class UACustomCellsRegion : UARegion {
    
    private var canCopy = false
    private var height: CGFloat
    private var countClosure: () -> Int
    private var closure: (tableView:UITableView, index: Int, indexPath: NSIndexPath) -> UITableViewCell
    private var actionClosure: ((index: Int) -> Bool)?
    
    init(height: CGFloat, countClosure: () -> Int, closure: (tableView:UITableView, index: Int, indexPath: NSIndexPath) -> UITableViewCell, section: UASection) {
        self.height = height
        self.countClosure = countClosure
        self.closure = closure
        super.init(section: section)
    }
    
    func setAction(actionClosure: (index: Int) -> Bool) -> UACustomCellsRegion {
        self.actionClosure = actionClosure
        return self
    }
    
    func setCanCopy(canCopy: Bool) -> UACustomCellsRegion {
        self.canCopy = canCopy
        return self
    }
    
    override func itemsCount() -> Int {
        return countClosure()
    }
    
    override func cellHeight(index: Int, width: CGFloat) -> CGFloat {
        return height
    }
    
    override func canSelect(index: Int) -> Bool {
        return actionClosure != nil
    }
    
    override func select(index: Int) -> Bool {
        return actionClosure!(index: index)
    }
    
    override func buildCell(tableView: UITableView, index: Int, indexPath: NSIndexPath) -> UITableViewCell {
        return closure(tableView: tableView, index: index, indexPath: indexPath)
    }
    
    override func canCopy(index: Int) -> Bool {
        return canCopy
    }
}

class UATextCellRegion: UASingleCellRegion {
    
    private var text: String
    private var title: String
    private var enableNavigation: Bool = false
    private var isAction: Bool = false
    
    init(title: String, text: String, section: UASection) {
        self.text = text
        self.title = title
        super.init(section: section)
    }
    
    func setEnableNavigation(enableNavigation: Bool) -> UATextCellRegion{
        self.enableNavigation = enableNavigation
        return self
    }
    
    func setContent(title: String, text: String) {
        self.text = text
        self.title = title
    }
    
    func setIsAction(isAction: Bool) {
        self.isAction = isAction
    }
    
    override func buildCell(tableView: UITableView, index: Int, indexPath: NSIndexPath) -> UITableViewCell {
        let res = tableView.dequeueReusableCellWithIdentifier(UABaseTableData.ReuseTextCell, forIndexPath: indexPath) as! TextCell
        res.setTitle(title, content: text)
        res.setAction(isAction)
        if enableNavigation {
            res.accessoryType = UITableViewCellAccessoryType.DisclosureIndicator
        } else {
            res.accessoryType = UITableViewCellAccessoryType.None
        }
        return res
    }
    
    override func cellHeight(index: Int, width: CGFloat) -> CGFloat {
        return TextCell.measure(text, width: width, enableNavigation: enableNavigation)
    }
}

class UATitledCellRegion: UASingleCellRegion {
    
    private var title: String
    private var text: String
    
    init(title: String, text: String, section: UASection) {
        self.title = title
        self.text = text
        super.init(section: section)
    }
    
    override func buildCell(tableView: UITableView, index: Int, indexPath: NSIndexPath) -> UITableViewCell {
        let res = tableView.dequeueReusableCellWithIdentifier(UABaseTableData.ReuseTitledCell, forIndexPath: indexPath) as! TitledCell
        res.setTitle(title, content: text)
        return res
    }
    
    override func cellHeight(index: Int, width: CGFloat) -> CGFloat {
        return 55
    }
    
    override func canCopy(index: Int) -> Bool {
        return true
    }
    
    override func copy(index: Int) {
        // Implemented in cell
    }
}

class UACommonCellRegion : UARegion {
    
    private var closure: ((cell: CommonCell) -> ())?
    private var actionClosure: (() -> Bool)?
    
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
    
    func setAction(actionClosure: (() -> Bool)) -> UACommonCellRegion {
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
    
    override func cellHeight(index: Int, width: CGFloat) -> CGFloat {
        return 44.0
    }
    
    override func canSelect(index: Int) -> Bool {
        return actionClosure != nil
    }
    
    override func select(index: Int) -> Bool {
        return actionClosure!()
    }
    
    override func buildCell(tableView: UITableView, index: Int, indexPath: NSIndexPath) -> UITableViewCell {
        let res = tableView
            .dequeueReusableCellWithIdentifier(
                UABaseTableData.ReuseCommonCell,
                forIndexPath: indexPath)
            as! CommonCell
        
        res.selectionStyle = canSelect(index) ? UITableViewCellSelectionStyle.Blue : UITableViewCellSelectionStyle.None
        res.separatorInset = UIEdgeInsets(top: 0, left: CGFloat(leftInset), bottom: 0, right: 0)
        
        if (content != nil) {
            res.setContent(NSLocalizedString(content!, comment: "Cell Title"))
        }
        if (style != nil) {
            res.style = style!
        }

        res.bottomSeparatorVisible = bottomSeparator
        res.bottomSeparatorLeftInset = CGFloat(bottomSeparatorLeftInset)
        
        res.topSeparatorVisible = topSeparator
        res.topSeparatorLeftInset = CGFloat(topSeparatorLeftInset)
        
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

