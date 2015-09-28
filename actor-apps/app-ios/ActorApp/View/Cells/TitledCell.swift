//
//  Copyright (c) 2014-2015 Actor LLC. <https://actor.im>
//

import UIKit

class TitledCell: UATableViewCell {
    
//    private var copyData: String?
    private var isAction: Bool = false
    let titleLabel: UILabel = UILabel(style: "cell.titled.title")
    let contentLabel: UILabel = UILabel(style: "cell.titled.content")
    
    override init(style: UITableViewCellStyle, reuseIdentifier: String?) {
        super.init(cellStyle: "cell.titled", reuseIdentifier: reuseIdentifier)
        
        contentView.addSubview(titleLabel)
        contentView.addSubview(contentLabel)
    }

    required init(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
//    override func canPerformAction(action: Selector, withSender sender: AnyObject?) -> Bool {
//        if action == "copy:" {
//            return copyData != nil && !isAction
//        }
//        return false
//    }
//    
//    override func copy(sender: AnyObject?) {
//        UIPasteboard.generalPasteboard().string = copyData
//    }
//    
    func setTitle(title: String, content: String) {
        titleLabel.text = title
        contentLabel.text = content
//        copyData = content
    }
    
    func setAction(isAction: Bool) {
        self.isAction = isAction
        if isAction {
            contentLabel.textColor = MainAppTheme.list.actionColor
        } else {
            contentLabel.textColor = MainAppTheme.list.textColor
        }
    }
    
    func enableNavigationIcon() {
        accessoryType = UITableViewCellAccessoryType.DisclosureIndicator
    }
    
    override func layoutSubviews() {
        super.layoutSubviews()
        
        titleLabel.frame = CGRect(x: separatorInset.left, y: 7, width: contentView.bounds.width - separatorInset.left - 10, height: 19)
        contentLabel.frame = CGRect(x: separatorInset.left, y: 27, width: contentView.bounds.width - separatorInset.left - 10, height: 22)
    }
}
