import java.io.*;
import java.net.Socket;
import java.util.*;

class GameHandler extends Thread {
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private String word;
    private int health;
    private char[] wordInProgress;
    private Set<Character> guesses;

    public GameHandler(Socket socket) throws IOException {
        this.socket = socket;
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new PrintWriter(socket.getOutputStream(), true);
        word = chooseWord();
        health = 7;
        wordInProgress = new char[word.length()];
        Arrays.fill(wordInProgress, '*');
        guesses = new HashSet<>();
    }

    public void run() {
        out.println("Добро пожаловать в виселицу!");
        out.println("Попробуйте угадать слово.");
        out.println("У вас " + health + " попыток");
        out.println(new String(wordInProgress));

        try {
            while (true) {
                if(word.equals(new String(wordInProgress))){
                    out.println("Поздравляем! Вы угадали слово!");
                }

                String line = in.readLine();

                if (line == null || line.length() != 1) continue;

                if (line.contains("1")) {
                    restartGame();
                    out.println("Добро пожаловать в виселицу!");
                    out.println("Попробуйте угадать слово.");
                    out.println("У вас " + health + " попыток");
                    out.println(new String(wordInProgress));
                    continue;
                }

                char guess = line.charAt(0);

                if (guesses.contains(guess)) {
                    out.println("Вы уже вводили эту букву!");
                }
                else {
                    guesses.add(guess);
                    boolean correct = false;

                    for (int i = 0; i < word.length(); i++) {
                        if (word.charAt(i) == guess) {
                            wordInProgress[i] = guess;
                            correct = true;
                        }
                    }
                    if (correct) {
                        out.println("Верно!   \n" + new String(wordInProgress));
                    }
                    else {
                        health--;
                        if (health == 0){
                            out.println("Вы проиграли... Правильное слово - "  + word);
                        }
                        else{
                            out.println("Ошибка! Осталось жизней: " + health);
                            out.println(new String(wordInProgress));
                        }
                    }
                }
            }
        }
        catch (IOException e) {
            System.out.println("Ошибка клиента #" + getName());
        }
        finally {
            try {
                socket.close();
            }
            catch (IOException e) {
                System.out.println("Ошибка! Не удалось закрыть сокет.");
                e.printStackTrace();
            }
            System.out.println("Соединение с клиентом #" + getName() + " закрыто");
        }
    }
    private void restartGame() {
        word = chooseWord();
        health = 7;
        wordInProgress = new char[word.length()];
        Arrays.fill(wordInProgress, '*');
        guesses.clear();
    }

    private String chooseWord() {
        String[] words = {"виселица", "игра", "победа"};
        int randomWordIndex = new Random().nextInt(words.length);
        return words[randomWordIndex];
    }
}