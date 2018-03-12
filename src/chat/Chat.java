package chat;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.*;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class Chat {
    static int W=300,H=200;
    public static void main(String[] args) 
    {
        JFrame fr=new JFrame("Чатик");
        fr.setPreferredSize( new Dimension(500,500));//по фпкту 300х300
        final JPanel pan= new JPanel();
        fr.add(pan);
        fr.setVisible(true);
        fr.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        JTextField jText = new JTextField();
        JButton btn = new JButton("Отправить сообщение");
        JLabel lable = new JLabel("Text");
        
        fr.add(jText);
        fr.add(btn);
        fr.add(lable);
        
        lable.setBounds(200,200,100,50);
        jText.setBounds(50, 51, 100, 50);
        btn.setBounds(1,102,200,50);
        
        btn.addActionListener(new ActionListener() {
            // @Owerride
            public void actionPerformed(ActionEvent event) {
                lable.setText(lable.getText()+" " + jText.getText());
                send(jText.getText());
                jText.setText("");
                System.out.println(lable.getText());
                lable.updateUI();
            }
        });
        
        fr.pack();
        
        while(true)
        {
            Socket socket = null;
            try {// получение строки клиентом
                socket = new Socket("192.168.0.44", 8030);
                BufferedReader dis = new BufferedReader(new InputStreamReader(
                socket.getInputStream()));
                String msg = dis.readLine();
                lable.setText(msg);
                System.out.println(msg);
                socket.close();
            } catch (IOException e) {
                System.out.println( "ошибка приема: " + e);
            }
        }
    }
    
    public static boolean send(String msg)
    {
        Socket s = null;
        try { // посылка строки клиенту
            ServerSocket server = new ServerSocket(8030);
            s = server.accept();
            PrintStream ps = new PrintStream(s.getOutputStream());
            ps.println( msg );
            ps.flush();
            s.close(); // разрыв соединения
            server.close();
            return true;
        } catch (IOException e) {
            System. out.println( " ошибка отправки: " + e);
            return false;
        }
    }
}
