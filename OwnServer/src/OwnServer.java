import java.awt.AWTException;
import java.awt.MouseInfo;
import java.awt.PointerInfo;
import java.awt.Robot;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import javax.swing.JOptionPane;
import Opcode.SendPacketOpcode;
import Opcode.RecvPacketOpcode;
import java.awt.Robot;
import java.awt.event.InputEvent;

public class OwnServer {
	static boolean isRunning = true;
	
	public static void main(String[] args) throws IOException, AWTException {
		// TODO Auto-generated method stub
		ServerSocket serverSocket = new ServerSocket(9000); // binding port
		System.out.println("Server Binding");
		Socket socket = null;
		socket = serverSocket.accept();
		
		System.out.println("Client Connecting...");
		System.out.println("Local Port : " + socket.getPort());
		System.out.println("IP Address" + socket.getRemoteSocketAddress());
		
		DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
		DataInputStream dis = new DataInputStream(socket.getInputStream());
		
		while(true) {
			PointerInfo pos = MouseInfo.getPointerInfo();
			
			int opcode = dis.readInt();
			
			if(opcode == RecvPacketOpcode.CON.getValue()) {
				dos.writeInt(SendPacketOpcode.MOUSE_POS.getValue());
				dos.writeInt(pos.getLocation().x);
				dos.writeInt(pos.getLocation().y);
			} else if(opcode == RecvPacketOpcode.MOUSE_REQ.getValue()) {
				int x = dis.readInt();
				int y = dis.readInt();
				Robot bot = new Robot();
				bot.mouseMove(pos.getLocation().x + x, pos.getLocation().y + y);
				System.out.println("Mouse Moved");
			} else if(opcode == RecvPacketOpcode.SERVER_EXIT.getValue()) {
				System.out.println("Server Closed");
				isRunning = false;
			} else if(opcode == RecvPacketOpcode.SHOW_MESSAGE.getValue()) {
				String msg = dis.readUTF();
				JOptionPane.showMessageDialog(null, msg);
				System.out.println("Show Message : " + msg);
			} else if(opcode == RecvPacketOpcode.LEFT_CLICK.getValue()) {
				Robot bot = new Robot();
				bot.mousePress(InputEvent.BUTTON1_MASK);
			    bot.mouseRelease(InputEvent.BUTTON1_MASK);
			    System.out.println("Left Clicked");
			} else if(opcode == RecvPacketOpcode.RIGHT_CLICK.getValue()) {
				Robot bot = new Robot();
				bot.mousePress(InputEvent.BUTTON3_MASK);
			    bot.mouseRelease(InputEvent.BUTTON3_MASK);
			    System.out.println("Right Clicked");
			} else if(opcode == RecvPacketOpcode.SCROLL_UP.getValue()) {
				Robot bot = new Robot();
				bot.mouseWheel(100);
				System.out.println("Scrolled Up");
			} else if(opcode == RecvPacketOpcode.SCROLL_DOWN.getValue()) {
				Robot bot = new Robot();
				bot.mouseWheel(-100);
				System.out.println("Scrolled Down");
			}
			if(!isRunning)
				break;
		}
		socket.close();
		serverSocket.close();
		System.exit(0);
	} 
}
