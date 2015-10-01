//
//  Copyright (c) 2014-2015 Actor LLC. <https://actor.im>
//

import UIKit

class SettingsNotificationsViewController: AATableViewController {
    
    // MARK: -
    // MARK: Constructors
    
    private let CellIdentifier = "CellIdentifier"
    
    init() {
        super.init(style: UITableViewStyle.Grouped)
        
        title = NSLocalizedString("NotificationsTitle", comment: "Notifcations and Sounds")
        
        content = ACAllEvents_Settings.NOTIFICATIONS()
    }
    
    required init(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    // MARK: -
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        tableView.registerClass(CommonCell.self, forCellReuseIdentifier: CellIdentifier)
        tableView.backgroundColor = MainAppTheme.list.backyardColor
        tableView.separatorStyle = UITableViewCellSeparatorStyle.None
    }
    
    // MARK: - 
    // MARK: UITableView Data Source
    
    override func numberOfSectionsInTableView(tableView: UITableView) -> Int {
        return 4
    }
    
    override func tableView(tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
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
    
    func tableView(tableView: UITableView, titleForHeaderInSection section: Int) -> String? {
        if (section == 0) {
            return NSLocalizedString("NotificationsEffectsTitle", comment: "Effects")
        } else if (section == 1) {
            return NSLocalizedString("NotificationsMobileTitle", comment: "Mobile Notifications")
        } else if (section == 2) {
            return NSLocalizedString("NotificationsGroups", comment: "Group Notifications")
        } else if (section == 3) {
            return NSLocalizedString("NotificationsPrivacyTitle", comment: "Privacy")
        }
        
        return nil
    }
    
    func tableView(tableView: UITableView, titleForFooterInSection section: Int) -> String? {
        if (section == 1) {
            return NSLocalizedString("NotificationsNotificationHint", comment: "Disable hint")
        } else if (section == 2) {
            return NSLocalizedString("NotificationsOnlyMentionsHint", comment: "Only Mentions hint")
        } else if (section == 3) {
            return NSLocalizedString("NotificationsPreviewHint", comment: "Preview hint")
        }
        
        return nil
    }
    
    private func notificationsTonesCell(indexPath: NSIndexPath) -> CommonCell {
        let cell = tableView.dequeueReusableCellWithIdentifier(CellIdentifier, forIndexPath: indexPath) as! CommonCell
        
        cell.setContent(NSLocalizedString("NotificationsSoundEffects", comment: "Sound Effects"))
        cell.style = .Switch
        cell.selectionStyle = UITableViewCellSelectionStyle.None
        cell.bottomSeparatorVisible = true
        cell.topSeparatorVisible = true
        cell.bottomSeparatorLeftInset = 0
        cell.topSeparatorLeftInset = 0
        
        cell.setSwitcherOn(Actor.isConversationTonesEnabled())
        cell.switchBlock = { (nValue: Bool) in
            Actor.changeConversationTonesEnabledWithValue(nValue)
        }
        
        return cell
    }
    
    private func notificationsEnableCell(indexPath: NSIndexPath) -> CommonCell {
        let cell = tableView.dequeueReusableCellWithIdentifier(CellIdentifier, forIndexPath: indexPath) as! CommonCell
        
        cell.setContent(NSLocalizedString("NotificationsEnable", comment: "Enable"))
        cell.style = .Switch
        cell.selectionStyle = UITableViewCellSelectionStyle.None
        cell.bottomSeparatorVisible = true
        cell.topSeparatorVisible = true
        cell.bottomSeparatorLeftInset = 0
        cell.topSeparatorLeftInset = 0
        
        cell.setSwitcherOn(Actor.isNotificationsEnabled())
        cell.switchBlock = { (nValue: Bool) in
            self.tableView.beginUpdates()
            Actor.changeNotificationsEnabledWithValue(nValue)
            let rows = [NSIndexPath(forRow: 1, inSection: indexPath.section)]
            if (nValue) {
                self.tableView.insertRowsAtIndexPaths(rows, withRowAnimation: UITableViewRowAnimation.Middle)
            } else {
                self.tableView.deleteRowsAtIndexPaths(rows, withRowAnimation: UITableViewRowAnimation.Middle)
            }
            self.tableView.endUpdates()
        }
        
        return cell
    }
    
    private func notificationsAlertCell(indexPath: NSIndexPath) -> CommonCell {
        let cell = tableView.dequeueReusableCellWithIdentifier(CellIdentifier, forIndexPath: indexPath) as! CommonCell
        
        cell.setContent(NSLocalizedString("NotificationsSound", comment: "Sound"))
        cell.style = .Switch
        cell.selectionStyle = UITableViewCellSelectionStyle.None
        cell.bottomSeparatorVisible = true
        //cell.topSeparatorVisible = true
        cell.bottomSeparatorLeftInset = 0
        cell.topSeparatorLeftInset = 0
        
        cell.setSwitcherOn(Actor.isNotificationSoundEnabled())
        cell.setSwitcherEnabled(Actor.isNotificationsEnabled())
        cell.switchBlock = { (nValue: Bool) in
            Actor.changeNotificationSoundEnabledWithValue(nValue)
        }
        
        return cell
    }
    
    
    private func groupEnabledCell(indexPath: NSIndexPath) -> CommonCell {
        let cell = tableView.dequeueReusableCellWithIdentifier(CellIdentifier, forIndexPath: indexPath) as! CommonCell
        
        cell.setContent(NSLocalizedString("NotificationsEnable", comment: "Enable"))
        cell.style = .Switch
        cell.selectionStyle = UITableViewCellSelectionStyle.None
        cell.topSeparatorVisible = true
        cell.bottomSeparatorVisible = true
        cell.bottomSeparatorLeftInset = 0
        cell.topSeparatorLeftInset = 0
        
        cell.setSwitcherOn(Actor.isGroupNotificationsEnabled())
        cell.switchBlock = { (nValue: Bool) in
            self.tableView.beginUpdates()
            Actor.changeGroupNotificationsEnabled(nValue)
            let rows = [NSIndexPath(forRow: 1, inSection: indexPath.section)]
            if (nValue) {
                self.tableView.insertRowsAtIndexPaths(rows, withRowAnimation: UITableViewRowAnimation.Middle)
            } else {
                self.tableView.deleteRowsAtIndexPaths(rows, withRowAnimation: UITableViewRowAnimation.Middle)
            }
            self.tableView.endUpdates()
        }
        
        return cell
    }
    
    private func groupEnabledMentionsCell(indexPath: NSIndexPath) -> CommonCell {
        let cell = tableView.dequeueReusableCellWithIdentifier(CellIdentifier, forIndexPath: indexPath) as! CommonCell
        
        cell.setContent(NSLocalizedString("NotificationsOnlyMentions", comment: "Mentions"))
        cell.style = .Switch
        cell.selectionStyle = UITableViewCellSelectionStyle.None
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

    private func inAppAlertCell(indexPath: NSIndexPath) -> CommonCell {
        let cell = tableView.dequeueReusableCellWithIdentifier(CellIdentifier, forIndexPath: indexPath) as! CommonCell
        
        cell.setContent(NSLocalizedString("NotificationsEnable", comment: "Enable"))
        cell.style = .Switch
        cell.selectionStyle = UITableViewCellSelectionStyle.None
        cell.bottomSeparatorVisible = true
        cell.topSeparatorVisible = true
        cell.bottomSeparatorLeftInset = 0
        cell.topSeparatorLeftInset = 0
        
        cell.setSwitcherOn(Actor.isInAppNotificationsEnabled())
        cell.switchBlock = { (nValue: Bool) in
            self.tableView.beginUpdates()
            Actor.changeInAppNotificationsEnabledWithValue(nValue)
            let rows = [NSIndexPath(forRow: 1, inSection: 3), NSIndexPath(forRow: 2, inSection: 3)]
            if (nValue) {
                self.tableView.insertRowsAtIndexPaths(rows, withRowAnimation: UITableViewRowAnimation.Middle)
            } else {
                self.tableView.deleteRowsAtIndexPaths(rows, withRowAnimation: UITableViewRowAnimation.Middle)
            }
            self.tableView.endUpdates()
        }
        
        return cell
    }
    
    private func inAppSoundCell(indexPath: NSIndexPath) -> CommonCell {
        let cell = tableView.dequeueReusableCellWithIdentifier(CellIdentifier, forIndexPath: indexPath) as! CommonCell
        
        cell.setContent(NSLocalizedString("NotificationsSound", comment: "Sound"))
        cell.style = .Switch
        cell.selectionStyle = UITableViewCellSelectionStyle.None
        cell.bottomSeparatorVisible = true
        // cell.topSeparatorVisible = true
        cell.bottomSeparatorLeftInset = 0
        cell.topSeparatorLeftInset = 0
        
        cell.setSwitcherOn(Actor.isInAppNotificationSoundEnabled())
        cell.setSwitcherEnabled(Actor.isInAppNotificationsEnabled())
        
        cell.switchBlock = { (nValue: Bool) in
            Actor.changeInAppNotificationSoundEnabledWithValue(nValue)
        }
        
        return cell
    }

    
    private func inAppVibrateCell(indexPath: NSIndexPath) -> CommonCell {
        let cell = tableView.dequeueReusableCellWithIdentifier(CellIdentifier, forIndexPath: indexPath) as! CommonCell
        
        cell.setContent(NSLocalizedString("NotificationsVibration", comment: "Vibration"))
        cell.style = .Switch
        cell.selectionStyle = UITableViewCellSelectionStyle.None
        cell.bottomSeparatorVisible = true
        //cell.topSeparatorVisible = true
        cell.bottomSeparatorLeftInset = 0
        cell.topSeparatorLeftInset = 0
        
        cell.setSwitcherOn(Actor.isInAppNotificationVibrationEnabled())
        cell.setSwitcherEnabled(Actor.isInAppNotificationsEnabled())
        
        cell.switchBlock = { (nValue: Bool) in
            Actor.changeInAppNotificationVibrationEnabledWithValue(nValue)
        }
        
        return cell
    }

    private func notificationsPreviewCell(indexPath: NSIndexPath) -> CommonCell {
        let cell = tableView.dequeueReusableCellWithIdentifier(CellIdentifier, forIndexPath: indexPath) as! CommonCell
        
        cell.setContent(NSLocalizedString("NotificationsPreview", comment: "Message Preview"))
        cell.style = .Switch
        cell.selectionStyle = UITableViewCellSelectionStyle.None
        cell.bottomSeparatorVisible = true
        cell.topSeparatorVisible = true
        cell.bottomSeparatorLeftInset = 0
        cell.topSeparatorLeftInset = 0
        
        cell.setSwitcherOn(Actor.isShowNotificationsText())
        cell.switchBlock = { (nValue: Bool) in
            Actor.changeShowNotificationTextEnabledWithValue(nValue)
        }
        
        return cell
    }
    
    override func tableView(tableView: UITableView, cellForRowAtIndexPath indexPath: NSIndexPath) -> UITableViewCell {
        if indexPath.section == 0 {
            return notificationsTonesCell(indexPath)
        } else if (indexPath.section == 1) {
            if (indexPath.row == 0) {
                return notificationsEnableCell(indexPath)
            } else if (indexPath.row == 1) {
                return notificationsAlertCell(indexPath)
            }
        } else if (indexPath.section == 2) {
            if (indexPath.row == 0) {
                return groupEnabledCell(indexPath)
            } else {
                return groupEnabledMentionsCell(indexPath)
            }
        } else if (indexPath.section == 3) {
            return notificationsPreviewCell(indexPath)
        }
        
        return UITableViewCell()
    }
    
    func tableView(tableView: UITableView, willDisplayHeaderView view: UIView, forSection section: Int) {
        let header: UITableViewHeaderFooterView = view as! UITableViewHeaderFooterView
        header.textLabel!.textColor = MainAppTheme.list.sectionColor
    }
    
    func tableView(tableView: UITableView, willDisplayFooterView view: UIView, forSection section: Int) {
        let header: UITableViewHeaderFooterView = view as! UITableViewHeaderFooterView
        header.textLabel!.textColor = MainAppTheme.list.sectionHintColor
    }
    

    // MARK: -
    // MARK: UITableView Delegate

}
