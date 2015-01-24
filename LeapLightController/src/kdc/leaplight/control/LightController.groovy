package kdc.leaplight.control

import groovy.json.JsonBuilder

class LightController {

	private final String baseUrl = "http://192.168.1.119/api/${new File('hash.txt').text}/lights/"

	public void setLightsTo(LightState lightState) {
		def json = new JsonBuilder()
		json {
			bri lightState.brightness.value;
			xy( [lightState.color.x, lightState.color.y] )
			effect 'none'
		}

		send(json)
	}

	private void send(json) {
		[3, 4, 1, 7, 8, 9].each{ lamp ->
			println json

			def connection = new URL("${baseUrl}${lamp}/state").openConnection()
			connection.requestMethod = 'PUT'
			connection.doOutput = true

			Writer writer = new OutputStreamWriter(connection.outputStream)
			writer << json
			writer.flush()
			writer.close()

			connection.connect()
			def message = connection.content.text
			if (message.contains('error')) println message
		}
	}
}
