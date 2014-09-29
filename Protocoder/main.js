/*
* 
* Description: Quiet is the new loud.
*                Version 0.2
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


//Create the data base
console.log("Creating Data Base");
var CREATE_TABLE = "CREATE TABLE tiles " + " ( id INT, lat1 REAL, lat2 REAL, lon1 REAL, lon2 REAL);" 
var DROP_TABLE = "DROP TABLE IF EXISTS tiles;";
var dataBaseName = "tiles.db"
var db = fileio.openSqlLite(dataBaseName);//it opens the db if exists otherwise creates one 
console.log("Created data base: " + dataBaseName);

var totalLat1 = 52.55592;
var totalLat2 = 52.55783; 
var totalLon1 = 13.38136;
var totalLon2 = 13.38533;
var numColumns = 4;
var numRows = 3;
var latSection = (totalLat2 - totalLat1)/numRows;
var lonSection = (totalLon2 - totalLon1)/numColumns;

//if db exists drop the existing table (reset)
db.execSql(DROP_TABLE);
//create and insert data 
db.execSql(CREATE_TABLE);

console.log("Inserting tiles to data base");

var id = 0;
for(var i = 0; i < numRows; i = i+1) {
     for(var j = 0; j < numColumns; j = j+1) {
         var lat1_ = totalLat1 + latSection*i;
         var lat2_ = lat1_ + latSection;
         var lon1_ = totalLon1 + lonSection*j;
         var lon2_ = lon1_ + lonSection;
         lat1_ = lat1_.toString();
         lat2_ = lat2_.toString();
         lon1_ = lon1_.toString();
         lon2_ = lon2_.toString();
         var id_ = id.toString();
         var valuesString = "(" + id_ + ", " +  lat1_ + ", " +  lat2_ + ", " +  lon1_ + ", " +  lon2_ + ");";
         var dataBaseCommand = "INSERT INTO tiles (id, lat1, lat2, lon1, lon2) VALUES " + valuesString ;
         db.execSql(dataBaseCommand);
         //console.log("Command: " + dataBaseCommand);
         id = id + 1; 
    }
}

console.log("Creating Map");
var map = ui.addMap(0, 350, ui.screenWidth, 500);
map.moveTo(100 * Math.random(), 100 * Math.random());
map.setCenter(1, 12);
map.setZoom(18);
map.showControls(true);

console.log("Creating Labels");
// Labels to hold lat, lng & city name values of current location
var latLabel = ui.addText("Latitude : ",10,100,500,100);
var lonLabel = ui.addText("Longitude : ",10,150,500,100);
var altLabel = ui.addText("Altitude : ",10,200,500,100);
var batteryLifeLabel = ui.addText("Battery Life : ",10,250,200,100);

console.log("Creating Buttons");
ui.addButton("Region", 0, 0, 500, 100, function() { 
  media.playSound(SampleName);
});

console.log("Reading from the data base");
// Add Markers 
var columns = ["id", "lat1", "lat2", "lon1", "lon2"];
var c = db.query("tiles", columns); 
console.log("Data base has " + c.getCount() + " entries"); // how many positions

//go through results
var sectionsArray = []; // empty array

while (c.moveToNext()) {
  var lat = c.getFloat(1) +  (c.getFloat(2) - c.getFloat(1))*0.5;
  var lon = c.getFloat(3) +  (c.getFloat(4) - c.getFloat(3))*0.5;
  //console.log("Add position " + id.toString() + ": lat = " + lat.toString() + ", lon = " + lon.toString());
  console.log("Section " + c.getInt(0) + ": lat1 = " + c.getFloat(1)  + ", lat2 = " + c.getFloat(2)
  + ": lon1 = " + c.getFloat(3)  + ", lon2 = " + c.getFloat(4));
  map.addMarker("Position " + c.getInt(0), "text", lat, lon);
  var section = {id: c.getInt(0), lat1: c.getFloat(1), lat2: c.getFloat(2), lon1: c.getFloat(3), lon2: c.getFloat(4)};
 
  var sectionOSC = new Array();
  sectionOSC.push(section.id);
  sectionOSC.push(section.lat1);
  sectionOSC.push(section.lat2);
  sectionOSC.push(section.lon1);
  sectionOSC.push(section.lon2);
  client.send("/section", sectionOSC);
  
  sectionsArray.push(section);
} 
db.close();

//Send Mobile ID
var mobileId = new Array();
var info = device.getInfo();
mobileId.push(info.id);
client.send("/mobileId", mobileId);
console.log("Mobile ID: " + mobileId);


//for each GPS update the image and values are changed 
sensors.startGPS(function (lat, lon, alt, speed, bearing) { 
    latLabel.setText("Latitude : " + lat);
    lonLabel.setText("Longitude : " + lon);
    altLabel.setText("Altitude : " + alt);
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
                currentTileID=sectionsArray[i].id;
                SampleName = "samples/Region" + sectionsArray[i].id + ".ogg";
                media.playSound(SampleName);
                console.log("Play sample: " + SampleName);
                break;
            }
        }
     
    }
    
});





