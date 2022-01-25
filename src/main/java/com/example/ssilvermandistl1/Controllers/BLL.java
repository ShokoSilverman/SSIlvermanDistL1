package com.example.ssilvermandistl1.Controllers;

import com.example.ssilvermandistl1.Models.UserPOJO;
import com.example.ssilvermandistl1.Models.VideoGamePOJO;
import com.example.ssilvermandistl1.Repositories.OfferRepository;
import com.example.ssilvermandistl1.Repositories.UserRepository;
import com.example.ssilvermandistl1.Repositories.VideoGameRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

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




}
