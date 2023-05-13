<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags"%>
<!DOCTYPE html>
<html>
<head>
  <sec:csrfMetaTags/>
  <meta charset="utf-8" name="viewport" content="width=device-width, initial-scale=1">
  <title>Clock</title>
  <script type="text/javascript" src="<c:url value="/webjars/jquery/1.9.1/jquery.min.js"/>"></script>
  <script type="text/javascript" src="<c:url value="/webjars/bootstrap/3.3.6/js/bootstrap.min.js"/>"></script>
  <script type="text/javascript" src="<c:url value="/resources/javascript/index.js"/>"></script>
  <link rel="stylesheet" href="<c:url value="/webjars/bootstrap/3.3.6/css/bootstrap.min.css"/>"/>  
	<script type="text/javascript">
	$(document).on("keydown", function(e){
		if (e.which >= 112 && e.which <= 123) { // Suppress default behaviour of F1 to F12
			e.preventDefault();
		}
		processUserSelectionData('LOGGER_FORM_KEYPRESS',e.which);
	});
  </script>
</head>
<body onload="afterPageLoad('CHECK_CLOCK_OPTION')">
<form:form name="kabaddi_form" autocomplete="off" action="match" method="POST" enctype="multipart/form-data">
<div class="content py-5" style="background-color: #EAE8FF; color: #2E008B">
  <div class="container">
	<div class="row">
	 <div class="col-md-8 offset-md-2">
       <span class="anchor"></span>
         <div class="card card-outline-secondary">
           <div class="card-header">
			  <div class="form-group row row-bottom-margin ml-2" style="margin-bottom:5px;">
	               <h6 id="match_time_hdr"></h6>
	          </div>
           </div>
          <div class="card-body">
			  <div class="panel-group" id="match_configuration">
			    <div class="panel panel-default">
			      <div class="panel-heading">
			        <h4 class="panel-title">
			          <a data-toggle="collapse" data-parent="#match_configuration" href="#load_setup_match">Configuration</a>
			        </h4>
			      </div>
			      <div id="load_setup_match" class="panel-collapse collapse">
					<div class="panel-body">
 					  <div id="start_pause_match_time_div" style="margin-bottom:5px;">
						<div class="row">
							<div class="col-4 col-sm-3">
						    <h6>START : S | PAUSE : A</h6>
						  </div>
						  <div class="col-4 col-sm-4">
						    <label for="select_match_halves" class="col-form-label text-left">Halves</label>
						      <select id="select_match_halves" name="select_match_halves" 
						      		class="browser-default custom-select custom-select-sm" onchange="processUserSelection(this)">
						          <option value=""></option>
						          <option value="first">First Half</option>
						          <option value="half">Half Time</option>
						          <option value="second">Second Half</option>
						          <option value="full">Full Time</option>
						          <option value="extra1a">OT 1 first</option>
						          <option value="extra1b">OT 1 second</option>
						          <option value="extra2a">OT 2 first</option>
						          <option value="extra2b">OT 2 second</option>
						      </select>
						  </div>
					    <div class="col-sm-2 col-md-2">
						    <button style="background-color:#2E008B;color:#FEFEFE;" class="btn btn-sm" type="button"
						  		name="overwrite_match_time" id="overwrite_match_time" onclick="processUserSelection(this);">
						  		<i class="fas fa-tools"></i> Overwrite Clock</button>
						 </div>
						 <div class="col-sm-2 col-md-2">
						    <button style="background-color:#2E008B;color:#FEFEFE;" class="btn btn-sm" type="button"
						  		name="reset_clock" id="reset_clock" onclick="processUserSelection(this);">
						  		<i class="fas fa-tools"></i> Reset Clock</button>
						 </div>
						</div>
					  </div> 
				    </div>
			      </div>
			    </div>
			  </div> 
		    <div class="form-group row row-bottom-margin ml-2" style="margin-bottom:5px;">
			  <div id="select_event_div" style="display:none;"></div>
           </div>
          </div>
         </div>
       </div>
    </div>
  </div>
 </div>
 <input type="hidden" name="selectedBroadcaster" id="selectedBroadcaster" value="${session_selected_broadcaster}"/>
</form:form>
</body>
</html>