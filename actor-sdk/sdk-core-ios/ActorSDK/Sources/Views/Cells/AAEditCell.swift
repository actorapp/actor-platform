//
//  Copyright (c) 2014-2016 Actor LLC. <https://actor.im>
//

import Foundation

open class AAEditCell: AATableViewCell {
    
    open let textPrefix = UILabel()
    open let textField = UITextField()
    
    public override init(style: UITableViewCellStyle, reuseIdentifier: String?) {
        super.init(style: style, reuseIdentifier: reuseIdentifier)
        
        textField.autocapitalizationType = .none
        textField.autocorrectionType = .no
        textField.textColor = appStyle.cellTextColor
        textField.keyboardAppearance = appStyle.isDarkApp ? .dark : .light
        
        textPrefix.isHidden = true
        
        contentView.addSubview(textPrefix)
        contentView.addSubview(textField)
    }

    public required init(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    open override func layoutSubviews() {
        super.layoutSubviews()
        
        if textPrefix.isHidden {
            textField.frame = CGRect(x: 15, y: 0, width: contentView.width - 30, height: 44)
        } else {
            textPrefix.frame = CGRect(x: 15, y: 0, width: contentView.width - 30, height: 44)
            textPrefix.sizeToFit()
            textPrefix.frame = CGRect(x: 15, y: 0, width: textPrefix.width, height: 44)
            textField.frame = CGRect(x: 15 + textPrefix.width, y: 0, width: contentView.width - textPrefix.width - 30, height: 44)
        }
    }
}
