package de.dosmike.twitch.knarf;

import com.itwookie.telnet.ReceiverCallback;
import com.itwookie.telnet.TelnetClient;
import com.itwookie.telnet.TelnetServer;

/*
 Telnet API design
< RegisterCommand commandName
> CE userName chatRank commandName args

< RegisterPropertyChange propertyName
> PE userName propertyName propertyValue

< RequestProperty userName propertyName
> PR userName propertyName propertyValue

< RequestProperty userName propertyName newPropertyValue
> PR* userName propertyName OK

< InvokeCommandFor userName commandName arguments
> IC* userName commandName OK

< ModuleRemote moduleName commandName arguments
> MR* moduleName commandName OK				- the module does not return information
> MR moduleName commandName response		- in case the module returns information
> MR* moduleName commandName errorMessage	- see errorMessage
> MR* moduleName commandName INVALID		- as in command not found
> MR* moduleName INVALID					- as in module not found

< invalidCommand
> * invalidCommand INVALID
 */

public class APItelnetServer implements ReceiverCallback {
	TelnetServer sv;
	
	public APItelnetServer(int port) {
		sv = new TelnetServer(port, this);
		sv.start();
	}
	
	public void halt() {
		sv.halt();
	}
	
	@Override
	public void onReceive(String message, TelnetClient client) {
		//TODO handle message
		String[] command = message.split(" ");
		if (command.length < 1)
			client.send("* INVALID");
		else
			client.send("* "+command[0]+" INVALID");
	}
}
