import java.io.*;
import java.net.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class FileTransferProtocol {

    String home = System.getProperty("user.dir")+"\\FTPClient\\";

    Socket clientCommunicationSocket;
    InputStreamReader clientCommunicationDataInput;
    OutputStreamWriter clientCommunicationDataOutput;

    BufferedReader reader,commandReader;


    BufferedInputStream fileInput;
    BufferedOutputStream fileOutput;


    public FileTransferProtocol(Socket clientCommunicationSocket){

        try {
            this.clientCommunicationSocket = clientCommunicationSocket;
            clientCommunicationDataInput = new InputStreamReader(clientCommunicationSocket.getInputStream());
            commandReader = new BufferedReader(clientCommunicationDataInput);
            clientCommunicationDataOutput = new OutputStreamWriter(clientCommunicationSocket.getOutputStream(),"UTF-8");
            reader = new BufferedReader(new InputStreamReader(System.in));
            menu();
        }
        catch(Exception ex){}

    }

    public void logToFTP() throws Exception{

        String user, password;
        while(true) {
            System.out.print("User: ");
            user = reader.readLine();
            System.out.print("Password: ");
            password = reader.readLine();
            user = user + "\n";
            password = password + "\n";


            clientCommunicationDataOutput.write("USER\n",0,"USER\n".length());
            clientCommunicationDataOutput.flush();
            clientCommunicationDataOutput.write(user,0,user.length());
            clientCommunicationDataOutput.flush();
            String serverLoginResponse = commandReader.readLine();


            clientCommunicationDataOutput.write("PASS\n",0,"PASS\n".length());
            clientCommunicationDataOutput.flush();
            clientCommunicationDataOutput.write(password,0,password.length());
            clientCommunicationDataOutput.flush();
            String serverPasswordResponse = commandReader.readLine();


            if (serverLoginResponse.compareTo("331 User name okay, need password") == 0
                    && serverPasswordResponse.compareTo("230 User logged in, proceed") == 0) {
                break;
            }
            else {}

        }


    }

    void sendFile() throws Exception
    {
        try {
            Socket clientTransferSocket = new Socket(ServerIP.getServerIP(), 1200); //127.0.0.1
            if(listDir() == 1){
                System.out.println("451 Empty local directory");
                clientCommunicationDataOutput.write("Empty directory\n",0,"Empty directory\n".length());
                clientCommunicationDataOutput.flush();
                commandReader.readLine();
                commandReader.readLine();
                return;
            }

            System.out.println();
            String filePath, fileExtension, filename;



            System.out.print("Enter filename to put on server: ");
            filePath = home + reader.readLine();

            Pattern trimPattern = Pattern.compile("(\\.[a-z0-9]*)");
            Matcher matcher = trimPattern.matcher(filePath);
            if (matcher.find()) {
                fileExtension = matcher.group(1);
            } else {
                System.out.println("500 Syntax error, wrong path");
                clientCommunicationDataOutput.write("Wrong path\n",0,"Wrong path\n".length());
                clientCommunicationDataOutput.flush();
                commandReader.readLine();
                commandReader.readLine();
                return;
            }

            trimPattern = Pattern.compile("([a-zA-Z0-9]*\\.[a-z0-9]*)");
            matcher = trimPattern.matcher(filePath);
            if (matcher.find()) {
                filename = matcher.group(1);
            } else {
                System.out.println("Wrong path");
                return;
            }



            String fileServerPath = "C:/Users/Krolik/IdeaProjects/NiceFTP-Server/FTPServer/" + filename;


            File file = new File(filePath);
            if (!file.exists()) {
                System.out.println("550 File not found");
                clientCommunicationDataOutput.write("File not found\n",0,"File not found\n".length());
                clientCommunicationDataOutput.flush();
                commandReader.readLine();
                commandReader.readLine();
                return;
            }
            fileServerPath = fileServerPath + "\n";
            clientCommunicationDataOutput.write(fileServerPath,0,fileServerPath.length());
            clientCommunicationDataOutput.flush();
            fileServerPath = fileServerPath.replaceAll("\n","");


            String msgFromServer = commandReader.readLine();

            if (msgFromServer.compareTo("450 Requested file action not taken; File Already Exists; Continue anyway?") == 0) {
                String Option;
                System.out.println("File Already Exists On The Server. Do you want to continue anyway (Y/N) ?");
                Option = reader.readLine();
                if (Option.equals("Y")) {
                    clientCommunicationDataOutput.write("Y\n",0,"Y\n".length());
                    clientCommunicationDataOutput.flush();
                } else {
                    clientCommunicationDataOutput.write("N\n",0,"N\n".length());
                    clientCommunicationDataOutput.flush();
                    clientTransferSocket.close();
                    return;
                }
            }


            String sendToDirPath, sendToThisPath;
            sendToDirPath = "C:/Users/Krolik/IdeaProjects/NiceFTP-Server/FTPServer/";
            System.out.print("Set filename: ");
            sendToThisPath = sendToDirPath + reader.readLine() + fileExtension;
            sendToThisPath = sendToThisPath + "\n";
            clientCommunicationDataOutput.write(sendToThisPath,0,sendToThisPath.length());
            clientCommunicationDataOutput.flush();
            sendToThisPath = sendToThisPath.replaceAll("\n","");


            InputStream fin = new FileInputStream(file);
            fileInput = new BufferedInputStream(fin);
            fileOutput = new BufferedOutputStream(clientTransferSocket.getOutputStream());


            commandReader.readLine();
            System.out.println("Sending File ...");

            byte[] buffer = new byte[2048];
            int bytesRead;

            while ((bytesRead = fileInput.read(buffer)) != -1) {
                fileOutput.write(buffer, 0, bytesRead);
            }

            fileOutput.flush();
            fileOutput.close();
            fileInput.close();
            fin.close();
            commandReader.readLine();

            commandReader.readLine();

            System.out.println("\n***********************************************\n");
            System.out.println("File Was Sent Successfully");
            System.out.println("\n***********************************************\n");
            clientTransferSocket.close();
        }
        catch(Exception ex){
            System.out.println(ex);


            return;
        }

    }


    void receiveFile() throws Exception
    {
        String serverHomeDir = listServerDir();
        if(serverHomeDir.compareTo("1")==0) {

            return;
        }
        Socket clientTransferSocket = new Socket(ServerIP.getServerIP(),1200); //połączenie na porcie 1200 192.168.0.10
        String filePath, fileExtension;
        System.out.print("Enter filename to receive: ");
        filePath=serverHomeDir + reader.readLine();

        Pattern trimPattern = Pattern.compile("(\\.[a-z0-9]*)");
        Matcher matcher = trimPattern.matcher(filePath);
        if(matcher.find()){
            fileExtension = matcher.group(1);
        }
        else {
            System.out.println("Wrong path");
            fileExtension = "";
        }


        filePath = filePath + "\n";
        clientCommunicationDataOutput.write(filePath,0,filePath.length());
        clientCommunicationDataOutput.flush();
        filePath = filePath.replaceAll("\n","");

        String msgFromServer= commandReader.readLine();

        if(msgFromServer.compareTo(" File Not Found")==0)
        {
            commandReader.readLine();
            System.out.println("File not found on the Server ...");
            return;
        }
        else if(msgFromServer.compareTo(" 150 OK")==0)
        {

            String saveToThisPath;

            System.out.print("Set filename: ");
            saveToThisPath = home + reader.readLine() + fileExtension;


            File f=new File(saveToThisPath);
            if(f.exists())
            {
                String Option;
                System.out.println("File Already Exists. Want to Save it Anyway (Y/N) ?");
                Option=reader.readLine();
                if(Option=="N")
                {
                    clientCommunicationDataOutput.write("552 Requested file action aborted\n",0,"552 Requested file action aborted\n".length());
                    clientCommunicationDataOutput.flush();
                    return;
                }
            }
            clientCommunicationDataOutput.write("150 OK\n",0,"150 OK\n".length());
            clientCommunicationDataOutput.flush();



            fileInput = new BufferedInputStream(clientTransferSocket.getInputStream());
            FileOutputStream fout=new FileOutputStream(saveToThisPath);
            fileOutput = new BufferedOutputStream(fout);

            System.out.println("Receiving File ...");

            int i;

            commandReader.readLine();

            while((i = fileInput.read()) != -1){
                fileOutput.write(i);
            }

            fileOutput.close();
            fileInput.close();
            clientTransferSocket.close();
            fout.close();

            System.out.println("\n***********************************************\n");
            System.out.println("File Saved Successfully");
            System.out.println("\n***********************************************\n");
            commandReader.readLine();
        }
    }

    public void deleteFile(){
        try {
            String serverHomeDir = listServerDir();
            String filePath;
            System.out.print("Enter filename to delete: ");
            filePath = serverHomeDir + reader.readLine();
            filePath = filePath + "\n";
            clientCommunicationDataOutput.write(filePath,0,filePath.length());
            clientCommunicationDataOutput.flush();
            filePath = filePath.replaceAll("\n","");

            String msg = commandReader.readLine();

            if(msg.compareTo(" 250 Requested file action okay, completed") == 0){
                System.out.println("\n***********************************************\n");
                System.out.println("File Was Deleted Successfully");
                System.out.println("\n***********************************************\n");
            }
            else if(msg.compareTo(" 550 Requested action not taken; file not found") == 0){
                String exceptionText = commandReader.readLine();
                System.out.println(exceptionText);
            }
            else if(msg.compareTo(" 550 Requested action not taken; directory not empty") == 0){
                System.out.println("!!! Directory is not empty !!!");
            }
            else {
                System.out.println("!!! Unidentified exception occurred !!!");
            }
        }
        catch(IOException exception){
            System.out.println(exception);
        }
        catch(Exception ex){
            System.out.println("Something wrong with server listing");
        }

    }
    int listDir() throws Exception {

        String dirPath = home;

        File dir = new File(dirPath);
        File[] files = dir.listFiles();


        if(files.length == 0){
            return 1;
        }
        else{
            System.out.println("\n*********************************************************************************************\n");
            for(File temp : files){
                System.out.println(temp.getName() + " " + temp.length() + " " + temp.getAbsolutePath());
            }
            System.out.println("\n*********************************************************************************************\n");
        }
        return 0;
    }

    String listServerDir() throws Exception {

        commandReader.readLine();
        Socket transferSocket = new Socket(ServerIP.getServerIP(),1200);

        ObjectInputStream in = new ObjectInputStream(transferSocket.getInputStream());
        String serverHomeDir = (String)in.readObject();
        FileInfo[] info = (FileInfo[])in.readObject();

        if(info.length == 0){
            System.out.println("Empty directory");
            clientCommunicationDataOutput.write("Empty directory\n",0,"Empty directory\n".length());
            clientCommunicationDataOutput.flush();
            commandReader.readLine();
            transferSocket.close();
            return "1";
        }
        else{
            System.out.println("\n*********************************************************************************************\n");
            for(FileInfo temp : info){
                System.out.println(temp.getFileName() + " " + temp.getFileLength() + " " + temp.getFileAbsolutePath());
            }
            System.out.println("\n*********************************************************************************************\n");
        }
        clientCommunicationDataOutput.write("OK\n",0,"OK\n".length());
        clientCommunicationDataOutput.flush();
        commandReader.readLine();
        transferSocket.close();

        return serverHomeDir;
    }



    void menu() throws Exception {
        String msg = commandReader.readLine();
        logToFTP();
        while(true)
        {

            System.out.println("\n\t  [ MENU ]");
            System.out.println("1. APPE (APPEND Data)");
            System.out.println("2. RETR (RETRIVE Data)");
            System.out.println("3. LIST (LIST Files on Server)");
            System.out.println("4. DELE (DELETE)");
            System.out.println("5. QUIT (DISCONNECT)");
            System.out.print("\nEnter Choice : ");
            int choice;
            choice=Integer.parseInt(reader.readLine());
            if(choice==1)
            {
                clientCommunicationDataOutput.write("APPE\n",0,"APPE\n".length());
                clientCommunicationDataOutput.flush();
                sendFile();

            }
            else if(choice==2)
            {
                clientCommunicationDataOutput.write("RETR\n",0,"RETR\n".length());
                clientCommunicationDataOutput.flush();
                receiveFile();

            }
            else if(choice==3){

                clientCommunicationDataOutput.write("LIST\n",0,"LIST\n".length());
                clientCommunicationDataOutput.flush();
                listServerDir();
            }
            else if(choice==4){
                clientCommunicationDataOutput.write("DELE\n",0,"DELE\n".length());
                clientCommunicationDataOutput.flush();
                deleteFile();
            }
            else
            {
                clientCommunicationDataOutput.write("QUIT\n",0,"QUIT\n".length());
                clientCommunicationDataOutput.flush();
                System.out.println(commandReader.readLine());
                System.exit(0);
            }
        }
    }
}