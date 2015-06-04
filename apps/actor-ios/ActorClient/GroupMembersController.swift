//
//  Copyright (c) 2015 Actor LLC. <https://actor.im>
//

import UIKit

class GroupMembersController: ContactsBaseController, VENTokenFieldDataSource, VENTokenFieldDelegate {

    @IBOutlet weak var tokenField: UIView!
    @IBOutlet weak var contactsTable: UITableView!
    var tokenFieldView: VENTokenField!;
    var selectedNames: Array<AMContact> = []
    var groupTitle: String!
    init(title: String) {
        super.init(contentSection: 0, nibName: "GroupMembersController", bundle: nil)

        groupTitle = title
        navigationItem.title = "Group Members";
        navigationItem.rightBarButtonItem = UIBarButtonItem(title: "Next", style: UIBarButtonItemStyle.Done, target: self, action: "doNext")
    }

    required init(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    override func viewDidLoad() {
        
        tokenFieldView = VENTokenField(frame: CGRectMake(0, 66, tokenField.frame.width, 48))
        tokenFieldView.delegate = self
        tokenFieldView.dataSource = self
        tokenFieldView.maxHeight = 48
        tokenFieldView.contentMode = UIViewContentMode.Center
        tokenFieldView.inputTextFieldTextColor = MainAppTheme.common.tokenFieldText
        tokenFieldView.setColorScheme(MainAppTheme.common.tokenFieldBg)
        tokenFieldView.backgroundColor = MainAppTheme.list.backyardColor
        
        tokenField.addSubview(tokenFieldView)
        tokenField.backgroundColor = MainAppTheme.list.backyardColor
        view.backgroundColor = MainAppTheme.list.backyardColor
        
        bindTable(contactsTable, fade: true)
        
        super.viewDidLoad()
    }
    
    func doNext() {
        var res = IOSIntArray(length: UInt(selectedNames.count))
        for i in 0..<selectedNames.count {
            res.replaceIntAtIndex(UInt(i), withInt: selectedNames[i].getUid())
        }
        execute(MSG.createGroupCommandWithTitle(groupTitle, withAvatar: nil, withUids: res), successBlock: { (val) -> Void in
            var gid = val as! JavaLangInteger
            self.navigateNext(AAConversationController(peer: AMPeer.groupWithInt(gid.intValue)), removeCurrent: true)
        }) { (val) -> Void in
            
        }
    }
    
    func tokenField(tokenField: VENTokenField!, didChangeText text: String!) {
        filter(text)
    }
    
    func tableView(tableView: UITableView, didSelectRowAtIndexPath indexPath: NSIndexPath) {
        var contact = objectAtIndexPath(indexPath) as! AMContact
        var isRemoved = false
        for i in 0..<selectedNames.count {
            var n = selectedNames[i]
            if (n.getUid() == contact.getUid()) {
                selectedNames.removeAtIndex(i)
                isRemoved = true
                break
            }
        }
        
        if (!isRemoved) {
            selectedNames.append(contact)
            filter("")
        }
        tokenFieldView?.reloadData()
        
        tableView.deselectRowAtIndexPath(indexPath, animated: true)
    }
    
    func tokenField(tokenField: VENTokenField!, didDeleteTokenAtIndex index: UInt) {
        self.selectedNames.removeAtIndex(Int(index))
        tokenFieldView?.reloadData()
    }
    
    func tokenField(tokenField: VENTokenField!, titleForTokenAtIndex index: UInt) -> String! {
        return self.selectedNames[Int(index)].getName()
    }
    
    func tokenFieldCollapsedText(tokenField: VENTokenField!) -> String! {
        return "selected \(self.selectedNames.count)"
    }
    
    func numberOfTokensInTokenField(tokenField: VENTokenField!) -> UInt {
        return UInt(self.selectedNames.count)
    }
}
