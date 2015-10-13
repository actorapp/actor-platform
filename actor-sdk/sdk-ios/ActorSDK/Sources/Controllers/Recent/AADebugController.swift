//
//  Copyright (c) 2014-2015 Actor LLC. <https://actor.im>
//

import UIKit

class AADebugController: AAContentTableController {
    
    var debugData: AAManagedArrayRows<ACDialogDesc, AACommonCell>!
    
    init() {
        super.init(style: .Plain)
    }

    required init(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    override func tableDidLoad() {
        section { (s) -> () in
            self.debugData = s.arrays { (r:AAManagedArrayRows<ACDialogDesc, AACommonCell>) -> () in
                r.bindData = { (cell: AACommonCell, data: ACDialogDesc) -> () in
                    cell.setContent(data.title)
                }
            }
        }
    }
    
    override func tableWillBind(binder: AABinder) {
        binder.bind(ActorSDK.sharedActor().messenger.getDialogGroupsVM().getGroupsValueModel()) { (value: JavaUtilArrayList?) -> () in
            
            if value != nil {
                var items = [ACDialogDesc]()
                
                for i in  0..<value!.size() {
                    for j in (value!.getWithInt(i) as! ACDialogGroup).getDialogs() {
                        items.append(j as! ACDialogDesc)
                    }
                }
                self.debugData.data = items
                self.debugData.reload()
            }
        }
    }
}