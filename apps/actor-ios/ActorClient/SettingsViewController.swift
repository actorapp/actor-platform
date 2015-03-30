//
//  SettingsViewController.swift
//  ActorClient
//
//  Created by Stepan Korshakov on 12.03.15.
//  Copyright (c) 2015 Actor LLC. All rights reserved.
//

import UIKit

class SettingsViewController: UIViewController, UITableViewDelegate, UITableViewDataSource {

    let binder: Binder = Binder();
    
    var user: AMUserVM? = nil
    
    @IBOutlet weak var tableView: UITableView!
    
    required init(coder aDecoder: NSCoder) {
        super.init(coder: aDecoder);
        initCommon();
    }
    
    init() {
        super.init(nibName: "SettingsViewController", bundle: nil)
        initCommon();
    }
    
    func initCommon(){
        var icon = UIImage(named: "ic_settings_blue_24")!;
        tabBarItem = UITabBarItem(title: nil,
            image: icon.tintImage(Resources.BarTintUnselectedColor)
                .imageWithRenderingMode(UIImageRenderingMode.AlwaysOriginal),
            selectedImage: icon);
        tabBarItem.imageInsets=UIEdgeInsetsMake(6, 0, -6, 0);
    }
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        tableView.backgroundColor = Resources.BackyardColor
        tableView.registerNib(UINib(nibName: "AvatarCell", bundle: nil), forCellReuseIdentifier: "cell_avatar")
        tableView.registerNib(UINib(nibName: "ContactRecordCell", bundle: nil), forCellReuseIdentifier: "cell_contact")
        tableView.registerNib(UINib(nibName: "MenuItemCell", bundle: nil), forCellReuseIdentifier: "cell_menu")
        tableView.delegate = self
        tableView.dataSource = self
    }
    
    override func viewWillAppear(animated: Bool) {
        super.viewWillAppear(animated)
        
        user = MSG.getUsers().getWithLong(jlong(MSG.myUid())) as! AMUserVM;
        
        binder.bind(user!.getAvatar(), closure: { (avatar: AMAvatar?) -> () in
            self.tableView.reloadData()
        })

        binder.bind(user!.getName(), closure: { (name: NSString?) -> () in
            self.tableView.reloadData()
        })
    }
    
    func numberOfSectionsInTableView(tableView: UITableView) -> Int {
        return 3
    }
    
    func tableView(tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        if (section == 0) {
            return 1
        } else if (section == 1) {
            var phones = user!.getPhones().get() as! JavaUtilArrayList;
            return Int(phones.size())
        } else if (section == 2) {
            return 4
        }
        
        fatalError("??")
    }
    
    func tableView(tableView: UITableView, heightForRowAtIndexPath indexPath: NSIndexPath) -> CGFloat {

        if (indexPath.section == 0) {
            // Avatar
            return 160
        } else if (indexPath.section == 1) {
            // Contacts
            return 66
        } else if (indexPath.section == 2) {
            return 44
        }
        
         fatalError("??")
    }
    
    func tableView(tableView: UITableView, cellForRowAtIndexPath indexPath: NSIndexPath) -> UITableViewCell {
        if (indexPath.section == 0) {
            var res = tableView.dequeueReusableCellWithIdentifier("cell_avatar") as! AvatarCell;
            res.bind(user!)
            return res
        } else if (indexPath.section == 1) {
            var res = tableView.dequeueReusableCellWithIdentifier("cell_contact") as! ContactRecordCell;
            var phones = user!.getPhones().get() as! JavaUtilArrayList;
            var phone = phones.getWithInt(jint(indexPath.row)) as! AMUserPhone;
            res.bind(phone)
            return res
        } else if (indexPath.section == 2) {
            var res = tableView.dequeueReusableCellWithIdentifier("cell_menu") as! MenuItemCell;
            res.setData("ic_profile_help",title: "Help")
            return res
        }
        
        fatalError("??")
    }
    
    func tableView(tableView: UITableView, heightForFooterInSection section: Int) -> CGFloat {
        return 10
    }
    
    func tableView(tableView: UITableView, heightForHeaderInSection section: Int) -> CGFloat {
        if (section == 0){
            return 0
        } else {
            return 48
        }
    }
    
    func tableView(tableView: UITableView, viewForHeaderInSection section: Int) -> UIView? {
        if (section == 0) {
            return nil
        } else {
            var res = UIView(frame: CGRectMake(0, 0, 320, 36))
            
            var bg = UIView(frame: CGRectMake(0, 12, 320, 36))
            bg.backgroundColor = UIColor.whiteColor()
            res.addSubview(bg)
            
            var shadow = UIImageView(frame: CGRectMake(0, 8, 320, 4))
            shadow.contentMode = UIViewContentMode.ScaleToFill
            shadow.image =  UIImage(named: "CardTop2")
            res.addSubview(shadow)

            var title = UILabel()
            title.text = section == 1 ? "Contacts" : "Help";
            title.textColor = Resources.TintColor
            title.font = UIFont(name: "HelveticaNeue-Medium", size: 14)
            title.frame = CGRectMake(60, 12, 320 - 60, 36)
            res.addSubview(title)
            
            return res
        }
    }
    
    func tableView(tableView: UITableView, viewForFooterInSection section: Int) -> UIView? {
        var res = UIView(frame: CGRectMake(0, 0, 320, 10))
        res.backgroundColor = Resources.BackyardColor
        var shadow = UIImageView(image: UIImage(named: "CardBottom2"))
        shadow.contentMode = UIViewContentMode.ScaleToFill
        shadow.frame = CGRectMake(0, 0, 320, 4)
        res.addSubview(shadow)
        return res
    }
}