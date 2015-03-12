/*
* 
* Description: Quiet is the new loud.
*              Version 0.81
* Author: Imanol Gómez
*
*/

//Global values
var CurrentRegion, CurrentSample, TownMap, GlobalRegion, OutsideRegion,
BeaconId, BeaconStrength, RegionsArray,
CurrentLocation, CurrentBeacon, pd,
MobileId, OscClient, RegionsDataBase,
LatLabel,LonLabel,AltLabel,RegionLabel,BatteryLifeLabel,
buttonPlay,buttonStop;


//Initialize App
initializeApp();
//createRegions();
createDataBase();
readRegions();
getDeviceId();


//for each GPS update the image and values are changed 
sensors.startGPS(function (lat, lon, alt, speed, bearing) { 
    
    updateLocation(lat, lon, alt, speed, bearing);

    if (!isCurrentLocationValid()){ 
        return;
    }
    
    if(updateOutsideSample()){
      return;
    }
    
    if(updateRegionSample()){
      return;
    }

    if(updateGlobalSample()){
      return;
    }
});


function initializeApp(){

  console.log("Starting QUINL");

  initializeAttributes();
  initializePureData();
  initializeOSC();
  createMap();
  createGUI();
}

function initializeAttributes(){

  console.log("Setup initial attributes");
  media.setVolume(50);
  device.enableVolumeKeys(true);
  GlobalRegion = {Id: 0, Lat1: 0, Lon1: 0, Lat2: 0, Lon2: 0, SampleName: "sonar1"};
  CurrentRegion = {Id: -10, Lat1: 0, Lon1: 0, Lat2: 0, Lon2: 0, SampleName: "sonar1"};
  OutsideRegion = {Id: -1, Lat1: 0, Lon1: 0, Lat2: 0, Lon2: 0, SampleName: "alarm1"};
  CurrentLocation = {Latitude: 56, Longitude: 13, Altitude: 0, Accuracy: 0, Speed: 0};
  CurrentBeacon = {Id: 23, Strength: 0.26};
  RegionsArray = []; // empty array
  
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

function createGUI(){

  console.log("Creating GUI...");
  console.log("Creating Labels");
  // Labels to hold lat, lng & city name values of current location
  LatLabel = ui.addText("Latitude : ",10,100,500,100);
  LonLabel = ui.addText("Longitude : ",10,150,500,100);
  AltLabel = ui.addText("Altitude : ",10,200,500,100);
  RegionLabel = ui.addText("Region : " + CurrentRegion.Id,10,250,200,100);
  BatteryLifeLabel = ui.addText("Battery Life : "  + parseInt(device.getBatteryLevel()),10,300,200,100);
  
  console.log("Creating Buttons");
  buttonPlay = ui.addButton("Play", ui.screenWidth-330, 10, 300, 100, function(){
       pd.sendBang("playBang");
  });
  
  buttonStop = ui.addButton("Stop",  ui.screenWidth-330, 130, 300, 100, function(){
        pd.sendBang("stopBang");
  });
}

function initializePureData(){
    var patchName = "loopSample.pd";
    console.log("Initialize Pure Data Patch: " + patchName);
    pd = media.initPDPatch(patchName, function(data) { 
        console.log(data);
    });
}

function createRegions() {
    //Create the data base
    console.log("Creating Regions");
    
    var globalLat1 = 52.55592;
    var globalLat2 = 52.55783; 
    var globalLon1 = 13.38136;
    var globalLon2 = 13.38533;
    var numColumns = 4;
    var numRows = 3;
    var latSection = (totalLat2 - totalLat1)/numRows;
    var lonSection = (totalLon2 - totalLon1)/numColumns;
    var id = 0;
    var data = [];

    //Create the global region. Outside here the mobile phone is not suppose to be
    var regionString = id  + ", " +
                            globalLat1  + " " +
                            globalLon1  + " " +
                            globalLat2  + " " +
                            globalLon2  + " " +
                            "sonar1;";
    data.push(regionString);
    console.log(regionString); 
    id = id + 1; 

    //Create specific regions
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

function createDataBase(){

  //Create the data base
  console.log("Creating Data Base");
  var CREATE_TABLE = "CREATE TABLE regions " + " ( id INT, lat1 REAL, lon1 REAL, lat2 REAL, lon2 REAL, sampleName TEXT);" 
  var DROP_TABLE = "DROP TABLE IF EXISTS regions;";
  var dataBaseName = "regions.db"
  RegionsDataBase = fileio.openSqlLite("data/" + dataBaseName);//it opens the data base if exists otherwise creates one 
  //if db exists drop the existing table (reset)
  RegionsDataBase.execSql(DROP_TABLE);
  //create and insert data
  RegionsDataBase.execSql(CREATE_TABLE);
  console.log("Created data base: " + dataBaseName);

  console.log("Inserting regions to data base");

  //read data and store it in readData
  var readData = fileio.loadStrings("data/regions.txt");
    
    for(var i = 0; i < readData.length; i++) { 
      console.log(readData[i]);  
      var id_ = readData[i].split(",")[0].toString();
      var info = readData[i].split(",")[1];
      info = readData[i].split(";")[0];
      if(info.length<=0){
          break;
      }
      var lat1_ = info.split(" ")[1].toString();
      var lon1_ = info.split(" ")[2].toString();
      var lat2_ = info.split(" ")[3].toString();
      var lon2_ = info.split(" ")[4].toString();
      var sampleName_ = info.split(" ")[5].toString();

      var valuesString = "(" + id_ + ", " +  lat1_ + ", " +  lon1_ + ", " +  lat2_ + ", " +  lon2_ + ", '" +  sampleName_ + "');";
      var dataBaseCommand = "INSERT INTO regions (id, lat1, lon1, lat2, lon2, sampleName) VALUES " + valuesString ;
      //console.log(dataBaseCommand);  
      RegionsDataBase.execSql(dataBaseCommand);
    } 

    RegionsDataBase.close();
}

function readRegions() {

    console.log("Reading regions from data base");

    var dataBaseName = "regions.db"
    RegionsDataBase = fileio.openSqlLite("data/" + dataBaseName);//it opens the data base if exists otherwise creates on
    console.log("Opening data base: " + dataBaseName);
    
    var columns = ["id", "lat1", "lon1", "lat2", "lon2", "sampleName"];
    var c = RegionsDataBase.query("regions", columns); 
    console.log("Data base has " + c.getCount() + " entries"); // how many positions

    RegionsArray = new Array(); // empty array

    while (c.moveToNext()) {

       var id = c.getInt(0);
       var lat1 = c.getFloat(1);
       var lon1 = c.getFloat(2);
       var lat2 = c.getFloat(3);
       var lon2 = c.getFloat(4);
       var sampleName = c.getString(5);
    
       console.log("Region " + id + ": lat1 = " + lat1 + ", lon1 = " + lon1
        + ", lat2 = " + lat2 + ", lon2 = " + lon2 + ", sample name = " + sampleName);

        TownMap.addMarker("Region-> " + id +":1", "", lat1, lon1);
        TownMap.addMarker("Region-> " + id +":2", "", lat1, lon2);
        TownMap.addMarker("Region-> " + id +":3", "", lat2, lon2);
        TownMap.addMarker("Region-> " + id +":4", "", lat2, lon1);
        
        var region = {Id: id, Lat1: lat1, Lon1: lon1, Lat2: lat2, Lon2: lon2, SampleName: sampleName};
        
        if(region.Id == GlobalRegion.Id){ // Id 0 is being used for the global region
          GlobalRegion = region;
        }
        
        var regionsOSC = [];
        regionsOSC.push(region.Id);
        regionsOSC.push(region.Lat1);
        regionsOSC.push(region.Lon1);
        regionsOSC.push(region.Lat2);
        regionsOSC.push(region.Lon2);
        OscClient.send("/region", regionsOSC);
        
        RegionsArray.push(region);
    }    
    
    RegionsDataBase.close();
}

function updateRegionSample()
{
   for (var i = 0; i < RegionsArray.length; i++)
   {
      var region = RegionsArray[i];
      if ((!isGlobalRegion(region)) && (isCurrentLocationInside(region)))
      {  
          if(regionHasChanged(region))
          {
              CurrentRegion = RegionsArray[i];
              console.log("Region: " + CurrentRegion.Id);
              updateLabels();
              updateSample();
          }
           
          return true;
      }
  }
 
  return false;
}

function updateGlobalSample()
{
  if(isCurrentLocationInside(GlobalRegion))
  {
    if(regionHasChanged(GlobalRegion))
    {
        CurrentRegion = GlobalRegion;
        console.log("Region: " + CurrentRegion.Id);
        updateLabels();
        updateSample();
    }
     
    return true;
  }

  return false;
}

function updateOutsideSample()
{
  if(!(isCurrentLocationInside(GlobalRegion)))
  { 
    if(regionHasChanged(OutsideRegion))
    {
        CurrentRegion = OutsideRegion;
        console.log("Region: " + CurrentRegion.Id);
        updateLabels();
        updateSample();
    }
     
    return true;
  }

  return false;
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
  LatLabel.setText("Latitude : " + CurrentLocation.Latitude);
  LonLabel.setText("Longitude : " + CurrentLocation.Longitude);
  AltLabel.setText("Altitude : " + CurrentLocation.Altitude);
  RegionLabel.setText("Region : " +  CurrentRegion.Id);
  BatteryLifeLabel.setText("Battery Life : " + parseInt(device.getBatteryLevel()));
}

function updateOscValues(){

    var batteryLife = [];
    batteryLife.push(device.getBatteryLevel());
    OscClient.send("/batteryLife", batteryLife);
    
    var geoPosition = [];
    geoPosition.push(CurrentLocation.Latitude);
    geoPosition.push(CurrentLocation.Longitude);
    OscClient.send("/position", geoPosition);
}

function updateLocation(lat, lon, alt, speed, bearing)
{
    CurrentLocation = {Latitude: lat, Longitude: lon, Altitude: alt, Accuracy: 0, Speed: speed};
    
    updateLabels();    
    //updateOscValues();
    sendDataToServer();
    updateMap();
}

function isCurrentLocationValid(){
  return (CurrentLocation.Longitude>0&&CurrentLocation.Latitude>0);
}

function isCurrentRegionGlobal(){
    return (CurrentLocation.Latitude>=GlobalRegion.Lat1&&CurrentLocation.Latitude<=GlobalRegion.Lat2 &&
    CurrentLocation.Longitude>=GlobalRegion.Lon1&&CurrentLocation.Longitude<=GlobalRegion.Lon2);
}

function isCurrentLocationInside(region){

    return (CurrentLocation.Latitude>=region.Lat1&&CurrentLocation.Latitude<=region.Lat2 &&
            CurrentLocation.Longitude>=region.Lon1&&CurrentLocation.Longitude<=region.Lon2);
}

function regionHasChanged(region){
    return (CurrentRegion.Id!=region.Id);
}

function isRegionUpdatable(region){
   return (isCurrentLocationInside(region.Id) && regionHasChanged(region.Id));
}

function updateMap(){
  TownMap.moveTo(CurrentLocation.Latitude, CurrentLocation.Longitude);
  TownMap.showControls(true);
}

function isGlobalRegion(region){
  return (GlobalRegion.Id==region.Id);
}

function  updateSample(){
  
  var sampleName = CurrentRegion.SampleName;
  pd.sendMessage("sampleName", sampleName);
  pd.sendFloat("regionId", CurrentRegion.Id);
  console.log("Play sample: " + sampleName);

}
  
function sendDataToServer(){
  
  var date = getFormattedDate();
  //console.log("Date: " + date);
  var url = "http://o-a.info/qitnl/track.php?time=" + date +
           "&phone=" + MobileId +
           "&bat=" + device.getBatteryLevel() +
            "&region=" + CurrentRegion.Id +
            "&pos=" + CurrentLocation.Latitude + "," + CurrentLocation.Longitude +
            "&beacons=" + BeaconId + "," + BeaconStrength;
    //console.log(url); 
  network.httpGet(url, function(status, response) { 
      //console.log(status + " " + response); 
      //console.log(status + " " + response); 
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
    var dayInt = parseInt(day);
    if(dayInt<10){day = "0" + day;}
    //console.log("day: " + day);
    var hours = d.getHours();
    var hoursInt = parseInt(hours);
    if(hoursInt<10){hours = "0" + hours;}
    //console.log("hours: " + hours);
    var minutes = d.getMinutes();
    var minutesInt = parseInt(minutes);
    if(minutesInt<10){minutes = "0" + minutes;}
    //console.log("minutes: " + minutes);
    var seconds = d.getSeconds();
    var secondsInt = parseInt(seconds);
    if(secondsInt<10){seconds = "0" + seconds;}
    //console.log("seconds: " + seconds);
    var formattedString = year+month+day+hours+minutes+seconds;
    //console.log(formattedString);
    return formattedString;    //Return the date          
}



