//
//  Copyright (c) 2014-2016 Actor LLC. <https://actor.im>
//

import UIKit

class AAVoiceRecorderView: UIView {
    
    //////////////////////////////////

    var timeLabel:UILabel!
    
    var sliderLabel: UILabel!
    var sliderArrow: UIImageView!
    
    var recorderImageCircle:UIImageView!
    
    //////////////////////////////////
    
    var trackTouchPoint: CGPoint!
    var firstTouchPoint: CGPoint!
    
    //////////////////////////////////
    
    weak var binedController : ConversationViewController!
    var meterTimer:Timer!
    
    //////////////////////////////////
    
    var appStyle: ActorStyle { get { return ActorSDK.sharedActor().style } }
    
    override init(frame: CGRect) {
        super.init(frame: frame)
        
        self.createSubviews()
        
    }

    required init?(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    func createSubviews() {
        //////////////////////////////////
        //          init
        //////////////////////////////////
        
        self.timeLabel = UILabel()
        
        self.sliderLabel = UILabel()
        self.sliderArrow = UIImageView()
        
        self.recorderImageCircle = UIImageView()
        
        //////////////////////////////////
        //      add as subviews
        //////////////////////////////////
        
        self.addSubview(self.timeLabel)
        self.addSubview(self.sliderLabel)
        self.addSubview(self.sliderArrow)
        self.addSubview(self.recorderImageCircle)
        
        //////////////////////////////////
        //        customize
        //////////////////////////////////
        
        self.backgroundColor = appStyle.chatInputFieldBgColor
        
        self.timeLabel.text = "0:00"
        self.timeLabel.font = UIFont.systemFont(ofSize: 15)
        self.timeLabel.textColor = appStyle.vcHintColor
        self.timeLabel.frame = CGRect(x: 29, y: 12, width: 50, height: 20)
        
        self.sliderLabel.text = "Slide to cancel"
        self.sliderLabel.font = UIFont.systemFont(ofSize: 14)
        self.sliderLabel.textAlignment = .left
        self.sliderLabel.frame = CGRect(x: 140,y: 12,width: 100,height: 20)
        self.sliderLabel.textColor = appStyle.vcHintColor
        
        self.sliderArrow.image = UIImage.tinted("aa_recorderarrow", color: appStyle.vcHintColor)
        self.sliderArrow.frame = CGRect(x: 110,y: 12,width: 20,height: 20)
        
        self.recorderImageCircle.image = UIImage.tinted("aa_recordercircle", color: UIColor(red: 0.7287, green: 0.7252, blue: 0.7322, alpha: 1.0))
        self.recorderImageCircle.frame = CGRect(x: 10, y: 15, width: 14, height: 14)
        
        //
        
    }
    
    // update location views from track touch position
    func updateLocation(_ offset:CGFloat,slideToRight:Bool) {
        
        var sliderLabelFrame = self.sliderLabel.frame
        sliderLabelFrame.origin.x += offset
        self.sliderLabel.frame = sliderLabelFrame
        
        var sliderArrowImageFrame = self.sliderArrow.frame
        sliderArrowImageFrame.origin.x += offset
        self.sliderArrow.frame = sliderArrowImageFrame
        
        if (slideToRight == true) {
            
            if (self.timeLabel.frame.minX < 29) {
                
                var timeLabelFrame = self.timeLabel.frame
                timeLabelFrame.origin.x += offset
                self.timeLabel.frame = timeLabelFrame
                
                var circleFrame = self.recorderImageCircle.frame
                circleFrame.origin.x += offset
                self.recorderImageCircle.frame = circleFrame
                
            }
            
        } else {
            
            if (self.timeLabel.frame.maxX-5 > sliderArrowImageFrame.minX) {
                
                var timeLabelFrame = self.timeLabel.frame
                timeLabelFrame.origin.x += offset
                self.timeLabel.frame = timeLabelFrame
                
                var circleFrame = self.recorderImageCircle.frame
                circleFrame.origin.x += offset
                self.recorderImageCircle.frame = circleFrame
                
            }
            
        }
    
    }
    
    func startAnimation() {
        
        self.timeLabel.frame = CGRect(x: -129, y: 12, width: 50, height: 20)
        self.sliderLabel.frame = CGRect(x: 440,y: 12,width: 100,height: 20)
        self.sliderArrow.frame = CGRect(x: 310,y: 12,width: 20,height: 20)
        self.recorderImageCircle.frame = CGRect(x: -110, y: 15, width: 14, height: 14)
        
        UIView.animate(withDuration: 0.5, delay: 0.0, usingSpringWithDamping: 0.5, initialSpringVelocity: 1.0, options: UIViewAnimationOptions.curveLinear, animations: { () -> Void in
            
            self.timeLabel.frame = CGRect(x: 29, y: 12, width: 50, height: 20)
            self.sliderLabel.frame = CGRect(x: 140,y: 12,width: 100,height: 20)
            self.sliderArrow.frame = CGRect(x: 110,y: 12,width: 20,height: 20)
            self.recorderImageCircle.frame = CGRect(x: 10, y: 15, width: 14, height: 14)
            
            }, completion: { (complite) -> Void in
                
                // animation complite
                
        })
        
    }
    
    func recordingStarted() {
        
        UIView.animate(withDuration: 0.3, animations: { () -> Void in
            
                self.recorderImageCircle.alpha = 0
            
            }, completion: { (comp) -> Void in
                
                self.recorderImageCircle.image = UIImage.bundled("aa_recordercircle")
                
                UIView.animate(withDuration: 0.3, animations: { () -> Void in
                    self.recorderImageCircle.alpha = 1
                })
                
                self.addAnimationsOnRecorderCircle()
                self.startUpdateTimer()
                
        }) 
        
    }
    
    func recordingStoped() {
        meterTimer?.invalidate()
        self.timeLabel.text = "0:00"
        self.recorderImageCircle.layer.removeAllAnimations()
        self.recorderImageCircle.image = UIImage.tinted("aa_recordercircle", color: UIColor(red: 0.7287, green: 0.7252, blue: 0.7322, alpha: 1.0))
        
        self.timeLabel.frame = CGRect(x: 29, y: 12, width: 50, height: 20)
        self.sliderLabel.frame = CGRect(x: 140,y: 12,width: 100,height: 20)
        self.sliderArrow.frame = CGRect(x: 110,y: 12,width: 20,height: 20)
        self.recorderImageCircle.frame = CGRect(x: 10, y: 15, width: 14, height: 14)
    }
    
    
    func startUpdateTimer() {
        self.meterTimer = Timer.scheduledTimer(timeInterval: 0.1,
            target:self,
            selector:#selector(AAVoiceRecorderView.updateAudioMeter(_:)),
            userInfo:nil,
            repeats:true)
    }
    
    func updateAudioMeter(_ timer:Timer) {
        
        if let recorder = self.binedController?.audioRecorder {
            
            let dur = recorder.currentDuration()
            
            let minutes = Int(dur / 60)
            let seconds = Int(dur.truncatingRemainder(dividingBy: 60))
            
            if seconds < 10 {
                self.timeLabel.text = "\(minutes):0\(seconds)"
            } else {
                self.timeLabel.text = "\(minutes):\(seconds)"
            }
            
        }
    }
    
    
    func addAnimationsOnRecorderCircle() {
        
        let circleAnimation = CABasicAnimation(keyPath: "opacity")
        circleAnimation.repeatCount = 100000000
        circleAnimation.duration = 1.0
        circleAnimation.autoreverses = true
        circleAnimation.fromValue = 1.0
        circleAnimation.toValue = 0.1
        self.recorderImageCircle.layer.add(circleAnimation, forKey: nil)
        
    }
    
    func closeRecording() {
        
    }
    

}
