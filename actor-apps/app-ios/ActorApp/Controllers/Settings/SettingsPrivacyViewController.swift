//
//  Copyright (c) 2014-2015 Actor LLC. <https://actor.im>
//

import UIKit

class SettingsPrivacyViewController: AATableViewController {
    
    // MARK: -
    // MARK: Private vars
    
    private let CellIdentifier = "CellIdentifier"
    
    private var authSessions: [ARApiAuthSession]?
    
    // MARK: -
    // MARK: Constructors
    
    init() {
        super.init(style: UITableViewStyle.Grouped)
    }
    
    required init(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    // MARK: -
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        navigationItem.title = NSLocalizedString("PrivacyTitle", comment: "Controller title")
        
        tableView.registerClass(CommonCell.self, forCellReuseIdentifier: CellIdentifier)
        tableView.backgroundColor = MainAppTheme.list.backyardColor
        tableView.separatorStyle = UITableViewCellSeparatorStyle.None
        
        execute(Actor.loadSessionsCommand(), successBlock: { (val) -> Void in
            let list = val as! JavaUtilList
            self.authSessions = []
            for i in 0..<list.size() {
                self.authSessions!.append(list.getWithInt(jint(i)) as! ARApiAuthSession)
            }
            self.tableView.reloadData()
        }, failureBlock: nil)
    }
    
    // MARK: -
    // MARK: Getters
    
    private func terminateSessionsCell(indexPath: NSIndexPath) -> CommonCell {
        let cell = tableView.dequeueReusableCellWithIdentifier(CellIdentifier, forIndexPath: indexPath) as! CommonCell
        
        cell.setContent(NSLocalizedString("PrivacyTerminate", comment: "Terminate action"))
        cell.style = .Normal
//        cell.showTopSeparator()
//        cell.showBottomSeparator()
        
        return cell
    }
    
    private func sessionsCell(indexPath: NSIndexPath) -> CommonCell {
        let cell = tableView.dequeueReusableCellWithIdentifier(CellIdentifier, forIndexPath: indexPath) as! CommonCell
        let session = authSessions![indexPath.row]
        cell.setContent(session.getDeviceTitle())
        cell.style = .Normal
//        if (indexPath.row == 0) {
//            cell.showTopSeparator()
//        } else {
//            cell.hideTopSeparator()
//        }
//        cell.showBottomSeparator()
        return cell
    }

    // MARK: -
    // MARK: UITableView Data Source
    
    override func numberOfSectionsInTableView(tableView: UITableView) -> Int {
        if authSessions != nil {
            if authSessions!.count > 0 {
                return 2
            }
        }
        return 1
    }
    
    override func tableView(tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        if section == 1 {
            return authSessions!.count
        }
        return 1
    }
    
    override func tableView(tableView: UITableView, cellForRowAtIndexPath indexPath: NSIndexPath) -> UITableViewCell {
        if indexPath.section == 0 && indexPath.row == 0 {
            return terminateSessionsCell(indexPath)
        } else if (indexPath.section == 1) {
            return sessionsCell(indexPath)
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
            execute(Actor.terminateAllSessionsCommand())
        } else if (indexPath.section == 1) {
            execute(Actor.terminateSessionCommandWithId(authSessions![indexPath.row].getId()), successBlock: { (val) -> Void in
                self.execute(Actor.loadSessionsCommand(), successBlock: { (val) -> Void in
                    let list = val as! JavaUtilList
                    self.authSessions = []
                    for i in 0..<list.size() {
                        self.authSessions!.append(list.getWithInt(jint(i)) as! ARApiAuthSession)
                    }
                    self.tableView.reloadData()
                    }, failureBlock: nil)
            }, failureBlock: nil)
        }
    }
    
    func tableView(tableView: UITableView, willDisplayHeaderView view: UIView, forSection section: Int) {
        let header: UITableViewHeaderFooterView = view as! UITableViewHeaderFooterView
        header.textLabel!.textColor = MainAppTheme.list.sectionColor
    }
    
    func tableView(tableView: UITableView, willDisplayFooterView view: UIView, forSection section: Int) {
        let header: UITableViewHeaderFooterView = view as! UITableViewHeaderFooterView
        header.textLabel!.textColor = MainAppTheme.list.hintColor
    }    
}
