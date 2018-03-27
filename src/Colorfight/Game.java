package Colorfight;

import javafx.util.Pair;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import javax.swing.plaf.synth.SynthEditorPaneUI;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Dictionary;

public class Game {
    static final String hostUrl = "http://colorfight.herokuapp.com/";

    public JSONObject data = null;
    public String token = "";
    public String name = "";
    public int uid = -1;

    // Game info
    public int width;
    public int height;
    public long currTime;
    public long endTime;
    public long joinEndTime;
    public int gameId;
    public long lastUpdate;

    public boolean JoinGame(String name) {
        if (name == null)
            return false;

        String token = ReadTokenFile();
        if (token != null) {
            try {
                JSONObject gameData = CheckToken(token);
                if (!gameData.isEmpty()) {
                    if (name.equals((String) gameData.get("name"))) {
                        this.name = (String)gameData.get("name");
                        uid = Math.toIntExact((long)gameData.get("uid"));
                        System.out.println("CheckToToken Succeed!"); // Debug
                        return true;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // Config post data
        Pair<String, String> header = new Pair<>("content-type", "application/json");
        JSONObject postData = new JSONObject();
        postData.put("name", name);

        JSONObject gameData = PostForData(hostUrl + "joingame", postData, header);
        if (!gameData.isEmpty()) {
            WriteTokenFile((String) gameData.get("token"));
            this.token = (String) gameData.get("token");
            this.uid = Math.toIntExact((long) gameData.get("uid"));
            this.data = null;
            System.out.println("CreateNewPlayer Succeed!");
            Refresh();
        }
        else
            return false;
        return true;
    }

    public boolean Refresh() {
        Pair<String, String> header = new Pair<>("content-type", "application/json");
        if (this.data == null) {
            JSONObject postData = new JSONObject();
            postData.put("protocol", 2);
            JSONObject gameData = PostForData(hostUrl + "getgameinfo", postData, header);
            if (!gameData.isEmpty()) {
                this.data = gameData;
                JSONObject info = (JSONObject) data.get("info");
                this.width = Math.toIntExact((long)info.get("width"));
                this.height = Math.toIntExact((long)info.get("height"));
                this.currTime = (long)info.get("time");
                this.endTime = (long)info.get("end_time");
                this.joinEndTime = (long)info.get("join_end_time");
                this.gameId = Math.toIntExact((long)info.get("game_id"));
                this.lastUpdate = this.currTime;
                //******************* Refresh Users
            } else {
                return false;
            }
        }
        else {
            JSONObject postData = new JSONObject();
            postData.put("protocol", 1);
            postData.put("timeAfter", this.lastUpdate);
            JSONObject gameData = PostForData(hostUrl + "getgameinfo", postData, header);
            if(!gameData.isEmpty()) {
                //
            }
        }
        return true;
    }

    private JSONObject PostForData(String host, JSONObject postData, Pair<String, String> header) {
        try {
            // Config post property
            URL url = new URL(host);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty(header.getKey(), header.getValue());

            // Start post
            con.setDoOutput(true);
            DataOutputStream output = new DataOutputStream(con.getOutputStream());
            output.writeBytes(postData.toJSONString());
            output.flush();
            output.close();

            if (con.getResponseCode() == 200) {
                // Fetch Response
                BufferedReader input = new BufferedReader(new InputStreamReader(con.getInputStream()));
                String inline;
                StringBuilder reponse = new StringBuilder();
                while ((inline = input.readLine()) != null) {
                    reponse.append(inline);
                }
                input.close();

                // Parse response
                JSONObject gameData;
                try {
                    gameData = (JSONObject) new JSONParser().parse(reponse.toString());
                    return gameData;
                } catch (ParseException e) {
                    System.out.println("Error happened when parsing game data: " + e.getPosition());
                }
            } else {
                return new JSONObject(); // Return a empty JSONObject;
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return new JSONObject(); // Return a empty JSONObject;
    }

    private JSONObject CheckToken(String token) throws IOException {
        // Config post data
        Pair<String, String> header = new Pair<>("content-type", "application/json");
        JSONObject postData = new JSONObject();
        postData.put("token", token);

        // Config post properties
        URL url = new URL(hostUrl + "checktoken");
        HttpURLConnection con = (HttpURLConnection)url.openConnection();
        con.setDoOutput(true);
        con.setRequestMethod("POST");
        con.setRequestProperty(header.getKey(), header.getValue());

        // Start Post
        DataOutputStream output = new DataOutputStream(con.getOutputStream());
        output.writeBytes(postData.toJSONString());
        output.flush();
        output.close();

        // Get respond
        if (con.getResponseCode() == 200) {
            BufferedReader input = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inline;
            StringBuilder response = new StringBuilder();
            while ((inline = input.readLine()) != null) {
                response.append(inline);
            }
            input.close();
            JSONObject gameData = null;
            try {
                gameData = (JSONObject) new JSONParser().parse(response.toString());
            } catch (ParseException e) {
                System.out.println("Error happened when parsing: " + e.getPosition());
            }
            return gameData;
        }
        return new JSONObject();
    }

    private String ReadTokenFile() {
        File tokenFile = new File("token");
        if (tokenFile.exists()) {
            StringBuilder sb = new StringBuilder();
            BufferedReader reader;
            try {
                reader = new BufferedReader(new FileReader(tokenFile));
                String inline;
                while ((inline = reader.readLine()) != null) {
                    sb.append(inline);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return sb.toString();
        }
        else
            return null; // Pass
    }

    private boolean WriteTokenFile(String token) {
        File tokenFile = new File("token");
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(tokenFile));
            writer.write(token);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return true; //Pass
    }

    public static void main(String[] args) throws IOException {

        /*
        //Check Token Test
        JSONParser parser = new JSONParser();
        String s = "{\"boy\":{\"name\": }}";
        try {
            JSONObject obj = (JSONObject) parser.parse(s);
            System.out.println(obj.isEmpty());
        } catch (ParseException e) {
            System.out.print("Exp Position" + e.getPosition());
        }
        JSONObject obj = new JSONObject();
        System.out.println(obj.isEmpty()); */

        Game g = new Game();
        g.JoinGame("BD Ja1");
    }
}
