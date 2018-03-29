import Colorfight.Cell;
import Colorfight.Game;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;
import java.util.Random;

public class ExampleAI {

    public static void main(String[] args) throws IOException {
       Game g = new Game();
       if (g.JoinGame("JavaAI")) {
           while (true) {
                for (int x = 0; x < g.width; x++)
                    for (int y = 0; y < g.height; y++) {
                        Cell cell = g.GetCell(x, y);
                        if (cell.owner == g.uid) {
                            if (g.gold > 60 && g.baseNum < 3)
                                System.out.println(g.BuildBase(x, y).toString());
                            int dx = 0, dy = 0;
                            while (dx == 0 && dy == 0) {
                                dx = (int) (Math.random() * 3) - 1;
                                dy = (int) (Math.random() * 3) - 1;
                            }
                            Cell attackCell = g.GetCell(x + dx, y + dy);
                            if (attackCell != null) {
                                if (attackCell.owner != g.uid)
                                    System.out.println(g.AttackCell(x + dx, y + dy).toString());
                                g.Refresh();
                            }
                        }
                    }
           }
       }
       else
           System.out.println("Failed to join the game");
    }
}
