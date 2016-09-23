//
//  Copyright (c) 2014-2016 Actor LLC. <https://actor.im>
//

import UIKit

open class AASettingsLastSeenController: AATableViewController {

    fileprivate var privacy = Actor.getPrivacy()
    
    // MARK: -
    // MARK: Constructors
    
    fileprivate let CellIdentifier = "CellIdentifier"
    
    public init() {
        super.init(style: UITableViewStyle.grouped)
        
        title = AALocalized("PrivacyLastSeen")
        
        content = ACAllEvents_Settings.notifications()
    }
    
    public required init(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    // MARK: -
    
    open override func viewDidLoad() {
        super.viewDidLoad()
        
        tableView.register(AACommonCell.self, forCellReuseIdentifier: CellIdentifier)
        tableView.backgroundColor = appStyle.vcBackyardColor
        tableView.separatorColor = appStyle.vcSeparatorColor
        
        view.backgroundColor = tableView.backgroundColor
    }
    
    // MARK: -
    // MARK: UITableView Data Source
    
    open override func numberOfSections(in tableView: UITableView) -> Int {
        return 1
    }
    
    open override func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        return 3
    }
    
    open func tableView(_ tableView: UITableView, titleForHeaderInSection section: Int) -> String? {
        return nil
    }
    
    open func tableView(_ tableView: UITableView, titleForFooterInSection section: Int) -> String? {
        return nil
    }
    
    fileprivate func lastSeenCell(_ indexPath: IndexPath) -> AACommonCell {
        let cell = tableView.dequeueReusableCell(withIdentifier: CellIdentifier, for: indexPath) as! AACommonCell
        
        if (indexPath as NSIndexPath).row == 0 {
            
            cell.setContent(AALocalized("PrivacyLastSeenEverybody"))
            
            if (self.privacy == "always") {
                cell.style = .checkmark
            } else {
                cell.style = .normal
            }
            
        } else if (indexPath as NSIndexPath).row == 1 {
            
            cell.setContent(AALocalized("PrivacyLastSeenContacts"))
            
            if (self.privacy == "contacts") {
                cell.style = .checkmark
            } else {
                cell.style = .normal
            }
            
        } else if (indexPath as NSIndexPath).row == 2 {
            
            cell.setContent(AALocalized("PrivacyLastSeenNone"))
            
            if (self.privacy == "none") {
                cell.style = .checkmark
            } else {
                cell.style = .normal
            }
            
        }
        
        cell.selectionStyle = UITableViewCellSelectionStyle.none
        cell.bottomSeparatorVisible = false
        cell.topSeparatorVisible = false
        cell.bottomSeparatorLeftInset = 0
        cell.topSeparatorLeftInset = 0
        
        return cell
    }
    
    open override func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        return lastSeenCell(indexPath)
    }
    
    open func tableView(_ tableView: UITableView, willDisplayHeaderView view: UIView, forSection section: Int) {
        let header: UITableViewHeaderFooterView = view as! UITableViewHeaderFooterView
        header.textLabel!.textColor = ActorSDK.sharedActor().style.cellFooterColor
    }
    
    open func tableView(_ tableView: UITableView, willDisplayFooterView view: UIView, forSection section: Int) {
        let header: UITableViewHeaderFooterView = view as! UITableViewHeaderFooterView
        header.textLabel!.textColor = ActorSDK.sharedActor().style.cellFooterColor
    }
    
    
    open func tableView(_ tableView: UITableView, didSelectRowAtIndexPath indexPath: IndexPath) {
        
        if (indexPath as NSIndexPath).row == 0 {
            
            Actor.setPrivacyWithPrivacy("always")
            
        } else if (indexPath as NSIndexPath).row == 1 {
            
            Actor.setPrivacyWithPrivacy("contacts")
            
        } else if (indexPath as NSIndexPath).row == 2 {
            
            Actor.setPrivacyWithPrivacy("none")
            
        }
        
        self.privacy = Actor.getPrivacy()
        self.tableView.reloadData()
        
    }

}
