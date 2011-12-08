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
}
