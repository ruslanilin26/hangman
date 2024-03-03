import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
public class ScoreExtractor {
    public static List<String> extractTopUsers(String filePath, String targetUser) throws IOException {
        List<UserScore> users = new ArrayList<>();
        try(BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(" ");
                if (parts.length == 2) {
                    String name = parts[0];
                    int score = Integer.parseInt(parts[1].replace(".", ""));
                    users.add(new UserScore(name, score));
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return Collections.emptyList();
        }

        Collections.sort(users);

        List<String> topUsers = new ArrayList<>();
        int position = 0; // Переменная для хранения позиции пользователя
        int score = 0;
        boolean isUserFound = false;
        int endIndex = Math.min(users.size(), 5);

        for (int i = 0; i < users.size(); i++) {
            if (i < endIndex) {
                topUsers.add(users.get(i).toString() + " очков");
            }
            if (users.get(i).name.equals(targetUser)) {
                position = i + 1;
                score = users.get(i).score;
                isUserFound = true;
            }
        }
        // Добавляем позицию целевого пользователя в список, если он найден
        if (isUserFound) {
            topUsers.add("Вы в топе на " + position + " месте ("+ score + " очков)" );
        } else {
            topUsers.add(targetUser + " Вы не в топе...");
        }
        return topUsers;
    }
}