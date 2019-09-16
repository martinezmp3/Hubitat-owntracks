metadata {
    definition (name: "Owntracks Parent MQTT Traking", , namespace: "jorge.martinez", author: "Jorge Martinez"){
        capability "Initialize"
		capability "Sensor"
		capability "TemperatureMeasurement"
//		command "publishMsg", ["String"]
//		command "Open"
//		command "Close"
    }
    preferences {
		section("Device Settings:") 
		{
			input "IP", "String", title:"MQTT Server IP", description: "", required: true, displayDuringSetup: true
			input "Port", "NUMBER", title:"MQTT Server port", description: "", required: true, displayDuringSetup: true
			input "User", "String", title:"MQTT Server User", description: "", required: false, displayDuringSetup: true
			input "Password", "String", title:"MQTT Server Password", description: "", required: false, displayDuringSetup: true
			input "BaseTopic", "String", title:"Base Topic", description: "", required: false, displayDuringSetup: true
		}
    }
}
void publishMsg(String s) {
//	log.warn "/test/hubitat"  {"clearCache": true}   cmnd/POWER
//    interfaces.mqtt.publish((settings.BaseTopic + "cmnd/POWER"), s)
}
// Parse incoming device messages to generate events
void parse(String description){
	log.debug "parse call"
	def responce = interfaces.mqtt.parseMessage(description)
//	log.debug "topic: " +responce.get ('topic')
//	log.debug "payload:" +responce.get ('payload')

	def data = responce.get('topic')
	def start = settings.BaseTopic.length()-1
	def end = data.indexOf("/",start)
	def user = data.substring(start,end)
	log.debug user
	def children = getChildDevices()
	def found = false
	if (!children){
			log.debug "you dont have children yet"
			addChildDevice("jorge.martinez","Owntracks Child MQTT Traking", "Owntracks-child-${user}", [name: "Owntracks-${user}", label: user, isComponent: false])
		}
	childDevices.each {
		//	log.debug "We found ${it.name}"
			if (it.name == "Owntracks-${user}"){
				data = responce.get('payload')
				//lat
				start = data.indexOf("lat")+5
				end = data.indexOf(",",start)
				def lat = data.substring(start,end)
				//lon
				start = data.indexOf("lon")+5
				end = data.indexOf(",",start)
				def lon = data.substring(start,end)
				//battery
				start = data.indexOf("batt")+6
				end = data.indexOf(",",start)
				def bat = data.substring(start,end)
				//speed
				def spe = 0
				if (data.contains("vel")){
					start = data.indexOf("vel")+5
					end = data.indexOf("}",start)
					if (end > start+3){
						end = data.indexOf(",",start)
					}
					spe = data.substring(start,end)
				}
				def inregions = ""
				if (data.contains("inregions")){
					start = data.indexOf("inregions")+13
					end = data.indexOf(",",start)-2
					inregions = data.substring(start,end)
					log.debug inregions
				}
//				log.debug "lat:${lat} lon:${lon} bat:${bat} speed:${spe}"
				it.UpdateData(lat,lon,bat,spe)
				found = true
			}
		}
	if (!found){
			log.debug "childrend not found"
			addChildDevice("jorge.martinez","Owntracks Child MQTT Traking", "Owntracks-child-${user}", [name: "Owntracks-${user}", label: user, isComponent: false])
		}
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
		mqttInt.subscribe(settings.BaseTopic)
    } catch(e) {
        log.debug "initialize error: ${e.message}"
    }//deviceNetworkId
//	log.info device.deviceNetworkId
//	log.info device.getName() 
//	log.info device.displayName
}

void mqttClientStatus(String message) {	log.info "Received status message ${message}"}
void on(){
	publishMsg("on")
}
void off (){
	publishMsg("off")
}
