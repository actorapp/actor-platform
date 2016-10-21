//
//  Copyright (c) 2014-2016 Actor LLC. <https://actor.im>
//

import Foundation
import SZTextView

open class AAGroupEditInfoController: AAViewController, UITextViewDelegate {
    
    fileprivate var isChannel = false
    fileprivate let scrollView = UIScrollView()
    fileprivate let bgContainer = UIView()
    fileprivate let topSeparator = UIView()
    fileprivate let bottomSeparator = UIView()
    
    fileprivate let avatarView = AAAvatarView()
    fileprivate let nameInput = UITextField()
    fileprivate let nameInputSeparator = UIView()
    fileprivate let descriptionView = SZTextView()
    fileprivate let descriptionSeparator = UIView()
    
    public init(gid: Int) {
        super.init()
        self.gid = gid
        self.isChannel = group.groupType == ACGroupType.channel()
    }
    
    public required init(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    open override func viewDidLoad() {
        super.viewDidLoad()
        
        view.backgroundColor = appStyle.vcBackyardColor
        
        scrollView.alwaysBounceVertical = true
        
        nameInputSeparator.backgroundColor = appStyle.vcSeparatorColor
        topSeparator.backgroundColor = appStyle.vcSeparatorColor
        bottomSeparator.backgroundColor = appStyle.vcSeparatorColor
        descriptionSeparator.backgroundColor = appStyle.vcSeparatorColor
        
        bgContainer.backgroundColor = appStyle.vcBgColor
        
        scrollView.addSubview(bgContainer)
        bgContainer.addSubview(avatarView)
        bgContainer.addSubview(nameInput)
        bgContainer.addSubview(nameInputSeparator)
        bgContainer.addSubview(descriptionView)
        bgContainer.addSubview(descriptionSeparator)
        bgContainer.addSubview(topSeparator)
        bgContainer.addSubview(bottomSeparator)
        view.addSubview(scrollView)
        
        avatarView.bind(group.name.get(), id: gid, avatar: group.avatar.get())
        avatarView.viewDidTap = {
            self.avatarDidTap()
        }
        
        nameInput.font = UIFont.systemFont(ofSize: 19)
        if isChannel {
            nameInput.placeholder = AALocalized("GroupEditNameChannel")
        } else {
            nameInput.placeholder = AALocalized("GroupEditName")
        }
        nameInput.text = group.name.get()
        
        descriptionView.delegate = self
        descriptionView.font = UIFont.systemFont(ofSize: 17)
        descriptionView.placeholder = AALocalized("GroupEditDescription")
        descriptionView.text = group.about.get()
        descriptionView.isScrollEnabled = false
        
        if isChannel {
            navigationItem.title = AALocalized("GroupEditTitleChannel")
        } else {
            navigationItem.title = AALocalized("GroupEditTitle")
        }
        navigationItem.rightBarButtonItem = UIBarButtonItem(title: AALocalized("NavigationSave"), style: .done, target: self, action: #selector(saveDidPressed))
        navigationItem.leftBarButtonItem = UIBarButtonItem(title: AALocalized("NavigationCancel"), style: .done, target: self, action: #selector(cancelEdit))
    }
    
    open override func viewWillLayoutSubviews() {
        super.viewWillLayoutSubviews()
        
        scrollView.frame = CGRect(x: 0, y: 0, width: view.width, height: view.height)
        
        nameInput.frame = CGRect(x: 94, y: 34, width: view.width - 94 - 10, height: 24)
        nameInputSeparator.frame = CGRect(x: 94, y: 34 + 24, width: view.width - 94, height: 0.5)
        avatarView.frame = CGRect(x: 14, y: 14, width: 66, height: 66)
        
        descriptionSeparator.frame = CGRect(x: 0, y: 94, width: view.width, height: 0.5)
        topSeparator.frame = CGRect(x: 0, y: 0, width: view.width, height: 0.5)
        
        layoutContainer()
    }
    
    open func avatarDidTap() {
        let hasCamera = UIImagePickerController.isSourceTypeAvailable(UIImagePickerControllerSourceType.camera)
        self.showActionSheet( hasCamera ? ["PhotoCamera", "PhotoLibrary"] : ["PhotoLibrary"],
                              cancelButton: "AlertCancel",
                              destructButton: self.group.getAvatarModel().get() != nil ? "PhotoRemove" : nil,
                              sourceView: self.view,
                              sourceRect: self.view.bounds,
                              tapClosure: { (index) -> () in
                                if (index == -2) {
                                    self.confirmAlertUser("PhotoRemoveGroupMessage",
                                        action: "PhotoRemove",
                                        tapYes: { () -> () in
                                            Actor.removeGroupAvatar(withGid: jint(self.gid))
                                            self.avatarView.bind(self.group.name.get(), id: self.gid, avatar: nil)
                                        }, tapNo: nil)
                                } else if (index >= 0) {
                                    let takePhoto: Bool = (index == 0) && hasCamera
                                    self.pickAvatar(takePhoto, closure: { (image) -> () in
                                        let fileName = Actor.changeGroupAvatar(jint(self.gid), image: image)
                                        self.avatarView.bind(self.group.name.get(), id: self.gid, fileName: CocoaFiles.pathFromDescriptor(fileName))
                                    })
                                }
        })
    }
    
    open func saveDidPressed() {
        let text = nameInput.text!.trim()
        let about = self.descriptionView.text!.trim()
        nameInput.resignFirstResponder()
        descriptionView.resignFirstResponder()
        if text != group.name.get() {
            executePromise(Actor.editGroupTitle(withGid: jint(gid), withTitle: text).then({ (v: ARVoid!) in
                if about != self.group.about.get() {
                    self.executePromise(Actor.editGroupAbout(withGid: jint(self.gid), withAbout: about).then({ (v: ARVoid!) in
                        self.cancelEdit()
                    }))
                } else {
                    self.cancelEdit()
                }
            }))
        } else {
            if about != self.group.about.get() {
                self.executePromise(Actor.editGroupAbout(withGid: jint(self.gid), withAbout: about).then({ (v: ARVoid!) in
                    self.cancelEdit()
                }))
            } else {
                self.cancelEdit()
            }
        }
    }
    
    func cancelEdit() {
        nameInput.resignFirstResponder()
        descriptionView.resignFirstResponder()
        dismissController()
    }
    
    open func textViewDidChange(_ textView: UITextView) {
        layoutContainer()
    }
    
    fileprivate func layoutContainer() {
        let newSize = descriptionView.sizeThatFits(CGSize(width: view.width - 20, height: CGFloat.greatestFiniteMagnitude))
        descriptionView.frame = CGRect(x: 10, y: 102, width: view.width - 20, height: max(newSize.height, 33))
        bgContainer.frame = CGRect(x: 0, y: 0, width: view.width, height: 100 + descriptionView.height + 8)
        bottomSeparator.frame = CGRect(x: 0, y: bgContainer.height - 0.5, width: bgContainer.width, height: 0.5)
    }
}
