package Colorfight;

import org.json.simple.JSONObject;

import java.util.function.LongToIntFunction;

public class Cell {
    public int owner; //User id
    public int attacker; //User id
    public boolean isTaking;
    public int x, y;
    public double occupyTime;
    public double attackTime;
    public double takeTime;
    public double finishTime;
    public String cellType;
    public String buildType;
    public boolean isBase;
    public boolean isBuilding;
    public double buildTime;

    Cell(JSONObject cellData) {
        owner = Math.toIntExact((long)cellData.get("o"));
        attacker =  Math.toIntExact((long)cellData.get("a"));
        isTaking = (boolean)cellData.get("c");
        x = Math.toIntExact((long)cellData.get("x"));
        y = Math.toIntExact((long)cellData.get("y"));
        occupyTime = (double)cellData.get("ot");
        attackTime = (double)cellData.get("at");
        takeTime = (double)cellData.get("t");
        finishTime = (double) cellData.get("f");
        cellType = (String)cellData.get("ct");
        buildType = (String)cellData.get("b");
        isBase = buildType.equals("base");
        isBuilding = !((boolean)cellData.get("bf"));
        buildTime = (double)cellData.get("bt");
    }
}
