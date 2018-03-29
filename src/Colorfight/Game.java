package Colorfight;

import javafx.util.Pair;
import org.jetbrains.annotations.Nullable;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;

public class Game {

    public Data data = null;
    public String token = "";
    public String name = "";
    public int uid = -1;
    public String gameVersion = "";

    public int cellNum = 0;
    public int baseNum = 0;
    public int goldCellNum = 0;
    public int energyCellNum = 0;
    public double cdTime = 0.0;
    public double buildCdTime = 0.0;
    public double energy = 0.0;
    public double gold = 0.0;
    public ArrayList<User> users = new ArrayList<>();

    // Game info
    public int width;
    public int height;
    public long gameId;
    public double currTime;
    public double endTime;
    public double joinEndTime;
    public double lastUpdate;

    // Constructor
    public Game() {
        Refresh();
    }

    // Inner class and static methods
    private class Data {
        // Game Info
        boolean aiOnly;
        double joinEndTime;
        int height, width;
        double currTime;
        double planStartTime;
        double endTime;
        long gameId;
        String gameVersion;
        ArrayList<User> users = new ArrayList<>();
        ArrayList<Cell> cells = new ArrayList<>();

        Data(JSONObject data) {
            // Basic info
            JSONObject info = (JSONObject) data.get("info");
            aiOnly = (boolean) info.get("ai_only");
            joinEndTime = (double) info.get("join_end_time");
            height = Math.toIntExact((long)info.get("height"));
            width = Math.toIntExact((long)info.get("width"));
            currTime = (double) info.get("time");
            planStartTime = Double.parseDouble(info.get("plan_start_time").toString());
            endTime = (double) info.get("end_time");
            gameId = (long) info.get("game_id");
            gameVersion = info.get("game_version").toString();
            // Users
            JSONArray usersData = (JSONArray) data.get("users");
            Iterator<JSONObject> usersItr = usersData.iterator();
            while (usersItr.hasNext())
                users.add(new User(usersItr.next()));

            // Cells
            JSONArray cellsData = (JSONArray) data.get("cells");
            Iterator<JSONObject> cellsItr = cellsData.iterator();
            while (cellsItr.hasNext())
                cells.add(new Cell(cellsItr.next()));
        }
    }

    public static enum BlastDirection {
        Square("square"),
        Vertical("vertical"),
        Horizontal("horizontal");

        private final String string;

        private BlastDirection(String s) {
            string = s;
        }

        public String toString() {
            return string;
        }
    }

    public static enum BlastType {
        Attack("attack"),
        Defense("defense");

        private final String string;

        private BlastType(String s) {
            string = s;
        }

        public String toString() {
            return string;
        }
    }

    static final String hostUrl = "http://colorfight.herokuapp.com/";

    static void CopyInfoTo(Data target, Data origin) {
        target.aiOnly = origin.aiOnly;
        target.joinEndTime = origin.joinEndTime;
        target.height = origin.height;
        target.width = origin.width;
        target.currTime = origin.currTime;
        target.planStartTime = origin.planStartTime;
        target.endTime = origin.endTime;
        target.gameId = origin.gameId;
        target.gameVersion = origin.gameVersion;
    }

    // Public game methods
    public boolean JoinGame(String name) {
        if (name == null)
            return false;

        String token = ReadTokenFile();
        if (token != null) {
            try {
                JSONObject gameData = CheckToken(token);
                if (!gameData.isEmpty()) {
                    if (name.equals((String) gameData.get("name"))) {
                        this.token = token;
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
                data = new Data(gameData);
                UpdateGameInfo();
                UpdateUsersInfo(new Data(gameData));
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
                Data tempData = new Data(gameData);
                CopyInfoTo(data, tempData);
                UpdateGameInfo();
                UpdateCellsInfo(tempData);
                UpdateUsersInfo(tempData);
            } else {
                return false;
            }
        }
        return true;
    }

    public Cell GetCell(int x, int y) {
        if (0 <= x && x < width && 0 <= y && y < height)
            return new Cell(data.cells.get(x + y * width));
        else {
            System.out.println("Invalid x, y value. Cannot get cell. Return null");
            return null;
        }
    }

    public Result AttackCell (int x, int y) {
        return AttackCell(x, y, false);
    }

    public Result AttackCell (int x, int y, boolean boost) {
        if (!token.equals("")) {
            // Load post info
            Pair<String, String> header = new Pair<>("content-type", "application/json");
            JSONObject postData = new JSONObject();
            postData.put("cellx", x);
            postData.put("celly", y);
            postData.put("boost", boost);
            postData.put("token", token);
            // Post attack action
            JSONObject attackResult = PostForData(hostUrl + "attack", postData, header);
            // Return result
            if (!attackResult.isEmpty())
                return new Result(attackResult);
            else
                return new Result(false, -1, "Server did not return correctly.");
        } else {
            return new Result(false, -1, "You need to join the game first!");
        }
    }

    public Result BuildBase (int x, int y) {
        if (!token.equals("")) {
            // Load post info
            Pair<String, String> header = new Pair<>("content-type", "application/json");
            JSONObject postData = new JSONObject();
            postData.put("cellx", x);
            postData.put("celly", y);
            postData.put("token", token);
            // Post build action
            JSONObject buildResult = PostForData(hostUrl + "buildbase", postData, header);
            // Return result
            if (!buildResult.isEmpty())
                return new Result(buildResult);
            else
                return new Result(false, -1, "Server did not return correctly.");
        } else {
            return new Result(false, -1, "You need to join the game first!");
        }
    }

    public Result Blast (int x, int y, BlastDirection direction, BlastType type) {
        if (!token.equals("")) {
            // Load post info
            Pair<String, String> header = new Pair<>("content-type", "application/json");
            JSONObject postData = new JSONObject();
            postData.put("cellx", x);
            postData.put("celly", y);
            postData.put("token", token);
            postData.put("direction", direction.toString());
            postData.put("blastType", type.toString());
            // Post blast action
            JSONObject blastResult = PostForData(hostUrl + "blast", postData, header);
            if (!blastResult.isEmpty())
                return new Result(blastResult);
            else
                return new Result(false, -1, "Server did not return correctly.");
        } else {
            return new Result(false, -1, "You need to join the game first!");
        }
    }

    // Private game methods
    private void UpdateGameInfo() {
        this.width = data.width;
        this.height = data.height;
        this.currTime = data.currTime;
        this.endTime = data.endTime;
        this.joinEndTime = data.joinEndTime;
        this.gameId = data.gameId;
        this.lastUpdate = this.currTime;
    }

    private double GetTakeTimeEq(double timeDiff) {
        if (timeDiff <= 0)
            return 33.0;
        return 30.0 * Math.pow(2, -timeDiff/30.0) + 3;
    }

    private void UpdateCellsInfo(Data tempData) {
        for (Cell cell : tempData.cells) {
            int i = cell.x + cell.y * width;
            data.cells.set(i, cell);
        }
        for (Cell cell : data.cells) {
            if (cell.isTaking)
                cell.takeTime = -1;
            else {
                if (cell.owner == 0)
                    cell.takeTime = 2.0;
                else
                    cell.takeTime = GetTakeTimeEq(currTime - cell.occupyTime);
            }
        }
    }

    private void UpdateUsersInfo(Data tempData) {
        users = new ArrayList<>();
        for (User user : tempData.users) {
            users.add(user);
            if (user.id == uid) {
                gold = user.gold;
                energy = user.energy;
                cdTime = user.cdTime;
                buildCdTime = user.buildCdTime;
                cellNum = user.cellNum;
                baseNum = user.baseNum;
                goldCellNum = user.goldCellNum;
                energyCellNum = user.energyCellNum;
            }
        }
        Collections.sort(users, Collections.reverseOrder());
    }

    // Json Related methods
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

}
