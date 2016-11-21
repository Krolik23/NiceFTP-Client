import java.io.*;
import java.net.Socket;


public class FileTransferProtocol {

    Socket clientCommunicationSocket;
    DataInputStream clientCommunicationDataInput;
    DataOutputStream clientCommunicationDataOutput;
    BufferedReader reader;

    DataOutputStream clientTransferDataOutput;


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

        String filename;
        System.out.print("Enter File Name :");
        filename=reader.readLine();

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
            System.out.println("File Already Exists. Want to OverWrite (Y/N) ?");
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
        Socket clientTransferSocket = new Socket("127.0.0.1",4888);
        clientTransferDataOutput = new DataOutputStream(clientTransferSocket.getOutputStream());

        System.out.println("Sending File ...");

        InputStream fin = new FileInputStream(file);

        //FileInputStream fin=new FileInputStream(file);
        int ch;
        do
        {
            ch = fin.read();
            clientTransferDataOutput.writeUTF(String.valueOf(ch));
        }
        while(ch!=-1);
        fin.close();
        System.out.println(clientCommunicationDataInput.readUTF());
        clientTransferSocket.close();

    }


    void ReceiveFile() throws Exception
    {
        String fileName;
        System.out.print("Enter File Name :");
        fileName=reader.readLine();
        clientCommunicationDataOutput.writeUTF(fileName);
        String msgFromServer= clientCommunicationDataInput.readUTF();

        if(msgFromServer.compareTo("File Not Found")==0)
        {
            System.out.println("File not found on Server ...");
            return;
        }
        else if(msgFromServer.compareTo("READY")==0)
        {
            System.out.println("Receiving File ...");
            File f=new File(fileName);
            if(f.exists())
            {
                String Option;
                System.out.println("File Already Exists. Want to OverWrite (Y/N) ?");
                Option=reader.readLine();
                if(Option=="N")
                {
                    clientCommunicationDataOutput.flush();
                    return;
                }
            }
            FileOutputStream fout=new FileOutputStream(f);
            int ch;
            String temp;
            do
            {
                temp= clientCommunicationDataInput.readUTF();
                ch=Integer.parseInt(temp);
                if(ch!=-1)
                {
                    fout.write(ch);
                }
            }while(ch!=-1);
            fout.close();
            System.out.println(clientCommunicationDataInput.readUTF());

        }


    }



    void Menu() throws Exception {

        while(true)
        {
            System.out.println("[ MENU ]");
            System.out.println("1. Send File");
            System.out.println("2. Receive File");
            System.out.println("3. Exit");
            System.out.print("\nEnter Choice :");
            int choice;
            choice=Integer.parseInt(reader.readLine());
            if(choice==1)
            {
                clientCommunicationDataOutput.writeUTF("SEND");
                SendFile();

            }
            else if(choice==2)
            {
                clientCommunicationDataOutput.writeUTF("RECEIVE");
                ReceiveFile();

            }
            else
            {
                clientCommunicationDataOutput.writeUTF("DISCONNECT");
                System.exit(1);
            }
        }
    }



}
