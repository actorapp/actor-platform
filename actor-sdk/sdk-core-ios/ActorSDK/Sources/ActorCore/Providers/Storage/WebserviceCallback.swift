//
//  WebserviceCallback.swift
//  ActorSDK
//
//  Created by zhangshanbo on 16/5/3.
//  Copyright © 2016年 Steve Kite. All rights reserved.
//

import Foundation
import SwiftyJSON

public protocol WebserviceCallback
{
    func onServiceSuccess(result:JSON)
    
    func onServiceFail(result:JSON)
    
    func onServiceError(result:String)
    
    func onNetworkProblem()
    
    
}
