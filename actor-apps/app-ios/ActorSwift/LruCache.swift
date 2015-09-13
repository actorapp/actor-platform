//
// Copyright (c) 2014 Justin M Fischer
//
// Permission is hereby granted, free of charge, to any person obtaining a copy of
// this software and associated documentation files (the "Software"), to deal in
// the Software without restriction, including without limitation the rights to
// use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
// the Software, and to permit persons to whom the Software is furnished to do so,
// subject to the following conditions:
//
// The above copyright notice and this permission notice shall be included in all
// copies or substantial portions of the Software.
//
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
// FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
// COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
// IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
// CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
//
//  Created by JUSTIN M FISCHER on 12/1/14.
//  Copyright (c) 2013 Justin M Fischer. All rights reserved.
//

import Foundation

class Node<K, V> {
    var next: Node?
    var previous: Node?
    var key: K
    var value: V?
    
    init(key: K, value: V?) {
        self.key = key
        self.value = value
    }
}

class LinkedList<K, V> {
    
    var head: Node<K, V>?
    var tail: Node<K, V>?
    
    init() {
        
    }
    
    func addToHead(node: Node<K, V>) {
        if self.head == nil  {
            self.head = node
            self.tail = node
        } else {
            let temp = self.head
            
            self.head?.previous = node
            self.head = node
            self.head?.next = temp
        }
    }
    
    func remove(node: Node<K, V>) {
        if node === self.head {
            if self.head?.next != nil {
                self.head = self.head?.next
                self.head?.previous = nil
            } else {
                self.head = nil
                self.tail = nil
            }
        } else if node.next != nil {
            node.previous?.next = node.next
            node.next?.previous = node.previous
        } else {
            node.previous?.next = nil
            self.tail = node.previous
        }
    }
    
    func display() -> String {
        var description = ""
        var current = self.head
        
        while current != nil {
            description += "Key: \(current!.key) Value: \(current?.value) \n"
            
            current = current?.next
        }
        return description
    }
}


class SwiftlyLRU<K : Hashable, V> : CustomStringConvertible {
    
    let capacity: Int
    var length = 0
    
    private let queue: LinkedList<K, V>
    private var hashtable: [K : Node<K, V>]
    
    /**
    Least Recently Used "LRU" Cache, capacity is the number of elements to keep in the Cache.
    */
    init(capacity: Int) {
        self.capacity = capacity
        
        self.queue = LinkedList()
        self.hashtable = [K : Node<K, V>](minimumCapacity: self.capacity)
    }
    
    subscript (key: K) -> V? {
        get {
            if let node = self.hashtable[key] {
                self.queue.remove(node)
                self.queue.addToHead(node)
                
                return node.value
            } else {
                return nil
            }
        }
        
        set(value) {
            if let node = self.hashtable[key] {
                node.value = value
                
                self.queue.remove(node)
                self.queue.addToHead(node)
            } else {
                let node = Node(key: key, value: value)
                
                if self.length < capacity {
                    self.queue.addToHead(node)
                    self.hashtable[key] = node
                    
                    self.length++
                } else {
                    hashtable.removeValueForKey(self.queue.tail!.key)
                    self.queue.tail = self.queue.tail?.previous
                    
                    if let node = self.queue.tail {
                        node.next = nil
                    }
                    
                    self.queue.addToHead(node)
                    self.hashtable[key] = node
                }
            }
        }
    }
    
    var description : String {
        return "SwiftlyLRU Cache(\(self.length)) \n" + self.queue.display()
    }
}