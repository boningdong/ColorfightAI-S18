package Colorfight;

import org.json.simple.JSONObject;

public class User implements Comparable<User> {
    public int id;
    public String name;
    public double cdTime;
    public double buildCdTime;
    public int cellNum;
    public int baseNum;
    public int goldCellNum;
    public int energyCellNum;
    public double energy = -1;
    public double gold = -1;

    User(JSONObject userData) {
        id = Math.toIntExact((long)userData.get("id"));
        name = userData.get("name").toString();
        cdTime = (double)userData.get("cd_time");
        buildCdTime = (double)userData.get("build_cd_time");
        cellNum = Math.toIntExact((long)userData.get("cell_num"));
        baseNum = Math.toIntExact((long)userData.get("base_num"));
        goldCellNum = Math.toIntExact((long)userData.get("gold_cell_num"));
        energyCellNum = Math.toIntExact((long)userData.get("energy_cell_num"));
        if (userData.get("energy") != null)
            energy = (double)userData.get("energy");
        if (userData.get("gold") != null)
            gold = (double)userData.get("gold");
    }

    @Override
    public int compareTo(User user) {
        return (this.cellNum < user.cellNum ? -1 : (this.cellNum == user.cellNum ? 0 : 1));
    }

}


