//
//  Copyright (c) 2014-2015 Actor LLC. <https://actor.im>
//

import Foundation

@objc class FMDBIndex: NSObject, ARIndexStorage {
    
    var db :FMDatabase? = nil
    var isTableChecked: Bool = false
    
    let databasePath: String
    let tableName: String
    
    let queryCreate: String
    let queryCreateIndex: String
    
    let queryAdd: String
    let queryItem: String
    let queryFind: String
    
    let queryDelete: String
    let queryDeleteValue: String
    let queryDeleteAll: String
    
    let queryCount: String
    
    init(databasePath: String, tableName: String) {
        self.databasePath = databasePath
        self.tableName = tableName;
        
        self.queryCreate = "CREATE TABLE IF NOT EXISTS " + tableName + " (" + //
            "\"ID\" INTEGER NOT NULL," + // 0: id
            "\"VALUE\" INTEGER NOT NULL," + // 1: value
            "PRIMARY KEY(\"ID\"));";
        self.queryCreateIndex = "CREATE INDEX IF NOT EXISTS IDX_ID_VALUE ON " + tableName + " (\"VALUE\");"
        
        self.queryAdd = "REPLACE INTO " + tableName + " (\"ID\",\"VALUE\") VALUES (?,?)";
        self.queryItem = "SELECT \"VALUE\" FROM " + tableName + " WHERE \"ID\" = ?;";
        self.queryFind = "SElECT \"ID\" FROM " + tableName + " WHERE \"VALUE\" <= ?;"
        
        self.queryDelete = "DELETE FROM " + tableName + " WHERE \"ID\" = ?;"
        self.queryDeleteAll = "DELETE FROM " + tableName + ";"
        self.queryDeleteValue = "DELETE FROM " + tableName + " WHERE \"VALUE\" <= ?;"
        self.queryCount = "SELECT COUNT(*) FROM " + tableName + ";"
    }
    
    func checkTable() {
        if (isTableChecked) {
            return
        }
        isTableChecked = true;
        
        self.db = FMDatabase(path: databasePath)
        self.db!.open()
        if (!db!.tableExists(tableName)) {
            db!.executeUpdate(queryCreate)
            db!.executeUpdate(queryCreateIndex)
        }
    }
    
    func putWithKey(key: jlong, withValue value: jlong) {
        checkTable()
        
        db!.beginTransaction()
        db!.executeUpdate(queryAdd, key.toNSNumber(), value.toNSNumber())
        db!.commit()
    }
    
    func get(key: jlong) -> JavaLangLong! {
        checkTable()
        
        // TODO: Fix?
        let result = db!.longForQuery(queryItem, key.toNSNumber());
        if (result == nil) {
            return nil
        }
        
        return JavaLangLong(long: jlong(result))
    }
    
    func findBeforeValue(value: jlong) -> JavaUtilList! {
        checkTable()
        
        let dbResult = db!.executeQuery(queryFind, value.toNSNumber())
        
        if dbResult == nil {
            return JavaUtilArrayList()
        }
        
        let res = JavaUtilArrayList()
        while(dbResult!.next()) {
            res.addWithId(JavaLangLong(long: jlong(dbResult!.longLongIntForColumn("ID"))))
        }
        dbResult!.close()
        
        return res
    }
    
    func removeBeforeValue(value: jlong) -> JavaUtilList! {
        checkTable()
        
        let res = findBeforeValue(value)
        
        removeWithKeys(res)
        
        return res
    }
    
    func removeWithKey(key: jlong) {
        checkTable()
        
        db!.beginTransaction()
        db!.executeUpdate(queryDelete, key.toNSNumber())
        db!.commit()
    }
    
    func removeWithKeys(keys: JavaUtilList!) {
        checkTable()
     
        db!.beginTransaction()
        for index in 0..<keys.size() {
            let key = (keys.getWithInt(index) as! JavaLangLong).longLongValue()
            db!.executeUpdate(queryDelete, key.toNSNumber())
        }
        db!.commit()
    }
    
    func getCount() -> jint {
        checkTable()
        
        let result = db!.executeQuery(queryCount)
        if (result == nil) {
            return 0;
        }
        if (result!.next()) {
            let res = jint(result!.intForColumnIndex(0))
            result?.close()
            return res
        } else {
            result?.close()
        }
        
        return 0;
    }
    
    func clear() {
        checkTable()
     
        db!.beginTransaction()
        db!.executeUpdate(queryDeleteAll);
        db!.commit()
    }
}