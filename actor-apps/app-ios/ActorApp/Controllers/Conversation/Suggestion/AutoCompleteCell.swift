//
//  Copyright (c) 2014-2015 Actor LLC. <https://actor.im>
//

import Foundation

class AutoCompleteCell: UITableViewCell {
    
    var avatarView = AvatarView(frameSize: 32)
    var nickView = TTTAttributedLabel(frame: CGRectZero)
    var nameView = TTTAttributedLabel(frame: CGRectZero)
    
    override init(style: UITableViewCellStyle, reuseIdentifier: String?) {
        super.init(style: UITableViewCellStyle.Default, reuseIdentifier: reuseIdentifier)
        
        avatarView.enableAnimation = false
        nickView.font = UIFont.systemFontOfSize(14)
        nickView.textColor = MainAppTheme.list.textColor
        
        nameView.font = UIFont.systemFontOfSize(14)
        nameView.textColor = MainAppTheme.list.hintColor
        
        self.contentView.addSubview(avatarView)
        self.contentView.addSubview(nickView)
        self.contentView.addSubview(nameView)
    }
    
    required init(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }

    func bindData(user: ACMentionFilterResult, highlightWord: String) {
        avatarView.bind(user.mentionString, id: user.uid, avatar: user.avatar, clearPrev: true)
        
        var nickText: String
        var nameText: String
        
        nickText = user.mentionString
        if user.originalString != nil {
            nameText = " \u{2022} \(user.originalString)";
        } else {
            nameText = ""
        }
        
        let nickAttrs = NSMutableAttributedString(string: nickText)
        let nameAttrs = NSMutableAttributedString(string: nameText)
        
        nickAttrs.addAttribute(NSFontAttributeName, value: UIFont.systemFontOfSize(14), range: NSMakeRange(0, nickText.length))
        nickAttrs.addAttribute(NSForegroundColorAttributeName, value: MainAppTheme.list.textColor, range: NSMakeRange(0, nickText.length))
        
        nameAttrs.addAttribute(NSFontAttributeName, value: UIFont.systemFontOfSize(14), range: NSMakeRange(0, nameText.length))
        nameAttrs.addAttribute(NSForegroundColorAttributeName, value: MainAppTheme.list.hintColor, range: NSMakeRange(0, nameText.length))
        
        for i in 0..<user.mentionMatches.size() {
            let match = user.mentionMatches.getWithInt(i) as! ACStringMatch
            let nsRange = NSMakeRange(Int(match.getStart()), Int(match.getLength()))
            nickAttrs.addAttribute(NSForegroundColorAttributeName, value: MainAppTheme.chat.autocompleteHighlight, range: nsRange)
        }
        
        if user.originalString != nil {
            for i in 0..<user.originalMatches.size() {
                let match = user.originalMatches.getWithInt(i) as! ACStringMatch
                let nsRange = NSMakeRange(Int(match.getStart()) + 3, Int(match.getLength()))
                nameAttrs.addAttribute(NSForegroundColorAttributeName, value: MainAppTheme.chat.autocompleteHighlight, range: nsRange)
            }
        }
        
        nickView.setText(nickAttrs)
        nameView.setText(nameAttrs)
    }
    
    override func layoutSubviews() {
        super.layoutSubviews()
        
        avatarView.frame = CGRectMake(6, 6, 32, 32)
        nickView.frame = CGRectMake(44, 6, 100, 32)
        nickView.sizeToFit()
        nickView.frame = CGRectMake(44, 6, nickView.frame.width, 32)
        
        let left = 44 + nickView.frame.width
        nameView.frame = CGRectMake(left, 6, self.contentView.frame.width - left, 32)
    }
}