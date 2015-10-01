//
//  Copyright (c) 2014-2015 Actor LLC. <https://actor.im>
//

import Foundation

class EditCell: UATableViewCell {
    
    let textField = UITextField()
    
    override init(style: UITableViewCellStyle, reuseIdentifier: String?) {
        super.init(style: style, reuseIdentifier: reuseIdentifier)
        
        textField.autocapitalizationType = .None
        textField.autocorrectionType = .No
        
        contentView.addSubview(textField)
    }

    required init(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    override func layoutSubviews() {
        super.layoutSubviews()
        
        textField.frame = CGRectMake(15, 0, contentView.width - 30, 44)
    }
}