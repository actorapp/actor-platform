//
//  Copyright (c) 2014-2015 Actor LLC. <https://actor.im>
//

import UIKit

class GroupMembersController: ContactsContentViewController, ContactsContentViewControllerDelegate, CLTokenInputViewDelegate {

    private var groupTitle: String!
    private var groupImage: UIImage?
    
    private var tokenView = CLTokenInputView()
    private var tokenViewHeight: CGFloat = 48

    private var selected = [TokenRef]()

    init(title: String, image: UIImage?) {
        super.init()

        self.searchEnabled = false
        self.delegate = self
        self.groupTitle = title
        self.groupImage = image
        
        navigationItem.title = localized("CreateGroupMembersTitle")
        navigationItem.rightBarButtonItem = UIBarButtonItem(title: localized("NavigationDone"), style: UIBarButtonItemStyle.Done, target: self, action: "doNext")
    }

    required init(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    override func tableDidLoad() {
        super.tableDidLoad()
        
        tokenView.delegate = self
        tokenView.backgroundColor = MainAppTheme.list.backyardColor
        tokenView.fieldName = ""
        tokenView.placeholderText = localized("CreateGroupMembersPlaceholders")
        
        self.view.addSubview(tokenView)
        
        tableView.keyboardDismissMode = UIScrollViewKeyboardDismissMode.OnDrag
    }
    
    func contactDidTap(controller: ContactsContentViewController, contact: ACContact) -> Bool {
        
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
    
    func doNext() {
        let res = IOSIntArray(length: UInt(selected.count))
        for i in 0..<selected.count {
            res.replaceIntAtIndex(UInt(i), withInt: selected[i].contact.uid)
        }
        
        executeSafeOnlySuccess(Actor.createGroupCommandWithTitle(groupTitle, withAvatar: nil, withUids: res)) { (val) -> Void in
            let gid = (val as! JavaLangInteger).intValue
            if self.groupImage != nil {
                Actor.changeGroupAvatar(gid, image: self.groupImage!)
            }
            self.navigateNext(ConversationViewController(peer: ACPeer.groupWithInt(gid)), removeCurrent: true)
        }
    }
    
    // Handling token input updates
    
    func tokenInputView(view: CLTokenInputView!, didChangeText text: String!) {
        contactRows.filter(text)
    }
    
    func tokenInputView(view: CLTokenInputView!, didChangeHeightTo height: CGFloat) {
        tokenViewHeight = height
        
        self.view.setNeedsLayout()
    }
    
    func tokenInputView(view: CLTokenInputView!, didRemoveToken token: CLToken!) {
        for i in 0..<selected.count {
            let n = selected[i]
            if (n.token == token) {
                selected.removeAtIndex(i)
                return
            }
        }
    }

    // Hacking layout 
    
    override func viewDidLayoutSubviews() {
        super.viewDidLayoutSubviews()
        
        tokenView.frame = CGRectMake(0, 64, view.frame.width, tokenViewHeight)
        tableView.frame = CGRectMake(0, tokenViewHeight, view.frame.width, view.frame.height - tokenViewHeight)
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
