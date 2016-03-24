//
//  Copyright (c) 2014-2016 Actor LLC. <https://actor.im>
//

import Foundation

// Edit Row

public class AAEditRow: AAManagedRow, UITextFieldDelegate {
    
    public var text: String?
    public var placeholder: String?
    public var returnKeyType = UIReturnKeyType.Default
    public var autocorrectionType = UITextAutocorrectionType.Default
    public var autocapitalizationType = UITextAutocapitalizationType.Sentences
    public var returnAction: (()->())?
    
    public override func rangeCellHeightForItem(table: AAManagedTable, indexPath: AARangeIndexPath) -> CGFloat {
        return 44
    }
    
    public override func rangeCellForItem(table: AAManagedTable, indexPath: AARangeIndexPath) -> UITableViewCell {
        let res = table.dequeueCell(indexPath.indexPath) as AAEditCell
        res.textField.text = text
        
        if placeholder != nil {
            let placeholderText = NSMutableAttributedString(string: placeholder!)
            placeholderText.addAttribute(NSForegroundColorAttributeName, value: ActorSDK.sharedActor().style.cellHintColor, range:  NSMakeRange(0, placeholder!.length))
            res.textField.attributedPlaceholder = placeholderText
        } else {
            res.textField.placeholder = nil
        }
    
        res.textField.returnKeyType = returnKeyType
        res.textField.autocapitalizationType = autocapitalizationType
        res.textField.delegate = self
        res.textField.removeTarget(nil, action: nil, forControlEvents: .AllEvents)
        res.textField.addTarget(self, action: #selector(AAEditRow.textFieldDidChange(_:)), forControlEvents: .EditingChanged)
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

public extension AAManagedSection {
    
    public func edit(@noescape closure: (r: AAEditRow) -> ()) -> AAEditRow {
        let r = AAEditRow()
        regions.append(r)
        closure(r: r)
        r.initTable(self.table)
        return r
    }
}

// Titled Row

public class AATitledRow: AAManagedRow {
    
    public var title: String?
    public var subtitle: String?
    
    public var isAction: Bool = false
    public var accessoryType = UITableViewCellAccessoryType.None
    
    public var bindAction: ((r: AATitledRow)->())?
    
    // Cell
    
    public override func rangeCellHeightForItem(table: AAManagedTable, indexPath: AARangeIndexPath) -> CGFloat {
        return 55
    }
    
    public override func rangeCellForItem(table: AAManagedTable, indexPath: AARangeIndexPath) -> UITableViewCell {
        let res = table.dequeueTitledCell(indexPath.indexPath)
        bindCell(res)
        return res
    }
    
    public func bindCell(res: AATitledCell) {
        res.titleLabel.text = title
        res.contentLabel.text = subtitle
        res.accessoryType = accessoryType
        
        if isAction {
            res.contentLabel.textColor = ActorSDK.sharedActor().style.vcHintColor
        } else {
            res.contentLabel.textColor = ActorSDK.sharedActor().style.cellTextColor
        }

    }
    
    // Copy
    
    public override func rangeCopyData(table: AAManagedTable, indexPath: AARangeIndexPath) -> String? {
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

public extension AAManagedSection {
    
    private func titled() -> AATitledRow {
        let r = AATitledRow()
        let itemsCount = numberOfItems(table)
        regions.append(r)
        r.indexPath = NSIndexPath(forRow: itemsCount, inSection: index)
        r.section = self
        r.initTable(self.table)
        return r
    }

    public func titled(@noescape closure: (r: AATitledRow) -> ()) -> AATitledRow {
        let r = titled()
        closure(r: r)
        r.initTable(self.table)
        return r
    }
    
    public func titled(title: String, @noescape closure: (r: AATitledRow) -> ()) -> AATitledRow {
        let r = titled()
        r.title = AALocalized(title)
        closure(r: r)
        r.initTable(self.table)
        return r
    }
    
    public func titled(title: String, content: String) -> AATitledRow {
        let r = titled()
        r.title = AALocalized(title)
        r.subtitle = content
        r.initTable(self.table)
        return r
    }
}

// Text Row

public class AATextRow: AAManagedRow {
    
    public var title: String?
    public var content: String?
    
    public var isAction: Bool = false
    public var navigate: Bool = false
    
    public var bindAction: ((r: AATextRow)->())?
    
    // Cell
    
    public override func rangeCellHeightForItem(table: AAManagedTable, indexPath: AARangeIndexPath) -> CGFloat {
        return AATextCell.measure(content!, width: table.tableView.width, enableNavigation: navigate)
    }
    
    public override func rangeCellForItem(table: AAManagedTable, indexPath: AARangeIndexPath) -> UITableViewCell {
        let res = table.dequeueTextCell(indexPath.indexPath)
        res.setContent(title, content: content, isAction: isAction)
        if navigate {
            res.accessoryType = UITableViewCellAccessoryType.DisclosureIndicator
        } else {
            res.accessoryType = UITableViewCellAccessoryType.None
        }
        return res
    }
    
    // Copy
    
    public override func rangeCopyData(table: AAManagedTable, indexPath: AARangeIndexPath) -> String? {
        return isAction ? nil : content
    }
    
    // Binding
    
    public override func reload() {
        
        bindAction?(r: self)
        
        super.reload()
    }
}

public extension AAManagedSection {
    
    private func text() -> AATextRow {
        let r = AATextRow()
        let itemsCount = numberOfItems(table)
        regions.append(r)
        r.indexPath = NSIndexPath(forRow: itemsCount, inSection: index)
        r.section = self
        r.initTable(self.table)
        return r
    }
    
    public func text(@noescape closure: (r: AATextRow) -> ()) -> AATextRow {
        let r = text()
        closure(r: r)
        r.bindAction?(r: r)
        r.initTable(self.table)
        return r
    }
    
    public func text(title: String, @noescape closure: (r: AATextRow) -> ()) -> AATextRow {
        let r = text()
        r.title = AALocalized(title)
        closure(r: r)
        r.bindAction?(r: r)
        r.initTable(self.table)
        return r
    }
    
    public func text(title: String, content: String) -> AATextRow {
        let r = text()
        r.title = AALocalized(title)
        r.content = content
        r.initTable(self.table)
        return r
    }
}


// Header Row

public class AAHeaderRow: AAManagedRow {
    
    public var height: CGFloat = 40
    public var title: String?
    public var icon: UIImage?
    
    public override func rangeCellHeightForItem(table: AAManagedTable, indexPath: AARangeIndexPath) -> CGFloat {
        return height
    }
    
    public override func rangeCellForItem(table: AAManagedTable, indexPath: AARangeIndexPath) -> UITableViewCell {
        let res = table.dequeueCell(indexPath.indexPath) as AAHeaderCell
        
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
            res.iconView.image = icon!.tintImage(ActorSDK.sharedActor().style.cellHeaderColor)
        }
        
        return res
    }
}

public extension AAManagedSection {
    
    private func header() -> AAHeaderRow {
        let r = AAHeaderRow()
        regions.append(r)
        r.section = self
        r.initTable(self.table)
        return r
    }
    
    public func header(title: String) -> AAHeaderRow {
        let r = header()
        r.title = title
        r.initTable(self.table)
        return r
    }
}


// Common Row

public class AACommonRow: AAManagedRow {
    
    public var style: AACommonCellStyle = .Normal
    public var hint: String?
    public var content: String?
    public var switchOn: Bool = false
    public var switchAction: ((v: Bool) -> ())?
    
    public var bindAction: ((r: AACommonRow)->())?
    
    public var contentInset: CGFloat = 15
    
    // Cell
    
    public override func rangeCellHeightForItem(table: AAManagedTable, indexPath: AARangeIndexPath) -> CGFloat {
        return 44
    }
    
    public override func rangeCellForItem(table: AAManagedTable, indexPath: AARangeIndexPath) -> UITableViewCell {
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

public extension AAManagedSection {

    // Common cell
    
    private func common() -> AACommonRow {
        let r = AACommonRow()
        let itemsCount = numberOfItems(table)
        regions.append(r)
        r.indexPath = NSIndexPath(forRow: itemsCount, inSection: index)
        r.section = self
        r.initTable(self.table)
        return r
    }
    
    public func common(@noescape closure: (r: AACommonRow) -> ()) -> AACommonRow {
        let r = common()
        closure(r: r)
        r.bindAction?(r: r)
        r.initTable(self.table)
        return r
    }
    
    // Action cell
    
    public func action(@noescape closure: (r: AACommonRow) -> ()) -> AACommonRow {
        let r = common()
        r.style = .Action
        closure(r: r)
        r.bindAction?(r: r)
        r.initTable(self.table)
        return r
    }
    
    public func action(content: String, @noescape closure: (r: AACommonRow) -> ()) -> AACommonRow {
        let r = common()
        r.content = AALocalized(content)
        r.style = .Action
        closure(r: r)
        r.bindAction?(r: r)
        r.initTable(self.table)
        return r
    }
    
    // Navigation cell
    
    public func navigate(@noescape closure: (r: AACommonRow) -> ()) -> AACommonRow {
        let r = common()
        r.style = .Navigation
        closure(r: r)
        r.bindAction?(r: r)
        r.initTable(self.table)
        return r
    }
    
    public func navigate(content: String, @noescape closure: (r: AACommonRow) -> ()) -> AACommonRow {
        let r = common()
        r.content = AALocalized(content)
        r.style = .Navigation
        closure(r: r)
        r.bindAction?(r: r)
        r.initTable(self.table)
        return r
    }
    
    public func navigate(content: String, controller: UIViewController.Type) -> AACommonRow {
        let r = common()
        r.content = AALocalized(content)
        r.style = .Navigation
        r.selectAction = { () -> Bool in
            self.table.controller.navigateNext(controller.init())
            return false
        }
        r.bindAction?(r: r)
        r.initTable(self.table)
        return r
    }
    
    // Danger
    
    public func danger(content: String, @noescape closure: (r: AACommonRow) -> ()) -> AACommonRow {
        let r = common()
        r.content = AALocalized(content)
        r.style = .Destructive
        closure(r: r)
        r.bindAction?(r: r)
        r.initTable(self.table)
        return r
    }
    
    // Content
    
    func url(content: String, url: String) -> AACommonRow {
        let r = common()
        r.content = AALocalized(content)
        r.style = .Navigation
        r.selectAction = { () -> Bool in
            if let u = NSURL(string: url) {
                UIApplication.sharedApplication().openURL(u)
            }
            return true
        }
        r.initTable(self.table)
        return r
    }
    
    public func hint(content: String) -> AACommonRow {
        let r = common()
        r.content = AALocalized(content)
        r.style = .Hint
        r.bindAction?(r: r)
        r.initTable(self.table)
        return r
    }

}

// Custom cell

public class AACustomRow<T where T: UITableViewCell>: AAManagedRow {
    
    public var height: CGFloat = 44
    public var closure: ((cell: T) -> ())?
    
    public var bindAction: ((r: AACustomRow<T>)->())?
    
    // Cell
    
    public override func rangeCellHeightForItem(table: AAManagedTable, indexPath: AARangeIndexPath) -> CGFloat {
        return height
    }
    
    public override func rangeCellForItem(table: AAManagedTable, indexPath: AARangeIndexPath) -> UITableViewCell {
        let res: T = table.dequeueCell(indexPath.indexPath)
        rangeBindCellForItem(table, indexPath: indexPath, cell: res)
        return res
    }
    
    public func rangeBindCellForItem(table: AAManagedTable, indexPath: AARangeIndexPath, cell: T) {
        closure?(cell: cell)
    }
    
    // Binding
    
    public override func reload() {
        
        bindAction?(r: self)
        
        super.reload()
    }
}

public extension AAManagedSection {
    
    private func custom<T where T: UITableViewCell>() -> AACustomRow<T> {
        let r = AACustomRow<T>()
        let itemsCount = numberOfItems(table)
        regions.append(r)
        r.indexPath = NSIndexPath(forRow: itemsCount, inSection: index)
        r.section = self
        r.initTable(self.table)
        return r
    }
    
    public func custom<T where T: UITableViewCell>(@noescape closure: (r: AACustomRow<T>) -> ()) -> AACustomRow<T> {
        let r: AACustomRow<T> = custom()
        closure(r: r)
        r.bindAction?(r: r)
        r.initTable(self.table)
        return r
    }
}

// Avatar Row

public class AAAvatarRow: AAManagedRow {
    
    public var id: Int?
    public var title: String?
    
    public var avatar: ACAvatar?
    public var avatarPath: String?
    public var avatarLoading: Bool = false
    
    public var subtitleHidden: Bool = false
    public var subtitle: String?
    public var subtitleColor: UIColor?
    
    public var bindAction: ((r: AAAvatarRow)->())?
    
    public var avatarDidTap: ((view: UIView) -> ())?
    
    // Cell
    
    public override func rangeCellHeightForItem(table: AAManagedTable, indexPath: AARangeIndexPath) -> CGFloat {
        return 92
    }
    
    public override func rangeCellForItem(table: AAManagedTable, indexPath: AARangeIndexPath) -> UITableViewCell {
        let res: AAAvatarCell = table.dequeueCell(indexPath.indexPath)
        bindCell(res)
        return res
    }
    
    public func bindCell(res: AAAvatarCell) {
        res.titleLabel.text = title
        
        res.subtitleLabel.hidden = subtitleHidden
        res.subtitleLabel.text = subtitle
        
        if avatarPath != nil {
            res.avatarView.bind(title!, id: id!, fileName: avatarPath!)
        } else {
            res.avatarView.bind(title!, id: id!, avatar: avatar)
        }
        
        if subtitleColor != nil {
            res.subtitleLabel.textColor = subtitleColor!
        } else {
            res.subtitleLabel.textColor = ActorSDK.sharedActor().style.cellTextColor
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
            if let cell = s.table.tableView.visibleCellForIndexPath(p) as? AAAvatarCell {
                bindCell(cell)
            }
        }
    }
}

public extension AAManagedSection {
    
    private func addAvatar() -> AAAvatarRow {
        let r = AAAvatarRow()
        let itemsCount = numberOfItems(table)
        regions.append(r)
        r.indexPath = NSIndexPath(forRow: itemsCount, inSection: index)
        r.section = self
        r.initTable(self.table)
        return r
    }
    
    public func avatar(@noescape closure: (r: AAAvatarRow) -> ()) -> AAAvatarRow {
        let r: AAAvatarRow = addAvatar()
        closure(r: r)
        r.bindAction?(r: r)
        r.initTable(self.table)
        return r
    }
}

// Arrays

public class AAManagedArrayRows<T, R where R: UITableViewCell>: AAManagedRange {
    
    public var height: CGFloat = 44
    public var selectAction: ((t: T) -> Bool)?
    public var bindCopy: ((t: T) -> String?)?
    public var section: AAManagedSection?
    
    public var bindData: ((cell: R, item: T) -> ())?
    
    public var data = [T]()
    
    public func initTable(table: AAManagedTable) {
        
    }
    
    // Number of items
    
    public func rangeNumberOfItems(table: AAManagedTable) -> Int {
        return data.count
    }
    
    // Cells
    
    public func rangeCellHeightForItem(table: AAManagedTable, indexPath: AARangeIndexPath) -> CGFloat {
        return height
    }
    
    public func rangeCellForItem(table: AAManagedTable, indexPath: AARangeIndexPath) -> UITableViewCell {
        let res: R = table.dequeueCell(indexPath.indexPath)
        rangeBindData(table, indexPath: indexPath, cell: res, item: data[indexPath.item])
        return res
    }
    
    public func rangeBindData(table: AAManagedTable, indexPath: AARangeIndexPath, cell: R, item: T) {
        bindData?(cell: cell, item: item)
    }
    
    // Selection
    
    public func rangeCanSelect(table: AAManagedTable, indexPath: AARangeIndexPath) -> Bool {
        return selectAction != nil
    }
    
    public func rangeSelect(table: AAManagedTable, indexPath: AARangeIndexPath) -> Bool {
        return selectAction!(t: data[indexPath.item])
    }
    
    // Copy
    
    public func rangeCanCopy(table: AAManagedTable, indexPath: AARangeIndexPath) -> Bool {
        if bindCopy != nil {
            return bindCopy!(t: data[indexPath.item]) != nil
        }
        return false
    }
    
    public func rangeCopy(table: AAManagedTable, indexPath: AARangeIndexPath) {
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

public extension AAManagedSection {
    
    private func addArrays<T, R where R: UITableViewCell>() -> AAManagedArrayRows<T, R> {
        let r = AAManagedArrayRows<T, R>()
        regions.append(r)
        r.section = self
        r.initTable(self.table)
        return r
    }
    
    public func arrays<T, R where R: UITableViewCell>(@noescape closure: (r: AAManagedArrayRows<T,R>) -> ()) -> AAManagedArrayRows<T, R> {
        let res: AAManagedArrayRows<T, R> = addArrays()
        closure(r: res)
        res.initTable(self.table)
        return res
    }
}


// Single item row

public class AAManagedRow: NSObject, AAManagedRange {
    
    public var selectAction: (() -> Bool)?
    
    public var section: AAManagedSection?
    public var indexPath: NSIndexPath?
    
    public func initTable(table: AAManagedTable) {
        
    }
    
    // Number of items
    
    public func rangeNumberOfItems(table: AAManagedTable) -> Int {
        return 1
    }
    
    // Cells
    
    public func rangeCellHeightForItem(table: AAManagedTable, indexPath: AARangeIndexPath) -> CGFloat {
        return 44
    }
    
    public func rangeCellForItem(table: AAManagedTable, indexPath: AARangeIndexPath) -> UITableViewCell {
        fatalError("Not implemented")
    }
    
    // Selection
    
    public func rangeCanSelect(table: AAManagedTable, indexPath: AARangeIndexPath) -> Bool {
        return selectAction != nil
    }
    
    public func rangeSelect(table: AAManagedTable, indexPath: AARangeIndexPath) -> Bool {
        return selectAction!()
    }
    
    // Copying
    
    public func rangeCanCopy(table: AAManagedTable, indexPath: AARangeIndexPath) -> Bool {
        return rangeCopyData(table, indexPath: indexPath) != nil
    }
    
    public func rangeCopy(table: AAManagedTable, indexPath: AARangeIndexPath) {
        UIPasteboard.generalPasteboard().string = rangeCopyData(table, indexPath: indexPath)
    }
    
    public func rangeCopyData(table: AAManagedTable, indexPath: AARangeIndexPath) -> String? {
        return nil
    }
    
    public func reload() {
        if let p = indexPath, let s = section {
            s.table.tableView.reloadRowsAtIndexPaths([p], withRowAnimation: .Automatic)
        }
    }
}