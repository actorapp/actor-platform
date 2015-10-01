//
//  Copyright (c) 2014-2015 Actor LLC. <https://actor.im>
//

import Foundation

// Edit Row

class ACEditRow: ACManagedRow, UITextFieldDelegate {
    
    var text: String?
    var placeholder: String?
    var returnKeyType = UIReturnKeyType.Default
    var autocorrectionType = UITextAutocorrectionType.Default
    var autocapitalizationType = UITextAutocapitalizationType.Sentences
    var returnAction: (()->())?
    
    override func rangeCellHeightForItem(table: ACManagedTable, indexPath: ACRangeIndexPath) -> CGFloat {
        return 44
    }
    
    override func rangeCellForItem(table: ACManagedTable, indexPath: ACRangeIndexPath) -> UITableViewCell {
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
    
    func textFieldDidChange(textField: UITextField) {
        text = textField.text
    }
    
    @objc func textFieldDidEndEditing(textField: UITextField) {
        text = textField.text
    }
    
    func textFieldShouldReturn(textField: UITextField) -> Bool {
        returnAction?()
        return false
    }
}

extension ACManagedSection {
    
    func edit(closure: (r: ACEditRow) -> ()) -> ACEditRow {
        let r = ACEditRow()
        regions.append(r)
        closure(r: r)
        return r
    }
}

// Titled Row

class ACTitledRow: ACManagedRow {
    
    var title: String?
    var subtitle: String?
    
    var isAction: Bool = false
    var accessoryType = UITableViewCellAccessoryType.None
    
    var bindAction: ((r: ACTitledRow)->())?
    
    // Cell
    
    override func rangeCellHeightForItem(table: ACManagedTable, indexPath: ACRangeIndexPath) -> CGFloat {
        return 55
    }
    
    override func rangeCellForItem(table: ACManagedTable, indexPath: ACRangeIndexPath) -> UITableViewCell {
        let res = table.dequeueTitledCell(indexPath.indexPath)
        bindCell(res)
        return res
    }
    
    func bindCell(res: TitledCell) {
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
    
    override func rangeCopyData(table: ACManagedTable, indexPath: ACRangeIndexPath) -> String? {
        return isAction ? nil : subtitle
    }
    
    // Binding
    
    override func reload() {
        
        bindAction?(r: self)
        
        if let p = indexPath, let s = section {
            if let cell = s.table.tableView.cellForRowAtIndexPath(p) as? TitledCell {
                bindCell(cell)
            }
        }
    }
}

extension ACManagedSection {
    
    private func titled() -> ACTitledRow {
        let r = ACTitledRow()
        let itemsCount = numberOfItems(table)
        regions.append(r)
        r.indexPath = NSIndexPath(forRow: itemsCount, inSection: index)
        r.section = self
        return r
    }

    func titled(closure: (r: ACTitledRow) -> ()) -> ACTitledRow {
        let r = titled()
        closure(r: r)
        return r
    }
    
    func titled(title: String, closure: (r: ACTitledRow) -> ()) -> ACTitledRow {
        let r = titled()
        r.title = localized(title)
        closure(r: r)
        return r
    }
    
    func titled(title: String, content: String) -> ACTitledRow {
        let r = titled()
        r.title = localized(title)
        r.subtitle = content
        return r
    }
}

// Text Row

class ACTextRow: ACManagedRow {
    
    var title: String?
    var content: String?
    
    var isAction: Bool = false
    var navigate: Bool = false
    
    var bindAction: ((r: ACTextRow)->())?
    
    // Cell
    
    override func rangeCellHeightForItem(table: ACManagedTable, indexPath: ACRangeIndexPath) -> CGFloat {
        return TextCell.measure(content!, width: table.tableView.width, enableNavigation: navigate)
    }
    
    override func rangeCellForItem(table: ACManagedTable, indexPath: ACRangeIndexPath) -> UITableViewCell {
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
    
    override func rangeCopyData(table: ACManagedTable, indexPath: ACRangeIndexPath) -> String? {
        return isAction ? nil : content
    }
    
    // Binding
    
    override func reload() {
        
        bindAction?(r: self)
        
        super.reload()
    }
}

extension ACManagedSection {
    
    private func text() -> ACTextRow {
        let r = ACTextRow()
        let itemsCount = numberOfItems(table)
        regions.append(r)
        r.indexPath = NSIndexPath(forRow: itemsCount, inSection: index)
        r.section = self
        return r
    }
    
    func text(closure: (r: ACTextRow) -> ()) -> ACTextRow {
        let r = text()
        closure(r: r)
        r.bindAction?(r: r)
        return r
    }
    
    func text(title: String, closure: (r: ACTextRow) -> ()) -> ACTextRow {
        let r = text()
        r.title = localized(title)
        closure(r: r)
        r.bindAction?(r: r)
        return r
    }
    
    func text(title: String, content: String) -> ACTextRow {
        let r = text()
        r.title = localized(title)
        r.content = content
        return r
    }
}


// Header Row

class ACHeaderRow: ACManagedRow {
    
    var height: CGFloat = 40
    var title: String?
    var icon: UIImage?
    
    override func rangeCellHeightForItem(table: ACManagedTable, indexPath: ACRangeIndexPath) -> CGFloat {
        return height
    }
    
    override func rangeCellForItem(table: ACManagedTable, indexPath: ACRangeIndexPath) -> UITableViewCell {
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

extension ACManagedSection {
    
    private func header() -> ACHeaderRow {
        let r = ACHeaderRow()
        regions.append(r)
        r.section = self
        return r
    }
    
    func header(title: String) -> ACHeaderRow {
        let r = header()
        r.title = title
        return r
    }
}


// Common Row

class ACCommonRow: ACManagedRow {
    
    var style: CommonCellStyle = .Normal
    var hint: String?
    var content: String?
    var switchOn: Bool = false
    var switchAction: ((v: Bool) -> ())?
    
    var bindAction: ((r: ACCommonRow)->())?
    
    var contentInset: CGFloat = 15
    
    // Cell
    
    override func rangeCellHeightForItem(table: ACManagedTable, indexPath: ACRangeIndexPath) -> CGFloat {
        return 44
    }
    
    override func rangeCellForItem(table: ACManagedTable, indexPath: ACRangeIndexPath) -> UITableViewCell {
        let res = table.dequeueCommonCell(indexPath.indexPath)
        bindCell(res)
        return res
    }
    
    func bindCell(res: CommonCell) {
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
    
    override func reload() {
        
        bindAction?(r: self)
        
        if let p = indexPath, let s = section {
            if let cell = s.table.tableView.cellForRowAtIndexPath(p) as? CommonCell {
                bindCell(cell)
            }
        }
    }
}

extension ACManagedSection {

    // Common cell
    
    private func common() -> ACCommonRow {
        let r = ACCommonRow()
        let itemsCount = numberOfItems(table)
        regions.append(r)
        r.indexPath = NSIndexPath(forRow: itemsCount, inSection: index)
        r.section = self
        return r
    }
    
    func common(closure: (r: ACCommonRow) -> ()) -> ACCommonRow {
        let r = common()
        closure(r: r)
        r.bindAction?(r: r)
        return r
    }
    
    // Action cell
    
    func action(closure: (r: ACCommonRow) -> ()) -> ACCommonRow {
        let r = common()
        r.style = .Action
        closure(r: r)
        r.bindAction?(r: r)
        return r
    }
    
    func action(content: String, closure: (r: ACCommonRow) -> ()) -> ACCommonRow {
        let r = common()
        r.content = localized(content)
        r.style = .Action
        closure(r: r)
        r.bindAction?(r: r)
        return r
    }
    
    // Navigation cell
    
    func navigate(closure: (r: ACCommonRow) -> ()) -> ACCommonRow {
        let r = common()
        r.style = .Navigation
        closure(r: r)
        r.bindAction?(r: r)
        return r
    }
    
    func navigate(content: String, closure: (r: ACCommonRow) -> ()) -> ACCommonRow {
        let r = common()
        r.content = localized(content)
        r.style = .Navigation
        closure(r: r)
        r.bindAction?(r: r)
        return r
    }
    
    func navigate(content: String, controller: UIViewController.Type) -> ACCommonRow {
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
    
    func danger(content: String, closure: (r: ACCommonRow) -> ()) -> ACCommonRow {
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
    
    func hint(content: String) -> ACCommonRow {
        let r = common()
        r.content = localized(content)
        r.style = .Hint
        r.bindAction?(r: r)
        return r
    }

}

// Custom cell

class ACCustomRow<T where T: UITableViewCell>: ACManagedRow {
    
    var height: CGFloat = 44
    var closure: ((cell: T) -> ())?
    
    var bindAction: ((r: ACCustomRow<T>)->())?
    
    // Cell
    
    override func rangeCellHeightForItem(table: ACManagedTable, indexPath: ACRangeIndexPath) -> CGFloat {
        return height
    }
    
    override func rangeCellForItem(table: ACManagedTable, indexPath: ACRangeIndexPath) -> UITableViewCell {
        let res: T = table.dequeueCell(indexPath.indexPath)
        rangeBindCellForItem(table, indexPath: indexPath, cell: res)
        return res
    }
    
    func rangeBindCellForItem(table: ACManagedTable, indexPath: ACRangeIndexPath, cell: T) {
        closure?(cell: cell)
    }
    
    // Binding
    
    override func reload() {
        
        bindAction?(r: self)
        
        super.reload()
    }
}

extension ACManagedSection {
    
    private func custom<T where T: UITableViewCell>() -> ACCustomRow<T> {
        let r = ACCustomRow<T>()
        let itemsCount = numberOfItems(table)
        regions.append(r)
        r.indexPath = NSIndexPath(forRow: itemsCount, inSection: index)
        r.section = self
        return r
    }
    
    func custom<T where T: UITableViewCell>(closure: (r: ACCustomRow<T>) -> ()) -> ACCustomRow<T> {
        let r: ACCustomRow<T> = custom()
        closure(r: r)
        r.bindAction?(r: r)
        return r
    }
}

// Avatar Row

class ACAvatarRow: ACManagedRow {
    
    var id: Int?
    var title: String?
    
    var avatar: ACAvatar?
    var avatarPath: String?
    var avatarLoading: Bool = false
    
    var subtitle: String?
    var subtitleStyle: String?
    
    var bindAction: ((r: ACAvatarRow)->())?
    
    var avatarDidTap: ((view: UIView) -> ())?
    
    // Cell
    
    override func rangeCellHeightForItem(table: ACManagedTable, indexPath: ACRangeIndexPath) -> CGFloat {
        return 92
    }
    
    override func rangeCellForItem(table: ACManagedTable, indexPath: ACRangeIndexPath) -> UITableViewCell {
        let res: AvatarCell = table.dequeueCell(indexPath.indexPath)
        bindCell(res)
        return res
    }
    
    func bindCell(res: AvatarCell) {
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
    
    override func reload() {
        
        bindAction?(r: self)
        
        if let p = indexPath, let s = section {
            if let cell = s.table.tableView.cellForRowAtIndexPath(p) as? AvatarCell {
                bindCell(cell)
            }
        }
    }
}

extension ACManagedSection {
    
    private func addAvatar() -> ACAvatarRow {
        let r = ACAvatarRow()
        let itemsCount = numberOfItems(table)
        regions.append(r)
        r.indexPath = NSIndexPath(forRow: itemsCount, inSection: index)
        r.section = self
        return r
    }
    
    func avatar(closure: (r: ACAvatarRow) -> ()) -> ACAvatarRow {
        let r: ACAvatarRow = addAvatar()
        closure(r: r)
        r.bindAction?(r: r)
        return r
    }
}

// Arrays

class ACManagedArrayRows<T, R where R: UITableViewCell>: ACManagedRange {
    
    var height: CGFloat = 44
    var selectAction: ((t: T) -> Bool)?
    var bindCopy: ((t: T) -> String?)?
    var section: ACManagedSection?
    
    var bindData: ((cell: R, item: T) -> ())?
    
    var data = [T]()
    
    // Number of items
    
    func rangeNumberOfItems(table: ACManagedTable) -> Int {
        return data.count
    }
    
    // Cells
    
    func rangeCellHeightForItem(table: ACManagedTable, indexPath: ACRangeIndexPath) -> CGFloat {
        return height
    }
    
    func rangeCellForItem(table: ACManagedTable, indexPath: ACRangeIndexPath) -> UITableViewCell {
        let res: R = table.dequeueCell(indexPath.indexPath)
        rangeBindData(table, indexPath: indexPath, cell: res, item: data[indexPath.item])
        return res
    }
    
    func rangeBindData(table: ACManagedTable, indexPath: ACRangeIndexPath, cell: R, item: T) {
        bindData?(cell: cell, item: item)
    }
    
    // Selection
    
    func rangeCanSelect(table: ACManagedTable, indexPath: ACRangeIndexPath) -> Bool {
        return selectAction != nil
    }
    
    func rangeSelect(table: ACManagedTable, indexPath: ACRangeIndexPath) -> Bool {
        return selectAction!(t: data[indexPath.item])
    }
    
    // Copy
    
    func rangeCanCopy(table: ACManagedTable, indexPath: ACRangeIndexPath) -> Bool {
        if bindCopy != nil {
            return bindCopy!(t: data[indexPath.item]) != nil
        }
        return false
    }
    
    func rangeCopy(table: ACManagedTable, indexPath: ACRangeIndexPath) {
        if let s = bindCopy!(t: data[indexPath.item]) {
            UIPasteboard.generalPasteboard().string = s
        }
    }
    
    // Reloading
    
    func reload() {
        if let s = section {
            s.table.tableView.reloadSections(NSIndexSet(index: s.index), withRowAnimation: .Automatic)
        }
    }
}

extension ACManagedSection {
    
    private func addArrays<T, R where R: UITableViewCell>() -> ACManagedArrayRows<T, R> {
        let r = ACManagedArrayRows<T, R>()
        regions.append(r)
        r.section = self
        return r
    }
    
    func arrays<T, R where R: UITableViewCell>(closure: (r: ACManagedArrayRows<T,R>) -> ()) -> ACManagedArrayRows<T, R> {
        let res: ACManagedArrayRows<T, R> = addArrays()
        closure(r: res)
        return res
    }
}


// Single item row

class ACManagedRow: NSObject, ACManagedRange {
    
    var selectAction: (() -> Bool)?
    
    var section: ACManagedSection?
    var indexPath: NSIndexPath?
    
    // Number of items
    
    func rangeNumberOfItems(table: ACManagedTable) -> Int {
        return 1
    }
    
    // Cells
    
    func rangeCellHeightForItem(table: ACManagedTable, indexPath: ACRangeIndexPath) -> CGFloat {
        return 44
    }
    
    func rangeCellForItem(table: ACManagedTable, indexPath: ACRangeIndexPath) -> UITableViewCell {
        fatalError("Not implemented")
    }
    
    // Selection
    
    func rangeCanSelect(table: ACManagedTable, indexPath: ACRangeIndexPath) -> Bool {
        return selectAction != nil
    }
    
    func rangeSelect(table: ACManagedTable, indexPath: ACRangeIndexPath) -> Bool {
        return selectAction!()
    }
    
    // Copying
    
    func rangeCanCopy(table: ACManagedTable, indexPath: ACRangeIndexPath) -> Bool {
        return rangeCopyData(table, indexPath: indexPath) != nil
    }
    
    func rangeCopy(table: ACManagedTable, indexPath: ACRangeIndexPath) {
        UIPasteboard.generalPasteboard().string = rangeCopyData(table, indexPath: indexPath)
    }
    
    func rangeCopyData(table: ACManagedTable, indexPath: ACRangeIndexPath) -> String? {
        return nil
    }
    
    func reload() {
        if let p = indexPath, let s = section {
            s.table.tableView.reloadRowsAtIndexPaths([p], withRowAnimation: .Automatic)
        }
    }
}