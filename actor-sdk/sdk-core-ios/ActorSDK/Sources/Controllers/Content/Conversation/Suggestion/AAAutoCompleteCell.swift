//
//  Copyright (c) 2014-2016 Actor LLC. <https://actor.im>
//

import Foundation
import TTTAttributedLabel

class AAAutoCompleteCell: AATableViewCell {
    
    var avatarView = AAAvatarView()
    var nickView = TTTAttributedLabel(frame: CGRectZero)
    var nameView = TTTAttributedLabel(frame: CGRectZero)
    
    override init(style: UITableViewCellStyle, reuseIdentifier: String?) {
        super.init(style: UITableViewCellStyle.Default, reuseIdentifier: reuseIdentifier)
        
        // avatarView.enableAnimation = false
        nickView.font = UIFont.systemFontOfSize(14)
        nickView.textColor = ActorSDK.sharedActor().style.cellTextColor
        
        nameView.font = UIFont.systemFontOfSize(14)
        nameView.textColor = ActorSDK.sharedActor().style.cellHintColor
        
        self.contentView.addSubview(avatarView)
        self.contentView.addSubview(nickView)
        self.contentView.addSubview(nameView)
    }
    
    required init(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }

    func bindData(user: ACMentionFilterResult, highlightWord: String) {
        
        avatarView.bind(user.mentionString, id: Int(user.uid), avatar: user.avatar)
        
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
        nickAttrs.addAttribute(NSForegroundColorAttributeName, value: ActorSDK.sharedActor().style.cellTextColor, range: NSMakeRange(0, nickText.length))
        
        nameAttrs.addAttribute(NSFontAttributeName, value: UIFont.systemFontOfSize(14), range: NSMakeRange(0, nameText.length))
        nameAttrs.addAttribute(NSForegroundColorAttributeName, value: ActorSDK.sharedActor().style.cellHintColor, range: NSMakeRange(0, nameText.length))
        
        for i in 0..<user.mentionMatches.size() {
            let match = user.mentionMatches.getWithInt(i) as! ACStringMatch
            let nsRange = NSMakeRange(Int(match.getStart()), Int(match.getLength()))
            nickAttrs.addAttribute(NSForegroundColorAttributeName, value: appStyle.chatAutocompleteHighlight, range: nsRange)
        }
        
        if user.originalString != nil {
            for i in 0..<user.originalMatches.size() {
                let match = user.originalMatches.getWithInt(i) as! ACStringMatch
                let nsRange = NSMakeRange(Int(match.getStart()) + 3, Int(match.getLength()))
                nameAttrs.addAttribute(NSForegroundColorAttributeName, value: appStyle.chatAutocompleteHighlight, range: nsRange)
            }
        }
        
        nickView.setText(nickAttrs)
        nameView.setText(nameAttrs)
    }
    
    override func layoutSubviews() {
        super.layoutSubviews()
        
        avatarView.frame = CGRectMake(6, 6, 38, 38)
        nickView.frame = CGRectMake(44, 6, 100, 32)
        nickView.sizeToFit()
        nickView.frame = CGRectMake(44, 6, nickView.frame.width, 32)
        
        let left = 44 + nickView.frame.width
        nameView.frame = CGRectMake(left, 6, self.contentView.frame.width - left, 32)
    }
}