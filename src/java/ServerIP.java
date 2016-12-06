import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class ServerIP {

    private static String serverIP;
    private static BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

    public static void setServerIP() throws IOException{
        System.out.print("Set server ip: ");
        String IP = reader.readLine();
        String regex = "^(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$";

        if(IP.matches(regex)){
            serverIP = IP;
        }
        else{
            System.out.println("Wrong format of IP adress");
            setServerIP();
        }
    }

    public static String getServerIP(){
        return serverIP;
    }
}
