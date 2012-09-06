package models.cms;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import play.db.jpa.Model;

/**
 * @author benoit
 */
@Entity
@Table(name="cms_editor",uniqueConstraints=@UniqueConstraint(columnNames={"path", "code", "language"}))
public class Editor extends Model {

    @Column(length=128)
    public String path;

    @Column(nullable=false, length=32)
    public String code;

    @Column(nullable=false, length=2)
    public String language;

    @Lob
    @Column(nullable=false)
    public String content = "";
    
    
    public static Editor findByPathAndCodeAndLanguage(String path, String code, String lang){
        
        return Editor.find("byPathAndCodeAndLanguage", path, code, lang).first();
    }

    public static Editor findStaticByCodeAndLanguage(String code, String lang){

        String   jpql = "";
        JPAQuery query;

        jpql += " SELECT e";
        jpql += " FROM   Editor e";
        jpql += " WHERE  e.path IS NULL";
        jpql += " AND   e.code     = :code";
        jpql += " AND   e.language = :lang";

        query = Editor.find(jpql);
        query.bind("code", code);
        query.bind("lang", lang);

        return query.first();
    }
}