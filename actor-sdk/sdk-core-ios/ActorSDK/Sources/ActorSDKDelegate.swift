//
//  Copyright (c) 2014-2016 Actor LLC. <https://actor.im>
//

import Foundation

/// Actor SDK Delegate that helps you customize logic of messenger
public protocol ActorSDKDelegate {
    
    /// Create initial Auth contoller. With Navigation controller (if needed).
    func actorControllerForAuthStart() -> UIViewController?
    
    /// Create root logged in controller. With Navigation controller (if needed).
    func actorControllerForStart() -> UIViewController?

    /// Create root controller after successful login. With Navigation controller (if needed).
    func actorControllerAfterLogIn() -> UIViewController?
    
    /// User profile controller
    func actorControllerForUser(uid: Int) -> AAViewController?
    
    /// User profile controller
    func actorControllerForGroup(gid: Int) -> AAViewController?
    
    /// Conversation controller
    func actorControllerForConversation(peer: ACPeer) -> UIViewController?
    
    /// Contacts controller
    func actorControllerForContacts() -> UIViewController?
    
    /// Dialogs controller
    func actorControllerForDialogs() -> UIViewController?
    
    /// Settings controller
    func actorControllerForSettings() -> UIViewController?
    
    /// Root controllers
    func actorRootControllers() -> [UIViewController]?
    
    /// Root Intial controller
    func actorRootInitialControllerIndex() -> Int?
    
    /// Configuration of bubble cells
    func actorConfigureBubbleLayouters(builtIn: [AABubbleLayouter]) -> [AABubbleLayouter]
    
    /// Conversation custom attach menu
    func actorConversationCustomAttachMenu(controller: UIViewController) -> Bool
    
    /// Called after header is created in settings page
    func actorSettingsHeaderDidCreated(controller: AASettingsViewController, section: AAManagedSection)

    /// Called after header is created in settings page
    func actorSettingsConfigurationWillCreated(controller: AASettingsViewController, section: AAManagedSection)

    /// Called after header is created in settings page
    func actorSettingsConfigurationDidCreated(controller: AASettingsViewController, section: AAManagedSection)
    
    /// Called after header is created in settings page
    func actorSettingsSupportWillCreated(controller: AASettingsViewController, section: AAManagedSection)
    
    /// Called after header is created in settings page
    func actorSettingsSupportDidCreated(controller: AASettingsViewController, section: AAManagedSection)
}

/// Default empty implementation of SDK Delegate
public class ActorSDKDelegateDefault: NSObject, ActorSDKDelegate {

    public func actorControllerForAuthStart() -> UIViewController? {
        return nil
    }
    
    public func actorControllerForStart() -> UIViewController? {
        return nil
    }
    
    public func actorControllerForUser(uid: Int) -> AAViewController? {
        return nil
    }
    
    public func actorControllerForGroup(gid: Int) -> AAViewController? {
        return nil
    }
    
    public func actorControllerForConversation(peer: ACPeer) -> UIViewController? {
        return nil
    }
    
    public func actorControllerForContacts() -> UIViewController? {
        return nil
    }
    
    public func actorControllerForDialogs() -> UIViewController? {
        return nil
    }
    
    public func actorControllerForSettings() -> UIViewController? {
        return nil
    }
    
    public func actorRootControllers() -> [UIViewController]? {
        return nil
    }
    
    public func actorRootInitialControllerIndex() -> Int? {
        return nil
    }
    
    public func actorConfigureBubbleLayouters(builtIn: [AABubbleLayouter]) -> [AABubbleLayouter] {
        return builtIn
    }
    
    public func actorControllerAfterLogIn() -> UIViewController? {
        return nil
    }
    
    public func actorConversationCustomAttachMenu(controller: UIViewController) -> Bool {
        return false
    }
    
    public func actorSettingsHeaderDidCreated(controller: AASettingsViewController, section: AAManagedSection) {
        
    }
    
    public func actorSettingsConfigurationWillCreated(controller: AASettingsViewController, section: AAManagedSection) {
        
    }
    
    public func actorSettingsConfigurationDidCreated(controller: AASettingsViewController, section: AAManagedSection) {
        
    }
    
    public func actorSettingsSupportWillCreated(controller: AASettingsViewController, section: AAManagedSection) {
        
    }
    
    public func actorSettingsSupportDidCreated(controller: AASettingsViewController, section: AAManagedSection) {
        
    }
}