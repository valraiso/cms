package elfinder;

/**
 * @author benoit
 */
public class ElfinderException {
    
    static public class _404 extends Exception {

        public _404(String string) {
            super(string);
        }
    }
    static public class _403 extends Exception {

        public _403(String string) {
            super(string);
        }
    }
}
