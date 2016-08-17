//
//  Copyright (c) 2014-2016 Actor LLC. <https://actor.im>
//

import Foundation

class FMDBList : NSObject, ARListStorageDisplayEx {
    
    let dbQueue: FMDatabaseQueue
    let tableName: String
    
    var isTableChecked: Bool = false
    
    let queryCreate: String
    let queryCreateIndex: String
    let queryCreateFilter: String
    
    let queryCount: String
    let queryEmpty: String
    let queryAdd: String
    let queryItem: String

    let queryDelete: String
    let queryDeleteAll: String
    
    let queryForwardFirst: String
    let queryForwardMore: String
    
    let queryForwardFilterFirst: String
    let queryForwardFilterMore: String
    
    let queryBackwardFirst: String
    let queryBackwardMore: String
    let queryBackwardFilterFirst: String
    let queryBackwardFilterMore: String
    
    let queryCenterBackward: String
    let queryCenterForward: String
    
    init (dbQueue: FMDatabaseQueue, tableName: String){
        self.dbQueue = dbQueue
        self.tableName = tableName
        
        self.queryCreate = "CREATE TABLE IF NOT EXISTS " + tableName + " (" + //
            "\"ID\" INTEGER NOT NULL," + // 0: id
            "\"SORT_KEY\" INTEGER NOT NULL," + // 1: sortKey
            "\"QUERY\" TEXT," + // 2: query
            "\"BYTES\" BLOB NOT NULL," + // 3: bytes
            "PRIMARY KEY(\"ID\"));"
        self.queryCreateIndex = "CREATE INDEX IF NOT EXISTS IDX_ID_SORT ON " + tableName + " (\"SORT_KEY\");"
        self.queryCreateFilter = "CREATE INDEX IF NOT EXISTS IDX_ID_QUERY_SORT ON " + tableName + " (\"QUERY\", \"SORT_KEY\");"
        
        self.queryCount = "SELECT COUNT(*) FROM " + tableName + ";"
        self.queryEmpty = "EXISTS (SELECT * FROM " + tableName + ");"
        self.queryAdd = "REPLACE INTO " + tableName + " (\"ID\",\"QUERY\",\"SORT_KEY\",\"BYTES\") VALUES (?,?,?,?)"
        self.queryItem = "SELECT \"ID\",\"QUERY\",\"SORT_KEY\",\"BYTES\" FROM " + tableName + " WHERE \"ID\" = ?;"

        self.queryDeleteAll = "DELETE FROM " + tableName + ";"
        self.queryDelete = "DELETE FROM " + tableName + " WHERE \"ID\"= ?;"
        
        self.queryForwardFirst = "SELECT \"ID\", \"QUERY\",\"SORT_KEY\", \"BYTES\" FROM " + tableName + " ORDER BY SORT_KEY DESC LIMIT ?"
        self.queryForwardMore = "SELECT \"ID\", \"QUERY\",\"SORT_KEY\", \"BYTES\" FROM " + tableName + " WHERE \"SORT_KEY\" < ? ORDER BY SORT_KEY DESC LIMIT ?"
        
        self.queryBackwardFirst = "SELECT \"ID\", \"QUERY\",\"SORT_KEY\", \"BYTES\" FROM " + tableName + " ORDER BY SORT_KEY ASC LIMIT ?"
        self.queryBackwardMore = "SELECT \"ID\", \"QUERY\",\"SORT_KEY\", \"BYTES\" FROM " + tableName + " WHERE \"SORT_KEY\" > ? ORDER BY SORT_KEY ASC LIMIT ?"
        
        self.queryCenterForward = queryForwardMore
        self.queryCenterBackward = "SELECT \"ID\", \"QUERY\",\"SORT_KEY\", \"BYTES\" FROM " + tableName + " WHERE \"SORT_KEY\" >= ? ORDER BY SORT_KEY ASC LIMIT ?"
        
        self.queryForwardFilterFirst = "SELECT \"ID\", \"QUERY\",\"SORT_KEY\", \"BYTES\" FROM " + tableName + " WHERE \"QUERY\" LIKE ? OR \"QUERY\" LIKE ? ORDER BY SORT_KEY DESC LIMIT ?"
        self.queryForwardFilterMore = "SELECT \"ID\", \"QUERY\",\"SORT_KEY\", \"BYTES\" FROM " + tableName + " WHERE (\"QUERY\" LIKE ? OR \"QUERY\" LIKE ?) AND \"SORT_KEY\" < ? ORDER BY SORT_KEY DESC LIMIT ?"

        self.queryBackwardFilterFirst = "SELECT \"ID\", \"QUERY\",\"SORT_KEY\", \"BYTES\" FROM " + tableName + " WHERE \"QUERY\" LIKE ? OR \"QUERY\" LIKE ? ORDER BY SORT_KEY ASC LIMIT ?"
        self.queryBackwardFilterMore = "SELECT \"ID\", \"QUERY\",\"SORT_KEY\", \"BYTES\" FROM " + tableName + " WHERE (\"QUERY\" LIKE ? OR \"QUERY\" LIKE ?) AND \"SORT_KEY\" > ? ORDER BY SORT_KEY ASC LIMIT ?"
    }
    
    func checkTable() {
        if (isTableChecked) {
            return
        }
        isTableChecked = true;
        
        dbQueue.inDatabase { (db) in
            if (!db!.tableExists(self.tableName)) {
                db!.executeUpdate(self.queryCreate)
                db!.executeUpdate(self.queryCreateIndex)
                db!.executeUpdate(self.queryCreateFilter)
            }
        }
    }
    
    func updateOrAddWithValue(valueContainer: ARListEngineRecord!) {
        checkTable()
        
        let start = NSDate()
        
        dbQueue.inDatabase { (db) in
            db.executeUpdate(self.queryAdd, withArgumentsInArray: [valueContainer.getKey().toNSNumber(), valueContainer.dbQuery(), valueContainer.getOrder().toNSNumber(),
                valueContainer.getData().toNSData()])
            
        }
        
        log("updateOrAddWithValue \(tableName): \(valueContainer.getData().length()) in \(Int((NSDate().timeIntervalSinceDate(start)*1000)))")
    }
    
    func updateOrAddWithList(items: JavaUtilList!) {
        checkTable()
        
        dbQueue.inTransaction { (db, rollout) in
            for i in 0..<items.size() {
                let record = items.getWithInt(i) as! ARListEngineRecord;
                db.executeUpdate(self.queryAdd, record.getKey().toNSNumber(), record.dbQuery(), record.getOrder().toNSNumber(),
                    record.getData().toNSData())
            }
        }
    }
    
    func deleteWithKey(key: jlong) {
        checkTable()
        
        dbQueue.inDatabase { (db) in
            db!.executeUpdate(self.queryDelete, key.toNSNumber())
        }
    }

    func deleteWithKeys(keys: IOSLongArray!) {
        checkTable()
        
        dbQueue.inTransaction { (db, rollout) in
            for i in 0..<keys.length() {
                let k = keys.longAtIndex(UInt(i));
                db.executeUpdate(self.queryDelete, k.toNSNumber())
            }
        }
    }
    
    func getCount() -> jint {
        checkTable()
        
        var res: jint = 0
        dbQueue.inDatabase { (db) in
            let result = db.executeQuery(self.queryCount)
            if (result == nil) {
                return;
            }
            if (result!.next()) {
                res = jint(result!.intForColumnIndex(0))
                result?.close()
            } else {
                result?.close()
            }
        }
        return res;
    }
    
    func isEmpty() -> Bool {
        checkTable()
        
        var res: Bool = false
        dbQueue.inDatabase { (db) in
            let result = db!.executeQuery(self.queryEmpty)
            if (result == nil) {
                return
            }
            if (result!.next()) {
                res = result!.intForColumnIndex(0) > 0
            }
            result?.close()
        }
        return res
    }
    
    func clear() {
        checkTable()
        
        dbQueue.inTransaction { (db, rollout) in
            db.executeUpdate(self.queryDeleteAll)
        }
    }
    
    func loadItemWithKey(key: jlong) -> ARListEngineRecord! {
        checkTable()
        
        var res: ARListEngineRecord! = nil
        
        dbQueue.inDatabase { (db) in
            let result = db!.executeQuery(self.queryItem, key.toNSNumber())
            if (result == nil) {
                return
            }
            if (result!.next()) {
                var query: AnyObject! = result!.objectForColumnName("QUERY")
                if (query is NSNull){
                    query = nil
                }
                res = ARListEngineRecord(key: jlong(result!.longLongIntForColumn("ID")), withOrder: jlong(result!.longLongIntForColumn("SORT_KEY")), withQuery: query as! String?, withData: result!.dataForColumn("BYTES").toJavaBytes())
            }
            result?.close()
        }
        
        
        return res
    }
    
    func loadAllItems() -> JavaUtilList! {
        let res = JavaUtilArrayList()
        // TODO: Implement
        return res
    }
    
    func loadForwardWithSortKey(sortingKey: JavaLangLong!, withLimit limit: jint) -> JavaUtilList! {
        checkTable()
        
        let res = JavaUtilArrayList()
        dbQueue.inDatabase { (db) in
            var result : FMResultSet? = nil
            if (sortingKey == nil) {
                result = db!.executeQuery(self.queryForwardFirst, limit.toNSNumber())
            } else {
                result = db!.executeQuery(self.queryForwardMore, sortingKey!.toNSNumber(), limit.toNSNumber())
            }
            
            if (result == nil) {
                NSLog(db!.lastErrorMessage())
                return
            }
            let queryIndex = result!.columnIndexForName("QUERY")
            let idIndex = result!.columnIndexForName("ID")
            let sortKeyIndex = result!.columnIndexForName("SORT_KEY")
            let bytesIndex = result!.columnIndexForName("BYTES")
            var dataSize = 0
            var rowCount = 0
            
            while(result!.next()) {
                let key = jlong(result!.longLongIntForColumnIndex(idIndex))
                let order = jlong(result!.longLongIntForColumnIndex(sortKeyIndex))
                var query: AnyObject! = result!.objectForColumnIndex(queryIndex)
                if (query is NSNull) {
                    query = nil
                }
                let data = result!.dataForColumnIndex(bytesIndex).toJavaBytes()
                dataSize += Int(data.length())
                rowCount += 1
                
                let record = ARListEngineRecord(key: key, withOrder: order, withQuery: query as! String?, withData: data)
                res.addWithId(record)
            }
            result!.close()
        }
        return res
    }
    
    func loadForwardWithQuery(query: String!, withSortKey sortingKey: JavaLangLong!, withLimit limit: jint) -> JavaUtilList! {
        checkTable()
        
        let res = JavaUtilArrayList()
        dbQueue.inDatabase { (db) in
            var result : FMResultSet? = nil
            if (sortingKey == nil) {
                result = db!.executeQuery(self.queryForwardFilterFirst, query + "%", "% " + query + "%", limit.toNSNumber())
            } else {
                result = db!.executeQuery(self.queryForwardFilterMore, query + "%", "% " + query + "%", sortingKey!.toNSNumber(), limit.toNSNumber())
            }
            if (result == nil) {
                NSLog(db!.lastErrorMessage())
                return
            }
            
            while(result!.next()) {
                var query: AnyObject! = result!.objectForColumnName("QUERY")
                if (query is NSNull) {
                    query = nil
                }
                let record = ARListEngineRecord(key: jlong(result!.longLongIntForColumn("ID")), withOrder: jlong(result!.longLongIntForColumn("SORT_KEY")), withQuery: query as! String?, withData: result!.dataForColumn("BYTES").toJavaBytes())
                res.addWithId(record)
            }
            result!.close()
        }
        return res
    }
    
    func loadBackwardWithSortKey(sortingKey: JavaLangLong!, withLimit limit: jint) -> JavaUtilList! {
        checkTable()
        
        let res = JavaUtilArrayList()
        dbQueue.inDatabase { (db) in
            var result : FMResultSet? = nil
            if (sortingKey == nil) {
                result = db!.executeQuery(self.queryBackwardFirst, limit.toNSNumber())
            } else {
                result = db!.executeQuery(self.queryBackwardMore, sortingKey!.toNSNumber(), limit.toNSNumber())
            }
            if (result == nil) {
                NSLog(db!.lastErrorMessage())
                return
            }
            
            while(result!.next()) {
                var query: AnyObject! = result!.objectForColumnName("QUERY")
                if (query is NSNull) {
                    query = nil
                }
                let record = ARListEngineRecord(key: jlong(result!.longLongIntForColumn("ID")), withOrder: jlong(result!.longLongIntForColumn("SORT_KEY")), withQuery: query as! String?, withData: result!.dataForColumn("BYTES").toJavaBytes())
                res.addWithId(record)
            }
            result!.close()
        }
        return res
    }
    
    func loadBackwardWithQuery(query: String!, withSortKey sortingKey: JavaLangLong!, withLimit limit: jint) -> JavaUtilList! {
        checkTable()
        
        let res = JavaUtilArrayList()
        dbQueue.inDatabase { (db) in
            var result : FMResultSet? = nil
            if (sortingKey == nil) {
                result = db!.executeQuery(self.queryBackwardFilterFirst, query + "%", "% " + query + "%", limit.toNSNumber())
            } else {
                result = db!.executeQuery(self.queryBackwardFilterMore, query + "%", "% " + query + "%", sortingKey!.toNSNumber(), limit.toNSNumber())
            }
            if (result == nil) {
                NSLog(db!.lastErrorMessage())
                return
            }
            
            while(result!.next()) {
                var query: AnyObject! = result!.objectForColumnName("QUERY")
                if (query is NSNull) {
                    query = nil
                }
                let record = ARListEngineRecord(key: jlong(result!.longLongIntForColumn("ID")), withOrder: jlong(result!.longLongIntForColumn("SORT_KEY")), withQuery: query as! String?, withData: result!.dataForColumn("BYTES").toJavaBytes())
                res.addWithId(record)
            }
            result!.close()
        }
        return res
    }
    
    func loadCenterWithSortKey(centerSortKey: JavaLangLong!, withLimit limit: jint) -> JavaUtilList! {
        checkTable()
        
        let res: JavaUtilArrayList = JavaUtilArrayList()
        dbQueue.inDatabase { (db) in
            res.addAllWithJavaUtilCollection(self.loadSlise(db.executeQuery(self.queryCenterBackward, centerSortKey.toNSNumber(), limit.toNSNumber()), db: db))
            res.addAllWithJavaUtilCollection(self.loadSlise(db.executeQuery(self.queryCenterForward, centerSortKey.toNSNumber(), limit.toNSNumber()), db: db))
        }
        return res
    }
    
    func loadSlise(result: FMResultSet?, db: FMDatabase) -> JavaUtilList! {
        if (result == nil) {
            NSLog(db.lastErrorMessage())
            return nil
        }
        
        let res: JavaUtilArrayList = JavaUtilArrayList()
        
        while(result!.next()) {
            var query: AnyObject! = result!.objectForColumnName("QUERY")
            if (query is NSNull) {
                query = nil
            }
            let record = ARListEngineRecord(key: jlong(result!.longLongIntForColumn("ID")), withOrder: jlong(result!.longLongIntForColumn("SORT_KEY")), withQuery: query as! String?, withData: result!.dataForColumn("BYTES").toJavaBytes())
            res.addWithId(record)
        }
        result!.close()
        return res
    }
}