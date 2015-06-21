//
//  Copyright (c) 2015 Actor LLC. <https://actor.im>
//

import Foundation

class BasicCell : UITableViewCell {
    let separatorView = TableViewSeparator(color: MainAppTheme.list.separatorColor)
    let separatorPadding: Int
    
    init(reuseIdentifier:String, separatorPadding: Int) {
        self.separatorPadding = separatorPadding
        super.init(style: UITableViewCellStyle.Default, reuseIdentifier: reuseIdentifier)
        
        self.contentView.addSubview(separatorView);
    }

    required init(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    func hideSeparator(hidden: Bool) {
        separatorView.hidden = hidden
    }
    
    override func layoutSubviews() {
        super.layoutSubviews()
        var width = self.contentView.frame.width;
        var height = self.contentView.frame.height;

        separatorView.frame = CGRectMake(CGFloat(separatorPadding), CGFloat(height - 0.5), width - CGFloat(separatorPadding), CGFloat(0.5));
    }
}