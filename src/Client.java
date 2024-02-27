import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;

public class Client {
    private JFrame frame;
    private JPanel topPanel;
    private JTextField inputField;
    private JTextArea statusArea;
    private JButton restart;
    private JButton quite;
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;

    public Client(String serverAddress) throws IOException {
        socket = new Socket(serverAddress, 6666);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new PrintWriter(socket.getOutputStream(), true);

        topPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        restart = new JButton("Рестарт");
        restart.setSize(new Dimension(100, 40));
        quite = new JButton("Выход");
        quite.setSize(new Dimension(100, 40));
        topPanel.add(restart);
        topPanel.add(quite);

        inputField = new JTextField(5);
        inputField.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                out.println(inputField.getText());
                inputField.setText("");
            }
        });

        statusArea = new JTextArea(20, 25);
        statusArea.setEditable(false);

        frame = new JFrame("Hangman");
        frame.add(topPanel, BorderLayout.NORTH);
        frame.getContentPane().add(inputField, BorderLayout.SOUTH);
        frame.getContentPane().add(new JScrollPane(statusArea), BorderLayout.CENTER);
        frame.pack();

        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                out.println("0");
                System.exit(0);
            }
        });

        restart.addActionListener(e -> {
            statusArea.setText("");
            out.println("1");
            inputField.setEditable(true);
        });

        quite.addActionListener(e -> {
            out.println("0");
            System.exit(0);
        });
    }
    public void play() throws IOException {
        String response;
        while ((response = in.readLine()) != null) {
            statusArea.append(response + "\n");
            statusArea.setCaretPosition(statusArea.getDocument().getLength());

            if(response.contains("Вы проиграли...")){
                inputField.setEditable(false);
            }
            if(response.contains("Поздравляем! Вы угадали слово!")){
                inputField.setEditable(false);
            }
        }
    }
    public static void main(String[] args) throws Exception {
        Client client = new Client("127.0.0.1");
        client.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        client.frame.setVisible(true);
        client.play();
    }
}