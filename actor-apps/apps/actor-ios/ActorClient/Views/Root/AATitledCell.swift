//
//  Copyright (c) 2015 Actor LLC. <https://actor.im>
//

import UIKit

class AATitledCell: AATableViewCell {
    
    // MARK: -
    // MARK: Private vars
    
    private var titleLabel: UILabel = UILabel()
    private var contentLabel: UILabel = UILabel()
    
    // MARK: -
    // MARK: Constructors
    
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
        contentLabel.sizeToFit()
        contentView.addSubview(contentLabel)
    }

    required init(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    // MARK: -
    // MARK: Setters 
    
    func setTitle(title: String, content: String) {
        titleLabel.text = title
        contentLabel.text = content
    }
    
    // MARK: -
    // MARK: Layout
    
    override func layoutSubviews() {
        super.layoutSubviews()
        
        titleLabel.frame = CGRect(x: separatorInset.left, y: 7, width: contentView.bounds.width - separatorInset.left - 10, height: titleLabel.bounds.height)
        contentLabel.frame = CGRect(x: separatorInset.left, y: 27, width: contentView.bounds.width - separatorInset.left - 10, height: contentLabel.bounds.height)
    }

}
