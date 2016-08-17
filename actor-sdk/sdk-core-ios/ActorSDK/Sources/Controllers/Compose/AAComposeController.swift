//
//  Copyright (c) 2014-2016 Actor LLC. <https://actor.im>
//

import UIKit

public class AAComposeController: AAContactsListContentController, AAContactsListContentControllerDelegate {

    public override init() {
        super.init()
        
        self.delegate = self
        self.isSearchAutoHide = false
        
        self.navigationItem.title = AALocalized("ComposeTitle")
        
        if AADevice.isiPad {
            self.navigationItem.leftBarButtonItem = UIBarButtonItem(title: AALocalized("NavigationCancel"), style: UIBarButtonItemStyle.Plain, target: self, action: #selector(AAViewController.dismiss))
        }
    }

    public required init(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    public func willAddContacts(controller: AAContactsListContentController, section: AAManagedSection) {
        
        if ActorSDK.sharedActor().enableSecretChats {
            section.custom { (r:AACustomRow<AAContactActionCell>) -> () in
                
                r.height = 56
                
                r.closure = { (cell) -> () in
                    cell.bind("ic_secret", actionTitle: AALocalized("ActionStartSecret"))
                }
                
                r.selectAction = { () -> Bool in
                    self.navigateNext(AAComposeSecretController(), removeCurrent: true)
                    return false
                }
            }
        }
        
        section.custom { (r:AACustomRow<AAContactActionCell>) -> () in
            
            r.height = 56
            
            r.closure = { (cell) -> () in
                cell.bind("ic_add_user", actionTitle: AALocalized("CreateGroup"))
            }
            
            r.selectAction = { () -> Bool in
                self.navigateNext(AAGroupCreateViewController(isChannel: false), removeCurrent: true)
                return false
            }
        }
        
        section.custom { (r:AACustomRow<AAContactActionCell>) -> () in
            
            r.height = 56
            
            r.closure = { (cell) -> () in
                cell.bind("ic_create_channel", actionTitle: AALocalized("CreateChannel"))
            }
            
            r.selectAction = { () -> Bool in
                self.navigateNext(AAGroupCreateViewController(isChannel: true), removeCurrent: true)
                return false
            }
        }
    }
    
    public func contactDidTap(controller: AAContactsListContentController, contact: ACContact) -> Bool {
        let peer = ACPeer_userWithInt_(contact.uid)
        if let customController = ActorSDK.sharedActor().delegate.actorControllerForConversation(peer) {
            navigateDetail(customController)
        } else {
            navigateDetail(ConversationViewController(peer: peer))
        }
        return false
    }
}