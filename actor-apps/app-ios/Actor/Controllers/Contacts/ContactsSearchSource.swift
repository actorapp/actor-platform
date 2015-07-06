//
//  Copyright (c) 2015 Actor LLC. <https://actor.im>
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
        var contact = item as! AMContact;
        let isLast = indexPath.row == tableView.numberOfRowsInSection(indexPath.section)-1;
        
        // Building short name
        var shortName : String? = nil;
        if (indexPath.row == 0) {
            shortName = contact.getName().smallValue();
        } else {
            var prevContact = objectAtIndexPath(NSIndexPath(forRow: indexPath.row-1, inSection: indexPath.section)) as! AMContact;
            
            var prevName = prevContact.getName().smallValue();
            var name = contact.getName().smallValue();
            
            if (prevName != name){
                shortName = name;
            }
        }
        
        (cell as! ContactCell).bindContact(contact, shortValue: shortName, isLast: isLast);
    }
    
    override func buildDisplayList() -> AMBindedDisplayList {
        return MSG.buildContactDisplayList()
    }
}