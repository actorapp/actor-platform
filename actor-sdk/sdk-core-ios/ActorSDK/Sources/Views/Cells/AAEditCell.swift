//
//  Copyright (c) 2014-2016 Actor LLC. <https://actor.im>
//

import Foundation

public class AAEditCell: AATableViewCell {
    
    public let textField = UITextField()
    
    public override init(style: UITableViewCellStyle, reuseIdentifier: String?) {
        super.init(style: style, reuseIdentifier: reuseIdentifier)
        
        textField.autocapitalizationType = .None
        textField.autocorrectionType = .No
        textField.textColor = appStyle.cellTextColor
        textField.keyboardAppearance = appStyle.isDarkApp ? .Dark : .Light
        
        contentView.addSubview(textField)
    }

    public required init(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    public override func layoutSubviews() {
        super.layoutSubviews()
        
        textField.frame = CGRectMake(15, 0, contentView.width - 30, 44)
    }
}