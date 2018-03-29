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
        isTaking = (long)cellData.get("c") == 1;
        x = Math.toIntExact((long)cellData.get("x"));
        y = Math.toIntExact((long)cellData.get("y"));
        occupyTime = (double)(Object)cellData.get("ot");
        attackTime = (double)(Object)cellData.get("at");
        takeTime = Double.parseDouble(cellData.get("t") + "");
        finishTime = (double)cellData.get("f");
        cellType = (String)cellData.get("ct");
        buildType = (String)cellData.get("b");
        isBase = buildType.equals("base");
        isBuilding = !((boolean)cellData.get("bf"));
        buildTime = (double)cellData.get("bt");
    }

    Cell(Cell cell) {
        owner = cell.owner;
        attacker = cell.attacker;
        isTaking = cell.isTaking;
        x = cell.x;
        y = cell.y;
        occupyTime = cell.occupyTime;
        attackTime = cell.attackTime;
        takeTime = cell.takeTime;
        finishTime = cell.finishTime;
        cellType = cell.cellType;
        buildType = cell.buildType;
        isBase = cell.isBase;
        isBuilding = cell.isBuilding;
        buildTime = cell.buildTime;
    }
}
