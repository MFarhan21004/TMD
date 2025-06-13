public class Thasil {
    private String username;
    private int skor;
    private int count;

    public Thasil(String username, int skor, int count) {
        this.username = username;
        this.skor = skor;
        this.count = count;
    }

    public String getUsername() {
        return username;
    }

    public int getSkor() {
        return skor;
    }

    public int getCount() {
        return count;
    }
}