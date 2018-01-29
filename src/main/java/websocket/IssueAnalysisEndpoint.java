package websocket;

import ch.unibe.scg.curtys.vectorization.issue.Issue;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.apache.commons.lang3.StringUtils;

import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.logging.Logger;

@ServerEndpoint("/service")
public class IssueAnalysisEndpoint {

	private final static Logger LOG = Logger.getLogger(IssueAnalysisEndpoint.class.getName());

	@OnOpen
	public void onOpen(Session session) {
		LOG.info("connection established " + session.getId());
	}

    @OnError
	public void onError(Session session, Throwable e) {
    	try {
    		session.close(new CloseReason(CloseReason.CloseCodes.UNEXPECTED_CONDITION, "Server error."));
		} catch (IOException e1) {
			e1.printStackTrace();
		} finally {
    		e.printStackTrace();
		}
	}

    @OnMessage
    public void onMessage(Session session, String message) {
		LOG.info("message received: " + message);
        ObjectMapper mapper = new ObjectMapper();
        Protocol in;
        Protocol out = new Protocol(Protocol.Event.DOES_NOT_UNDERSTAND);
        Issue issue = null;
        try {
			in = mapper.readValue(message, Protocol.class);
			if (in != null && in.event == Protocol.Event.REQUEST &&
					!StringUtils.isBlank(in.payload)) {
				issue = mapper.readValue(in.payload, Issue.class);
				Result res = Controller.instance().analyse(issue);
				out = new Protocol(res);
			}
		} catch (IOException e) {
			LOG.info("Did not understand message: " + message);
			out = new Protocol(Protocol.Event.DOES_NOT_UNDERSTAND);
        } finally {
			try {
				send(session, out);
			} catch (IOException e) {
				LOG.warning("Could not send message to remote.");
				e.printStackTrace();
			} finally {
				try {
					session.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
        }
    }

	public static void send(Session session, Protocol msg)
			throws IOException {
		ObjectMapper mapper = new ObjectMapper();
		mapper.enable(SerializationFeature.WRITE_ENUMS_USING_INDEX);
		session.getBasicRemote().sendText(mapper.writeValueAsString(msg));
	}

}
