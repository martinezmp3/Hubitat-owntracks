metadata {
    definition (name: "Owntracks Parent MQTT Traking", , namespace: "jorge.martinez", author: "Jorge Martinez"){
        capability "Initialize"
		capability "Sensor"
		capability "TemperatureMeasurement"
		command "publishMsg", ["string", "string"]
//		command "Open"
//		command "Close"
    }
    preferences {
		section("Device Settings:") 
		{
            input name: "logEnable", type: "bool", title: "Enable debug logging", defaultValue: false
			input "IP", "String", title:"MQTT Server IP", description: "", required: true, displayDuringSetup: true
			input "Port", "NUMBER", title:"MQTT Server port", description: "", required: true, displayDuringSetup: true , defaultValue: 1883
			input "User", "String", title:"MQTT Server User", description: "", required: false, displayDuringSetup: true , defaultValue: "admin"
            input "HomeWaypoint", "String", title:"Waypoint Assing to home", description: "", required: false, displayDuringSetup: true, defaultValue: "Home"
			input "Password", "String", title:"MQTT Server Password", description: "", required: false, displayDuringSetup: true
			input "BaseTopic", "String", title:"Base Topic", description: "", required: false, displayDuringSetup: true, defaultValue: "owntracks"
		}
    }
}
void publishMsg(topic,payload) {
    if (logEnable) log.debug "publish topic:${topic} Payload ${payload}"
    interfaces.mqtt.publish(topic, payload,2)
    
}
// Parse incoming device messages to generate events
void parse(String description){
	log.debug "parse call"
	def responce = interfaces.mqtt.parseMessage(description)
    //*****new approach********************
    def data = responce.get('topic')
    def start = settings.BaseTopic.length()+1
    def end = data.indexOf("/",start)
    def user = data.substring(start,end)
    def children = getChildDevice("Owntracks-child-${user}")
    if (!children){
        if (logEnable) log.debug "Children NOT found creating one"
        children = addChildDevice("jorge.martinez","Owntracks Child MQTT Traking", "Owntracks-child-${user}", [name: "Owntracks-${user}", label: user, isComponent: false])
        children.Updatetopic(responce.get('topic'))
    }
    data = responce.get('payload')
    if (data.contains('"_type":"lwt"')){
        end = data.indexOf(",",start)
        log.debug data.substring(start,end)
        children.updateepoch (data.substring(start,end).toLong()) 
        children.lastUpdated()
        children.SetIncommunicado()
    }
    if (data.contains('"_type":"location"')){//if data is an location update 
        if (logEnable) log.debug "Location update for ${user}"
        //latitude
        start = data.indexOf("lat")+5
		end = data.indexOf(",",start)
		def lat = data.substring(start,end)
        //longitude
       	start = data.indexOf("lon")+5
		end = data.indexOf(",",start)
		def lon = data.substring(start,end)
        //epoch
        start = data.indexOf("tst")+5
        end = data.indexOf(",",start)
        log.debug data.substring(start,end)
        children.updateepoch (data.substring(start,end).toLong())        
        children.UpdateData(lat,lon)
        children.lastUpdated()      
		//battery
		start = data.indexOf("batt")+6
		end = data.indexOf(",",start)
        children.Updatebattery(data.substring(start,end))
		//speed
		if (data.contains("vel")){
			start = data.indexOf("vel")+5
			end = data.indexOf("}",start)
			if (end > start+3)	end = data.indexOf(",",start)
            children.Updatespeed (data.substring(start,end))
        }
        if (data.contains("inregions")){
            start = data.indexOf("inregions")+13
			end = data.indexOf(",",start)-2
			region = data.substring(start,end)
			if (logEnable) log.debug inregions
            children.Updateinregions(region)
            if (region == settings.HomeWaypoint)children.Updatepresence("present")
            if (region != settings.HomeWaypoint)children.Updatepresence("not present")
        }
        if (!data.contains("inregions")){
            children.Updatepresence("not present")
            children.Updateinregions("no region")
        }
    }//end location update
    if (data.contains('"_type":"dump"')){//if data is a configuration info
        if (logEnable) log.debug "Configuration update for ${user}"
        children.updateconfiguartion(data.toString())
    }
    
//***************end of new approach*******************


}
def updated(){
	log.info('Owntracks Parent MQTT Traking: updated()')
	initialize()
}


void initialize() {
    try {
		def mqttInt = interfaces.mqtt
        //open connection
		if (!settings.User){
			mqttInt.connect("tcp://${settings.IP}:${settings.Port}", device.deviceNetworkId, null, null)
		}
		if (settings.User){
			mqttInt.connect("tcp://${settings.IP}:${settings.Port}", device.deviceNetworkId, settings.User, settings.Password)
		}
        //give it a chance to start
        pauseExecution(1000)
        log.info "connection established"
        mqttInt.subscribe("${settings.BaseTopic}/#")
    } catch(e) {
        log.debug "initialize error: ${e.message}"
    }//deviceNetworkId
//	log.info device.deviceNetworkId
//	log.info device.getName() 
//	log.info device.displayName
}
void uninstalled() {
    log.info "disconnecting from mqtt"
    interfaces.mqtt.disconnect()
}
void mqttClientStatus(String message) {	log.info "Received status message ${message}"}
void on(){
	publishMsg("on")
}
void off (){
	publishMsg("off")
}
