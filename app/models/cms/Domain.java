package models.cms;

import java.util.List;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.JoinTable;
import javax.persistence.Table;
import play.db.jpa.Model;
import java.util.*;

/**
 * @author benoit
 */
@Entity
@Table(name="cms_domain")
public class Domain extends Model {
    
    @Column(nullable=false, length=255, unique=true)
    public String host;

    @Column(nullable=false, length=5)
    public String defaultLocale;

    @Column(nullable=false)
    public boolean doTracking = false;

    @ElementCollection
    @JoinTable(name="cms_supported_locale")
    public List<String> supportedLocales;

    public String getDefaultLang(){
        if (defaultLocale == null||defaultLocale.isEmpty()||!defaultLocale.contains("_")){
            return null;
        }
        return defaultLocale.split("_")[0];
    }
    
    public List<String> getSupportedLang(){
        if (this.supportedLocales == null){
            return null;
        }
        List<String> supportedLang = new ArrayList<String>();
        for (String current : supportedLocales){
            if (current != null && !current.isEmpty() && current.contains("_")){
                supportedLang.add(current.split("_")[0]);
            }
        }
        return supportedLang;
    }
}