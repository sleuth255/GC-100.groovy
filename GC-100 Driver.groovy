/**
 *  Global Cache GC-100 Control Device Type for Hubitat
 *  Kevin Wilcox
 *  Based on code by Carson Dallum and Bryan Turcotte
 *
 *  Usage:
 *  1. Add this code as a device driver in the Hubitat Drivers Code section
 *  2. Create a device using Global Cache GC-100 Digital Input Device as the device handler
 * 
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 */

metadata {
    definition (name: "Global Cache GC-100 Digital Input Device", namespace: "sleuth255", author: "Kevin Wilcox") {
    capability "Initialize"
    capability "Telnet"
    capability "Actuator"
    capability "Sensor"
    capability "ContactSensor"
    attribute "telnet", ""
    attribute "input1",""
    attribute "input2",""
    attribute "input3",""
    attribute "input4",""
    attribute "input5",""
    attribute "input6",""
	}
}

preferences() {    	
    section() {
        input "ipaddress", "text", required: true, title: "GC-100 IP Address", defaultValue: "0.0.0.0"
        input "logEnable", "bool", title: "Enable logging", required: true, defaultValue: true
    }
}

// device commands

def sendMsg(String msg) {
    log.debug "GC100 (sendMsg) - sending ${msg}"
	return new hubitat.device.HubAction("""$msg\r\n""",hubitat.device.Protocol.TELNET)
}

// General App Events
def initialize() {
	try {
		if(logEnable) log.debug "Opening telnet connection"
		    sendEvent(name: "telnet", value: "Opening")
		telnetConnect([termChars:[13]],"${ipaddress}", 4998, null, null)
		//give it a chance to start
		isConnected()
    } catch(e) {
		log.warn "Initialize Error: ${e.message}"
        sendEvent(name: "telnet", value: "Error")
    }
}

def parse(String msg) {
    if(logEnable) log.debug "GC100 (parse) - ${msg}"
	sendEvent(name: "telnet", value: "Connected")
    def String input = ""
    if (msg.indexOf("4:1") != -1)
        input = "input1"
    else
    if (msg.indexOf("4:2") != -1)
        input = "input2"
    else
    if (msg.indexOf("4:3") != -1)
        input = "input3"
    else
    if (msg.indexOf("5:1") != -1)
        input = "input4"
    else
    if (msg.indexOf("5:2") != -1)
        input = "input5"
    else
    if (msg.indexOf("5:3") != -1)
        input = "input6"
    
    if (input.length() > 0){
       sendEvent(name: "${input}", value: msg.substring(msg.length() - 1),isStateChange: true)
       if(logEnable) log.debug "GC100 (parse) - ${input} state set to ${msg.substring(msg.length() - 1)}"
    }
}

def isConnected() {
    pauseExecution(1000)
    def telnet1 = device.currentValue('telnet')
    if(logEnable) log.debug "GC100 (isConnected) - telnet status: ${telnet1}"
    if(telnet1 != "Error") {
        log.debug "GC100 - Telnet connection established"
        sendEvent(name: "telnet", value: "Connected")
    }
}

def installed(){
	initialize()
}

def updated(){
	initialize()
}

def telnetStatus(String status) {
	if(logEnable) log.debug "GC100 telnetStatus: ${status}"
	if (status == "receive error: Stream is closed" || status == "send error: Broken pipe (Write failed)") {
        sendEvent(name: "telnet", value: "Disconnected")
		telnetClose()
		// runIn(10, initialize)
	   initialize()
    } else {
        sendEvent(name: "telnet", value: "${status}")
    }
}

def telnetEvent(String event) {
	if(logEnable) log.debug "telnetStatus: ${event}"
}