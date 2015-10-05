package im.actor.generator.generators.java;

public class StringJoin{
    public static String join(String delimeter, String[] strings){
        if(strings!=null && strings.length>0){
            String str = strings[0];
            for (int i = 1; i < strings.length; i++) {
                str = str.concat(delimeter).concat(strings[i]);
            }
            return str;
        }else{
            return "";
        }
    }
}