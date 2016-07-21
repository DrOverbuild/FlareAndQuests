package com.drizzard.faq;

import org.bukkit.Location;
import org.bukkit.entity.Player;

/**
 * Created by jasper on 7/6/16.
 */
public class TimedAction {

    int time;
    ActionType action;
    String message;

    public TimedAction(int time, ActionType action, String message) {
        this.time = time;
        this.action = action;
        this.message = message;
    }

    public static TimedAction parseTimedAction(String code) {
        String[] args = code.split(" ", 2);

        if (args.length < 2) {
            return new TimedAction(0, ActionType.ERR_ARGS, "");
        }

        String[] timeArgs = args[0].split(":", 2);

        if (args.length < 2) {
            return new TimedAction(0, ActionType.ERR_TIME_ARGS, "");
        }

        if (!timeArgs[0].equalsIgnoreCase("time")) {
            return new TimedAction(0, ActionType.ERR_NOT_TIME, "");
        }

        int seconds;

        try {
            seconds = Integer.parseInt(timeArgs[1]);
        } catch (NumberFormatException e) {
            return new TimedAction(0, ActionType.ERR_PARSING_INT, "");
        }

        String[] actionArgs = args[1].split(":", 2);

        if (actionArgs.length < 2) {
            return new TimedAction(0, ActionType.ERR_ACTION_ARGS, "");
        }

        ActionType actionType;

        try {
            actionType = ActionType.valueOf(actionArgs[0].toUpperCase());
        } catch (IllegalArgumentException e) {
            return new TimedAction(0, ActionType.ERR_PARSING_ACTION, "");
        }

        String message = actionArgs[1];

        return new TimedAction(seconds, actionType, message);
    }

    public static String formatMessage(String message, Player p, String leftSeconds, Location loc) {
        message = Config.formatLine(message, loc, p, leftSeconds, null, null, null);

        if (leftSeconds != null) {
            message = message.replace("{left-seconds}", leftSeconds);
        }

        return message;
    }

    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        this.time = time;
    }

    public ActionType getAction() {
        return action;
    }

    public void setAction(ActionType action) {
        this.action = action;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String[] formatMultiLineMessage(Player p, String leftSeconds, Location loc) {
        String[] lines = getMessage().split("\\|");

        for (int i = 0; i < lines.length; i++) {
            lines[i] = formatMessage(lines[i], p, leftSeconds, loc);
        }

        return lines;
    }

    public enum ActionType {
        MSG, BROADCAST, EXEC, ERR_ARGS, ERR_TIME_ARGS, ERR_NOT_TIME, ERR_PARSING_INT, ERR_ACTION_ARGS, ERR_PARSING_ACTION
    }
}
