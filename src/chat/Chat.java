package chat;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public class Chat {
    
    public static String nick = "Guest";
    public static int PortWait = 8031; //порт отправки IP адресов
    public static int PortTalk = 8030; //порт для передачи сообщений
    public static int TimeToWaitAnswer = 100; //таймаут для поиска собеседников
    public static  ArrayList<String> FindedIp = new ArrayList<String>();
    
    public static void main(String[] args) throws InterruptedException 
    {        
        setNick();
        JFrame fr=new JFrame("Чат"); 
        fr.setPreferredSize( new Dimension(425,300));//по фпкту 300х300 
        fr.setLocation(600,300);
        fr.setVisible(true); 
        fr.setResizable(false);
        fr.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); 
        fr.setLayout(null); 
        JTextField jText = new JTextField(); 
        JButton btn = new JButton("ОТПРАВИТЬ СООБЩЕНИЕ"); 
        JTextArea outText = new JTextArea(); 
        JScrollPane scroll = new JScrollPane(outText);

        fr.add(jText); 
        fr.add(btn); 
        fr.add(scroll); 
        
        scroll.setBounds(10,122,400,140);
        outText.setEditable(false); 
        outText.setLineWrap(true);
        
        jText.setBounds(10, 11, 400, 30); 
        btn.setBounds(10,62,400,50);

        btn.addActionListener(new ActionListener() { 
            // @Owerride 
            public void actionPerformed(ActionEvent event) { 
                if(!FindedIp.isEmpty())
                {
                    if(!jText.getText().isEmpty())
                    {
			outText.setText(outText.getText() + nick + ": " +jText.getText() + "\n");
                        send(nick + ": " +jText.getText()); 
                    }
                    jText.setText(""); 
                }
                else
                {
                    outText.setText(outText.getText() + "Нет получателя"  + "\n");
                }
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
        ToTryIp = ToTryIp + partIp[2] + ".";
        
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
                    if(false==true) break;
                    try { // посылка известных IP адресов 
                        s = server.accept();
                        if(s.isConnected()){
                            synchronized(FindedIp){
                                String ConnectedIp = (((InetSocketAddress) s.getRemoteSocketAddress()).getAddress()).toString().replace("/","");
                                System.out.println(ConnectedIp);
                                PrintStream ps = new PrintStream(s.getOutputStream());
                                for(String ip:FindedIp)
                                {
                                    ps.println(ip);
                                    //ps.flush();
                                }
                                ps.println("END");
                                ps.flush();
//                                for(String ip:FindedIp)
//                                {
//                                    SendListIp(ConnectedIp);
//                                }
                                FindedIp.add(ConnectedIp);
                            }
                        }
                        s.close(); // разрыв соединения
                    } catch (IOException e) {
                        System. out.println( " ошибка ожидания : " + e);
                    }
                }
                try {
                    server.close();
                } catch (IOException ex) {
                    Logger.getLogger(Chat.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
        myThready.start();	//Запуск потока
        
        System.out.println( "Перешел в режим поиска соучастников");
        
        outText.setText("Finding..." + "\n");
        
        //ищем первого собеседника и качаем с него известные ему IP адреса        
        String FindHost = FindIp(ToTryIp, MyIp, 3);
        if(FindHost!=null)
        {   
            outText.setText("Connecting..." + "\n");
            GetListIp(FindHost);
            outText.setText("Connected" + "\n");
        }
        else
        {
            outText.setText("Don't Found. Wait interlocutors" + "\n");
        }
        
        System.out.println( "Найдено Ip: " + FindedIp.size());
        for(String ip : FindedIp)
        {
            System.out.println(ip);
        }
        
        //192.168.43.171
        //172.31.3.9
        //переходим в режим общения
        Socket socetToTryConnect = null;
        System.out.println( "Перешел в режим приема/отправки сообщений");
        while(true)
        {
            Thread.sleep(1000);
            if(!FindedIp.isEmpty()){
                synchronized(FindedIp){
                    FindedIp.remove(MyIp);
                    for(String ip: FindedIp)
                    {
                        try{
                            socetToTryConnect = new Socket();
                            socetToTryConnect.connect(new InetSocketAddress(ip, PortTalk), 10000);
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
                                            if(msg.equals("CONNECTED"))
                                            {
                                                String NewIP = dis.readLine();
                                                FindedIp.add(NewIP);
                                                outText.setText(outText.getText() + "\n" + "Connected:");
                                            }
                                            else{
                                                outText.setText(outText.getText() + "\n" + msg);
                                            }
                                        }
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
            else{
                System.out.println("Список Ip адресов пустой");
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
    //передавать в формате ххх.ххх. или ххх.ххх.ххх.
    public static String FindIp(String IPstart, String LocalIp, int CountIPByte)
    {
        switch(CountIPByte)
        {
            case 2:
                for(int i = 0; i< 256; i++)
                {
                    String host = IPstart + i;
                    try {
                        if(CheckIP(host, LocalIp)!=null)
                        {
                            return host;
                        }
                    } catch (IOException e) {
                        System. out.println( "ошибка проверки (FindIp)2: " + e);
                    }
                }
                break;
            case 3:
                    try {
                        String host = CheckIP(IPstart, LocalIp);
                        if(host!=null)
                        {
                            return host;
                        }
                    } catch (IOException e) {
                        System. out.println( "ошибка проверки (FindIp)3: " + e);
                    }
                break;
            default:
                return null;
        }
        return null;
    }
    
    //передавать в формате ххх.ххх.ххх.
    public static String CheckIP(String IP, String LocalIp) throws IOException
    {
        int timeout = 100;
        boolean find = false;
        Socket socetToTryConnect = null;
        for (int i = 2; i < 256; i++){
            String host = IP + i;
            if(!host.equals(LocalIp))
            {
                try {
                    socetToTryConnect = new Socket();
                    System.out.println("Check :" + host);
                    socetToTryConnect.connect(new InetSocketAddress(host, PortWait), timeout);
                    if(socetToTryConnect.isConnected())
                    {                    
                        return host;
                    }
                    socetToTryConnect.close();
                } catch (IOException e) {
                    System.out.println( "ошибка подключения к хосту IP адресов: " + e);
                }
            }
        }
        return null;
    }
    
    public static void GetListIp(String host)
    {
        Socket Host = null;
        try {// получение списка уже известных ip адресов
            Host = new Socket(host, PortWait);
            if(Host.isConnected()){
                FindedIp.add(host);
                BufferedReader dis = new BufferedReader(new InputStreamReader(
                Host.getInputStream()));
                String Ip = "";
                while(true)
                {
                    Ip = dis.readLine();     
                    if(Ip.equals("END"))
                    {
                        break;
                    }
                    FindedIp.add(Ip);
                }
            }
        } catch (IOException e) {
            System.out.println( "ошибка приема списка IP: " + e);
        }
        try {
            Host.close();
        } catch (IOException e) {
            System.out.println( "ошибка закрытия сокета: " + e);
        }
    }
    
    public static void SendListIp(String ip)
    {
        Socket s = null;
        try { // посылка строки клиенту
            ServerSocket server = new ServerSocket(PortTalk);
            s = server.accept();
            PrintStream ps = new PrintStream(s.getOutputStream());
            ps.println( "CONNECTED" );
            ps.println( ip );
            ps.flush();
            s.close(); // разрыв соединения
            server.close();
        } catch (IOException e) {
            System. out.println( " ошибка отправки ip: " + e);
        }
    }
    
    public static boolean setNickNotEmpty = false;
    
    public static void setNick()
    {
        JFrame f=new JFrame("Никнейм"); 
        f.setPreferredSize( new Dimension(325,150));//по фпкту 300х300 
        f.setLocation(600,300);
        f.setVisible(true);
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.setResizable(false);
        f.setLayout(null); 
        JTextField Text = new JTextField(""); 
        JButton bt = new JButton("ОK"); 
        JLabel label = new JLabel("ВВЕДИТЕ НИК:");

        f.add(label);
        f.add(Text); 
        f.add(bt); 
        label.setBounds(10, 11, 100, 30);
        Text.setBounds(100, 11, 210, 30); 
        bt.setBounds(10,62,300,50); 
        
        
        bt.addActionListener(new ActionListener() 
        { 
            @Override
            public void actionPerformed(ActionEvent event) 
            { 
                if(!Text.getText().isEmpty()) 
                {
                    nick = Text.getText();
                    f.dispose();
                    setNickNotEmpty = true;
                }
            } 
        }); 
        f.pack();
        while(!setNickNotEmpty)
        {
            try {
                Thread.sleep(1000); // без сна отказывается работать
            } catch (InterruptedException e) {
                System.out.println( "ошибка сна потока в выборе никнейма: " + e);
            }
        }
    }
    
}
