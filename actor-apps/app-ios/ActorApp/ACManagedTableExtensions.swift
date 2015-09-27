//
//  Copyright (c) 2014-2015 Actor LLC. <https://actor.im>
//

import Foundation


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
        let res = tableView.dequeueReusableCellWithIdentifier(ACManagedTable.ReuseTextCell, forIndexPath: indexPath) as! TextCell
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
        let res = tableView.dequeueReusableCellWithIdentifier(ACManagedTable.ReuseTitledCell, forIndexPath: indexPath) as! TitledCell
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
        let res = tableView.dequeueReusableCellWithIdentifier(ACManagedTable.ReuseCommonCell, forIndexPath: indexPath) as! CommonCell
        
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
