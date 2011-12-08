package plugins.cms;

import plugins.cms.navigation.NavigationCache;
import play.jobs.Job;
import play.jobs.OnApplicationStart;

import play.Play;
import models.cms.User;
import models.cms.Role;

/**
 * @author benoit
 */
@OnApplicationStart
public class ApplicationStart extends Job {

    public void doJob() {

		Role role = Role.find("byName", "cms_editor").first();
    	if (role==null){

    		role = new Role();
    		role.name = "cms_editor";
    		role.save();
    	}

		if (Play.mode.isDev()){

    		User user = User.find("byEmail", "test@test.me").first();
    		if (user == null){

    			user = new User();
    			user.email = "test@test.me";
    			user.password = "test";
    			user.firstName = "John";
    			user.lastName = "Doe";

    			user.roles.add(role);
    			user.save();
    		}
    	}
    	
        NavigationCache.loadPlugins();
        
        NavigationCache.init();
    }
}
