package models.cms;

import java.util.List;
import javax.persistence.*;
import plugins.cms.navigation.NavigationCache;
import play.db.jpa.Model;
import plugins.cms.navigation.NavigationPlugin;
import java.util.*;
import play.db.jpa.*;


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

    @OrderBy("position,name")
    @OneToMany(mappedBy = "parent",fetch=FetchType.EAGER)
    public List<NavigationItem> children = new ArrayList<NavigationItem>();

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
    
    @PostPersist @PostUpdate
    private void clearcache(){

        if (parent != null){
            EntityManager em = JPA.em();
            if (em.contains(parent)){
                em.refresh(parent);
            }
        }
        NavigationCache.init();
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
        
        oql += " ORDER BY n.position,n.name";
        
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
