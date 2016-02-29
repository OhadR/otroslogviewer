<!DOCTYPE html>
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
	<meta http-equiv="cache-control" content="max-age=0" />
	<meta http-equiv="cache-control" content="no-cache" />
	<title>File Selector</title>
	<link href="./CSS/fancytree/skin-win7/ui.fancytree.css" rel="stylesheet" type="text/css">

	<script src="../JS/jquery-1.11.3.min.js"></script>
	<script src="../JS/jquery-ui-1.9.2.min.js"></script>
	<script src="../JS/bootstrap-3.3.5.min.js"></script>
	<script src="../JS/ui/jquery.fancytree.js" type="text/javascript"></script>
	<script src="../JS/ui/jquery.fancytree.childcounter.js" type="text/javascript"></script>

<script type="text/javascript">
	var treeData = [  ${treeJSON}  ];
	$(document).ready( function() {
		$("#foldersTree").fancytree({
     		extensions: ["childcounter"],
			checkbox: false,
			selectMode: 3,
			source: treeData,
			//source: { url: "getFileSys.htm" },
			childcounter: {
		        deep: false,
		        hideZeros: true,
		        hideExpanded: true
		    },
			select: function(e, data) {
				// Get a list of all selected nodes, and convert to a key array:
				var selKeys = $.map(data.tree.getSelectedNodes(), function(node){
					return node.key;
				});

				// Get a list of all selected TOP nodes
				var selRootNodes = data.tree.getSelectedNodes(true);
				// ... and convert to a key array:
				var selRootKeys = $.map(selRootNodes, function(node){
					return node.key;
				});
				
				$("#selectedFiles").val(selRootKeys.join(","));
				//$("#echoSelection3").text(selKeys.join(", "));
				//$("#echoSelectionRootKeys3").html(selRootKeys.join(",<BR/>"));
				//$("#echoSelectionRoots3").text(selRootNodes.join(", ")); 
			},
			dblclick: function(e, data) {
				data.node.toggleSelected();
			},
			keydown: function(e, data) {
				if( e.which === 32 ) {
					data.node.toggleSelected();
					return false;
				}
			},
			// The following options are only required, if we have more than one tree on one page:
//				initId: "treeData",
			cookieId: "fancytree-Cb3",
			idPrefix: "fancytree-Cb3-"
		});
	});
</script>

</head>

<body>


<sec:authorize access="hasRole('downloadsRead')">
	
	<div style="width:80%; float: left">
		<h2 class="title7">Operation > Download Zip</h2>
		<p class="description">
			A double-click handler selects the node.<br>
			A keydown handler selects on [space].
		</p>
	</div>	
	<!-- <div style="width:20%; float: right">
	 	<a href="/docmenta/content/content/downloads.html" target="blank"><img src="/MARS/Images/need_help.jpg" class="img-responsive"/></a> 
	</div> -->
	<div style="clear:both"></div>	
	
	<div id="foldersTree" style="height: 500px, width: 500px; align: left"></div>
	<br/>
	<form id="zipFiles" names="zipFiles" method="post" action="./otros.html" target="_blank">
		<input type="hidden" id="selectedFiles" name="selectedFiles" value="">
		<input type="submit" value="Get Zip File" onclick="zipFiles.submit();">
	</form>
	<div id="result"></div>
	<%-- <div><B>Selected keys:</B> <span id="echoSelection3">-</span></div>
	<div><B>Selected root keys:</B> <span id="echoSelectionRootKeys3">-</span></div>
	<div>Selected root nodes: <span id="echoSelectionRoots3">-</span></div> 

 	<textarea>${treeJSON}</textarea> --%>

 </sec:authorize>

</body>