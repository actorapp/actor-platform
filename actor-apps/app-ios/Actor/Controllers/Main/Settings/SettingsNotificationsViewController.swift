//
//  Copyright (c) 2015 Actor LLC. <https://actor.im>
//

import UIKit

class SettingsNotificationsViewController: AATableViewController {
    
    // MARK: -
    // MARK: Constructors
    
    private let CellIdentifier = "CellIdentifier"
    
    init() {
        super.init(style:
            UITableViewStyle.Grouped)
    }
    
    required init(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    // MARK: -
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        navigationItem.title = NSLocalizedString("NotificationsTitle", comment: "Notifcations and Sounds")
        
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
            return MSG.isNotificationsEnabled() ? 2 : 1
        } else if (section == 2) {
            return MSG.isInAppNotificationsEnabled() ? 3 : 1
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
            return NSLocalizedString("NotificationsInAppTitle", comment: "InApp Notifications")
        } else if (section == 3) {
            return NSLocalizedString("NotificationsPrivacyTitle", comment: "Privacy")
        }
        
        return nil
    }
    
    func tableView(tableView: UITableView, titleForFooterInSection section: Int) -> String? {
        if (section == 1) {
            return NSLocalizedString("NotificationsNotificationHint", comment: "Disable hint")
        } else if (section == 3) {
            return NSLocalizedString("NotificationsPreviewHint", comment: "Preview hint")
        }
        
        return nil
    }
    
    private func notificationsTonesCell(indexPath: NSIndexPath) -> CommonCell {
        var cell = tableView.dequeueReusableCellWithIdentifier(CellIdentifier, forIndexPath: indexPath) as! CommonCell
        
        cell.setContent(NSLocalizedString("NotificationsSoundEffects", comment: "Sound Effects"))
        cell.style = .Switch
        cell.selectionStyle = UITableViewCellSelectionStyle.None
        cell.showBottomSeparator()
        cell.showTopSeparator()
        
        cell.setSwitcherOn(MSG.isConversationTonesEnabled())
        cell.switchBlock = { (nValue: Bool) in
            MSG.changeConversationTonesEnabledWithValue(nValue)
        }
        
        return cell
    }
    
    private func notificationsEnableCell(indexPath: NSIndexPath) -> CommonCell {
        var cell = tableView.dequeueReusableCellWithIdentifier(CellIdentifier, forIndexPath: indexPath) as! CommonCell
        
        cell.setContent(NSLocalizedString("NotificationsEnable", comment: "Enable"))
        cell.style = .Switch
        cell.selectionStyle = UITableViewCellSelectionStyle.None
        cell.showBottomSeparator()
        cell.showTopSeparator()
        
        cell.setSwitcherOn(MSG.isNotificationsEnabled())
        cell.switchBlock = { (nValue: Bool) in
            self.tableView.beginUpdates()
            MSG.changeNotificationsEnabledWithValue(nValue)
            var rows = [NSIndexPath(forRow: 1, inSection: 1)]
            if (nValue) {
                self.tableView.insertRowsAtIndexPaths(rows, withRowAnimation: UITableViewRowAnimation.Middle)
            } else {
                self.tableView.deleteRowsAtIndexPaths(rows, withRowAnimation: UITableViewRowAnimation.Middle)
            }
//            var soundCell = self.tableView.cellForRowAtIndexPath(NSIndexPath(forRow: 1, inSection: 1)) as! AATableViewCell
//            if (nValue) {
//                soundCell.setSwitcherEnabled(true)
//            } else {
//                soundCell.setSwitcherEnabled(false)
//            }
            self.tableView.endUpdates()
        }
        
        return cell
    }
    
    private func notificationsAlertCell(indexPath: NSIndexPath) -> CommonCell {
        var cell = tableView.dequeueReusableCellWithIdentifier(CellIdentifier, forIndexPath: indexPath) as! CommonCell
        
        cell.setContent(NSLocalizedString("NotificationsSound", comment: "Sound"))
        cell.style = .Switch
        cell.selectionStyle = UITableViewCellSelectionStyle.None
        cell.showBottomSeparator()
        
        cell.setSwitcherOn(MSG.isNotificationSoundEnabled())
        cell.setSwitcherEnabled(MSG.isNotificationsEnabled())
        cell.switchBlock = { (nValue: Bool) in
            MSG.changeNotificationSoundEnabledWithValue(nValue)
        }
        
        return cell
    }

    private func inAppAlertCell(indexPath: NSIndexPath) -> CommonCell {
        var cell = tableView.dequeueReusableCellWithIdentifier(CellIdentifier, forIndexPath: indexPath) as! CommonCell
        
        cell.setContent(NSLocalizedString("NotificationsEnable", comment: "Enable"))
        cell.style = .Switch
        cell.selectionStyle = UITableViewCellSelectionStyle.None
        cell.showTopSeparator()
        cell.showBottomSeparator()
        
        cell.setSwitcherOn(MSG.isInAppNotificationsEnabled())
        cell.switchBlock = { (nValue: Bool) in
            self.tableView.beginUpdates()
            MSG.changeInAppNotificationsEnabledWithValue(nValue)
            var rows = [NSIndexPath(forRow: 1, inSection: 2), NSIndexPath(forRow: 2, inSection: 2)]
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
        var cell = tableView.dequeueReusableCellWithIdentifier(CellIdentifier, forIndexPath: indexPath) as! CommonCell
        
        cell.setContent(NSLocalizedString("NotificationsSound", comment: "Sound"))
        cell.style = .Switch
        cell.selectionStyle = UITableViewCellSelectionStyle.None
        cell.showBottomSeparator()
        
        cell.setSwitcherOn(MSG.isInAppNotificationSoundEnabled())
        cell.setSwitcherEnabled(MSG.isInAppNotificationsEnabled())
        
        cell.switchBlock = { (nValue: Bool) in
            MSG.changeInAppNotificationSoundEnabledWithValue(nValue)
        }
        
        return cell
    }

    
    private func inAppVibrateCell(indexPath: NSIndexPath) -> CommonCell {
        var cell = tableView.dequeueReusableCellWithIdentifier(CellIdentifier, forIndexPath: indexPath) as! CommonCell
        
        cell.setContent(NSLocalizedString("NotificationsVibration", comment: "Vibration"))
        cell.style = .Switch
        cell.selectionStyle = UITableViewCellSelectionStyle.None
        cell.showBottomSeparator()
        
        cell.setSwitcherOn(MSG.isInAppNotificationVibrationEnabled())
        cell.setSwitcherEnabled(MSG.isInAppNotificationsEnabled())
        
        cell.switchBlock = { (nValue: Bool) in
            MSG.changeInAppNotificationVibrationEnabledWithValue(nValue)
        }
        
        return cell
    }

    private func notificationsPreviewCell(indexPath: NSIndexPath) -> CommonCell {
        var cell = tableView.dequeueReusableCellWithIdentifier(CellIdentifier, forIndexPath: indexPath) as! CommonCell
        
        cell.setContent(NSLocalizedString("NotificationsPreview", comment: "Message Preview"))
        cell.style = .Switch
        cell.selectionStyle = UITableViewCellSelectionStyle.None
        cell.showBottomSeparator()
        cell.showTopSeparator()
        
        cell.setSwitcherOn(MSG.isShowNotificationsText())
        cell.switchBlock = { (nValue: Bool) in
            MSG.changeShowNotificationTextEnabledWithValue(nValue)
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
                return inAppAlertCell(indexPath)
            } else if (indexPath.row == 1) {
                return inAppSoundCell(indexPath)
            } else if (indexPath.row == 2) {
                return inAppVibrateCell(indexPath)
            }
        } else if (indexPath.section == 3) {
            return notificationsPreviewCell(indexPath)
        }
         return UITableViewCell()
    }
    
    func tableView(tableView: UITableView, willDisplayHeaderView view: UIView, forSection section: Int) {
        let header: UITableViewHeaderFooterView = view as! UITableViewHeaderFooterView
        header.textLabel.textColor = MainAppTheme.list.sectionColor
    }
    
    func tableView(tableView: UITableView, willDisplayFooterView view: UIView, forSection section: Int) {
        let header: UITableViewHeaderFooterView = view as! UITableViewHeaderFooterView
        header.textLabel.textColor = MainAppTheme.list.hintColor
    }
    

    // MARK: -
    // MARK: UITableView Delegate

}
