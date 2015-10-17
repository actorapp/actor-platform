package im.actor.messenger.app.util.country;

import android.text.TextUtils;

import im.actor.messenger.R;

import java.util.ArrayList;

public class CountryDb {

    private static CountryDb instance;

    public synchronized static CountryDb getInstance() {
        if (instance == null) {
            instance = new CountryDb();
        }
        return instance;
    }


    public Country getCountryByShortName(String shortName) {
        if (!TextUtils.isEmpty(shortName)) {
            final String upperCaseShortName = shortName.toUpperCase();
            for (Country c : countries) {
                if (c.shortName.equals(upperCaseShortName)) {
                    return c;
                }
            }
        }
        return null;
    }

    public Country getCountryByPhoneCode(String code) {
        if (!TextUtils.isEmpty(code)) {
            for (Country c : countries) {
                if (c.phoneCode.equals(code)) {
                    return c;
                }
            }
        }
        return null;
    }

    public ArrayList<Country> getCountries() {
        return countries;
    }

    private static final ArrayList<Country> countries;


    static {
        countries = new ArrayList<Country>();

        countries.add(new Country("1", "US", R.string.US));
        countries.add(new Country("1", "CA", R.string.CA));
        countries.add(new Country("1", "DO", R.string.DO));
        countries.add(new Country("1", "PR", R.string.PR));
        countries.add(new Country("7", "RU", R.string.RU));
        countries.add(new Country("7", "KZ", R.string.KZ));
        countries.add(new Country("20", "EG", R.string.EG));
        countries.add(new Country("27", "ZA", R.string.ZA));
        countries.add(new Country("30", "GR", R.string.GR));
        countries.add(new Country("31", "NL", R.string.NL));
        countries.add(new Country("32", "BE", R.string.BE));
        countries.add(new Country("33", "FR", R.string.FR));
        countries.add(new Country("34", "ES", R.string.ES));
        countries.add(new Country("36", "HU", R.string.HU));
        countries.add(new Country("39", "IT", R.string.IT));
        countries.add(new Country("40", "RO", R.string.RO));
        countries.add(new Country("41", "CH", R.string.CH));
        countries.add(new Country("42", "YL", R.string.YL));
        countries.add(new Country("43", "AT", R.string.AT));
        countries.add(new Country("44", "GB", R.string.GB));
        countries.add(new Country("45", "DK", R.string.DK));
        countries.add(new Country("46", "SE", R.string.SE));
        countries.add(new Country("47", "NO", R.string.NO));
        countries.add(new Country("48", "PL", R.string.PL));
        countries.add(new Country("49", "DE", R.string.DE));
        countries.add(new Country("51", "PE", R.string.PE));
        countries.add(new Country("52", "MX", R.string.MX));
        countries.add(new Country("53", "CU", R.string.CU));
        countries.add(new Country("54", "AR", R.string.AR));
        countries.add(new Country("55", "BR", R.string.BR));
        countries.add(new Country("56", "CL", R.string.CL));
        countries.add(new Country("57", "CO", R.string.CO));
        countries.add(new Country("58", "VE", R.string.VE));
        countries.add(new Country("60", "MY", R.string.MY));
        countries.add(new Country("61", "AU", R.string.AU));
        countries.add(new Country("62", "ID", R.string.ID));
        countries.add(new Country("63", "PH", R.string.PH));
        countries.add(new Country("64", "NZ", R.string.NZ));
        countries.add(new Country("65", "SG", R.string.SG));
        countries.add(new Country("66", "TH", R.string.TH));
        countries.add(new Country("81", "JP", R.string.JP));
        countries.add(new Country("82", "KR", R.string.KR));
        countries.add(new Country("84", "VN", R.string.VN));
        countries.add(new Country("86", "CN", R.string.CN));
        countries.add(new Country("90", "TR", R.string.TR));
        countries.add(new Country("91", "IN", R.string.IN));
        countries.add(new Country("92", "PK", R.string.PK));
        countries.add(new Country("93", "AF", R.string.AF));
        countries.add(new Country("94", "LK", R.string.LK));
        countries.add(new Country("95", "MM", R.string.MM));
        countries.add(new Country("98", "IR", R.string.IR));
        countries.add(new Country("211", "SS", R.string.SS));
        countries.add(new Country("212", "MA", R.string.MA));
        countries.add(new Country("213", "DZ", R.string.DZ));
        countries.add(new Country("216", "TN", R.string.TN));
        countries.add(new Country("218", "LY", R.string.LY));
        countries.add(new Country("220", "GM", R.string.GM));
        countries.add(new Country("221", "SN", R.string.SN));
        countries.add(new Country("222", "MR", R.string.MR));
        countries.add(new Country("223", "ML", R.string.ML));
        countries.add(new Country("224", "GN", R.string.GN));
        countries.add(new Country("225", "CI", R.string.CI));
        countries.add(new Country("226", "BF", R.string.BF));
        countries.add(new Country("227", "NE", R.string.NE));
        countries.add(new Country("228", "TG", R.string.TG));
        countries.add(new Country("229", "BJ", R.string.BJ));
        countries.add(new Country("230", "MU", R.string.MU));
        countries.add(new Country("231", "LR", R.string.LR));
        countries.add(new Country("232", "SL", R.string.SL));
        countries.add(new Country("233", "GH", R.string.GH));
        countries.add(new Country("234", "NG", R.string.NG));
        countries.add(new Country("235", "TD", R.string.TD));
        countries.add(new Country("236", "CF", R.string.CF));
        countries.add(new Country("237", "CM", R.string.CM));
        countries.add(new Country("238", "CV", R.string.CV));
        countries.add(new Country("239", "ST", R.string.ST));
        countries.add(new Country("240", "GQ", R.string.GQ));
        countries.add(new Country("241", "GA", R.string.GA));
        countries.add(new Country("242", "CG", R.string.CG));
        countries.add(new Country("243", "CD", R.string.CD));
        countries.add(new Country("244", "AO", R.string.AO));
        countries.add(new Country("245", "GW", R.string.GW));
        countries.add(new Country("246", "IO", R.string.IO));
        countries.add(new Country("247", "SH", R.string.SH));
        countries.add(new Country("248", "SC", R.string.SC));
        countries.add(new Country("249", "SD", R.string.SD));
        countries.add(new Country("250", "RW", R.string.RW));
        countries.add(new Country("251", "ET", R.string.ET));
        countries.add(new Country("252", "SO", R.string.SO));
        countries.add(new Country("253", "DJ", R.string.DJ));
        countries.add(new Country("254", "KE", R.string.KE));
        countries.add(new Country("255", "TZ", R.string.TZ));
        countries.add(new Country("256", "UG", R.string.UG));
        countries.add(new Country("257", "BI", R.string.BI));
        countries.add(new Country("258", "MZ", R.string.MZ));
        countries.add(new Country("260", "ZM", R.string.ZM));
        countries.add(new Country("261", "MG", R.string.MG));
        countries.add(new Country("262", "RE", R.string.RE));
        countries.add(new Country("263", "ZW", R.string.ZW));
        countries.add(new Country("264", "NA", R.string.NA));
        countries.add(new Country("265", "MW", R.string.MW));
        countries.add(new Country("266", "LS", R.string.LS));
        countries.add(new Country("267", "BW", R.string.BW));
        countries.add(new Country("268", "SZ", R.string.SZ));
        countries.add(new Country("269", "KM", R.string.KM));
        countries.add(new Country("290", "SH", R.string.SH));
        countries.add(new Country("291", "ER", R.string.ER));
        countries.add(new Country("297", "AW", R.string.AW));
        countries.add(new Country("298", "FO", R.string.FO));
        countries.add(new Country("299", "GL", R.string.GL));
        countries.add(new Country("350", "GI", R.string.GI));
        countries.add(new Country("351", "PT", R.string.PT));
        countries.add(new Country("352", "LU", R.string.LU));
        countries.add(new Country("353", "IE", R.string.IE));
        countries.add(new Country("354", "IS", R.string.IS));
        countries.add(new Country("355", "AL", R.string.AL));
        countries.add(new Country("356", "MT", R.string.MT));
        countries.add(new Country("357", "CY", R.string.CY));
        countries.add(new Country("358", "FI", R.string.FI));
        countries.add(new Country("359", "BG", R.string.BG));
        countries.add(new Country("370", "LT", R.string.LT));
        countries.add(new Country("371", "LV", R.string.LV));
        countries.add(new Country("372", "EE", R.string.EE));
        countries.add(new Country("373", "MD", R.string.MD));
        countries.add(new Country("374", "AM", R.string.AM));
        countries.add(new Country("375", "BY", R.string.BY));
        countries.add(new Country("376", "AD", R.string.AD));
        countries.add(new Country("377", "MC", R.string.MC));
        countries.add(new Country("378", "SM", R.string.SM));
        countries.add(new Country("380", "UA", R.string.UA));
        countries.add(new Country("381", "RS", R.string.RS));
        countries.add(new Country("382", "ME", R.string.ME));
        countries.add(new Country("385", "HR", R.string.HR));
        countries.add(new Country("386", "SI", R.string.SI));
        countries.add(new Country("387", "BA", R.string.BA));
        countries.add(new Country("389", "MK", R.string.MK));
        countries.add(new Country("420", "CZ", R.string.CZ));
        countries.add(new Country("421", "SK", R.string.SK));
        countries.add(new Country("423", "LI", R.string.LI));
        countries.add(new Country("500", "FK", R.string.FK));
        countries.add(new Country("501", "BZ", R.string.BZ));
        countries.add(new Country("502", "GT", R.string.GT));
        countries.add(new Country("503", "SV", R.string.SV));
        countries.add(new Country("504", "HN", R.string.HN));
        countries.add(new Country("505", "NI", R.string.NI));
        countries.add(new Country("506", "CR", R.string.CR));
        countries.add(new Country("507", "PA", R.string.PA));
        countries.add(new Country("508", "PM", R.string.PM));
        countries.add(new Country("509", "HT", R.string.HT));
        countries.add(new Country("590", "GP", R.string.GP));
        countries.add(new Country("591", "BO", R.string.BO));
        countries.add(new Country("592", "GY", R.string.GY));
        countries.add(new Country("593", "EC", R.string.EC));
        countries.add(new Country("594", "GF", R.string.GF));
        countries.add(new Country("595", "PY", R.string.PY));
        countries.add(new Country("596", "MQ", R.string.MQ));
        countries.add(new Country("597", "SR", R.string.SR));
        countries.add(new Country("598", "UY", R.string.UY));
        countries.add(new Country("599", "CW", R.string.CW));
        countries.add(new Country("599", "BQ", R.string.BQ));
        countries.add(new Country("670", "TL", R.string.TL));
        countries.add(new Country("672", "NF", R.string.NF));
        countries.add(new Country("673", "BN", R.string.BN));
        countries.add(new Country("674", "NR", R.string.NR));
        countries.add(new Country("675", "PG", R.string.PG));
        countries.add(new Country("676", "TO", R.string.TO));
        countries.add(new Country("677", "SB", R.string.SB));
        countries.add(new Country("678", "VU", R.string.VU));
        countries.add(new Country("679", "FJ", R.string.FJ));
        countries.add(new Country("680", "PW", R.string.PW));
        countries.add(new Country("681", "WF", R.string.WF));
        countries.add(new Country("682", "CK", R.string.CK));
        countries.add(new Country("683", "NU", R.string.NU));
        countries.add(new Country("685", "WS", R.string.WS));
        countries.add(new Country("686", "KI", R.string.KI));
        countries.add(new Country("687", "NC", R.string.NC));
        countries.add(new Country("688", "TV", R.string.TV));
        countries.add(new Country("689", "PF", R.string.PF));
        countries.add(new Country("690", "TK", R.string.TK));
        countries.add(new Country("691", "FM", R.string.FM));
        countries.add(new Country("692", "MH", R.string.MH));
        countries.add(new Country("850", "KP", R.string.KP));
        countries.add(new Country("852", "HK", R.string.HK));
        countries.add(new Country("853", "MO", R.string.MO));
        countries.add(new Country("855", "KH", R.string.KH));
        countries.add(new Country("856", "LA", R.string.LA));
        countries.add(new Country("880", "BD", R.string.BD));
        countries.add(new Country("886", "TW", R.string.TW));
        countries.add(new Country("960", "MV", R.string.MV));
        countries.add(new Country("961", "LB", R.string.LB));
        countries.add(new Country("962", "JO", R.string.JO));
        countries.add(new Country("963", "SY", R.string.SY));
        countries.add(new Country("964", "IQ", R.string.IQ));
        countries.add(new Country("965", "KW", R.string.KW));
        countries.add(new Country("966", "SA", R.string.SA));
        countries.add(new Country("967", "YE", R.string.YE));
        countries.add(new Country("968", "OM", R.string.OM));
        countries.add(new Country("970", "PS", R.string.PS));
        countries.add(new Country("971", "AE", R.string.AE));
        countries.add(new Country("972", "IL", R.string.IL));
        countries.add(new Country("973", "BH", R.string.BH));
        countries.add(new Country("974", "QA", R.string.QA));
        countries.add(new Country("975", "BT", R.string.BT));
        countries.add(new Country("976", "MN", R.string.MN));
        countries.add(new Country("977", "NP", R.string.NP));
        countries.add(new Country("992", "TJ", R.string.TJ));
        countries.add(new Country("993", "TM", R.string.TM));
        countries.add(new Country("994", "AZ", R.string.AZ));
        countries.add(new Country("995", "GE", R.string.GE));
        countries.add(new Country("996", "KG", R.string.KG));
        countries.add(new Country("998", "UZ", R.string.UZ));
        countries.add(new Country("1242", "BS", R.string.BS));
        countries.add(new Country("1246", "BB", R.string.BB));
        countries.add(new Country("1264", "AI", R.string.AI));
        countries.add(new Country("1268", "AG", R.string.AG));
        countries.add(new Country("1284", "VG", R.string.VG));
        countries.add(new Country("1340", "VI", R.string.VI));
        countries.add(new Country("1345", "KY", R.string.KY));
        countries.add(new Country("1441", "BM", R.string.BM));
        countries.add(new Country("1473", "GD", R.string.GD));
        countries.add(new Country("1649", "TC", R.string.TC));
        countries.add(new Country("1664", "MS", R.string.MS));
        countries.add(new Country("1670", "MP", R.string.MP));
        countries.add(new Country("1671", "GU", R.string.GU));
        countries.add(new Country("1684", "AS", R.string.AS));
        countries.add(new Country("1721", "SX", R.string.SX));
        countries.add(new Country("1758", "LC", R.string.LC));
        countries.add(new Country("1767", "DM", R.string.DM));
        countries.add(new Country("1784", "VC", R.string.VC));
        countries.add(new Country("1868", "TT", R.string.TT));
        countries.add(new Country("1869", "KN", R.string.KN));
        countries.add(new Country("1876", "JM", R.string.JM));
    }
}
