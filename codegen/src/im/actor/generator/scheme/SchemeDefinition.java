package im.actor.generator.scheme;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by ex3ndr on 14.11.14.
 */
public class SchemeDefinition {
    private List<SchemeSection> sections = new ArrayList<SchemeSection>();

    private HashMap<String, SchemeType> aliases = new HashMap<String, SchemeType>();

    public HashMap<String, SchemeType> getAliases() {
        return aliases;
    }

    public List<SchemeSection> getSections() {
        return sections;
    }

    public List<SchemeRpc> getAllRpc() {
        List<SchemeRpc> res = new ArrayList<SchemeRpc>();
        for (SchemeSection section : sections) {
            res.addAll(section.getAllRpc());
        }
        return res;
    }

    public List<SchemeTrait> getAllTraits() {
        List<SchemeTrait> res = new ArrayList<SchemeTrait>();
        for (SchemeSection section : sections) {
            res.addAll(section.getAllTraits());
        }
        return res;
    }

    public List<SchemeStruct> getAllStructs() {
        List<SchemeStruct> res = new ArrayList<SchemeStruct>();
        for (SchemeSection section : sections) {
            res.addAll(section.getAllStructs());
        }
        return res;
    }

    public List<SchemeUpdate> getAllUpdates() {
        ArrayList<SchemeUpdate> res = new ArrayList<SchemeUpdate>();
        for (SchemeSection section : sections) {
            res.addAll(section.getAllUpdates());
        }
        return res;
    }

    public List<SchemeUpdateBox> getAllUpdateBoxes() {
        ArrayList<SchemeUpdateBox> res = new ArrayList<SchemeUpdateBox>();
        for (SchemeSection section : sections) {
            res.addAll(section.getAllUpdateBoxes());
        }
        return res;
    }

    public List<SchemeEnum> getAllEnums() {
        ArrayList<SchemeEnum> res = new ArrayList<SchemeEnum>();
        for (SchemeSection section : sections) {
            res.addAll(section.getAllEnums());
        }
        return res;
    }

    public List<SchemeBaseResponse> getAllResponses() {
        ArrayList<SchemeBaseResponse> res = new ArrayList<SchemeBaseResponse>();
        for (SchemeSection section : sections) {
            res.addAll(section.getAllResponses());
        }
        return res;
    }
}
