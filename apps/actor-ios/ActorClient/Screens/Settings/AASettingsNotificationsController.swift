//
//  Copyright (c) 2015 Actor LLC. <https://actor.im>
//

import UIKit

class AASettingsNotificationsController: AATableViewController {
    
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
        
        navigationItem.title = "Notifications and Sounds" // TODO: Localize
        
        tableView.registerClass(AATableViewCell.self, forCellReuseIdentifier: CellIdentifier)
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
            return 2
        } else if (section == 2) {
            return 2
        } else if (section == 3) {
            return 1
        }
        
        return 1
    }
    
    func tableView(tableView: UITableView, titleForHeaderInSection section: Int) -> String? {
        if (section == 0) {
            return "Effects"
        } else if (section == 1) {
            return "Mobile Notifications"
        } else if (section == 2) {
            return "In-App Notifications"
        } else if (section == 3) {
            return "Privacy"
        }
        
        return nil
    }
    
    func tableView(tableView: UITableView, titleForFooterInSection section: Int) -> String? {
        if (section == 1) {
            return "You can disable notifications for specific users or groups on their info pages."
        } else if (section == 3) {
            return "Remove message text from notifications."
        }
        
        return nil
    }
    
    private func notificationsTonesCell(indexPath: NSIndexPath) -> AATableViewCell {
        var cell = tableView.dequeueReusableCellWithIdentifier(CellIdentifier, forIndexPath: indexPath) as! AATableViewCell
        
        cell.setContent("Sound Effects")
        cell.style = AATableViewCellStyle.Switch
        cell.selectionStyle = UITableViewCellSelectionStyle.None
        cell.showBottomSeparator()
        cell.showTopSeparator()
        
        cell.setSwitcherOn(MSG.isConversationTonesEnabled())
        cell.switchBlock = { (nValue: Bool) in
            MSG.changeConversationTonesEnabledWithBoolean(nValue)
        }
        
        return cell
    }
    
    private func notificationsEnableCell(indexPath: NSIndexPath) -> AATableViewCell {
        var cell = tableView.dequeueReusableCellWithIdentifier(CellIdentifier, forIndexPath: indexPath) as! AATableViewCell
        
        cell.setContent("Enable")
        cell.style = AATableViewCellStyle.Switch
        cell.selectionStyle = UITableViewCellSelectionStyle.None
        cell.showBottomSeparator()
        cell.showTopSeparator()
        
        cell.setSwitcherOn(MSG.isNotificationSoundEnabled())
        cell.switchBlock = { (nValue: Bool) in
            MSG.changeNotificationSoundEnabledWithBoolean(nValue)
            var soundCell = self.tableView.cellForRowAtIndexPath(NSIndexPath(forRow: 1, inSection: 1)) as! AATableViewCell
            if (nValue) {
                soundCell.setSwitcherEnabled(true)
            } else {
                soundCell.setSwitcherEnabled(false)
            }
        }
        
        return cell
    }
    
    private func notificationsAlertCell(indexPath: NSIndexPath) -> AATableViewCell {
        var cell = tableView.dequeueReusableCellWithIdentifier(CellIdentifier, forIndexPath: indexPath) as! AATableViewCell
        
        cell.setContent("Sound")
        cell.style = AATableViewCellStyle.Switch
        cell.selectionStyle = UITableViewCellSelectionStyle.None
        cell.showBottomSeparator()
        
        cell.setSwitcherOn(MSG.isNotificationSoundEnabled())
        cell.setSwitcherEnabled(MSG.isNotificationsEnabled())
        cell.switchBlock = { (nValue: Bool) in
            MSG.changeNotificationSoundEnabledWithBoolean(nValue)
        }
        
        return cell
    }

    private func inAppAlertCell(indexPath: NSIndexPath) -> AATableViewCell {
        var cell = tableView.dequeueReusableCellWithIdentifier(CellIdentifier, forIndexPath: indexPath) as! AATableViewCell
        
        cell.setContent("Enable")
        cell.style = AATableViewCellStyle.Switch
        cell.selectionStyle = UITableViewCellSelectionStyle.None
        cell.showTopSeparator()
        cell.showBottomSeparator()
        
        cell.setSwitcherOn(MSG.isInAppNotificationsEnabled())
        cell.switchBlock = { (nValue: Bool) in
            MSG.changeInAppNotificationsEnabledWithBoolean(nValue)
            var soundCell = self.tableView.cellForRowAtIndexPath(NSIndexPath(forRow: 1, inSection: 2)) as! AATableViewCell
            if (nValue) {
                soundCell.setSwitcherEnabled(true)
            } else {
                soundCell.setSwitcherEnabled(false)
            }
        }
        
        return cell
    }
    
    private func inAppSoundCell(indexPath: NSIndexPath) -> AATableViewCell {
        var cell = tableView.dequeueReusableCellWithIdentifier(CellIdentifier, forIndexPath: indexPath) as! AATableViewCell
        
        cell.setContent("Sound")
        cell.style = AATableViewCellStyle.Switch
        cell.selectionStyle = UITableViewCellSelectionStyle.None
        cell.showBottomSeparator()
        
        cell.setSwitcherOn(MSG.isNotificationSoundEnabled())
        cell.setSwitcherEnabled(MSG.isInAppNotificationsEnabled())
        
        cell.switchBlock = { (nValue: Bool) in
            MSG.changeNotificationSoundEnabledWithBoolean(nValue)
        }
        
        return cell
    }
    
    private func notificationsPreviewCell(indexPath: NSIndexPath) -> AATableViewCell {
        var cell = tableView.dequeueReusableCellWithIdentifier(CellIdentifier, forIndexPath: indexPath) as! AATableViewCell
        
        cell.setContent("Preview")
        cell.style = AATableViewCellStyle.Switch
        cell.selectionStyle = UITableViewCellSelectionStyle.None
        cell.showBottomSeparator()
        cell.hideTopSeparator()
        
        cell.setSwitcherOn(MSG.isShowNotificationsText())
        cell.switchBlock = { (nValue: Bool) in
            MSG.changeShowNotificationTextEnabledWithBoolean(nValue)
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
            } else {
                return inAppSoundCell(indexPath)
            }
        } else if (indexPath.section == 3) {
            return notificationsPreviewCell(indexPath)
        }
         return UITableViewCell()
    }

    // MARK: -
    // MARK: UITableView Delegate

}
