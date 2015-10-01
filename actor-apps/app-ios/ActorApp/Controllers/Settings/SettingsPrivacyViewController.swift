//
//  Copyright (c) 2014-2015 Actor LLC. <https://actor.im>
//

import UIKit

class SettingsPrivacyViewController: ACContentTableController {
    
    private var sessionsCell: ACManagedArrayRows<ARApiAuthSession, CommonCell>?
    
    init() {
        super.init(style: ACContentTableStyle.SettingsGrouped)
        
        navigationItem.title = localized("SecurityTitle")
        
        content = ACAllEvents_Settings.PRIVACY()
    }
    
    required init(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    override func tableDidLoad() {
        
        section { (s) -> () in
            
            s.footerText = localized("PrivacyTerminateHint")
            
            s.danger("PrivacyTerminate") { (r) -> () in
                r.selectAction = { () -> Bool in
                    self.confirmDangerSheetUser("PrivacyTerminateAlert", tapYes: { [unowned self] () -> () in
                        // Terminating all sessions and reload list
                        self.executeSafe(Actor.terminateAllSessionsCommand(), successBlock: { (val) -> Void in
                            self.loadSessions()
                        })
                        }, tapNo: nil)
                    return true
                }
            }
        }
        
        section { (s) -> () in
            self.sessionsCell = s.arrays() { (r: ACManagedArrayRows<ARApiAuthSession, CommonCell>) -> () in
                r.bindData = { (c: CommonCell, d: ARApiAuthSession) -> () in
                    if d.getAuthHolder().ordinal() != jint(ARApiAuthHolder.THISDEVICE.rawValue) {
                        c.style = .Normal
                        c.setContent(d.getDeviceTitle())
                    } else {
                        c.style = .Hint
                        c.setContent("(Current) \(d.getDeviceTitle())")
                    }
                }
                
                r.selectAction = { (d) -> Bool in
                    if d.getAuthHolder().ordinal() != jint(ARApiAuthHolder.THISDEVICE.rawValue) {
                        self.confirmDangerSheetUser("PrivacyTerminateAlertSingle", tapYes: { [unowned self] () -> () in
                            // Terminating session and reload list
                            self.executeSafe(Actor.terminateSessionCommandWithId(d.getId()), successBlock: { [unowned self] (val) -> Void in
                                self.loadSessions()
                                })
                        }, tapNo: nil)
                    }
                    return true
                }
            }
        }
        
        // Request sessions load
        
        loadSessions()
    }
    
    private func loadSessions() {
        execute(Actor.loadSessionsCommand(), successBlock: { [unowned self] (val) -> Void in
            self.sessionsCell!.data = (val as! JavaUtilList).toArray().toSwiftArray()
            self.managedTable.tableView.reloadData()
        }, failureBlock: nil)
    }
}
