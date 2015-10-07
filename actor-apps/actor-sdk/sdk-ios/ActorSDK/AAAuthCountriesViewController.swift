//
//  Copyright (c) 2014-2015 Actor LLC. <https://actor.im>
//

import UIKit

public protocol AAAuthCountriesViewControllerDelegate {
    func countriesController(countriesController: AAAuthCountriesViewController, didChangeCurrentIso currentIso: String)
}

public class AAAuthCountriesViewController: AATableViewController {
    
    // MARK: -
    // MARK: Private vars
    
    private let countryCellIdentifier = "countryCellIdentifier"
    
    private var _countries: NSDictionary!
    private var _letters: NSArray!
    
    // MARK: -
    // MARK: Public vars
    
    var delegate: AAAuthCountriesViewControllerDelegate?
    var currentIso: String = ""
    
    // MARK: -
    // MARK: Contructors
    
    public init() {
        super.init(style: UITableViewStyle.Plain)
        
        self.title = "Country" // TODO: Localize
        
        let cancelButtonItem = UIBarButtonItem(title: "Cancel", style: UIBarButtonItemStyle.Plain, target: self, action: Selector("cancelButtonPressed")) // TODO: Localize
        self.navigationItem.setLeftBarButtonItem(cancelButtonItem, animated: false)
        
        self.content = ACAllEvents_Auth.AUTH_PICK_COUNTRY()
    }

    public required init(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    // MARK: -

    public override func viewDidLoad() {
        super.viewDidLoad()
        
        tableView.registerClass(AAAuthCountryCell.self, forCellReuseIdentifier: countryCellIdentifier)
        tableView.tableFooterView = UIView()
        tableView.rowHeight = 44.0
        tableView.sectionIndexBackgroundColor = UIColor.clearColor()
    }
    
    public override func viewWillAppear(animated: Bool) {
        super.viewWillAppear(animated)
        
        MainAppTheme.navigation.applyStatusBar()
    }
    
    public override func viewWillDisappear(animated: Bool) {
        super.viewWillDisappear(animated)
    }
    
    // MARK: -
    // MARK: Methods
    
    public func cancelButtonPressed() {

        dismiss()
    }
    
    // MARK: -
    // MARK: Getters
    
    private func countries() -> NSDictionary {
        if (_countries == nil) {
            var countries = NSMutableDictionary()
            for (index, iso) in ABPhoneField.sortedIsoCodes().enumerate() {
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
            _letters = (countries().allKeys as NSArray).sortedArrayUsingSelector(Selector("compare:"))
        }
        return _letters;
    }
    
    // MARK: -
    // MARK: UITableView Data Source
    
    public func sectionIndexTitlesForTableView(tableView: UITableView) -> [AnyObject]! {
        return [UITableViewIndexSearch] + letters() as [AnyObject]
    }
    
    public func tableView(tableView: UITableView, sectionForSectionIndexTitle title: String, atIndex index: Int) -> Int {
        if title == UITableViewIndexSearch {
            return 0
        }
        return index - 1
    }
    
    public override func numberOfSectionsInTableView(tableView: UITableView) -> Int {
        return letters().count;
    }
    
    public override func tableView(tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        return (countries()[letters()[section] as! String] as! NSArray).count
    }
    
    public override func tableView(tableView: UITableView, cellForRowAtIndexPath indexPath: NSIndexPath) -> UITableViewCell {
        var cell: AAAuthCountryCell = tableView.dequeueReusableCellWithIdentifier(countryCellIdentifier, forIndexPath: indexPath) as! AAAuthCountryCell
        
        cell.setSearchMode(false) // TODO: Add search bar
        
        let letter = letters()[indexPath.section] as! String
        let countryData = (countries().objectForKey(letter) as! NSArray)[indexPath.row] as! [String]
        cell.setTitle(countryData[0])
        cell.setCode("+\(countryData[2])")

        return cell
    }
    
    // MARK: -
    // MARK: UITableView Delegate
    
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
        let letter = letters()[section] as! String
        return letter
    }
}