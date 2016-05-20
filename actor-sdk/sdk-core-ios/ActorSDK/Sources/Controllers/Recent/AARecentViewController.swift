//
//  Copyright (c) 2014-2016 Actor LLC. <https://actor.im>
//

import UIKit

public class AARecentViewController: AADialogsListContentController, AADialogsListContentControllerDelegate {

    public override init() {
        
        super.init()
        
        // Enabling dialogs page tracking
        
        content = ACAllEvents_Main.RECENT()
        
        // Setting delegate
        
        self.delegate = self
    }

    public required init(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    public override func viewDidLoad() {
        super.viewDidLoad()
        
        // Setting UITabBarItem
        
        tabBarItem = UITabBarItem(title: "TabMessages", img: "TabIconChats", selImage: "TabIconChatsHighlighted")
        
        // Setting navigation item
        
        navigationItem.title = AALocalized("TabMessages")
        navigationItem.leftBarButtonItem = editButtonItem()
        navigationItem.leftBarButtonItem!.title = AALocalized("NavigationEdit")
        navigationItem.rightBarButtonItem = UIBarButtonItem(barButtonSystemItem: UIBarButtonSystemItem.Compose, target: self, action: #selector(AARecentViewController.compose))
    }
    
    // Implemention of editing
    
    public override func setEditing(editing: Bool, animated: Bool) {
        super.setEditing(editing, animated: animated)
        tableView.setEditing(editing, animated: animated)
        
        if (editing) {
            self.navigationItem.leftBarButtonItem!.title = AALocalized("NavigationDone")
            self.navigationItem.leftBarButtonItem!.style = UIBarButtonItemStyle.Done
            
            navigationItem.rightBarButtonItem = nil
        }
        else {
            self.navigationItem.leftBarButtonItem!.title = AALocalized("NavigationEdit")
            self.navigationItem.leftBarButtonItem!.style = UIBarButtonItemStyle.Plain
            
            navigationItem.rightBarButtonItem = UIBarButtonItem(barButtonSystemItem: UIBarButtonSystemItem.Compose, target: self, action: #selector(AARecentViewController.compose))
        }
        
        if editing == true {
            navigationItem.rightBarButtonItem = nil
        } else {
            navigationItem.rightBarButtonItem = UIBarButtonItem(barButtonSystemItem: UIBarButtonSystemItem.Compose, target: self, action: #selector(AARecentViewController.compose))
        }
    }
    
    public func compose() {
        if AADevice.isiPad {
            self.presentElegantViewController(AANavigationController(rootViewController: AAComposeController()))
        } else {
            navigateNext(AAComposeController())
        }
    }
    
    // Tracking app state
    
    public override func viewWillAppear(animated: Bool) {
        super.viewWillAppear(animated)
        
        binder.bind(Actor.getGlobalState().globalCounter, closure: { (value: JavaLangInteger?) -> () in
            if value != nil {
                if value!.integerValue > 0 {
                    self.tabBarItem.badgeValue = "\(value!.integerValue)"
                } else {
                    self.tabBarItem.badgeValue = nil
                }
            } else {
                self.tabBarItem.badgeValue = nil
            }
        })
    }
    
    public override func viewDidAppear(animated: Bool) {
        super.viewDidAppear(animated)
        Actor.onDialogsOpen()
    }
    
    public override func viewDidDisappear(animated: Bool) {
        super.viewDidDisappear(animated)
        Actor.onDialogsClosed()
    }
    
    // Handling selections
    
    public func recentsDidTap(controller: AADialogsListContentController, dialog: ACDialog) -> Bool {
        if let customController = ActorSDK.sharedActor().delegate.actorControllerForConversation(dialog.peer) {
            self.navigateDetail(customController)
        } else {
            self.navigateDetail(ConversationViewController(peer: dialog.peer))
        }
        return false
    }
    
    public func searchDidTap(controller: AADialogsListContentController, entity: ACSearchEntity) {
        if let customController = ActorSDK.sharedActor().delegate.actorControllerForConversation(entity.peer) {
            self.navigateDetail(customController)
        } else {
            self.navigateDetail(ConversationViewController(peer: entity.peer))
        }
    }
}
