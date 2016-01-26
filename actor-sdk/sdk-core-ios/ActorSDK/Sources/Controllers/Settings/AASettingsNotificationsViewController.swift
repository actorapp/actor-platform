//
//  Copyright (c) 2014-2016 Actor LLC. <https://actor.im>
//

import UIKit

public class AASettingsNotificationsViewController: AATableViewController {
    
    // MARK: -
    // MARK: Constructors
    
    private let CellIdentifier = "CellIdentifier"
    
    public init() {
        super.init(style: UITableViewStyle.Grouped)
        
        title = AALocalized("NotificationsTitle")
        
        content = ACAllEvents_Settings.NOTIFICATIONS()
    }
    
    public required init(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    // MARK: -
    
    public override func viewDidLoad() {
        super.viewDidLoad()
        
        tableView.registerClass(AACommonCell.self, forCellReuseIdentifier: CellIdentifier)
        tableView.backgroundColor = appStyle.vcBackyardColor
        tableView.separatorStyle = UITableViewCellSeparatorStyle.None
        
        view.backgroundColor = tableView.backgroundColor
    }
    
    // MARK: - 
    // MARK: UITableView Data Source
    
    public override func numberOfSectionsInTableView(tableView: UITableView) -> Int {
        return 4
    }
    
    public override func tableView(tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
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
    
    public func tableView(tableView: UITableView, titleForHeaderInSection section: Int) -> String? {
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
    
    public func tableView(tableView: UITableView, titleForFooterInSection section: Int) -> String? {
        if (section == 1) {
            return AALocalized("NotificationsNotificationHint")
        } else if (section == 2) {
            return AALocalized("NotificationsOnlyMentionsHint")
        } else if (section == 3) {
            return AALocalized("NotificationsPreviewHint")
        }
        
        return nil
    }
    
    private func notificationsTonesCell(indexPath: NSIndexPath) -> AACommonCell {
        let cell = tableView.dequeueReusableCellWithIdentifier(CellIdentifier, forIndexPath: indexPath) as! AACommonCell
        
        cell.setContent(AALocalized("NotificationsSoundEffects"))
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
    
    private func notificationsEnableCell(indexPath: NSIndexPath) -> AACommonCell {
        let cell = tableView.dequeueReusableCellWithIdentifier(CellIdentifier, forIndexPath: indexPath) as! AACommonCell
        
        cell.setContent(AALocalized("NotificationsEnable"))
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
    
    private func notificationsAlertCell(indexPath: NSIndexPath) -> AACommonCell {
        let cell = tableView.dequeueReusableCellWithIdentifier(CellIdentifier, forIndexPath: indexPath) as! AACommonCell
        
        cell.setContent(AALocalized("NotificationsSound"))
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
    
    
    private func groupEnabledCell(indexPath: NSIndexPath) -> AACommonCell {
        let cell = tableView.dequeueReusableCellWithIdentifier(CellIdentifier, forIndexPath: indexPath) as! AACommonCell
        
        cell.setContent(AALocalized("NotificationsEnable"))
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
    
    private func groupEnabledMentionsCell(indexPath: NSIndexPath) -> AACommonCell {
        let cell = tableView.dequeueReusableCellWithIdentifier(CellIdentifier, forIndexPath: indexPath) as! AACommonCell
        
        cell.setContent(AALocalized("NotificationsOnlyMentions"))
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

    private func inAppAlertCell(indexPath: NSIndexPath) -> AACommonCell {
        let cell = tableView.dequeueReusableCellWithIdentifier(CellIdentifier, forIndexPath: indexPath) as! AACommonCell
        
        cell.setContent(AALocalized("NotificationsEnable"))
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
    
    private func inAppSoundCell(indexPath: NSIndexPath) -> AACommonCell {
        let cell = tableView.dequeueReusableCellWithIdentifier(CellIdentifier, forIndexPath: indexPath) as! AACommonCell
        
        cell.setContent(AALocalized("NotificationsSound"))
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

    
    private func inAppVibrateCell(indexPath: NSIndexPath) -> AACommonCell {
        let cell = tableView.dequeueReusableCellWithIdentifier(CellIdentifier, forIndexPath: indexPath) as! AACommonCell
        
        cell.setContent(AALocalized("NotificationsVibration"))
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

    private func notificationsPreviewCell(indexPath: NSIndexPath) -> AACommonCell {
        let cell = tableView.dequeueReusableCellWithIdentifier(CellIdentifier, forIndexPath: indexPath) as! AACommonCell
        
        cell.setContent(AALocalized("NotificationsPreview"))
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
    
    public override func tableView(tableView: UITableView, cellForRowAtIndexPath indexPath: NSIndexPath) -> UITableViewCell {
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
    
    public func tableView(tableView: UITableView, willDisplayHeaderView view: UIView, forSection section: Int) {
        let header: UITableViewHeaderFooterView = view as! UITableViewHeaderFooterView
        header.textLabel!.textColor = ActorSDK.sharedActor().style.cellFooterColor
    }
    
    public func tableView(tableView: UITableView, willDisplayFooterView view: UIView, forSection section: Int) {
        let header: UITableViewHeaderFooterView = view as! UITableViewHeaderFooterView
        header.textLabel!.textColor = ActorSDK.sharedActor().style.cellFooterColor
    }
}
