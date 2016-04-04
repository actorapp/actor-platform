//
//  Copyright (c) 2014-2016 Actor LLC. <https://actor.im>
//

import UIKit

public class AASettingsSessionsController: AAContentTableController {

    private var sessionsCell: AAManagedArrayRows<ARApiAuthSession, AACommonCell>?
    
    public init() {
        super.init(style: AAContentTableStyle.SettingsGrouped)
        
        navigationItem.title = AALocalized("SettingsAllSessions")
        
        content = ACAllEvents_Settings.PRIVACY()
    }
    
    public required init(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    public override func tableDidLoad() {
        
        section { (s) -> () in
            
            s.footerText = AALocalized("PrivacyTerminateHint")
            
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
            self.sessionsCell = s.arrays() { (r: AAManagedArrayRows<ARApiAuthSession, AACommonCell>) -> () in
                r.bindData = { (c: AACommonCell, d: ARApiAuthSession) -> () in
                    if d.getAuthHolder().ordinal() != ARApiAuthHolder.THISDEVICE().ordinal() {
                        c.style = .Normal
                        c.setContent(d.getDeviceTitle())
                    } else {
                        c.style = .Hint
                        c.setContent("(Current) \(d.getDeviceTitle())")
                    }
                }
                
                r.selectAction = { (d) -> Bool in
                    if d.getAuthHolder().ordinal() != ARApiAuthHolder.THISDEVICE().ordinal() {
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
