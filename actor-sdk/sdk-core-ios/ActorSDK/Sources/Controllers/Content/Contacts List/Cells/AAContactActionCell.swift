//
//  Copyright (c) 2014-2016 Actor LLC. <https://actor.im>
//

import Foundation

open class AAContactActionCell: AATableViewCell {
    
    open let titleView = YYLabel()
    open let iconView = UIImageView()
    
    public override init(style: UITableViewCellStyle, reuseIdentifier: String?) {
        super.init(style: style, reuseIdentifier: reuseIdentifier)
        
        titleView.font = UIFont.systemFont(ofSize: 18)
        titleView.textColor = ActorSDK.sharedActor().style.cellTintColor
        titleView.displaysAsynchronously = true
        iconView.contentMode = UIViewContentMode.center
        self.contentView.addSubview(titleView)
        self.contentView.addSubview(iconView)
    }

    public required init(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    open func bind(_ icon: String, actionTitle: String) {
        titleView.text = actionTitle
        iconView.image = UIImage.bundled(icon)?.tintImage(ActorSDK.sharedActor().style.cellTintColor)
    }
    
    open override func layoutSubviews() {
        super.layoutSubviews()
        let width = self.contentView.frame.width;
        iconView.frame = CGRect(x: 30, y: 8, width: 40, height: 40);
        titleView.frame = CGRect(x: 80, y: 8, width: width - 80 - 14, height: 40);
    }
}
