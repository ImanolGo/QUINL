/*
* 
* Description: Quiet is the new loud.
*              Version 0.52
* Author: Imanol Gómez
*
*/

//Global values
var SonarLoopTime = 1000;
var SonarLoop = 1000;
var CurrentRegion, CurrentSample, TownMap,
BeaconId, BeaconStrength, RegionsArray,
CurrentLatitude, CurrentLongitude, CurrentAltitude, CurrentSpeed,
MobileId, OscClient,
LatLabel,LonLabel,AltLabel,RegionLabel,BatteryLifeLabel;

var sonarLoop = util.loop(SonarLoopTime, function () { 
    media.playSound(CurrentSample);
}); 

//Initialize App
initializeApp();
createRegions();
readRegions();
getDeviceId();


//for each GPS update the image and values are changed 
sensors.startGPS(function (lat, lon, alt, speed, bearing) { 
    CurrentLatitude = lat;
    CurrentLongitude = lon;
    CurrentAltitude = alt;
    CurrentSpeed = speed;

    updateLabels();    
    updateOscValues();
    
    if (isCurrentLocationValid()){ 
        updateMap();
        CurrentRegion = -1;
        
        for (var i = 0; i < RegionsArray.length; i++){
            
            if (isInsideRegion(i))
            { 
               CurrentRegion=RegionsArray[i].id;
               
               if(regionHasChanged(i)){
                  updateLabels();
                  updateSample();
                  sendDataToServer();
                  break;

               }  
               
            }
        }

        if(CurrentRegion==-1){
          
        }
     
    }
    
});





function initializeApp(){

  console.log("Starting QUINL");

  initializeAttributes();
  initializeOSC();
  createMap();
  createButtons();
}

function initializeAttributes(){

  console.log("Setup initial attributes");
  media.setVolume(50);
  device.enableVolumeKeys(true);
  CurrentRegion = -1;
  CurrentSample = "samples/Region1.ogg"
  BeaconId = 23;
  BeaconStrength = 0.26;
  RegionsArray = []; // empty array
  CurrentLatitude = 56;
  CurrentLongitude = 13;
  CurrentAltitude = 0;
  SonarLoopTime = 1000;
  sonarLoop.stop();
  
}

function initializeOSC(){

  // OSC networking protocol
  console.log("Connecting to OSC server");
   
  var portNumber = 12000;

  network.startOSCServer(portNumber, function(name, data) { 
      console.log(name + " " + data);
  });

  OscClient = network.connectOSC("192.168.2.110", portNumber);
  console.log("OSC Connected to port " + portNumber);
}

function createMap(){

  console.log("Creating Map");
  TownMap = ui.addMap(0, 400, ui.screenWidth, 500);
  TownMap.moveTo(100 * Math.random(), 100 * Math.random());
  TownMap.setCenter(1, 12);
  TownMap.setZoom(18);
  TownMap.showControls(true);
}

function createButtons(){

  console.log("Creating Labels");
  // Labels to hold lat, lng & city name values of current location
  LatLabel = ui.addText("Latitude : ",10,100,500,100);
  LonLabel = ui.addText("Longitude : ",10,150,500,100);
  AltLabel = ui.addText("Altitude : ",10,200,500,100);
  RegionLabel = ui.addText("Region : ",10,250,200,100);
  BatteryLifeLabel = ui.addText("Battery Life : ",10,300,200,100);

  console.log("Creating Buttons");
  ui.addButton("Region", 0, 0, 500, 100, function() { 
    media.playSound(CurrentSample);
  });
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
    var data = [];
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
    fileio.saveStrings("data/regions.txt", data);
}

function readRegions() {

    console.log("Reading Regions");
    //read data and store it in readData
    var readData = fileio.loadStrings("data/regions.txt");
    
    for(var i = 0; i < readData.length; i++) { 
      console.log(readData[i]);  
      var id = readData[i].split(",")[0];
      var info = readData[i].split(",")[1];
      var lat1 = info.split(" ")[1];
      var lon1 = info.split(" ")[2];
      var lat2 = info.split(" ")[3];
      var lon2 = info.split(" ")[4];
      var sampleName = info.split(" ")[5];
      
      console.log("Region " + id + ": lat1 = " + lat1 + ", lon1 = " + lon1
        + ", lat2 = " + lat2 + ", lon2 = " + lon2 + ", sample name = " + sampleName);
        
      TownMap.addMarker("Region-> " + id +":1", "", lat1, lon1);
      TownMap.addMarker("Region-> " + id +":2", "", lat1, lon2);
      TownMap.addMarker("Region-> " + id +":3", "", lat2, lon2);
      TownMap.addMarker("Region-> " + id +":4", "", lat2, lon1);
      
      var region = {Id: id, Lat1: lat1, Lon1: lon1, Lat2: lat2, Lon2: lon2, SampleName: sampleName};
      
      var regionsOSC = [];
      regionsOSC.push(region.Id);
      regionsOSC.push(region.Lat1);
      regionsOSC.push(region.Lon1);
      regionsOSC.push(region.Lat2);
      regionsOSC.push(region.Lon2);
      OscClient.send("/region", regionsOSC);
      
      RegionsArray.push(region);
      
    } 
}

function getDeviceId(){
  
  //Send Mobile ID
  var mobileIdArray = [];
  var info = device.getInfo();
  MobileId = info.id;
  MobileId = 99;
  mobileIdArray.push(MobileId);
  OscClient.send("/mobileId", mobileIdArray);
  console.log("Device ID -> " + MobileId);
}

function updateLabels(){
  LatLabel.setText("Latitude : " + CurrentLatitude);
  LonLabel.setText("Longitude : " + CurrentLongitude);
  AltLabel.setText("Speed : " + CurrentSpeed);
  RegionLabel.setText("Region : " + CurrentRegion);
  BatteryLifeLabel.setText("Battery Life : " + parseInt(device.getBatteryLevel()));
  RegionLabel.setText("Region : " + CurrentRegion);
}

function updateOscValues(){

    var batteryLife = [];
    batteryLife.push(device.getBatteryLevel());
    OscClient.send("/batteryLife", batteryLife);
    
    var geoPosition = [];
    geoPosition.push(CurrentLatitude);
    geoPosition.push(CurrentLongitude);
    OscClient.send("/position", geoPosition);
}

function isCurrentLocationValid(){
  return (CurrentLongitude>0&&CurrentLatitude>0);
}

function isInsideRegion(regionId){
    return (CurrentLatitude>=RegionsArray[regionId].Lat1&&CurrentLatitude<=RegionsArray[regionId].Lat2 &&
    CurrentLongitude>=RegionsArray[regionId].Lon1&&CurrentLongitude<=RegionsArray[regionId].lon2);
    
}

function regionHasChanged(regionId){
    return (CurrentRegion!=RegionsArray[regionId].Id);
}

function updateMap(){
  TownMap.moveTo(CurrentLatitude, CurrentLongitude);
  TownMap.showControls(true);
}

function  updateSample(){
  ///MAKE A MAP INSTEAD OF AN ARRAY
  SampleName = RegionsArray[CurrentRegion].SampleName;
  CurrentSample = "samples/Region" + RegionsArray[CurrentRegion].SampleName + ".ogg";
  media.playSound(CurrentSample);
  console.log("Play sample: " + CurrentSample);

  if(SampleName == "alarm"){
     sonarLoop.start();
  }
  else{
    sonarLoop.stop();
  }
 
}
  
function sendDataToServer(){
  
  var date = getFormattedDate();
  console.log("Date: " + date);
  var url = "http://o-a.info/qitnl/track.php?time=" + date +
           "&phone=" + MobileId +
           "&bat=" + device.getBatteryLevel() +
            "&region=" + CurrentRegion +
            "&pos=" + latitude + "," + longitude +
            "&beacons=" + BeaconId + "," + BeaconStrength;
  network.httpGet(url, function(status, response) { 
      console.log(status + " " + response);   
  });    

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



