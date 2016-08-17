//
//  Copyright (c) 2014-2016 Actor LLC. <https://actor.im>
//

import Foundation

@objc class FMDBKeyValue: NSObject, ARKeyValueStorage {

    let dbQueue: FMDatabaseQueue
    
    let tableName: String
    
    let queryCreate: String
    let queryItem: String
    let queryItems: String
    let queryAll: String
    let queryAdd: String
    let queryDelete: String
    let queryDeleteAll: String
    
    var isTableChecked: Bool = false
    
    init(dbQueue: FMDatabaseQueue, tableName: String) {
        self.dbQueue = dbQueue
        self.tableName = tableName
        
        // Queries
        self.queryCreate = "CREATE TABLE IF NOT EXISTS " + tableName + " (" +
            "\"ID\" INTEGER NOT NULL, " +
            "\"BYTES\" BLOB NOT NULL, " +
            "PRIMARY KEY (\"ID\"));"
        self.queryItem = "SELECT \"BYTES\" FROM " + tableName + " WHERE \"ID\" = ?;"
        self.queryItems = "SELECT \"ID\", \"BYTES\" FROM " + tableName + " WHERE \"ID\" in ?;"
        self.queryAll = "SELECT \"ID\", \"BYTES\" FROM " + tableName + ";"
        self.queryAdd = "REPLACE INTO " + tableName + " (\"ID\", \"BYTES\") VALUES (?, ?);"
        self.queryDelete = "DELETE FROM " + tableName + " WHERE \"ID\" = ?;"
        self.queryDeleteAll = "DELETE FROM " + tableName + ";"
        
        super.init()
    }
    
    private func checkTable() {
        if (isTableChecked) {
            return
        }
        isTableChecked = true
        
        dbQueue.inDatabase { (db) in
            if (!db.tableExists(self.tableName)) {
                db.executeUpdate(self.queryCreate)
            }
        }
    }
    
    func addOrUpdateItems(values: JavaUtilList!) {
        checkTable()
        
        dbQueue.inTransaction { (db, rollback) in
            for i in 0..<values.size() {
                let record = values.getWithInt(i) as! ARKeyValueRecord
                db.executeUpdate(self.queryAdd, record.getId().toNSNumber(),record.getData().toNSData())
            }
        }
    }
    
    func addOrUpdateItemWithKey(key: jlong, withData data: IOSByteArray!) {
        checkTable()
        
        dbQueue.inDatabase { (db) in
            db.executeUpdate(self.queryAdd, key.toNSNumber(), data!.toNSData())
        }
    }
    
    func removeItemsWithKeys(keys: IOSLongArray!) {
        checkTable()
        
        dbQueue.inTransaction { (db, rollback) in
            for i in 0..<keys.length() {
                let key = keys.longAtIndex(UInt(i));
                db.executeUpdate(self.queryDelete, key.toNSNumber())
            }
        }
    }
    
    func removeItemWithKey(key: jlong) {
        checkTable()
        
        dbQueue.inDatabase { (db) in
            db.executeUpdate(self.queryDelete, key.toNSNumber())
        }
    }
    
    func loadItemWithKey(key: jlong) -> IOSByteArray! {
        checkTable()
        
        var res: IOSByteArray! = nil
        dbQueue.inDatabase { (db) in
            let result = db.dataForQuery(self.queryItem, key.toNSNumber())
            if (result == nil) {
                return
            }
            res = result.toJavaBytes()
        }
        return res
    }
    
    func loadAllItems() -> JavaUtilList! {
        checkTable()
        
        let res = JavaUtilArrayList()
        dbQueue.inDatabase { (db) in
            if let result = db.executeQuery(self.queryAll) {
                while(result.next()) {
                    res.addWithId(ARKeyValueRecord(key: jlong(result.longLongIntForColumn("ID")), withData: result.dataForColumn("BYTES").toJavaBytes()))
                }
            }
        }
        return res
    }
    
    func loadItems(keys: IOSLongArray!) -> JavaUtilList! {
        checkTable()
        
        // Converting to NSNumbers
        var ids = [NSNumber]()
        for i in 0..<keys.length() {
            ids.append(keys.longAtIndex(UInt(i)).toNSNumber())
        }
        
        let res = JavaUtilArrayList()
        dbQueue.inDatabase { (db) in
            if let result = db.executeQuery(self.queryItems, ids) {
                while(result.next()) {
                    // TODO: Optimize lookup
                    res.addWithId(ARKeyValueRecord(key: jlong(result.longLongIntForColumn("ID")), withData: result.dataForColumn("BYTES").toJavaBytes()))
                }
            }
        }
        return res
    }
    
    func clear() {
        checkTable()
        
        dbQueue.inTransaction { (db, rollout) in
            db.executeUpdate(self.queryDeleteAll)
        }
    }
}