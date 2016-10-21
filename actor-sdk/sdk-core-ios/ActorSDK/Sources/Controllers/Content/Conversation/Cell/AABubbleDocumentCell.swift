//
//  Copyright (c) 2014-2016 Actor LLC. <https://actor.im>
//

import UIKit
import VBFPopFlatButton

open class AABubbleDocumentCell: AABubbleBaseFileCell, UIDocumentInteractionControllerDelegate {
    
    fileprivate let progress = AAProgressView(size: CGSize(width: 48, height: 48))
    fileprivate let fileIcon = UIImageView()
    
    fileprivate let titleLabel = UILabel()
    fileprivate let sizeLabel = UILabel()
    
    fileprivate let dateLabel = UILabel()
    fileprivate let statusView = UIImageView()
    
    fileprivate var bindedLayout: DocumentCellLayout!
    
    public init(frame: CGRect) {
        super.init(frame: frame, isFullSize: false)
        
        dateLabel.font = UIFont.italicSystemFont(ofSize: 11)
        dateLabel.lineBreakMode = .byClipping
        dateLabel.numberOfLines = 1
        dateLabel.contentMode = UIViewContentMode.topLeft
        dateLabel.textAlignment = NSTextAlignment.right
        
        statusView.contentMode = UIViewContentMode.center
        
        titleLabel.font = UIFont.systemFont(ofSize: 16.0)
        titleLabel.textColor = appStyle.chatTextOutColor
        titleLabel.text = " "
        titleLabel.sizeToFit()
        titleLabel.lineBreakMode = NSLineBreakMode.byTruncatingTail
        
        sizeLabel.font = UIFont.systemFont(ofSize: 13.0)
        sizeLabel.textColor = appStyle.chatTextOutColor
        sizeLabel.text = " "
        sizeLabel.sizeToFit()
        
        contentView.addSubview(titleLabel)
        contentView.addSubview(sizeLabel)
        
        contentView.addSubview(dateLabel)
        contentView.addSubview(statusView)
        
        contentView.addSubview(fileIcon)
        contentView.addSubview(progress)
        
        self.contentInsets = UIEdgeInsetsMake(0, 0, 0, 0)
        
        self.bubble.isUserInteractionEnabled = true
        self.bubble.addGestureRecognizer(UITapGestureRecognizer(target: self, action: #selector(AABubbleDocumentCell.documentDidTap)))
    }

    public required init(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    // MARK: -
    // MARK: Bind
    
    open override func bind(_ message: ACMessage, receiveDate: jlong, readDate: jlong, reuse: Bool, cellLayout: AACellLayout, setting: AACellSetting) {

        self.bindedLayout = cellLayout as! DocumentCellLayout
        
        let document = message.content as! ACDocumentContent
        
        self.bubbleInsets = UIEdgeInsets(
            top: setting.clenchTop ? AABubbleCell.bubbleTopCompact : AABubbleCell.bubbleTop,
            left: 10 + (AADevice.isiPad ? 16 : 0),
            bottom: setting.clenchBottom ? AABubbleCell.bubbleBottomCompact : AABubbleCell.bubbleBottom,
            right: 10 + (AADevice.isiPad ? 16 : 0))
        
        if (!reuse) {
            if (isOut) {
                bindBubbleType(.mediaOut, isCompact: false)
                dateLabel.textColor = appStyle.chatTextDateOutColor
                self.statusView.isHidden = false
            } else {
                bindBubbleType(.mediaIn, isCompact: false)
                dateLabel.textColor = appStyle.chatTextDateInColor
                self.statusView.isHidden = true
            }
            
            titleLabel.text = bindedLayout.fileName
            sizeLabel.text = bindedLayout.fileSize
            
            // Reset progress
            self.progress.hideButton()
            UIView.animate(withDuration: 0, animations: { () -> Void in
                self.progress.isHidden = true
                self.fileIcon.isHidden = true
            })
            
            // Bind file
            
            // Respecting Photo Dowload settings for small documents
            let autoDownload: Bool
            if self.peer.isGroup {
                autoDownload = ActorSDK.sharedActor().isPhotoAutoDownloadGroup
            } else if self.peer.isPrivate {
                autoDownload = ActorSDK.sharedActor().isPhotoAutoDownloadPrivate
            } else {
                autoDownload = false
            }
            fileBind(message, autoDownload: autoDownload && (document.getSource().getSize() < 1024 * 1024 * 1024))
        }
        
        // Always update date and state
        dateLabel.text = cellLayout.date
        
        // Setting message status
        if (isOut) {
            switch(message.messageState.toNSEnum()) {
            case .SENT:
                if message.sortDate <= readDate {
                    self.statusView.image = appStyle.chatIconCheck2
                    self.statusView.tintColor = appStyle.chatStatusRead
                } else if message.sortDate <= receiveDate {
                    self.statusView.image = appStyle.chatIconCheck2
                    self.statusView.tintColor = appStyle.chatStatusReceived
                } else {
                    self.statusView.image = appStyle.chatIconCheck1
                    self.statusView.tintColor = appStyle.chatStatusSent
                }
            case .ERROR:
                self.statusView.image = appStyle.chatIconError
                self.statusView.tintColor = appStyle.chatStatusError
                break
            default:
                self.statusView.image = appStyle.chatIconClock
                self.statusView.tintColor = appStyle.chatStatusSending
                break
            }
        }
    }
    
    open func documentDidTap() {
        
        let content = bindedMessage!.content as! ACDocumentContent
        if let fileSource = content.getSource() as? ACFileRemoteSource {
            
            Actor.requestState(withFileId: fileSource.getFileReference().getFileId(), with: AAFileCallback(
                notDownloaded: { () -> () in
                    Actor.startDownloading(with: fileSource.getFileReference())
                }, onDownloading: { (progress) -> () in
                    Actor.cancelDownloading(withFileId: fileSource.getFileReference().getFileId())
                }, onDownloaded: { (reference) -> () in
                    let docController = UIDocumentInteractionController(url: URL(fileURLWithPath: CocoaFiles.pathFromDescriptor(reference)))
                    docController.delegate = self
                    
                    if (docController.presentPreview(animated: true)) {
                        return
                    }
            }))
            
        } else if let fileSource = content.getSource() as? ACFileLocalSource {
            let rid = bindedMessage!.rid
            Actor.requestUploadState(withRid: rid, with: AAUploadFileCallback(
                notUploaded: { () -> () in
                    Actor.resumeUpload(withRid: rid)
                }, onUploading: { (progress) -> () in
                    Actor.pauseUpload(withRid: rid)
                }, onUploadedClosure: { () -> () in
                    let docController = UIDocumentInteractionController(url: URL(fileURLWithPath: CocoaFiles.pathFromDescriptor(fileSource.getFileDescriptor())))
                    docController.delegate = self
                    
                    if (docController.presentPreview(animated: true)) {
                        return
                    }
            }))
        }
    }
    
    open override func fileStateChanged(_ reference: String?, progress: Int?, isPaused: Bool, isUploading: Bool, selfGeneration: Int) {
        self.runOnUiThread(selfGeneration) { () -> () in
            if isUploading {
                if isPaused {
                    self.fileIcon.hideView()
                    
                    self.progress.showView()
                    self.progress.hideProgress()
                    self.progress.setButtonType(FlatButtonType.buttonUpBasicType, animated: true)
                    
                } else {
                    self.fileIcon.hideView()
                    
                    self.progress.showView()
                    self.progress.setProgress(Double(progress!)/100.0)
                    self.progress.setButtonType(FlatButtonType.buttonPausedType, animated: true)
                }
            } else {
                if reference != nil {
                    self.fileIcon.image = self.bindedLayout.icon
                    self.fileIcon.showView()
                    
                    self.progress.hideView()
                    self.progress.setProgress(1)
                } else {
                    if isPaused {
                        self.fileIcon.hideView()
                        
                        self.progress.showView()
                        self.progress.hideProgress()
                        self.progress.setButtonType(FlatButtonType.buttonDownloadType, animated: true)
                    } else {
                        
                        self.fileIcon.hideView()
                        
                        self.progress.showView()
                        self.progress.setProgress(Double(progress!)/100.0)
                        self.progress.setButtonType(FlatButtonType.buttonPausedType, animated: true)
                    }
                }
            }
        }
    }
    
    open override func layoutContent(_ maxWidth: CGFloat, offsetX: CGFloat) {
        let insets = fullContentInsets
        
        let contentWidth = self.contentView.frame.width
        let top = insets.top - 2
//        let contentHeight = self.contentView.frame.height
        
        layoutBubble(200, contentHeight: 66)
        
        let contentLeft = self.isOut ? contentWidth - 200 - insets.right - contentInsets.left : insets.left
        
        // Content
        self.titleLabel.frame = CGRect(x: contentLeft + 62, y: 16 + top, width: 200 - 64, height: 22)
        self.sizeLabel.frame = CGRect(x: contentLeft + 62, y: 16 + 22 + top, width: 200 - 64, height: 22)
        
        // Progress state
        let progressRect = CGRect(x: contentLeft + 8, y: 12 + top, width: 48, height: 48)
        self.progress.frame = progressRect
        self.fileIcon.frame = CGRect(x: contentLeft + 16, y: 20 + top, width: 32, height: 32)
        
        // Message state
        if (self.isOut) {
            self.dateLabel.frame = CGRect(x: self.bubble.frame.maxX - 70 - self.bubblePadding, y: self.bubble.frame.maxY - 24, width: 46, height: 26)
            self.statusView.frame = CGRect(x: self.bubble.frame.maxX - 24 - self.bubblePadding, y: self.bubble.frame.maxY - 24, width: 20, height: 26)
            self.statusView.isHidden = false
        } else {
            self.dateLabel.frame = CGRect(x: self.bubble.frame.maxX - 47 - self.bubblePadding, y: self.bubble.frame.maxY - 24, width: 46, height: 26)
            self.statusView.isHidden = true
        }
    }

    open func documentInteractionControllerViewControllerForPreview(_ controller: UIDocumentInteractionController) -> UIViewController {
        return self.controller
    }
}

open class AABubbleDocumentCellLayout: AABubbleLayouter {
    
    open func isSuitable(_ message: ACMessage) -> Bool {
        
        return message.content is ACDocumentContent
        
    }
    
    open func buildLayout(_ peer: ACPeer, message: ACMessage) -> AACellLayout {
        return DocumentCellLayout(message: message, layouter: self)
    }
    
    open func cellClass() -> AnyClass {
        return AABubbleDocumentCell.self
    }
}

open class DocumentCellLayout: AACellLayout {
    
    open let fileName: String
    open let fileExt: String
    open let fileSize: String
    
    open let icon: UIImage
    open let fastThumb: Data?
    
    open let autoDownload: Bool
    
    public init(fileName: String, fileExt: String, fileSize: Int, fastThumb: ACFastThumb?, date: Int64, autoDownload: Bool, layouter: AABubbleLayouter) {
        
        // File metadata
        self.fileName = fileName
        self.fileExt = fileExt.lowercased()
        self.fileSize = Actor.getFormatter().formatFileSize(jint(fileSize))
        
        // Auto download flag
        self.autoDownload = autoDownload
        
        // Fast thumb
        self.fastThumb = fastThumb?.getImage().toNSData()
        
        // File icon
        var fileName = "file_unknown"
        if (AAFileTypes[self.fileExt] != nil) {
            switch(AAFileTypes[self.fileExt]!) {
            case AAFileType.music:
                fileName = "file_music"
                break
            case AAFileType.doc:
                fileName = "file_doc"
                break
            case AAFileType.spreadsheet:
                fileName = "file_xls"
                break
            case AAFileType.video:
                fileName = "file_video"
                break
            case AAFileType.presentation:
                fileName = "file_ppt"
                break
            case AAFileType.pdf:
                fileName = "file_pdf"
                break
            case AAFileType.apk:
                fileName = "file_apk"
                break
            case AAFileType.rar:
                fileName = "file_rar"
                break
            case AAFileType.zip:
                fileName = "file_zip"
                break
            case AAFileType.csv:
                fileName = "file_csv"
                break
            case AAFileType.html:
                fileName = "file_html"
                break
            default:
                fileName = "file_unknown"
                break
            }
        }
        self.icon = UIImage.bundled(fileName)!
        
        super.init(height: 66, date: date, key: "document", layouter: layouter)
    }
    
    public convenience init(document: ACDocumentContent, date: Int64, layouter: AABubbleLayouter) {
        self.init(fileName: document.getName(), fileExt: document.getExt(), fileSize: Int(document.getSource().getSize()), fastThumb: document.getFastThumb(), date: date, autoDownload: false, layouter: layouter)
    }
    
    public convenience init(message: ACMessage, layouter: AABubbleLayouter) {
        self.init(document: message.content as! ACDocumentContent, date: Int64(message.date), layouter: layouter)
    }
}
