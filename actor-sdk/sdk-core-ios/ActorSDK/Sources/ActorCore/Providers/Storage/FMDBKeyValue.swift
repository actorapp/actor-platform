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
        self.queryItems = "SELECT (\"ID\", \"BYTES\") FROM " + tableName + " WHERE \"ID\" in ?;"
        self.queryAll = "SELECT (\"ID\", \"BYTES\") FROM " + tableName + ";"
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
        
        self.db = FMDatabase(path: databasePath)
        self.db.open()
        if (!db.tableExists(tableName)) {
            db.executeUpdate(queryCreate)
        }
    }
    
    func addOrUpdateItems(values: JavaUtilList!) {
        checkTable()
        
        db.beginTransaction()
        for i in 0..<values.size() {
            let record = values.getWithInt(i) as! ARKeyValueRecord
            db.executeUpdate(queryAdd, record.getId().toNSNumber(),record.getData().toNSData())
        }
        db.commit()
    }
    
    func addOrUpdateItemWithKey(key: jlong, withData data: IOSByteArray!) {
        checkTable()
        
        db.beginTransaction()
        db.executeUpdate(queryAdd, key.toNSNumber(), data!.toNSData())
        db.commit()
    }
    
    func removeItemsWithKeys(keys: IOSLongArray!) {
        checkTable()
        
        db.beginTransaction()
        for i in 0..<keys.length() {
            let key = keys.longAtIndex(UInt(i));
            db.executeUpdate(queryDelete, key.toNSNumber())
        }
        db.commit()
    }
    
    func removeItemWithKey(key: jlong) {
        checkTable()
        
        db.beginTransaction()
        db.executeUpdate(queryDelete, key.toNSNumber())
        db.commit()
    }
    
    func loadItemWithKey(key: jlong) -> IOSByteArray! {
        checkTable()
        
        let result = db.dataForQuery(queryItem, key.toNSNumber())
        if (result == nil) {
            return nil
        }
        return result.toJavaBytes()
    }
    
    func loadAllItems() -> JavaUtilList! {
        checkTable()
        
        let res = JavaUtilArrayList()
        
        if let result = db.executeQuery(queryAll) {
            while(result.next()) {
                res.addWithId(ARKeyValueRecord(key: jlong(result.longLongIntForColumn("ID")), withData: result.dataForColumn("BYTES").toJavaBytes()))
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
        
        if let result = db.executeQuery(queryItems, ids) {
            while(result.next()) {
                // TODO: Optimize lookup
                res.addWithId(ARKeyValueRecord(key: jlong(result.longLongIntForColumn("ID")), withData: result.dataForColumn("BYTES").toJavaBytes()))
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