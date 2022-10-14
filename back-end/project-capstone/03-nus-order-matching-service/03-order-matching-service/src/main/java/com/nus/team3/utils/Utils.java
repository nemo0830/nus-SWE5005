package com.nus.team3.utils;

import com.nus.team3.constants.TradeEnum;
import com.nus.team3.dto.Order;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;

public class Utils {

    private static final Logger logger = LoggerFactory.getLogger(Utils.class);

    public static String getSaltString() {
        String SALTCHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
        StringBuilder salt = new StringBuilder();
        Random rnd = new Random();
        while (salt.length() < 18) { // length of the random string.
            int index = (int) (rnd.nextFloat() * SALTCHARS.length());
            salt.append(SALTCHARS.charAt(index));
        }
        String saltStr = salt.toString();
        return saltStr;
    }

    public static Order constructOrder(String message){
        try{
            String[] messageBodyList = message.split("#");
            String side = messageBodyList[0];
            String stockName = messageBodyList[1];
            int quantity = Integer.parseInt(messageBodyList[2]);
            float price = Float.parseFloat(String.format("%.2f", Double.valueOf(messageBodyList[3])));
            String user = messageBodyList[4];
            long timestamp = System.currentTimeMillis();
            String transactionId = Utils.getSaltString();
            if(orderIsValid(side, quantity, price)){
                return new Order(side,transactionId,stockName, user, timestamp,price,quantity);
            }
            else{
                logger.info("Invalid order message received {}, will not processed further", message);
                return null;
            }
        }catch(Exception e){
            logger.info("Invalid order message received {}, will not processed further", message);
            return null;
        }
    }

    private static boolean orderIsValid(String side, int quantity, float price){
        return (side.equalsIgnoreCase(TradeEnum.SIDE.BUY.name()) ||
                side.equalsIgnoreCase(TradeEnum.SIDE.SELL.name()))
                && quantity > 0
                && price > 0;
    }
}
