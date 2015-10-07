//
//  Copyright (c) 2014-2015 Actor LLC. <https://actor.im>
//

import UIKit

public class TitledCell: UATableViewCell {
    
//    private var copyData: String?
    private var isAction: Bool = false
    public let titleLabel: UILabel = UILabel(style: "cell.titled.title")
    public let contentLabel: UILabel = UILabel(style: "cell.titled.content")
    
    public override init(style: UITableViewCellStyle, reuseIdentifier: String?) {
        super.init(cellStyle: "cell.titled", reuseIdentifier: reuseIdentifier)
        
        contentView.addSubview(titleLabel)
        contentView.addSubview(contentLabel)
    }

    public required init(coder aDecoder: NSCoder) {
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
    public func setTitle(title: String, content: String) {
        titleLabel.text = title
        contentLabel.text = content
//        copyData = content
    }
    
    public func setAction(isAction: Bool) {
        self.isAction = isAction
        if isAction {
            contentLabel.textColor = MainAppTheme.list.actionColor
        } else {
            contentLabel.textColor = MainAppTheme.list.textColor
        }
    }
    
    public func enableNavigationIcon() {
        accessoryType = UITableViewCellAccessoryType.DisclosureIndicator
    }
    
    public override func layoutSubviews() {
        super.layoutSubviews()
        
        titleLabel.frame = CGRect(x: separatorInset.left, y: 7, width: contentView.bounds.width - separatorInset.left - 10, height: 19)
        contentLabel.frame = CGRect(x: separatorInset.left, y: 27, width: contentView.bounds.width - separatorInset.left - 10, height: 22)
    }
}
