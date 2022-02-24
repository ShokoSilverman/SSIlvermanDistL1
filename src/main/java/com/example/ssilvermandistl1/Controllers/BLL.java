package com.example.ssilvermandistl1.Controllers;

import com.example.ssilvermandistl1.Models.UserPOJO;
import com.example.ssilvermandistl1.Models.VideoGamePOJO;
import com.example.ssilvermandistl1.Repositories.OfferRepository;
import com.example.ssilvermandistl1.Repositories.UserRepository;
import com.example.ssilvermandistl1.Repositories.VideoGameRepository;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import net.minidev.json.JSONObject;
import netscape.javascript.JSObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class BLL {
    @Autowired
    private UserRepository userRepo;
    @Autowired
    private VideoGameRepository vidRepo;
    @Autowired
    private OfferRepository offerRepo;

    @Value("${RABBIT_NAME:localhost}")
    String hostname;
    @Value("${RABBIT_PORT:9001}")
    int rabbitPort;

    public BLL() throws UnknownHostException {
    }


    public boolean userHasGames(UserPOJO userPOJO, List<VideoGamePOJO> gameList){
        List<VideoGamePOJO> userGameList = userPOJO.getGameList();
        for(VideoGamePOJO game : gameList){
            if(!userGameList.contains(game)){
                return false;
            }
        }
        return true;
    }

    public List<VideoGamePOJO> convertListToGames(List<Integer> gameIdList){
        List<VideoGamePOJO> outList = new ArrayList<>();
        for(int game : gameIdList){
            if(vidRepo.existsById(game)){
                outList.add(vidRepo.getById(game));
            }else{
                outList.add(null);
            }
        }
        return outList;
    }

    public boolean userContainsGames(int userId, List<Integer> gameIdList){
        List<VideoGamePOJO> gameList = convertListToGames(gameIdList);
        if(gameList.contains(null)) return false; //check if the game list has any games that don't exist
        Optional<UserPOJO> optUser = userRepo.findById(userId);
        if(optUser.isEmpty()){ //check if user exists
            return false;
        }
        UserPOJO userPOJO = optUser.get();
        return (userHasGames(userPOJO, gameList)); //returns true if user has all games passed in, false otherwise
    }

    public List<Integer> convertStrToIntegerList(String strOfferList){
        List<Integer> intOffersList = new ArrayList<>();
        try{
            strOfferList = strOfferList.replace(" ", "");
            String[] stringOfferList = strOfferList.split(",");
            intOffersList = Arrays.stream(stringOfferList)    // stream of String
                    .map(Integer::valueOf) // stream of Integer
                    .collect(Collectors.toList());
        }catch (NumberFormatException nfe){
//            System.err.println("numbers must be entered!");
            throw new NumberFormatException();
        }

        return intOffersList;
    }

    public String sendEmail(String message, String email, String subject){
        JSONObject obj = new JSONObject();
        obj.put("email", email);
        obj.put("message", message);
        obj.put("subject", subject);
        try{
            String strInfoOut = String.format("%s||%s",email,message);
            ConnectionFactory factory = new ConnectionFactory();
            factory.setHost(hostname);
            factory.setPort(rabbitPort); //used to change port
            Connection connection = factory.newConnection();
            Channel channel = connection.createChannel();
            channel.queueDeclare("SSilverman_Queue", false, false, false, null);
//            channel.basicPublish("", "SSilverman_Queue", null, strInfoOut.getBytes());
            channel.basicPublish("", "SSilverman_Queue", null, obj.toString().getBytes());
            channel.close();
            connection.close();
        }catch (Exception e){
            System.err.println("FUck");
            e.printStackTrace();
            return "failed";
        }
        return "done";
    }




}
