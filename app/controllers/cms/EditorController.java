package controllers.cms;

import elfinder.Elfinder;
import elfinder.ElfinderException._403;
import elfinder.ElfinderException._404;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import models.cms.Editor;
import play.Play;
import play.data.Upload;
import play.mvc.Controller;
import plugins.cms.CmsContext;

import play.Logger;

/**
 * @author benoit
 */
public class EditorController extends Controller {

    public static void save(String path, String lang) {

        int i = 0;
        while (true) {
            
            String  code    = params.get("editors[" + i + "][code]");
            String  content = params.get("editors[" + i + "][content]");
            Boolean fixed   = params.get("editors[" + i + "][fixed]", Boolean.class);

            fixed = (fixed == null ? false : fixed);

            i++;

            if (code == null) {
                break;
            }

            Editor editor;

            if (fixed) {

                editor = Editor.findStaticByCodeAndLanguage(code, lang);
            }
            else {

                editor = Editor.findByPathAndCodeAndLanguage(path, code, lang);
            }

            if (editor == null) {

                editor = new Editor();

                editor.path     = (fixed ? null : path);
                editor.code     = code;
                editor.language = lang;
            }

            if (!CmsContext.Constant.CMS_EDITOR_DEFAULT.equals(content)){
                editor.content = content;
            }
            
            editor.save();
        }
    }

    public static void browser(Upload[] upload) throws Exception {

        File root = FileController.getOrCreateUploadFolder();
        
        List<File> fileArray = new ArrayList<File>();
        if (upload != null){
            
            for (int n=0;n<upload.length;n++) {
                
                Upload up = upload[n];
                
                if (up.getSize() > 0 && up.getFieldName().equals("upload[]")) {
                    
                    File file = null;
                    
                    if (up.isInMemory()){
                        
                        file = new File(up.getFileName());
                        play.libs.IO.write(up.asStream(), file);
                        
                    } else {
                        
                        file = up.asFile();
                    }
                    
                    if (!fileArray.contains(file) && file.length() > 0) {
                        
                        fileArray.add(file);
                    }
                }
            }
        }
        
        //files = params.get("upload[]", File.class);
        
        
        Elfinder.options opts = new Elfinder.options();
        opts.root = root.getAbsolutePath();
        opts.URL  = "/public/files";
        //opts.rootAlias = "/public/files";
        
        Elfinder elfinder = new Elfinder(opts);
        
        try {
            Object result = elfinder.run(params.all(), fileArray);
            
            if (result.getClass() == File.class){
               
               // handle the file 
               renderBinary((File) result);
            }
            else {
            
               //put json in http response;
               renderHtml((String) result);
            }
            
        } catch (_404 ex) {
            play.Logger.info("404");
        } catch (_403 ex) {
            play.Logger.info("403");
        } catch (Exception ex){
            play.Logger.info("ex");
            throw ex;
        }
        
    }

}
