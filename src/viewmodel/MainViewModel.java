import javax.swing.JOptionPane;
import java.util.List;

public class MainViewModel {
    private MainFrame mainFrame;
    private DatabaseModel databaseModel;
    private String currentUsername;

    public MainViewModel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        this.databaseModel = new DatabaseModel();
    }

    public void loadScores() {
        List<Thasil> results = databaseModel.getAllThasil();
        mainFrame.updateScoreTable(results);
    }

    public void startGame(String username) {
        if (username == null || username.trim().isEmpty()) {
            mainFrame.showMessage("Input Error", "Username cannot be empty!", JOptionPane.WARNING_MESSAGE);
            return;
        }
        this.currentUsername = username.trim();
        
        mainFrame.switchToGamePanel();
    }

    public String getCurrentUsername() {
        return currentUsername;
    }

    public void exitApplication() {
        int confirm = JOptionPane.showConfirmDialog(mainFrame, "Are you sure you want to quit?", "Quit Game", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            System.exit(0);
        }
    }
}