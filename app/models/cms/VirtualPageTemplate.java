package models.cms;

import javax.persistence.Entity;
import javax.persistence.Table;
import play.db.jpa.Model;

/**
 * @author benoit
 */
@Entity
@Table(name="cms_virtual_page_template")
public class VirtualPageTemplate extends Model{
    
    public String name;
    
    public String view;
}
