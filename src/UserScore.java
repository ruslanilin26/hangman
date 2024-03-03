// Класс для хранения пары имя пользователя - число
public class UserScore implements Comparable<UserScore> {
    String name;
    int score;

    public UserScore(String name, int score) {
        this.name = name;
        this.score = score;
    }

    // Определяем критерий сравнения (для сортировки в обратном порядке)
    @Override
    public int compareTo(UserScore other) {
        return other.score - this.score;
    }

    // Возвращаем строку в требуемом формате
    @Override
    public String toString() {
        return name + " " + score;
    }
}

