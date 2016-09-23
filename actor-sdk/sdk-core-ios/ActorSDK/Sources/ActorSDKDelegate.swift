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
    func actorControllerForUser(_ uid: Int) -> AAViewController?
    
    /// User profile controller
    func actorControllerForGroup(_ gid: Int) -> AAViewController?
    
    /// Conversation controller
    func actorControllerForConversation(_ peer: ACPeer) -> UIViewController?
    
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
    func actorConfigureBubbleLayouters(_ builtIn: [AABubbleLayouter]) -> [AABubbleLayouter]
    
    /// Conversation custom attach menu
    func actorConversationCustomAttachMenu(_ controller: UIViewController) -> Bool
    
    /// Called after header is created in settings page
    func actorSettingsHeaderDidCreated(_ controller: AASettingsViewController, section: AAManagedSection)

    /// Called after header is created in settings page
    func actorSettingsConfigurationWillCreated(_ controller: AASettingsViewController, section: AAManagedSection)

    /// Called after header is created in settings page
    func actorSettingsConfigurationDidCreated(_ controller: AASettingsViewController, section: AAManagedSection)
    
    /// Called after header is created in settings page
    func actorSettingsSupportWillCreated(_ controller: AASettingsViewController, section: AAManagedSection)
    
    /// Called after header is created in settings page
    func actorSettingsSupportDidCreated(_ controller: AASettingsViewController, section: AAManagedSection)
}

/// Default empty implementation of SDK Delegate
open class ActorSDKDelegateDefault: NSObject, ActorSDKDelegate {

    open func actorControllerForAuthStart() -> UIViewController? {
        return nil
    }
    
    open func actorControllerForStart() -> UIViewController? {
        return nil
    }
    
    open func actorControllerForUser(_ uid: Int) -> AAViewController? {
        return nil
    }
    
    open func actorControllerForGroup(_ gid: Int) -> AAViewController? {
        return nil
    }
    
    open func actorControllerForConversation(_ peer: ACPeer) -> UIViewController? {
        return nil
    }
    
    open func actorControllerForContacts() -> UIViewController? {
        return nil
    }
    
    open func actorControllerForDialogs() -> UIViewController? {
        return nil
    }
    
    open func actorControllerForSettings() -> UIViewController? {
        return nil
    }
    
    open func actorRootControllers() -> [UIViewController]? {
        return nil
    }
    
    open func actorRootInitialControllerIndex() -> Int? {
        return nil
    }
    
    open func actorConfigureBubbleLayouters(_ builtIn: [AABubbleLayouter]) -> [AABubbleLayouter] {
        return builtIn
    }
    
    open func actorControllerAfterLogIn() -> UIViewController? {
        return nil
    }
    
    open func actorConversationCustomAttachMenu(_ controller: UIViewController) -> Bool {
        return false
    }
    
    open func actorSettingsHeaderDidCreated(_ controller: AASettingsViewController, section: AAManagedSection) {
        
    }
    
    open func actorSettingsConfigurationWillCreated(_ controller: AASettingsViewController, section: AAManagedSection) {
        
    }
    
    open func actorSettingsConfigurationDidCreated(_ controller: AASettingsViewController, section: AAManagedSection) {
        
    }
    
    open func actorSettingsSupportWillCreated(_ controller: AASettingsViewController, section: AAManagedSection) {
        
    }
    
    open func actorSettingsSupportDidCreated(_ controller: AASettingsViewController, section: AAManagedSection) {
        
    }
}
