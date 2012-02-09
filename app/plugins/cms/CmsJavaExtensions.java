package plugins.cms;


import org.apache.commons.lang.StringEscapeUtils;
import play.templates.JavaExtensions;

/**
 * @author benoit
 */
public class CmsJavaExtensions extends JavaExtensions{
    
    public static String stripSlashes(Object o){
        
        if (o == null){
            return null;
        }
        
        String string = o.toString();
        
        return string.replace("\\\"", "\"").replace("\\'", "'");
    }
    
    public static String unescape(Object o) {
        
        if (o == null){
            return null;
        }
        
        String string = o.toString();
        
        return StringEscapeUtils.unescapeHtml(string);
    }

    public static String crop(String toCrop, int size){

        return crop(toCrop, size, "...");
    }

    public static String crop(String toCrop, int size, String replacement){

        int length = replacement.length();

        if (toCrop == null || toCrop.length() + length < size ){
            return toCrop;
        }
        toCrop = toCrop.substring(0, size - length);
        toCrop = toCrop.concat(replacement);
        return toCrop;
    }
}
