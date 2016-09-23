//
//  Copyright (c) 2014-2016 Actor LLC. <https://actor.im>
//

import UIKit
import VBFPopFlatButton
import YYImage

open class AABubbleVoiceCell: AABubbleBaseFileCell,AAModernConversationAudioPlayerDelegate,AAModernViewInlineMediaContextDelegate {

    // Views
    
    fileprivate let progress = AAProgressView(size: CGSize(width: 44, height: 44))
    fileprivate let timeLabel = AttributedLabel()
    fileprivate let voiceTimeLabel = AttributedLabel()
    fileprivate let playPauseButton = UIButton()
    fileprivate let soundProgressSlider = UISlider()
    fileprivate let statusView = UIImageView()
    

    fileprivate let durationLabel = AttributedLabel()
    fileprivate let titleLabel = AttributedLabel()
    
    // Binded data
    
    var bindedLayout: VoiceMessageCellLayout!
    var thumbLoaded = false
    var contentLoaded = false
    
    // Constructors
    
    public init(frame: CGRect) {
        
        super.init(frame: frame, isFullSize: false)
        
        ////////////////////////////////////////////////////
        
        timeLabel.font = UIFont.italicSystemFont(ofSize: 11)
        timeLabel.textColor = appStyle.chatTextDateOutColor
        
        ////////////////////////////////////////////////////
        
        statusView.contentMode = UIViewContentMode.center
        
        ////////////////////////////////////////////////////
        
        soundProgressSlider.tintColor = appStyle.chatStatusSending
        
        soundProgressSlider.isUserInteractionEnabled = false
        
        //soundProgressSlider.addTarget(self, action: "seekToNewAudioValue", forControlEvents: UIControlEvents.ValueChanged)
        
        let insets = UIEdgeInsets(top: 0, left: 0, bottom: 0, right: 0)
        
        let trackLeftImage = UIImage.tinted("aa_voiceplaybackground", color: UIColor(red: 0.0, green: 0.761, blue: 0.9964, alpha: 1.0 ))
        let trackLeftResizable = trackLeftImage.resizableImage(withCapInsets: insets)
        soundProgressSlider.setMinimumTrackImage(trackLeftResizable, for: UIControlState())
        
        let trackRightImage = UIImage.tinted("aa_voiceplaybackground", color: UIColor(red: 0.0, green: 0.5856, blue: 0.9985, alpha: 1.0 ))
        let trackRightResizable = trackRightImage.resizableImage(withCapInsets: insets)
        soundProgressSlider.setMaximumTrackImage(trackRightResizable, for: UIControlState())
        
        let thumbImageNormal = UIImage.bundled("aa_thumbvoiceslider")
        soundProgressSlider.setThumbImage(thumbImageNormal, for: UIControlState())
        
        ////////////////////////////////////////////////////
        
        voiceTimeLabel.font = UIFont.italicSystemFont(ofSize: 11)
        voiceTimeLabel.textColor = appStyle.chatTextDateOutColor
        
        ////////////////////////////////////////////////////

        durationLabel.font = UIFont.systemFont(ofSize: 11.0)
        durationLabel.textColor = appStyle.chatTextDateOutColor
        durationLabel.text = " "
        durationLabel.sizeToFit()
        
        ////////////////////////////////////////////////////
        
        durationLabel.layer.drawsAsynchronously         = true
        progress.layer.drawsAsynchronously              = true
        playPauseButton.layer.drawsAsynchronously       = true
        soundProgressSlider.layer.drawsAsynchronously   = true
        voiceTimeLabel.layer.drawsAsynchronously        = true
        timeLabel.layer.drawsAsynchronously             = true
        statusView.layer.drawsAsynchronously            = true
        
        ////////////////////////////////////////////////////
        
        contentView.addSubview(durationLabel)
        contentView.addSubview(progress)
        contentView.addSubview(playPauseButton)
        contentView.addSubview(soundProgressSlider)
        contentView.addSubview(voiceTimeLabel)
        contentView.addSubview(timeLabel)
        contentView.addSubview(statusView)
        
        ////////////////////////////////////////////////////
        
        playPauseButton.setImage(UIImage.bundled("aa_playrecordbutton"), for: UIControlState())
        playPauseButton.addTarget(self, action: #selector(AABubbleVoiceCell.mediaDidTap), for: UIControlEvents.touchUpInside)
        
        contentInsets = UIEdgeInsets(top: 1, left: 1, bottom: 1, right: 1)
        
    }
    
    public required init(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    // MARK: - Binding
    
    open override func bind(_ message: ACMessage, receiveDate: jlong, readDate: jlong, reuse: Bool, cellLayout: AACellLayout, setting: AACellSetting) {
        self.bindedLayout = cellLayout as! VoiceMessageCellLayout
        
        let document = message.content as! ACVoiceContent
        
        bubbleInsets = UIEdgeInsets(
            top: setting.clenchTop ? AABubbleCell.bubbleTopCompact : AABubbleCell.bubbleTop,
            left: 10 + (AADevice.isiPad ? 16 : 0),
            bottom: setting.clenchBottom ? AABubbleCell.bubbleBottomCompact : AABubbleCell.bubbleBottom,
            right: 10 + (AADevice.isiPad ? 16 : 0))
        
        
        if (!reuse) {
            
            // Bind bubble
            if (self.isOut) {
                bindBubbleType(BubbleType.mediaOut, isCompact: false)
            } else {
                bindBubbleType(BubbleType.mediaIn, isCompact: false)
            }
            
            titleLabel.text = AALocalized("ChatVoiceMessage")
            durationLabel.text = bindedLayout.voiceDuration
            
            // Reset progress
            self.progress.hideButton()
            UIView.animate(withDuration: 0, animations: { () -> Void in
                self.progress.isHidden = true
            })
            
            // Bind file
            let autoDownload: Bool
            if self.peer.isGroup {
                autoDownload = ActorSDK.sharedActor().isAudioAutoDownloadGroup
            } else if self.peer.isPrivate {
                autoDownload = ActorSDK.sharedActor().isAudioAutoDownloadPrivate
            } else {
                autoDownload = false
            }
            fileBind(message, autoDownload: autoDownload)
        }
        
        dispatchOnUi { () -> Void in
            
            //let content = bindedMessage!.content as! ACDocumentContent
            
            let content = self.bindedMessage!.content as! ACDocumentContent
            if let fileSource = content.getSource() as? ACFileRemoteSource {
                let fileID = fileSource.getFileReference().getFileId()
                
                if self.controller.currentAudioFileId != fileID {
                    self.soundProgressSlider.value = 0.0
                    self.playPauseButton.setImage(UIImage.bundled("aa_playrecordbutton"), for: UIControlState())
                    self.controller.voiceContext?.removeDelegate(self)
                } else {
                    self.controller.voiceContext?.delegate = self
                    self.controller.voicePlayer?.delegate = self
                    
                    if self.controller.voicePlayer?.isPaused() == false {
                        self.playPauseButton.setImage(UIImage.bundled("aa_pauserecordbutton"), for: UIControlState())
                    } else {
                        self.playPauseButton.setImage(UIImage.bundled("aa_playrecordbutton"), for: UIControlState())
                    }
                    
                }
                
                if let progressVoice = self.controller.voicesCache[fileID] {
                    if progressVoice > 0.0 {
                        self.soundProgressSlider.value = progressVoice
                    }
                }
                
            }
            
        }
        
        
        // Update time
        timeLabel.text = cellLayout.date
        
        // Update status
        if (isOut) {
            statusView.isHidden = false
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
        } else {
            statusView.isHidden = true
        }
    }
    
    
    //MARK: - File state binding
    
    open override func fileStateChanged(_ reference: String?, progress: Int?, isPaused: Bool, isUploading: Bool, selfGeneration: Int) {
        runOnUiThread(selfGeneration) { () -> () in
            if isUploading {
                if isPaused {
                    self.playPauseButton.hideView()
                    
                    self.progress.showView()
                    self.progress.setButtonType(FlatButtonType.buttonUpBasicType, animated: true)
                    self.progress.hideProgress()
                } else {
                    self.playPauseButton.hideView()
                    
                    self.progress.showView()
                    self.progress.setButtonType(FlatButtonType.buttonPausedType, animated: true)
                    self.progress.setProgress(Double(progress!)/100.0)
                }
            } else {
                if (reference != nil) {
                    self.playPauseButton.showView()
                    
                    self.progress.setProgress(1)
                    self.progress.hideView()
                } else if isPaused {
                    self.playPauseButton.hideView()
                    
                    self.progress.showView()
                    self.progress.setButtonType(FlatButtonType.buttonDownloadType, animated: true)
                    self.progress.hideProgress()
                } else {
                    self.playPauseButton.hideView()
                    
                    self.progress.showView()
                    self.progress.setButtonType(FlatButtonType.buttonPausedType, animated: true)
                    self.progress.setProgress(Double(progress!)/100.0)
                }
            }
        }
    } 
    
    //MARK: - Media Action
    
    open func mediaDidTap() {
        let content = bindedMessage!.content as! ACDocumentContent
        if let fileSource = content.getSource() as? ACFileRemoteSource {
            Actor.requestState(withFileId: fileSource.getFileReference().getFileId(), with: AAFileCallback(
                notDownloaded: { () -> () in
                    Actor.startDownloading(with: fileSource.getFileReference())
                }, onDownloading: { (progress) -> () in
                    Actor.cancelDownloading(withFileId: fileSource.getFileReference().getFileId())
                }, onDownloaded: { (reference) -> () in
                    
                    dispatchOnUi({ () -> Void in
                        
                        let path = CocoaFiles.pathFromDescriptor(reference)
                        
                        let fileID = fileSource.getFileReference().getFileId()
                        
                        self.controller.playVoiceFromPath(path,fileId: fileID,position:self.soundProgressSlider.value)
                        
                        self.playPauseButton.setImage(UIImage.bundled("aa_pauserecordbutton"), for: UIControlState())
                        
                        self.controller.voicePlayer.delegate = self
                        self.controller.voiceContext.delegate = self
                        
                    })

            }))
            
        } else if let fileSource = content.getSource() as? ACFileLocalSource {
            let rid = bindedMessage!.rid
            Actor.requestUploadState(withRid: rid, with: AAUploadFileCallback(
                notUploaded: { () -> () in
                    Actor.resumeUpload(withRid: rid)
                }, onUploading: { (progress) -> () in
                    Actor.pauseUpload(withRid: rid)
                }, onUploadedClosure: { () -> () in
                    
                    dispatchOnUi({ () -> Void in
                        
                        let path = CocoaFiles.pathFromDescriptor(fileSource.getFileDescriptor())
                        
                        let content = self.bindedMessage!.content as! ACDocumentContent
                        if let fileSource = content.getSource() as? ACFileRemoteSource {
                            let fileID = fileSource.getFileReference().getFileId()
                            
                            self.controller.playVoiceFromPath(path,fileId: fileID,position:self.soundProgressSlider.value)
                            
                            self.playPauseButton.setImage(UIImage.bundled("aa_pauserecordbutton"), for: UIControlState())
                            
                            self.controller.voicePlayer.delegate = self
                            self.controller.voiceContext.delegate = self
                        }
                        

                    })
                    
                    
            }))
            
        }
    }
    
    open func seekToNewAudioValue() {
        
    }
    
    open func audioPlayerDidFinish() {
        
        dispatchOnUi { () -> Void in
            
            self.playPauseButton.setImage(UIImage.bundled("aa_playrecordbutton"), for: UIControlState())
            self.soundProgressSlider.value = 0.0
            self.controller.voicesCache[self.controller.currentAudioFileId] = 0.0
            
        }
        
    }
    
    open func inlineMediaPlaybackStateUpdated(_ isPaused: Bool, playbackPosition: Float, timestamp: TimeInterval, preciseDuration: TimeInterval) {
        
        dispatchOnUi({ () -> Void in
        
            self.soundProgressSlider.value = playbackPosition
            
            if (isPaused == true) {
                self.playPauseButton.setImage(UIImage.bundled("aa_playrecordbutton"), for: UIControlState())
            }
            
            self.controller.voicesCache[self.controller.currentAudioFileId] = playbackPosition
        
            
        })
        
    }
    
    //MARK: - Layouting
    
    open override func layoutContent(_ maxWidth: CGFloat, offsetX: CGFloat) {
        let insets = fullContentInsets
        let contentWidth = self.contentView.frame.width
        
        layoutBubble(200, contentHeight: 55)
        
        let contentLeft = self.isOut ? contentWidth - 200 - insets.right - contentInsets.left : insets.left
        
        let top = insets.top - 2
        
        // Progress state
        let progressRect = CGRect(x: contentLeft + 7.5, y: 7.5 + top, width: 44, height: 44)
        self.progress.frame = progressRect
        self.playPauseButton.frame = progressRect
        
        timeLabel.frame = CGRect(x: 0, y: 0, width: 1000, height: 1000)
        timeLabel.sizeToFit()
        
        // Content
        self.soundProgressSlider.frame = CGRect(x: contentLeft + 62, y: 16 + top, width: 200 - 70, height: 22)
        self.durationLabel.frame = CGRect(x: contentLeft + 62, y: 10 + 25 + top, width: 200 - 64, height: 22)
        
        // Message state
        if (self.isOut) {
            self.timeLabel.frame = CGRect(x: self.bubble.frame.maxX - 55 - self.bubblePadding, y: self.bubble.frame.maxY - 24, width: 46, height: 26)
            self.statusView.frame = CGRect(x: self.bubble.frame.maxX - 24 - self.bubblePadding, y: self.bubble.frame.maxY - 24, width: 20, height: 26)
            self.statusView.isHidden = false
        } else {
            self.timeLabel.frame = CGRect(x: self.bubble.frame.maxX - 47 - self.bubblePadding, y: self.bubble.frame.maxY - 24, width: 46, height: 26)
            self.statusView.isHidden = true
        }
        
    }
    
}

/**
 Voice cell layout
 */
open class VoiceMessageCellLayout: AACellLayout {
    
    open let contentSize: CGSize
    open let screenSize: CGSize
    open let autoDownload: Bool
    
    open let fileName: String
    open let fileExt: String
    open let fileSize: String
    open var voiceDuration: String!
    
    /**
     Creting layout for media bubble
     */
    public init(fileName: String, fileExt: String, fileSize: Int,id: Int64, date: Int64, autoDownload: Bool,duration:jint, layouter: AABubbleLayouter) {
        
        // Saving content size
        self.contentSize = CGSize(width: 200, height: 55)
        
        // Saving autodownload flag
        self.autoDownload = autoDownload
        
        // Calculating bubble screen size
        self.screenSize = CGSize(width: 200, height: 55)
        
        self.fileName = fileName
        self.fileExt = fileExt.lowercased()
        self.fileSize = Actor.getFormatter().formatFileSize(jint(fileSize))
        
        // Creating layout
        super.init(height: self.screenSize.height + 2, date: date, key: "voice", layouter: layouter)
        
        self.voiceDuration = getTimeString(Int(duration))
    }
    
    func getTimeString(_ totalSeconds:Int) -> String {
        
        let seconds = Int(totalSeconds % 60)
        let minutes = Int((totalSeconds / 60) % 60)
        
        if minutes < 10 {
            if seconds < 10 {
                return "0\(minutes):0\(seconds)"
            } else {
                return "0\(minutes):\(seconds)"
            }
        } else {
            if seconds < 10 {
                return "\(minutes):0\(seconds)"
            } else {
                return "\(minutes):\(seconds)"
            }
        }
        
    }
    
    /**
     Creating layout for voice content
     */
    
    public convenience init(id: Int64, voiceContent: ACVoiceContent, date: Int64, layouter: AABubbleLayouter) {
        self.init(fileName: voiceContent.getName(), fileExt: voiceContent.getExt(), fileSize: Int(voiceContent.getSource().getSize()),id: id, date: date, autoDownload: true,duration:jint(voiceContent.getDuration()/1000), layouter: layouter)
    }
    
    
    /**
     Creating layout for message
     */
    public convenience init(message: ACMessage, layouter: AABubbleLayouter) {
        if let content = message.content as? ACVoiceContent {
            self.init(id: Int64(message.rid), voiceContent: content, date: Int64(message.date), layouter: layouter)
        } else {
            fatalError("Unsupported content for media cell")
        }
    }
}

/**
 Layouter for voice bubbles
 */
open class AABubbleVoiceCellLayouter: AABubbleLayouter {
    
    open func isSuitable(_ message: ACMessage) -> Bool {
        if message.content is ACVoiceContent {
            return true
        }
        
        return false
    }
    
    open func buildLayout(_ peer: ACPeer, message: ACMessage) -> AACellLayout {
        return VoiceMessageCellLayout(message: message, layouter: self)
    }
    
    open func cellClass() -> AnyClass {
        return AABubbleVoiceCell.self
    }
}
