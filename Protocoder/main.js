/*
* 
* Description: Quiet is the new loud.
*                Version 0.4
* by Imanol Gomez
*
*/

 console.log("Starting QUINL");
 
// OSC networking protocol
console.log("Connecting to OSC server");
 
var portNumber = 12000;

network.startOSCServer(portNumber, function(name, data) { 
    console.log(name + " " + data);
});

var client = network.connectOSC("192.168.2.110", portNumber);
console.log("OSC Connected to port " + portNumber);

//Set up some initial parameters
console.log("Setup initial conditions");
media.setVolume(50);
device.enableVolumeKeys(true);
var currentTileID = -1;
var SampleName = "samples/Region1.ogg"
var BeaconId = 23;
var BeaconStrength = 0.26;
var RegionsArray = []; // empty array


console.log("Creating Map");
var map = ui.addMap(0, 400, ui.screenWidth, 500);
map.moveTo(100 * Math.random(), 100 * Math.random());
map.setCenter(1, 12);
map.setZoom(18);
map.showControls(true);

console.log("Creating Labels");
// Labels to hold lat, lng & city name values of current location
var latLabel = ui.addText("Latitude : ",10,100,500,100);
var lonLabel = ui.addText("Longitude : ",10,150,500,100);
var altLabel = ui.addText("Altitude : ",10,200,500,100);
var regionLabel = ui.addText("Region : ",10,250,200,100);
var batteryLifeLabel = ui.addText("Battery Life : ",10,300,200,100);

console.log("Creating Buttons");
ui.addButton("Region", 0, 0, 500, 100, function() { 
  media.playSound(SampleName);
});

console.log("Reading regions...");
readRegions();


//Send Mobile ID
var mobileIdArray = new Array();
var info = device.getInfo();
var MobileId = info.id;
mobileIdArray.push(MobileId);
client.send("/mobileId", mobileIdArray);
console.log("Mobile ID: " + mobileIdArray);


//for each GPS update the image and values are changed 
sensors.startGPS(function (lat, lon, alt, speed, bearing) { 
    latLabel.setText("Latitude : " + lat);
    lonLabel.setText("Longitude : " + lon);
    altLabel.setText("Altitude : " + alt);
    regionLabel.setText("Region : " + currentTileID);
    batteryLifeLabel.setText("Battery Life : " + device.getBatteryLevel());
    var latitude = lat;
    var longitude  = lon;
    
    var batteryLife = new Array();
    batteryLife.push(device.getBatteryLevel());
    client.send("/batteryLife", batteryLife);
    
    var geoPosition = new Array();
    geoPosition.push(latitude);
    geoPosition.push(longitude);
    client.send("/position", geoPosition);
    
    if (latitude>0&&longitude>0){ 
        map.moveTo(latitude, longitude);
        map.showControls(true);
        
        for (var i = 0; i < sectionsArray.length; i++){
            
            if (latitude>=sectionsArray[i].lat1&&latitude<=sectionsArray[i].lat2 &&
            longitude>=sectionsArray[i].lon1&&longitude<=sectionsArray[i].lon2 &&
            currentTileID!=sectionsArray[i].id)
            { 
                regionLabel.setText("Region : " + sectionsArray[i].id);
                currentTileID=sectionsArray[i].id;
                SampleName = "samples/Region" + sectionsArray[i].id + ".ogg";
                media.playSound(SampleName);
                console.log("Play sample: " + SampleName);
                
                var date = getFormattedDate();
                console.log("Date: " + date);
                var url = "http://o-a.info/qitnl/track.php?time=" + date +
                         "&phone=" + MobileId +
                         "&bat=" + device.getBatteryLevel() +
                          "&region=" + currentTileID +
                          "&pos=" + latitude + "," + longitude +
                          "&beacons=" + BeaconId + "," + BeaconStrength;
                network.httpGet(url, function(status, response) { 
                    console.log(status + " " + response);   
                });
          
                break;
            }
        }
     
    }
    
});

function readRegions() {

    console.log("Reading Regions");
    //read data and store it in readData
    var readData = fileio.loadStrings("data/regions.txt");
    
    for(var i = 0; i < readData.length; i++) { 
      console.log(readData[i]);  
      var id = readData[i].split(",")[0];
      var info = readData[i].split(",")[1];
      var lat1 = info.split(" ")[0];
      var lon1 = info.split(" ")[1];
      var lat2 = info.split(" ")[2];
      var lon2 = info.split(" ")[3];
      var sampleName = info.split(" ")[4];
      
      console.log("Region " + id + ": lat1 = " + lat1 + ", lon1 = " + lon1
        + ", lat2 = " + lat2 + ", lon2 = " + lon2 + ", sample name = " + sampleName);
        
      map.addMarker("Region-> " + id +":1", "", lat1, lon1);
      map.addMarker("Region-> " + id +":2", "", lat1, lon2);
      map.addMarker("Region-> " + id +":3", "", lat2, lon2);
      map.addMarker("Region-> " + id +":4", "", lat2, lon1);
      
      var region = {Id: id, Lat1: lat1, Lon1: lon1, Lat2: lat2, Lon2: lon2, SampleName: sampleName};
      
      var regionsOSC = new Array();
      regionsOSC.push(region.Id);
      regionsOSC.push(region.Lat1);
      regionsOSC.push(region.Lon1);
      regionsOSC.push(region.Lat2);
      regionsOSC.push(region.Lon2);
      client.send("/region", regionsOSC);
      
      RegionsArray.push(region);
      
    } 
}

function createRegions() {
    //Create the data base
    console.log("Creating Regions");
    
    var totalLat1 = 52.55592;
    var totalLat2 = 52.55783; 
    var totalLon1 = 13.38136;
    var totalLon2 = 13.38533;
    var numColumns = 4;
    var numRows = 3;
    var latSection = (totalLat2 - totalLat1)/numRows;
    var lonSection = (totalLon2 - totalLon1)/numColumns;
    
    var id = 1;
    var data = new Array();
    for(var i = 0; i < numRows; i = i+1) {
         for(var j = 0; j < numColumns; j = j+1) {
            var lat1 = totalLat1 + latSection*i;
            var lat2 = lat1 + latSection;
            var lon1 = totalLon1 + lonSection*j;
            var lon2 = lon1 + lonSection;
            
            var regionString = id  + ", " +
                            lat1  + " " +
                            lon1  + " " +
                            lat2  + " " +
                            lon2  + " " +
                            "Region" + id + ";";
            data.push(regionString);
            console.log(regionString); 
            id = id + 1; 
        }
    }
    
    //saving data in regions.txt
    fileio.saveStrings("regions.txt", data);
}

function getFormattedDate() {
    var d = new Date();
    var year = d.getFullYear();
    //console.log("year: " + year);
    var month = d.getMonth();
    var monthInt = parseInt(month) + 1;
    month = monthInt.toString();
    if(monthInt<10){month = "0" + month;}
    //console.log("month: " + month);
    var day = d.getDate();
    //console.log("day: " + day);
    var hours = d.getHours();
    //console.log("hours: " + hours);
    var minutes = d.getMinutes();
    //console.log("minutes: " + minutes);
    var seconds = d.getSeconds();
    //console.log("seconds: " + seconds);
    var formattedString = year+month+day+hours+minutes+seconds;
    return formattedString;    //Return the date          
}



