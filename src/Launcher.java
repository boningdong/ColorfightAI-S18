import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class Launcher {

   public static void main(String[] args) throws IOException {
       String urlStr = "http://www.google.com";
       URL url = new URL(urlStr);
       HttpURLConnection con = (HttpURLConnection) url.openConnection();
       con.setRequestMethod("GET");
       BufferedReader reader = new BufferedReader(new InputStreamReader(con.getInputStream()));
       StringBuilder sb = new StringBuilder();
       String inputLine;
       while((inputLine = reader.readLine()) != null) {
           sb.append(inputLine);
       }
       System.out.print(sb.toString());
   }
}
