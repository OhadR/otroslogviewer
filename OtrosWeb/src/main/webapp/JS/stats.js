var get_baclenk_stats_history_url = './getBackendStatsHistory';

//set the timer:
var myVar = setInterval(getStatsDataFromOtros, 30000);


function getStatsDataFromOtros()
{
	//AJAX call to get the password policy:
	$.ajax({
		url : get_baclenk_stats_history_url,
		type: 'GET',
		dataType: 'text',
		success: function(response)
		{
			populateResult(response);
		}
	});
}


function populateResult(response)
{
	// Get context with jQuery - using jQuery's .get() method.
	var ctx = $("#myChart").get(0).getContext("2d");
	var threadsCtx = $("#threadsChart").get(0).getContext("2d");
	// This will get the first returned node in the jQuery collection.
	var myNewChart = new Chart(ctx);

	var normalizedStats = JSON.parse( response );

	var measureTimes = [];
	for(i=0; i< normalizedStats.timeOfMeasurements.length; ++i)
	{
		var obj = normalizedStats.timeOfMeasurements[i];
		var lineDate = new Date( obj );

		measureTimes[i] = lineDate.toISOString().slice(11, -1); //-1 to eliminate the Z.
	}

	var threadsData = {
	    labels: measureTimes,
	    datasets: [
	        {
	            label: "My First dataset",
	            fillColor: "rgba(220,220,220,0.2)",
	            strokeColor: "rgba(220,220,220,1)",
	            pointColor: "rgba(220,220,220,1)",
	            pointStrokeColor: "#fff",
	            pointHighlightFill: "#fff",
	            pointHighlightStroke: "rgba(220,220,220,1)",
	            data: normalizedStats.numThreads
	        }
	    ]
	};
	var data = {
	    labels: measureTimes,
	    datasets: [
	        {
	            label: "My Second dataset",
	            fillColor: "rgba(151,187,205,0.2)",
	            strokeColor: "rgba(151,187,205,1)",
	            pointColor: "rgba(151,187,205,1)",
	            pointStrokeColor: "#fff",
	            pointHighlightFill: "#fff",
	            pointHighlightStroke: "rgba(151,187,205,1)",
	            data: normalizedStats.numElementsInCache
	        }
	    ]
	};
	
	var myLineChart = new Chart(ctx).Line(data);
	var threadsLineChart = new Chart(threadsCtx).Line(threadsData);
};

function performClick(elemId) 
{
	var elem = document.getElementById(elemId);
	if(elem && document.createEvent) {
		var evt = document.createEvent("MouseEvents");
		evt.initEvent("click", true, false);
		elem.dispatchEvent(evt);
		
		logFileToTail =  elem.value ;
		
		setLogDataToTail();
	}
}