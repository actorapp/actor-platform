//
//  AASettingsController.swift
//  ActorClient
//
//  Created by Danil Gontovnik on 4/6/15.
//  Copyright (c) 2015 Actor LLC. All rights reserved.
//

import UIKit

class AASettingsController: AATableViewController {
    
    // MARK: -
    // MARK: Private vars
    
    private let UserInfoCellIdentifier = "UserInfoCellIdentifier"
    private let CellIdentifier = "CellIdentifier"
    
    private let uid: Int
    private var user: AMUserVM?
    private var binder = Binder()
    
    // MARK: -
    // MARK: Constructors
    
    init() {
        uid = Int(MSG.myUid())
        
        super.init(style: UITableViewStyle.Grouped)
        initCommon()
    }

    required init(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    // MARK: -

    override func viewDidLoad() {
        super.viewDidLoad()
        
        user = MSG.getUsers().getWithLong(jlong(uid)) as? AMUserVM
        
        navigationItem.title = "Settings" // TODO: Localize
        navigationItem.rightBarButtonItem = editButtonItem()
        
        tableView.registerClass(AAUserInfoCell.self, forCellReuseIdentifier: UserInfoCellIdentifier)
        tableView.registerClass(AATableViewCell.self, forCellReuseIdentifier: CellIdentifier)
        
        tableView.reloadData()
        
        tableView.tableFooterView = UIView()
        
        binder.bind(user!.getAvatar(), closure: { (value: AMAvatar?) -> () in
            if let cell = self.tableView.cellForRowAtIndexPath(NSIndexPath(forItem: 0, inSection: 0)) as? AAUserInfoCell {
                cell.userAvatarView.bind(self.user!.getName().get() as! String, id: jint(self.uid), avatar: value)
            }
        })
    }
    
    func initCommon(){
        var icon = UIImage(named: "ic_settings_blue_24")!;
        tabBarItem = UITabBarItem(title: nil,
            image: icon.tintImage(Resources.BarTintUnselectedColor)
                .imageWithRenderingMode(UIImageRenderingMode.AlwaysOriginal),
            selectedImage: icon);
        tabBarItem.imageInsets=UIEdgeInsetsMake(6, 0, -6, 0);
    }
    
    // MARK: -
    // MARK: Getters
    
    private func userInfoCell(indexPath: NSIndexPath) -> AAUserInfoCell {
        var cell: AAUserInfoCell = tableView.dequeueReusableCellWithIdentifier(UserInfoCellIdentifier, forIndexPath: indexPath) as! AAUserInfoCell
        
        if user != nil {
            
            if let username = user!.getName().get() as? String {
                cell.setUsername(username)
                cell.usernameChangedBlock = { (newUsername: String) -> () in
                    self.execute(MSG.editMyNameWithNSString(newUsername))
                }
            }
            
        }
        cell.setLeftInset(15.0)
        
        return cell
    }
    
    private func setProfilePhotoCell(indexPath: NSIndexPath) -> AATableViewCell {
        var cell: AATableViewCell = tableView.dequeueReusableCellWithIdentifier(CellIdentifier, forIndexPath: indexPath) as! AATableViewCell
        
        cell.style = AATableViewCellStyle.Blue
        cell.setTitle("Set Profile Photo") // TODO: Localize
        cell.setLeftInset(15.0)
        
        return cell
    }
    
    private func notificationsCell(indexPath: NSIndexPath) -> AATableViewCell {
        var cell: AATableViewCell = tableView.dequeueReusableCellWithIdentifier(CellIdentifier, forIndexPath: indexPath) as! AATableViewCell
        
        cell.style = AATableViewCellStyle.Navigation
        cell.setTitle("Notifications and Sounds") // TODO: Localize
        cell.setLeftInset(15.0)
        
        return cell
    }
    
    private func privacyCell(indexPath: NSIndexPath) -> AATableViewCell {
        var cell: AATableViewCell = tableView.dequeueReusableCellWithIdentifier(CellIdentifier, forIndexPath: indexPath) as! AATableViewCell
        
        cell.style = AATableViewCellStyle.Navigation
        cell.setTitle("Privacy and Security") // TODO: Localize
        cell.setLeftInset(15.0)
        
        return cell
    }
    
    // MARK: -
    // MARK: UITableView Data Source
    
    override func numberOfSectionsInTableView(tableView: UITableView) -> Int {
        return 2
    }
    
    override func tableView(tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        switch (section) {
        case 0:
            return 2
        case 1:
            return 2
        default:
            return 0
        }
    }
    
    override func tableView(tableView: UITableView, cellForRowAtIndexPath indexPath: NSIndexPath) -> UITableViewCell {
        if indexPath.section == 0 && indexPath.row == 0 {
            return userInfoCell(indexPath)
        } else if indexPath.section == 0 && indexPath.row == 1 {
            return setProfilePhotoCell(indexPath)
        } else if indexPath.section == 1 && indexPath.row == 0 {
            return notificationsCell(indexPath)
        } else if indexPath.section == 1 && indexPath.row == 1 {
            return privacyCell(indexPath)
        }
        return UITableViewCell()
    }
    
    // MARK: -
    // MARK: UITableView Delegate
    
    func tableView(tableView: UITableView, didSelectRowAtIndexPath indexPath: NSIndexPath) {
        tableView.deselectRowAtIndexPath(indexPath, animated: true)
        
        if indexPath.section == 1 && indexPath.row == 0 {
            navigateToNotificationsSettings()
        } else if indexPath.section == 1 && indexPath.row == 1 {
            navigateToPrivacySettings()
        }
    }

    func tableView(tableView: UITableView, heightForRowAtIndexPath indexPath: NSIndexPath) -> CGFloat {
        if indexPath.section == 0 && indexPath.row == 0 {
            return 89
        }
        return 44
    }
    
    func tableView(tableView: UITableView, heightForHeaderInSection section: Int) -> CGFloat {
        if section == 0 {
            return CGFloat.min
        }
        return tableView.sectionHeaderHeight
    }
    
    // MARK: -
    // MARK: Navigation
    
    private func navigateToNotificationsSettings() {
        let notificationsSettingsController = AASettingsNotificationsController()
        notificationsSettingsController.hidesBottomBarWhenPushed = true
        navigationController?.pushViewController(notificationsSettingsController, animated: true)
    }
    
    private func navigateToPrivacySettings() {
        let privacySettingsController = AASettingsPrivacyController()
        privacySettingsController.hidesBottomBarWhenPushed = true
        navigationController?.pushViewController(privacySettingsController, animated: true)
    }
    
}
