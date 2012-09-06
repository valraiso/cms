package plugins.cms;

import java.util.*;
import plugins.cms.navigation.NavigationCache;
import models.cms.*;
import play.i18n.Lang;
import play.mvc.Http;
import play.mvc.Router.ActionDefinition;
import play.mvc.Scope;
import play.mvc.Http.Request;

/**
 * @author benoit
 */
public class Tag {
    
    public static String url(ActionDefinition actionDefinition){
        
        String url = actionDefinition.url;
        
        return url;
    }
    
    public static String url(String path){

        String lang = Lang.get();
        NavigationMappedItem mappedItem = NavigationCache.getReverseMappedItem(lang, path);
        if (mappedItem != null){
            
            return mappedItem.destination;
        }

        return path;
    }

    public static String fullUrl(String path) {
        
        Request request = Request.current();
        String host = "http://"+ request.host;
        if (path == null || path.isEmpty() ){
            return host;
        }
        if (!path.startsWith("/")){
            path = "/" + path;
        }

        return host + Tag.url( path);
    }
    
    public static String staticeditor(String code){
        
        String     lang        = Lang.get();
        CmsContext cmsContext  = CmsContext.current();
        Editor     editor      = Editor.findStaticByCodeAndLanguage(code, lang);
        String     content     = "";
        String     htmlContent = "";

        if (isCmsEditingAuthorized() && !cmsContext._tagGenerated ){
            
            content += generateCommon();
            cmsContext._tagGenerated = true;
        }

        if (editor != null){

            htmlContent += editor.content;
        }

        if (isCmsEditingAuthorized()){
            
            htmlContent = "<div class=\"cms_editor\" data-fixed="true" data-code=\""+code+"\">" + htmlContent;
            
            if ((editor == null || editor.content.isEmpty())){

                htmlContent += CmsContext.Constant.CMS_EDITOR_DEFAULT;
            }
            
            htmlContent += "</div>";
        }
        
        return content + htmlContent;
    }
    
    public static String editor(String code){
        
        Http.Request request = Http.Request.current();
        
        String path = request.path;
        String lang = Lang.get();
        
        String content = "";
        
        CmsContext cmsContext = CmsContext.current();

        if (isCmsEditingAuthorized() && !cmsContext._tagGenerated ){
            
            content += generateCommon();
            cmsContext._tagGenerated = true;
        }
        
        Editor editor = Editor.findByPathAndCodeAndLanguage(path, code, lang);

        String htmlContent="";        
        if (editor != null){
            htmlContent += editor.content;
        }

        if (isCmsEditingAuthorized()){
            
            htmlContent = "<div class=\"cms_editor\" data-code=\""+code+"\">" + htmlContent;
            
            if ((editor == null || editor.content.isEmpty())){
                htmlContent += CmsContext.Constant.CMS_EDITOR_DEFAULT;
            }
            
            htmlContent += "</div>";
        }
        
        return content + htmlContent;
    }
    
    public static String login(){
        
        String scripts = "";
        
        CmsContext cmsContext = CmsContext.current();
        if (!cmsContext._tagGenerated){
            scripts = generateCommon();
            cmsContext._tagGenerated = true;
        }
        
        return scripts;
    }
    
    public static String translate(String code){
        
        String lang = Lang.get();
        
        Translation translation = translate(code, lang);
        
        if (translation == null){
            
            return "??"+code+"??";
        }
        
        return translation.value;
    }
    
    public static Translation translate(String code, String lang){
        
        Translation translation = Translation.findByCodeAndLanguage(code, lang);
        
        return translation;
    }
    
    public static NavigationItem rootNavigationItem(){
        
        return NavigationCache.get("/");

        /*
        List<NavigationItem> items = NavigationItem.findByParent(null);
        
        if (items.isEmpty()){
            return null;
        }
        
        return items.get(0);
        */
    }
    public static List<NavigationItem> getActiveChildrens(NavigationItem item){

        return getActiveChildrens(item, true);
    }

    public static List<NavigationItem> getActiveChildrens(NavigationItem item, Boolean includePlugins){
  
        if (item == null){
            return null;
        }

        if (includePlugins == null){
            includePlugins = true;
        }

        List<NavigationItem> childs = item.getChildren();

        List<NavigationItem> result = new ArrayList<NavigationItem>();
        for (NavigationItem child : childs){
            if ((child.active || isCmsUserLogged()) && (includePlugins || (!includePlugins && child.navigationPlugin == null))) {
                result.add( child );
            }
        }
        return result;
    }
    
    private static String generateCommon(){
        
        Http.Request request = Http.Request.current();
        
        String path     = request.path;
        String lang     = Lang.get();
        //String logged   = "true";
        
        StringBuilder value = new StringBuilder();

        value.append("<link rel=\"stylesheet\" type=\"text/css\" href=\"/public/javascripts/elrte-1.3/css/elrte.min.css\" media=\"screen\" charset=\"utf-8\"/>");
        value.append("<link rel=\"stylesheet\" type=\"text/css\" href=\"/public/javascripts/elfinder-1.2/css/elfinder.css\" media=\"screen\" charset=\"utf-8\"/>");
        value.append("<link rel=\"stylesheet\" type=\"text/css\" href=\"/public/cms/cms.css\"/>");
        value.append("<script type=\"text/javascript\" src=\"/public/cms/depend.js\"></script>");
        value.append("<script type=\"text/javascript\" src=\"/public/cms/cms.js\"></script>");
        value.append("<script type=\"text/javascript\">");

        value.append("cms.requestedResource = '").append(path).append("';");
        value.append("cms.language = '").append(lang).append("';");
        value.append("cms.logged = ").append(isCmsUserLogged()).append(";");
        value.append("</script>");
        
        return value.toString();
    }
    
    private static boolean isCmsEditingAuthorized(){
        
        CmsContext cmsContext = CmsContext.current();
        if (cmsContext.isCmsEditingAuthorized == null){
            
            cmsContext.isCmsEditingAuthorized = false;
            
            String userid = Scope.Session.current().get("cms_userid");
            if (userid != null){
                
                User user       = User.findById(Long.parseLong(userid));
                Role cmsEditor  = Role.find("byName", "cms_editor").first();
                
                if (user.roles.contains(cmsEditor)){
                    
                    cmsContext.isCmsEditingAuthorized = true;
                }
            }
        }
        
        return cmsContext.isCmsEditingAuthorized;
    }
    
    private static boolean isCmsUserLogged(){
        
        String userid = Scope.Session.current().get("cms_userid");
        
        return (userid != null);
    }
}
