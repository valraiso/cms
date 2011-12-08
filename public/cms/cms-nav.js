$(".cms_overlaybox").overlay({
	// custom top position
	top: '20%',

	// some mask tweaks suitable for facebox-looking dialogs
	mask: {

		// you might also consider a "transparent" color for the mask
		color: '#000',

		// load mask a little faster
		loadSpeed: 200,

		// very transparent
		opacity: 0.8

	},       

	// load it immediately after the construction
	load: false
});


$(".cms_langs a").live('click', function (){

	$(".cms_langs li").removeClass('active');
	$(this).parent().addClass('active');

	$(".cms_bloc").removeClass('active')
	var lang = $(this).attr('href').replace('#', '')

	$(".cms_bloc." + lang).addClass('active');

	document.cookie = "cms-edit-nav-last-lang"+"="+lang+"; path=/";

	return false;
})

$(".cms_tabs a").live('click', function (){

	if ($(this).parents('.cms_langs').length > 0){
		return false;
	}

	$(".cms_tab-title").removeClass('active');
	$(this).parent().addClass('active');

	$(".cms_tab-content").removeClass('active')
	$($(this).attr('href')).addClass('active');


	document.cookie = "cms-edit-nav-last-tab"+"="+$(this).attr('href')+"; path=/";

	return false;
})

$("#cms_nav_save").click(function (){

	var params = $("#cms_nav_form").serializeArray();

	$.post('/--cms/navigation/edit', params, function (result){

		if (result.success){

			$("#cms-nav-result").show()
			.removeClass("important").addClass("success")
			.html("Les informations ont bien étés sauvegardées")

		}
		else {

			$("#cms-nav-result").show()
			.removeClass("success").addClass("important")
			.html("Erreur lors de la sauvegarde")

			if (window.console && window.console.log){
                    
				window.console.log(result.message);
				window.console.log(result.stacktrace);
			}
		}

		var i = 0;
		function pulsate() {
			if(i >= 3) return;
			$("#cms-nav-result").
			animate({
				opacity: 0.2
			}, 200, 'linear').
			animate({
				opacity: 1
			}, 200, 'linear', pulsate);
			i++;
		}
		pulsate();


	}, 'json');
})

// Tree
var $firstOpened = $(".firstopened");

var initially_open = [];
$firstOpened.parents('li').each(function (){
		
	initially_open.push($(this).attr('id'));
})

var initially_select = ($firstOpened.is("li")) ? $firstOpened.attr('id') : $firstOpened.parents('li:first').attr('id');


$("#cms_nav").jstree({
	"plugins" : ["themes","html_data","ui","crrm", "contextmenu", "dnd"]
	, 
	"core" : {
		animation : 0
		, 
		initially_open : initially_open
	}
	, 
	"ui" : {
		initially_select : initially_select
	}
	, 
	"crrm" : {
		'move' : {
			'check_move' : function (m){
				return (m.cr != -1);
			}
		}
	}
	, 
	"contextmenu" : {
		items : {
			ccp:null
		}
	}
	, 
	"themes" : {
		url : '/public/javascripts/treejs/themes/default/style.css'
	}
})
.bind("create.jstree", function (e, data) {

	var ref = data.rslt.parent.attr("id").replace('cms_nav', '');
	var pos = data.rslt.position;
	var name= data.rslt.name

	//console.log('ref:' + ref + ", pos:" + pos + ", name:" + name);

	$.post('/--cms/navigation/create', {
		parentid:ref, 
		name:name, 
		pos:pos
	}, function (r){
		if(r.status) {
			$(data.rslt.obj).attr("id", r.id);
		}
		else {
			$.jstree.rollback(data.rlbk)
		}
	}, 'json');
})
.bind("remove.jstree", function (e, data) {

	if (window.confirm('Etes-vous sur de vouloir supprimer cet element ?')){

		data.rslt.obj.each(function () {

			$.ajax({
				async : false,
				type: 'POST',
				url: '/--cms/navigation/remove',
				data : {
					navid : this.id.replace('cms_nav', '')
				},
				success : function (r) {
					if(!r.status) {
						data.inst.refresh();
					}
				}
			});
		});
	}

})
.bind("rename.jstree", function (e, data) {

	var id      = data.rslt.obj.attr("id").replace('cms_nav', '');
	var newname = data.rslt.new_name;

	$.post('/--cms/navigation/rename', {
		navid:id, 
		newname:newname
	}, function (r) {
		if(!r.status) {
			$.jstree.rollback(data.rlbk);
		}
	},'json');
})
.bind("move_node.jstree", function (e, data) {

	data.rslt.o.each(function (i) {

		var id = $(this).attr("id").replace('cms_nav', '');
		var ref= data.rslt.cr === -1 ? 1 : data.rslt.np.attr("id").replace('cms_nav', '');
		var pos= data.rslt.cp + i;

		// console.log('id:' + id + ", ref:" + ref + ", pos:" + pos);

		$.post('/--cms/navigation/move', {
			navid : id
			, 
			parentid : ref
			, 
			pos : pos
		},'json');
	})
})
.bind("select_node.jstree", function (e, data){

	var id = data.rslt.obj.attr('id').replace('cms_nav', '');
	$("#cms-nav-result").hide();
	$("#cms_nav_edit").load('/--cms/navigation/edit', {
		navid:id
	})
})
                
