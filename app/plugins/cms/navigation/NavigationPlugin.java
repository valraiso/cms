package plugins.cms.navigation;


import java.util.Map;
import models.cms.NavigationItem;
import models.cms.NavigationMappedItem;
import models.cms.SeoParameter;

/**
 * <b>Nemo NavigationPlugin abstract class</b>
 * 
 * <p>
 * NavigationPlugin is the class plugin implementors must derive.
 * </p>
 * 
 * <p>
 * A NavigationPlugin is declared in the navigation_item_plugin table.
 * It's referenced using a contextPath and an attachment path.<br/>
 * Those value are automaticaly injected in the plugin instance.
 * </p>
 * 
 * @author cedric
 */
public abstract class NavigationPlugin {
    
    /**
     * The path of the node this plugin is attached to.
     */
    protected String attachmentPath;
       
    /**
     * <b>Navigation</b>
     * 
     * <p>
     * Must return a root node for the tree managed by the plugin
     * implementation, wrapped in a NavigationItem.
     * </p>
     * 
     * @return the root navigation item (with null id)
     */
    public abstract NavigationItem findRootNavigationItem();
    
    /**
     * <b>Navigation</b>
     * 
     * <p>
     * Must return the given node having the managed tree filled by
     * the plugin implementation, wrapped in a NavigationItem.
     * 
     * The Transient field navigationPlugin in all children should be completed by the plugin.
     * --> navigationItem.setNavigationPlugin(this);
     * </p>
     * 
     * @param navigationItem the plugin ty fill
     * @return the given navigation item filled by plugin (with null id)
     */
    public abstract NavigationItem findCurrentNavigationItem(NavigationItem navigationItem, Map<String, NavigationItem> items);
    
    /**
     * <b>NavigationMappedItem</b>
     * 
     * <p>
     * Returns a mapped item depending on the destiation URL (pretty URL).<br/>
     * If this plugin handles this resource, it must return a mapped item, 
     * otherwise it must return null.
     * </p>
     * 
     * @param destination the pretty URL
     * @param language the language
     * @return null if this plugin does not handle this resource,a mapped item  (with null id) otherwise
     */
    public abstract NavigationMappedItem findNavigationMappedItemByDestination(String destination, String language);
    
    /**
     * <b>NavigationMappedItem</b>
     * 
     * <p>
     * Returns a mapped item depending on the source URL (technical URL).<br/>
     * If this plugin handles this resource, it must return a mapped item, 
     * otherwise it must return null.
     * </p>
     * 
     * @param source the technical url
     * @param language the language
     * @return null if this plugin does not handle this resource, a mapped item (with null id) otherwise
     */
    public abstract NavigationMappedItem findNavigationMappedItemBySource(String source, String language);
    
    /**
     * <b>SeoParameter</b>
     * 
     * <p>
     * Returns a SeoParameter Object containing teh SEO informations for this
     * resource and this language.
     * </p>
     * 
     * @param resource the resource
     * @param languagethe language
     * @return the SeoParameter instance  (with null id)
     */
    public abstract SeoParameter findSeoParameter(String resource, String language);
    

    /**
     * Returns the plugin attachment path
     * @return the path
     */
    public String getPath() {
        return attachmentPath;
    }

    /**
     * Returns the "pathInfo" for this resource (technical URL) by stripping the
     * attachment path.
     * 
     * @param resource the resource (technical URL)
     * @return the remaining path (pathInfo)
     */
    public String getPathInfo(String resource) {
        
        if (!resource.startsWith(attachmentPath)) {
            return null;
        }
        
        return resource.substring(attachmentPath.length(), resource.length());
    }
}
