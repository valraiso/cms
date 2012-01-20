package controllers.cms;

import models.cms.*;
import java.util.*;
import java.io.File;
import play.Play;
import play.mvc.Controller;
import plugins.cms.navigation.*;

/**
 * @author benoit
 */
public class NavDebugController extends Controller {
    
    public static class  NavDTO {
        
        public NavigationItem item;
        public NavigationMappedItem mappedItem;

        public List<NavDebugController.NavDTO> childrens;
    }

    public static void index(String lang) {
        
        if (lang == null){
            
            lang = "fr";
        }
        
        NavDTO root = buildNav("/", lang);

        renderTemplate("debug/index.html", root);
    }

    private static NavDTO buildNav(String path, String lang){
        
        NavigationItem item = NavigationCache.get(path);
        if (item != null){
            
            NavDebugController.NavDTO dto = new NavDebugController.NavDTO();
            dto.item = item;
            dto.mappedItem = NavigationCache.getReverseMappedItem(lang, path);

            if (item.children != null){
                
                List<NavDebugController.NavDTO> childs = new ArrayList<NavDebugController.NavDTO>();
                for (NavigationItem i : item.children){
                    
                    childs.add(buildNav(i.path, lang));
                }
                dto.childrens = childs;

            }

            return dto;
        }

        return null;
    }
}
