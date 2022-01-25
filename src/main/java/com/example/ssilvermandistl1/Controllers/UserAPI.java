package com.example.ssilvermandistl1.Controllers;

import com.example.ssilvermandistl1.Models.Email.SendMail;
import com.example.ssilvermandistl1.Models.Offers;
import com.example.ssilvermandistl1.Models.UserPOJO;
import com.example.ssilvermandistl1.Models.VideoGamePOJO;
import com.example.ssilvermandistl1.Repositories.OfferRepository;
import com.example.ssilvermandistl1.Repositories.UserRepository;
import com.example.ssilvermandistl1.Repositories.VideoGameRepository;
import lombok.Data;
import net.minidev.json.annotate.JsonIgnore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.Link;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.web.bind.annotation.*;

import javax.management.openmbean.KeyAlreadyExistsException;
import javax.servlet.http.HttpServletResponse;
import javax.transaction.Transactional;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

@RestController
@RequestMapping(path="/user")
public class UserAPI {

    @Autowired
    private UserDetailsManager udm;
    @Autowired
    private PasswordEncoder pswdEnc = null;
    @Autowired
    private UserRepository userRepo;
    @Autowired
    private VideoGameRepository vidRepo;
    @Autowired
    private OfferRepository offerRepo;
    @Autowired
    private BLL bll;



    //add a user
    @PostMapping(path = "/createUser", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(code= HttpStatus.CREATED)
    public UserPOJO createUser(@RequestBody UserPOJO testUser) {
        if (userRepo.getFirstByEmail(testUser.getEmail()).isPresent()){
            throw new KeyAlreadyExistsException("A user with that email already exists!");
        }
        userRepo.save(testUser);
        UserDetails newUser = User.withUsername(testUser.getEmail())
                .password(pswdEnc.encode(testUser.getPassword()))
                .roles("USER").build();
        udm.createUser(newUser);
        for(Link link : generateUserLinks(testUser.getId())){
            testUser.add(link); //puts all the generated links into the user
        }

        return testUser;
    }

    private ArrayList<Link> generateUserLinks(int id){
        ArrayList<Link> links = new ArrayList<>();
        String selfLink = String.format("http://localhost:8080/userPOJOes/%s", id);
        Link linkSelf = Link.of(selfLink, "self"); //adds the self link
        links.add(linkSelf);
        String POJOLink = String.format("http://localhost:8080/userPOJOes/%s", id);
        Link linkPOJO = Link.of(POJOLink, "userPOJO"); //adds the userPOJO link
        links.add(linkPOJO);
        String gameListLink = String.format("http://localhost:8080/userPOJOes/%s/gameList", id);
        Link linkGameList = Link.of(gameListLink, "gameList"); //adds the gameList link
        links.add(linkGameList);
        return links;
    }

    @GetMapping(path="/userVer/testVer")
    public Boolean isVerified(){
        return true;
    }

    @PatchMapping(path="/userVer/removeGameFromUser")
    public Link removeGameFromUser(@RequestParam String strGameId, @RequestParam String strUserId, HttpServletResponse res){
        int gameId = Integer.parseInt(strGameId);
        int userId = Integer.parseInt(strUserId);
        Optional<UserPOJO> optUser = userRepo.findById(userId);
        Optional<VideoGamePOJO> optGame = vidRepo.findById(gameId);
        if(optUser.isPresent() && optGame.isPresent()){
            UserPOJO userPOJO = optUser.get();
            VideoGamePOJO game = optGame.get();
//            List<VideoGamePOJO> gameList = userPOJO.getGameList();
//            gameList.remove(game);
//            userPOJO.setGameList(gameList);
            for(VideoGamePOJO vidGame : userPOJO.getGameList()){ //this should(please) only let you remove a game from a user if they own it
                if(game.equals(vidGame)){
                    vidRepo.delete(game);
//                    userRepo.save(userPOJO);
                    String outLink = String.format("http://localhost:8080/userPOJOes/%s/gameList", userId);
                    return Link.of(outLink, "gameList");
                }
            }
            res.setStatus(404);
            return null;

        }else{
            res.setStatus(404);
            return null;
        }
    }

    //swap the items inside of the offer lists
    //when proposing an offer check that both users have the games asked for

    @GetMapping(path="/userVer/UserContainsGames")
    public String userContainsGames(@RequestParam String strUserId, @RequestBody GameListPOJO gameListPOJO){
        System.err.println("1");
        List<VideoGamePOJO> gameList = bll.convertListToGames(gameListPOJO.getGameIdList());
        if(gameList.contains(null)) return "not all of those games exist!";
        int userId;
        try{
            userId = Integer.parseInt(strUserId);
        }catch (NumberFormatException nfe){
            return "User Id needs to be an integer!";
        }
        Optional<UserPOJO> optUser = userRepo.findById(userId);
        if(optUser.isEmpty()){
            return "User with that Id does not exist!";
        }
        UserPOJO userPOJO = optUser.get();
        if (bll.userHasGames(userPOJO, gameList)) return String.format("%s contains all of the listed games", userPOJO.getName());
        else return String.format("%s does not contain all of the listed games", userPOJO.getName());
    }

    @Data
    public static class GameListPOJO{
        private List<Integer> gameIdList;
    }

    public String[] decodeAuth(String encodedString) {
        // Tristyn's baby
        encodedString = encodedString.substring(encodedString.indexOf(" ") + 1);
        byte[] decodedBytes = Base64.getDecoder().decode(encodedString);
        String decodedString = new String(decodedBytes);
        return decodedString.split(":", 2);
    }

    // change name, change address, change password
    @PatchMapping(path="/userVer/changeName", produces = MediaType.APPLICATION_JSON_VALUE)
    public UserPOJO changeName(@RequestHeader(value="Authorization") String authorizationHeader, @RequestParam String updateName) {
        // get user at auth, change name, save
        UserPOJO curUser = userRepo.getByEmail(decodeAuth(authorizationHeader)[0]);
        curUser.setName(updateName);
        userRepo.save(curUser);
        for(Link link : generateUserLinks(curUser.getId())){
            curUser.add(link); //puts all the generated links into the user
        }
        return curUser;
    }
    @PatchMapping(path="/userVer/changeAddress", produces = MediaType.APPLICATION_JSON_VALUE)
    public UserPOJO changeAddress(@RequestHeader(value="Authorization") String authorizationHeader, @RequestParam String updateAddress) {
        // get user at auth, change name, save
        UserPOJO curUser = userRepo.getByEmail(decodeAuth(authorizationHeader)[0]);
        curUser.setStreetAddress(updateAddress);
        userRepo.save(curUser);
        for(Link link : generateUserLinks(curUser.getId())){
            curUser.add(link); //puts all the generated links into the user
        }
        return curUser;
    }
    @PatchMapping(path="/userVer/changePassword", produces = MediaType.APPLICATION_JSON_VALUE)
    public UserPOJO changePassword(@RequestHeader(value="Authorization") String authorizationHeader, @RequestParam String updatePassword) {
        // get user at auth, change name, save
        UserPOJO curUser = userRepo.getByEmail(decodeAuth(authorizationHeader)[0]);
        curUser.setPassword(updatePassword);
        userRepo.save(curUser);
        for(Link link : generateUserLinks(curUser.getId())){
            curUser.add(link); //puts all the generated links into the user
        }
//        udm.deleteUser(curUser.getEmail()); //delete the user and create a new one to update the password
        UserDetails newUser = User.withUsername(curUser.getEmail())
                .password(pswdEnc.encode(curUser.getPassword()))
                .roles("USER").build();
//        udm.createUser(newUser);
        udm.updateUser(newUser);//updates the user (probably based on if the email matches up... idk though)
        return curUser;
    }

    @GetMapping(path="/forgotPassword")
    public String sendTemporaryPassword(@RequestParam String name, @RequestParam String email, HttpServletResponse res) {
        //Remove Old User From InMemoryUserDetailsManager
        String tempNums = "1234567890";
        String tempLowLetters = "qwertyuiopasdfghjklmnbvcxz";
        String tempUpLetters = "QWERTYUIOPASDFGHJKLZXCVBNM";
        String tempChars = "!@#$%^&*()[]{};:'<>,.\\/?";

        StringBuilder tempPassword = new StringBuilder();
        Random rand = new Random();
        while(tempPassword.length() < 16) {
            tempPassword.append(tempNums.charAt(rand.nextInt(tempNums.length())));
            tempPassword.append(tempLowLetters.charAt(rand.nextInt(tempLowLetters.length())));
            tempPassword.append(tempUpLetters.charAt(rand.nextInt(tempUpLetters.length())));
            tempPassword.append(tempChars.charAt(rand.nextInt(tempChars.length())));
        }

        Optional<UserPOJO> optUser = userRepo.findByNameAndEmail(name, email);
        if(optUser.isEmpty()){
            res.setStatus(404);
            return "Name or Email does not exist";
        }
        UserPOJO currentUser = optUser.get();
        currentUser.setPassword(tempPassword.toString());
        userRepo.save(currentUser);
        UserDetails updUser = User.withUsername(currentUser.getEmail())
                .password(pswdEnc.encode(currentUser.getPassword()))
                .roles("USER").build();
        udm.updateUser(updUser);//update the user to match the new password
        new SendMail(email, "Recovery Password", "Here is your temporary password: " + currentUser.getPassword());
        return String.format("A recovery password has been sent to %s \nMake sure to check your junk/spam folder if you do not see the email", currentUser.getEmail());
    }

    //create CUD endpoints for videogame creation that link it to the user and check that the user owns it for update

    @Transactional
    @PostMapping(path="/userVer/addGame")
    public VideoGamePOJO addGame(@RequestBody VideoGamePOJO game, @RequestHeader(value="Authorization") String authorizationHeader){
        UserPOJO userPOJO = userRepo.getByEmail(decodeAuth(authorizationHeader)[0]);
        game.setGameOwner(userPOJO);
        vidRepo.save(game);
        userPOJO.addGame(game);
        userRepo.save(userPOJO);
        for(Link link : generateGameLinks(game.getId())){
            game.add(link); //puts all the generated links into the game
        }
        return game;
    }

//    "name": "Mario",
//            "publisher": "Nintendo",
//            "yearPublished": 1993,
//            "systemUsed": "MS-DOS",
//            "gameCondition": "MINT",
//            "previousOwners": 80,

    @PatchMapping(path="/userVer/updateGameName")
    public VideoGamePOJO updateGameName(@RequestParam String strGameId, @RequestParam String newName, HttpServletResponse res, @RequestHeader(value="Authorization") String authorizationHeader){
        int gameId;
        try{
            gameId = Integer.parseInt(strGameId);
        }catch (NumberFormatException nfe){
            System.err.println("A number must be entered!");
            res.setStatus(400);
            return null;
        }
        Optional<VideoGamePOJO> optGame = vidRepo.findById(gameId);
        if(optGame.isEmpty()){
            res.setStatus(404);
            System.err.println("game with that id does not exist");
            return null;
        }
        VideoGamePOJO game = optGame.get();
        UserPOJO userPOJO = userRepo.getByEmail(decodeAuth(authorizationHeader)[0]);
        if (userPOJO.getGameList().contains(game)){
            game.setName(newName);
            vidRepo.save(game);
        }else{
            res.setStatus(404);
            System.err.println("user does not have that game!");
            return null;
        }
        for(Link link : generateGameLinks(game.getId())){
            game.add(link); //puts all the generated links into the game
        }
        return game;

    }

    @PatchMapping(path="/userVer/updateGamePublisher")
    public VideoGamePOJO updateGamePublisher(@RequestParam String strGameId, @RequestParam String updPublisher, HttpServletResponse res, @RequestHeader(value="Authorization") String authorizationHeader){
        int gameId;
        try{
            gameId = Integer.parseInt(strGameId);
        }catch (NumberFormatException nfe){
            System.err.println("A number must be entered!");
            res.setStatus(400);
            return null;
        }
        Optional<VideoGamePOJO> optGame = vidRepo.findById(gameId);
        if(optGame.isEmpty()){
            res.setStatus(404);
            System.err.println("game with that id does not exist");
            return null;
        }
        VideoGamePOJO game = optGame.get();
        UserPOJO userPOJO = userRepo.getByEmail(decodeAuth(authorizationHeader)[0]);
        if (userPOJO.getGameList().contains(game)){
            game.setPublisher(updPublisher);
            vidRepo.save(game);
        }else{
            res.setStatus(404);
            System.err.println("user does not have that game!");
            return null;
        }
        for(Link link : generateGameLinks(game.getId())){
            game.add(link); //puts all the generated links into the game
        }
        return game;

    }

    @PatchMapping(path="/userVer/updateGameYear")
    public VideoGamePOJO updateGameYear(@RequestParam String strGameId, @RequestParam String updYear, HttpServletResponse res, @RequestHeader(value="Authorization") String authorizationHeader){
        int gameId;
        int gameYear;
        try{
            gameId = Integer.parseInt(strGameId);
            gameYear = Integer.parseInt(updYear);
        }catch (NumberFormatException nfe){
            System.err.println("A number must be entered!");
            res.setStatus(400);
            return null;
        }
        Optional<VideoGamePOJO> optGame = vidRepo.findById(gameId);
        if(optGame.isEmpty()){
            res.setStatus(404);
            System.err.println("game with that id does not exist");
            return null;
        }
        VideoGamePOJO game = optGame.get();
        UserPOJO userPOJO = userRepo.getByEmail(decodeAuth(authorizationHeader)[0]);
        if (userPOJO.getGameList().contains(game)){
            game.setYearPublished(gameYear);
            vidRepo.save(game);
        }else{
            res.setStatus(404);
            System.err.println("user does not have that game!");
            return null;
        }
        for(Link link : generateGameLinks(game.getId())){
            game.add(link); //puts all the generated links into the game
        }
        return game;

    }

    @PatchMapping(path="/userVer/updateGameSystem")
    public VideoGamePOJO updateGameSystem(@RequestParam String strGameId, @RequestParam String updSystem, HttpServletResponse res, @RequestHeader(value="Authorization") String authorizationHeader){
        int gameId;
        try{
            gameId = Integer.parseInt(strGameId);
        }catch (NumberFormatException nfe){
            System.err.println("A number must be entered!");
            res.setStatus(400);
            return null;
        }
        Optional<VideoGamePOJO> optGame = vidRepo.findById(gameId);
        if(optGame.isEmpty()){
            res.setStatus(404);
            System.err.println("game with that id does not exist");
            return null;
        }
        VideoGamePOJO game = optGame.get();
        UserPOJO userPOJO = userRepo.getByEmail(decodeAuth(authorizationHeader)[0]);
        if (userPOJO.getGameList().contains(game)){
            game.setSystemUsed(updSystem);
            vidRepo.save(game);
        }else{
            res.setStatus(404);
            System.err.println("user does not have that game!");
            return null;
        }
        for(Link link : generateGameLinks(game.getId())){
            game.add(link); //puts all the generated links into the game
        }
        return game;

    }

    @PatchMapping(path="/userVer/updateGameCondition")
    public VideoGamePOJO updateGameCondition(@RequestParam String strGameId, @RequestParam String updCondition, HttpServletResponse res, @RequestHeader(value="Authorization") String authorizationHeader){
        int gameId;
        try{
            gameId = Integer.parseInt(strGameId);
        }catch (NumberFormatException nfe){
            System.err.println("A number must be entered!");
            res.setStatus(400);
            return null;
        }
        Optional<VideoGamePOJO> optGame = vidRepo.findById(gameId);
        if(optGame.isEmpty()){
            res.setStatus(404);
            System.err.println("game with that id does not exist");
            return null;
        }
        VideoGamePOJO game = optGame.get();
        UserPOJO userPOJO = userRepo.getByEmail(decodeAuth(authorizationHeader)[0]);
        try {
            if (userPOJO.getGameList().contains(game)) {
                game.setGameCondition(VideoGamePOJO.GameCondition.valueOf(updCondition.toUpperCase(Locale.ROOT)));
                vidRepo.save(game);
            } else {
                res.setStatus(404);
                System.err.println("user does not have that game!");
                return null;
            }
        }catch(Exception e){
            System.err.println("That condition does not exist!");
            res.setStatus(400);
            return null;
        }
        for(Link link : generateGameLinks(game.getId())){
            game.add(link); //puts all the generated links into the game
        }
        return game;

    }

    @PatchMapping(path="/userVer/updateGamePrevOwners")
    public VideoGamePOJO updateGamePrevOwners(@RequestParam String strGameId, @RequestParam String updPrevOwner, HttpServletResponse res, @RequestHeader(value="Authorization") String authorizationHeader){
        int gameId;
        int prevOwnerNum;
        try{
            gameId = Integer.parseInt(strGameId);
            prevOwnerNum = Integer.parseInt(updPrevOwner);
        }catch (NumberFormatException nfe){
            System.err.println("A number must be entered!");
            res.setStatus(400);
            return null;
        }
        Optional<VideoGamePOJO> optGame = vidRepo.findById(gameId);
        if(optGame.isEmpty()){
            res.setStatus(404);
            System.err.println("game with that id does not exist");
            return null;
        }
        VideoGamePOJO game = optGame.get();
        UserPOJO userPOJO = userRepo.getByEmail(decodeAuth(authorizationHeader)[0]);
        if (userPOJO.getGameList().contains(game)){
            game.setPreviousOwners(prevOwnerNum);
            vidRepo.save(game);
        }else{
            res.setStatus(404);
            System.err.println("user does not have that game!");
            return null;
        }
        for(Link link : generateGameLinks(game.getId())){
            game.add(link); //puts all the generated links into the game
        }
        return game;

    }

    private ArrayList<Link> generateGameLinks(int id){
        ArrayList<Link> links = new ArrayList<>();
        String selfLink = String.format("http://localhost:8080/videoGamePOJOes/%s", id);
        Link linkSelf = Link.of(selfLink, "self"); //adds the self link
        links.add(linkSelf);
        String POJOLink = String.format("http://localhost:8080/videoGamePOJOes/%s", id);
        Link linkPOJO = Link.of(POJOLink, "videoGamePOJO"); //adds the userPOJO link
        links.add(linkPOJO);
        String gameOwnerLink = String.format("http://localhost:8080/videoGamePOJOes/%s/gameOwner", id);
        Link linkGameList = Link.of(gameOwnerLink, "gameOwner"); //adds the gameList link
        links.add(linkGameList);
        return links;
    }

    @JsonIgnore
    @Transactional
    @PostMapping(path="/userVer/createOffer", produces = MediaType.APPLICATION_JSON_VALUE)
    public Offers createOffer(HttpServletResponse res, @RequestHeader(value="Authorization") String authorizationHeader, @RequestParam String strOfferList, @RequestParam String strReceiveId, @RequestParam String strReceiveList){
        UserPOJO offerUser = userRepo.getByEmail(decodeAuth(authorizationHeader)[0]);
        UserPOJO receiveUser;
        List<VideoGamePOJO> offerList = new ArrayList<>();
        List<VideoGamePOJO> receiveList = new ArrayList<>();
        List<Integer> intOffersList = new ArrayList<>();
        List<Integer> intReceiveList = new ArrayList<>();
        int receiveId;
        try{
            receiveId = Integer.parseInt(strReceiveId);
        }catch (NumberFormatException nfe){
            System.err.println("Id must be a number!");
            res.setStatus(404);
            return null;
        }
        if(userRepo.findById(receiveId).isEmpty()){
            res.setStatus(404);
            System.err.println("User with that id does not exist!");
            return null;
        }
        receiveUser = userRepo.getById(receiveId);
        try{
            intOffersList = bll.convertStrToIntegerList(strOfferList);
            System.out.println(intOffersList);
            intReceiveList = bll.convertStrToIntegerList(strReceiveList);
            System.out.println(intReceiveList);

        }catch (NumberFormatException nfe){
            res.setStatus(400);
            System.err.println("You must enter a list of numbers!"); //BRUH, change the bll to work with arrays >:(
            return null;
        }
        if(bll.userContainsGames(offerUser.getId(), intOffersList)){
            System.err.println("user has all of these games"); //fix the user has games with just int list
            offerList = bll.convertListToGames(intOffersList); //sets the offerlist to a list of games if the offer user has all of them
        }
        else{
            System.err.println("Offer User does not have all of these games!");
            res.setStatus(404);
            return null;
        }
        if(bll.userContainsGames(receiveUser.getId(), intReceiveList)){
            System.err.println("user has all of these games"); //fix the user has games with just int list
            receiveList = bll.convertListToGames(intReceiveList); //sets the receivelist to a list of games if the offer user has all of them
        }
        else{
            System.err.println("Offer User does not have all of these games!");
            res.setStatus(404);
            return null;
        }
        Offers offer = new Offers(offerUser, offerList, receiveUser, receiveList);
        //System.out.println(offer);
        return offer;
    }

}
