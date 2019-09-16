metadata {
  definition (name: "Owntracks Child MQTT Traking", namespace: "jorge.martinez", author: "Jorge Martinez") {
		capability "Initialize"
		capability "Sensor"
		capability "Battery"
	    capability "Beacon"
	    capability "EstimatedTimeOfArrival"
	    capability "PresenceSensor"
	    attribute "latitude", "float"
	    attribute "longitude", "float"
	    attribute "battery", "float"
	    attribute "speed ", "integrer"
    	attribute "lastUpdated", "String"
//	    command "SetLogLat", ["float" , "float"]
  }
	preferences {
	section("settings"){
			input "logEnable", "bool", title: "Enable Debug Logging?"
		}
		}
	}

def UpdateData (lat , lon, bat, spe) {
	sendEvent(name: "latitude", value: lat, displayed: false)
	sendEvent(name: "longitude", value: lon, displayed: false)
	sendEvent(name: "battery", value: bat, displayed: false)
	sendEvent(name: "speed", value: spe, displayed: false)

	def nowDay = new Date().format("MMM dd", location.timeZone)
    def nowTime = new Date().format("h:mm a", location.timeZone)
    sendEvent(name: "lastUpdated", value: nowDay + " at " + nowTime, displayed: false)
}
