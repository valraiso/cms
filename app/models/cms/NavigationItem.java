package models.cms;

import java.util.List;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;
import play.db.jpa.Model;
import plugins.cms.navigation.NavigationPlugin;

/** 
 * @author benoit
 */
@Entity
@Table(name="cms_navigation_item",
       uniqueConstraints=@UniqueConstraint(columnNames={"path"}))
public class NavigationItem extends Model {

    @Column(nullable=false)
    public long position = 0;
    
    @Column(nullable=false)
    public boolean active = true;
    
    @Column(nullable=false,length=64)
    public String name;
    
    @Column(nullable=false,length=128)
    public String path;
    
    @Column(nullable=false)
    public boolean volatil = false;
    
    @ManyToOne
    public NavigationItem parent;
    
    @Transient
    public NavigationPlugin navigationPlugin = null;

    @OneToMany(mappedBy = "parent",fetch=FetchType.EAGER)
    @OrderBy("position,name")
    public List<NavigationItem> children;

    public boolean isParentOf (String path) {

        if (children == null) {
            return false;
        }
        
        for (NavigationItem child : children) {
            if (child.path.equals(path) || child.isParentOf(path)) {
                return true;
            }
        }

        return false;
    }
    
    
    public static NavigationItem findRoot(){
        
        String jpql = " SELECT ni"
                    + " FROM   NavigationItem ni"
                    + " WHERE  ni.parent IS NULL";

        JPAQuery query = NavigationItem.find(jpql);
        
        return query.first();
    }
    
    public static NavigationItem findByPath(String path){
        
        String jpql = " SELECT ni"
                    + " FROM   NavigationItem ni"
                    + " WHERE  ni.path = :path";

        JPAQuery query = NavigationItem.find(jpql);
        query.bind("path", path);
        
        return query.first();
    }
    
    public static List<NavigationItem> findByParent(NavigationItem item){
        
        String oql = "SELECT n "
                    + " FROM NavigationItem n"
                    + " WHERE n.parent";
        
        if (item == null){
            
            oql += " is null";
        }
        else {
            oql += " = :parent";
        }
        
        oql += " ORDER BY n.position";
        
        JPAQuery query = NavigationItem.find(oql);
            
        if (item != null){
            query.bind("parent", item);
        }
        
        return query.fetch();
    }
    
    public static List<NavigationItem> findByParentAndPos(NavigationItem item, Long pos){
        
        String oql = "SELECT n "
                    + " FROM NavigationItem n"
                    + " WHERE n.parent = :parent"
                    + "     AND n.position >= :position";
        
        oql += " ORDER BY n.position";
        
        JPAQuery query = NavigationItem.find(oql);
        query.bind("parent", item);
        query.bind("position",  pos);
        
        return query.fetch();
    }
}
