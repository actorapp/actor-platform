//
//  Copyright (c) 2014-2016 Actor LLC. <https://actor.im>
//

import Foundation

@objc class FMDBKeyValue: NSObject, ARKeyValueStorage {

    var db :FMDatabase!
    
    let databasePath: String
    let tableName: String
    
    let queryCreate: String
    let queryItem: String
    let queryItems: String
    let queryAll: String
    let queryAdd: String
    let queryDelete: String
    let queryDeleteAll: String
    
    var isTableChecked: Bool = false
    
    init(databasePath: String, tableName: String) {
        self.databasePath = databasePath
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
    
    fileprivate func checkTable() {
        if (isTableChecked) {
            return
        }
        isTableChecked = true
        
        self.db = FMDatabase(path: databasePath)
        self.db.open()
        if (!db.tableExists(tableName)) {
            db.executeUpdate(queryCreate)
        }
    }
    
    func addOrUpdateItems(_ values: JavaUtilList!) {
        checkTable()
        
        db.beginTransaction()
        for i in 0..<values.size() {
            let record = values.getWith(i) as! ARKeyValueRecord
            db.executeUpdate(queryAdd, record.getId().toNSNumber(),record.getData().toNSData() as AnyObject)
        }
        db.commit()
    }
    
    func addOrUpdateItem(withKey key: jlong, withData data: IOSByteArray!) {
        checkTable()
        
        db.beginTransaction()
        db.executeUpdate(queryAdd, key.toNSNumber(), data!.toNSData() as AnyObject)
        db.commit()
    }
    
    func removeItems(withKeys keys: IOSLongArray!) {
        checkTable()
        
        db.beginTransaction()
        for i in 0..<keys.length() {
            let key = keys.long(at: UInt(i));
            db.executeUpdate(queryDelete, key.toNSNumber())
        }
        db.commit()
    }
    
    func removeItem(withKey key: jlong) {
        checkTable()
        
        db.beginTransaction()
        db.executeUpdate(queryDelete, key.toNSNumber())
        db.commit()
    }
    
    func loadItem(withKey key: jlong) -> IOSByteArray! {
        checkTable()
        
        let result = db.dataForQuery(queryItem, key.toNSNumber())
        if (result == nil) {
            return nil
        }
        return result!.toJavaBytes()
    }
    
    func loadAllItems() -> JavaUtilList! {
        checkTable()
        
        let res = JavaUtilArrayList()!
        
        if let result = db.executeQuery(queryAll) {
            while(result.next()) {
                res.add(withId: ARKeyValueRecord(key: jlong(result.longLongInt(forColumn: "ID")), withData: result.data(forColumn: "BYTES").toJavaBytes()))
            }
        }
        
        return res
    }
    
    func loadItems(_ keys: IOSLongArray!) -> JavaUtilList! {
        checkTable()
        
        // Converting to NSNumbers
        var ids = [NSNumber]()
        for i in 0..<keys.length() {
            ids.append(keys.long(at: UInt(i)).toNSNumber())
        }
        
        let res = JavaUtilArrayList()
        
        if let result = db.executeQuery(queryItems, ids as AnyObject) {
            while(result.next()) {
                // TODO: Optimize lookup
                res!.add(withId: ARKeyValueRecord(key: jlong(result.longLongInt(forColumn: "ID")), withData: result.data(forColumn: "BYTES").toJavaBytes()))
            }
        }
        
        return res
    }
    
    func clear() {
        checkTable()
        
        db.beginTransaction()
        db.executeUpdate(queryDeleteAll)
        db.commit()
    }
}
