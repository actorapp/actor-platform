//
//  Copyright (c) 2014-2015 Actor LLC. <https://actor.im>
//

import Foundation


class TextCell: UATableViewCell {

    var titleLabel: UILabel = UILabel()
    var contentLabel: UILabel = UILabel()
    
    override init(style: UITableViewCellStyle, reuseIdentifier: String?) {
        super.init(style: style, reuseIdentifier: reuseIdentifier)
        
        titleLabel.font = UIFont.systemFontOfSize(14.0)
        titleLabel.text = " "
        titleLabel.sizeToFit()
        titleLabel.textColor = MainAppTheme.list.actionColor
        contentView.addSubview(titleLabel)
        
        contentLabel.font = UIFont.systemFontOfSize(17.0)
        contentLabel.text = " "
        contentLabel.textColor = MainAppTheme.list.textColor
        contentLabel.lineBreakMode = NSLineBreakMode.ByWordWrapping
        contentLabel.numberOfLines = 0
        contentLabel.sizeToFit()
        contentView.addSubview(contentLabel)
    }
    
    required init(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    func setTitle(title: String?, content: String?) {
        titleLabel.text = title
        contentLabel.text = content
    }
    
    func setAction(isAction: Bool) {
        if isAction {
            contentLabel.textColor = MainAppTheme.list.actionColor
        } else {
            contentLabel.textColor = MainAppTheme.list.textColor
        }
    }
    
    override func layoutSubviews() {
        super.layoutSubviews()
        
        titleLabel.frame = CGRect(x: 15, y: 7, width: contentView.bounds.width - 30, height: titleLabel.bounds.height)
        contentLabel.frame = CGRect(x: 15, y: 27, width: contentView.bounds.width - 30, height: 10000)
        contentLabel.sizeToFit()
    }
    
    class func measure(text: String, width: CGFloat, enableNavigation: Bool) -> CGFloat {
        let size = UIViewMeasure.measureText(text, width: width - 30 - (enableNavigation ? 30 : 0), fontSize: 17)
        return CGFloat(size.height + 36)
    }
}