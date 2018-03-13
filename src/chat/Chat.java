package chat;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Arrays;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public class Chat {
    static int W=300,H=200;
    
    public static int PortWait = 8031;
    public static int PortTalk = 8030;
    public static int TimeToWaitAnswer = 100;
    
    public static void main(String[] args) 
    {        
        JFrame fr=new JFrame("Чат"); 
        fr.setPreferredSize( new Dimension(440,300));//по фпкту 300х300 
        fr.setVisible(true); 
        fr.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); 
        fr.setLayout(null); 
        JTextField jText = new JTextField(); 
        JButton btn = new JButton("Отправить сообщение"); 
        JTextArea outText = new JTextArea("text"); 
        JScrollPane scroll = new JScrollPane(outText);

        fr.add(jText); 
        fr.add(btn); 
        fr.add(scroll); 

        scroll.setBounds(10,122,400,100);
        outText.setEditable(false); 
        outText.setLineWrap(true);

        jText.setBounds(10, 11, 400, 30); 
        btn.setBounds(10,62,400,50);

        btn.addActionListener(new ActionListener() { 
            // @Owerride 
            public void actionPerformed(ActionEvent event) { 
                send(jText.getText()); 
                outText.setText(outText.getText() + "\n" + jText.getText());
                jText.setText(""); 
            } 
        }); 

        fr.pack();
        
        
        InetAddress myIP = null;
        try {
            myIP = InetAddress.getLocalHost();
        } catch (UnknownHostException e) {
            System. out.println( " ошибка доступа ->" + e);
        }
        String MyIp = "";
        MyIp = myIP.getHostAddress();
        
        String partIp[] =  MyIp.split("\\.");
        
        System. out.println( " Мой IP ->" + myIP.getHostAddress());
        String ToTryIp = partIp[0] + ".";
        ToTryIp = ToTryIp + partIp[1]+ ".";
        //ToTryIp = ToTryIp + partIp[2] + ".";
        
        ArrayList<String> FindedIp = new ArrayList<String>();
        FindedIp.add(MyIp);
        
        //поток "сервер" который радает список известных IP подключившимся
        Thread myThready = new Thread(new Runnable(){
            @Override
            public void run() //Этот метод будет говорить остальным, что это чат
            {
                Socket s = null;
                ServerSocket server = null;
                try{
                    server = new ServerSocket(PortWait);
                } catch(IOException e)
                {
                    System. out.println( " ошибка получения порта: " + e);
                }
                while(true)
                {
                    try { // посылка строки клиенту
                        s = server.accept();
                        if(s.isConnected()){
                            System.out.println(Arrays.toString(s.getInetAddress().getAddress()));
                            FindedIp.add(Arrays.toString(s.getInetAddress().getAddress()));
                            PrintStream ps = new PrintStream(s.getOutputStream());
                            for(String ip:FindedIp)
                            {
                                ps.println(ip);
                                ps.flush();
                            }
                            ps.println("END");
                            ps.flush();
                        }
                        s.close(); // разрыв соединения
                        server.close();
                    } catch (IOException e) {
                        System. out.println( " ошибка ожидания : " + e);
                    }
                }
            }
        });
        myThready.start();	//Запуск потока
        
        System.out.println( "Перешел в режим поиска соучастников");
        //ищем первого собеседника
        Socket socetToTryConnect = null;
        boolean find = false;
        for(int i = 0; i < Integer.parseInt(partIp[2]) + 1;i++){
            if(find) break;
            for(int j = 0; j<256;j++){
                if(find) break;
                if((ToTryIp + i +"." +j).equals(MyIp)) continue;
                try {
                    socetToTryConnect = new Socket();
                    System.out.println("Try :" + ToTryIp + i +"." +j);
                    socetToTryConnect.connect(new InetSocketAddress(ToTryIp + i +"." +j, PortWait), TimeToWaitAnswer);
                    if(socetToTryConnect.isConnected())
                    {
                        Socket Host = null;
                        while(true)
                        {
                            try {// получение строки клиентом
                                Host = new Socket(ToTryIp + i +"." +j, PortWait);
                                if(Host.isConnected()){
                                    BufferedReader dis = new BufferedReader(new InputStreamReader(
                                    Host.getInputStream()));
                                    String Ip = dis.readLine();
                                    if(Ip.equals("END"))
                                    {
                                        find = true;
                                        break;
                                    }
                                    FindedIp.add(Ip);
                                }
                            } catch (IOException e) {
                                System.out.println( "ошибка приема: " + e);
                            }
                        }
                        Host.close();
                        System.out.println("Find :" + ToTryIp + i +"." +j);
                        FindedIp.add(ToTryIp + i +"." +j);
                    }
                    socetToTryConnect.close();
                } catch (IOException e) {
                    System.out.println( "ошибка подключения: " + e);
                }
            }
        }
        System.out.println( "Найдено Ip: ");
        for(String ip : FindedIp)
        {
            System.out.println(ip);
        }
        
        //192.168.43.171
        //172.31.3.9
        //переходим в режим общения
        System.out.println( "Перешел в режим ожидания");
        while(true)
        {
            for(String ip: FindedIp)
            {
                try{
                    socetToTryConnect = new Socket();
                    socetToTryConnect.connect(new InetSocketAddress(ip, PortTalk), TimeToWaitAnswer);
                    if(socetToTryConnect.isConnected())
                    {
                        Socket Host = null;
                        while(true)
                        {
                            try {// получение строки клиентом
                                Host = new Socket(ip, PortTalk);
                                if(Host.isConnected()){
                                    BufferedReader dis = new BufferedReader(new InputStreamReader(
                                    Host.getInputStream()));
                                    String msg = dis.readLine();
                                    outText.setText(outText.getText() + "\n" + msg);
                                }
                                Host.close();
                            } catch (IOException e) {
                                System.out.println( "ошибка приема: " + e);
                            }
                        }
                    }
                    socetToTryConnect.close();
                } catch (IOException e) {
                    System.out.println( "ошибка подключения: " + e);
                }
            }
        }
    }
    
    public static boolean send(String msg)
    {
        Socket s = null;
        try { // посылка строки клиенту
            ServerSocket server = new ServerSocket(PortTalk);
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
