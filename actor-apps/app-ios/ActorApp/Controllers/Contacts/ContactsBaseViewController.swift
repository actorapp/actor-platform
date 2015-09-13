//
//  Copyright (c) 2014-2015 Actor LLC. <https://actor.im>
//

import Foundation

class ContactsBaseViewController: EngineListController {
    
    override func bindTable(table: UITableView, fade: Bool) {
        view.backgroundColor = MainAppTheme.list.bgColor
        
        table.rowHeight = 56
        table.separatorStyle = UITableViewCellSeparatorStyle.None
        table.backgroundColor = MainAppTheme.list.backyardColor
        
        super.bindTable(table, fade: fade)
    }
    
    override func buildCell(tableView: UITableView, cellForRowAtIndexPath indexPath: NSIndexPath, item: AnyObject?) -> UITableViewCell {
        let reuseId = "cell_contact";
        
        var cell = tableView.dequeueReusableCellWithIdentifier(reuseId) as! ContactCell?;
        
        if (cell == nil) {
            cell = ContactCell(reuseIdentifier:reuseId);
        }
        
        return cell!;
    }
    
    override func bindCell(tableView: UITableView, cellForRowAtIndexPath indexPath: NSIndexPath, item: AnyObject?, cell: UITableViewCell) {
        let contact = item as! ACContact;
        var isLast = false
        if (indexPath.section == tableView.numberOfSections - 1) {
            isLast = indexPath.row == tableView.numberOfRowsInSection(indexPath.section)
        }
        
        // Building short name
        var shortName : String? = nil;
        if (indexPath.row == 0) {
            shortName = contact.getName().smallValue();
        } else {
            let prevContact = objectAtIndexPath(NSIndexPath(forRow: indexPath.row-1, inSection: indexPath.section)) as! ACContact;
        
            let prevName = prevContact.getName().smallValue();
            let name = contact.getName().smallValue();
        
            if (prevName != name) {
                shortName = name;
            }
        }
        
        (cell as! ContactCell).bindContact(contact, shortValue: shortName, isLast: isLast);
    }
    
    override func buildDisplayList() -> ARBindedDisplayList {
        return Actor.buildContactsDisplayList()
    }
}