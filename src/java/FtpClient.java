import java.net.Socket;

public class FtpClient {

        public static void main (String[] args) {
            try {
                Socket clientComunicationSocket = new Socket("127.0.0.1",21);
                FileTransferProtocol transferFile = new FileTransferProtocol(clientComunicationSocket);
                transferFile.menu();
            }
            catch(Exception ex)
            {
                System.out.println(ex);
            }
        }



    }
