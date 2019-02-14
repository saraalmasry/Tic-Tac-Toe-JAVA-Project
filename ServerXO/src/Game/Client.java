/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Game;

import Network.Message;
import java.io.*;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author LapTop MarKet
 */
class Client extends Thread {

    Socket socket;
    ObjectInputStream input;
    ObjectOutputStream output;

    public Client(Socket socket, ObjectInputStream input, ObjectOutputStream output) {
        this.socket = socket;
        this.input = input;
        this.output = output;
    }

    public void run() {
        int isLogin = -1;
        while (isLogin == -1) {
            try {
                
                Message msg = (Message) input.readObject();
                if (msg == null) {
                    System.out.println("client is offline");
                    this.input.close();
                    this.output.close();
                    this.socket.close();
                    return;
                }
                System.out.println(msg.getType());
                System.out.println(msg.getData()[0] + " " + msg.getData()[1]);
                if ("Login".equals(msg.getType())) {
                    isLogin = GameController.dbManger.login(msg.getData()[0], msg.getData()[1]);
                }
                System.out.println("Login Result:" + isLogin);
                if (isLogin != -1) {
                    makePlayerOnline(this, isLogin);
                } else {
                    output.writeObject(new Message("Login", new String[]{"Wrong"}));
                }
                System.out.println(msg.getType());
            } catch (IOException ex) {
                 System.out.println("client is offline");
                try {
                    this.input.close();
                    this.output.close();
                    this.socket.close();
                } catch (IOException ex1) {
                    Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex1);
                }
                 return;
            } catch (ClassNotFoundException ex) {
                Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
            }

        }
    }

    public void makePlayerOnline(Client client, int id){
        int playersLength = GameController.players.size();
        try {
            // send accept message
            System.out.println("Sending Accept Message");
            Message msg = new Message("Login", new String[]{"Accept",Integer.toString(id)});
            output.writeObject(msg);
        } catch (IOException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }
        for (int i = 0; i < playersLength; i++) {
            Player p = GameController.players.get(i);
            if (p.idnum == id) {
                p.socket = client.socket;
                p.output = client.output;
                p.input = client.input;
                
                //sara to make player online
                p.isOnline= true;
                // end     
                p.startThread();
                try {
                    p.output.writeObject(new Message("Hello",new String[]{}));
                } catch (IOException ex) {
                    Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }

    }

}
