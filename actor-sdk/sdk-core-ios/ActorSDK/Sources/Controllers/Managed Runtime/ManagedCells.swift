//
//  Copyright (c) 2014-2016 Actor LLC. <https://actor.im>
//

import Foundation

// Edit Row

open class AAEditRow: AAManagedRow, UITextFieldDelegate {
    
    open var prefix: String?
    open var text: String?
    open var placeholder: String?
    open var returnKeyType = UIReturnKeyType.default
    open var autocorrectionType = UITextAutocorrectionType.default
    open var autocapitalizationType = UITextAutocapitalizationType.sentences
    open var returnAction: (()->())?
    
    open override func rangeCellHeightForItem(_ table: AAManagedTable, indexPath: AARangeIndexPath) -> CGFloat {
        return 44
    }
    
    open override func rangeCellForItem(_ table: AAManagedTable, indexPath: AARangeIndexPath) -> UITableViewCell {
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
        res.textField.removeTarget(nil, action: nil, for: .allEvents)
        res.textField.addTarget(self, action: #selector(AAEditRow.textFieldDidChange(_:)), for: .editingChanged)
        
        if prefix != nil {
            res.textPrefix.text = prefix
            res.textPrefix.isHidden = false
        } else {
            res.textPrefix.isHidden = true
        }
        
        return res
    }
    
    open func textFieldDidChange(_ textField: UITextField) {
        text = textField.text
    }
    
    @objc open func textFieldDidEndEditing(_ textField: UITextField) {
        text = textField.text
    }
    
    open func textFieldShouldReturn(_ textField: UITextField) -> Bool {
        returnAction?()
        return false
    }
}

public extension AAManagedSection {
    
    public func edit(_ closure: (_ r: AAEditRow) -> ()) -> AAEditRow {
        let r = AAEditRow()
        regions.append(r)
        closure(r)
        r.initTable(self.table)
        return r
    }
}

// Titled Row

open class AATitledRow: AAManagedRow {
    
    open var title: String?
    open var subtitle: String?
    
    open var isAction: Bool = false
    open var accessoryType = UITableViewCellAccessoryType.none
    
    open var bindAction: ((_ r: AATitledRow)->())?
    
    // Cell
    
    open override func rangeCellHeightForItem(_ table: AAManagedTable, indexPath: AARangeIndexPath) -> CGFloat {
        return 55
    }
    
    open override func rangeCellForItem(_ table: AAManagedTable, indexPath: AARangeIndexPath) -> UITableViewCell {
        let res = table.dequeueTitledCell(indexPath.indexPath)
        bindCell(res)
        return res
    }
    
    open func bindCell(_ res: AATitledCell) {
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
    
    open override func rangeCopyData(_ table: AAManagedTable, indexPath: AARangeIndexPath) -> String? {
        return isAction ? nil : subtitle
    }
    
    // Binding
    
    open override func reload() {
        
        bindAction?(self)
        
        if let p = indexPath, let s = section {
            if let cell = s.table.tableView.cellForRow(at: p) as? AATitledCell {
                bindCell(cell)
            }
        }
    }
}

public extension AAManagedSection {
    
    fileprivate func titled() -> AATitledRow {
        let r = AATitledRow()
        let itemsCount = numberOfItems(table)
        regions.append(r)
        r.indexPath = IndexPath(row: itemsCount, section: index)
        r.section = self
        r.initTable(self.table)
        return r
    }

    public func titled(_ closure: (_ r: AATitledRow) -> ()) -> AATitledRow {
        let r = titled()
        closure(r)
        r.initTable(self.table)
        return r
    }
    
    public func titled(_ title: String, closure: (_ r: AATitledRow) -> ()) -> AATitledRow {
        let r = titled()
        r.title = AALocalized(title)
        closure(r)
        r.initTable(self.table)
        return r
    }
    
    public func titled(_ title: String, content: String) -> AATitledRow {
        let r = titled()
        r.title = AALocalized(title)
        r.subtitle = content
        r.initTable(self.table)
        return r
    }
}

// Text Row

open class AATextRow: AAManagedRow {
    
    open var title: String?
    open var content: String?
    
    open var isAction: Bool = false
    open var navigate: Bool = false
    
    open var bindAction: ((_ r: AATextRow)->())?
    
    // Cell
    
    open override func rangeCellHeightForItem(_ table: AAManagedTable, indexPath: AARangeIndexPath) -> CGFloat {
        return AATextCell.measure(title, text: content!, width: table.tableView.width, enableNavigation: navigate)
    }
    
    open override func rangeCellForItem(_ table: AAManagedTable, indexPath: AARangeIndexPath) -> UITableViewCell {
        let res = table.dequeueTextCell(indexPath.indexPath)
        res.setContent(title, content: content, isAction: isAction)
        if navigate {
            res.accessoryType = UITableViewCellAccessoryType.disclosureIndicator
        } else {
            res.accessoryType = UITableViewCellAccessoryType.none
        }
        return res
    }
    
    // Copy
    
    open override func rangeCopyData(_ table: AAManagedTable, indexPath: AARangeIndexPath) -> String? {
        return isAction ? nil : content
    }
    
    // Binding
    
    open override func reload() {
        
        bindAction?(self)
        
        super.reload()
    }
}

public extension AAManagedSection {
    
    fileprivate func text() -> AATextRow {
        let r = AATextRow()
        let itemsCount = numberOfItems(table)
        regions.append(r)
        r.indexPath = IndexPath(row: itemsCount, section: index)
        r.section = self
        r.initTable(self.table)
        return r
    }
    
    public func text(_ closure: (_ r: AATextRow) -> ()) -> AATextRow {
        let r = text()
        closure(r)
        r.bindAction?(r)
        r.initTable(self.table)
        return r
    }
    
    public func text(_ title: String?, closure: (_ r: AATextRow) -> ()) -> AATextRow {
        let r = text()
        if title != nil {
            r.title = AALocalized(title!)
        }
        closure(r)
        r.bindAction?(r)
        r.initTable(self.table)
        return r
    }
    
    public func text(_ title: String?, content: String) -> AATextRow {
        let r = text()
        if title != nil {
            r.title = AALocalized(title!)
        }
        r.content = content
        r.initTable(self.table)
        return r
    }
    
    public func text(_ content: String) -> AATextRow {
        let r = text()
        r.title = nil
        r.content = content
        r.initTable(self.table)
        return r
    }
}


// Header Row

open class AAHeaderRow: AAManagedRow {
    
    open var height: CGFloat = 40
    open var title: String?
    open var icon: UIImage?
    
    open override func rangeCellHeightForItem(_ table: AAManagedTable, indexPath: AARangeIndexPath) -> CGFloat {
        return height
    }
    
    open override func rangeCellForItem(_ table: AAManagedTable, indexPath: AARangeIndexPath) -> UITableViewCell {
        let res = table.dequeueCell(indexPath.indexPath) as AAHeaderCell
        
        if title == nil {
            res.titleView.isHidden = true
        } else {
            res.titleView.text = title
            res.titleView.isHidden = false
        }
        
        if icon == nil {
            res.iconView.isHidden = true
        } else {
            res.iconView.isHidden = false
            res.iconView.image = icon!.tintImage(ActorSDK.sharedActor().style.cellHeaderColor)
        }
        
        return res
    }
}

public extension AAManagedSection {
    
    fileprivate func header() -> AAHeaderRow {
        let r = AAHeaderRow()
        regions.append(r)
        r.section = self
        r.initTable(self.table)
        return r
    }
    
    public func header(_ title: String) -> AAHeaderRow {
        let r = header()
        r.title = title
        r.initTable(self.table)
        return r
    }
}


// Common Row

open class AACommonRow: AAManagedRow {
    
    open var style: AACommonCellStyle = .normal
    open var hint: String?
    open var content: String?
    open var switchOn: Bool = false
    open var switchAction: ((_ v: Bool) -> ())?
    
    open var bindAction: ((_ r: AACommonRow)->())?
    
    open var contentInset: CGFloat = 15
    
    // Cell
    
    open override func rangeCellHeightForItem(_ table: AAManagedTable, indexPath: AARangeIndexPath) -> CGFloat {
        return 44
    }
    
    open override func rangeCellForItem(_ table: AAManagedTable, indexPath: AARangeIndexPath) -> UITableViewCell {
        let res = table.dequeueCommonCell(indexPath.indexPath)
        bindCell(res)
        return res
    }
    
    open func bindCell(_ res: AACommonCell) {
        res.style = style
        res.setContent(content)
        res.setHint(hint)
        res.switchBlock = switchAction
        res.setSwitcherOn(switchOn, animated: true)
        res.contentInset = contentInset
        
        if selectAction != nil {
            res.selectionStyle = .default
        } else {
            res.selectionStyle = .none
        }
    }
    
    // Binding
    
    open func rangeBind(_ table: AAManagedTable, binder: AABinder) {
        bindAction?(self)
    }
    
    open override func reload() {
        
        bindAction?(self)
        
        if let p = indexPath, let s = section {
            if let cell = s.table.tableView.cellForRow(at: p) as? AACommonCell {
                bindCell(cell)
            }
        }
    }
    
    open func rebind() {
        bindAction?(self)
    }
}

public extension AAManagedSection {

    // Common cell
    
    fileprivate func common() -> AACommonRow {
        let r = AACommonRow()
        let itemsCount = numberOfItems(table)
        regions.append(r)
        r.indexPath = IndexPath(row: itemsCount, section: index)
        r.section = self
        r.initTable(self.table)
        return r
    }
    
    public func common(_ closure: (_ r: AACommonRow) -> ()) -> AACommonRow {
        let r = common()
        closure(r)
        r.bindAction?(r)
        r.initTable(self.table)
        return r
    }
    
    // Action cell
    
    public func action(_ closure: (_ r: AACommonRow) -> ()) -> AACommonRow {
        let r = common()
        r.style = .action
        closure(r)
        r.bindAction?(r)
        r.initTable(self.table)
        return r
    }
    
    public func action(_ content: String, closure: (_ r: AACommonRow) -> ()) -> AACommonRow {
        let r = common()
        r.content = AALocalized(content)
        r.style = .action
        closure(r)
        r.bindAction?(r)
        r.initTable(self.table)
        return r
    }
    
    // Navigation cell
    
    public func navigate(_ closure: (_ r: AACommonRow) -> ()) -> AACommonRow {
        let r = common()
        r.style = .navigation
        closure(r)
        r.bindAction?(r)
        r.initTable(self.table)
        return r
    }
    
    public func navigate(_ content: String, closure: (_ r: AACommonRow) -> ()) -> AACommonRow {
        let r = common()
        r.content = AALocalized(content)
        r.style = .navigation
        closure(r)
        r.bindAction?(r)
        r.initTable(self.table)
        return r
    }
    
    public func navigate(_ content: String, controller: UIViewController.Type) -> AACommonRow {
        let r = common()
        r.content = AALocalized(content)
        r.style = .navigation
        r.selectAction = { () -> Bool in
            self.table.controller.navigateNext(controller.init())
            return false
        }
        r.bindAction?(r)
        r.initTable(self.table)
        return r
    }
    
    // Danger
    
    public func danger(_ content: String, closure: (_ r: AACommonRow) -> ()) -> AACommonRow {
        let r = common()
        r.content = AALocalized(content)
        r.style = .destructive
        closure(r)
        r.bindAction?(r)
        r.initTable(self.table)
        return r
    }
    
    // Content
    
    func url(_ content: String, url: String) -> AACommonRow {
        let r = common()
        r.content = AALocalized(content)
        r.style = .navigation
        r.selectAction = { () -> Bool in
            if let u = URL(string: url) {
                UIApplication.shared.openURL(u)
            }
            return true
        }
        r.initTable(self.table)
        return r
    }
    
    public func hint(_ content: String) -> AACommonRow {
        let r = common()
        r.content = AALocalized(content)
        r.style = .hint
        r.bindAction?(r)
        r.initTable(self.table)
        return r
    }

}

// Custom cell

open class AACustomRow<T>: AAManagedRow where T: UITableViewCell {
    
    open var height: CGFloat = 44
    open var closure: ((_ cell: T) -> ())?
    
    open var bindAction: ((_ r: AACustomRow<T>)->())?
    
    // Cell
    
    open override func rangeCellHeightForItem(_ table: AAManagedTable, indexPath: AARangeIndexPath) -> CGFloat {
        return height
    }
    
    open override func rangeCellForItem(_ table: AAManagedTable, indexPath: AARangeIndexPath) -> UITableViewCell {
        let res: T = table.dequeueCell(indexPath.indexPath)
        rangeBindCellForItem(table, indexPath: indexPath, cell: res)
        return res
    }
    
    open func rangeBindCellForItem(_ table: AAManagedTable, indexPath: AARangeIndexPath, cell: T) {
        closure?(cell)
    }
    
    // Binding
    
    open override func reload() {
        
        bindAction?(self)
        
        super.reload()
    }
}

public extension AAManagedSection {
    
    fileprivate func custom<T>() -> AACustomRow<T> where T: UITableViewCell {
        let r = AACustomRow<T>()
        let itemsCount = numberOfItems(table)
        regions.append(r)
        r.indexPath = IndexPath(row: itemsCount, section: index)
        r.section = self
        r.initTable(self.table)
        return r
    }
    
    public func custom<T>(_ closure: (_ r: AACustomRow<T>) -> ()) -> AACustomRow<T> where T: UITableViewCell {
        let r: AACustomRow<T> = custom()
        closure(r)
        r.bindAction?(r)
        r.initTable(self.table)
        return r
    }
}

// Avatar Row

open class AAAvatarRow: AAManagedRow {
    
    open var id: Int?
    open var title: String?
    
    open var avatar: ACAvatar?
    open var avatarPath: String?
    open var avatarLoading: Bool = false
    
    open var subtitleHidden: Bool = false
    open var subtitle: String?
    open var subtitleColor: UIColor?
    
    open var bindAction: ((_ r: AAAvatarRow)->())?
    
    open var avatarDidTap: ((_ view: UIView) -> ())?
    
    // Cell
    
    open override func rangeCellHeightForItem(_ table: AAManagedTable, indexPath: AARangeIndexPath) -> CGFloat {
        return 92
    }
    
    open override func rangeCellForItem(_ table: AAManagedTable, indexPath: AARangeIndexPath) -> UITableViewCell {
        let res: AAAvatarCell = table.dequeueCell(indexPath.indexPath)
        bindCell(res)
        return res
    }
    
    open func bindCell(_ res: AAAvatarCell) {
        res.titleLabel.text = title
        
        res.subtitleLabel.isHidden = subtitleHidden
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
        
        res.progress.isHidden = !avatarLoading
        if avatarLoading {
            res.progress.startAnimating()
        } else {
            res.progress.stopAnimating()
        }
        
        res.didTap = avatarDidTap
    }
    
    // Binding
    
    open override func reload() {
        
        bindAction?(self)
        
        if let p = indexPath, let s = section {
            if let cell = s.table.tableView.visibleCellForIndexPath(p) as? AAAvatarCell {
                bindCell(cell)
            }
        }
    }
}

public extension AAManagedSection {
    
    fileprivate func addAvatar() -> AAAvatarRow {
        let r = AAAvatarRow()
        let itemsCount = numberOfItems(table)
        regions.append(r)
        r.indexPath = IndexPath(row: itemsCount, section: index)
        r.section = self
        r.initTable(self.table)
        return r
    }
    
    public func avatar(_ closure: (_ r: AAAvatarRow) -> ()) -> AAAvatarRow {
        let r: AAAvatarRow = addAvatar()
        closure(r)
        r.bindAction?(r)
        r.initTable(self.table)
        return r
    }
}

// Arrays

open class AAManagedArrayRows<T, R>: AAManagedRange where R: UITableViewCell {
    
    open var height: CGFloat = 44
    open var selectAction: ((_ t: T) -> Bool)?
    open var bindCopy: ((_ t: T) -> String?)?
    open var section: AAManagedSection?
    
    open var bindData: ((_ cell: R, _ item: T) -> ())?
    
    open var itemShown: ((_ index: Int, _ item: T) -> ())?
    
    open var data = [T]()
    
    open func initTable(_ table: AAManagedTable) {
        
    }
    
    // Number of items
    
    open func rangeNumberOfItems(_ table: AAManagedTable) -> Int {
        return data.count
    }
    
    // Cells
    
    open func rangeCellHeightForItem(_ table: AAManagedTable, indexPath: AARangeIndexPath) -> CGFloat {
        return height
    }
    
    open func rangeCellForItem(_ table: AAManagedTable, indexPath: AARangeIndexPath) -> UITableViewCell {
        let res: R = table.dequeueCell(indexPath.indexPath)
        let item = data[indexPath.item]
        rangeBindData(table, indexPath: indexPath, cell: res, item: item)
        
        if let shown = itemShown {
            shown(indexPath.item, item)
        }
        
        return res
    }
    
    open func rangeBindData(_ table: AAManagedTable, indexPath: AARangeIndexPath, cell: R, item: T) {
        bindData?(cell, item)
    }
    
    // Selection
    
    open func rangeCanSelect(_ table: AAManagedTable, indexPath: AARangeIndexPath) -> Bool {
        return selectAction != nil
    }
    
    open func rangeSelect(_ table: AAManagedTable, indexPath: AARangeIndexPath) -> Bool {
        return selectAction!(data[indexPath.item])
    }
    
    // Copy
    
    open func rangeCanCopy(_ table: AAManagedTable, indexPath: AARangeIndexPath) -> Bool {
        if bindCopy != nil {
            return bindCopy!(data[indexPath.item]) != nil
        }
        return false
    }
    
    open func rangeCopy(_ table: AAManagedTable, indexPath: AARangeIndexPath) {
        if let s = bindCopy!(data[indexPath.item]) {
            UIPasteboard.general.string = s
        }
    }
    
    // Reloading
    
    open func reload() {
        if let s = section {
            s.table.tableView.reloadSections(IndexSet(integer: s.index), with: .automatic)
        }
    }
}

public extension AAManagedSection {
    
    fileprivate func addArrays<T, R>() -> AAManagedArrayRows<T, R> where R: UITableViewCell {
        let r = AAManagedArrayRows<T, R>()
        regions.append(r)
        r.section = self
        r.initTable(self.table)
        return r
    }
    
    public func arrays<T, R>(_ closure: (_ r: AAManagedArrayRows<T,R>) -> ()) -> AAManagedArrayRows<T, R> where R: UITableViewCell {
        let res: AAManagedArrayRows<T, R> = addArrays()
        closure(res)
        res.initTable(self.table)
        return res
    }
}


// Single item row

open class AAManagedRow: NSObject, AAManagedRange {
    
    open var selectAction: (() -> Bool)?
    
    open var section: AAManagedSection?
    open var indexPath: IndexPath?
    
    open func initTable(_ table: AAManagedTable) {
        
    }
    
    // Number of items
    
    open func rangeNumberOfItems(_ table: AAManagedTable) -> Int {
        return 1
    }
    
    // Cells
    
    open func rangeCellHeightForItem(_ table: AAManagedTable, indexPath: AARangeIndexPath) -> CGFloat {
        return 44
    }
    
    open func rangeCellForItem(_ table: AAManagedTable, indexPath: AARangeIndexPath) -> UITableViewCell {
        fatalError("Not implemented")
    }
    
    // Selection
    
    open func rangeCanSelect(_ table: AAManagedTable, indexPath: AARangeIndexPath) -> Bool {
        return selectAction != nil
    }
    
    open func rangeSelect(_ table: AAManagedTable, indexPath: AARangeIndexPath) -> Bool {
        return selectAction!()
    }
    
    // Copying
    
    open func rangeCanCopy(_ table: AAManagedTable, indexPath: AARangeIndexPath) -> Bool {
        return rangeCopyData(table, indexPath: indexPath) != nil
    }
    
    open func rangeCopy(_ table: AAManagedTable, indexPath: AARangeIndexPath) {
        UIPasteboard.general.string = rangeCopyData(table, indexPath: indexPath)
    }
    
    open func rangeCopyData(_ table: AAManagedTable, indexPath: AARangeIndexPath) -> String? {
        return nil
    }
    
    open func reload() {
        if let p = indexPath, let s = section {
            s.table.tableView.reloadRows(at: [p], with: .automatic)
        }
    }
}
