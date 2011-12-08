package elfinder;

import com.google.gson.Gson;
import elfinder.ElfinderException._403;
import elfinder.ElfinderException._404;
import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;
import javax.activation.MimetypesFileTypeMap;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;

/**
 * @author benoit
 */
public class Elfinder {
    
    private Elfinder.options opts;
    private Elfinder.httpParams params;
    
    public static class options {
        
        public String       root            = "";                      // path to root directory
        public String       URL             = "";                      // root directory URL
        public String       rootAlias       = "Home";                  // display this instead of root directory name
        public List<String> disabled        = new ArrayList<String>(); // list of not allowed commands
        public boolean      dotFiles        = false;                   // display dot files
        public boolean      dirSize         = false;                   // count total directories sizes
        public String       fileMode        = "0666";                  // new files mode
        public String       dirMode         = "0777";                  // new folders mode
        public String       mimeDetect      = "auto";                  // files mimetypes detection method (finfo, mime_content_type, linux (file -ib), bsd (file -Ib), internal (by extensions))
        public List<String> uploadAllow     = new ArrayList<String>(); // mimetypes which allowed to upload
        public List<String> uploadDeny      = new ArrayList<String>(); // mimetypes which not allowed to upload
        public String       uploadOrder     = "deny,allow";            // order to proccess uploadAllow and uploadAllow options
        public String       imgLib          = "auto";                  // image manipulation library (imagick, mogrify, gd)
        public String       tmbDir          = ".tmb";                  // directory name for image thumbnails. Set to "" to avoid thumbnails generation
    	public int          tmbCleanProb    = 1;                       // how frequiently clean thumbnails dir (0 - never, 200 - every init request)
    	public int          tmbAtOnce       = 5;                       // number of thumbnails to generate per request
    	public int          tmbSize         = 48;                      // images thumbnails size (px)
    	public boolean      tmbCrop         = true;                    // crop thumbnails (true - crop, false - scale image to fit thumbnail size)
    	public String       tmbBgColor      = "#ffffff";               // thumbnail background color
    	public boolean      fileURL         = true;                    // display file URL in "get info"
    	public String       dateFormat      = "dd-MM-yyyy HH:mm";      // file modification date format
    	public Object       logger          = null;                    // object logger
    	public Object       aclObj          = null;                    // acl object (not implemented yet)
    	public String       aclRole         = "user";                  // role for acl
        public boolean      debug           = false;                   // send debug to client
        
        public Map<String, Boolean> perms       = new HashMap<String, Boolean>();   // individual folders/files permisions 
        public Map<String, Boolean> defaults    = new HashMap<String, Boolean>(){{  // default permisions
            put("read",  true);
            put("write", true);
            put("rm",    true);
        }};
        
    	public Map<String, String> archiveMimes = new HashMap<String, String>();    // allowed archive's mimetypes to create. Leave empty for all available types.
    	public Object archivers   = null;                                           // info about archivers to use. See example below. Leave empty for auto detect
    }
    
    private static class httpParams {
        
        Map<String,String[]> _httpParams = new HashMap<String, String[]>();

        public httpParams() {
        }

        public httpParams(Map<String,String[]> params) {
            _httpParams = params;
        }
        
        public String get(String key){
            
            String[] val = _httpParams.get(key);
            
            return (val == null) ? null : val[0];
        }
        
        public String[] getMultiple(String key){
            
            return _httpParams.get(key);
        }
    }
    
    private static class ImageFilter implements java.io.FilenameFilter {

        public boolean accept(File file, String name) {
            
            name = name.toLowerCase();
            return (name.endsWith(".jpg") || name.endsWith(".jpeg") || name.endsWith(".png") || name.endsWith(".gif"));
        }
        
    }
    
    public Map<String,String> _commands = new HashMap<String, String>(){{
        put("open"      , "_open");
        put("reload"    , "_reload");
        put("mkdir"     , "_mkdir");
        put("mkfile"    , "_mkfile");
        put("rename"    , "_rename");
        put("upload"    , "_upload");
        put("paste"     , "_paste");
        put("rm"        , "_rm");
        put("duplicate" , "_duplicate");
        put("read"      , "_fread");
        put("edit"      , "_edit");
        put("archive"   , "_archive");
        put("extract"   , "_extract");
        put("resize"    , "_resize");
        put("tmb"       , "_thumbnails");
        put("ping"      , "_ping");
    }};

    public static Map<String,String> _mimeTypes = new HashMap<String, String> (){{
        
        //applications
        put("ai"    , "application/postscript");
        put("eps"   , "application/postscript");
        put("exe"   , "application/octet-stream");
        put("doc"   , "application/vnd.ms-word");
        put("xls"   , "application/vnd.ms-excel");
        put("ppt"   , "application/vnd.ms-powerpoint");
        put("pps"   , "application/vnd.ms-powerpoint");
        put("pdf"   , "application/pdf");
        put("xml"   , "application/xml");
        put("odt"   , "application/vnd.oasis.opendocument.text");
        put("swf"   , "application/x-shockwave-flash");
        // archives
        put("gz"    , "application/x-gzip");
        put("tgz"   , "application/x-gzip");
        put("bz"    , "application/x-bzip2");
        put("bz2"   , "application/x-bzip2");
        put("tbz"   , "application/x-bzip2");
        put("zip"   , "application/zip");
        put("rar"   , "application/x-rar");
        put("tar"   , "application/x-tar");
        put("7z"    , "application/x-7z-compressed");
        // texts
        put("txt"   , "text/plain");
        put("php"   , "text/x-php");
        put("html"  , "text/html");
        put("htm"   , "text/html");
        put("js"    , "text/javascript");
        put("css"   , "text/css");
        put("rtf"   , "text/rtf");
        put("rtfd"  , "text/rtfd");
        put("py"    , "text/x-python");
        put("java"  , "text/x-java-source");
        put("rb"    , "text/x-ruby");
        put("sh"    , "text/x-shellscript");
        put("pl"    , "text/x-perl");
        put("sql"   , "text/x-sql");
        // images
        put("bmp"   , "image/x-ms-bmp");
        put("jpg"   , "image/jpeg");
        put("jpeg"  , "image/jpeg");
        put("gif"   , "image/gif");
        put("png"   , "image/png");
        put("tif"   , "image/tiff");
        put("tiff"  , "image/tiff");
        put("tga"   , "image/x-targa");
        put("psd"   , "image/vnd.adobe.photoshop");
        //audio
        put("mp3"   , "audio/mpeg");
        put("mid"   , "audio/midi");
        put("ogg"   , "audio/ogg");
        put("mp4a"  , "audio/mp4");
        put("wav"   , "audio/wav");
        put("wma"   , "audio/x-ms-wma");
        // video
        put("avi"   , "video/x-msvideo");
        put("dv"    , "video/x-dv");
        put("mp4"   , "video/mp4");
        put("mpeg"  , "video/mpeg");
        put("mpg"   , "video/mpeg");
        put("mov"   , "video/quicktime");
        put("wm"    , "video/x-ms-wmv");
        put("flv"   , "video/x-flv");
        put("mkv"   , "video/x-matroska");
        
    }};   
    
    /**
     * Additional data about error
     **/
    private Map<String,String> _errorData;
    
    private String   _fakeRoot   = "";
    
    /**
     * Command result to send to client
     **/
    private Map<String, Object> _result = new HashMap<String, Object>();
    
    private long     _time      = 0;
    private long     _today     = 0; 
    private long     _yesterday = 0;

    private List<File> _tmpFiles   = null;
    
    public Elfinder() {
        
        __construct(new Elfinder.options());
    }

    public Elfinder(options options) {
        
        __construct(options);
    }
    
    private void __construct(options customOptions){
        
        if (customOptions == null){
            
            customOptions = new options();
        }
        
        opts = customOptions;
        
        if (opts.root.endsWith(File.separator)){
            
            opts.root = opts.root.substring(0, opts.root.length()-1);
        }
        
        Date now = new Date();
        _time     = (opts.debug) ? now.getTime() : 0;
        _fakeRoot = (opts.rootAlias == null || opts.rootAlias.isEmpty()) 
                ? opts.root 
                : new File(opts.root, opts.rootAlias).toString();
        
        if (!opts.disabled.isEmpty()){
            
            List<String> no = Arrays.asList("open", "reload", "tmb", "ping");
            Iterator<String> it = opts.disabled.iterator();
            while (it.hasNext()){
                
                String cmd = it.next();
                if (! _commands.containsKey(cmd) || no.contains(cmd)){
                    
                    it.remove();
                }
                else {
                    _commands.remove(cmd);
                }
            }
        }
        
        if (opts.tmbDir != null && !opts.tmbDir.isEmpty()){
            
            File tmbDir = new File(opts.root ,opts.tmbDir);
            try {
                if (!tmbDir.exists()){
                    
                    tmbDir.mkdirs();
                }
                
                opts.tmbDir = tmbDir.getAbsolutePath();
            }
            catch(Exception e){
                opts.tmbDir = "";
            }
        }
        
        Calendar cal = Calendar.getInstance();
        cal.setTime(now);
        
        cal.set(Calendar.MILLISECOND, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.HOUR, 0);
        _today = cal.getTimeInMillis();
        
        cal.set(Calendar.DATE, -1);
        _yesterday = cal.getTimeInMillis();
        
    }
    
    /**
     * 
     * @param httpParams
     * @return Object File.class or String.class 
     * always test like this : 
     * 
     * Object o = elfinder.run();
     * if (o.getClass == File.class){
     *    File f = (File) o;
     *    // handle the file 
     * }
     * else {
     *    //put json in http response;
     * }
     */
    public Object run(Map<String,String[]> httpParams, List<File> tmpFiles) throws  _404, _403, Exception{
        
        _tmpFiles   = tmpFiles;
        params      = new Elfinder.httpParams(httpParams);
        
        String  cmd  = (String) params.get("cmd");
        boolean init = (params.get("init") != null);
        
        
        if (opts.root == null || opts.root.isEmpty() || !is_dir(opts.root)){
            
            return "{error:'Invalid backend configuration'}";
        }
        if (!_isAllowed(opts.root, "read")){
            
            return "{error:'Access denied'}";
        }
        if (cmd == null){
            
            return "{error:'Command not specified'}";
        }
        
        cmd = cmd.trim();
        if (!_commands.containsKey(cmd)){
            
            return "{error:'Unknown command'}";
        }
        
        if (init){
            
            long ts = new Date().getTime();
            
            _result.put("disabled", opts.disabled);
            
            Map<String,Object> params = new HashMap<String, Object>();
            params.put("dotFiles", opts.dotFiles);
            params.put("uplMaxSize", 0);
            params.put("url", opts.fileURL ? opts.URL : "");
            
            if (_commands.containsKey("archive") || _commands.containsKey("extract")){
                
                //checkArchivers();
                
                String[] archives = {"application/zip"};
                
                if (_commands.containsKey("archive")){
                    
                    params.put("archives", archives);
                }
                if (_commands.containsKey("extract")){
                    
                    params.put("extract", archives);
                }
            }
            
            if (opts.tmbDir != null && !opts.tmbDir.isEmpty()){
                
                if (Math.random() * 200 <= opts.tmbCleanProb){
                    
                    File tmbDir = new File(opts.tmbDir);
                    for (File f : tmbDir.listFiles()){
                        f.delete();
                    }
                }
            }
            
            _result.put("params", params);
        }
        
        if (opts.debug){
            
            Map<String,Object> debug = new HashMap<String, Object>();
            
            debug.put("time", new Date().getTime() - _time);
            debug.put("mimeDetect" , opts.mimeDetect);
            debug.put("imgLib"     , opts.imgLib);
            
            if (opts.dirSize){
                
                debug.put("dirSize", true);
                debug.put("du", "?");
            }
            
            _result.put("debug", debug);
        }
        
        Object o = null;
        
        try {
            
            Method m = this.getClass().getMethod(_commands.get(cmd));
            o = m.invoke(this);
        }
        catch(Exception ex){
            
            Class[] exceptions = {NoSuchMethodException.class, SecurityException.class, 
                                  IllegalAccessException.class, IllegalArgumentException.class,
                                  IllegalArgumentException.class
                                };
            
            Class klass = ex.getClass();
            
            if (Arrays.asList(exceptions).contains(klass)){
                return "{error:'not yet implemented'}";
            }
            
            throw ex;
        }
        
        if (o != null){
            
            return o;
        }
        
        
        Gson gson = new Gson();
        return gson.toJson(_result);
    }
    
    // elfinder commands
    
    public File _open() throws IOException, _404, _403 {
        
        String current = trim(params.get("current"));
        String target  = trim(params.get("target"));
        
        if (current != null){
            
            String dir    = _findDir(trim(current), null);
            String file   = _find(trim(target), dir);
            
            if (current.isEmpty()
                || target.isEmpty()
                || dir == null
                || file == null
                || is_dir(file)){
                
                throw new ElfinderException._404("File not found");
            }
            
            if (! _isAllowed(dir, "read") || _isAllowed(file, "read")){
                
                throw new ElfinderException._403("Access denied");
            }
            
            if (isSymbolicLink(file)){
                
                File f = _readlink(file); 
                
                if (f == null || f.isDirectory()){
                    
                    throw new ElfinderException._404("File not found");
                }
                if (! _isAllowed(f.getParentFile().getAbsolutePath(), "read") || _isAllowed(f.getAbsolutePath(), "read")){
                
                    throw new ElfinderException._403("Access denied");
                }
            }
            
            return new File(file);
        }
        else {
            
            String path = opts.root;
            String init = params.get("init");
            
            if (target != null && !target.isEmpty()){
                
                String p = _findDir(target, null);
                if (p == null){
                    
                    if (init == null){ 
                        _result.put("error", "Invalid parameters");
                    }
                    
                }
                else if (!_isAllowed(p, "read")){
                    
                    if (init == null){ 
                        _result.put("error", "Access denied");
                    }
                }
                else {
                    path = p;
                }
            }
            
            _content(path, (params.get("tree") != null));
        }
        
        return null;
    }
    
    public void _rename() throws IOException{
        
        String current = trim(params.get("current"));
        String target  = trim(params.get("target"));
            
        String dir    = _findDir(trim(current));
        String file   = _find(trim(target), dir);

        String name   = _checkName(params.get("name"));

        if (current == null || current.isEmpty()
            || target == null || target.isEmpty()
            || dir == null
            || file == null){

            _result.put("error", "File not found");
        }
        else if (name == null){
            _result.put("error", "Invalid name");
        }
        else if (!_isAllowed(dir, "write")){
            _result.put("error", "Access denied");
        }
        else if (new File(dir, name).exists()){
            _result.put("error", "File or folder with the same name already exists");
        }
        else {

            File targ = new File(file);
            File dest = new File(dir, name);

            if (! targ.renameTo(dest)){
                _result.put("error", "Unable to rename file");
            }
            else {
                _rmTmb(target);
                List<String> tmp = new ArrayList<String>();
                tmp.add(_hash(dir + File.separator + name));
                _result.put("select", tmp);
                _content(dir, dest.isDirectory());
            }
        }
    }
        
    public void _mkdir() throws IOException{
        
        String current = trim(params.get("current"));
        String dir     = _findDir(trim(current));
        String name    = _checkName(params.get("name"));
        
        File dest = new File(dir, name);
        
        
        if (current == null || current.isEmpty()
            || dir == null){
            _result.put("error", "Invalid parameters");
        }
        else if (!_isAllowed(dir, "write")){
            _result.put("error", "Access denied");
        }
        else if (dest.exists()){
            _result.put("error", "File or folder with the same name already exists");
        }
        else if (!dest.mkdirs()){
            _result.put("error", "Unable to create folder");
        }
        else {
            _chmod(dest);
            List<String> tmp = new ArrayList<String>();
            tmp.add(_hash(dir + File.separator + name));
                
            _result.put("select", tmp);
            _content(dir, true);
        }
    }
    
    public void _ping() {
        return;
    }
    
    public void _upload() throws IOException{
        
        String dir = _findDir(trim(params.get("current")));
        
        if (dir == null){
            
            _result.put("error", "Invalid parameters");
            return;
        }
        
        if (!_isAllowed(dir, "write")){
            
            _result.put("error", "Access denied");
            return;
        }
        
        if (_tmpFiles == null || _tmpFiles.isEmpty()){
            
            _result.put("error", "No file to upload");
            return;
        }
        
        for (File file : _tmpFiles){
            
            String originalName = file.getName();
            
            String name = _checkName(originalName);
            if (name == null){
                
                _errorData(originalName, "Invalid name");
            }
            else if (!_isUploadAllow(file)){
                
                _errorData(originalName, "Not allowed file type");
            }
            else {
                
                File dest = new File (dir, name);
                
                if (! file.renameTo(dest)){
                    
                    _errorData(originalName, "Unable to save uploaded file");
                }
                else {
                    
                    _chmod(dest);
                    List<String> select = (List<String>) _result.get("select");
                    if (select == null){
                        select = new ArrayList<String>();
                    }
                    select.add(_hash(dest.getAbsolutePath()));
                    
                    _result.put("select", select);
                }
            }
        }
        
        Map<String,String> errorData = (Map<String,String>) _result.get("errorData");
        
        int errCnt = (errorData == null) ? 0 : errorData.size();
        
        if (errCnt == _tmpFiles.size()){
            _result.put("error", "Unable to upload files");
        }
        else {
            if (errCnt > 0){
                _result.put("error", "Some files was not uploaded");
            }
            _content(dir, false);
        }
    }
    
    public void _mkfile() throws IOException {
        
        String dir  = _findDir(trim(params.get("current")));
        String name = _checkName(params.get("name"));
        
        
        if (dir == null){
            
            _result.put("error", "Invalid parameters");
            return;
        }
                
        if (!_isAllowed(dir, "write")){
            
            _result.put("error", "Access denied");
            return;
        }
                
        if (name == null){
            
            _result.put("error", "Invalid name");
            return;
        }
        
        File dest = new File(dir, name);
        if (dest.exists()){
            
            _result.put("error", "File or folder with the same name already exists");
        }
        else {
            
            if (dest.createNewFile()){
                
                List<String> select = new ArrayList<String>();
                select.add(_hash(dir + File.separator + name));
                
                _result.put("select", select);
                _content(dir);
            }
            else {
                _result.put("error", "Unable to create file");
            }
        }
    }
    
    public void _rm() throws IOException {
        
        String dir  = _findDir(trim(params.get("current")));
        
        if (dir == null){
            
            _result.put("error", "Invalid parameters");
            return;
        }
        
        String[] targets = params.getMultiple("targets[]");
        if (targets != null){
            
            for(String hash : targets){
                
                String file = _find(hash, dir);
                if (file != null){
                    
                    _remove(file);
                }
            }
        }
        
        Map<String,String> errorData = (Map<String,String>) _result.get("errorData");
        if (errorData != null && !errorData.isEmpty()){
            
            _result.put("error", "Unable to remove file");
        }
        
        _content(dir, true);
    }
    
    public void _paste() throws IOException {
        
        String current  = _findDir(trim(params.get("current")));
        String src      = _findDir(trim(params.get("src")));
        String dst      = _findDir(trim(params.get("dst")));
        
        String[] targets  = params.getMultiple("targets[]");
        
        if (current == null || src == null || dst == null || targets == null || targets.length == 0){
            
            _result.put("error", "Invalid parameters");
            return;
        }
        
        boolean cut = ("1".equals(params.get("cut")));
        
        
        if (!_isAllowed(dst, "write") || !_isAllowed(src, "read")){
            
            _result.put("error", "Access denied");
            return;
        }
        
        for (String hash : targets){
            
            String filepath = _find(hash, src);
            if (filepath == null){
                _result.put("error", "File not found");
                _content(current, true);
                return;
            }
            
            File dest = new File(dst, filepath.substring(filepath.lastIndexOf("/")+1));
            
            if (dst.equals(filepath)){
                
                _result.put("error", "Unable to copy into itself");
                _content(current, true);
                return;
            }
            else if (dest.exists()){
                
                _result.put("error", "File or folder with the same name already exists");
                _content(current, true);
                return;
            }
            else if (cut && !_isAllowed(filepath, "rm")){
                
                _result.put("error", "Access denied");
                _content(current, true);
                return;
            }
            
            File from = new File(filepath);
            if (cut){
                
                if (!from.renameTo(dest)){
                    
                    _result.put("error", "Unable to move files");
                    _content(current, true);
                    return;
                }
                else if (!from.isDirectory()){
                    _rmTmb(filepath);
                }
            }
            else if (! _copy(from, dest)){
                
                _result.put("error", "Unable to copy files");
                _content(current, true);
                return;
            }
        }
        
        _content(current, true);
    }
        
    public void _duplicate() throws IOException{
        
        String current  = _findDir(trim(params.get("current")));
        String target   = _find(trim(params.get("target")), current);
        
        if (current == null
                || target == null){
            
            _result.put("error", "Invalid parameters");
            return;
        }
        
        if (!_isAllowed(current, "write") || !_isAllowed(target, "read")){
            
            _result.put("error", "Access denied");
            return;
        }
        
        String dup = _uniqueName(target);
        File from = new File(target);
        File to   = new File(dup);
        
        if (! _copy(from, to)){
            
            _result.put("error", "Access denied");
            return;
        }
        List<String> select = new ArrayList<String>();
        select.add(_hash(dup));
        _result.put("select", select);
        
        _content(current, to.isDirectory());
    }
    
    public void _thumbnails() throws IOException {
        
        String current = _findDir(trim(params.get("current")));
        
        if (! opts.tmbDir.isEmpty()){
            
            _result.put("current", _hash(current));
            
            Map<String,String> images = new HashMap<String, String> ();
            //_result.put("images", );
            File dir = new File(current);
            
            int cnt = 0;
            int max = opts.tmbAtOnce > 0 ? opts.tmbAtOnce : 5;
            
            for (File file : dir.listFiles(new ImageFilter())){
                
                if (_isAccepted(file.getName())){
                    
                    String path = file.getAbsolutePath();
                    if (file.getParentFile().canRead() && _canCreateTmb(_mimetype(path))){
                        
                        String tmbPath = _tmbPath(path);
                        File   tmbFile = new File(tmbPath);
                        
                        if (!tmbFile.exists()){
                            
                            if (cnt >= max){
                                _result.put("tmb", true);
                                return;
                            }
                            else if (_tmb(path, tmbPath)) {
                                images.put(_hash(path), _path2url(tmbPath));
                                _result.put("images", images);
                                cnt ++;
                            }
                        }
                    }
                }
            }
        }
    }
    
    public void _archive() throws IOException{
        
        String current   = _findDir(trim(params.get("current")));
        String[] targets = params.getMultiple("targets[]");
        
        if (current == null || targets == null || targets.length == 0
                || !_isAllowed(current, "write")){
            _result.put("error", "Invalid parameters");
        }
        
        String name = (targets.length == 1) ? _find(targets[0], current) : params.get("name");
        
        String archiveName  = _uniqueName(name + ".zip", "");
        File archive        = new File(archiveName);
      
        ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(archive));
        
        for (String hash : targets){
            
            String path = _find(hash, current);
            if (path == null){
                _result.put("error", "File not found");
                return;
            }
            File file = new File(path);
            
            if (file.isDirectory()){
                _zip(file, zos, file.getName());
            }
            else {
                
                FileInputStream fis = new FileInputStream(file);
                //create a new zip entry 
                ZipEntry anEntry = new ZipEntry(file.getName());
                //place the zip entry in the ZipOutputStream object 
                zos.putNextEntry(anEntry);
                //now write the content of the file to the ZipOutputStream 
                
                byte[] readBuffer = new byte[2156];
                int bytesIn = 0;
                
                while ((bytesIn = fis.read(readBuffer)) != -1) {
                    zos.write(readBuffer, 0, bytesIn);
                }
                //close the Stream 
                fis.close();
            }
        }
        // Complete the ZIP file
        zos.close();
        
        if (archive.exists()){
            _content(current);
            _result.put("select", _hash(archiveName));
        }
        else {
            _result.put("error", "Unable to create archive");
        }
    }
    
    public void _extract() throws IOException{
        
        String current  = _findDir(trim(params.get("current")));
        String target   = _find(trim(params.get("target")), current);
        
        if (current == null || target == null || !_isAllowed(current, "write")){
            _result.put("error", "Invalid parameters");
        }
        
        try {
            
            ZipFile zipFile = new ZipFile(target);
            Enumeration entries = zipFile.entries();
            while(entries.hasMoreElements()) {
                ZipEntry zipEntry = (ZipEntry)entries.nextElement();

                String fullPath = current + File.separator + zipEntry.getName();
                
                if (zipEntry.isDirectory()){

                    new File(fullPath).mkdir();
                }
                else {
                    FileUtils.copyInputStreamToFile(zipFile.getInputStream(zipEntry), new File (fullPath));
                }
            }
            zipFile.close();
            _content(current, true);
        }
        catch(Exception ex) {
            _result.put("error", "Unable to extract files from archive");
        }
    }
    
    public void _resize() throws IOException{
        
        String current  = _findDir(trim(params.get("current")));
        String target   = _find(trim(params.get("target")), current);
        
        Integer width    = parseInteger(params.get("width"));
        Integer height   = parseInteger(params.get("height"));
        
        if (current == null || target == null || width == null || height == null){
            _result.put("error", "Invalid parameters");
            return;
        }
        
        if (!_isAllowed(target, "write")){           
            _result.put("error", "Access denied ");
            return;
        }
        String mime = _mimetype(target);
        if (!mime.startsWith("image")){
            _result.put("error", "File is not an image");
            return;
        }
        
        File          imageFile = new File(target);
        BufferedImage oldimage  = ImageIO.read(imageFile);
        
        BufferedImage newimage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = newimage.createGraphics();
        g.drawImage(oldimage, 0, 0, width, height, null);
        g.dispose();
        g.setComposite(AlphaComposite.Src);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.setRenderingHint(RenderingHints.KEY_RENDERING,RenderingHints.VALUE_RENDER_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
        
        String format = getFormatName(imageFile);
        if (format == null){
            format = "jpg";
        }
        
        ImageIO.write(newimage, format, imageFile);
        
        List<String> select = new ArrayList<String>();
        select.add(_hash(target));
        _result.put("select", select);
        _content(current);
    }
    // elfinder utils
    
    private Integer parseInteger(String val){
        
        try {
            return Integer.parseInt(val);
        }
        catch(Exception ex) {
            return null;
        }
    }
    
    private static String getFormatName(Object o) {
        try {
            // Create an image input stream on the image
            ImageInputStream iis = ImageIO.createImageInputStream(o);

            // Find all image readers that recognize the image format
            Iterator iter = ImageIO.getImageReaders(iis);
            if (!iter.hasNext()) {
                // No readers found
                return null;
            }

            // Use the first reader
            ImageReader reader = (ImageReader)iter.next();

            // Close stream
            iis.close();

            // Return the format name
            return reader.getFormatName();
        } catch (IOException e) {
        }
        // The image could not be read
        return null;
    }
    
    private void _zip(File zipDir, ZipOutputStream zos, String basename){
        
        try {
            
            //get a listing of the directory content 
            String[] dirList = zipDir.list();
            byte[] readBuffer = new byte[2156];
            int bytesIn = 0;
            //loop through dirList, and zip the files 
            for (int i = 0; i < dirList.length; i++) {
                File f = new File(zipDir, dirList[i]);
                
                String zipName = basename + File.separator + f.getName();
                
                if (f.isDirectory()) {
                    
                    //if the File object is a directory, call this 
                    //function again to add its content recursively 
                    _zip(f, zos, zipName);
                    //loop again 
                    continue;
                }
                //if we reached here, the File object f was not 
                //a directory 
                //create a FileInputStream on top of f 
                FileInputStream fis = new FileInputStream(f);
                //create a new zip entry 
                ZipEntry anEntry = new ZipEntry(zipName);
                //place the zip entry in the ZipOutputStream object 
                zos.putNextEntry(anEntry);
                //now write the content of the file to the ZipOutputStream 
                while ((bytesIn = fis.read(readBuffer)) != -1) {
                    zos.write(readBuffer, 0, bytesIn);
                }
                //close the Stream 
                fis.close();
            }
        } catch (Exception e) {
            //handle exception 
        }
    }
    
    private boolean _tmb(String img, String tmb) throws IOException {
        
        Integer[] s = getimagesize(img);
        if (s == null){
            
            return false;
        }
        
        try {
            
            BufferedImage   image = ImageIO.read(new File(img));
            File            thumb = new File(tmb);

            int tmbSize = opts.tmbSize;

            if (!opts.tmbCrop){

                int newwidth  = 0;
                int newheight = 0;

                /* Keeping original dimensions if image fitting into thumbnail without scale */
                if (s[0] <= tmbSize && s[1] <= tmbSize){

                    newwidth  = s[0];
                    newheight = s[1];
                }
                else {
                    /* Calculating image scale width and height */
                    float xscale = (float)s[0] / tmbSize;
                    float yscale = (float)s[1] / tmbSize;

                    if (yscale > xscale){
                        newwidth  = (int) (s[0] * (1 / yscale));
                        newheight = (int) (s[1] * (1 / yscale));
                    } else {
                        newwidth  = (int) (s[0] * (1 / xscale));
                        newheight = (int) (s[1] * (1 / xscale));
                    }
                }

                int align_x = (tmbSize - newwidth) / 2;
                int align_y = (tmbSize - newheight) / 2;

                BufferedImage buffThumb = new BufferedImage(tmbSize, tmbSize, BufferedImage.TYPE_INT_RGB);
                Graphics2D g = buffThumb.createGraphics();
                g.drawImage(image, align_x, align_y, tmbSize, tmbSize, null);
                g.dispose();
                g.setComposite(AlphaComposite.Src);
                g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                g.setRenderingHint(RenderingHints.KEY_RENDERING,RenderingHints.VALUE_RENDER_QUALITY);
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);

                ImageIO.write(buffThumb, "png", thumb);
            }
            else {

                int size = s[0];
                if (size > s[1]){
                    size = s[1];
                }
                
                int x = 0;
                int y = 0;
                
                if (s[0] > s[1]){
                    x = (s[0] - s[1]) / 2; 
                } else {
                    y = (s[1] - s[0]) / 2;
                }
                

                BufferedImage cropedImage = image.getSubimage(x, y, size, size);
                BufferedImage scaledImage = new BufferedImage(tmbSize, tmbSize, BufferedImage.TYPE_INT_RGB);
                Graphics2D g = scaledImage.createGraphics();
                g.drawImage(cropedImage, 0, 0, tmbSize, tmbSize, null);
                g.dispose();
                g.setComposite(AlphaComposite.Src);
                g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                g.setRenderingHint(RenderingHints.KEY_RENDERING,    RenderingHints.VALUE_RENDER_QUALITY);
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                ImageIO.write(scaledImage, "png", thumb);
            }
            
            return true;
        }
        catch (Exception ex) {
            return false;
        }
    }
    
    private boolean _copy (File from, File to){
        
        String fromPath = from.getAbsolutePath();
        String toPath   = from.getAbsolutePath();
        String toDirPath= from.getParentFile().getAbsolutePath();
        
        if (!_isAllowed(fromPath, "read")){
            return _errorData(fromPath, "Access denied");
        }
        
        if (!_isAllowed(toDirPath, "write")){
            return _errorData(toDirPath, "Access denied");
        }
        
        if (to.exists()){
            return _errorData(fromPath, "File or folder with the same name already exists");
        }
        
        if (from.isDirectory()){
            
            try {
                FileUtils.copyDirectory(from, to);
            }
            catch(Exception ex) {
                return _errorData(fromPath, "Unable to copy directory");
            }
        }
        else {
            
            try {
                FileUtils.copyFile(from, to);
            }
            catch(Exception ex) {
                return _errorData(fromPath, "Unable to copy file");
            }
        }
        
        return true;
    }
    
    private String _uniqueName(String path){
        
        return _uniqueName(path, " copy");
    }
    
    private String _uniqueName(String path, String suffix){
        
        String ext = "";
        int pos = path.lastIndexOf(".");
        if (pos > -1){
            ext = path.substring(pos);
            path = path.substring(0, pos);
        }
        
        String dupName = path + suffix;
        
        File dup  = new File(dupName + ext);
        int nb = 2;
        while (dup.exists()){
            
            dup = new File(dupName + " " + nb + ext);
            nb++;
        }
        
        return dup.getAbsolutePath();
    }
    
    private void _remove(String path){
        
        if (!_isAllowed(path, "rm")){
            
           _errorData(path, "Access denied");
           return;
        }
        
        File file = new File(path);
        if (!file.exists()){
            
            _errorData(path, "File not found");
           return;
        }
        else if (file.isFile()) {
            
            if (!file.delete()){
                _errorData(path, "Unable to remove file");
            } 
            else {
                _rmTmb(path);
            }
        }
        else {
            
            for (File tmp : file.listFiles()){
                
                _remove(tmp.getAbsolutePath());
            }
            
            if (!file.delete()){
                _errorData(path, "Unable to remove directory");
            } 
        }
    }
    
    private void _chmod(File file){
        
        if (file.isDirectory()){
            file.setReadable(true);
            file.setWritable(true);
            file.setExecutable(true);
        }
        else {
            file.setReadable(true);
            file.setWritable(true);
            file.setExecutable(false);
        }
    }
    
    private String _findDir(String hash) throws IOException {
        
        return _findDir(hash, null);
    }

    private boolean _isUploadAllow(File file){
        
        boolean allow = false;
        boolean deny  = false;
        
        String mime = new MimetypesFileTypeMap().getContentType(file);
        
        if (opts.uploadAllow.contains("all")){
            
            allow = true;
        }
        else {
            if (opts.uploadAllow.contains(mime)){
                allow = true;
            }
        }
        
        if (opts.uploadDeny.contains("all")){
            
            deny = true;
        }
        else {
            if (opts.uploadDeny.contains(mime)){
                deny = true;
            }
        }
        
        if (opts.uploadOrder.startsWith("allow")){
            
            if (deny){
                return false;
            }
            else if (allow){
                return true;
            }
            else {
                return false;
            }
        }
        else {
            if (allow) {
                return true;
            } 
            else if (deny) {
                return false;
            } 
            else {
                return true;
            }
        }
    }

    private String _findDir(String hash, String path) throws IOException {
        
        if (path == null){
            
            path = opts.root;
            
            if (_hash(path).equals(hash)){
                
                return path;
            }
        }
        
        File dir = new File(path);
        for (File f : dir.listFiles()){
            
            String p = path + File.separator + f.getName();
            
            if (isSymbolicLink(f)){
                //_readlink(f);
            }
            else if (_isAccepted(f.getName()) && f.isDirectory() ){
                
                if (_hash(p).equals(hash)){
                    
                    return p;
                }
                
                p = _findDir(hash, p);
                if (null != p){
                    
                    return p;
                }
            }
        }
        
        return null;
    }
    
    private String _find(String hash, String path){
        
        File dir = new File(path);
        for (File f : dir.listFiles()){
            
            if (_isAccepted(f.getName())){
                
                String p = path + File.separator + f.getName();
                if (_hash(p).equals(hash)){
                    return p;
                }
            }
        }
        
        return null;
    }
    
    private void _rmTmb(String path){
        
        File img = new File(_tmbPath(path));
        
        if(img.exists()){
            
            img.delete();
        }
    }
    
    private String _checkName(String name){
        
        if (name == null){
            
            return null;
        }
        
        name = name.trim().replaceAll("<.*>", "");
        if (opts.dotFiles && name.startsWith(".")){
            
            return null;
        }
        
        return ( Pattern.compile("^[^\\/<>:]+$").matcher(name).matches() ) ? name : null;
    }

    private File _readlink(String path){
        
        return null;
    }
    
    private String _hash(String path){
        //return path;
        return DigestUtils.md5Hex(path);
    }
    
    private boolean is_dir(String path){
        
        try {
            File f = new File(path);
            boolean exist = f.exists();
            return exist;
        } catch (Exception ex){}
        
        return false;
    }
    
    private boolean _isAllowed(String path, String action){
        
        File f = new File(path);
        
        if ("read".equals(action)
               && !f.canRead()){
            
            return false;
        }
        else if ("write".equals(action)
                && !f.canWrite()){
            
            return false;
        }
        else if ("rm".equals(action)
                && !f.getParentFile().canWrite()){
            
            return false;
        }
        
        return true;
    }
    
    private boolean _isAccepted(String filename){
        
        if (!opts.dotFiles && filename.startsWith(".")){
            
            return false;
        }
        
        return true;
    }
    
    private void _content(String path) throws IOException{
        
        _content(path, false);
    }
    
    private void _content(String path, boolean tree) throws IOException{
        
        _cwd(path);
        _cdc(path);
        
        if (tree){
            
            _result.put("tree", _tree(opts.root));
        }
    }
    
    private void _cwd(String path){
        
        String name = "";
        String rel = (opts.rootAlias != null) ? opts.rootAlias : basename(path);
        if (path.equals(opts.root)){
            
            name = rel;    
        }
        else {
            name = basename(path);
            rel += File.separator + path.substring(opts.root.length() +1);
        }
        
        Map<String,Object> cwd = new HashMap<String, Object>();
        cwd.put("hash", _hash(path));
        cwd.put("name", name);
        cwd.put("mime", "directory");
        cwd.put("rel",  rel);
        cwd.put("size", 0);
        cwd.put("date", lastModified(path, opts.dateFormat));
        cwd.put("read", true);
        cwd.put("write", _isAllowed(path, "write"));
        cwd.put("rm",   (path.equals(opts.root))?false : _isAllowed(path, "rm"));
        
        _result.put("cwd", cwd);
    }
    
    private void _cdc(String path) throws IOException {
        
        //List<Map<String,Object>> dirs  = new ArrayList<Map<String, Object>>();
        List<Map<String,Object>> files = new ArrayList<Map<String, Object>>();
        
        File dir = new File(path);
        for (File file : dir.listFiles()){
            
            Map<String,Object> info;
            if (_isAccepted(file.getName())){
                
                info = _info(path + File.separator + file.getName());
                
                // -> wtf ?
                //if (info.get("mime").equals("directory")){
                //    dirs.add(info);
                //}
                //else {
                    files.add(info);
                //}
            }
        }
        
        _result.put("cdc", files);
    }
    
    private Map<String,Object> _info(String path) throws IOException{
        
        String symPath = null;
        
        File f = new File (path);
        long lastmodified = f.lastModified();
        
        String d = "";
        if (lastmodified > _today){
            d = "Today " + formatDate(lastmodified, "HH:mm");
        }
        else if (lastmodified > _yesterday){
            d = "Yesterday " + formatDate(lastmodified, "HH:mm");
        }
        else {
            d = formatDate(lastmodified, opts.dateFormat);
        }
        
        Map<String,Object> info = new HashMap<String, Object>();
        
        info.put("name", htmlspecialcars(f.getName()));
        info.put("hash", _hash(path));
        info.put("mime", f.isDirectory() ? "directory" : _mimetype(path));
        info.put("date", d);
        info.put("size", f.isDirectory() ? _dirSize(path) : FileUtils.sizeOf(f));
        info.put("read", _isAllowed(path, "read"));
        info.put("write",_isAllowed(path, "write"));
        info.put("rm",   _isAllowed(path, "rm"));
        
        
        if (isSymbolicLink(f)){
            
            File symLink = _readlink(path);
            
            if (symLink == null){
                info.put("mime", "symlink-broken");
                return info;
            }
            symPath = symLink.getAbsolutePath();
            
            if (symLink.isDirectory()){
                
                info.put("mime", _hash(symLink.getName()));
            } else {
                info.put("parent", "directory");
                //info.put("mime", $this->_mimetype($lpath);
            }
            info.put("link",   _hash(path));
            info.put("linkTo", (opts.rootAlias != null && !opts.rootAlias.isEmpty()) ? opts.rootAlias : basename(opts.root) + symPath.substring(opts.root.length()));
            info.put("read", _isAllowed(symPath, "read"));
            info.put("write",_isAllowed(symPath, "write"));
            info.put("rm",   _isAllowed(symPath, "rm"));
        }
        
        String mime = (String) info.get("mime");
        if (! "directory".equals(mime)){
            
            Boolean read = (Boolean) info.get("read");
            
            if (opts.fileURL && read){
                info.put("url", _path2url((symPath != null) ? symPath : path));
            }
            
            if (mime.startsWith("image")){
                
                Integer[] size = getimagesize(path);   
                
                if (size != null){
                    info.put("dim", size[0] +"x"+size[1]);
                }
                
                if (read){
                    info.put("resize",  size != null && _canCreateTmb(mime) );
                    
                    String tmb = _tmbPath(path);
                    
                    if (new File(tmb).exists()){
                        info.put("tmb", _path2url(tmb) );
                    } else if (size != null) {
                        _result.put("tmb", true);
                    }
                }
            }
        }
        
        return info;
    }
    
    private boolean _canCreateTmb(String mime){
        
        if (opts.tmbDir != null){
            
            return ("image/jpeg".equals(mime) || "image/png".equals(mime) || "image/gif".equals(mime));
        }
        return false;
    }
    
    private String _tmbPath(String path){
        
        String tmb = "";
        if (opts.tmbDir != null){
            
            File f = new File(path);
            tmb = (!path.startsWith(opts.tmbDir)) 
                    ? opts.tmbDir + File.separator + _hash(path) + ".png"
                    : path;
        }
        
        return tmb;
    }
    
    private Map<String, Object> _tree(String path) throws IOException{
        
        Map<String, Object> tree = new HashMap<String, Object>();
        
        tree.put("hash", _hash(path));
        tree.put("name", (path.equals(opts.root) && !opts.rootAlias.isEmpty()) ? opts.rootAlias : basename(path));
        tree.put("read", _isAllowed(path, "read"));
        tree.put("write",_isAllowed(path, "write"));
        
        List<Map<String, Object>> dirs = new ArrayList<Map<String, Object>>();
        tree.put("dirs", dirs);
        
        File dir = new File(path);
        for (File file : dir.listFiles()){
            
            String p = path + File.separator + file.getName();

            if (_isAccepted(file.getName()) && file.isDirectory() && !isSymbolicLink(file)){
                
                dirs.add(_tree(p));
            }
        }
        
        return tree;
    }
    
    private long _dirSize(String path){
        
        return FileUtils.sizeOfDirectory(new File(path));
    }
    
    private String _mimetype(String path){
        
        int pos = path.lastIndexOf(".");
        if (!path.isEmpty() && pos != -1){
            
            String ext = path.substring(pos+1);
            String mime = _mimeTypes.get(ext);
            
            if (mime != null){
                return mime;
            }
        }
        
        return new MimetypesFileTypeMap().getContentType(path);
    }
    
    private String _path2url(String path) throws UnsupportedEncodingException {
        
        File f = new File (path);

        String dir  = (f.getParentFile().getAbsolutePath().substring(opts.root.length()));
        
        String file = URLEncoder.encode(f.getName(), "UTF-8");
        
        String url = opts.URL;
        if (!dir.isEmpty()){
            
            url += dir.replaceAll(File.separator, "/") + "/";
        }
        if (!url.endsWith("/")){
            url += "/";
        }
        
        
        return url + file;
     }
    
    private boolean _errorData(String path, String msg){
        
        path = path.replace(opts.root, opts.rootAlias);
        
        Map<String,String> errorData = (Map<String,String>) _result.get("errorData");
        if (errorData == null){
            
            errorData = new HashMap<String,String>();
        }
        errorData.put(path, msg);
        _result.put("errorData", errorData);
        
        return false;
    }
    
    private String htmlspecialcars (String content){
        
        StringBuilder sb = new StringBuilder();
        for(int i=0; i<content.length(); i++) {
          char c = content.charAt(i);

          switch (c) {
            case '<' : 
              sb.append("&lt;");
              break;
            case '>' : 
              sb.append("&gt;");
              break;

            case '&' :
              sb.append("&amp;");
              break;
            case '"' :
              sb.append("&quot;");
              break;
            case '\'' :

              sb.append("&apos;");
              break;
            default:
              sb.append(c);
            }
        }

        return sb.toString();
    }
    
    private String basename(String path){
        
        int pos = path.lastIndexOf("/");
        
        return path.substring(pos+1);
    }
    
    private String lastModified(String path, String pattern){
        
        File dir = new File(path);
        if (!dir.exists()){
            return "";
        }
        
        long time = dir.lastModified();
        
        return formatDate(time, pattern);
    }
    
    private String formatDate(long time, String pattern){
        
        SimpleDateFormat sdf = new SimpleDateFormat(pattern);
        return sdf.format(new Date(time));
    }
    
    private Integer[] getimagesize(String path){
        
        Integer[] size = {0,0};
        
        try {
            BufferedImage image = ImageIO.read(new File(path));
            
            size[0] = image.getWidth();
            size[1] = image.getHeight();
        }
        catch(Exception ex) {
            
            return null;
        }
        
        
        return size;
    }
    
    public boolean isSymbolicLink(File file) throws IOException {
        return isSymbolicLink(file.getParentFile(), file.getName());
    }
    
    public boolean isSymbolicLink(String name) throws IOException {
        return isSymbolicLink(new File(name));
    }
    
    private boolean isSymbolicLink(File parent, String name) throws IOException {
        
        File toTest = parent != null
            ? new File(parent.getCanonicalPath(), name)
            : new File(name);
        return !toTest.getAbsolutePath().equals(toTest.getCanonicalPath());
    }
    
    private String trim(String string){
        
        if (string != null){
            
            return string.trim();
        }
        
        return null;
    }
}
