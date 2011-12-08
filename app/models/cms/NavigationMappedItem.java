package models.cms;

import java.util.List;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import play.db.jpa.Model;

/**
 * @author benoit
 */
@Entity
@Table(name="cms_navigation_mapped_item",uniqueConstraints=@UniqueConstraint(columnNames={"source", "language"}))
public class NavigationMappedItem extends Model {

    @Column(nullable=false,length=128)
    public String source;

    @Column(nullable=false,length=128)
    public String destination;

    @Column(nullable=true,length=2)
    public String language;

    @Column(nullable=false)
    public boolean redirect = false;
    
    public static List<NavigationMappedItem> findBySource(String path){
        
        return NavigationMappedItem.find("bySource", path).fetch();
    }
    
    public static NavigationMappedItem findBySourceAndLang(String source, String lang){
        
        String oql = " SELECT nmi"
                    + " FROM   NavigationMappedItem nmi"
                    + " WHERE  nmi.source = :source"
                    + "     AND nmi.language = :lang";

        JPAQuery query = NavigationMappedItem.find(oql);
        
        query.bind("source", source);
        query.bind("lang", lang);
        
        return query.first();
    }
}
