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
@Table(name="cms_translation",uniqueConstraints=@UniqueConstraint(columnNames={"code","language"}))
public class Translation extends Model {
    
    @Column(nullable=false, length=32)
    public String code;

    @Column(nullable=false, length=2)
    public String language;

    @Column(nullable=false, length=2000)
    public String value;
    
    public static Translation findByCodeAndLanguage(String code, String lang){
        
        return Translation.find("byCodeAndLanguage", code, lang).first();
    }
    
    public static List<Translation> findByCode (String code) {
        
        return Translation.find("byCode", code).fetch();
    }
    
    public static List<String> findDistinctLanguages(){
        
        String oql = " SELECT DISTINCT (t.language)"
                    + " FROM   Translation t"
                    + " ORDER BY t.language";

        JPAQuery query = Translation.find(oql);
        
        return query.fetch();
    }
}
