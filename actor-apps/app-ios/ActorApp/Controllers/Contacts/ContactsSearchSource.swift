//
//  Copyright (c) 2014-2015 Actor LLC. <https://actor.im>
//

import Foundation

class ContactsSearchSource: SearchSource {
    
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
        let isLast = indexPath.row == tableView.numberOfRowsInSection(indexPath.section)-1;
        
        // Building short name
        var shortName : String? = nil;
        if (indexPath.row == 0) {
            shortName = contact.getName().smallValue();
        } else {
            let prevContact = objectAtIndexPath(NSIndexPath(forRow: indexPath.row-1, inSection: indexPath.section)) as! ACContact;
            
            let prevName = prevContact.getName().smallValue();
            let name = contact.getName().smallValue();
            
            if (prevName != name){
                shortName = name;
            }
        }
        
        (cell as! ContactCell).bindContact(contact, shortValue: shortName, isLast: isLast);
    }
    
    override func buildDisplayList() -> ARBindedDisplayList {
        return Actor.buildContactsDisplayList()
    }
}