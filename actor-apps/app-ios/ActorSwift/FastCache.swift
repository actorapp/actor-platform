//
//  Copyright (c) 2014-2015 Actor LLC. <https://actor.im>
//

import Foundation

struct HashMap<T> {
    var table = Array<SinglyLinkedList<T>?>()
    init() {
        for _ in 0...99 {
            table.append(SinglyLinkedList<T>())
        }
    }
    
    mutating func setKey(key: Int64, withValue val: T?) {
        let hashedString = Int(abs(key) % 10)
        if let collisionList = table[hashedString] {
            collisionList.upsertNodeWithKey(key, AndValue: val)
        } else {
            table[hashedString] = SinglyLinkedList<T>()
            table[hashedString]!.upsertNodeWithKey(key, AndValue: val)
        }
    }
    func getValueAtKey(key: Int64) -> T? {
        let hashedString = Int(abs(key) % 10)
        if let collisionList = table[hashedString] {
            return collisionList.findNodeWithKey(key)?.value
        } else {
            return nil
        }
    }
}

struct SinglyLinkedList<T> {
    var head = CCHeadNode<CCSinglyNode<T>>()
    func findNodeWithKey(key: Int64) -> CCSinglyNode<T>? {
        if var currentNode = head.next {
            while currentNode.key != key {
                if let nextNode = currentNode.next {
                    currentNode = nextNode
                } else {
                    return nil
                }
            }
            return currentNode
        } else {
            return nil
        }
    }
    func upsertNodeWithKey(key: Int64, AndValue val: T?) -> CCSinglyNode<T> {
        if var currentNode = head.next {
            while let nextNode = currentNode.next {
                if currentNode.key == key {
                    break
                } else {
                    currentNode = nextNode
                }
            }
            if currentNode.key == key {
                currentNode.value = val
                return currentNode
            } else {
                currentNode.next = CCSinglyNode<T>(key: key, value: val, nextNode: nil)
                return currentNode.next!
            }
        } else {
            head.next = CCSinglyNode<T>(key: key, value: val, nextNode: nil)
            return head.next!
        }
    }
    func displayNodes() {
        print("Printing Nodes")
        if var currentNode = head.next {
            print("First Node's Value is \(currentNode.value!)")
            while let nextNode = currentNode.next {
                currentNode = nextNode
                print("Next Node's Value is \(currentNode.value!)")
            }
        } else {
            print("List is empty")
        }
    }
}

class CCNode<T> {
    var value: T?
    init(value: T?) {
        self.value = value
    }
}
class CCHeadNode<T> {
    var next: T?
}
class CCSinglyNode<T>: CCNode<T> {
    var key: Int64
    var next: CCSinglyNode<T>?
    init(key: Int64, value: T?, nextNode: CCSinglyNode<T>?) {
        self.next = nextNode
        self.key = key
        super.init(value: value)
    }
}
