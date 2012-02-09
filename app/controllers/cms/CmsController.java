package controllers.cms;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import models.cms.NavigationItem;
import models.cms.NavigationMappedItem;
import models.cms.VirtualPage;
import models.cms.VirtualPageTemplate;
import models.cms.Translation;
import models.cms.SeoParameter;
import models.cms.User;
import play.i18n.Lang;
import play.mvc.Controller;
import play.mvc.Http.Cookie;
import play.templates.JavaExtensions;
import plugins.cms.navigation.NavigationCache;

import plugins.cms.CmsContext;
import java.util.ArrayList;
import java.util.Arrays;
import play.Play;

import play.db.jpa.*;

/**
 * @author benoit
 */
public class CmsController extends Controller {
    
    public static void auth(String cms_email, String cms_password, String cms_logout){
        
        if (cms_logout != null){
            
            session.remove(CmsContext.Constant.CMS_USER);
        }
        
        if (cms_email != null && cms_password != null){
            
            User user = User.find("byEmailAndPassword", cms_email, cms_password).first();
            
            if (user != null){
                
                session.put(CmsContext.Constant.CMS_USER, user.id);
            }
            else {
                session.remove(CmsContext.Constant.CMS_USER);
            }
        }

        redirect(request.headers.get("referer").value());
    }
    
    public static void ClearCache(){
        
        NavigationCache.init();
    }
    
    public static void redirectMappedItem(){
        
        String lang     = Lang.get();
        String resource = request.path;
        
        NavigationMappedItem mappedItem = NavigationCache.getMappedItem(lang, resource);
        
        if (mappedItem != null){
            
            redirect(mappedItem.source);
        }
    }
    
    public static void virtualPage(){
        
        String lang     = Lang.get();
        String resource = request.path;
        
        NavigationMappedItem mappedItem = NavigationCache.getMappedItem(lang, resource);
        if (mappedItem != null){

            resource = mappedItem.source;
        }

        VirtualPage virtualPage = NavigationCache.getVirtualPage(resource);
        if (virtualPage != null){
            
            renderTemplate(virtualPage.view);
        }
    }
	
	public static void pageNotFound(){
				
		notFound();
	}
	
    
    public static void manageNavigation(String path){
        
        if (path == null){
            path = "/";
        }
        
        NavigationItem openedNavItem = NavigationItem.findByPath(path);
        
        renderTemplate("cms/navigation.html", openedNavItem);
    }
    
    public static void create(Long parentid, String name, Integer pos){
        
        Map<String,Object> result = new HashMap<String, Object>();
        boolean status = false;
        
        try {
            NavigationItem parentItem = NavigationItem.findById(parentid);

            String path = JavaExtensions.slugify(name);
            if (parentItem != null){

                path = parentItem.path +(parentItem.path.endsWith("/") ? path : "/" + path);
            }

            NavigationItem navigationItem = new NavigationItem();
            navigationItem.name     = name;
            navigationItem.path     = path;
            navigationItem.parent   = parentItem;
            navigationItem.position = pos;

            navigationItem.save();
            
            result.put("id", navigationItem.id);
            status = true;
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
        
        result.put("status", status);

        renderJSON(result);
    }
    
    public static void rename(Long navid, String newname){
        
        Map<String,Object> result = new HashMap<String, Object>();
        boolean status = false;
        
        NavigationItem navItem= NavigationItem.findById(navid);
        
        if (navItem != null){
            
            List<Translation> translations = Translation.findByCode(navItem.name);
            for (Translation translation : translations){
                
                translation.code = newname;
                translation.save();
            }
            
            navItem.name = newname;
            navItem.save();
            status = true;
        }
        
        result.put("status", status);
        renderJSON(result);
    }
    
    public static void remove(Long navid){
        
        Map<String,Object> result = new HashMap<String, Object>();
        boolean status = false;
        
        NavigationItem navItem= NavigationItem.findById(navid);
        if (navItem != null){
            
            recursRemove(navItem);
            if (navItem.parent != null){
                JPA.em().refresh(navItem.parent);
            }
            NavigationCache.init();
            status = true;
        }
        
        result.put("status", status);
        renderJSON(result);
    }

    private static void recursRemove(NavigationItem item) {
        
        for (NavigationItem i : NavigationItem.findByParent(item)){
            
            recursRemove(i);
        }

        List<NavigationMappedItem> mappedItems = NavigationMappedItem.findBySource(item.path);
        for (NavigationMappedItem mappedItem : mappedItems){
            
            mappedItem.delete();
        }

        VirtualPage vp = VirtualPage.findByPath(item.path);
        if (vp != null){
            
            vp.delete();
        }
        
        item.delete();
    }

    
    public static void move(Long parentid, Long navid, Long pos){
        
        Map<String,Object> result = new HashMap<String, Object>();
        boolean status = false;
        
        try {
            NavigationItem parent = NavigationItem.findById(parentid);
        
            List<NavigationItem> items = NavigationItem.findByParentAndPos(parent, pos);
            int n=0;
            for (NavigationItem ni : items){
                ni.position = pos + n++;
                ni.save();
            }

            NavigationItem navItem= NavigationItem.findById(navid);

            navItem.parent   = parent;
            navItem.position = pos;
            navItem.save();
        }
        catch (Exception ex) {}
        
        result.put("status", status);
        renderJSON(result);
    }
    
    public static void edit(Long navid, Boolean update){
        
        NavigationItem  navItem = NavigationItem.findById(navid);
        List<String>    langs   = getApplicationLangs();
        
        if (update != null){
            
            boolean success = false;
            Map<String,Object> results = new HashMap<String, Object>();
            
            try {
                
                String newpath = params.get("fr_url");
                
                for (String lang : langs){
                    
                    if (!"fr".equals(lang)){
                    
                        /**
                         * Gestion des urls
                         */
                        NavigationMappedItem mappedItem = NavigationMappedItem.findBySourceAndLang(navItem.path, lang);
                        String lang_url = params.get(lang + "_url").trim();

                        if (lang_url.isEmpty()){

                            if (mappedItem != null){
                                mappedItem.delete();
                            }
                        }
                        else {

                            if (mappedItem == null){

                                mappedItem = new NavigationMappedItem();
                                mappedItem.language = lang;
                            }

                            mappedItem.source       = newpath;
                            mappedItem.destination  = lang_url;

                            mappedItem.save();
                        }
                    }
                    
                    /**
                     * Trad de la nav
                     */
                    String lang_trad = params.get(lang + "_trad");
                    Translation translation = Translation.findByCodeAndLanguage(navItem.name, lang);
                    if (translation == null){
                        translation = new Translation();
                        translation.code     = navItem.name;
                        translation.language = lang;
                    }
                    translation.value = lang_trad;
                    translation.save();
                    
                    /**
                     * Gestion du seo
                     */
                    SeoParameter seo = SeoParameter.findByPathAndLang(navItem.path, lang);
                    
                    if (seo == null){
                        
                        seo = new SeoParameter();
                        seo.language = lang;
                    }
                    seo.path        = newpath;
                    
                    seo.title       = JavaExtensions.addSlashes(params.get(lang + "_meta_title"));
                    seo.keywords    = JavaExtensions.addSlashes(params.get(lang + "_meta_keywords"));
                    seo.description = JavaExtensions.addSlashes(JavaExtensions.escape(params.get(lang + "_meta_desc")));
                    seo.robots      = params.get(lang + "_robots");
                    
                    seo.inSitemap   = (params.get(lang + "_insitemap") != null);
                    seo.frequency   = params.get(lang + "_freq");
                    seo.priority    = params.get(lang + "_prio", BigDecimal.class);
                    
                    seo.save();
                }

                VirtualPage vp = VirtualPage.findByPath(navItem.path);
                if (vp != null){
                    
                    vp.path = newpath;
                    vp.save();
                }

                VirtualPageTemplate virtualPageTemplate = null;
                Long virtualpagetemplateid = params.get("virtualpagetemplateid", Long.class);
                if (virtualpagetemplateid != null){

                    virtualPageTemplate = VirtualPageTemplate.findById(virtualpagetemplateid);  
                } 
                if (virtualPageTemplate != null){
                    
                    if (vp == null){
                        vp = new VirtualPage();
                        vp.path = newpath;
                    }
                    vp.view   = virtualPageTemplate.view;
                    vp.action = virtualPageTemplate.action;
                    vp.save();
                }
                else if (virtualpagetemplateid != null && vp != null){
                    
                    vp.delete();
                }
                
                navItem.active = (params.get("navitem_active") != null);
                navItem.path   = newpath;
                navItem.save();
                
                success = true;
            }
            catch (Exception ex) {
                
                Writer stacktrace = new StringWriter();
                PrintWriter printWriter = new PrintWriter(stacktrace);
                ex.printStackTrace(printWriter);
                
                results.put("error", ex.getMessage());
                results.put("stacktrace", stacktrace.toString());
            }
            
            results.put("success", success);
            renderJSON(results);
        }
        else if (navItem != null){
            
            List<VirtualPageTemplate> templates = VirtualPageTemplate.all().fetch();

            VirtualPageTemplate virtualPageTemplate = null;
            VirtualPage virtualPage = VirtualPage.findByPath(navItem.path);
            if (virtualPage != null){

                for (VirtualPageTemplate template : templates){

                    if (template.view.equals(virtualPage.view)){
                        virtualPageTemplate = template;
                        break;
                    }
                }
            }
            
            Cookie tabCookie    = request.cookies.get("cms-edit-nav-last-tab");
            String selectedTab  = (tabCookie == null) ? "#cms_nav_urls" : tabCookie.value;
            
            Cookie langCookie   = request.cookies.get("cms-edit-nav-last-lang");
            String selectedLang = (langCookie == null) ? "fr" : langCookie.value;
            

            Map<String, SeoParameter> seos = new HashMap<String, SeoParameter>();
            Map<String, NavigationMappedItem> mappedItems = new HashMap<String, NavigationMappedItem>();
            for (String lang : langs){

                NavigationMappedItem mappedItem = NavigationMappedItem.findBySourceAndLang(navItem.path, lang);
                mappedItems.put(lang, mappedItem);

                SeoParameter seoParameter = SeoParameter.findByPathAndLang(navItem.path, lang);
                seos.put(lang, seoParameter);
            }

            renderTemplate("cms/nav-edit.html", navItem, virtualPage, virtualPageTemplate, templates, langs, selectedLang, selectedTab, mappedItems, seos);
        }
    }

    private static List<String> getApplicationLangs(){
        
        List<String> langs = new ArrayList<String>();
        String configLangs = Play.configuration.getProperty("application.langs");
        if (configLangs != null){

            langs = Arrays.asList(configLangs.split(","));
        }
        return langs;
    }
}
