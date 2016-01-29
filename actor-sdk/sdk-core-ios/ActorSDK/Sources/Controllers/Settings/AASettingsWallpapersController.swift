//
//  Copyright (c) 2014-2016 Actor LLC. <https://actor.im>
//

import Foundation
import UIKit
import MobileCoreServices

public class AASettingsWallpapersController: AATableViewController {
    
    // MARK: -
    // MARK: Constructors
    
    private let CellIdentifier = "CellIdentifier"
    
    public init() {
        
        super.init(style: UITableViewStyle.Grouped)
        title = AALocalized("WallpapersTitle")
    }
    
    required public init(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    
    override public func viewDidLoad() {
        super.viewDidLoad()
        
        tableView.registerClass(AAWallpapersCell.self, forCellReuseIdentifier: CellIdentifier)
        tableView.backgroundColor = appStyle.vcBackyardColor
        
        view.backgroundColor = tableView.backgroundColor
    }
    
    // MARK: -
    // MARK: UITableView Data Source
    
    override public func numberOfSectionsInTableView(tableView: UITableView) -> Int {
        return 2
    }
    
    override public func tableView(tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        return 1
    }
    
    override public func tableView(tableView: UITableView, cellForRowAtIndexPath indexPath: NSIndexPath) -> UITableViewCell {
        if indexPath.section == 0 {
            return photosLibrary(indexPath)
        } else {
            return wallpapersCell(indexPath)
        }
    }
    
    func tableView(tableView: UITableView, didSelectRowAtIndexPath indexPath: NSIndexPath) {
        //
        self.tableView.deselectRowAtIndexPath(indexPath, animated: true)
        
        if indexPath.section == 0 {
            
            self.pickImage(.PhotoLibrary)
            
        }
        
    }
    
    
    func tableView(tableView: UITableView, titleForHeaderInSection section: Int) -> String? {
        return nil
    }
    
    func tableView(tableView: UITableView, titleForFooterInSection section: Int) -> String? {
        return nil
    }
    
    func tableView(tableView: UITableView, heightForRowAtIndexPath indexPath: NSIndexPath) -> CGFloat {
        if indexPath.section == 0 {
            return 40
        } else {
            return 180
        }
    }
    
    // MARK: -
    // MARK: Create cells
    
    private func photosLibrary(indexPath: NSIndexPath) -> AACommonCell {
        let cell = AACommonCell()
        
        cell.textLabel?.text = AALocalized("WallpapersPhoto")
        cell.style = .Navigation
        
        return cell
    }
    
    private func wallpapersCell(indexPath: NSIndexPath) -> AAWallpapersCell {
        let cell = tableView.dequeueReusableCellWithIdentifier(CellIdentifier, forIndexPath: indexPath) as! AAWallpapersCell
        
        cell.selectionStyle = UITableViewCellSelectionStyle.None
        cell.wallpapperDidTap = { [unowned self] (name) -> () in
            self.presentViewController(AAWallpapperPreviewController(imageName: name), animated: true, completion: nil)
        }
        
        return cell
    }
    
    // MARK: -
    // MARK: Picker delegate
    
    override public func imagePickerController(picker: UIImagePickerController, didFinishPickingImage image: UIImage, editingInfo: [String : AnyObject]?) {
        
        picker.navigationController?.pushViewController(AAWallpapperPreviewController(selectedImage: image), animated: true)
        
    }
    
    override public func imagePickerController(picker: UIImagePickerController, didFinishPickingMediaWithInfo info: [String : AnyObject]) {
        
        if let image = info[UIImagePickerControllerOriginalImage] as? UIImage {
            picker.pushViewController(AAWallpapperPreviewController(selectedImage: image), animated: true)
        }
        
    }
    
    override public func imagePickerControllerDidCancel(picker: UIImagePickerController) {
        picker.dismissViewControllerAnimated(true, completion: nil)
    }
    
    // MARK: -
    // MARK: Image picking
    
    func pickImage(source: UIImagePickerControllerSourceType) {
        let pickerController = AAImagePickerController()
        pickerController.sourceType = source
        pickerController.mediaTypes = [kUTTypeImage as String]
        pickerController.delegate = self
        
        self.presentViewController(pickerController, animated: true, completion: nil)
    }
    
    
}
