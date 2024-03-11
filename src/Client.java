import javax.swing.*;
import java.awt.*;
import javax.swing.text.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;
import java.net.Socket;
import java.util.Objects;

public class Client {
    private JFrame frame;
    private JPanel topPanel;
    private JPanel rightPanel;
    private JLabel label0;
    private JTextField inputField;
    private JTextPane statusPane;
    private JTextPane guessesLetter;
    private JMenuBar menuBar;
    private JMenu difficultyMenu;
    private ButtonGroup buttonGroup;
    private JRadioButtonMenuItem easyItem;
    private JRadioButtonMenuItem mediumItem;
    private JRadioButtonMenuItem hardItem;
    private JButton rating;
    private JButton restart;
    private JButton quite;
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private static String nickname;
    public Client(String serverAddress) throws IOException {
        socket = new Socket(serverAddress, 6666);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new PrintWriter(socket.getOutputStream(), true);

        topPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        rightPanel = new JPanel(new BorderLayout());

        label0 = new JLabel();
        label0.setPreferredSize(new Dimension(256, 256));
        label0.setIcon(new ImageIcon("C:\\Users\\Ruslan\\Data\\University\\8semestr\\Java\\Hangman\\Hangman\\src\\image\\zero.png"));

        menuBar = new JMenuBar();
        difficultyMenu = new JMenu("Сложность");
        buttonGroup = new ButtonGroup();
        easyItem = new JRadioButtonMenuItem("Легкая");
        mediumItem = new JRadioButtonMenuItem("Средняя");
        hardItem = new JRadioButtonMenuItem("Трудная");

        buttonGroup.add(easyItem);
        buttonGroup.add(mediumItem);
        buttonGroup.add(hardItem);

        ActionListener difficultyListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String selectedDifficulty = ((JRadioButtonMenuItem) e.getSource()).getText();
                out.println("*" + selectedDifficulty);
                statusPane.setText("");
                label0.setIcon(new ImageIcon("C:\\Users\\Ruslan\\Data\\University\\8semestr\\Java\\Hangman\\Hangman\\src\\image\\zero.png"));
                guessesLetter.setText("");
                out.println("1");
                inputField.setEditable(true);
            }
        };

        easyItem.addActionListener(difficultyListener);
        mediumItem.addActionListener(difficultyListener);
        hardItem.addActionListener(difficultyListener);

        difficultyMenu.add(easyItem);
        difficultyMenu.add(mediumItem);
        difficultyMenu.add(hardItem);

        menuBar.add(difficultyMenu);

        rating = new JButton("Рейтинг");
        rating.setSize(new Dimension(100, 40));

        restart = new JButton("Рестарт");
        restart.setSize(new Dimension(100, 40));

        quite = new JButton("Выход");
        quite.setSize(new Dimension(100, 40));

        topPanel.add(menuBar);
        topPanel.add(rating);
        topPanel.add(restart);
        topPanel.add(quite);

        inputField = new JTextField(25);
        inputField.setPreferredSize(new Dimension(100, 25));
        inputField.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if(Objects.equals(inputField.getText(), "1")){
                    statusPane.setText("");
                }
                out.println(inputField.getText());
                inputField.setText("");
            }
        });

        statusPane = new JTextPane();
        statusPane.setPreferredSize(new Dimension(350, 350));
        statusPane.setEditable(false);

        guessesLetter = new JTextPane();
        guessesLetter.setPreferredSize(new Dimension(256, 90));
        guessesLetter.setEditable(false);

        rightPanel.add(label0, BorderLayout.NORTH);
        rightPanel.add(guessesLetter, BorderLayout.SOUTH);

        frame = new JFrame("Hangman");

        do{
            nickname = JOptionPane.showInputDialog(frame, "Введите ваш никнейм:");
            if (nickname == null || nickname.trim().isEmpty()) {
                JOptionPane.showMessageDialog(frame, "Никнейм не может быть пустым.");
            }
        }
        while (nickname == null || nickname.trim().isEmpty());

        out.println("*" + nickname);

        frame.add(topPanel, BorderLayout.NORTH);
        frame.add(rightPanel, BorderLayout.EAST);
        frame.getContentPane().add(inputField, BorderLayout.SOUTH);
        frame.getContentPane().add(new JScrollPane(statusPane), BorderLayout.CENTER);

        frame.pack();
        frame.setLocationRelativeTo(null);

        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });

        rating.addActionListener(e -> {
            statusPane.setText("");
            label0.setIcon(new ImageIcon("C:\\Users\\Ruslan\\Data\\University\\8semestr\\Java\\Hangman\\Hangman\\src\\image\\zero.png"));
            guessesLetter.setText("");
            appendStyledText("Топ игроков:\n", Color.red, 16);
            out.println("*Рейтинг");
            inputField.setEditable(true);
        });

        restart.addActionListener(e -> {
            statusPane.setText("");
            label0.setIcon(new ImageIcon("C:\\Users\\Ruslan\\Data\\University\\8semestr\\Java\\Hangman\\Hangman\\src\\image\\zero.png"));
            guessesLetter.setText("");
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
            if(response.contains("Добро пожаловать в виселицу!") || response.contains("Попробуйте угадать слово.") || response.contains("У вас ") || response.contains("Верно")){
                appendStyledText(response + "\n", Color.decode("#007929"), 16);
                continue;
            }
            if(response.contains("Вы проиграли...")){
                inputField.setEditable(false);
                statusPane.setText("");
                label0.setIcon(new ImageIcon("C:\\Users\\Ruslan\\Data\\University\\8semestr\\Java\\Hangman\\Hangman\\src\\image\\hangman7.png"));
                appendStyledText(response + "\n", Color.red, 14);
                continue;
            }
            if(response.contains("Поздравляем! Вы угадали слово!")){
                inputField.setEditable(false);
                statusPane.setText("");
                label0.setIcon(new ImageIcon("C:\\Users\\Ruslan\\Data\\University\\8semestr\\Java\\Hangman\\Hangman\\src\\image\\zero.png"));
                guessesLetter.setText("");
                appendStyledText(response + "\n", Color.decode("#007929"), 14);
                continue;
            }
            if(response.contains("Вы в топе на")){
                inputField.setEditable(false);
                appendStyledText(response + "\n", Color.red, 14);
                continue;
            }
            if(response.contains("Эти буквы вы уже пробовали")){
                guessesLetter.setText(response);
                continue;
            }
            if(response.contains("Ошибка! Осталось жизней: 6")){
                label0.setIcon(new ImageIcon("C:\\Users\\Ruslan\\Data\\University\\8semestr\\Java\\Hangman\\Hangman\\src\\image\\hangman1.png"));
                appendStyledText(response + "\n", Color.red, 14);
                continue;
            }
            if(response.contains("Ошибка! Осталось жизней: 5")){
                label0.setIcon(new ImageIcon("C:\\Users\\Ruslan\\Data\\University\\8semestr\\Java\\Hangman\\Hangman\\src\\image\\hangman2.png"));
                appendStyledText(response + "\n", Color.red, 14);
                continue;
            }
            if(response.contains("Ошибка! Осталось жизней: 4")){
                label0.setIcon(new ImageIcon("C:\\Users\\Ruslan\\Data\\University\\8semestr\\Java\\Hangman\\Hangman\\src\\image\\hangman3.png"));
                appendStyledText(response + "\n", Color.red, 14);
                continue;
            }
            if(response.contains("Ошибка! Осталось жизней: 3")){
                label0.setIcon(new ImageIcon("C:\\Users\\Ruslan\\Data\\University\\8semestr\\Java\\Hangman\\Hangman\\src\\image\\hangman4.png"));
                appendStyledText(response + "\n", Color.red, 14);
                continue;
            }
            if(response.contains("Ошибка! Осталось жизней: 2")){
                label0.setIcon(new ImageIcon("C:\\Users\\Ruslan\\Data\\University\\8semestr\\Java\\Hangman\\Hangman\\src\\image\\hangman5.png"));
                appendStyledText(response + "\n", Color.red, 14);
                continue;
            }
            if(response.contains("Ошибка! Осталось жизней: 1")){
                label0.setIcon(new ImageIcon("C:\\Users\\Ruslan\\Data\\University\\8semestr\\Java\\Hangman\\Hangman\\src\\image\\hangman6.png"));
                appendStyledText(response + "\n", Color.red, 14);
                continue;
            }

            appendStyledText(response + "\n", Color.BLACK, 14);
            statusPane.setCaretPosition(statusPane.getDocument().getLength());
        }
    }

    private void appendStyledText(String text, Color color, int fontSize) {
        StyledDocument styledDoc = statusPane.getStyledDocument();
        Style style = styledDoc.addStyle(null, StyleContext.getDefaultStyleContext().getStyle(StyleContext.DEFAULT_STYLE));

        StyleConstants.setForeground(style, color);
        StyleConstants.setFontSize(style, fontSize);

        try {
            styledDoc.insertString(styledDoc.getLength(), text, style);
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }
    public static void main(String[] args) throws Exception {
        Client client = new Client("127.0.0.1");
        client.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        client.frame.setVisible(true);
        client.play();
    }
}