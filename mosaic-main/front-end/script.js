// ---------------------------------------------------------
// This file is part of the Prototype Search Application
// ---------------------------------------------------------

// turn logging on the console on or off
var logging_ = true;

// the host/location where the search service is running (no trailing slash!)
var searchserviceurl_ = "https://qnode.eu/ows/mosaic/service";
if ((window.location.hostname == "localhost") || (window.location.hostname == ""))
  searchserviceurl_ = "http://localhost:8008";
if (logging_) console.log ("searchserviceurl = " + searchserviceurl_);




// bind a key listener at the beginning to detect enter in the search field
$(document).ready(function () {
  $('#searchterm').keypress(function(event){
    var keycode = (event.keyCode ? event.keyCode : event.which);
    if(keycode == '13'){
      doSearch ();  
    }
  });  
});


//perform a search
function indexInfo ()
{
  if (logging_) console.log ("index-info");

  $("#searchurl").html ("");
  $("#searchresult").html ("");

  
  var searchurl = searchserviceurl_ + "/index-info";
  $("#searchurl").html ("<b>Search URL:</b> " + searchurl);

  $.ajax ({
    type: "get",
    url:searchurl,
    success:function (result) { 

    
      if (logging_) console.log (result);

      var iicount = result.results.length;
      var html = "<p>Search result for index info</p>";

      
      for (var i = 0; i < iicount; i++)
      {
        var indexname = Object.keys (result.results[i])[0];
        var obj = result.results[i][indexname];

        html += "<h2>Index: " + indexname + "</h2>";
        html += "<p><i>Document count: " + obj.documentCount+ "</i></p>";
        html += "<p><i>Language count: " + obj.languages.length + "</i></p>";
      }    
    
      $("#searchresult").html (html);
    
    }
  });
  
}

// perform a search
function doSearch ()
{
  if (logging_) console.log ("dosearch");

  $("#searchurl").html ("");
  $("#searchresult").html ("");

  // get the search parameters from the ui
  var searchterm = $("#searchterm").val ().trim ();
  var optionindex = $('input[name="option-index"]:checked').val();
  //var optionranking = $('input[name="option-ranking"]:checked').val();
  var keyword = $("#keyword").val ().trim ();
  var optionlang= $('input[name="option-language"]:checked').val();
  var optionlimit = $('input[name="option-limit"]:checked').val();
  if (logging_) console.log ("searchterm: " + searchterm);
  if (logging_) console.log ("options: " + optionindex + ", " + keyword + ", " + optionlang + ", " + optionlimit);
  if (searchterm == "")
    return;
  
  // build the search request with url parameters
  var searchurl = searchserviceurl_ + "/search?q=" + searchterm;
  if (optionindex != "default")
    searchurl += "&index=" + optionindex;
//  if (optionranking != "default")
//    searchurl += "&ranking=" + optionranking;
  if (keyword != "")
    searchurl += "&keyword=" + keyword;
  if (optionlang!= "default")
    searchurl += "&lang=" + optionlang;
  if (optionlimit != "default")
    searchurl += "&limit=" + optionlimit;
  if (logging_) console.log (searchurl); 
  
  
  var geoboxwest = $("#geobox-west").val ().trim ();
  var geoboxeast= $("#geobox-east").val ().trim ();
  var geoboxnorth= $("#geobox-north").val ().trim ();
  var geoboxsouth = $("#geobox-south").val ().trim ();
  
  if ((geoboxwest != "") && (geoboxeast != "") && (geoboxnorth != "") && (geoboxsouth != ""))
  {
    var geourlsection = "&west=" + geoboxwest + "&east=" + geoboxeast + "&north=" + geoboxnorth + "&south=" + geoboxsouth;
    searchurl += geourlsection;
  }

  
  // do the query and build result html
  //const date = new Date();
  
  $("#searchurl").html ("<b>Search URL:</b> " + searchurl);
  var starttime= new Date().getTime();
  
  $.ajax ({
    type: "get",
    url:searchurl,
    success:function (result) { 

      if (logging_) console.log (result);

      var iicount = result.results.length;
      var html = "<p>Search result for term: \"" + searchterm + "\"</p>";

      
      
      for (var i = 0; i < iicount; i++)
      {
        var indexname = Object.keys (result.results[i])[0];
        var items = result.results[i][indexname];

        html += "<h2>Index: " + indexname + "</h2>";
        html += "<p><i>Number of items: " + items.length + "</i></p>";

        
        
        for (var j = 0; j < items.length; j++)
        {
          var item = items[j];
          
          var title = item.title;
          var description = item.textSnippet.substring (0, 250);
          var url = item.url;
          var wordcount = item.wordCount;
          var warcdate = ISODateString (item.warcDate);
          var language = item.language;
          var locations = item.locations;
          var keywords = item.keywords;

          console.log (description);
          
          html += "<h6>" + title + "</h6>";
          html += "<div><i>" + description + "</i></div>";
          html += "<div>Metadata: <span class='info'>language:" + language + ", word count:" + wordcount + ", index date:" + warcdate + "</span></div>";
          
          html += "<div>Locations: ";
          for (var k = 0; k < locations.length; k++)
          {
            //console.log (locations[k].locationName + ": " + locations[k].locationEntries[0].latitude + "," + locations[k].locationEntries[0].longitude);
          
            var name = locations[k].locationName;
            var long = locations[k].locationEntries[0].longitude;
            var lat = locations[k].locationEntries[0].latitude;
            
            var lurl = "https://www.openstreetmap.org/?mlat=" + lat + "&mlon=" + long + "#map=6/" + lat + "/" + long;
            html += "<a class='location' href='" + lurl + "'>" + name + "</a> &bull; ";
    
            // https://www.openstreetmap.org/?mlat=52.17927&mlon=0.14885#map=6/52.179/0.149
            
          }
          html += "</div>";

          html += "<div>Keywords: ";
          for (var k = 0; k < keywords.length; k++)
          {
          
            var keyword= keywords[k];
            html += "<span class='info'>" + keyword + "</span>" + " &bull; ";
    
          }
          html += "</div>";
          
          
          html += "<div><a href='" + url + "'>" + url + "</a>" + "</div>";
        }
          
          
          
//        var itemcount = result.results.length;
//        var retrievaltime = new Date().getTime() - starttime;
//        
//        if (logging_) console.log (result);
//        if (logging_) console.log (itemcount);
//        var html = "<h3>Search result for term: \"" + searchterm + "\"</h3>";
//        html += "<div class='info'>Number of retrieved items: " + itemcount + " &bull; Retrieval time: " + retrievaltime + " ms</div>"
//        
//        for (var i = 0; i < itemcount; i++)
//        {
//          //console.log (result.results[i].url);
//          var title = result.results[i].title;
//          if (title == "")
//            title = "[Title missing]";
//          var description = result.results[i].textSnippet.substring (0, 250);
//          var url = result.results[i].url;
//          var wordcount = result.results[i].wordCount;
//          var warcdate = ISODateString (result.results[i].warcDate);
//          var language = result.results[i].language;
//          html += "<h5>" + title + "</h5>";
//          html += "<div>" + description + "</div>";
//          html += "<div class='info'>language:" + language + ", word count:" + wordcount + ", index date:" + warcdate + "</div>";
//          html += "<div><a href='" + url + "'>" + url + "</a>" + "</div>";
//        }
       
        
        $("#searchresult").html (html);
        
      }
    },
    error: function (xhr, status, error) {
      console.log ("error: "+ xhr + status + error);
    }
  });

  
  
}



//convert unix timestamp in milliseconds to ISO 8601 date format
function ISODateString(timestamp)
{
    timestamp = timestamp / 1000;
    var d = new Date(timestamp);
    return d.getUTCFullYear()+'-'
        + pad(d.getUTCMonth()+1)+'-'
        + pad(d.getUTCDate())+' '
        + pad(d.getUTCHours())+':'
        + pad(d.getUTCMinutes());
}

function pad(n)
{
  return n<10 ? '0'+n : n
}

