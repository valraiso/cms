package plugins.cms.navigation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import models.cms.Domain;
import models.cms.NavigationItem;
import models.cms.NavigationMappedItem;
import models.cms.VirtualPage;
import play.Logger;
import play.Play;
import play.classloading.ApplicationClasses;
import javax.persistence.*;
import play.db.jpa.*;

/**
 * @author benoit
 */
public class NavigationCache {

	private static Map<String, Domain>         domains		   = new HashMap<String, Domain>();
	
    private static List<NavigationPlugin>      plugins         = new ArrayList<NavigationPlugin>();
    
    private static Map<String, NavigationItem> items           = new HashMap<String, NavigationItem>();
    private static Map<String, VirtualPage>    virtualPages    = new HashMap<String, VirtualPage>();
    
    private static Map<String, Map<String, NavigationMappedItem>> mappedItemsByLangs = new HashMap<String, Map<String, NavigationMappedItem>>();
    private static Map<String, Map<String, NavigationMappedItem>> reverseMappedItemsByLangs = new HashMap<String, Map<String, NavigationMappedItem>>();
    
    
    public static void init() {

		initDomains();
		
        initVirtualPage();
        
        initNavigationItem();
        
        initNavigationMappedItem();

        initPluginsNavigation();
    }
	
	public static void initDomains(){
		
		domains.clear();
		
		List<Domain> dom = Domain.findAll();
		for (Domain d : dom){
			
			domains.put(d.host, d);
		}
	}
    
    public static void initVirtualPage(){
        
        virtualPages.clear();
        
        List<VirtualPage> vp = VirtualPage.findAll();
        for (VirtualPage virtualPage : vp){
            
            virtualPages.put(virtualPage.path, virtualPage);
        }
    }
    
    public static void initNavigationItem(){
        
        items.clear();
        
        List<NavigationItem> roots = NavigationItem.findByParent(null);
        for (NavigationItem item : roots){

            createItemsForNavigationItems(item);
        }
        
    }
    
    public static void initNavigationMappedItem(){
        
        mappedItemsByLangs.clear();
        reverseMappedItemsByLangs.clear();

        List<NavigationMappedItem> navigationMappedItems = NavigationMappedItem.findAll();
        for (NavigationMappedItem mappedItem : navigationMappedItems){
            
            String lang = mappedItem.language;
            
            Map<String,NavigationMappedItem> mappedItemsByLang        = mappedItemsByLangs.get(lang);
            Map<String,NavigationMappedItem> reverseMappedItemsByLang = reverseMappedItemsByLangs.get(lang);
            if (mappedItemsByLang == null){
                
                mappedItemsByLang = new HashMap<String, NavigationMappedItem>();
            }
            if (reverseMappedItemsByLang == null){
                
                reverseMappedItemsByLang = new HashMap<String, NavigationMappedItem>();
            }
            
            mappedItemsByLang.put(mappedItem.destination, mappedItem);
            mappedItemsByLangs.put(lang, mappedItemsByLang);

            reverseMappedItemsByLang.put(mappedItem.source, mappedItem);
            reverseMappedItemsByLangs.put(lang, reverseMappedItemsByLang);
        }
    }

    public static void initPluginsNavigation(){
        
        Map<String, NavigationItem> navItems = new HashMap<String, NavigationItem>();

        for (NavigationItem item : items.values()){       
        
            for (NavigationPlugin plugin : plugins){
                
                Map<NavigationItem, List<NavigationMappedItem>> nav = plugin.buildNavigation(item);

                if (nav != null){
                    
                    List<NavigationItem> childrens = new ArrayList(nav.keySet());
                    item.addChilds(childrens);

                    for (NavigationItem i : childrens){
                        
                        navItems.put(i.path, i);
                        List<NavigationMappedItem> mappedItems = nav.get(i);
                        if (mappedItems != null){
                            
                            for (NavigationMappedItem m : mappedItems){
                            
                                String lang = m.language;
                
                                Map<String,NavigationMappedItem> mappedItemsByLang        = mappedItemsByLangs.get(lang);
                                Map<String,NavigationMappedItem> reverseMappedItemsByLang = reverseMappedItemsByLangs.get(lang);
                                if (mappedItemsByLang == null){
                                    
                                    mappedItemsByLang = new HashMap<String, NavigationMappedItem>();
                                }
                                if (reverseMappedItemsByLang == null){
                                    
                                    reverseMappedItemsByLang = new HashMap<String, NavigationMappedItem>();
                                }
                                
                                mappedItemsByLang.put(m.destination, m);
                                mappedItemsByLangs.put(lang, mappedItemsByLang);

                                reverseMappedItemsByLang.put(m.source, m);
                                reverseMappedItemsByLangs.put(lang, reverseMappedItemsByLang);
                            }
                        }
                    }
                }
            }
        }
        items.putAll(navItems);
    }
    
    private static void createItemsForNavigationItems(NavigationItem item) {


        String p = item.parent != null ? String.valueOf(item.parent.id) : "null";
        play.Logger.info("item " + item.id + " -> " + p);

        items.put(item.path, item);

        List<NavigationItem> childrens = item.getChildren();
        for (NavigationItem i : childrens) {
            play.Logger.info("child " + i.id + " -> " + item.id);
            createItemsForNavigationItems(i);
        }

        JPA.em().detach(item);
    }

    public static NavigationItem get(String path) {
    
        return items.get(path);
    }
	
	public static Domain getDomain (String host) {
        
        return domains.get(host);
    }
    
    
    public static VirtualPage getVirtualPage (String resource) {
        
        return virtualPages.get(resource);
    }
    
    public static NavigationMappedItem getMappedItem (String lang, String resource) {
        
        Map<String, NavigationMappedItem> mappedItems = mappedItemsByLangs.get(lang);
        
        if (mappedItems == null){
            
            return null;
        }
        
        return mappedItems.get(resource);
    }
    
    public static NavigationMappedItem getReverseMappedItem (String lang, String resource) {
        
        Map<String, NavigationMappedItem> mappedItems = reverseMappedItemsByLangs.get(lang);
        
        if (mappedItems == null){
            
            return null;
        }
        
        return mappedItems.get(resource);
    }

    public static void loadPlugins(){
        
        for (ApplicationClasses.ApplicationClass c : Play.classes.getAssignableClasses(NavigationPlugin.class)) {
            
            Class<? extends NavigationPlugin> klass = (Class<? extends NavigationPlugin>) c.javaClass;
            
            try {
                
                NavigationPlugin plugin = (NavigationPlugin) klass.newInstance();
                plugins.add(plugin);
                
            } catch (Exception ex) {
                Logger.warn("unable to instanciate plugin " + klass.getName());
            }
        }
    }
}
