//
//  Copyright (c) 2014-2015 Actor LLC. <https://actor.im>
//

import UIKit

public class AASettingsLastSeenController: AATableViewController {

    private var privacy = Actor.getPrivacy()
    
    // MARK: -
    // MARK: Constructors
    
    private let CellIdentifier = "CellIdentifier"
    
    public init() {
        super.init(style: UITableViewStyle.Grouped)
        
        title = AALocalized("SettingsLastSeen")
        
        content = ACAllEvents_Settings.NOTIFICATIONS()
    }
    
    public required init(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    // MARK: -
    
    public override func viewDidLoad() {
        super.viewDidLoad()
        
        tableView.registerClass(AACommonCell.self, forCellReuseIdentifier: CellIdentifier)
        tableView.backgroundColor = appStyle.vcBackyardColor
        
        view.backgroundColor = tableView.backgroundColor
    }
    
    // MARK: -
    // MARK: UITableView Data Source
    
    public override func numberOfSectionsInTableView(tableView: UITableView) -> Int {
        return 1
    }
    
    public override func tableView(tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        return 3
    }
    
    public func tableView(tableView: UITableView, titleForHeaderInSection section: Int) -> String? {
        return nil
    }
    
    public func tableView(tableView: UITableView, titleForFooterInSection section: Int) -> String? {
        return nil
    }
    
    private func lastSeenCell(indexPath: NSIndexPath) -> AACommonCell {
        let cell = tableView.dequeueReusableCellWithIdentifier(CellIdentifier, forIndexPath: indexPath) as! AACommonCell
        
        if indexPath.row == 0 {
            
            cell.setContent(AALocalized("SettingsLastSeenEverybody"))
            
            if (self.privacy == "always") {
                cell.style = .Checkmark
            } else {
                cell.style = .Normal
            }
            
        } else if indexPath.row == 1 {
            
            cell.setContent(AALocalized("SettingsLastSeenContacts"))
            
            if (self.privacy == "contacts") {
                cell.style = .Checkmark
            } else {
                cell.style = .Normal
            }
            
        } else if indexPath.row == 2 {
            
            cell.setContent(AALocalized("SettingsLastSeenNone"))
            
            if (self.privacy == "none") {
                cell.style = .Checkmark
            } else {
                cell.style = .Normal
            }
            
        }
        
        cell.selectionStyle = UITableViewCellSelectionStyle.None
        cell.bottomSeparatorVisible = false
        cell.topSeparatorVisible = false
        cell.bottomSeparatorLeftInset = 0
        cell.topSeparatorLeftInset = 0
        
        return cell
    }
    
    public override func tableView(tableView: UITableView, cellForRowAtIndexPath indexPath: NSIndexPath) -> UITableViewCell {
        return lastSeenCell(indexPath)
    }
    
    public func tableView(tableView: UITableView, willDisplayHeaderView view: UIView, forSection section: Int) {
        let header: UITableViewHeaderFooterView = view as! UITableViewHeaderFooterView
        header.textLabel!.textColor = ActorSDK.sharedActor().style.cellFooterColor
    }
    
    public func tableView(tableView: UITableView, willDisplayFooterView view: UIView, forSection section: Int) {
        let header: UITableViewHeaderFooterView = view as! UITableViewHeaderFooterView
        header.textLabel!.textColor = ActorSDK.sharedActor().style.cellFooterColor
    }
    
    
    public func tableView(tableView: UITableView, didSelectRowAtIndexPath indexPath: NSIndexPath) {
        
        if indexPath.row == 0 {
            
            Actor.setPrivacyWithPrivacy("always")
            
        } else if indexPath.row == 1 {
            
            Actor.setPrivacyWithPrivacy("contacts")
            
        } else if indexPath.row == 2 {
            
            Actor.setPrivacyWithPrivacy("none")
            
        }
        
        self.privacy = Actor.getPrivacy()
        self.tableView.reloadData()
        
    }

}
