//
//  Copyright (c) 2014-2016 Actor LLC. <https://actor.im>
//

import Foundation
import MapKit

private let mapWidth: CGFloat = 200
private let mapHeight: CGFloat = 160

open class AABubbleLocationCell: AABubbleCell {
    
    fileprivate let map = AAMapFastView(mapWidth: mapWidth, mapHeight: mapHeight)
    
    fileprivate let pin = UIImageView()
    fileprivate let timeBg = UIImageView()
    fileprivate let timeLabel = UILabel()
    fileprivate let statusView = UIImageView()
    
    fileprivate var bindedLat: Double? = nil
    fileprivate var bindedLon: Double? = nil
    
    public init(frame: CGRect) {
        super.init(frame: frame, isFullSize: false)
        
        timeBg.image = ActorSDK.sharedActor().style.statusBackgroundImage
        
        timeLabel.font = UIFont.italicSystemFont(ofSize: 11)
        timeLabel.textColor = appStyle.chatMediaDateColor
        
        statusView.contentMode = UIViewContentMode.center

        pin.image = UIImage.bundled("LocationPin")
        
        contentView.addSubview(map)
        map.addSubview(pin)
        
        contentView.addSubview(timeBg)
        contentView.addSubview(timeLabel)
        contentView.addSubview(statusView)
        
        contentInsets = UIEdgeInsets(top: 1, left: 1, bottom: 1, right: 1)

        map.addGestureRecognizer(UITapGestureRecognizer(target: self, action: #selector(AABubbleLocationCell.mapDidTap)))
        map.isUserInteractionEnabled = true
    }

    public required init(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    func mapDidTap() {
        let url = "http://maps.apple.com/?q=\(bindedLat!),\(bindedLon!)"
        // print("url: \(url)")
        UIApplication.shared.openURL(URL(string: url)!)
    }
    
    open override func bind(_ message: ACMessage, receiveDate: jlong, readDate: jlong, reuse: Bool, cellLayout: AACellLayout, setting: AACellSetting) {
        
        let layout = cellLayout as! AALocationCellLayout
        
        bindedLat = layout.latitude
        bindedLon = layout.longitude
        
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
        }
        
        map.bind(layout.latitude, longitude: layout.longitude)
        
        // Update time
        timeLabel.text = cellLayout.date
        
        // Update status
        if (isOut) {
            statusView.isHidden = false
            switch(message.messageState.toNSEnum()) {
            case .SENT:
                if message.sortDate <= readDate {
                    self.statusView.image = appStyle.chatIconCheck2
                    self.statusView.tintColor = appStyle.chatStatusMediaRead
                } else if message.sortDate <= receiveDate {
                    self.statusView.image = appStyle.chatIconCheck2
                    self.statusView.tintColor = appStyle.chatStatusMediaReceived
                } else {
                    self.statusView.image = appStyle.chatIconCheck1
                    self.statusView.tintColor = appStyle.chatStatusMediaSent
                }
            case .ERROR:
                self.statusView.image = appStyle.chatIconError
                self.statusView.tintColor = appStyle.chatStatusMediaError
                break
            case .PENDING:
                self.statusView.image = appStyle.chatIconClock
                self.statusView.tintColor = appStyle.chatStatusMediaSending
                break
            default:
                self.statusView.image = appStyle.chatIconClock
                self.statusView.tintColor = appStyle.chatStatusMediaSending
                break
            }
        } else {
            statusView.isHidden = true
        }
    }
    
    open override func layoutContent(_ maxWidth: CGFloat, offsetX: CGFloat) {
        let insets = fullContentInsets
        let contentWidth = self.contentView.frame.width
        
        layoutBubble(mapWidth, contentHeight: mapHeight)
        
        if isOut {
            map.frame = CGRect(x: contentWidth - insets.right - mapWidth , y: insets.top, width: mapWidth, height: mapHeight)
        } else {
            map.frame = CGRect(x: insets.left, y: insets.top, width: mapWidth, height: mapHeight)
        }
        
        timeLabel.frame = CGRect(x: 0, y: 0, width: 1000, height: 1000)
        timeLabel.sizeToFit()
        
        let timeWidth = (isOut ? 23 : 0) + timeLabel.bounds.width
        let timeHeight: CGFloat = 20
        
        timeLabel.frame = CGRect(x: map.frame.maxX - timeWidth - 18, y: map.frame.maxY - timeHeight - 6, width: timeLabel.frame.width, height: timeHeight)
        
        if (isOut) {
            statusView.frame = CGRect(x: timeLabel.frame.maxX, y: timeLabel.frame.minY, width: 23, height: timeHeight)
        }
        
        pin.frame = CGRect(x: (map.width - pin.image!.size.width)/2, y: (map.height / 2 - pin.image!.size.height),
            width: pin.image!.size.width, height: pin.image!.size.height)
        
        timeBg.frame = CGRect(x: timeLabel.frame.minX - 4, y: timeLabel.frame.minY - 1, width: timeWidth + 8, height: timeHeight + 2)
        
    }
}

open class AALocationCellLayout: AACellLayout {
    
    let latitude: Double
    let longitude: Double
    
    init(latitude: Double, longitude: Double, date: Int64, layouter: AABubbleLayouter) {
        self.latitude = latitude
        self.longitude = longitude
        super.init(height: mapHeight + 2, date: date, key: "location", layouter: layouter)
    }
}

open class AABubbleLocationCellLayouter: AABubbleLayouter {
    
    open func isSuitable(_ message: ACMessage) -> Bool {
        if (message.content is ACLocationContent) {
            return true
        }
        return false
    }
    
    open func buildLayout(_ peer: ACPeer, message: ACMessage) -> AACellLayout {
        let content = message.content as! ACLocationContent
        return AALocationCellLayout(latitude: Double(content.getLatitude()), longitude: Double(content.getLongitude()), date: Int64(message.date), layouter: self)
    }
    
    open func cellClass() -> AnyClass {
        return AABubbleLocationCell.self
    }
}
