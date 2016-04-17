//
//  Copyright (c) 2014-2016 Actor LLC. <https://actor.im>
//

import UIKit

public class GroupMembersController: AAContactsListContentController, AAContactsListContentControllerDelegate, CLTokenInputViewDelegate {

    private var groupTitle: String!
    private var groupImage: UIImage?
    
    private var tokenView = CLTokenInputView()
    private var tokenViewHeight: CGFloat = 48

    private var selected = [TokenRef]()

    public init(title: String, image: UIImage?) {
        super.init()

        self.searchEnabled = false
        self.delegate = self
        self.groupTitle = title
        self.groupImage = image
        
        navigationItem.title = AALocalized("CreateGroupMembersTitle")
        
        if AADevice.isiPad {
            self.navigationItem.leftBarButtonItem = UIBarButtonItem(title: AALocalized("NavigationCancel"), style: UIBarButtonItemStyle.Plain, target: self, action: Selector("dismiss"))
        }
        
        navigationItem.rightBarButtonItem = UIBarButtonItem(title: AALocalized("NavigationDone"), style: UIBarButtonItemStyle.Done, target: self, action: #selector(GroupMembersController.doNext))
    }

    public required init(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    public override func tableDidLoad() {
        super.tableDidLoad()
        
        tokenView.delegate = self
        tokenView.fieldColor = appStyle.vcTokenFieldTextColor
        tokenView.fieldTextColor = appStyle.vcTokenFieldTextColor        
        tokenView.backgroundColor = appStyle.vcTokenFieldBgColor
        tokenView.tintColor = appStyle.vcTokenTintColor
        tokenView.fieldName = ""
        
        let placeholder = AALocalized("CreateGroupMembersPlaceholders")
        let attributedPlaceholder = NSMutableAttributedString(string: placeholder)
        attributedPlaceholder.addAttribute(NSForegroundColorAttributeName, value: appStyle.vcHintColor, range: NSRange(location: 0, length: placeholder.length))
        tokenView.placeholderAttributedText = attributedPlaceholder
        
        self.view.addSubview(tokenView)
        
        tableView.keyboardDismissMode = UIScrollViewKeyboardDismissMode.OnDrag
    }
    
    public func contactDidTap(controller: AAContactsListContentController, contact: ACContact) -> Bool {
        
        for i in 0..<selected.count {
            let n = selected[i]
            if (n.contact.uid == contact.uid) {
                selected.removeAtIndex(i)
                tokenView.removeToken(n.token)
                return true
            }
        }
        
        let token = CLToken(displayText: contact.name, context: nil)
        tokenView.addToken(token)
        selected.append(TokenRef(contact: contact, token: token))
        
        contactRows.filter("")

        return true
    }
    
    public func doNext() {
        let res = IOSIntArray(length: UInt(selected.count))
        for i in 0..<selected.count {
            res.replaceIntAtIndex(UInt(i), withInt: selected[i].contact.uid)
        }
        
        executeSafeOnlySuccess(Actor.createGroupCommandWithTitle(groupTitle, withAvatar: nil, withUids: res)!) { (val) -> Void in
            let gid = (val as! JavaLangInteger).intValue
            if self.groupImage != nil {
                Actor.changeGroupAvatar(gid, image: self.groupImage!)
            }
            if let customController = ActorSDK.sharedActor().delegate.actorControllerForConversation(ACPeer.groupWithInt(gid)) {
                self.navigateDetail(customController)
            } else {
                self.navigateDetail(ConversationViewController(peer: ACPeer.groupWithInt(gid)))
            }
            self.dismiss()
        }
    }
    
    // Handling token input updates
    
    public func tokenInputView(view: CLTokenInputView, didChangeText text: String?) {
        contactRows.filter(text!)
    }
    
    public func tokenInputView(view: CLTokenInputView, didChangeHeightTo height: CGFloat) {
        tokenViewHeight = height
        
        self.view.setNeedsLayout()
    }
    
    public func tokenInputView(view: CLTokenInputView, didRemoveToken token: CLToken) {
        for i in 0..<selected.count {
            let n = selected[i]
            if (n.token == token) {
                selected.removeAtIndex(i)
                return
            }
        }
    }

    // Hacking layout 
    
    public override func viewDidLayoutSubviews() {
        super.viewDidLayoutSubviews()
        
        if AADevice.isiPad {
            tokenView.frame = CGRectMake(0, 44, view.frame.width, tokenViewHeight)
            tableView.frame = CGRectMake(0, tokenViewHeight, view.frame.width, view.frame.height - tokenViewHeight)
        } else {
            tokenView.frame = CGRectMake(0, 64, view.frame.width, tokenViewHeight)
            tableView.frame = CGRectMake(0, tokenViewHeight, view.frame.width, view.frame.height - tokenViewHeight)
        }
    }
}

private class TokenRef {
    var contact: ACContact
    var token: CLToken
    
    init(contact: ACContact, token: CLToken) {
        self.contact = contact
        self.token = token
    }
}
