package models.cms;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import play.db.jpa.Model;

/**
 * @author benoit
 */
@Entity
@Table(name="cms_role")
public class Role extends Model{
    
    @Column(nullable=false,length=32)
    public String name;

}
