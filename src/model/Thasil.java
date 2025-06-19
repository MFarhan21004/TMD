public class Thasil {
    // Nama pengguna yang menyimpan skor
    private String username;

    // Nilai skor yang didapat pemain
    private int skor;

    // Jumlah bola yang berhasil dikumpulkan pemain
    private int count;

    // Konstruktor untuk menginisialisasi objek Thasil
    public Thasil(String username, int skor, int count) {
        this.username = username;
        this.skor = skor;
        this.count = count;
    }

    // Getter untuk mengambil nilai username
    public String getUsername() {
        return username;
    }

    // Getter untuk mengambil nilai skor
    public int getSkor() {
        return skor;
    }

    // Getter untuk mengambil jumlah bola yang dikumpulkan
    public int getCount() {
        return count;
    }
}
