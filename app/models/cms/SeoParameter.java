package models.cms;

import java.math.BigDecimal;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import play.db.jpa.Model;

/**
 * @author benoit
 */
@Entity
@Table(name="cms_seo_parameter",uniqueConstraints=@UniqueConstraint(columnNames={"path", "language"}))
public class SeoParameter extends Model {

    @Column(nullable=false,length=128)
    public String path = "";

    @Column(nullable=false,length=2)
    public String language;
    
    @Column(nullable=false,length=128)
    public String title = "";

    @Column(nullable=false,length=1000)
    public String description = "";

    @Column(nullable=false,length=1000)
    public String keywords = "";

    @Column(nullable=false,length=255)
    public String robots = "";
    
    @Column(nullable=false,length=8)
    public String frequency = "WEEKLY";

    @Column(scale=1,precision=2,nullable=false)
    public BigDecimal priority = new BigDecimal("0.8");

    @Column(nullable=false)
    public boolean inSitemap = true;
    
    
    public static SeoParameter findByPathAndLang(String path, String lang){
        
        String oql = " SELECT s"
                    + " FROM   SeoParameter s"
                    + " WHERE  s.path = :path"
                    + "     AND s.language = :lang";

        JPAQuery query = NavigationMappedItem.find(oql);
        
        query.bind("path", path);
        query.bind("lang", lang);
        
        return query.first();
    }
}
