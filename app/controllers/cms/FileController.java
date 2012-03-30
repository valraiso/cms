package controllers.cms;

import java.io.File;
import play.Play;
import play.mvc.Controller;

/**
 * @author benoit
 */
public class FileController extends Controller {
    
    public static File getOrCreateUploadFolder(){
        
        String rootPath = Play.applicationPath.getAbsolutePath();
        
        rootPath = rootPath.substring(0, rootPath.lastIndexOf("/"));
        rootPath += "/__files/" + getApplicationFilesPath();
        
        File root = new File(rootPath);
        if(!root.exists()){
            root.mkdirs();
        }

        return root;
    }

    public static void files(String filepath) {
        
        File root = FileController.getOrCreateUploadFolder();
        File file = new File(root, filepath);
        
        if (file.exists() && file.isFile()){
            renderBinary(file);
        }
        else {
            error(404, "File not Found");
        }
    }

    /**
    * Remove a file in upload folder
    * @param fileName
    * @return boolean
    */
   public static boolean removeFile(String fileName){
       File folder = getOrCreateUploadFolder();
       File file   = new File(folder, fileName);
       if (!file.exists()){
           return false;
       }
       return file.delete();
   }

   public static String getApplicationFilesPath(){

      String path = Play.configuration.getProperty("application.files");
      if (path == null || path.isEmpty()){
         path = Play.configuration.getProperty("application.name");
      }

      return path;
   }
}
