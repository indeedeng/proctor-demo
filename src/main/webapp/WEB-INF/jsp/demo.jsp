<!doctype html>
<%@taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<html>
<head>
    <meta charset="utf-8">
    <title>Proctor Demo</title>

    <meta content="IE=edge,chrome=1" http-equiv="X-UA-Compatible">
    <meta name="viewport" content="width=device-width, height=device-height, initial-scale=1.0">

    <link href="//netdna.bootstrapcdn.com/bootstrap/3.0.0/css/bootstrap.min.css" rel="stylesheet">
    <style>
        .control-label { font-weight: normal; }
        .btn { margin-top: 4px; }
        #defn-rw { display: none }
        #defn { width: 420px; height: 21px; line-height: 1.5; font-size: 90%; font-family: Monaco,Menlo,Consolas,"Courier New",monospace; }
<c:if test="${not empty groups.bgcolortstPayload}">
    html,body { background-color: ${groups.bgcolortstPayload}; color: #ffffff; }
</c:if>
    </style>
</head>

<body>

<div class="container" style="display: none">
    <div><h1>Hello Proctor</h1></div>
    <div>
        <button type="button" class="btn btn-default btn-xs" id="showDetails" onclick="toggleDetails()">
            <span class="glyphicon glyphicon-chevron-down"></span>
            show details
        </button>
    </div>
    <div id="details">
        <p id="defn-ro">
            Proctor definition: <code><a target="_new" href="${definitionUrl}">${definitionUrl}</a></code>
            <button class="btn btn-default btn-xs" onclick="return toggleEditDefinition()">Change</button>
            <c:if test="${!defaultDefinition}">
                <button onclick="$('#defn').val(''); $('#defn-rw').submit();" class="btn btn-default btn-xs">Restore Default</button>
            </c:if>
        </p>
        <form class="form-inline" id="defn-rw" method="POST" action="/change-definition#details" role="form">
            <div class="form-group">
                <label class="control-label" for="defn">Proctor definition:</label>
                <input class="form-control" type="text" id="defn" name="defn" value="${definitionUrl}">
            </div>
            <input type="submit" class="btn btn-default btn-xs" value="OK">
            <input type="cancel" class="btn btn-default btn-xs" value="Cancel" onclick="return toggleEditDefinition()">
        </form>
        <p>Your user ID is: <code>${userId}</code> <a class="btn btn-default btn-sm" href="/reset#details"><span class="glyphicon glyphicon-refresh"></span> Reset</a></p>
        <p>Your background color test group is: <code>${groups.bgcolortst}</code></p>
<c:if test="${not empty groups.bgcolortstPayload}">
        <p>The test group background color is: <code>${groups.bgcolortstPayload}</code></p>
</c:if>
        <p id="allocations"></p>
        <p><button type="button" class="btn btn-default btn-xs" id="hideDetails" onclick="toggleDetails()">
            <span class="glyphicon glyphicon-chevron-up"></span>
            hide details
        </button></p>
    </div>
</div>
<script src="//ajax.googleapis.com/ajax/libs/jquery/2.0.3/jquery.min.js"></script>
<script src="//netdna.bootstrapcdn.com/bootstrap/3.0.0/js/bootstrap.min.js"></script>
<script>
    var groupsJson = ${groupsJson};

    function toggleEditDefinition() {
        $("#defn-ro").toggle();
        $("#defn-rw").toggle();
        return true;
    }

    function toggleDetails() {
        $("#showDetails").toggle();
        $("#details").toggle(100);
        if (window.location.hash.indexOf("details") > 0) {
            window.location.hash = "";
        } else {
            window.location.hash = "#details";
        }
    }

    function refreshPage() {
		if ($("#defn-rw").is(":visible")) {
			// don't refresh when editing definition URL
			return;
		}
        if (window.location.hash.indexOf("details") < 0) {
            document.location.href = "/";
        } else {
            document.location.href = "/?details=1";
        }
    }

    function showAllocations() {
        var allocations = groupsJson['proctorResult']['testDefinitions']['bgcolortst']['allocations'];
        var buckets = groupsJson['proctorResult']['testDefinitions']['bgcolortst']['buckets'];
        var bucketMap = {};
        for (var b = 0; b < buckets.length; b++) {
            bucketMap[buckets[b].value] = buckets[b];
        }
        var output = [];
        for (var a = 0; a < allocations.length; a++) {
            var ranges = allocations[a]['ranges'];
            var rule = allocations[a]['rule'];
            var ruleName = rule != null ? rule : 'Default Rule';
            var rangeText = [];
            for (var r = 0; r < ranges.length; r++) {
                var bucket = ranges[r]['bucketValue'];
                var length = ranges[r]['length']*100;
                var bucketName = bucketMap[bucket]['name'];
                var bucketDesc = bucketMap[bucket]['description'];
                rangeText.push(length + "%: " + bucketName + (bucketDesc != "" ? " (" + bucketDesc + ")" : ""));
            }
            output.push("<div>" + ruleName + "<ul><li>" + rangeText.join("</li><li>") + "</li></ul></div>");
        }
        var outputHtml = output.join("");
        $("#allocations").html(outputHtml);
    }

    $(document).ready(function() {
        if (window.location.search == '?details=1') {
            window.location.href = "/#details";
            return;
        }
        if (window.location.hash.indexOf("details") < 0) {
            $("#showDetails").show();
            $("#details").hide();
        } else {
            $("#showDetails").hide();
            $("#details").show();
        }
        showAllocations();
        $('.container').show();

		if (window.location.pathname.indexOf('/reload') < 0) {
			setTimeout(refreshPage, 5000);
		}
    });
</script>
</body>
</html>
