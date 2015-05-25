//
//  Copyright (c) 2015 Actor LLC. <https://actor.im>
//

import Foundation

class FMDBList : NSObject, DKListStorageDisplayEx {
    
    var db :FMDatabase? = nil;
    var isTableChecked: Bool = false;
    
    let databasePath: String;
    let tableName: String;

    let queryCreate: String;
    let queryCreateIndex: String;
    let queryCreateFilter: String;
    
    let queryCount: String;
    let queryAdd: String;
    let queryItem: String;

    let queryDelete: String;
    let queryDeleteAll: String;
    
    let queryForwardFirst: String;
    let queryForwardMore: String;
    
    let queryForwardFilterFirst: String;
    let queryForwardFilterMore: String;
    
    let queryBackwardFirst: String;
    let queryBackwardMore: String;
    let queryBackwardFilterFirst: String;
    let queryBackwardFilterMore: String;
    
    init (databasePath: String, tableName: String){
        self.databasePath = databasePath
        self.tableName = tableName;
        
        self.queryCreate = "CREATE TABLE IF NOT EXISTS " + tableName + " (" + //
            "\"ID\" INTEGER NOT NULL," + // 0: id
            "\"SORT_KEY\" INTEGER NOT NULL," + // 1: sortKey
            "\"QUERY\" TEXT," + // 2: query
            "\"BYTES\" BLOB NOT NULL," + // 3: bytes
            "PRIMARY KEY(\"ID\"));";
        self.queryCreateIndex = "CREATE INDEX IF NOT EXISTS IDX_ID_SORT ON " + tableName + " (\"SORT_KEY\");"
        self.queryCreateFilter = "CREATE INDEX IF NOT EXISTS IDX_ID_QUERY_SORT ON " + tableName + " (\"QUERY\", \"SORT_KEY\");"
        
        self.queryCount = "SELECT COUNT(*) FROM " + tableName + ";";
        self.queryAdd = "REPLACE INTO " + tableName + " (\"ID\",\"QUERY\",\"SORT_KEY\",\"BYTES\") VALUES (?,?,?,?)";
        self.queryItem = "SELECT \"ID\",\"QUERY\",\"SORT_KEY\",\"BYTES\" FROM " + tableName + " WHERE \"ID\" = ?;";

        self.queryDeleteAll = "DELETE FROM " + tableName + ";";
        self.queryDelete = "DELETE FROM " + tableName + " WHERE \"ID\"= ?;";
        
        self.queryForwardFirst = "SELECT \"ID\", \"QUERY\",\"SORT_KEY\", \"BYTES\" FROM " + tableName + " ORDER BY SORT_KEY DESC LIMIT ?";
        self.queryForwardMore = "SELECT \"ID\", \"QUERY\",\"SORT_KEY\", \"BYTES\" FROM " + tableName + " WHERE \"SORT_KEY\" < ? ORDER BY SORT_KEY DESC LIMIT ?";
        
        self.queryBackwardFirst = "SELECT \"ID\", \"QUERY\",\"SORT_KEY\", \"BYTES\" FROM " + tableName + " ORDER BY SORT_KEY ASC LIMIT ?";
        self.queryBackwardMore = "SELECT \"ID\", \"QUERY\",\"SORT_KEY\", \"BYTES\" FROM " + tableName + " WHERE \"SORT_KEY\" >= ? ORDER BY SORT_KEY ASC LIMIT ?";
        
        self.queryForwardFilterFirst = "SELECT \"ID\", \"QUERY\",\"SORT_KEY\", \"BYTES\" FROM " + tableName + " WHERE \"QUERY\" LIKE ? OR \"QUERY\" LIKE ? ORDER BY SORT_KEY DESC LIMIT ?";
        self.queryForwardFilterMore = "SELECT \"ID\", \"QUERY\",\"SORT_KEY\", \"BYTES\" FROM " + tableName + " WHERE (\"QUERY\" LIKE ? OR \"QUERY\" LIKE ?) AND \"SORT_KEY\" < ? ORDER BY SORT_KEY DESC LIMIT ?";

        self.queryBackwardFilterFirst = "SELECT \"ID\", \"QUERY\",\"SORT_KEY\", \"BYTES\" FROM " + tableName + " WHERE \"QUERY\" LIKE ? OR \"QUERY\" LIKE ? ORDER BY SORT_KEY ASC LIMIT ?";
        self.queryBackwardFilterMore = "SELECT \"ID\", \"QUERY\",\"SORT_KEY\", \"BYTES\" FROM " + tableName + " WHERE (\"QUERY\" LIKE ? OR \"QUERY\" LIKE ?) AND \"SORT_KEY\" > ? ORDER BY SORT_KEY ASC LIMIT ?";
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
            db!.executeUpdate(queryCreateFilter)
        }
    }
    
    func updateOrAddWithValue(valueContainer: DKListEngineRecord!) {
        checkTable();
        
        db!.beginTransaction()
        db!.executeUpdate(queryAdd, withArgumentsInArray: [valueContainer.getKey().toNSNumber(), valueContainer.dbQuery(), valueContainer.getOrder().toNSNumber(),
            valueContainer.getData().toNSData()])
        db!.commit()
    }
    
    func updateOrAddWithList(items: JavaUtilList!) {
        checkTable();
        
        db!.beginTransaction()
        for i in 0..<items.size() {
            let record = items.getWithInt(i) as! DKListEngineRecord;
            db!.executeUpdate(queryAdd, record.getKey().toNSNumber(), record.dbQuery(), record.getOrder().toNSNumber(),
                record.getData().toNSData())
        }
        db!.commit()
    }
    
    func deleteWithKey(key: jlong) {
        checkTable();
        
        db!.beginTransaction()
        db!.executeUpdate(queryDelete, key.toNSNumber());
        db!.commit()
    }

    func deleteWithKeys(keys: IOSLongArray!) {
        checkTable();
        
        db!.beginTransaction()
        for i in 0..<keys.length() {
            var k = keys.longAtIndex(UInt(i));
            db!.executeUpdate(queryDelete, k.toNSNumber());
        }
        db!.commit()
    }
    
    func getCount() -> jint {
        checkTable();
        
        var result = db!.executeQuery(queryCount)
        if (result == nil) {
            return 0;
        }
        if (result!.next()) {
            var res = jint(result!.intForColumnIndex(0))
            result?.close()
            return res
        } else {
            result?.close()
        }
        
        return 0;
    }
    
    func isEmpty() -> Bool {
        return getCount() == 0;
    }
    
    func clear() {
        checkTable();
        
        db!.beginTransaction()
        db!.executeUpdate(queryDeleteAll);
        db!.commit()
    }
    
    func loadItemWithKey(key: jlong) -> DKListEngineRecord! {
        checkTable();
        
        var result = db!.executeQuery(queryItem, key.toNSNumber());
        if (result == nil) {
            return nil
        }
        if (result!.next()) {
            var query: AnyObject! = result!.objectForColumnName("QUERY");
            if (query is NSNull){
                query = nil
            }
            var res = DKListEngineRecord(key: jlong(result!.longLongIntForColumn("ID")), withOrder: jlong(result!.longLongIntForColumn("SORT_KEY")), withQuery: query as! String?, withData: result!.dataForColumn("BYTES").toJavaBytes())
            result?.close()
            return res;
        } else {
            result?.close()
            return nil
        }
    }
    
    func loadForwardWithSortKey(sortingKey: JavaLangLong!, withLimit limit: jint) -> JavaUtilList! {
        checkTable();
        var result : FMResultSet? = nil;
        if (sortingKey == nil) {
            result = db!.executeQuery(queryForwardFirst, limit.toNSNumber());
        } else {
            result = db!.executeQuery(queryForwardMore, sortingKey!.toNSNumber(), limit.toNSNumber());
        }
        if (result == nil) {
            NSLog(db!.lastErrorMessage())
            return nil
        }
        
        var res: JavaUtilArrayList = JavaUtilArrayList();
        
        while(result!.next()) {
            var query: AnyObject! = result!.objectForColumnName("QUERY");
            if (query is NSNull) {
                query = nil
            }
            var record = DKListEngineRecord(key: jlong(result!.longLongIntForColumn("ID")), withOrder: jlong(result!.longLongIntForColumn("SORT_KEY")), withQuery: query as! String?, withData: result!.dataForColumn("BYTES").toJavaBytes())
            res.addWithId(record)
        }
        result!.close()
        return res;
    }
    
    func loadForwardWithQuery(query: String!, withSortKey sortingKey: JavaLangLong!, withLimit limit: jint) -> JavaUtilList! {
        checkTable();
        
        var result : FMResultSet? = nil;
        if (sortingKey == nil) {
            result = db!.executeQuery(queryForwardFilterFirst, query + "%", "% " + query + "%", limit.toNSNumber());
        } else {
            result = db!.executeQuery(queryForwardFilterMore, query + "%", "% " + query + "%", sortingKey!.toNSNumber(), limit.toNSNumber());
        }
        if (result == nil) {
            NSLog(db!.lastErrorMessage())
            return nil
        }
        
        var res: JavaUtilArrayList = JavaUtilArrayList();
        
        while(result!.next()) {
            var query: AnyObject! = result!.objectForColumnName("QUERY");
            if (query is NSNull) {
                query = nil
            }
            var record = DKListEngineRecord(key: jlong(result!.longLongIntForColumn("ID")), withOrder: jlong(result!.longLongIntForColumn("SORT_KEY")), withQuery: query as! String?, withData: result!.dataForColumn("BYTES").toJavaBytes())
            res.addWithId(record)
        }
        result!.close()
        
        return res;

    }
    
    func loadBackwardWithSortKey(sortingKey: JavaLangLong!, withLimit limit: jint) -> JavaUtilList! {
        checkTable();
        var result : FMResultSet? = nil;
        if (sortingKey == nil) {
            result = db!.executeQuery(queryBackwardFirst, limit.toNSNumber());
        } else {
            result = db!.executeQuery(queryBackwardMore, sortingKey!.toNSNumber(), limit.toNSNumber());
        }
        if (result == nil) {
            NSLog(db!.lastErrorMessage())
            return nil
        }
        
        var res: JavaUtilArrayList = JavaUtilArrayList();
        
        while(result!.next()) {
            var query: AnyObject! = result!.objectForColumnName("QUERY");
            if (query is NSNull) {
                query = nil
            }
            var record = DKListEngineRecord(key: jlong(result!.longLongIntForColumn("ID")), withOrder: jlong(result!.longLongIntForColumn("SORT_KEY")), withQuery: query as! String?, withData: result!.dataForColumn("BYTES").toJavaBytes())
            res.addWithId(record)
        }
        result!.close()
        return res;
    }
    
    func loadBackwardWithQuery(query: String!, withSortKey sortingKey: JavaLangLong!, withLimit limit: jint) -> JavaUtilList! {
        checkTable();
        
        var result : FMResultSet? = nil;
        if (sortingKey == nil) {
            result = db!.executeQuery(queryBackwardFilterFirst, query + "%", "% " + query + "%", limit.toNSNumber());
        } else {
            result = db!.executeQuery(queryBackwardFilterMore, query + "%", "% " + query + "%", sortingKey!.toNSNumber(), limit.toNSNumber());
        }
        if (result == nil) {
            NSLog(db!.lastErrorMessage())
            return nil
        }
        
        var res: JavaUtilArrayList = JavaUtilArrayList();
        
        while(result!.next()) {
            var query: AnyObject! = result!.objectForColumnName("QUERY");
            if (query is NSNull) {
                query = nil
            }
            var record = DKListEngineRecord(key: jlong(result!.longLongIntForColumn("ID")), withOrder: jlong(result!.longLongIntForColumn("SORT_KEY")), withQuery: query as! String?, withData: result!.dataForColumn("BYTES").toJavaBytes())
            res.addWithId(record)
        }
        result!.close()
        
        return res;

    }
    
    func loadCenterWithSortKey(centerSortKey: JavaLangLong!, withLimit limit: jint) -> JavaUtilList! {
        checkTable();
        
        NSLog("loadCenterWithSortKey:\(centerSortKey)")
        
        var res: JavaUtilArrayList = JavaUtilArrayList();
        res.addAllWithJavaUtilCollection(loadBackwardWithSortKey(centerSortKey, withLimit: limit))
        res.addAllWithJavaUtilCollection(loadForwardWithSortKey(centerSortKey, withLimit: limit))
        return res
    }
}