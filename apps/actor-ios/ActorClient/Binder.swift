//
//  Binder.swift
//  ActorClient
//
//  Created by Stepan Korshakov on 12.03.15.
//  Copyright (c) 2015 Actor LLC. All rights reserved.
//

import Foundation

class Binder {
    
    var bindings : [BindHolder] = [];
    
    func bind<T1,T2,T3>(valueModel1:AMValueModel, valueModel2:AMValueModel, valueModel3:AMValueModel, closure: (value1:T1?, value2:T2?, value3:T3?) -> ()) {
        
        var listener1 = BindListener { (_value1) -> () in
            closure(value1: _value1 as? T1, value2: valueModel2.get() as? T2, value3: valueModel2.get() as? T3)
        };
        var listener2 = BindListener { (_value2) -> () in
            closure(value1: valueModel1.get() as? T1, value2: _value2 as? T2, value3: valueModel2.get() as? T3)
        };
        var listener3 = BindListener { (_value3) -> () in
            closure(value1: valueModel1.get() as? T1,  value2: valueModel2.get() as? T2, value3: _value3 as? T3)
        };
        bindings.append(BindHolder(valueModel: valueModel1, listener: listener1))
        bindings.append(BindHolder(valueModel: valueModel2, listener: listener2))
        bindings.append(BindHolder(valueModel: valueModel3, listener: listener3))
        valueModel1.subscribeWithAMValueChangedListener(listener1, withBoolean: false)
        valueModel2.subscribeWithAMValueChangedListener(listener2, withBoolean: false)
        valueModel3.subscribeWithAMValueChangedListener(listener3, withBoolean: false)
        closure(value1: valueModel1.get() as? T1, value2: valueModel2.get() as? T2, value3: valueModel3.get() as? T3)
    }

    
    func bind<T1,T2>(valueModel1:AMValueModel, valueModel2:AMValueModel, closure: (value1:T1?, value2:T2?) -> ()) {
        var listener1 = BindListener { (_value1) -> () in
            closure(value1: _value1 as? T1, value2: valueModel2.get() as? T2)
        };
        var listener2 = BindListener { (_value2) -> () in
            closure(value1: valueModel1.get() as? T1, value2: _value2 as? T2)
        };
        bindings.append(BindHolder(valueModel: valueModel1, listener: listener1))
        bindings.append(BindHolder(valueModel: valueModel2, listener: listener2))
        valueModel1.subscribeWithAMValueChangedListener(listener1, withBoolean: false)
        valueModel2.subscribeWithAMValueChangedListener(listener2, withBoolean: false)
        closure(value1: valueModel1.get() as? T1, value2: valueModel2.get() as? T2)
    }
    
    func bind<T>(value:AMValueModel, closure: (value: T?)->()) {
        var listener = BindListener { (value2) -> () in
            closure(value: value2 as? T);
        };
        var holder = BindHolder(valueModel: value, listener: listener);
        bindings.append(holder);
        value.subscribeWithAMValueChangedListener(listener);
    }
    
    func unbindAll() {
        for holder in bindings {
            holder.valueModel.unsubscribeWithAMValueChangedListener(holder.listener);
        }
        bindings.removeAll(keepCapacity: true);
    }
    
}

class BindListener: NSObject, JavaObject, AMValueChangedListener {
    
    var closure: (value: AnyObject?)->();
    
    init(closure: (value: AnyObject?)->()){
        self.closure = closure;
    }
    
    @objc func onChangedWithId(val: AnyObject!, withAMValueModel valueModel: AMValueModel!) {
        closure(value: val);
    }
}

class BindHolder {
    var listener: BindListener;
    var valueModel: AMValueModel;
    
    init(valueModel: AMValueModel, listener: BindListener) {
        self.valueModel = valueModel;
        self.listener = listener;
    }
}