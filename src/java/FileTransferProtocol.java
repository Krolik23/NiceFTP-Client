import java.io.*;
import java.net.Socket;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.io.Console;


public class FileTransferProtocol {

    Socket clientCommunicationSocket;
    DataInputStream clientCommunicationDataInput;
    DataOutputStream clientCommunicationDataOutput;
    BufferedReader reader;


    BufferedInputStream fileInput;
    BufferedOutputStream fileOutput;


    public FileTransferProtocol(Socket clientCommunicationSocket){

        try {
            this.clientCommunicationSocket = clientCommunicationSocket;
            clientCommunicationDataInput = new DataInputStream(clientCommunicationSocket.getInputStream());
            clientCommunicationDataOutput = new DataOutputStream(clientCommunicationSocket.getOutputStream());
            reader = new BufferedReader(new InputStreamReader(System.in));
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

            clientCommunicationDataOutput.writeUTF("USER");
            clientCommunicationDataOutput.writeUTF(user);
            String serverLoginResponse = clientCommunicationDataInput.readUTF();

            clientCommunicationDataOutput.writeUTF("PASS");
            clientCommunicationDataOutput.writeUTF(password);
            String serverPasswordResponse = clientCommunicationDataInput.readUTF();


            if (serverLoginResponse.compareTo("331 User name okay, need password") == 0
                    && serverPasswordResponse.compareTo("230 User logged in, proceed") == 0) {
                break;
            }
            else {}

        }


    }

    void sendFile() throws Exception
    {

        listDir();
        System.out.println();
        String filePath, fileExtension, filename;
        System.out.print("Enter File Path :");
        filePath=reader.readLine();

        Pattern trimPattern = Pattern.compile("(\\.[a-z0-9]*)");
        Matcher matcher = trimPattern.matcher(filePath);
        if(matcher.find()){
            fileExtension = matcher.group(1);
        }
        else {fileExtension = "";}

        trimPattern = Pattern.compile("([a-zA-Z0-9]*\\.[a-z0-9]*)");
        matcher = trimPattern.matcher(filePath);
        if(matcher.find()){
            filename = matcher.group(1);
        }
        else {
            filename = "";
            System.out.println("Wrong path");
        }


        String fileServerPath = "C:/Users/Królik/IdeaProjects/NiceFTP-Server/FTPServer/" + filename;
        File fileOnServer = new File(fileServerPath);
        File file = new File(filePath);
        if(!file.exists())
        {
            System.out.println("File not Exists...");
            clientCommunicationDataOutput.writeUTF("File not found");
            clientCommunicationDataInput.readUTF();
            return;
        }
        clientCommunicationDataOutput.writeUTF(fileServerPath);



        String msgFromServer= clientCommunicationDataInput.readUTF();
        if(msgFromServer.compareTo(" 450 Requested file action not taken; File Already Exists")==0)
        {
            String Option;
            System.out.println("File Already Exists On The Server. Do you want to Append to that file (Y/N) ?");
            Option=reader.readLine();
            if(Option.equals("Y"))
            {
                clientCommunicationDataOutput.writeUTF("Y");
            }
            else
            {
                clientCommunicationDataOutput.writeUTF("N");
                return;
            }
        }


        String sendToDirPath, sendToThisPath;
        //System.out.print("Set target directory path: ");
        sendToDirPath = "C:/Users/Królik/IdeaProjects/NiceFTP-Server/FTPServer" + "/"; //reader.readLine()
        System.out.print("Set filename: ");
        sendToThisPath = sendToDirPath + reader.readLine() + fileExtension;
        clientCommunicationDataOutput.writeUTF(sendToThisPath);


        Socket clientTransferSocket = new Socket("127.0.0.1",1200);

        InputStream fin = new FileInputStream(file);
        fileInput = new BufferedInputStream(fin);
        fileOutput = new BufferedOutputStream(clientTransferSocket.getOutputStream());

        clientCommunicationDataInput.readUTF();
        System.out.println("Sending File ...");

        byte[] buffer = new byte[2048];
        int bytesRead;

        while((bytesRead = fileInput.read(buffer)) != -1){
            fileOutput.write(buffer,0,bytesRead);
        }

        fileOutput.flush();
        fileOutput.close();
        fileInput.close();
        fin.close();
        clientCommunicationDataInput.readUTF();

        clientCommunicationDataInput.readUTF();

        System.out.println("\n***********************************************\n");
        System.out.println("File Was Sent Successfully");
        System.out.println("\n***********************************************\n");
    }


    void receiveFile() throws Exception
    {
        listServerDir();
        String filePath, fileExtension;
        System.out.print("Enter Server File Path : ");
        filePath=reader.readLine();

        Pattern trimPattern = Pattern.compile("(\\.[a-z0-9]*)");
        Matcher matcher = trimPattern.matcher(filePath);
        if(matcher.find()){
            fileExtension = matcher.group(1);
        }
        else {
            System.out.println("Wrong path");
            fileExtension = "";
        }


        clientCommunicationDataOutput.writeUTF(filePath);
        String msgFromServer= clientCommunicationDataInput.readUTF();

        if(msgFromServer.compareTo(" File Not Found")==0)
        {
            clientCommunicationDataInput.readUTF();
            System.out.println("File not found on the Server ...");
            return;
        }
        else if(msgFromServer.compareTo(" 150 OK")==0)
        {

            String sendToDirPath, saveToThisPath;
            System.out.print("Set save directory path: ");
            sendToDirPath = reader.readLine() + "\\";
            System.out.print("Set filename: ");
            saveToThisPath = sendToDirPath + reader.readLine() + fileExtension;


            File f=new File(saveToThisPath);
            if(f.exists())
            {
                String Option;
                System.out.println("File Already Exists. Want to Save it Anyway (Y/N) ?");
                Option=reader.readLine();
                if(Option=="N")
                {
                    clientCommunicationDataOutput.writeUTF("552 Requested file action aborted");
                    return;
                }
            }
            clientCommunicationDataOutput.writeUTF("150 OK");

            Socket clientTransferSocket = new Socket("127.0.0.1",1200); //połączenie na porcie 1200

            fileInput = new BufferedInputStream(clientTransferSocket.getInputStream());
            FileOutputStream fout=new FileOutputStream(saveToThisPath);
            fileOutput = new BufferedOutputStream(fout);

            System.out.println("Receiving File ...");

            int i;

            clientCommunicationDataInput.readUTF();

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
            clientCommunicationDataInput.readUTF();
        }
    }

    public void deleteFile(){
        try {
            String filePath;
            System.out.print("Pass deletion file path : ");
            filePath = reader.readLine();
            clientCommunicationDataOutput.writeUTF(filePath);
            String msg = clientCommunicationDataInput.readUTF();
            if(msg.compareTo(" 250 Requested file action okay, completed") == 0){
                System.out.println("\n***********************************************\n");
                System.out.println("File Was Deleted Successfully");
                System.out.println("\n***********************************************\n");
            }
            else if(msg.compareTo(" 550 Requested action not taken; file not found") == 0){
                String exceptionText = clientCommunicationDataInput.readUTF();
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

    }
    void listDir() throws Exception {
        System.out.print("Pass directory path: ");
        String dirPath = reader.readLine();

        File dir = new File(dirPath);
        File[] files = dir.listFiles();


        if(files.length == 0){
            System.out.println("Empty directory");
        }
        else{
            System.out.println("********************************************************************");
            for(File temp : files){
                System.out.println(temp.getName() + " " + temp.length() + " " + temp.getAbsolutePath());
            }
            System.out.println("********************************************************************");
        }
    }

    void listServerDir() throws Exception {

        ObjectInputStream in = new ObjectInputStream(clientCommunicationDataInput);
        File[] files = (File[])in.readObject();

        if(files.length == 0){
            System.out.println("Empty directory");
        }
        else{
            for(File temp : files){
                System.out.println(temp.getName() + " " + temp.length() + " " + temp.getAbsolutePath());
            }
        }



    }



    void menu() throws Exception {
        String msg = clientCommunicationDataInput.readUTF();
        logToFTP();
        while(true)
        {

            System.out.println("[ MENU ]");
            System.out.println("1. APPE (APPEND Data)");
            System.out.println("2. RETR (RETRIVE Data)");
            System.out.println("3. DELE (DELETE)");
            System.out.println("4. QUIT (DISCONNECT)");
            System.out.print("\nEnter Choice : ");
            int choice;
            choice=Integer.parseInt(reader.readLine());
            if(choice==1)
            {

                clientCommunicationDataOutput.writeUTF("APPE");
                sendFile();

            }
            else if(choice==2)
            {
                clientCommunicationDataOutput.writeUTF("RETR");
                receiveFile();

            }
            else if(choice==3){
                clientCommunicationDataOutput.writeUTF("DELE");
                deleteFile();
            }
            else
            {
                clientCommunicationDataOutput.writeUTF("QUIT");
                System.exit(1);
            }
        }
    }
}