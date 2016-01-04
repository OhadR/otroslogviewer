var get_log_data_url = 	'./secured/getLogDataFromCache';
var set_log_file_url = 	'./secured/setLogFile';

//set the timer:
var myVar = setInterval(getLogDataFromOtros, 4000);
var logFileToTail;


$(function(){
    var data = [ [1,'Exxon Mobil','339,938.0','ssss', '36,130.0'],
            [2,'Wal-Mart Stores','315,654.0','11,231.0'],
            [3,'Royal Dutch Shell','306,731.0','25,311.0'],
            [4,'BP','267,600.0','22,341.0'],
            [5,'General Motors','192,604.0','-10,567.0'],
            [6,'Chevron','189,481.0','14,099.0'],
            [7,'DaimlerChrysler','186,106.3','3,536.3'],
            [8,'Toyota Motor','185,805.0','12,119.6'],
            [9,'Ford Motor','177,210.0','2,024.0', 'red'],
            [10,'ConocoPhillips','166,683.0','13,529.0'],
            [11,'General Electric','157,153.0','16,353.0'],         
            [12,'Total','152,360.7','15,250.0'],                
            [13,'ING Group','138,235.3','8,958.9'],
            [14,'Citigroup','131,045.0','24,589.0'],
            [15,'AXA','129,839.2','5,186.5'],
            [16,'Allianz','121,406.0','5,442.4'],
            [17,'Volkswagen','118,376.6','1,391.7'],
            [18,'Fortis','112,351.4','4,896.3'],
            [19,'Crédit Agricole','110,764.6','7,434.3'],
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
    $("#grid_array").pqGrid( obj );                                

    $("#grid_array").pqGrid( {editable:false} );		
 
});



function getLogDataFromOtros()
{
	//AJAX call to get the password policy:
	$.ajax({
		url : get_log_data_url,
		type: 'GET',
		dataType: 'text',
		data: {	logFilePath : logFileToTail },
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

		if(obj.level.name == 'ERROR')
		{
			var innn = '5';
		}
		var index = $( "#grid_array" ).pqGrid( "addRow", 
			    {rowData:  [obj.date, 
			                obj.level.name,
			                obj.message,
			                obj.clazz,
			                obj.thread] 
			    }
			);
	
	}

	//select last row to scroll down:
	$( "#grid_array" ).pqGrid( "setSelection", {rowIndx:index} );


}

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
		logFileToTail = "c:/MARS_pattern.log";
		
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
			populateResult(response);
		}
	});
}