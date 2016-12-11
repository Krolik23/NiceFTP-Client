import java.io.IOException;
import java.net.Socket;

public class FtpClient {

        public static void main (String[] args) throws IOException {

                ServerIP.setServerIP();
                Socket clientCommunicationSocket = new Socket(ServerIP.getServerIP(),21); // 192.168.0.10 127.0.0.1
                FileTransferProtocol transferFile = new FileTransferProtocol(clientCommunicationSocket);
        }



    }
