//
//  Copyright (c) 2014-2016 Actor LLC. <https://actor.im>
//

import UIKit

open class AASettingsNotificationsViewController: AATableViewController {
    
    // MARK: -
    // MARK: Constructors
    
    fileprivate let CellIdentifier = "CellIdentifier"
    
    public init() {
        super.init(style: UITableViewStyle.grouped)
        
        title = AALocalized("NotificationsTitle")
        
        content = ACAllEvents_Settings.notifications()
    }
    
    public required init(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    // MARK: -
    
    open override func viewDidLoad() {
        super.viewDidLoad()
        
        tableView.register(AACommonCell.self, forCellReuseIdentifier: CellIdentifier)
        tableView.backgroundColor = appStyle.vcBackyardColor
        tableView.separatorStyle = UITableViewCellSeparatorStyle.none
        
        view.backgroundColor = tableView.backgroundColor
    }
    
    // MARK: - 
    // MARK: UITableView Data Source
    
    open override func numberOfSections(in tableView: UITableView) -> Int {
        return 4
    }
    
    open override func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        if (section == 0) {
            return 1
        } else if (section == 1) {
            return Actor.isNotificationsEnabled() ? 2 : 1
        } else if (section == 2) {
            return Actor.isGroupNotificationsEnabled() ? 2 : 1
        } else if (section == 3) {
            return 1
        }
        
        return 1
    }
    
    open func tableView(_ tableView: UITableView, titleForHeaderInSection section: Int) -> String? {
        if (section == 0) {
            return AALocalized("NotificationsEffectsTitle")
        } else if (section == 1) {
            return AALocalized("NotificationsMobileTitle")
        } else if (section == 2) {
            return AALocalized("NotificationsGroups")
        } else if (section == 3) {
            return AALocalized("NotificationsPrivacyTitle")
        }
        
        return nil
    }
    
    open func tableView(_ tableView: UITableView, titleForFooterInSection section: Int) -> String? {
        if (section == 1) {
            return AALocalized("NotificationsNotificationHint")
        } else if (section == 2) {
            return AALocalized("NotificationsOnlyMentionsHint")
        } else if (section == 3) {
            return AALocalized("NotificationsPreviewHint")
        }
        
        return nil
    }
    
    fileprivate func notificationsTonesCell(_ indexPath: IndexPath) -> AACommonCell {
        let cell = tableView.dequeueReusableCell(withIdentifier: CellIdentifier, for: indexPath) as! AACommonCell
        
        cell.setContent(AALocalized("NotificationsSoundEffects"))
        cell.style = .switch
        cell.selectionStyle = UITableViewCellSelectionStyle.none
        cell.bottomSeparatorVisible = true
        cell.topSeparatorVisible = true
        cell.bottomSeparatorLeftInset = 0
        cell.topSeparatorLeftInset = 0
        
        cell.setSwitcherOn(Actor.isConversationTonesEnabled())
        cell.switchBlock = { (nValue: Bool) in
            Actor.changeConversationTonesEnabled(withValue: nValue)
        }
        
        return cell
    }
    
    fileprivate func notificationsEnableCell(_ indexPath: IndexPath) -> AACommonCell {
        let cell = tableView.dequeueReusableCell(withIdentifier: CellIdentifier, for: indexPath) as! AACommonCell
        
        cell.setContent(AALocalized("NotificationsEnable"))
        cell.style = .switch
        cell.selectionStyle = UITableViewCellSelectionStyle.none
        cell.bottomSeparatorVisible = true
        cell.topSeparatorVisible = true
        cell.bottomSeparatorLeftInset = 0
        cell.topSeparatorLeftInset = 0
        
        cell.setSwitcherOn(Actor.isNotificationsEnabled())
        cell.switchBlock = { (nValue: Bool) in
            self.tableView.beginUpdates()
            Actor.changeNotificationsEnabled(withValue: nValue)
            let rows = [IndexPath(row: 1, section: (indexPath as NSIndexPath).section)]
            if (nValue) {
                self.tableView.insertRows(at: rows, with: UITableViewRowAnimation.middle)
            } else {
                self.tableView.deleteRows(at: rows, with: UITableViewRowAnimation.middle)
            }
            self.tableView.endUpdates()
        }
        
        return cell
    }
    
    fileprivate func notificationsAlertCell(_ indexPath: IndexPath) -> AACommonCell {
        let cell = tableView.dequeueReusableCell(withIdentifier: CellIdentifier, for: indexPath) as! AACommonCell
        
        cell.setContent(AALocalized("NotificationsSound"))
        cell.style = .switch
        cell.selectionStyle = UITableViewCellSelectionStyle.none
        cell.bottomSeparatorVisible = true
        //cell.topSeparatorVisible = true
        cell.bottomSeparatorLeftInset = 0
        cell.topSeparatorLeftInset = 0
        
        cell.setSwitcherOn(Actor.isNotificationSoundEnabled())
        cell.setSwitcherEnabled(Actor.isNotificationsEnabled())
        cell.switchBlock = { (nValue: Bool) in
            Actor.changeNotificationSoundEnabled(withValue: nValue)
        }
        
        return cell
    }
    
    
    fileprivate func groupEnabledCell(_ indexPath: IndexPath) -> AACommonCell {
        let cell = tableView.dequeueReusableCell(withIdentifier: CellIdentifier, for: indexPath) as! AACommonCell
        
        cell.setContent(AALocalized("NotificationsEnable"))
        cell.style = .switch
        cell.selectionStyle = UITableViewCellSelectionStyle.none
        cell.topSeparatorVisible = true
        cell.bottomSeparatorVisible = true
        cell.bottomSeparatorLeftInset = 0
        cell.topSeparatorLeftInset = 0
        
        cell.setSwitcherOn(Actor.isGroupNotificationsEnabled())
        cell.switchBlock = { (nValue: Bool) in
            self.tableView.beginUpdates()
            Actor.changeGroupNotificationsEnabled(nValue)
            let rows = [IndexPath(row: 1, section: (indexPath as NSIndexPath).section)]
            if (nValue) {
                self.tableView.insertRows(at: rows, with: UITableViewRowAnimation.middle)
            } else {
                self.tableView.deleteRows(at: rows, with: UITableViewRowAnimation.middle)
            }
            self.tableView.endUpdates()
        }
        
        return cell
    }
    
    fileprivate func groupEnabledMentionsCell(_ indexPath: IndexPath) -> AACommonCell {
        let cell = tableView.dequeueReusableCell(withIdentifier: CellIdentifier, for: indexPath) as! AACommonCell
        
        cell.setContent(AALocalized("NotificationsOnlyMentions"))
        cell.style = .switch
        cell.selectionStyle = UITableViewCellSelectionStyle.none
        cell.bottomSeparatorVisible = true
        //cell.topSeparatorVisible = true
        cell.bottomSeparatorLeftInset = 0
        cell.topSeparatorLeftInset = 0
        
        cell.setSwitcherOn(Actor.isGroupNotificationsOnlyMentionsEnabled())
        cell.setSwitcherEnabled(Actor.isGroupNotificationsEnabled())
        cell.switchBlock = { (nValue: Bool) in
            Actor.changeGroupNotificationsOnlyMentionsEnabled(nValue)
        }
        
        return cell
    }

    fileprivate func inAppAlertCell(_ indexPath: IndexPath) -> AACommonCell {
        let cell = tableView.dequeueReusableCell(withIdentifier: CellIdentifier, for: indexPath) as! AACommonCell
        
        cell.setContent(AALocalized("NotificationsEnable"))
        cell.style = .switch
        cell.selectionStyle = UITableViewCellSelectionStyle.none
        cell.bottomSeparatorVisible = true
        cell.topSeparatorVisible = true
        cell.bottomSeparatorLeftInset = 0
        cell.topSeparatorLeftInset = 0
        
        cell.setSwitcherOn(Actor.isInAppNotificationsEnabled())
        cell.switchBlock = { (nValue: Bool) in
            self.tableView.beginUpdates()
            Actor.changeInAppNotificationsEnabled(withValue: nValue)
            let rows = [IndexPath(row: 1, section: 3), IndexPath(row: 2, section: 3)]
            if (nValue) {
                self.tableView.insertRows(at: rows, with: UITableViewRowAnimation.middle)
            } else {
                self.tableView.deleteRows(at: rows, with: UITableViewRowAnimation.middle)
            }
            self.tableView.endUpdates()
        }
        
        return cell
    }
    
    fileprivate func inAppSoundCell(_ indexPath: IndexPath) -> AACommonCell {
        let cell = tableView.dequeueReusableCell(withIdentifier: CellIdentifier, for: indexPath) as! AACommonCell
        
        cell.setContent(AALocalized("NotificationsSound"))
        cell.style = .switch
        cell.selectionStyle = UITableViewCellSelectionStyle.none
        cell.bottomSeparatorVisible = true
        // cell.topSeparatorVisible = true
        cell.bottomSeparatorLeftInset = 0
        cell.topSeparatorLeftInset = 0
        
        cell.setSwitcherOn(Actor.isInAppNotificationSoundEnabled())
        cell.setSwitcherEnabled(Actor.isInAppNotificationsEnabled())
        
        cell.switchBlock = { (nValue: Bool) in
            Actor.changeInAppNotificationSoundEnabled(withValue: nValue)
        }
        
        return cell
    }

    
    fileprivate func inAppVibrateCell(_ indexPath: IndexPath) -> AACommonCell {
        let cell = tableView.dequeueReusableCell(withIdentifier: CellIdentifier, for: indexPath) as! AACommonCell
        
        cell.setContent(AALocalized("NotificationsVibration"))
        cell.style = .switch
        cell.selectionStyle = UITableViewCellSelectionStyle.none
        cell.bottomSeparatorVisible = true
        //cell.topSeparatorVisible = true
        cell.bottomSeparatorLeftInset = 0
        cell.topSeparatorLeftInset = 0
        
        cell.setSwitcherOn(Actor.isInAppNotificationVibrationEnabled())
        cell.setSwitcherEnabled(Actor.isInAppNotificationsEnabled())
        
        cell.switchBlock = { (nValue: Bool) in
            Actor.changeInAppNotificationVibrationEnabled(withValue: nValue)
        }
        
        return cell
    }

    fileprivate func notificationsPreviewCell(_ indexPath: IndexPath) -> AACommonCell {
        let cell = tableView.dequeueReusableCell(withIdentifier: CellIdentifier, for: indexPath) as! AACommonCell
        
        cell.setContent(AALocalized("NotificationsPreview"))
        cell.style = .switch
        cell.selectionStyle = UITableViewCellSelectionStyle.none
        cell.bottomSeparatorVisible = true
        cell.topSeparatorVisible = true
        cell.bottomSeparatorLeftInset = 0
        cell.topSeparatorLeftInset = 0
        
        cell.setSwitcherOn(Actor.isShowNotificationsText())
        cell.switchBlock = { (nValue: Bool) in
            Actor.changeShowNotificationTextEnabled(withValue: nValue)
        }
        
        return cell
    }
    
    open override func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        if (indexPath as NSIndexPath).section == 0 {
            return notificationsTonesCell(indexPath)
        } else if ((indexPath as NSIndexPath).section == 1) {
            if ((indexPath as NSIndexPath).row == 0) {
                return notificationsEnableCell(indexPath)
            } else if ((indexPath as NSIndexPath).row == 1) {
                return notificationsAlertCell(indexPath)
            }
        } else if ((indexPath as NSIndexPath).section == 2) {
            if ((indexPath as NSIndexPath).row == 0) {
                return groupEnabledCell(indexPath)
            } else {
                return groupEnabledMentionsCell(indexPath)
            }
        } else if ((indexPath as NSIndexPath).section == 3) {
            return notificationsPreviewCell(indexPath)
        }
        
        return UITableViewCell()
    }
    
    open func tableView(_ tableView: UITableView, willDisplayHeaderView view: UIView, forSection section: Int) {
        let header: UITableViewHeaderFooterView = view as! UITableViewHeaderFooterView
        header.textLabel!.textColor = ActorSDK.sharedActor().style.cellFooterColor
    }
    
    open func tableView(_ tableView: UITableView, willDisplayFooterView view: UIView, forSection section: Int) {
        let header: UITableViewHeaderFooterView = view as! UITableViewHeaderFooterView
        header.textLabel!.textColor = ActorSDK.sharedActor().style.cellFooterColor
    }
}
