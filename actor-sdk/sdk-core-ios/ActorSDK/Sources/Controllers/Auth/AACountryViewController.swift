//
//  Copyright (c) 2014-2016 Actor LLC. <https://actor.im>
//

import UIKit

public protocol AACountryViewControllerDelegate {
    func countriesController(_ countriesController: AACountryViewController, didChangeCurrentIso currentIso: String)
}

open class AACountryViewController: AATableViewController {
    
    fileprivate var _countries: NSDictionary!
    fileprivate var _letters: Array<String>!
    
    open var delegate: AACountryViewControllerDelegate?
    
    public init() {
        super.init(style: UITableViewStyle.plain)
        
        self.title = AALocalized("AuthCountryTitle")
        
        let cancelButtonItem = UIBarButtonItem(title: AALocalized("NavigationCancel"), style: UIBarButtonItemStyle.plain, target: self, action: #selector(AAViewController.dismissController))
        self.navigationItem.setLeftBarButton(cancelButtonItem, animated: false)
        
        self.content = ACAllEvents_Auth.auth_PICK_COUNTRY()
    }

    public required init(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    open override func viewDidLoad() {
        super.viewDidLoad()
        
        tableView.rowHeight = 44.0
        tableView.sectionIndexBackgroundColor = UIColor.clear
    }
    
    fileprivate func countries() -> NSDictionary {
        if (_countries == nil) {
            let countries = NSMutableDictionary()
            for (_, iso) in ABPhoneField.sortedIsoCodes().enumerated() {
                let countryName = ABPhoneField.countryNameByCountryCode()[iso as! String] as! String
                let phoneCode = ABPhoneField.callingCodeByCountryCode()[iso as! String] as! String
                //            if (self.searchBar.text.length == 0 || [countryName rangeOfString:self.searchBar.text options:NSCaseInsensitiveSearch].location != NSNotFound)
                
                let countryLetter = countryName.substring(to: countryName.characters.index(countryName.startIndex, offsetBy: 1))
                if (countries[countryLetter] == nil) {
                    countries[countryLetter] = NSMutableArray()
                }
                
                (countries[countryLetter]! as AnyObject).add([countryName, iso, phoneCode])
            }
            _countries = countries;
        }
        return _countries;
    }
    
    fileprivate func letters() -> Array<String> {
        if (_letters == nil) {
            _letters = (countries().allKeys as! [String]).sorted(by: { (a: String, b: String) -> Bool in
              return a < b
            })
        }
        return _letters
    }
    
    open func sectionIndexTitlesForTableView(_ tableView: UITableView) -> [AnyObject]! {
        return letters() as [AnyObject]
    }
    
    open func tableView(_ tableView: UITableView, sectionForSectionIndexTitle title: String, atIndex index: Int) -> Int {
        return index
    }
    
    open override func numberOfSections(in tableView: UITableView) -> Int {
        return letters().count;
    }
    
    open override func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        let letter = letters()[section]
        let cs = countries().object(forKey: letter) as! NSArray
        return cs.count
    }
    
    open override func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        let cell: AAAuthCountryCell = tableView.dequeueCell(indexPath)
        let letter = letters()[(indexPath as NSIndexPath).section]
        let countryData = (countries().object(forKey: letter) as! NSArray)[(indexPath as NSIndexPath).row] as! [String]
        
        cell.setTitle(countryData[0])
        cell.setCode("+\(countryData[2])")
        cell.setSearchMode(false)
        
        return cell
    }
    
    open func tableView(_ tableView: UITableView, didSelectRowAtIndexPath indexPath: IndexPath) {
        tableView.deselectRow(at: indexPath, animated: true)
        
        let letter = letters()[(indexPath as NSIndexPath).section]
        let countryData = (countries().object(forKey: letter) as! NSArray)[(indexPath as NSIndexPath).row] as! [String]
        
        delegate?.countriesController(self, didChangeCurrentIso: countryData[1])

        dismissController()
    }
    
    open func tableView(_ tableView: UITableView, heightForHeaderInSection section: Int) -> CGFloat {
        return 25.0
    }
    
    open func tableView(_ tableView: UITableView, titleForHeaderInSection section: Int) -> String? {
        return letters()[section]
    }
    
    open override func viewWillDisappear(_ animated: Bool) {
        super.viewWillDisappear(animated)
        
        UIApplication.shared.setStatusBarStyle(.default, animated: true)
    }
}
