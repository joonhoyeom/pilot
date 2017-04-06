import message.*;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Scanner;

public class Main {


    public static void main(String[] args) {
        Communicator communicator = new Communicator();

        communicator.communicatorRun();

        System.out.println("If you want to shut down, press \'y\'");
        Scanner s = new Scanner(System.in);
        String str;
//    	do {
//    		str = s.next();
//    	} while ( "y".equals(str) == false );

        //=======TestCode=======
        while (true) {
            str = s.next();
            if ("y".equals(str) == true)
                break;

            int command = Integer.parseInt(str);

            Client client = null;
            synchronized (communicator.clientListMutex) {
                if (communicator.clientList.isEmpty() == false)
                    client = communicator.clientList.get(0);
            }

            if (client != null) {
                switch (command) {
                    case Command.COPY: {
                        System.out.print("targ dir : ");
                        String targ = s.next();
                        System.out.print("new name : ");
                        String newName = s.next();
                        CopyMessageBody body = new CopyMessageBody(targ, newName);
                        client.pushMessage(command, body.serialize());
                        break;
                    }

                    case Command.DELETE: {
                        System.out.print("which dir? : ");
                        String targ = s.next();
                        DeleteMessageBody body = new DeleteMessageBody(targ);
                        client.pushMessage(command, body.serialize());
                        break;
                    }
                    case Command.FILEUP: {
                        System.out.print("file path: ");
                        String filePath = s.next();
                        Path path = Paths.get(filePath);
                        try {
                            InputStream fin = Files.newInputStream(path, StandardOpenOption.READ);
                            byte[] buffer = new byte[0x2000];
                            int readCount = 0;
                            while ((readCount = fin.read(buffer)) != -1) {
                                if (readCount > 0) {
                                    byte[] data = new byte[readCount];
                                    System.arraycopy(buffer, 0, data, 0, readCount);
                                    FileUpMessageBody fileUpMessageBody = new FileUpMessageBody(filePath, data);
                                    while (client.pushMessage(Command.FILEUP, fileUpMessageBody.serialize()) == false) {
                                        try {
                                            Thread.sleep(100);
                                        } catch (InterruptedException e) {
                                            return;
                                        }
                                    }
                                }
                            }
                            //EOF message
                            FileUpMessageBody fileUpMessageBody = new FileUpMessageBody(filePath, null);
                            client.pushMessage(Command.FILEUP, fileUpMessageBody.serialize());
                        } catch (IOException e) {
                            e.printStackTrace();
                            break;
                        }

                        break;
                    }
                    case Command.FILEDOWN:
                        break;

                    case Command.RENAME: {
                        System.out.print("targ dir : ");
                        String targ = s.next();
                        System.out.print("new name : ");
                        String newName = s.next();
                        RenameMessageBody body = new RenameMessageBody(targ, newName);
                        client.pushMessage(command, body.serialize());
                        break;
                    }
                    case Command.DIR: {
                        System.out.print("which dir? : ");
                        String path = s.next();
                        DirMessageBody body = new DirMessageBody(path);
                        client.pushMessage(command, body.serialize());
                        break;
                    }
                    default:
                        System.out.println("Invalid Command");
                }
            }
        }
        //======================


        communicator.setStop();
        try {
            Thread.sleep(300);
        } catch (InterruptedException e1) {
        }

        if (communicator.isAlive()) {
            System.out.println("Communicator threads are still running, it will be interrupted");
            communicator.interrupt();
        }
        try {
            communicator.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
