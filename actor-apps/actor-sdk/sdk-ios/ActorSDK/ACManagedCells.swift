//
//  Copyright (c) 2014-2015 Actor LLC. <https://actor.im>
//

import Foundation

// Edit Row

public class ACEditRow: ACManagedRow, UITextFieldDelegate {
    
    public var text: String?
    public var placeholder: String?
    public var returnKeyType = UIReturnKeyType.Default
    public var autocorrectionType = UITextAutocorrectionType.Default
    public var autocapitalizationType = UITextAutocapitalizationType.Sentences
    public var returnAction: (()->())?
    
    public override func rangeCellHeightForItem(table: ACManagedTable, indexPath: ACRangeIndexPath) -> CGFloat {
        return 44
    }
    
    public override func rangeCellForItem(table: ACManagedTable, indexPath: ACRangeIndexPath) -> UITableViewCell {
        let res = table.dequeueCell(indexPath.indexPath) as EditCell
        res.textField.text = text
        res.textField.placeholder = placeholder
        res.textField.returnKeyType = returnKeyType
        res.textField.autocapitalizationType = autocapitalizationType
        res.textField.delegate = self
        res.textField.removeTarget(nil, action: nil, forControlEvents: .AllEvents)
        res.textField.addTarget(self, action: "textFieldDidChange:", forControlEvents: .EditingChanged)
        return res
    }
    
    public func textFieldDidChange(textField: UITextField) {
        text = textField.text
    }
    
    @objc public func textFieldDidEndEditing(textField: UITextField) {
        text = textField.text
    }
    
    public func textFieldShouldReturn(textField: UITextField) -> Bool {
        returnAction?()
        return false
    }
}

public extension ACManagedSection {
    
    public func edit(closure: (r: ACEditRow) -> ()) -> ACEditRow {
        let r = ACEditRow()
        regions.append(r)
        closure(r: r)
        return r
    }
}

// Titled Row

public class ACTitledRow: ACManagedRow {
    
    public var title: String?
    public var subtitle: String?
    
    public var isAction: Bool = false
    public var accessoryType = UITableViewCellAccessoryType.None
    
    public var bindAction: ((r: ACTitledRow)->())?
    
    // Cell
    
    public override func rangeCellHeightForItem(table: ACManagedTable, indexPath: ACRangeIndexPath) -> CGFloat {
        return 55
    }
    
    public override func rangeCellForItem(table: ACManagedTable, indexPath: ACRangeIndexPath) -> UITableViewCell {
        let res = table.dequeueTitledCell(indexPath.indexPath)
        bindCell(res)
        return res
    }
    
    public func bindCell(res: AATitledCell) {
        res.titleLabel.text = title
        res.contentLabel.text = subtitle
        res.accessoryType = accessoryType
        
        if isAction {
            res.contentLabel.textColor = MainAppTheme.list.actionColor
        } else {
            res.contentLabel.textColor = MainAppTheme.list.textColor
        }

    }
    
    // Copy
    
    public override func rangeCopyData(table: ACManagedTable, indexPath: ACRangeIndexPath) -> String? {
        return isAction ? nil : subtitle
    }
    
    // Binding
    
    public override func reload() {
        
        bindAction?(r: self)
        
        if let p = indexPath, let s = section {
            if let cell = s.table.tableView.cellForRowAtIndexPath(p) as? AATitledCell {
                bindCell(cell)
            }
        }
    }
}

public extension ACManagedSection {
    
    private func titled() -> ACTitledRow {
        let r = ACTitledRow()
        let itemsCount = numberOfItems(table)
        regions.append(r)
        r.indexPath = NSIndexPath(forRow: itemsCount, inSection: index)
        r.section = self
        return r
    }

    public func titled(closure: (r: ACTitledRow) -> ()) -> ACTitledRow {
        let r = titled()
        closure(r: r)
        return r
    }
    
    public func titled(title: String, closure: (r: ACTitledRow) -> ()) -> ACTitledRow {
        let r = titled()
        r.title = localized(title)
        closure(r: r)
        return r
    }
    
    public func titled(title: String, content: String) -> ACTitledRow {
        let r = titled()
        r.title = localized(title)
        r.subtitle = content
        return r
    }
}

// Text Row

public class ACTextRow: ACManagedRow {
    
    public var title: String?
    public var content: String?
    
    public var isAction: Bool = false
    public var navigate: Bool = false
    
    public var bindAction: ((r: ACTextRow)->())?
    
    // Cell
    
    public override func rangeCellHeightForItem(table: ACManagedTable, indexPath: ACRangeIndexPath) -> CGFloat {
        return AATextCell.measure(content!, width: table.tableView.width, enableNavigation: navigate)
    }
    
    public override func rangeCellForItem(table: ACManagedTable, indexPath: ACRangeIndexPath) -> UITableViewCell {
        let res = table.dequeueTextCell(indexPath.indexPath)
        res.setTitle(title, content: content)
        res.setAction(isAction)
        if navigate {
            res.accessoryType = UITableViewCellAccessoryType.DisclosureIndicator
        } else {
            res.accessoryType = UITableViewCellAccessoryType.None
        }
        return res
    }
    
    // Copy
    
    public override func rangeCopyData(table: ACManagedTable, indexPath: ACRangeIndexPath) -> String? {
        return isAction ? nil : content
    }
    
    // Binding
    
    public override func reload() {
        
        bindAction?(r: self)
        
        super.reload()
    }
}

public extension ACManagedSection {
    
    private func text() -> ACTextRow {
        let r = ACTextRow()
        let itemsCount = numberOfItems(table)
        regions.append(r)
        r.indexPath = NSIndexPath(forRow: itemsCount, inSection: index)
        r.section = self
        return r
    }
    
    public func text(closure: (r: ACTextRow) -> ()) -> ACTextRow {
        let r = text()
        closure(r: r)
        r.bindAction?(r: r)
        return r
    }
    
    public func text(title: String, closure: (r: ACTextRow) -> ()) -> ACTextRow {
        let r = text()
        r.title = localized(title)
        closure(r: r)
        r.bindAction?(r: r)
        return r
    }
    
    public func text(title: String, content: String) -> ACTextRow {
        let r = text()
        r.title = localized(title)
        r.content = content
        return r
    }
}


// Header Row

public class ACHeaderRow: ACManagedRow {
    
    public var height: CGFloat = 40
    public var title: String?
    public var icon: UIImage?
    
    public override func rangeCellHeightForItem(table: ACManagedTable, indexPath: ACRangeIndexPath) -> CGFloat {
        return height
    }
    
    public override func rangeCellForItem(table: ACManagedTable, indexPath: ACRangeIndexPath) -> UITableViewCell {
        let res = table.dequeueCell(indexPath.indexPath) as HeaderCell
        
        if title == nil {
            res.titleView.hidden = true
        } else {
            res.titleView.text = title
            res.titleView.hidden = false
        }
        
        if icon == nil {
            res.iconView.hidden = true
        } else {
            res.iconView.hidden = false
            res.iconView.image = icon!.tintImage(MainAppTheme.list.sectionColor)
        }
        
        return res
    }
}

public extension ACManagedSection {
    
    private func header() -> ACHeaderRow {
        let r = ACHeaderRow()
        regions.append(r)
        r.section = self
        return r
    }
    
    public func header(title: String) -> ACHeaderRow {
        let r = header()
        r.title = title
        return r
    }
}


// Common Row

public class ACCommonRow: ACManagedRow {
    
    public var style: CommonCellStyle = .Normal
    public var hint: String?
    public var content: String?
    public var switchOn: Bool = false
    public var switchAction: ((v: Bool) -> ())?
    
    public var bindAction: ((r: ACCommonRow)->())?
    
    public var contentInset: CGFloat = 15
    
    // Cell
    
    public override func rangeCellHeightForItem(table: ACManagedTable, indexPath: ACRangeIndexPath) -> CGFloat {
        return 44
    }
    
    public override func rangeCellForItem(table: ACManagedTable, indexPath: ACRangeIndexPath) -> UITableViewCell {
        let res = table.dequeueCommonCell(indexPath.indexPath)
        bindCell(res)
        return res
    }
    
    public func bindCell(res: AACommonCell) {
        res.style = style
        res.setContent(content)
        res.setHint(hint)
        res.switchBlock = switchAction
        res.setSwitcherOn(switchOn, animated: true)
        res.contentInset = contentInset
        
        if selectAction != nil {
            res.selectionStyle = .Default
        } else {
            res.selectionStyle = .None
        }
    }
    
    // Binding
    
    public override func reload() {
        
        bindAction?(r: self)
        
        if let p = indexPath, let s = section {
            if let cell = s.table.tableView.cellForRowAtIndexPath(p) as? AACommonCell {
                bindCell(cell)
            }
        }
    }
}

public extension ACManagedSection {

    // Common cell
    
    private func common() -> ACCommonRow {
        let r = ACCommonRow()
        let itemsCount = numberOfItems(table)
        regions.append(r)
        r.indexPath = NSIndexPath(forRow: itemsCount, inSection: index)
        r.section = self
        return r
    }
    
    public func common(closure: (r: ACCommonRow) -> ()) -> ACCommonRow {
        let r = common()
        closure(r: r)
        r.bindAction?(r: r)
        return r
    }
    
    // Action cell
    
    public func action(closure: (r: ACCommonRow) -> ()) -> ACCommonRow {
        let r = common()
        r.style = .Action
        closure(r: r)
        r.bindAction?(r: r)
        return r
    }
    
    public func action(content: String, closure: (r: ACCommonRow) -> ()) -> ACCommonRow {
        let r = common()
        r.content = localized(content)
        r.style = .Action
        closure(r: r)
        r.bindAction?(r: r)
        return r
    }
    
    // Navigation cell
    
    public func navigate(closure: (r: ACCommonRow) -> ()) -> ACCommonRow {
        let r = common()
        r.style = .Navigation
        closure(r: r)
        r.bindAction?(r: r)
        return r
    }
    
    public func navigate(content: String, closure: (r: ACCommonRow) -> ()) -> ACCommonRow {
        let r = common()
        r.content = localized(content)
        r.style = .Navigation
        closure(r: r)
        r.bindAction?(r: r)
        return r
    }
    
    public func navigate(content: String, controller: UIViewController.Type) -> ACCommonRow {
        let r = common()
        r.content = localized(content)
        r.style = .Navigation
        r.selectAction = { () -> Bool in
            self.table.controller.navigateNext(controller.init())
            return false
        }
        r.bindAction?(r: r)
        return r
    }
    
    // Danger
    
    public func danger(content: String, closure: (r: ACCommonRow) -> ()) -> ACCommonRow {
        let r = common()
        r.content = localized(content)
        r.style = .Destructive
        closure(r: r)
        r.bindAction?(r: r)
        return r
    }
    
    // Content
    
    func url(content: String, url: String) -> ACCommonRow {
        let r = common()
        r.content = localized(content)
        r.style = .Navigation
        r.selectAction = { () -> Bool in
            if let u = NSURL(string: url) {
                UIApplication.sharedApplication().openURL(u)
            }
            return true
        }
        return r
    }
    
    public func hint(content: String) -> ACCommonRow {
        let r = common()
        r.content = localized(content)
        r.style = .Hint
        r.bindAction?(r: r)
        return r
    }

}

// Custom cell

public class ACCustomRow<T where T: UITableViewCell>: ACManagedRow {
    
    public var height: CGFloat = 44
    public var closure: ((cell: T) -> ())?
    
    public var bindAction: ((r: ACCustomRow<T>)->())?
    
    // Cell
    
    public override func rangeCellHeightForItem(table: ACManagedTable, indexPath: ACRangeIndexPath) -> CGFloat {
        return height
    }
    
    public override func rangeCellForItem(table: ACManagedTable, indexPath: ACRangeIndexPath) -> UITableViewCell {
        let res: T = table.dequeueCell(indexPath.indexPath)
        rangeBindCellForItem(table, indexPath: indexPath, cell: res)
        return res
    }
    
    public func rangeBindCellForItem(table: ACManagedTable, indexPath: ACRangeIndexPath, cell: T) {
        closure?(cell: cell)
    }
    
    // Binding
    
    public override func reload() {
        
        bindAction?(r: self)
        
        super.reload()
    }
}

public extension ACManagedSection {
    
    private func custom<T where T: UITableViewCell>() -> ACCustomRow<T> {
        let r = ACCustomRow<T>()
        let itemsCount = numberOfItems(table)
        regions.append(r)
        r.indexPath = NSIndexPath(forRow: itemsCount, inSection: index)
        r.section = self
        return r
    }
    
    public func custom<T where T: UITableViewCell>(closure: (r: ACCustomRow<T>) -> ()) -> ACCustomRow<T> {
        let r: ACCustomRow<T> = custom()
        closure(r: r)
        r.bindAction?(r: r)
        return r
    }
}

// Avatar Row

public class ACAvatarRow: ACManagedRow {
    
    public var id: Int?
    public var title: String?
    
    public var avatar: ACAvatar?
    public var avatarPath: String?
    public var avatarLoading: Bool = false
    
    public var subtitle: String?
    public var subtitleStyle: String?
    
    public var bindAction: ((r: ACAvatarRow)->())?
    
    public var avatarDidTap: ((view: UIView) -> ())?
    
    // Cell
    
    public override func rangeCellHeightForItem(table: ACManagedTable, indexPath: ACRangeIndexPath) -> CGFloat {
        return 92
    }
    
    public override func rangeCellForItem(table: ACManagedTable, indexPath: ACRangeIndexPath) -> UITableViewCell {
        let res: AvatarCell = table.dequeueCell(indexPath.indexPath)
        bindCell(res)
        return res
    }
    
    public func bindCell(res: AvatarCell) {
        res.titleLabel.text = title
        
        if subtitle != nil {
            res.subtitleLabel.text = subtitle
            if subtitleStyle != nil {
                res.subtitleLabel.applyStyle(subtitleStyle!)
            }
            res.subtitleLabel.hidden = false
        } else {
            res.subtitleLabel.hidden = true
        }
        
        if avatarPath != nil {
            res.avatarView.bind(title!, id: jint(id!), fileName: avatarPath!)
        } else {
            res.avatarView.bind(title!, id: jint(id!), avatar: avatar, clearPrev: false)
        }
        
        res.progress.hidden = !avatarLoading
        if avatarLoading {
            res.progress.startAnimating()
        } else {
            res.progress.stopAnimating()
        }
        
        res.didTap = avatarDidTap
    }
    
    // Binding
    
    public override func reload() {
        
        bindAction?(r: self)
        
        if let p = indexPath, let s = section {
            if let cell = s.table.tableView.cellForRowAtIndexPath(p) as? AvatarCell {
                bindCell(cell)
            }
        }
    }
}

public extension ACManagedSection {
    
    private func addAvatar() -> ACAvatarRow {
        let r = ACAvatarRow()
        let itemsCount = numberOfItems(table)
        regions.append(r)
        r.indexPath = NSIndexPath(forRow: itemsCount, inSection: index)
        r.section = self
        return r
    }
    
    public func avatar(closure: (r: ACAvatarRow) -> ()) -> ACAvatarRow {
        let r: ACAvatarRow = addAvatar()
        closure(r: r)
        r.bindAction?(r: r)
        return r
    }
}

// Arrays

public class ACManagedArrayRows<T, R where R: UITableViewCell>: ACManagedRange {
    
    public var height: CGFloat = 44
    public var selectAction: ((t: T) -> Bool)?
    public var bindCopy: ((t: T) -> String?)?
    public var section: ACManagedSection?
    
    public var bindData: ((cell: R, item: T) -> ())?
    
    public var data = [T]()
    
    // Number of items
    
    public func rangeNumberOfItems(table: ACManagedTable) -> Int {
        return data.count
    }
    
    // Cells
    
    public func rangeCellHeightForItem(table: ACManagedTable, indexPath: ACRangeIndexPath) -> CGFloat {
        return height
    }
    
    public func rangeCellForItem(table: ACManagedTable, indexPath: ACRangeIndexPath) -> UITableViewCell {
        let res: R = table.dequeueCell(indexPath.indexPath)
        rangeBindData(table, indexPath: indexPath, cell: res, item: data[indexPath.item])
        return res
    }
    
    public func rangeBindData(table: ACManagedTable, indexPath: ACRangeIndexPath, cell: R, item: T) {
        bindData?(cell: cell, item: item)
    }
    
    // Selection
    
    public func rangeCanSelect(table: ACManagedTable, indexPath: ACRangeIndexPath) -> Bool {
        return selectAction != nil
    }
    
    public func rangeSelect(table: ACManagedTable, indexPath: ACRangeIndexPath) -> Bool {
        return selectAction!(t: data[indexPath.item])
    }
    
    // Copy
    
    public func rangeCanCopy(table: ACManagedTable, indexPath: ACRangeIndexPath) -> Bool {
        if bindCopy != nil {
            return bindCopy!(t: data[indexPath.item]) != nil
        }
        return false
    }
    
    public func rangeCopy(table: ACManagedTable, indexPath: ACRangeIndexPath) {
        if let s = bindCopy!(t: data[indexPath.item]) {
            UIPasteboard.generalPasteboard().string = s
        }
    }
    
    // Reloading
    
    public func reload() {
        if let s = section {
            s.table.tableView.reloadSections(NSIndexSet(index: s.index), withRowAnimation: .Automatic)
        }
    }
}

public extension ACManagedSection {
    
    private func addArrays<T, R where R: UITableViewCell>() -> ACManagedArrayRows<T, R> {
        let r = ACManagedArrayRows<T, R>()
        regions.append(r)
        r.section = self
        return r
    }
    
    public func arrays<T, R where R: UITableViewCell>(closure: (r: ACManagedArrayRows<T,R>) -> ()) -> ACManagedArrayRows<T, R> {
        let res: ACManagedArrayRows<T, R> = addArrays()
        closure(r: res)
        return res
    }
}


// Single item row

public class ACManagedRow: NSObject, ACManagedRange {
    
    public var selectAction: (() -> Bool)?
    
    public var section: ACManagedSection?
    public var indexPath: NSIndexPath?
    
    // Number of items
    
    public func rangeNumberOfItems(table: ACManagedTable) -> Int {
        return 1
    }
    
    // Cells
    
    public func rangeCellHeightForItem(table: ACManagedTable, indexPath: ACRangeIndexPath) -> CGFloat {
        return 44
    }
    
    public func rangeCellForItem(table: ACManagedTable, indexPath: ACRangeIndexPath) -> UITableViewCell {
        fatalError("Not implemented")
    }
    
    // Selection
    
    public func rangeCanSelect(table: ACManagedTable, indexPath: ACRangeIndexPath) -> Bool {
        return selectAction != nil
    }
    
    public func rangeSelect(table: ACManagedTable, indexPath: ACRangeIndexPath) -> Bool {
        return selectAction!()
    }
    
    // Copying
    
    public func rangeCanCopy(table: ACManagedTable, indexPath: ACRangeIndexPath) -> Bool {
        return rangeCopyData(table, indexPath: indexPath) != nil
    }
    
    public func rangeCopy(table: ACManagedTable, indexPath: ACRangeIndexPath) {
        UIPasteboard.generalPasteboard().string = rangeCopyData(table, indexPath: indexPath)
    }
    
    public func rangeCopyData(table: ACManagedTable, indexPath: ACRangeIndexPath) -> String? {
        return nil
    }
    
    public func reload() {
        if let p = indexPath, let s = section {
            s.table.tableView.reloadRowsAtIndexPaths([p], withRowAnimation: .Automatic)
        }
    }
}