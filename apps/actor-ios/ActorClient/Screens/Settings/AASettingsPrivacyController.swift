//
//  Copyright (c) 2015 Actor LLC. <https://actor.im>
//

import UIKit

class AASettingsPrivacyController: AATableViewController {
    
    // MARK: -
    // MARK: Private vars
    
    private let CellIdentifier = "CellIdentifier"
    
    private var user: AMUserVM?
    private var authSessions: [ImActorModelApiAuthSession]?
    
    // MARK: -
    // MARK: Constructors
    
    init(user: AMUserVM?) {
        self.user = user
        super.init(style: UITableViewStyle.Grouped)
    }
    
    required init(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    // MARK: -
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        navigationItem.title = NSLocalizedString("PrivacyTitle", comment: "Controller title")
        
        tableView.registerClass(AATableViewCell.self, forCellReuseIdentifier: CellIdentifier)
        tableView.backgroundColor = MainAppTheme.list.backyardColor
        tableView.separatorStyle = UITableViewCellSeparatorStyle.None
        
//        MBProgressHUD.showHUDAddedTo(UIApplication.sharedApplication().keyWindow, animated: true)
//        let messenger = CocoaMessenger.messenger().loadSessions()
//        messenger.startWithAMCommandCallback(CocoaCallback(result: { (val: Any?) -> () in
//            
//            // TODO: Which class returned? ImActorModelApiAuthSession?
//            
//            self.tableView.reloadData()
//            MBProgressHUD.hideAllHUDsForView(UIApplication.sharedApplication().keyWindow, animated: true)
//            }, error: { (exception) -> () in
//                println("\(exception)")
//                MBProgressHUD.hideAllHUDsForView(UIApplication.sharedApplication().keyWindow, animated: true)
//        }))
    }
    
    // MARK: -
    // MARK: Getters
    
    private func terminateSessionsCell(indexPath: NSIndexPath) -> AATableViewCell {
        var cell = tableView.dequeueReusableCellWithIdentifier(CellIdentifier, forIndexPath: indexPath) as! AATableViewCell
        
        cell.setContent(NSLocalizedString("PrivacyTerminate", comment: "Terminate action"))
        cell.style = AATableViewCellStyle.Normal
        
        return cell
    }

    // MARK: -
    // MARK: UITableView Data Source
    
    override func numberOfSectionsInTableView(tableView: UITableView) -> Int {
        if authSessions != nil {
            if count(authSessions!) > 0 {
                return 2
            }
        }
        return 1
    }
    
    override func tableView(tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        if section == 1 {
            return count(authSessions!)
        }
        return 1
    }
    
    override func tableView(tableView: UITableView, cellForRowAtIndexPath indexPath: NSIndexPath) -> UITableViewCell {
        if indexPath.section == 0 && indexPath.row == 0 {
            return terminateSessionsCell(indexPath)
        }
        return UITableViewCell()
    }
    
    func tableView(tableView: UITableView, titleForFooterInSection section: Int) -> String? {
        if section > 0 { return nil }
        return NSLocalizedString("PrivacyTerminateHint", comment: "Terminate hint")
    }
    
    // MARK: -
    // MARK: UITableView Delegate
    
    func tableView(tableView: UITableView, didSelectRowAtIndexPath indexPath: NSIndexPath) {
        tableView.deselectRowAtIndexPath(indexPath, animated: true)
        
        if indexPath.section == 0 {
            
            execute(MSG.terminateAllSessions())
        }
    }

}
