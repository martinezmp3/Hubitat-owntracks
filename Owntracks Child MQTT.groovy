metadata {
  definition (name: "Owntracks Child MQTT Traking", namespace: "jorge.martinez", author: "Jorge Martinez") {
      capability "Initialize"
      capability "Sensor"
      capability "Battery"
      capability "Beacon"
      capability "EstimatedTimeOfArrival"
      capability "PresenceSensor"
      attribute "topic", "String"
      attribute "latitude", "float"
      attribute "longitude", "float"
      attribute "battery", "float"
      attribute "speed", "integrer"
      attribute "lastUpdated", "Date"
      attribute "DevicelastUpdated", "String"
      attribute "inregions", "String"
      attribute "configuartion", "String"
      attribute "stepsToday", "number"
      attribute "stepsWeek", "number"
      attribute "stepsYear", "number"
      attribute "stepsLiveTime", "number"
      attribute "epoch", "long" 
      attribute "communicado", "boolean"
      //command "SetLogLat", ["float" , "float"]
      //command "setBattery",["number","boolean"]
      command "RequestLocation"
      command "addWaysPoint", ["string", "number", "string", "string"]
      command "GetConfiguration"
      command "convertDateUnix", ["long"]
  }
	preferences {
	section("settings"){
			input "logEnable", "bool", title: "Enable Debug Logging?"
		}
		}
	}
def SetIncommunicado (){
    state.communicado = false
}
String convertDateUnix (edate){
    def newdate = new java.text.SimpleDateFormat("MM/dd/yyyy HH:mm:ss").format(new java.util.Date (edate*1000));
    log.debug newdate
    return newdate
}

def updateepoch (Nepoch){
    state.epoch = Nepoch
    log.debug convertDateUnix (Nepoch)
    state.DevicelastUpdated = convertDateUnix (Nepoch)
}


def updateconfiguartion(confg){
    log.debug confg
    state.configuartion = conf
}
def GetConfiguration(){
    parent.publishMsg ("${state.topic}/cmd",'{"_type":"cmd","action":"dump"}')
}
def RequestLocation (){
    parent.publishMsg ("${state.topic}/cmd",'{"_type": "cmd","action": "reportLocation"}')
}
def Updatetopic (top){
//    sendEvent(name: "topic", value: top)
    state.topic = top
}
def Updatelatitude (lat){
    sendEvent(name: "latitude", value: lat)
    state.latitude = lat
}
def Updatelongitude (lon){
    sendEvent(name: "longitude", value: lat)
    state.latitude = lat
}
def Updatebattery (bat){
    sendEvent(name: "battery", value: bat)
}
def Updatespeed (spe){
    sendEvent(name: "speed", value: spe)
}
def Updateinregions (region){
    sendEvent(name: "inregions", value: region)
    state.inregions = region
}
    
def lastUpdated(){
    def nowDay = new Date().format("MMM dd", location.timeZone)
    def nowTime = new Date().format("h:mm a", location.timeZone)
    state.lastUpdated = nowDay + " at " + nowTime
    sendEvent(name: "lastUpdated", value: nowDay + " at " + nowTime, displayed: false)
}
def Updatepresence (pre){sendEvent(name: "presence", value: pre)}
def UpdateData (lat, lon) {
    if (!communicado) state.communicado = true
	sendEvent(name: "latitude", value: lat)
    state.latitude = lat
	sendEvent(name: "longitude", value: lon)
    state.longitude = lon
}
