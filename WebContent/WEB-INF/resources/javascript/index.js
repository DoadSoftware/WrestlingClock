var clock_data;
function millisToMinutesAndSeconds(millis) {
  var m = Math.floor(millis / 60000);
  var s = ((millis % 60000) / 1000).toFixed(0);
  return (m < 10 ? '0' + m : m) + ":" + (s < 10 ? '0' + s :s);
}
 
function processMatchTime() {
	if(clock_data) {
		if(clock_data.matchTimeStatus.toLowerCase() == 'start') {
			clock_data.matchTotalMilliSeconds = clock_data.matchTotalMilliSeconds + 1000;
			processHandballProcedures('LOG_TIME',clock_data.matchTotalMilliSeconds);
			if(document.getElementById('match_time_hdr')) {
					document.getElementById('match_time_hdr').innerHTML = 'MATCH TIME : ' + 
						millisToMinutesAndSeconds(clock_data.matchTotalMilliSeconds);
			}
		} else {
			if(clock_data){
				if(document.getElementById('match_time_hdr')) {
					document.getElementById('match_time_hdr').innerHTML = 'MATCH TIME : ' + 
						millisToMinutesAndSeconds(clock_data.matchTotalMilliSeconds);
				}
			}
		}
	}
}
function processWaitingButtonSpinner(whatToProcess) 
{
	switch (whatToProcess) {
	case 'START_WAIT_TIMER': 
		$('.spinner-border').show();
		$(':button').prop('disabled', true);
		break;	case 'END_WAIT_TIMER': 
		$('.spinner-border').hide();
		$(':button').prop('disabled', false);
		break;
	}
}
function afterPageLoad(whichPageHasLoaded)
{
	switch (whichPageHasLoaded) {
	case 'CHECK_CLOCK_OPTION':
		processHandballProcedures(whichPageHasLoaded,null);
		setInterval(processMatchTime, 1000);
		break;
	}
}
function processUserSelectionData(whatToProcess,dataToProcess){
	//alert(whatToProcess);
	switch (whatToProcess) {
	case 'LOGGER_FORM_KEYPRESS':
		switch (dataToProcess) {
		case 83: // S
			if(document.getElementById('select_match_halves').value == null){
				alert('Please First Select the Half Of Match');
				return false;
			}
			processHandballProcedures('LOG_CLOCK_STATUS','START');
			break;
		case 65: // A
			processHandballProcedures('LOG_CLOCK_STATUS','PAUSE');
			break;		
		}
		
		break;
	}
}
function initialiseForm(whatToProcess, dataToProcess)
{
	switch (whatToProcess) {
	case 'MATCH':
		if(dataToProcess) {
			document.getElementById('select_match_halves').value = dataToProcess.matchHalves;
		} else {
			document.getElementById('select_match_halves').selectedIndex = 0;
		}
		break;
	}
}
function uploadFormDataToSessionObjects(whatToProcess)
{
	var formData = new FormData();
	var url_path;

	$('input, select, textarea').each(
		function(index){  
			if($(this).is("select")) {
				formData.append($(this).attr('id'),$('#' + $(this).attr('id') + ' option:selected').val());  
			} else {
				formData.append($(this).attr('id'),$(this).val());  
			}	
		}
	);
	
	url_path = 'upload_match_setup_data';
	
	$.ajax({    
		headers: {'X-CSRF-TOKEN': $('meta[name="_csrf"]').attr('content')},
        url : url_path,     
        data : formData,
        cache: false,
        contentType: false,
        processData: false,
        type: 'POST',     
        success : function(data) {

        },    
        error : function(e) {    
       	 	console.log('Error occured in uploadFormDataToSessionObjects with error description = ' + e);     
        }    
    });		
	
}
function processUserSelection(whichInput)
{	
	switch ($(whichInput).attr('name')) {
	case 'load_scene_btn':
		/*if(checkEmpty($('#vizIPAddress'),'IP Address Blank') == false
			|| checkEmpty($('#vizPortNumber'),'Port Number Blank') == false) {
			return false;
		}*/
	  	document.initialise_form.submit();
		break;
	case 'selectedBroadcaster':
		switch ($('#selectedBroadcaster :selected').val()) {
		case 'PHL_2023':
			//$('#vizPortNumber').attr('value','1980');
			//$('label[for=vizScene], input#vizScene').hide();
			//$('label[for=which_scene], select#which_scene').hide();
			//$('label[for=which_layer], select#which_layer').hide();
			break;
		}
		break;
	case 'cancel_overwrite_btn': 
		document.getElementById('select_event_div').style.display = 'none';
		processWaitingButtonSpinner('END_WAIT_TIMER');
		break;
	case 'select_match_halves':
		processWaitingButtonSpinner('START_WAIT_TIMER');
		processHandballProcedures('SELECT_MATCH_HALVES',whichInput);
		break;
	/*case 'start_pause_match_time':
		processWaitingButtonSpinner('START_WAIT_TIMER');
		processHandballProcedures('CHECK_CLOCK_STATUS',whichInput);
		break;*/
	case 'log_match_time_overwrite_btn': 
		processWaitingButtonSpinner('START_WAIT_TIMER');
		switch ($(whichInput).attr('name')) {
		case 'log_match_time_overwrite_btn':
			processHandballProcedures('LOG_OVERWRITE_MATCH_TIME',whichInput);
			document.getElementById('select_event_div').style.display = 'none';
			break;
		}
		break;
	default:
		switch ($(whichInput).attr('id')) {
		case 'reset_clock':
			processHandballProcedures('RESET_CLOCK');
			break;
		case 'overwrite_match_time': 
			addItemsToList('LOAD_' + $(whichInput).attr('id').toUpperCase(),null);
			document.getElementById('select_event_div').style.display = '';
		}
		break;
	}
	
}
function processHandballProcedures(whatToProcess, whichInput)
{
	var value_to_process; 
	
	switch(whatToProcess) {
	case 'LOG_CLOCK_STATUS': case 'LOG_TIME':
		value_to_process = whichInput;
		break;
	case 'SELECT_MATCH_HALVES':
		if($('#select_match_halves option:selected').val() == 'first' && confirm('Do you want to reset the time') == true){
			value_to_process = $('#select_match_halves option:selected').val() + ',' + 'true' ;
		}else if($('#select_match_halves option:selected').val() == 'second' && confirm('Do you want to reset the time') == true){
			value_to_process = $('#select_match_halves option:selected').val() + ',' + 'true' ;
		}else if($('#select_match_halves option:selected').val() == 'extra1a' && confirm('Do you want to reset the time') == true){
			value_to_process = $('#select_match_halves option:selected').val() + ',' + 'true' ;
		}else if($('#select_match_halves option:selected').val() == 'extra1b' && confirm('Do you want to reset the time') == true){
			value_to_process = $('#select_match_halves option:selected').val() + ',' + 'true' ;
		}else if($('#select_match_halves option:selected').val() == 'extra2a' && confirm('Do you want to reset the time') == true){
			value_to_process = $('#select_match_halves option:selected').val() + ',' + 'true' ;
		}else if($('#select_match_halves option:selected').val() == 'extra2b' && confirm('Do you want to reset the time') == true){
			value_to_process = $('#select_match_halves option:selected').val() + ',' + 'true' ;
		}else {
			value_to_process = $('#select_match_halves option:selected').val() + ',' + 'false' ;
		}
		break;
	case 'LOG_OVERWRITE_MATCH_TIME': 
		switch (whatToProcess) {
		case 'LOG_OVERWRITE_MATCH_TIME':
			value_to_process = $('#overwrite_match_time_score').val();
			break;
		}
		break;
	}

	$.ajax({    
        type : 'Get',     
        url : 'processHandballProcedures.html',     
        data : 'whatToProcess=' + whatToProcess + '&valueToProcess=' + value_to_process, 
        dataType : 'json',
        success : function(data) {
        	switch(whatToProcess) {
			case 'LOG_TIME': case 'LOG_OVERWRITE_MATCH_TIME': case 'LOG_CLOCK_STATUS': case 'CHECK_CLOCK_OPTION': case 'RESET_CLOCK':
				clock_data = data;
	        	switch(whatToProcess) {
				case 'CHECK_CLOCK_OPTION': case 'RESET_CLOCK':
					initialiseForm('MATCH',data);
					break;
				}
				break;
        	}
    		processWaitingButtonSpinner('END_WAIT_TIMER');
	    },    
	    error : function(e) {    
	  	 	console.log('Error occured in ' + whatToProcess + ' with error description = ' + e);     
	    }    
	});
}
function addItemsToList(whatToProcess, dataToProcess)
{
	var div,row,header_text,option,table,tbody;
	
	switch (whatToProcess) {
	case "LOAD_OVERWRITE_MATCH_TIME":
	
		$('#select_event_div').empty();

		table = document.createElement('table');
		table.setAttribute('class', 'table table-bordered');
				
		tbody = document.createElement('tbody');
		row = tbody.insertRow(tbody.rows.length);
		
		option = document.createElement('input');
		option.type = "text";
		header_text = document.createElement('label');
		header_text.innerHTML = 'Match Time (MM:SS) ';
		option.id = 'overwrite_match_time_score';
		option.value = millisToMinutesAndSeconds(clock_data.matchTotalMilliSeconds);
		
		header_text.htmlFor = option.id;
		row.insertCell(0).appendChild(header_text).appendChild(option);
		
		option = document.createElement('input');
	    option.type = 'button';
		option.name = 'log_match_time_overwrite_btn';
		option.value = 'Log Match Time Overwrite';
	    option.id = option.name;
	    option.setAttribute('onclick','processUserSelection(this);');
	    
	    div = document.createElement('div');
	    div.append(option);

		option = document.createElement('input');
		option.type = 'button';
		option.name = 'cancel_overwrite_btn';
		option.id = option.name;
		option.value = 'Cancel';
		option.setAttribute('onclick','processUserSelection(this)');

	    div.append(document.createElement('br'));
	    div.append(option);
	    
	    row.insertCell(1).appendChild(div);

		table.appendChild(tbody);
		document.getElementById('select_event_div').appendChild(table);
		
		break;		
	}
}
function removeSelectDuplicates(select_id)
{
	var this_list = {};
	$("select[id='" + select_id + "'] > option").each(function () {
	    if(this_list[this.text]) {
	        $(this).remove();
	    } else {
	        this_list[this.text] = this.value;
	    }
	});
}
function checkEmpty(inputBox,textToShow) {

	var name = $(inputBox).attr('id');
	
	document.getElementById(name + '-validation').innerHTML = '';
	document.getElementById(name + '-validation').style.display = 'none';
	$(inputBox).css('border','');
	if(document.getElementById(name).value.trim() == '') {
		$(inputBox).css('border','#E11E26 2px solid');
		document.getElementById(name + '-validation').innerHTML = textToShow + ' required';
		document.getElementById(name + '-validation').style.display = '';
		document.getElementById(name).focus({preventScroll:false});
		return false;
	}
	return true;	
}	
