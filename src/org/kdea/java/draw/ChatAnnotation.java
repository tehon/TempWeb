package org.kdea.java.draw;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpSession;
import javax.websocket.EndpointConfig;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;
import org.json.simple.*;
import org.json.simple.parser.*;

// 톰캣이 제공하는 서버api를 이용해서 만든 웹소켓
//우리가 만든클래스지만 서버쪽 소켓이 된다
@ServerEndpoint(value = "/websocket/networkdraw", configurator = ServletAwareConfig.class) // 클라이언트가 접속할 때 사용될 URI ServletAwareConfig는 만들어야한다
// @ServerEndpoint(value = "/websocket/chat") //클라이언트가 접속할 때 사용될 URI
public class ChatAnnotation {

	private static Map<String, Session> sessionMap = new HashMap<>();
	private HttpSession httpSession;

	@OnOpen // 커넥션이 처음 열리면= 이용자가 처음접속하면 돌아간다 소켓이 열리면
	public void start(Session session, EndpointConfig config) {

		this.httpSession = (HttpSession) config.getUserProperties().get(HttpSession.class.getName());
		String userId = (String) httpSession.getAttribute("id");
		System.out.println("접속한 클라이언트 id" + userId);

		sessionMap.put(userId, session);

		Object objList = httpSession.getServletContext().getAttribute("usrList");
		if (objList == null) {// 채팅방에 들어온 최초사람에게 이 게 생김
			List<String> usrList = new ArrayList<>();
			httpSession.getServletContext().setAttribute("usrList", usrList);
			//usrList.add("all"); <전체한테 보내기 기능을 만들기 위해 여기 맵에 넣어서 맨위에 이걸 넣어줌
			objList = usrList;
		}
		List<String> usrList = (List<String>) objList;
		usrList.add(userId);

	}

	@OnClose
	public void end() {
		String usrId = (String) httpSession.getAttribute("id");
		sessionMap.remove(usrId);

		Object objList = httpSession.getServletContext().getAttribute("usrList");
		List<String> usrList = (List<String>) objList;
		usrList.remove(usrId);
	}

	// 현재 세션과 연결된 클라이언트로부터 메시지가 도착할 때마다 새로운 쓰레드가 실행되어 incoming()을 호출함
	@OnMessage
	public void incoming(String message) {

		String threadName = "Thread-Name:" + Thread.currentThread().getName();

		if (message == null || message.trim().equals("")){
			return;
		}

		JSONParser jsonParser = new JSONParser();
		try {
			JSONObject jsonObj = (JSONObject) jsonParser.parse(message);
			String sender = (String) jsonObj.get("sender");
			String receiver = (String) jsonObj.get("receiver");
			String x1 = (String) jsonObj.get("x1");
			System.out.println("clear"+jsonObj.get("clear"));
			/*if(receiver.equals("all")){
				while (en.hasMoreElements()) {
					type type = (type) en.nextElement();
					
				}
				sessionMap.get(receiver).getBasicRemote().sendText(message);
				
			}*/
			try {
				sessionMap.get(receiver).getBasicRemote().sendText(message);
				
				return;
			} catch (IOException e) {
				e.printStackTrace();
			}
		} catch (ParseException e) {
			e.printStackTrace();
		}

	}

	@OnError
	public void onError(Throwable t) throws Throwable {
		System.err.println("Chat Error: " + t.toString());
	}

}