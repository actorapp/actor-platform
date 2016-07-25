//
//  Copyright (c) 2014-2016 Actor LLC. <https://actor.im>
//

import Foundation

public class AAEditCell: AATableViewCell {
    
    public let textPrefix = UILabel()
    public let textField = UITextField()
    
    public override init(style: UITableViewCellStyle, reuseIdentifier: String?) {
        super.init(style: style, reuseIdentifier: reuseIdentifier)
        
        textField.autocapitalizationType = .None
        textField.autocorrectionType = .No
        textField.textColor = appStyle.cellTextColor
        textField.keyboardAppearance = appStyle.isDarkApp ? .Dark : .Light
        
        textPrefix.hidden = true
        
        contentView.addSubview(textPrefix)
        contentView.addSubview(textField)
    }

    public required init(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    public override func layoutSubviews() {
        super.layoutSubviews()
        
        if textPrefix.hidden {
            textField.frame = CGRectMake(15, 0, contentView.width - 30, 44)
        } else {
            textPrefix.frame = CGRectMake(15, 0, contentView.width - 30, 44)
            textPrefix.sizeToFit()
            textPrefix.frame = CGRectMake(15, 0, textPrefix.width, 44)
            textField.frame = CGRectMake(15 + textPrefix.width, 0, contentView.width - textPrefix.width - 30, 44)
        }
    }
}