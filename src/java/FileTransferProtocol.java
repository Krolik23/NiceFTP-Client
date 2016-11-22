import java.io.*;
import java.net.Socket;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class FileTransferProtocol {

    Socket clientCommunicationSocket;
    DataInputStream clientCommunicationDataInput;
    DataOutputStream clientCommunicationDataOutput;
    BufferedReader reader;

    DataOutputStream clientTransferDataOutput;
    DataInputStream clientTransferDataInput;


    public FileTransferProtocol(Socket clientCommunicationSocket){
        try {
            this.clientCommunicationSocket = clientCommunicationSocket;
            clientCommunicationDataInput = new DataInputStream(clientCommunicationSocket.getInputStream());
            clientCommunicationDataOutput = new DataOutputStream(clientCommunicationSocket.getOutputStream());
            reader = new BufferedReader(new InputStreamReader(System.in));
        }
        catch(Exception ex){}

    }

    void SendFile() throws Exception
    {

        String filename, fileExtension;
        System.out.print("Enter File Path :");
        filename=reader.readLine();

        Pattern trimPattern = Pattern.compile("(\\.[a-z]*)");
        Matcher matcher = trimPattern.matcher(filename);
        if(matcher.find()){
            fileExtension = matcher.group(1);
        }
        else {fileExtension = "";}



        File file = new File(filename);
        long length = file.length();
        if(!file.exists())
        {
            System.out.println("File not Exists...");
            clientCommunicationDataOutput.writeUTF("File not found");
            return;
        }
        clientCommunicationDataOutput.writeUTF(filename);

        String msgFromServer= clientCommunicationDataInput.readUTF();
        if(msgFromServer.compareTo("File Already Exists")==0)
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
        System.out.print("Set target directory path: ");
        sendToDirPath = reader.readLine() + "\\";
        System.out.print("Set filename: ");
        sendToThisPath = sendToDirPath + reader.readLine() + fileExtension;
        clientCommunicationDataOutput.writeUTF(sendToThisPath);

        Socket clientTransferSocket = new Socket("127.0.0.1",1200);
        clientTransferDataOutput = new DataOutputStream(clientTransferSocket.getOutputStream());
        System.out.println("Sending File ...");

        InputStream fin = new FileInputStream(file);


        int ch;
        do
        {
            ch = fin.read();
            clientTransferDataOutput.writeUTF(String.valueOf(ch));
        }
        while(ch!=-1);
        fin.close();
        System.out.println("\n***********************************************\n");
        System.out.println(clientCommunicationDataInput.readUTF());
        System.out.println("\n***********************************************\n");
        clientTransferSocket.close();
        clientTransferDataOutput.close();

    }


    void ReceiveFile() throws Exception
    {
        String filename, fileExtension;
        System.out.print("Enter Server File Path : ");
        filename=reader.readLine();

        Pattern trimPattern = Pattern.compile("(\\.[a-z]*)");
        Matcher matcher = trimPattern.matcher(filename);
        if(matcher.find()){
            fileExtension = matcher.group(1);
        }
        else {fileExtension = "";}

        clientCommunicationDataOutput.writeUTF(filename);
        String msgFromServer= clientCommunicationDataInput.readUTF();

        if(msgFromServer.compareTo("File Not Found")==0)
        {
            System.out.println("File not found on the Server ...");
            return;
        }
        else if(msgFromServer.compareTo("READY")==0)
        {
            File f=new File(filename);
            if(f.exists())
            {
                String Option;
                System.out.println("File Already Exists. Want to Save it Anyway (Y/N) ?");
                Option=reader.readLine();
                if(Option=="N")
                {
                    clientCommunicationDataOutput.flush();
                    return;
                }
            }

            String sendToDirPath, saveToThisPath;
            System.out.print("Set save directory path: ");
            sendToDirPath = reader.readLine() + "\\";
            System.out.print("Set filename: ");
            saveToThisPath = sendToDirPath + reader.readLine() + fileExtension;

            Socket clientTransferSocket = new Socket("127.0.0.1",1200); //połączenie na porcie 1200
            clientTransferDataInput = new DataInputStream(clientTransferSocket.getInputStream());
            FileOutputStream fout=new FileOutputStream(saveToThisPath);

            System.out.println("Receiving File ...");

            int ch;
            String temp;
            do
            {
                temp= clientTransferDataInput.readUTF();
                ch=Integer.parseInt(temp);
                if(ch!=-1)
                {
                    fout.write(ch);
                }
            }while(ch!=-1);
            fout.close();
            clientTransferDataInput.close();
            clientTransferSocket.close();
            System.out.println("\n***********************************************\n");
            System.out.println(clientCommunicationDataInput.readUTF());
            System.out.println("\n***********************************************\n");

        }


    }

    public void DeleteFile(){
        try {
            String filePath;
            System.out.print("Pass deletion file path : ");
            filePath = reader.readLine();
            clientCommunicationDataOutput.writeUTF(filePath);
            String msg = clientCommunicationDataInput.readUTF();
            if(msg.compareTo("DELATED") == 0){
                System.out.println("\n***********************************************\n");
                System.out.println("File Was Deleted Succesfully");
                System.out.println("\n***********************************************\n");
            }
            else{
                System.out.println(clientCommunicationDataInput.readUTF());
            }
        }
        catch(IOException exception){
            System.out.println(exception);
        }

    }



    void Menu() throws Exception {

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
                SendFile();

            }
            else if(choice==2)
            {
                clientCommunicationDataOutput.writeUTF("RETR");
                ReceiveFile();

            }
            else if(choice==3){
                clientCommunicationDataOutput.writeUTF("DELE");
                DeleteFile();
            }
            else
            {
                clientCommunicationDataOutput.writeUTF("QUIT");
                System.exit(1);
            }
        }
    }



}
