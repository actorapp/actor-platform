//
//  Copyright (c) 2014-2015 Actor LLC. <https://actor.im>
//

import UIKit

class SettingsPrivacyViewController: AATableViewController {
    
    private var authSessions = [ARApiAuthSession]()
    private var data: ACManagedTable!
    
    init() {
        super.init(style: UITableViewStyle.Grouped)
    }
    
    required init(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        navigationItem.title = NSLocalizedString("PrivacyTitle", comment: "Controller title")
        
        tableView.backgroundColor = MainAppTheme.list.backyardColor
        tableView.separatorStyle = UITableViewCellSeparatorStyle.None
        
        data = ACManagedTable(tableView: tableView)
        
        let header = data.addSection(true)
            .setFooterText("PrivacyTerminateHint")
        
        header.addDangerCell("PrivacyTerminate") { () -> Bool in
            self.confirmDangerSheetUser("PrivacyTerminateAlert", tapYes: { () -> () in
                
                // Terminating all sessions and reload list
                self.executeSafe(Actor.terminateAllSessionsCommand(), successBlock: { (val) -> Void in
                    self.loadSessions()
                })
            }, tapNo: nil)

            return true
        }
        
        data.addSection(true)
            .addCustomCells(44, countClosure: { () -> Int in
                    return self.authSessions.count
                }, closure: { (tableView, index, indexPath) -> UITableViewCell in
                    let cell = tableView.dequeueReusableCellWithIdentifier(ACManagedTable.ReuseCommonCell, forIndexPath: indexPath) as! CommonCell
                    let session = self.authSessions[indexPath.row]
                    if session.getAuthHolder().ordinal() != jint(ARApiAuthHolder.THISDEVICE.rawValue) {
                        cell.style = .Normal
                        cell.setContent(session.getDeviceTitle())
                    } else {
                        cell.style = .Hint
                        cell.setContent("(Current) \(session.getDeviceTitle())")
                    }
                    return cell
                })
            .setAction { (index) -> Bool in
                let session = self.authSessions[index]
                if session.getAuthHolder().ordinal() != jint(ARApiAuthHolder.THISDEVICE.rawValue) {
                    self.confirmDangerSheetUser("PrivacyTerminateAlertSingle", tapYes: { () -> () in
                        // Terminating session and reload list
                        self.executeSafe(Actor.terminateSessionCommandWithId(session.getId()), successBlock: { (val) -> Void in
                            self.loadSessions()
                        })
                    }, tapNo: nil)
                }
                return true
            }
        
        // Starting to load sessions
        loadSessions()
    }
    
    private func loadSessions() {
        execute(Actor.loadSessionsCommand(), successBlock: { (val) -> Void in
            let list = val as! JavaUtilList
            self.authSessions = []
            for i in 0..<list.size() {
                self.authSessions.append(list.getWithInt(jint(i)) as! ARApiAuthSession)
            }
            self.tableView.reloadData()
        }, failureBlock: nil)
    }
}
