//
//  ContactsController.swift
//  ActorClient
//
//  Created by Stepan Korshakov on 23.03.15.
//  Copyright (c) 2015 Actor LLC. All rights reserved.
//

import Foundation
class ContactsBaseController: EngineListController {
    
    override func bindTable(table: UITableView) {
        table.rowHeight = 56
        table.separatorStyle = UITableViewCellSeparatorStyle.None
        table.backgroundColor = Resources.BackyardColor
        
        var footer = UIView(frame: CGRectMake(0, 0, 320, 80));
        
        //        var footerHint = UILabel(frame: CGRectMake(0, 0, 320, 60));
        //        footerHint.textAlignment = NSTextAlignment.Center;
        //        footerHint.font = UIFont.systemFontOfSize(16);
        //        footerHint.textColor = UIColor(red: 164/255.0, green: 164/255.0, blue: 164/255.0, alpha: 1)
        //        footerHint.text = "Swipe for more options";
        //        footer.addSubview(footerHint);
        
        var shadow = UIImageView(image: UIImage(named: "CardBottom2"));
        shadow.frame = CGRectMake(0, 0, 320, 4);
        shadow.contentMode = UIViewContentMode.ScaleToFill;
        footer.addSubview(shadow);
        
        // Header
        
        var header = UIView(frame: CGRectMake(0, 0, 320, 0))
        
        var headerShadow = UIImageView(frame: CGRectMake(0, -4, 320, 4));
        headerShadow.image = UIImage(named: "CardTop2");
        headerShadow.contentMode = UIViewContentMode.ScaleToFill;
        header.addSubview(headerShadow);
        
        table.tableHeaderView = header
        table.tableFooterView = footer
        
        super.bindTable(table)
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