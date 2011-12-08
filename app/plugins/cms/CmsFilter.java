package plugins.cms;


import plugins.cms.navigation.NavigationCache;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.net.URLDecoder;
import models.cms.Domain;
import models.cms.NavigationItem;
import models.cms.NavigationMappedItem;
import models.cms.SeoParameter;
import models.cms.User;
import models.cms.VirtualPage;
import org.hibernate.Filter;
import play.Logger;
import play.Play;
import play.PlayPlugin;
import play.db.jpa.JPA;
import play.i18n.Lang;
import play.mvc.Http.Request;
import play.mvc.Router;
import play.mvc.Router.Route;
import play.mvc.Scope;
import play.mvc.Scope.RenderArgs;


/**
 * @author benoit
 */
public class CmsFilter extends PlayPlugin {

    private String cmsSkipPath = null;
    
    @Override
    public void routeRequest(Request request) {
        
		String resource = request.path;
		if (skipRequest(resource)){
			return;
		}
        
        CmsContext.current.set(new CmsContext());
        String lang     = Lang.get();
        
        if (cmsSkipPath != null && resource.startsWith(cmsSkipPath)){
            return;
        }
        
		/**
		 * handle domain
		 */
		Domain domain = NavigationCache.getDomain(request.host);
		if (domain == null){
			
			Logger.error("Domain not found: " + request.host);
		}
		
        VirtualPage virtualPage = NavigationCache.getVirtualPage(resource);
        if (virtualPage != null){
            
            // handle virtual page
            request.path = Router.reverse("cms.CmsController.virtualPage").url;
            return;
        }
        
        NavigationMappedItem mappedItem = NavigationCache.getMappedItem(lang, resource);
        if (mappedItem != null){
            
            if (mappedItem.redirect){
                
                request.path = Router.reverse("cms.CmsController.redirectMappedItem").url;
                return;
            }
            else {
                
                request.path = mappedItem.source;
            }
        }
		else if (domain != null && !lang.equals(domain.defaultLocale) && !resource.equals("/")) {
			
			request.path = Router.reverse("cms.CmsController.pageNotFound").url;
		}
    }
    
    @Override
    public void onRequestRouting(Route route) {
        
        Request request = Request.current();
        
		if (skipRequest(request.path)){
			return;
		}
		
        try {
            
            String uri      = request.url;
            String encoding = request.encoding;
            
            final int i = uri.indexOf("?");
            
            String path = URLDecoder.decode(uri, encoding);
            if (i != -1) {
                path = URLDecoder.decode(uri.substring(0, i), encoding);   
            }

            request.path = path;
            //Logger.info("reset path to :" + path);
            
        } catch (UnsupportedEncodingException ex) {
            
            Logger.error("[CustomRoutingPlugin] Unable to reset request.path");
        }
    }
    
    @Override
    public void beforeActionInvocation(Method method) {
        
		RenderArgs	renderArgs	= RenderArgs.current();
		CmsContext	cmsContext	= CmsContext.current();
		Request		request		= Request.current();
        
		renderArgs.put("cms", cmsContext);
		
		String resource = request.path;
		if (skipRequest(resource)){
			
			return;
		}
		
		String lang     = Lang.get();
		
		
        /**
         * handle navigation
         */
        NavigationMappedItem mappedItem = NavigationCache.getMappedItem(lang, resource);
        if (mappedItem != null && !mappedItem.redirect){
            
            resource = mappedItem.source;
        }
		
        renderArgs.put("__REQUESTED_RESOURCE", resource); //deprecated
        cmsContext.requestedResource = resource;
        
        NavigationItem item = NavigationCache.get(resource);
        if (item != null){
            renderArgs.put("__CURRENT_NAVIGATION_ITEM", item); //deprecated
            cmsContext.currentNavigationItem = item;
        }
        
        /**
         * handle seo parameter
         */        
        SeoParameter seo = SeoParameter.findByPathAndLang(resource, lang);
        if (seo == null 
                && item != null && item.navigationPlugin != null) {
            
            seo = item.navigationPlugin.findSeoParameter(resource, lang);
        }
        renderArgs.put(CmsContext.Constant.CMS_SEO_PARAMETER, seo);
        
        
        /**
         * handle hibernate filter
         */
        try {
            
            org.hibernate.Session hibernateSession = ((org.hibernate.Session)JPA.em().getDelegate());
        
            Filter filter = hibernateSession.enableFilter("langFilter");
            if (filter != null){
                filter.setParameter("langFilterParam", lang);
            }
        }
        catch (Exception ex) {}
    }

    @Override
    public void onConfigurationRead() {
        
        cmsSkipPath = (String) Play.configuration.getProperty("cms.skipPath");
    }
	
	private boolean skipRequest (String path){
		
		return (path.startsWith("/public")
				|| path.startsWith("/--cms/")
        );
	}
}
