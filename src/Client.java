import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;
import java.net.Socket;

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
    private static String nickname;
    private static int userScore;
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
        do{
            nickname = JOptionPane.showInputDialog(frame, "Введите ваш никнейм:");
            if (nickname == null || nickname.trim().isEmpty()) {
                JOptionPane.showMessageDialog(frame, "Никнейм не может быть пустым.");
            }
        }
        while (nickname == null || nickname.trim().isEmpty());
        writeNicknameToFile();
        frame.add(topPanel, BorderLayout.NORTH);
        frame.getContentPane().add(inputField, BorderLayout.SOUTH);
        frame.getContentPane().add(new JScrollPane(statusArea), BorderLayout.CENTER);
        frame.pack();
        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });
        restart.addActionListener(e -> {
            statusArea.setText("");
            out.println("1");
            inputField.setEditable(true);
        });
        quite.addActionListener(e -> {
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
                changeScore(false);
                statusArea.append("Ваш счет - " + userScore);
                statusArea.append("\n Это лучше, чем у " + printScore() + "% игроков");
            }
            if(response.contains("Поздравляем! Вы угадали слово!")){
                inputField.setEditable(false);
                changeScore(true);
                statusArea.append("Ваш счет - " + userScore);
                statusArea.append("\n Это лучше, чем у " + printScore() + "% игроков");
            }
        }
    }
    private boolean nicknameExistsInFile() {
        try (BufferedReader reader = new BufferedReader(new FileReader("nicknames.txt"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith(nickname + " ")) {
                    int startIndex = line.indexOf(" ") + 1;
                    int endIndex = line.indexOf(".", startIndex);
                    String numberStr = line.substring(startIndex, endIndex).trim();
                    userScore = Integer.parseInt(numberStr);
                    return true;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }
    private void writeNicknameToFile() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("nicknames.txt", true))) {
            if (!nicknameExistsInFile()) {
                userScore = 0;
                writer.write(nickname + " 0.\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static void changeScore(boolean game_result) {
        try {
            BufferedReader fileReader = new BufferedReader(new FileReader("nicknames.txt"));
            StringBuilder result = new StringBuilder();
            String line;
            while ((line = fileReader.readLine()) != null) {
                if (line.contains(nickname)) {
                    int startIndex = line.indexOf(" ") + 1;
                    int endIndex = line.indexOf(".", startIndex);
                    String numberStr = line.substring(startIndex, endIndex).trim();
                    int originalNumber = Integer.parseInt(numberStr);
                    int newNumber;
                    if(game_result) {
                        newNumber = originalNumber + 5;
                        userScore = newNumber;
                    }
                    else{
                        newNumber = originalNumber - 10;
                        userScore = newNumber;
                        if (newNumber < 0){
                            newNumber = 0;
                            userScore = newNumber;
                        }
                    }
                    line = line.replace(numberStr, String.valueOf(newNumber));
                }
                result.append(line).append("\n");
            }
            fileReader.close();
            BufferedWriter fileWriter = new BufferedWriter(new FileWriter("nicknames.txt"));
            fileWriter.write(result.toString());
            fileWriter.close();
        } catch (IOException | NumberFormatException e) {
            e.printStackTrace();
        }
    }
    double printScore(){
        int counter = 0;
        int ifcounter = 0;
        try {
            BufferedReader fileReader = new BufferedReader(new FileReader("nicknames.txt"));
            String line;
            while ((line = fileReader.readLine()) != null) {
                if (!line.contains(nickname)) {
                    int startIndex = line.indexOf(" ") + 1;
                    int endIndex = line.indexOf(".", startIndex);
                    String numberStr = line.substring(startIndex, endIndex).trim();
                    int number = Integer.parseInt(numberStr);
                    if(userScore > number){
                        ifcounter++;
                    }
                    counter++;
                }
            }
            fileReader.close();
        } catch (IOException | NumberFormatException e) {
            e.printStackTrace();
        }
        return ((double) ifcounter /counter)*100;
    }
    public static void main(String[] args) throws Exception {
        Client client = new Client("127.0.0.1");
        client.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        client.frame.setVisible(true);
        client.play();
    }
}