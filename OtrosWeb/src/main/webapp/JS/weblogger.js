var get_log_data_url = 	'./secured/getLogDataFromCache';
var set_log_file_url = 	'./secured/setLogFile';

//set the timer:
var myVar = setInterval(getLogDataFromOtros, 4000);
var logFileToTail;
var clientId;

var tableRowCounter=1;

$(function(){

     $("#add_row").click(function()
	 {
		 addRowToGrid('ff');
//      $('#addr'+i).html("<td>"+ (i+1) +"</td><td><input name='name"+i+"' type='text' placeholder='Name' class='form-control input-md'  /> </td><td><input  name='mail"+i+"' type='text' placeholder='Mail'  class='form-control input-md'></td><td><input  name='mobile"+i+"' type='text' placeholder='Mobile'  class='form-control input-md'></td>");

	});
	
});	 

function addRowToGrid(date, 
					level,
					message,
					clazz,
					thread)
{
      //fallback:
	 /* $('#addr'+tableRowCounter).html("<td>"+ (tableRowCounter+1) +"</td><td><input name='name"+tableRowCounter+"' type='text' placeholder='Name' class='form-control input-md'  /> </td><td><input  name='mail"+tableRowCounter+"' type='text' placeholder='Mail'  class='form-control input-md'></td><td><input  name='mobile"+tableRowCounter+"' type='text' placeholder='Mobile'  class='form-control input-md'></td>");

      $('#tab_logic').append('<tr id="addr'+(tableRowCounter+1)+'"></tr>');
      tableRowCounter++; */


	if(level == 'ERROR')
	{
		$('#tab_logic').append('<tr class="danger" id="tr_id_'+ tableRowCounter +'"></tr>');
	}
	else
	{
		$('#tab_logic').append('<tr id="tr_id_'+ (tableRowCounter) +'"></tr>');
	}
	$('#tr_id_' + tableRowCounter).html("<td>"+  tableRowCounter +"</td><td>" + date + "</td><td>" + level + "</td><td>" + message + "</td><td>" + thread + "</td>");
	++tableRowCounter; 
	
}

$(function(){
    var data = [ [1,'Exxon Mobil','339,938.0','ssss', '36,130.0'],
            [2,'Wal-Mart Stores','315,654.0','11,231.0'],
            [3,'Royal Dutch Shell','306,731.0','25,311.0'],
            [9,'Ford Motor','177,210.0','2,024.0', 'red'],
            [10,'ConocoPhillips','166,683.0','13,529.0'],
            [11,'General Electric','157,153.0','16,353.0'],         
            [20,'American Intl. Group','108,905.0','10,477.0']
			];
             
    var obj = {};
    obj.width = 1250;
    obj.height = 400;
    obj.colModel = [
        {title:"Time", width:100, dataType:"integer", dataIndex:"time"},
        {title:"Level", width:50, dataType:"string", align:"right", dataIndex:"level"},
        {title:"Message", width:500, dataType:"string", align:"left", dataIndex:"message"},
        {title:"Class", width:150, dataType:"string", align:"left", dataIndex:"clazz"},
        {title:"Thread", width:200, dataType:"string", dataIndex:"thread"}
        ];
    obj.dataModel = {data:data};
  	//rowInit can be used to conditionally apply HTML attributes, merge cells, css styles, css classes, modify data, etc
    //to rows or cells.
    /*obj.rowInit = function (ui) {
        if (ui.rowData.Level == 'ERROR') 
        {
            return { style: "background:red;" };
        }
    },*/
 
});



function getLogDataFromOtros()
{
	//AJAX call to get the password policy:
	$.ajax({
		url : get_log_data_url,
		type: 'GET',
		dataType: 'text',
		data: {	
			logFilePath : logFileToTail,
			clientIdentifier : clientId },
		success: function(response)
		{
			populateResult(response);
		}
	});
}


function populateResult(response)
{
	var logDataArray = JSON.parse( response );

	var i;
	for(i=0; i< logDataArray.length; ++i)
	{
		var obj = logDataArray[i];

		addRowToGrid( obj.date, 
					obj.level.name,
					obj.message,
					obj.clazz,
					obj.thread);

		scrollToBottom();
	}
	

};

//Test
function addLineToGrid() 
{
	var index = $( "#grid_array" ).pqGrid( "addRow", 
		    {rowData:  [20000,  'xxxxxxxxXXX',  5.0,  5.0] }
		);
	//commit all add, update and delete operations.            
//	$( "#grid_array" ).pqGrid( "commit" );

	//select 3rd row
	$( "#grid_array" ).pqGrid( "setSelection", {rowIndx:index} );
		
}

function performClick(elemId) 
{
	var elem = document.getElementById(elemId);
	if(elem && document.createEvent) {
		var evt = document.createEvent("MouseEvents");
		evt.initEvent("click", true, false);
		elem.dispatchEvent(evt);
		
		logFileToTail =  elem.value ;
//		logFileToTail = "c:/MARS_pattern.log";
		
		setLogDataToTail();
	}
}


function setLogDataToTail()
{
	//AJAX call to get the password policy:
	$.ajax({
		url : set_log_file_url,
		type: 'POST',
		dataType: "text",
		data: {	logFilePath : logFileToTail },
		success: function(response)
		{
			clientId = response;
		}
	});
};

function scrollToBottom(){
	$('#moshe').scrollTop(1000000);

   
}
