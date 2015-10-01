//
//  Copyright (c) 2014-2015 Actor LLC. <https://actor.im>
//

import Foundation

class ContactActionCell: UATableViewCell {
    
    let titleView = UILabel()
    let iconView = UIImageView()
    
    override init(style: UITableViewCellStyle, reuseIdentifier: String?) {
        super.init(style: style, reuseIdentifier: reuseIdentifier)
        
        titleView.font = UIFont.systemFontOfSize(18)
        titleView.textColor = MainAppTheme.list.actionColor
        iconView.contentMode = UIViewContentMode.Center
        self.contentView.addSubview(titleView)
        self.contentView.addSubview(iconView)
    }

    required init(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    func bind(icon: String, actionTitle: String) {
        titleView.text = actionTitle
        iconView.image = UIImage(named: icon)?.tintImage(MainAppTheme.list.actionColor)
    }
    
    override func layoutSubviews() {
        super.layoutSubviews()
        let width = self.contentView.frame.width;
        iconView.frame = CGRectMake(30, 8, 40, 40);
        titleView.frame = CGRectMake(80, 8, width - 80 - 14, 40);
    }
}