//
//  Copyright (c) 2014-2015 Actor LLC. <https://actor.im>
//

import UIKit

class GroupMembersController: ContactsBaseViewController, CLTokenInputViewDelegate {

    private var groupTitle: String!
    private var groupImage: UIImage?
    
    private var tableView = UITableView()
    private var tokenView = CLTokenInputView()
    private var tokenViewHeight: CGFloat = 48
    
    private var selected = [TokenRef]()
    
    init(title: String, image: UIImage?) {
        super.init(contentSection: 0, nibName: nil, bundle: nil)

        self.groupTitle = title
        self.groupImage = image
        
        navigationItem.title = localized("CreateGroupMembersTitle")
        navigationItem.rightBarButtonItem = UIBarButtonItem(title: localized("NavigationDone"), style: UIBarButtonItemStyle.Done, target: self, action: "doNext")
        self.extendedLayoutIncludesOpaqueBars = true
    }

    required init(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    override func viewDidLoad() {
        
        view.backgroundColor = MainAppTheme.list.backyardColor
        
        tokenView.delegate = self
        tokenView.backgroundColor = MainAppTheme.list.backyardColor
        tokenView.fieldName = ""
        tokenView.placeholderText = localized("CreateGroupMembersPlaceholders")
        
        tableView.keyboardDismissMode = UIScrollViewKeyboardDismissMode.OnDrag
        
        self.view.addSubview(tableView)
        self.view.addSubview(tokenView)
        
        bindTable(tableView, fade: true)
        
        super.viewDidLoad()
    }
    
    func doNext() {
        let res = IOSIntArray(length: UInt(selected.count))
        for i in 0..<selected.count {
            res.replaceIntAtIndex(UInt(i), withInt: selected[i].contact.getUid())
        }
        execute(Actor.createGroupCommandWithTitle(groupTitle, withAvatar: nil, withUids: res), successBlock: { (val) -> Void in
            let gid = (val as! JavaLangInteger).intValue
            if self.groupImage != nil {
                Actor.changeGroupAvatar(gid, image: self.groupImage!)
            }
            self.navigateNext(ConversationViewController(peer: ACPeer.groupWithInt(gid)), removeCurrent: true)
        }) { (val) -> Void in
            
        }
    }
    
    func tableView(tableView: UITableView, didSelectRowAtIndexPath indexPath: NSIndexPath) {
        tableView.deselectRowAtIndexPath(indexPath, animated: true)
        
        let contact = objectAtIndexPath(indexPath) as! ACContact
        
        for i in 0..<selected.count {
            let n = selected[i]
            if (n.contact.getUid() == contact.getUid()) {
                selected.removeAtIndex(i)
                tokenView.removeToken(n.token)
                return
            }
        }
        
        let token = CLToken(displayText: contact.getName(), context: nil)
        tokenView.addToken(token)
        selected.append(TokenRef(contact: contact, token: token))
        filter("")
    }

    func tokenInputView(view: CLTokenInputView!, didChangeText text: String!) {
        filter(text)
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
