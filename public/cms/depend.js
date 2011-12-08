var depend = {

    on : function (scriptUrl, callback){
        
        var script = document.createElement('script');script.type = 'text/javascript';script.async = false;
        script.src = scriptUrl;

        script.onload = callback; // Run main() once jQuery has loaded
        script.onreadystatechange = function () { // Same thing but for IE
            if (this.readyState == 'complete' || this.readyState == 'loaded') callback();
        }

        var s = document.getElementsByTagName('script')[0];s.parentNode.insertBefore(script, s);
    }
    , jquery : function (callback){

        var ready_callback = function (){

            if (!jQuery.isReady) {

                $(function (){
                    callback();
                })
            }
            else {
                callback();
            }
        }

        if (typeof jQuery == 'undefined') {

            (function() {

                var script = document.createElement('script');script.type = 'text/javascript';script.async = false;
                script.src = '/public/javascripts/jquery-1.6.4.min.js';

                script.onload = ready_callback; // Run main() once jQuery has loaded
                script.onreadystatechange = function () { // Same thing but for IE
                    if (this.readyState == 'complete' || this.readyState == 'loaded') ready_callback();
                }

                var s = document.getElementsByTagName('script')[0];s.parentNode.insertBefore(script, s);
            })();
        }
        else {
            ready_callback();
        }
    }

    , jqueryui : function (callback){

        var onjQueryLoad = function (){

            if (typeof jQuery.ui == 'undefined') {

                (function (){

                    var link  = document.createElement('link');link.type = 'text/css';link.rel = 'stylesheet';
                    link.href = '/public/stylesheets/smoothness/jquery-ui-1.8.13.custom.css';
                    var s = document.getElementsByTagName('script')[0];s.parentNode.insertBefore(link, s);
                    
                    var script = document.createElement('script');script.type = 'text/javascript';script.async = true;
                    script.src = '/public/javascripts/jquery-ui-1.8.13.custom.min.js';
                    s = document.getElementsByTagName('script')[0];s.parentNode.insertBefore(script, s);

                    script.onload = callback; // Run main() once jQuery has loaded
                    script.onreadystatechange = function () { // Same thing but for IE
                        if (this.readyState == 'complete' || this.readyState == 'loaded') callback();
                    }

                })();
            }
            else {
                callback();
            }
        }

        this.jquery(onjQueryLoad);
    }
};