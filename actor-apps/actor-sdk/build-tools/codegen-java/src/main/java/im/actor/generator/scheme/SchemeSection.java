package im.actor.generator.scheme;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ex3ndr on 14.11.14.
 */
public class SchemeSection {
    private String name;
    private String pkg;
    private List<SchemeRecord> records = new ArrayList<SchemeRecord>();

    private List<String> docs = new ArrayList<String>();

    public SchemeSection(String name, String pkg) {
        this.name = name;
        this.pkg = pkg;
    }

    public String getPkg() {
        return pkg;
    }

    public String getName() {
        return name;
    }

    public List<SchemeRecord> getRecords() {
        return records;
    }

    public List<String> getDocs() {
        return docs;
    }

    public String getUniformedDocs() {
        String res = "";
        for (String d : docs) {

            res += d + " ";
        }
        return res;
    }

    public List<SchemeRpc> getAllRpc() {
        ArrayList<SchemeRpc> res = new ArrayList<SchemeRpc>();
        for (SchemeRecord record : records) {
            if (record instanceof SchemeRpc) {
                res.add((SchemeRpc) record);
            }
        }
        return res;
    }

    public List<SchemeTrait> getAllTraits() {
        ArrayList<SchemeTrait> res = new ArrayList<SchemeTrait>();
        for (SchemeRecord record : records) {
            if (record instanceof SchemeTrait) {
                res.add((SchemeTrait) record);
            }
        }
        return res;
    }

    public List<SchemeUpdate> getAllUpdates() {
        ArrayList<SchemeUpdate> res = new ArrayList<SchemeUpdate>();
        for (SchemeRecord record : records) {
            if (record instanceof SchemeUpdate) {
                res.add((SchemeUpdate) record);
            }
        }
        return res;
    }

    public List<SchemeUpdateBox> getAllUpdateBoxes() {
        ArrayList<SchemeUpdateBox> res = new ArrayList<SchemeUpdateBox>();
        for (SchemeRecord record : records) {
            if (record instanceof SchemeUpdateBox) {
                res.add((SchemeUpdateBox) record);
            }
        }
        return res;
    }

    public List<SchemeStruct> getAllStructs() {
        ArrayList<SchemeStruct> res = new ArrayList<SchemeStruct>();
        for (SchemeRecord record : records) {
            if (record instanceof SchemeStruct) {
                res.add((SchemeStruct) record);
            }
        }
        return res;
    }

    public List<SchemeEnum> getAllEnums() {
        ArrayList<SchemeEnum> res = new ArrayList<SchemeEnum>();
        for (SchemeRecord record : records) {
            if (record instanceof SchemeEnum) {
                res.add((SchemeEnum) record);
            }
        }
        return res;
    }

    public List<SchemeBaseResponse> getAllResponses() {
        ArrayList<SchemeBaseResponse> res = new ArrayList<SchemeBaseResponse>();
        for (SchemeRecord record : records) {
            if (record instanceof SchemeResponse) {
                res.add((SchemeResponse) record);
            } else if (record instanceof SchemeRpc) {
                SchemeRpc.AbsResponse response = ((SchemeRpc) record).getResponse();
                if (response instanceof SchemeRpc.AnonymousResponse) {
                    res.add(((SchemeRpc.AnonymousResponse) response).getResponse());
                }
            }
        }
        return res;
    }

    @Override
    public String toString() {
        return ">" + name;
    }
}
