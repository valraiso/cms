package plugins.cms.navigation;


import java.util.*;
import models.cms.NavigationItem;
import models.cms.NavigationMappedItem;
import models.cms.SeoParameter;

public abstract class NavigationPlugin {

	public abstract Map<NavigationItem, List<NavigationMappedItem>> buildNavigation(NavigationItem item);

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
}