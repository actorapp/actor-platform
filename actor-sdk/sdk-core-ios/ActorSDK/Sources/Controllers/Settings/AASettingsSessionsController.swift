//
//  Copyright (c) 2014-2016 Actor LLC. <https://actor.im>
//

import UIKit

open class AASettingsSessionsController: AAContentTableController {

    fileprivate var sessionsCell: AAManagedArrayRows<ARApiAuthSession, AACommonCell>?
    
    public init() {
        super.init(style: AAContentTableStyle.settingsGrouped)
        
        navigationItem.title = AALocalized("PrivacyAllSessions")
        
        content = ACAllEvents_Settings.privacy()
    }
    
    public required init(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    open override func tableDidLoad() {
        
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
                    if d.getAuthHolder().ordinal() != ARApiAuthHolder.thisdevice().ordinal() {
                        c.style = .normal
                        c.setContent(d.getDeviceTitle())
                    } else {
                        c.style = .hint
                        c.setContent("(Current) \(d.getDeviceTitle())")
                    }
                }
                
                r.selectAction = { (d) -> Bool in
                    if d.getAuthHolder().ordinal() != ARApiAuthHolder.thisdevice().ordinal() {
                        self.confirmDangerSheetUser("PrivacyTerminateAlertSingle", tapYes: { [unowned self] () -> () in
                            // Terminating session and reload list
                            self.executeSafe(Actor.terminateSessionCommand(withId: d.getId()), successBlock: { [unowned self] (val) -> Void in
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
    
    fileprivate func loadSessions() {
        execute(Actor.loadSessionsCommand(), successBlock: { [unowned self] (val) -> Void in
            self.sessionsCell!.data = (val as! JavaUtilList).toArray().toSwiftArray()
            self.managedTable.tableView.reloadData()
            }, failureBlock: nil)
    }

}
