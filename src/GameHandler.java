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
    private static String nickname;
    private static int userScore;
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
                    changeScore(true, nickname);
                    out.println("Поздравляем! Вы угадали слово!");
                    out.println("Ваш счет - " + userScore);
                    out.println("Это лучше, чем у " + printScore(nickname) + "% игроков");
                }
                String line = in.readLine();
                if (line == null) continue;

                if(line.contains("*")){
                    if(line.contains("Рейтинг")){
                        restartGame();
                        printRating();
                    }
                    else{
                        nickname = line.substring(1);
                        writeNicknameToFile(nickname);
                    }

                }
                else if(line.length() != 1){
                    continue;
                }
                else{
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
                                changeScore(false, nickname);
                                out.println("Вы проиграли... Правильное слово - "  + word);
                                out.println("Ваш счет - " + userScore);
                                out.println("Это лучше, чем у " + printScore(nickname) + "% игроков");
                            }
                            else{
                                out.println("Ошибка! Осталось жизней: " + health);
                                out.println(new String(wordInProgress));
                            }
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
    private boolean nicknameExistsInFile(String nickname) {
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
    private void writeNicknameToFile(String nickname) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("nicknames.txt", true))) {
            if (!nicknameExistsInFile(nickname)) {
                userScore = 0;
                writer.write(nickname + " 0.\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static void changeScore(boolean game_result, String nickname) {
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
    double printScore(String nickname){
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

    private void printRating(){
        String responde = "";
        try {
            List<String> topUsers = ScoreExtractor.extractTopUsers("nicknames.txt", nickname);
            for(int i = 0; i <= topUsers.size()-2; i++){
                responde += i+1 + ". " + topUsers.get(i) + "\n";
            }
            responde += topUsers.get(topUsers.size()-1);
            out.println(responde);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String chooseWord() {
        String[] words = {"виселица", "игра", "победа"};
        int randomWordIndex = new Random().nextInt(words.length);
        return words[randomWordIndex];
    }
}