package com.sky.websocket;

import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 这里就相当于一个servelet,每个连接对应一个WebSocketServer实例对象，我们将这些实例对象放入ConcurrentHashMap中
 * 后续就可以通过取这个webSocket来进行群发了
 * @author raoxin
 */
@Component
@ServerEndpoint("/ws/{sid}")  // WebSocket 支持路径参数
public class WebSocketServer {
    //如果是来单提醒就是1 客户催单就是2
    public static final Integer TYPE_ORDER_COMMING = 1;
    public static final Integer TYPE_ORDER_REMIND = 2;
    private static final ConcurrentHashMap<String, WebSocketServer> clients = new ConcurrentHashMap<>();
    private static final List<String> messageList = new LinkedList<>();
    private Session session;
    private String sid; // 记录当前会话的ID

    @OnOpen
    public void onOpen(Session session, @PathParam("sid") String sid) {
        this.session = session;
        this.sid = sid;
        clients.put(sid, this);
        System.out.println("新连接：" + sid + "，当前在线用户数：" + clients.size());
        //一旦有用户上线就将所有堆积的消息发给他
        if (messageList.size() > 0){

            for (String message : messageList){
                sendToAll(message);
                messageList.remove(message);
            }
        }
    }

    @OnMessage
    public void onMessage(String message) {
        System.out.println("收到消息：" + message + "，来自：" + sid);
        sendMessage("服务器收到消息：" + message);
    }

    @OnClose
    public void onClose() {
        clients.remove(this.sid);
        System.out.println("连接关闭：" + sid + "，当前在线用户数：" + clients.size());
    }

    @OnError
    public void onError(Session session, Throwable error) {
        System.err.println("WebSocket 发生错误：" + error.getMessage());
    }

    /** 发送消息给当前客户端 */
    public void sendMessage(String message) {
        try {
            this.session.getBasicRemote().sendText(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /** 发送消息给指定客户端 */
    public static void sendToClient(String sid, String message) {
        WebSocketServer client = clients.get(sid);
        if (client != null) {
            client.sendMessage(message);
        } else {
            System.out.println("客户端 " + sid + " 不在线");
        }
    }

    /** 群发消息给所有在线用户 */
    public static void sendToAll(String message) {
        // 如果没有客户端在线，则将消息放入队列中，等有客户端上线时再发送
        if (clients.isEmpty()){
            messageList.add(message);
            return;
        }
        for (WebSocketServer client : clients.values()) {
            client.sendMessage(message);
        }
    }
}