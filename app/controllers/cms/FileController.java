package controllers.cms;

import java.io.File;
import play.Play;
import play.mvc.Controller;

/**
 * @author benoit
 */
public class FileController extends Controller {
    
    public static void files(String filepath) {
        
        String rootPath = Play.applicationPath.getAbsolutePath();
        rootPath = rootPath.substring(0, rootPath.lastIndexOf("/"));
        rootPath += "/__files/" + Play.configuration.getProperty("application.name");
        
        File file = new File(rootPath, filepath);
        
        if (file.exists() && file.isFile()){
            renderBinary(file);
        }
        else {
            error(404, "File not Found");
        }
    }
}
