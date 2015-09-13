//
//  Copyright (c) 2014-2015 Actor LLC. <https://actor.im>
//

import Foundation

class ContactActionCell: BasicCell {
    
    let titleView = UILabel()
    let iconView = UIImageView()
    
    init(reuseIdentifier:String) {
        super.init(reuseIdentifier: reuseIdentifier, separatorPadding: 80)
        titleView.font = UIFont(name: "HelveticaNeue", size: 18);
        titleView.textColor = MainAppTheme.list.actionColor
        iconView.contentMode = UIViewContentMode.Center
        self.contentView.addSubview(titleView)
        self.contentView.addSubview(iconView)
        
        backgroundColor = MainAppTheme.list.bgColor
        
        let selectedView = UIView()
        selectedView.backgroundColor = MainAppTheme.list.bgSelectedColor
        selectedBackgroundView = selectedView
    }

    required init(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    func bind(icon: String, actionTitle: String, isLast: Bool){
        hideSeparator(isLast)
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