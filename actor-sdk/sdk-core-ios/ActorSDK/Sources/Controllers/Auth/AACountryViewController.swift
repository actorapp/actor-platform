//
//  Copyright (c) 2014-2016 Actor LLC. <https://actor.im>
//

import UIKit

public protocol AACountryViewControllerDelegate {
    func countriesController(countriesController: AACountryViewController, didChangeCurrentIso currentIso: String)
}

public class AACountryViewController: AATableViewController {
    
    private var _countries: NSDictionary!
    private var _letters: NSArray!
    
    public var delegate: AACountryViewControllerDelegate?
    
    public init() {
        super.init(style: UITableViewStyle.Plain)
        
        self.title = AALocalized("AuthCountryTitle")
        
        let cancelButtonItem = UIBarButtonItem(title: AALocalized("NavigationCancel"), style: UIBarButtonItemStyle.Plain, target: self, action: Selector("dismiss"))
        self.navigationItem.setLeftBarButtonItem(cancelButtonItem, animated: false)
        
        self.content = ACAllEvents_Auth.AUTH_PICK_COUNTRY()
    }

    public required init(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    public override func viewDidLoad() {
        super.viewDidLoad()
        
        tableView.rowHeight = 44.0
        tableView.sectionIndexBackgroundColor = UIColor.clearColor()
    }
    
    private func countries() -> NSDictionary {
        if (_countries == nil) {
            let countries = NSMutableDictionary()
            for (_, iso) in ABPhoneField.sortedIsoCodes().enumerate() {
                let countryName = ABPhoneField.countryNameByCountryCode()[iso as! String] as! String
                let phoneCode = ABPhoneField.callingCodeByCountryCode()[iso as! String] as! String
                //            if (self.searchBar.text.length == 0 || [countryName rangeOfString:self.searchBar.text options:NSCaseInsensitiveSearch].location != NSNotFound)
                
                let countryLetter = countryName.substringToIndex(countryName.startIndex.advancedBy(1))
                if (countries[countryLetter] == nil) {
                    countries[countryLetter] = NSMutableArray()
                }
                
                countries[countryLetter]!.addObject([countryName, iso, phoneCode])
            }
            _countries = countries;
        }
        return _countries;
    }
    
    private func letters() -> NSArray {
        if (_letters == nil) {
            _letters = (countries().allKeys as NSArray).sortedArrayUsingSelector(#selector(YYTextPosition.compare(_:)))
        }
        return _letters;
    }
    
    public func sectionIndexTitlesForTableView(tableView: UITableView) -> [AnyObject]! {
        return letters() as [AnyObject]
    }
    
    public func tableView(tableView: UITableView, sectionForSectionIndexTitle title: String, atIndex index: Int) -> Int {
        return index
    }
    
    public override func numberOfSectionsInTableView(tableView: UITableView) -> Int {
        return letters().count;
    }
    
    public override func tableView(tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        return (countries()[letters()[section] as! String] as! NSArray).count
    }
    
    public override func tableView(tableView: UITableView, cellForRowAtIndexPath indexPath: NSIndexPath) -> UITableViewCell {
        let cell: AAAuthCountryCell = tableView.dequeueCell(indexPath)
        let letter = letters()[indexPath.section] as! String
        let countryData = (countries().objectForKey(letter) as! NSArray)[indexPath.row] as! [String]
        
        cell.setTitle(countryData[0])
        cell.setCode("+\(countryData[2])")
        cell.setSearchMode(false)
        
        return cell
    }
    
    public func tableView(tableView: UITableView, didSelectRowAtIndexPath indexPath: NSIndexPath) {
        tableView.deselectRowAtIndexPath(indexPath, animated: true)
        
        let letter = letters()[indexPath.section] as! String
        let countryData = (countries().objectForKey(letter) as! NSArray)[indexPath.row] as! [String]
        
        delegate?.countriesController(self, didChangeCurrentIso: countryData[1])

        dismiss()
    }
    
    public func tableView(tableView: UITableView, heightForHeaderInSection section: Int) -> CGFloat {
        return 25.0
    }
    
    public func tableView(tableView: UITableView, titleForHeaderInSection section: Int) -> String? {
        return letters()[section] as? String
    }
    
    public override func viewWillDisappear(animated: Bool) {
        super.viewWillDisappear(animated)
        
        UIApplication.sharedApplication().setStatusBarStyle(.Default, animated: true)
    }
}