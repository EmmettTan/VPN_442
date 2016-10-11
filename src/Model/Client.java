package Model;

import Helper.Common;
import Helper.Status;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

/**
 * Created by karui on 2016-10-03.
 */
public class Client implements Runnable {

    Socket clientSocket;

    protected Client() {
        System.out.println("Hi im a client");
    }

    @Override
    public void run() {
        try {
            byte[] nonce = Common.setupIdentity(clientSocket);
            //DataOutputStream writer = Vpn.getVpnManager().getWriter();
            //writer.write(nonce);

            // TODO: add code to receive nonce and do diffie hell thing; if everything okay, set status to both connected
            // for now we set the status to both connected directly and assume okay
            Vpn.getVpnManager().setStatus(Status.BothConnected);
            new Thread(new MessageReceiver()).start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean setSocket(String ip, int port) {
        try {
            clientSocket = new Socket(ip, port);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
}
