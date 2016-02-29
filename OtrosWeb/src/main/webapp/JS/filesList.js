	var treeData = [  ${treeJSON}  ];
	$(document).ready( function() {
		$("#foldersTree").fancytree({
     		extensions: ["childcounter"],
			checkbox: true,
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