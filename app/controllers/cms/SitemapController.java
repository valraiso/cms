package controllers.cms;

import java.io.File;
import play.Play;
import play.mvc.Controller;

import plugins.cms.navigation.*;
import models.cms.*;
import java.util.*;
import java.math.BigDecimal;
import play.i18n.Lang;

/**
 * @author benoit
 */
public class SitemapController extends Controller {

	public static class SitemapNode {
		
		public String url;
		public String frequency;
		public BigDecimal priority;

		public boolean show = false;

		public List<SitemapNode> childs;
	}

	public static void index(String lang){
		
		String host    = request.host;
		Domain domain  = NavigationCache.getDomain(request.host);
		boolean strict = false;

		if (domain != null ) {

			if (lang == null){
				lang = domain.defaultLocale;
			}
			else if (!domain.supportedLocales.contains(lang)){
				notFound();	
			}
			
			if (!lang.equals(domain.defaultLocale)) {
				strict = true;
			}
		}
		else if (lang == null){
			lang = Lang.get();
		}

		SitemapNode	node = explore(NavigationCache.get("/"), lang, strict);

		renderTemplate("cms/sitemap.xml", host, node);
	}

	private static SitemapNode explore (NavigationItem item, String lang, boolean strict){
		
		SitemapNode node = new SitemapNode();
		SeoParameter seo = findSeo(item, lang);

		if (seo != null && seo.inSitemap && item.active){

			node.show 	   = true;
			node.frequency = seo.frequency;
			node.priority  = seo.priority;
		}

		node.url = findUrl(item, lang, strict);
		if (node.url == null){

			node.show = false;
		}

		List<SitemapNode> nodes = new ArrayList<SitemapNode>();
		for (NavigationItem i : item.children){
			
			SitemapNode n = explore(i, lang, strict);
			if (n != null){
				nodes.add(n);
			}
		}
		node.childs = nodes;

		return node;
	}

	private static String findUrl(NavigationItem item, String lang, boolean strict){
        
        NavigationMappedItem mappedItem = NavigationCache.getReverseMappedItem(lang, item.path);
        if (mappedItem != null){

        	return mappedItem.destination;
        }
        else if (strict){
        	return null;
        }

        return item.path;
    }

    private static SeoParameter findSeo(NavigationItem item, String lang){

    	SeoParameter seo = SeoParameter.findByPathAndLang(item.path, lang);
        if (seo == null && item.navigationPlugin != null) {
            
            seo = item.navigationPlugin.findSeoParameter(item.path, lang);
        }

        return seo;
    }
}
