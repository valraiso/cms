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

/**
 * @author benoit
 */
public class NavigationCache {

	private static Map<String, Domain>         domains		   = new HashMap<String, Domain>();
	
    private static List<NavigationPlugin>      plugins         = new ArrayList<NavigationPlugin>();
    
    private static Map<String, NavigationItem> items           = new HashMap<String, NavigationItem>();
    private static Map<String, VirtualPage>    virtualPages    = new HashMap<String, VirtualPage>();
    
    private static Map<String, Map<String, NavigationMappedItem>> mappedItemsByLangs = new HashMap<String, Map<String, NavigationMappedItem>>();
    
    
    public static void init() {

		initDomains();
		
        initVirtualPage();
        
        initNavigationItem();
        
        initNavigationMappedItem();
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
        createItemsForNavigationItems(roots);
    }
    
    public static void initNavigationMappedItem(){
        
        mappedItemsByLangs.clear();
        
        List<NavigationMappedItem> navigationMappedItems = NavigationMappedItem.findAll();
        for (NavigationMappedItem mappedItem : navigationMappedItems){
            
            String lang = mappedItem.language;
            
            Map<String,NavigationMappedItem> mappedItemsByLang = mappedItemsByLangs.get(lang);
            if (mappedItemsByLang == null){
                
                mappedItemsByLang = new HashMap<String, NavigationMappedItem>();
            }
            
            mappedItemsByLang.put(mappedItem.destination, mappedItem);
            mappedItemsByLangs.put(lang, mappedItemsByLang);
        }
    }
    
    public static NavigationItem get(String path) {
    
        return items.get(path);
    }

    private static void createItemsForNavigationItems(List<NavigationItem> children) {

        for (NavigationItem currentChild : children) {

            List<NavigationItem> currentChildren = currentChild.children;
            boolean createdByPlugin = false;

            if (currentChildren == null || currentChildren.isEmpty()) {

                createdByPlugin = createItemsByPlugin(currentChild);
            }
            if (!createdByPlugin) {

                String currentPath = currentChild.path;

                createItemsForNavigationItems(currentChildren);
                NavigationItem.em().detach(currentChild);
                items.put(currentPath, currentChild);
            }
        }
    }

    private static boolean createItemsByPlugin(NavigationItem parent) {

        String parentPath = parent.path;
        NavigationPlugin plugin = findPlugin(parentPath);

        if (plugin != null) {

            List<NavigationItem> children;

            //Good practice : Set Transient field in findCurrentNavigationItem implementation
            parent = plugin.findCurrentNavigationItem(parent, items);
            children = parent.children;

            if (children != null && !children.isEmpty()) {

                NavigationItem firstChild = children.get(0);
                NavigationPlugin navigationPlugin = firstChild.navigationPlugin;

                if (navigationPlugin == null) {

                    setPlugin(parent, plugin);
                }
            }

            NavigationItem.em().detach(parent);
            items.put(parentPath, parent);

            return true;
        }

        return false;
    }

    private static void setPlugin(NavigationItem parent, NavigationPlugin plugin) {

        if (parent == null || plugin == null) {

            return;
        }

        List<NavigationItem> brothers = parent.children;

        for (NavigationItem item : brothers) {

            List<NavigationItem> children = item.children;

            item.navigationPlugin = plugin;

            if (children != null && !children.isEmpty()) {

                setPlugin(item, plugin);
            }
        }
    }
    
    /**
     * Loads the plugin matching the requestedResource
     * 
     * @param contextPath
     * @param resource
     * @return the plugin
     */
    public static NavigationPlugin findPlugin (String resource) {
        
        for (NavigationPlugin plugin : plugins) {
            
            String path = plugin.getPath();
                        
            if (resource.startsWith(path)) {
                return plugin;
            }
        }
        
        return null;
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
