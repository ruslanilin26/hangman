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
    private static int difficult;
    public GameHandler(Socket socket) throws IOException {
        this.socket = socket;
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new PrintWriter(socket.getOutputStream(), true);
        difficult = 2;
        word = chooseWord(difficult);
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
                    out.println("Поздравляем! Вы угадали слово! Это - " + word);
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
                    else if(line.contains("Легкая")){
                        restartGame();
                        difficult = 1;
                    }
                    else if(line.contains("Средняя")){
                        restartGame();
                        difficult = 2;
                    }
                    else if(line.contains("Трудная")){
                        restartGame();
                        difficult = 3;
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
                            sendGuesses();
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
                        sendGuesses();
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
        word = chooseWord(difficult);
        health = 7;
        wordInProgress = new char[word.length()];
        Arrays.fill(wordInProgress, '*');
        guesses.clear();
    }
    private boolean nicknameExistsInFile(String nickname) {
        try (BufferedReader reader = new BufferedReader(new FileReader("nicknames.txt"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(" ");
                if (parts.length == 2) {
                    String name = parts[0];
                    int score = Integer.parseInt(parts[1].replace(".", ""));
                    if (name.equals(nickname)){
                        userScore = score;
                        return true;
                    }
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
                String[] parts = line.split(" ");
                if (parts.length == 2) {
                    String name = parts[0];
                    int score = Integer.parseInt(parts[1].replace(".", ""));
                    if (name.equals(nickname)){
                        int newNumber = 0;
                        if(game_result) {
                            if(difficult == 1){
                                newNumber = score + 5;
                            }
                            if(difficult == 2){
                                newNumber = score + 10;
                            }
                            if(difficult == 3){
                                newNumber = score + 15;
                            }
                            userScore = newNumber;
                        }
                        else{
                            if(difficult == 1){
                                newNumber = score - 5;
                            }
                            if(difficult == 2){
                                newNumber = score - 10;
                            }
                            if(difficult == 3){
                                newNumber = score - 15;
                            }
                            if (newNumber < 0){
                                newNumber = 0;
                            }
                            userScore = newNumber;
                        }
                        line = line.replace(String.valueOf(score), String.valueOf(newNumber));
                    }
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
        return Math.round(((double) ifcounter /counter)*100);
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

    private void sendGuesses(){
        String responde = "Эти буквы вы уже пробовали: " + guesses.toString();
        out.println(responde);
    }

    private String chooseWord(int difficult) {
        String[] wordsEasy = {"кот", "дом", "лес", "море", "нос", "рука", "окно", "луна", "цвет", "глаз"};
        String[] wordsMedium = {"зеркало", "птица", "банан", "гитара", "книга", "камень", "берег", "слон", "звезда", "автомобиль"};
        String[] wordsHard = {"аппаратура", "экзамен", "гипотеза", "резонанс", "электроника", "трансформатор", "конгломерат", "катастрофа", "коллапс", "перспектива"};
        int randomWordIndex = 0;
        if(difficult == 1){
            randomWordIndex = new Random().nextInt(wordsEasy.length);
            return wordsEasy[randomWordIndex];
        }
        if(difficult == 2){
            randomWordIndex = new Random().nextInt(wordsMedium.length);
            return wordsMedium[randomWordIndex];
        }
        if(difficult == 3){
            randomWordIndex = new Random().nextInt(wordsHard.length);
            return wordsHard[randomWordIndex];
        }
        else return null;
    }
}