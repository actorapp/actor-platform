//
//  Copyright (c) 2014-2015 Actor LLC. <https://actor.im>
//

import Foundation
import UIKit
import MobileCoreServices

class AASettingsWallpapersController: AATableViewController {
    
    // MARK: -
    // MARK: Constructors
    
    private let CellIdentifier = "CellIdentifier"
    
    init() {
        
        super.init(style: UITableViewStyle.Grouped)
        title = "Wallpapers"
        
    }
    
    required init(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        tableView.registerClass(AAWallpapersCell.self, forCellReuseIdentifier: CellIdentifier)
        tableView.backgroundColor = appStyle.vcBackyardColor
        
        view.backgroundColor = tableView.backgroundColor
    }
    
    // MARK: -
    // MARK: UITableView Data Source
    
    override func numberOfSectionsInTableView(tableView: UITableView) -> Int {
        return 2
    }
    
    override func tableView(tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        return 1
    }
    
    override func tableView(tableView: UITableView, cellForRowAtIndexPath indexPath: NSIndexPath) -> UITableViewCell {
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
        
        cell.textLabel?.text = "Photo Library"
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
    
    override func imagePickerController(picker: UIImagePickerController, didFinishPickingImage image: UIImage, editingInfo: [String : AnyObject]?) {
        
        picker.navigationController?.pushViewController(AAWallpapperPreviewController(selectedImage: image), animated: true)
        
    }
    
    override func imagePickerController(picker: UIImagePickerController, didFinishPickingMediaWithInfo info: [String : AnyObject]) {
        
        if let image = info[UIImagePickerControllerOriginalImage] as? UIImage {
            picker.pushViewController(AAWallpapperPreviewController(selectedImage: image), animated: true)
        }
        
    }
    
    override func imagePickerControllerDidCancel(picker: UIImagePickerController) {
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
