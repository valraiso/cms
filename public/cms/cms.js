/*!
 * jQuery Tools v1.2.6 - The missing UI library for the Web
 * 
 * overlay/overlay.js
 * toolbox/toolbox.expose.js
 * 
 * NO COPYRIGHTS OR LICENSES. DO WHAT YOU LIKE.
 * 
 * http://flowplayer.org/tools/
 * 
 */
(function(a){a.tools=a.tools||{version:"v1.2.6"},a.tools.overlay={addEffect:function(a,b,d){c[a]=[b,d]},conf:{close:null,closeOnClick:!0,closeOnEsc:!0,closeSpeed:"fast",effect:"default",fixed:!a.browser.msie||a.browser.version>6,left:"center",load:!1,mask:null,oneInstance:!0,speed:"normal",target:null,top:"10%"}};var b=[],c={};a.tools.overlay.addEffect("default",function(b,c){var d=this.getConf(),e=a(window);d.fixed||(b.top+=e.scrollTop(),b.left+=e.scrollLeft()),b.position=d.fixed?"fixed":"absolute",this.getOverlay().css(b).fadeIn(d.speed,c)},function(a){this.getOverlay().fadeOut(this.getConf().closeSpeed,a)});function d(d,e){var f=this,g=d.add(f),h=a(window),i,j,k,l=a.tools.expose&&(e.mask||e.expose),m=Math.random().toString().slice(10);l&&(typeof l=="string"&&(l={color:l}),l.closeOnClick=l.closeOnEsc=!1);var n=e.target||d.attr("rel");j=n?a(n):null||d;if(!j.length)throw"Could not find Overlay: "+n;d&&d.index(j)==-1&&d.click(function(a){f.load(a);return a.preventDefault()}),a.extend(f,{load:function(d){if(f.isOpened())return f;var i=c[e.effect];if(!i)throw"Overlay: cannot find effect : \""+e.effect+"\"";e.oneInstance&&a.each(b,function(){this.close(d)}),d=d||a.Event(),d.type="onBeforeLoad",g.trigger(d);if(d.isDefaultPrevented())return f;k=!0,l&&a(j).expose(l);var n=e.top,o=e.left,p=j.outerWidth({margin:!0}),q=j.outerHeight({margin:!0});typeof n=="string"&&(n=n=="center"?Math.max((h.height()-q)/2,0):parseInt(n,10)/100*h.height()),o=="center"&&(o=Math.max((h.width()-p)/2,0)),i[0].call(f,{top:n,left:o},function(){k&&(d.type="onLoad",g.trigger(d))}),l&&e.closeOnClick&&a.mask.getMask().one("click",f.close),e.closeOnClick&&a(document).bind("click."+m,function(b){a(b.target).parents(j).length||f.close(b)}),e.closeOnEsc&&a(document).bind("keydown."+m,function(a){a.keyCode==27&&f.close(a)});return f},close:function(b){if(!f.isOpened())return f;b=b||a.Event(),b.type="onBeforeClose",g.trigger(b);if(!b.isDefaultPrevented()){k=!1,c[e.effect][1].call(f,function(){b.type="onClose",g.trigger(b)}),a(document).unbind("click."+m).unbind("keydown."+m),l&&a.mask.close();return f}},getOverlay:function(){return j},getTrigger:function(){return d},getClosers:function(){return i},isOpened:function(){return k},getConf:function(){return e}}),a.each("onBeforeLoad,onStart,onLoad,onBeforeClose,onClose".split(","),function(b,c){a.isFunction(e[c])&&a(f).bind(c,e[c]),f[c]=function(b){b&&a(f).bind(c,b);return f}}),i=j.find(e.close||".close"),!i.length&&!e.close&&(i=a("<a class=\"close\"></a>"),j.prepend(i)),i.click(function(a){f.close(a)}),e.load&&f.load()}a.fn.overlay=function(c){var e=this.data("overlay");if(e)return e;a.isFunction(c)&&(c={onBeforeLoad:c}),c=a.extend(!0,{},a.tools.overlay.conf,c),this.each(function(){e=new d(a(this),c),b.push(e),a(this).data("overlay",e)});return c.api?e:this}})(jQuery);
(function(a){a.tools=a.tools||{version:"v1.2.6"};var b;b=a.tools.expose={conf:{maskId:"exposeMask",loadSpeed:"slow",closeSpeed:"fast",closeOnClick:!0,closeOnEsc:!0,zIndex:9998,opacity:.8,startOpacity:0,color:"#fff",onLoad:null,onClose:null}};function c(){if(a.browser.msie){var b=a(document).height(),c=a(window).height();return[window.innerWidth||document.documentElement.clientWidth||document.body.clientWidth,b-c<20?c:b]}return[a(document).width(),a(document).height()]}function d(b){if(b)return b.call(a.mask)}var e,f,g,h,i;a.mask={load:function(j,k){if(g)return this;typeof j=="string"&&(j={color:j}),j=j||h,h=j=a.extend(a.extend({},b.conf),j),e=a("#"+j.maskId),e.length||(e=a("<div/>").attr("id",j.maskId),a("body").append(e));var l=c();e.css({position:"absolute",top:0,left:0,width:l[0],height:l[1],display:"none",opacity:j.startOpacity,zIndex:j.zIndex}),j.color&&e.css("backgroundColor",j.color);if(d(j.onBeforeLoad)===!1)return this;j.closeOnEsc&&a(document).bind("keydown.mask",function(b){b.keyCode==27&&a.mask.close(b)}),j.closeOnClick&&e.bind("click.mask",function(b){a.mask.close(b)}),a(window).bind("resize.mask",function(){a.mask.fit()}),k&&k.length&&(i=k.eq(0).css("zIndex"),a.each(k,function(){var b=a(this);/relative|absolute|fixed/i.test(b.css("position"))||b.css("position","relative")}),f=k.css({zIndex:Math.max(j.zIndex+1,i=="auto"?0:i)})),e.css({display:"block"}).fadeTo(j.loadSpeed,j.opacity,function(){a.mask.fit(),d(j.onLoad),g="full"}),g=!0;return this},close:function(){if(g){if(d(h.onBeforeClose)===!1)return this;e.fadeOut(h.closeSpeed,function(){d(h.onClose),f&&f.css({zIndex:i}),g=!1}),a(document).unbind("keydown.mask"),e.unbind("click.mask"),a(window).unbind("resize.mask")}return this},fit:function(){if(g){var a=c();e.css({width:a[0],height:a[1]})}},getMask:function(){return e},isLoaded:function(a){return a?g=="full":g},getConf:function(){return h},getExposed:function(){return f}},a.fn.mask=function(b){a.mask.load(b);return this},a.fn.expose=function(b){a.mask.load(b,this);return this}})(jQuery);

/**
 * md5 plugin
 */
(function(a){function s(a,b){return n(r(a,b))}function r(a,b){return m(o(a),o(b))}function q(a){return n(p(a))}function p(a){return l(o(a))}function o(a){return unescape(encodeURIComponent(a))}function n(a){var b="0123456789abcdef",c="",d,e;for(e=0;e<a.length;e+=1){d=a.charCodeAt(e);c+=b.charAt(d>>>4&15)+b.charAt(d&15)}return c}function m(a,b){var c,d=k(a),e=[],f=[],g;e[15]=f[15]=undefined;if(d.length>16){d=i(d,a.length*8)}for(c=0;c<16;c+=1){e[c]=d[c]^909522486;f[c]=d[c]^1549556828}g=i(e.concat(k(b)),512+b.length*8);return j(i(f.concat(g),512+128))}function l(a){return j(i(k(a),a.length*8))}function k(a){var b,c=[];c[(a.length>>2)-1]=undefined;for(b=0;b<c.length;b+=1){c[b]=0}for(b=0;b<a.length*8;b+=8){c[b>>5]|=(a.charCodeAt(b/8)&255)<<b%32}return c}function j(a){var b,c="";for(b=0;b<a.length*32;b+=8){c+=String.fromCharCode(a[b>>5]>>>b%32&255)}return c}function i(a,c){a[c>>5]|=128<<c%32;a[(c+64>>>9<<4)+14]=c;var d,i,j,k,l,m=1732584193,n=-271733879,o=-1732584194,p=271733878;for(d=0;d<a.length;d+=16){i=m;j=n;k=o;l=p;m=e(m,n,o,p,a[d],7,-680876936);p=e(p,m,n,o,a[d+1],12,-389564586);o=e(o,p,m,n,a[d+2],17,606105819);n=e(n,o,p,m,a[d+3],22,-1044525330);m=e(m,n,o,p,a[d+4],7,-176418897);p=e(p,m,n,o,a[d+5],12,1200080426);o=e(o,p,m,n,a[d+6],17,-1473231341);n=e(n,o,p,m,a[d+7],22,-45705983);m=e(m,n,o,p,a[d+8],7,1770035416);p=e(p,m,n,o,a[d+9],12,-1958414417);o=e(o,p,m,n,a[d+10],17,-42063);n=e(n,o,p,m,a[d+11],22,-1990404162);m=e(m,n,o,p,a[d+12],7,1804603682);p=e(p,m,n,o,a[d+13],12,-40341101);o=e(o,p,m,n,a[d+14],17,-1502002290);n=e(n,o,p,m,a[d+15],22,1236535329);m=f(m,n,o,p,a[d+1],5,-165796510);p=f(p,m,n,o,a[d+6],9,-1069501632);o=f(o,p,m,n,a[d+11],14,643717713);n=f(n,o,p,m,a[d],20,-373897302);m=f(m,n,o,p,a[d+5],5,-701558691);p=f(p,m,n,o,a[d+10],9,38016083);o=f(o,p,m,n,a[d+15],14,-660478335);n=f(n,o,p,m,a[d+4],20,-405537848);m=f(m,n,o,p,a[d+9],5,568446438);p=f(p,m,n,o,a[d+14],9,-1019803690);o=f(o,p,m,n,a[d+3],14,-187363961);n=f(n,o,p,m,a[d+8],20,1163531501);m=f(m,n,o,p,a[d+13],5,-1444681467);p=f(p,m,n,o,a[d+2],9,-51403784);o=f(o,p,m,n,a[d+7],14,1735328473);n=f(n,o,p,m,a[d+12],20,-1926607734);m=g(m,n,o,p,a[d+5],4,-378558);p=g(p,m,n,o,a[d+8],11,-2022574463);o=g(o,p,m,n,a[d+11],16,1839030562);n=g(n,o,p,m,a[d+14],23,-35309556);m=g(m,n,o,p,a[d+1],4,-1530992060);p=g(p,m,n,o,a[d+4],11,1272893353);o=g(o,p,m,n,a[d+7],16,-155497632);n=g(n,o,p,m,a[d+10],23,-1094730640);m=g(m,n,o,p,a[d+13],4,681279174);p=g(p,m,n,o,a[d],11,-358537222);o=g(o,p,m,n,a[d+3],16,-722521979);n=g(n,o,p,m,a[d+6],23,76029189);m=g(m,n,o,p,a[d+9],4,-640364487);p=g(p,m,n,o,a[d+12],11,-421815835);o=g(o,p,m,n,a[d+15],16,530742520);n=g(n,o,p,m,a[d+2],23,-995338651);m=h(m,n,o,p,a[d],6,-198630844);p=h(p,m,n,o,a[d+7],10,1126891415);o=h(o,p,m,n,a[d+14],15,-1416354905);n=h(n,o,p,m,a[d+5],21,-57434055);m=h(m,n,o,p,a[d+12],6,1700485571);p=h(p,m,n,o,a[d+3],10,-1894986606);o=h(o,p,m,n,a[d+10],15,-1051523);n=h(n,o,p,m,a[d+1],21,-2054922799);m=h(m,n,o,p,a[d+8],6,1873313359);p=h(p,m,n,o,a[d+15],10,-30611744);o=h(o,p,m,n,a[d+6],15,-1560198380);n=h(n,o,p,m,a[d+13],21,1309151649);m=h(m,n,o,p,a[d+4],6,-145523070);p=h(p,m,n,o,a[d+11],10,-1120210379);o=h(o,p,m,n,a[d+2],15,718787259);n=h(n,o,p,m,a[d+9],21,-343485551);m=b(m,i);n=b(n,j);o=b(o,k);p=b(p,l)}return[m,n,o,p]}function h(a,b,c,e,f,g,h){return d(c^(b|~e),a,b,f,g,h)}function g(a,b,c,e,f,g,h){return d(b^c^e,a,b,f,g,h)}function f(a,b,c,e,f,g,h){return d(b&e|c&~e,a,b,f,g,h)}function e(a,b,c,e,f,g,h){return d(b&c|~b&e,a,b,f,g,h)}function d(a,d,e,f,g,h){return b(c(b(b(d,a),b(f,h)),g),e)}function c(a,b){return a<<b|a>>>32-b}function b(a,b){var c=(a&65535)+(b&65535),d=(a>>16)+(b>>16)+(c>>16);return d<<16|c&65535}"use strict";a.md5=function(a,b,c){if(!b){if(!c){return q(a)}else{return p(a)}}if(!c){return s(b,a)}else{return r(b,a)}}})(typeof jQuery==="function"?jQuery:this)

function loadScript(url, callback){

    var script = document.createElement("script")
    script.type = "text/javascript";

    if (script.readyState){  //IE
        script.onreadystatechange = function(){
            if (script.readyState == "loaded" ||
                    script.readyState == "complete"){
                script.onreadystatechange = null;
                callback();
            }
        };
    } else {  //Others
        script.onload = callback;
    }

    script.src = url;
    document.getElementsByTagName("head")[0].appendChild(script);
}

var cms = {
    
    editor: null
    , language: null
    , requestedResource: null
    , modified : false
    , toolbar_opened : false
    , opts : {
        absoluteURLs: false,
        cssClass : 'el-rte cmsrte',
        lang     : 'fr',
        toolbar  : 'complete',
        allowSource : true,
        cssfiles : ['/public/javascripts/elrte-1.3/css/elrte-inner.css']
        ,fmOpen : function(callback) {
            $('<div id="myelfinder" />').elfinder({
             url : '/--cms/editor/browser',
             lang : 'fr',
             dialog : { width : 900, modal : true, title : 'Fichiers' }, // open in dialog window
             closeOnEditorCallback : true, // close after file select
             editorCallback : callback     // pass callback to file manager
            })
        }
    }
    , _rte : null // current elrte instance
    , _bodyPadTop : 0
    , init_toolbar : function (){
        var $body = $("body");
        $body.append('<div class="cms_opener">...</div>' +
                          '<div class="cms_toolbar">' +
                           '<div class="cms_actions">' +
                            '<span class="cms_left_actions">' +
                             '<span class="cms_action"><a href="javascript:cms.navigation();">Arborescence...</a></span>' +
                             '<span class="cms_action"><a href="javascript:cms.translation();">Traductions...</a></span>' +
                            '</span>' +
                            '<span class="cms_right_actions">' +
                             '<span class="cms_action cms_save"><a href="javascript:cms.save();">Sauvegarde</a></span>' +
                             '<span class="cms_action"><a href="javascript:cms.logout();">Déconnection</a></span>' +
                            '</span>' +
                           '</div>' +
                          '<div style="clear:both"></div>' +
                          '<div id="cms_editor_toolbar"></div>' +
                          '<div id="cms_navigation_location"></div>' +
                          '<form id="cms_logout" method="post" action="/--cms/auth"><input type="hidden" name="cms_logout" value="true"/></form>' +
                         '</div>');
                     
        var oldpadding = parseInt($body.css('padding-top').replace(/px/g,""), 10);
        
        $body.css('padding-top',  oldpadding + 40);
    }
    ,toolbar_toogle: function () {
        if (cms.toolbar_opened) {
            cms.toolbar_close();
        } else {
            cms.toolbar_open();
        }
    },

    toolbar_open: function () {
        
        $(".cms_toolbar").css('top', 0);
        cms.toolbar_opened = true;
    },

    toolbar_close: function () {
        $("#cms_navigation_location").hide();
        $(".cms_toolbar").animate({top:'-200px'}, 200, function () {
            cms.toolbar_opened = false;
        });
    }
    , save : function (){
            
        var data = {};

        data.lang     = cms.language;
        data.path     = cms.requestedResource;
        data.editors  = [];

        $("div.cms_editor").each(function() {

            var html = $(this).html();
            var code = $(this).data('code');

            var editor = {};

            editor.code = code;
            editor.content = html;

            data.editors.push (editor);
        });


        $.post('/--cms/editor/save', data, function (){

            $(".cms_editor").each(function (){
            
                $(this).data('md5', $.md5($(this).html()));
            });
        })
    }
    , kill_editor : function (){
        
        if (cms._rte != null){
            
            /**
             * update du rte si on est en mode source.
             */
            var $cmsrte = cms._rte.next();
            var $tab = $cmsrte.find('.tabsbar div.tab.active')
            if ($tab.hasClass("source")){
                $cmsrte.find("div.tab.editor").trigger('click');
            }
            /**
             * -------------------------------------------------- */
            
            cms._rte.elrte('destroy');
            cms._rte.get(0).elrte = null;
            cms._rte = null;
            $("body").css("padding-top", cms._bodyPadTop);
            $("#cms_editor_toolbar").height(0);
        }
    }
    , logout : function (){
        
        $("#cms_logout").submit();
    }
    , navScriptLoaded : false
    , navigation : function (){
        
        if (!cms.navScriptLoaded){
           
            cms.navScriptLoaded = true;
           
            $("body").append('<div class="cms_overlaybox">' + 
                                '<div class="cms_overlaybox-header">'+
                                    '<a href="#" class="close">×</a>'+
                                    '<h2>Structure du site</h2>'+    
                                '</div>'+
                                '<div class="cms_overlaybox-body">'+
                                    '<div id="cms_nav"></div>'+
                                    '<div id="cms_nav_edit"></div>'+
                                    '<div style="clear:both"></div>'+
                                '</div>'+
                                '<div class="cms_overlaybox-footer">'+
                                    '<button type="button" id="cms_nav_save">Sauvegarder</button>'+
                                    '<span id="cms-nav-result" class="label success" style="display: none"></span>'+
                                '</div>'+
                            '</div>');
                        
            
            
        }
        else {
            $(".cms_overlaybox").overlay().load();
        }

        setTimeout(function (){
            depend.on('/public/javascripts/treejs/jquery.jstree.js', function (){

                $.post('/--cms/navigation', {path : cms.requestedResource}, function (data){
                    $("#cms_nav").html(data);
                    $.getScript('/public/cms/cms-nav.js', function (){

                        $(".cms_overlaybox").overlay().load();
                    }); 
                })
            });
        }, 10);
    }
};

loadScript('/public/javascripts/elrte-1.3/js/elrte.min.js')
loadScript('/public/javascripts/elfinder-1.2/js/elfinder.min.js', function (){
   loadScript('/public/javascripts/elfinder-1.2/js/i18n/elfinder.fr.js');
})

depend.jqueryui(function (){
     
    (function($) {  

        elRTE.prototype.ui.prototype.buttons.source = function(rte, name) {
            this.constructor.prototype.constructor.call(this, rte, name);
            this.active = true;

            this.command = function() {
                var $tab = $(".cmsrte .tabsbar div.tab.active");
                if ($tab.hasClass("source")){
                    $(".cmsrte .tab.editor").trigger('click');
                    this.domElem.removeClass("active");
                }
                else {
                    $(".cmsrte .tab.source").trigger('click');
                    this.domElem.addClass("active");
                }
            }

            this.update = function() {
                this.domElem.removeClass('disabled');
            }

        }

        elRTE.prototype.options.panels.source = ['source'];
        elRTE.prototype.options.toolbars.tiny.splice(0,0,'source');
        elRTE.prototype.options.toolbars.compact.splice(0,0,'source');
        elRTE.prototype.options.toolbars.normal.splice(0,0,'source');
        elRTE.prototype.options.toolbars.complete.splice(0,0,'source');
        elRTE.prototype.options.toolbars.maxi.splice(0,0,'source');
        elRTE.prototype.options.toolbars.eldorado.splice(0,0,'source');

    })(jQuery);

    $(function (){


        $(window).bind('beforeunload', function(){ 

            var modified = false;

            $(".cms_editor").each(function (){

                var hash = $(this).data('md5');
                if (hash != null && hash != $.md5($(this).html())){
                    modified = true;
                    return false;
                }
            })

            if (modified){

                return "Attention ! vous n'avez pas sauvegarder vos modifications, voulez-vous vraiment quitter cette page ?"
            }
        })


        for (var i = 0; i < document.styleSheets.length; i++) {
            var ss = document.styleSheets[i].href;
            if (ss) {
                cms.opts.cssfiles.push(ss);
            }
        }

        $("#cms_user img").click(function () {
            $("#cms_user").toggleClass("opened");
        });

        if (!cms.logged){
            return;
        }

        cms.init_toolbar();

        $(".cms_opener").click (function(ev) {
            cms.toolbar_toogle();
        })

        $(document).click(function (e){

            var $target = $(e.target);

            if ($target.parents('.el-rte').length == 0 
                && $target.parents(".ui-dialog").length == 0
                && !$target.hasClass("ui-widget-overlay")
                && !$target.hasClass("ui-button-text")){

                cms.kill_editor();
            }
            return true;
        })

        $(".cms_editor").each(function (){

            $(this).data('md5', $.md5($(this).html()));

            $(this).click(function (){

                cms.kill_editor();

                var h = $(this).height();
                cms.opts.height = (h>50) ? h : 50; 
                cms._rte = $(this).elrte(cms.opts);

                cms.toolbar_open();

                var top = $("#cms_editor_toolbar").offset().top - $(document).scrollTop();

                var toolbar_height  = $(".cmsrte .toolbar").height();

                $("#cms_editor_toolbar").height(toolbar_height+7);
                $(".cmsrte .toolbar").css('top', top);

                var $body = $("body");
                cms._bodyPadTop = parseInt($body.css("padding-top") . replace(/px/, ""), 10);

                $body.css("padding-top", cms._bodyPadTop + toolbar_height);

                return false;
            }).mouseenter(function (){
                $(this).addClass("hover");
            }).mouseleave(function (){
                $(this).removeClass("hover");
            });
        })


    })
        
       
    
});


