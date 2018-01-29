package websocket;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author curtys
 */
public class Protocol {
	public Event event;
	public String payload;

	public Protocol() {}
	public Protocol(Result res) {
		this.event = Event.RESULT;
		ObjectMapper mapper = new ObjectMapper();
		try {
			this.payload = mapper.writeValueAsString(res);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
	}
	public Protocol(Event event) {
		this.event = event;
	}


	enum Event {
		DOES_NOT_UNDERSTAND,
		RESULT,
		REQUEST
	}
}
