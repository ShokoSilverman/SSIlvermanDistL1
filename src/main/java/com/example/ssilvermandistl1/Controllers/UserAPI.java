package com.example.ssilvermandistl1.Controllers;

import com.example.ssilvermandistl1.Models.Offers;
import com.example.ssilvermandistl1.Models.UserPOJO;
import com.example.ssilvermandistl1.Models.VideoGamePOJO;
import com.example.ssilvermandistl1.Repositories.OfferRepository;
import com.example.ssilvermandistl1.Repositories.UserRepository;
import com.example.ssilvermandistl1.Repositories.VideoGameRepository;
import com.example.ssilvermandistl1.Views.JSONViews;
import com.fasterxml.jackson.annotation.JsonView;
import lombok.Data;
import net.minidev.json.annotate.JsonIgnore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Links;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.web.bind.annotation.*;
import java.util.Objects;
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
    @ResponseStatus(code = HttpStatus.CREATED)
    public UserPOJO createUser(@RequestBody UserPOJO testUser) {
        if (userRepo.getFirstByEmail(testUser.getEmail()).isPresent()) {
            throw new KeyAlreadyExistsException("A user with that email already exists!");
        }
        testUser.setPassword(pswdEnc.encode(testUser.getPassword()));
        userRepo.save(testUser);
//        UserDetails newUser = User.withUsername(testUser.getEmail())
//                .password(pswdEnc.encode(testUser.getPassword()))
//                .roles("USER").build();
//        udm.createUser(newUser);
        for (Link link : generateUserLinks(testUser.getId())) {
            testUser.add(link); //puts all the generated links into the user
        }

        return testUser;
    }

    private ArrayList<Link> generateUserLinks(int id) {
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
        String offerInListLink = String.format("http://localhost:8080/userPOJOes/%s/offerListIn", id);
        Link linkOfferInList = Link.of(offerInListLink, "offerListIn");
        links.add(linkOfferInList);
        String offerOutListLink = String.format("http://localhost:8080/userPOJOes/%s/offerListOut", id);
        Link linkOfferOutList = Link.of(offerOutListLink, "offerListOut");
        links.add(linkOfferOutList);
        return links;
    }

    @GetMapping(path = "/userVer/testVer")
    public Boolean isVerified() {
        return true;
    }

    @Transactional
    @PatchMapping(path = "/userVer/removeGameFromUser")
    public Link removeGameFromUser(@RequestParam String strGameId, HttpServletResponse res, @RequestHeader(value = "Authorization") String authorizationHeader) {
        int gameId;
        try {
            gameId = Integer.parseInt(strGameId);
        } catch (NumberFormatException nfe) {
            System.err.println("A number must be entered!");
            throw new NumberFormatException("A number must be entered!");
        }

        UserPOJO userPOJO = userRepo.getByEmail(decodeAuth(authorizationHeader)[0]);
        Optional<VideoGamePOJO> optGame = vidRepo.findById(gameId);
        if (optGame.isPresent()) {
            VideoGamePOJO game = optGame.get();
            for (VideoGamePOJO vidGame : userPOJO.getGameList()) { //this should(please) only let you remove a game from a user if they own it
                if (game.equals(vidGame)) {
                    vidRepo.delete(game);
//                    userRepo.save(userPOJO);
                    String outLink = String.format("http://localhost:8080/userPOJOes/%s/gameList", userPOJO.getId());
                    return Link.of(outLink, "gameList");
                }
            }

        }
        throw new SecurityException("This game is not yours!");
    }

    //swap the items inside of the offer lists
    //when proposing an offer check that both users have the games asked for

    @GetMapping(path = "/userVer/UserContainsGames")
    public String userContainsGames(@RequestParam String strUserId, @RequestBody GameListPOJO gameListPOJO) {
        System.err.println("1");
        List<VideoGamePOJO> gameList = bll.convertListToGames(gameListPOJO.getGameIdList());
        if (gameList.contains(null)) return "not all of those games exist!";
        int userId;
        try {
            userId = Integer.parseInt(strUserId);
        } catch (NumberFormatException nfe) {
            return "User Id needs to be an integer!";
        }
        Optional<UserPOJO> optUser = userRepo.findById(userId);
        if (optUser.isEmpty()) {
            return "User with that Id does not exist!";
        }
        UserPOJO userPOJO = optUser.get();
        if (bll.userHasGames(userPOJO, gameList))
            return String.format("%s contains all of the listed games", userPOJO.getName());
        else return String.format("%s does not contain all of the listed games", userPOJO.getName());
    }

    @Data
    public static class GameListPOJO {
        private List<Integer> gameIdList;
    }

    public String[] decodeAuth(String encodedString) {
        // Tristyn's baby
        encodedString = encodedString.substring(encodedString.indexOf(" ") + 1);
        byte[] decodedBytes = Base64.getDecoder().decode(encodedString);
        String decodedString = new String(decodedBytes);
        return decodedString.split(":", 2);
    }

    @PatchMapping(path = "/userVer/updateUser", produces = MediaType.APPLICATION_JSON_VALUE)
    public UserPOJO updateUser(@RequestHeader(value = "Authorization") String authorizationHeader, @RequestBody UserPOJO userIn){
        UserPOJO curUser = userRepo.getByEmail(decodeAuth(authorizationHeader)[0]);
        if (!Objects.isNull(userIn.getName())) curUser.setName(userIn.getName());
        if (!Objects.isNull(userIn.getStreetAddress())) curUser.setStreetAddress(userIn.getStreetAddress());
        if (!Objects.isNull(userIn.getPassword())) {
            curUser.setPassword(pswdEnc.encode(userIn.getPassword()));
            bll.sendEmail(String.format("Your password has been changed to: %s", curUser.getPassword()), curUser.getEmail(), "Simon's API: Password Reset");
        }
        for (Link link : generateUserLinks(curUser.getId())) {
            curUser.add(link); //puts all the generated links into the user
        }
        userRepo.save(curUser);
//        UserDetails updUser = User.withUsername(curUser.getEmail())
//                .password(pswdEnc.encode(curUser.getPassword()))
//                .roles("USER").build();
//        udm.updateUser(updUser);//update the user to match the new password
        return curUser;
    }

    @GetMapping(path = "/forgotPassword")
    public String sendTemporaryPassword(@RequestParam String name, @RequestParam String email, HttpServletResponse res) {
        //Remove Old User From InMemoryUserDetailsManager
        String tempNums = "1234567890";
        String tempLowLetters = "qwertyuiopasdfghjklmnbvcxz";
        String tempUpLetters = "QWERTYUIOPASDFGHJKLZXCVBNM";
        String tempChars = "!#$%^&*<>.?";

        StringBuilder tempPassword = new StringBuilder();
        Random rand = new Random();
        while (tempPassword.length() < 16) {
            tempPassword.append(tempNums.charAt(rand.nextInt(tempNums.length())));
            tempPassword.append(tempLowLetters.charAt(rand.nextInt(tempLowLetters.length())));
            tempPassword.append(tempUpLetters.charAt(rand.nextInt(tempUpLetters.length())));
            tempPassword.append(tempChars.charAt(rand.nextInt(tempChars.length())));
        }

        Optional<UserPOJO> optUser = userRepo.findByNameAndEmail(name, email);
        if (optUser.isEmpty()) {
            throw new NullPointerException("Name or Email does not exist");
        }
        UserPOJO currentUser = optUser.get();
        currentUser.setPassword(tempPassword.toString());
        userRepo.save(currentUser);
//        UserDetails updUser = udm.loadUserByUsername(currentUser.getEmail());
//        UserDetails updUser = User.withUsername(currentUser.getEmail())
//                .password(pswdEnc.encode(currentUser.getPassword()))
//                .roles("USER").build();
//        udm.updateUser(updUser);//update the user to match the new password
        bll.sendEmail(String.format("Here is your recovery password %s", currentUser.getPassword()), email, "Simon's API: Password Reset");
//        new SendMail(email, "Recovery Password", "Here is your temporary password: " + currentUser.getPassword());
        return String.format("A recovery password has been sent to %s \nMake sure to check your junk/spam folder if you do not see the email", currentUser.getEmail());
    }

    @Transactional
    @PostMapping(path = "/userVer/addGame")
    public VideoGamePOJO addGame(@RequestBody VideoGamePOJO game, @RequestHeader(value = "Authorization") String authorizationHeader) {
        UserPOJO userPOJO = userRepo.getByEmail(decodeAuth(authorizationHeader)[0]);
        game.setGameOwner(userPOJO);
        vidRepo.save(game);
        userPOJO.addGame(game);
        userRepo.save(userPOJO);
        for (Link link : generateGameLinks(game.getId())) {
            game.add(link); //puts all the generated links into the game
        }
        return game;
    }


    @PatchMapping(path="/userVer/updateGame")
    public VideoGamePOJO updateGame(@RequestBody VideoGamePOJO game, @RequestParam String strGameId, @RequestHeader(value = "Authorization") String authorizationHeader){
        UserPOJO userPOJO = userRepo.getByEmail(decodeAuth(authorizationHeader)[0]);
        List<VideoGamePOJO> gameList = new ArrayList<>();
        int gameId;
        try {
            gameId = Integer.parseInt(strGameId);
        } catch (NumberFormatException nfe) {
            throw new NumberFormatException("A number must be entered!");
        }
        System.err.println("got the id!");
        gameList.add(vidRepo.getById(gameId));
        if (!bll.userHasGames(userPOJO, gameList)){
            throw new NullPointerException("User does not have that game!");
        }
        System.err.println("user has the game!");
        VideoGamePOJO userGame = vidRepo.findById(gameId).get();
        try{
            if (!Objects.isNull(game.getName())) userGame.setName(game.getName());
            System.err.println("set name");
            if (!Objects.isNull(game.getYearPublished())) userGame.setYearPublished(game.getYearPublished());
            System.err.println("set year");
            if (!Objects.isNull(game.getSystemUsed())) userGame.setSystemUsed(game.getSystemUsed());
            System.err.println("set system");
            if (!Objects.isNull(game.getPreviousOwners())) userGame.setPreviousOwners(game.getPreviousOwners());
            System.err.println("set prev own");
        }catch (Exception e){
            throw new IllegalArgumentException("Data has not be input correctly!");
        }
        try{
            if (!Objects.isNull(game.getGameCondition())) userGame.setGameCondition(game.getGameCondition());
            System.err.println("set condition");
        }catch (Exception e){
            throw new IllegalArgumentException("That is not a state a game can be!");
        }
        vidRepo.save(userGame);
        System.err.println("adding links!");
        for (Link link : generateGameLinks(userGame.getId())) {
            userGame.add(link); //puts all the generated links into the game
        }
        System.err.println("links added");
        return userGame;
    }

    private ArrayList<Link> generateGameLinks(int id) {
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

    //    @JsonView(JSONViews.OfferView.class)
//    @JsonIgnore
    @Transactional
    @PostMapping(path = "/userVer/createOffer", produces = MediaType.APPLICATION_JSON_VALUE)
    public Offers createOffer(HttpServletResponse res, @RequestHeader(value = "Authorization") String authorizationHeader, @RequestParam String strOfferList, @RequestParam String strReceiveId, @RequestParam String strReceiveList) {
        UserPOJO offerUser = userRepo.getByEmail(decodeAuth(authorizationHeader)[0]);
        UserPOJO receiveUser;
        List<VideoGamePOJO> offerList = new ArrayList<>();
        List<VideoGamePOJO> receiveList = new ArrayList<>();
        List<Integer> intOffersList = new ArrayList<>();
        List<Integer> intReceiveList = new ArrayList<>();
        int receiveId;
        try {
            receiveId = Integer.parseInt(strReceiveId);
        } catch (NumberFormatException nfe) {
            throw new NumberFormatException("A number must be entered!");
        }
        if (userRepo.findById(receiveId).isEmpty()) {
            throw new NullPointerException("User with that id does not exist!");
        }
        receiveUser = userRepo.getById(receiveId);
        try {
            intOffersList = bll.convertStrToIntegerList(strOfferList);
            System.out.println(intOffersList);
            intReceiveList = bll.convertStrToIntegerList(strReceiveList);
            System.out.println(intReceiveList);

        } catch (NumberFormatException nfe) {
            throw new NumberFormatException("A list of numbers must be entered!");
        }
        if (bll.userContainsGames(offerUser.getId(), intOffersList)) {
            System.err.println("user has all of these games"); //fix the user has games with just int list
            offerList = bll.convertListToGames(intOffersList); //sets the offerlist to a list of games if the offer user has all of them
        } else {
            throw new NullPointerException("Offer User does not have all of these games!");
        }
        if (bll.userContainsGames(receiveUser.getId(), intReceiveList)) {
            System.err.println("user has all of these games"); //fix the user has games with just int list
            receiveList = bll.convertListToGames(intReceiveList); //sets the receivelist to a list of games if the offer user has all of them
        } else {
            throw new NullPointerException("Receive User does not have all of these games!");
        }
        Offers offer = new Offers(offerUser, offerList, receiveUser, receiveList);
//        System.out.println(offer);
        offerRepo.save(offer);
        offerUser.addOfferOut(offer);
        userRepo.save(offerUser);
        receiveUser.addOfferIn(offer);
        userRepo.save(receiveUser);
        bll.sendEmail("You made an offer!", offerUser.getEmail(), "Simon's API: You made an offer");
        bll.sendEmail("You received an offer!", receiveUser.getEmail(), "Simon's API: You received an offer");
        //add links
        for (Link link : generateOfferLinks(offer.getId(), offerUser.getId(), receiveUser.getId(), intOffersList, intReceiveList)) {
            offer.add(link); //puts all the generated links into the game
        }
        return offer;
    }

    private ArrayList<Link> generateOfferLinks(int offerId, int offerUserId, int receiveUserId, List<Integer> offerList, List<Integer> receiveList) {
        ArrayList<Link> links = new ArrayList<>();
        links.add(Link.of((String.format("http://localhost:8080/offerses/%s", offerId)), "self"));
        links.add(Link.of((String.format("http://localhost:8080/offerses/%s", offerId)), "offer"));
        for (int offerGame : offerList) {
            links.add(Link.of((String.format("http://localhost:8080/videoGamePOJOes/%s", offerGame)), "offeredVideoGames"));
        }
        for (int receiveGame : receiveList) {
            links.add(Link.of((String.format("http://localhost:8080/videoGamePOJOes/%s", receiveGame)), "offeredVideoGames"));
        }
        links.add(Link.of((String.format("http://localhost:8080/userPOJOes/%s", offerUserId)), "offeringUser"));
        links.add(Link.of((String.format("http://localhost:8080/userPOJOes/%s", receiveUserId)), "receivingUser"));
        return links;
    }

    //accept offer -- check that status == pending, check that each user still has the games, drop/add games for each user, set status to accepted
    //decline offer -- set status to declined

    @Transactional
    @PostMapping(path = "/userVer/acceptOffer")
    public UserPOJO acceptOffer(@RequestHeader(value = "Authorization") String authorizationHeader, @RequestParam String strOfferId) {
        System.err.println("start");
        UserPOJO receiveUser = userRepo.getByEmail(decodeAuth(authorizationHeader)[0]);
        UserPOJO offerUser;
        Offers offer;
        int offerId;
        try {
            offerId = Integer.parseInt(strOfferId);
        } catch (NumberFormatException nfe) {
            throw new NumberFormatException("Must enter a number!");
        }
        if (offerRepo.findById(offerId).isEmpty()) {
            throw new NullPointerException("offer with that Id does not exist!");
        }
        offer = offerRepo.getById(offerId);
        offerUser = offer.getOfferingUser();
        if (!offer.getCurrentState().equals(Offers.CurrentState.Pending)) {
            throw new IllegalArgumentException("That offer is not still pending!");
        }
        System.out.println(offer.getReceivingUser().getId());
        System.out.println(receiveUser.getId());
        if (offer.getReceivingUser().equals(receiveUser)) {
            System.err.println("user matches");
            if (!bll.userHasGames(receiveUser, offer.getRequestedVideoGames()) && !bll.userHasGames(offerUser, offer.getOfferedVideoGames())) { //check if the users have their games
                offer.setCurrentState(Offers.CurrentState.Rejected);
                throw new IllegalArgumentException("Not all users still have these games! setting offer to declined");
            } else {
                for (VideoGamePOJO game : offer.getOfferedVideoGames()) {//adding games to receiving user
                    System.out.println(game.getId());
                    game.setPreviousOwners(game.getPreviousOwners() + 1);
                    game.setGameOwner(receiveUser);
                    System.out.println("here in receive");
                }
                for (VideoGamePOJO game : offer.getRequestedVideoGames()) {//adding games to offering user
                    System.out.println(game.getId());
                    game.setPreviousOwners(game.getPreviousOwners() + 1);
                    game.setGameOwner(offerUser);
                    System.out.println("here in offer");
                }
            }
        }
        offer.setCurrentState(Offers.CurrentState.Accepted);
        userRepo.save(receiveUser);
        userRepo.save(offerUser);
        offerRepo.save(offer);
        bll.sendEmail("Your offer has been accepted!", offerUser.getEmail(), "Simon's API: Offer Update");
        bll.sendEmail("You accepted an offer!", receiveUser.getEmail(), "Simon's API: Accepted Offer");
        //add links
        for (Link link : generateUserLinks(receiveUser.getId())) {
            receiveUser.add(link); //puts all the generated links into the user
        }
        return receiveUser;
    }

    @PostMapping(path = "/userVer/declineOffer")
    public Offers declineOffer(@RequestHeader(value = "Authorization") String authorizationHeader, @RequestParam String strOfferId) {
        UserPOJO userPOJO = userRepo.getByEmail(decodeAuth(authorizationHeader)[0]);
        Offers offer;
        int offerId;
        try {
            offerId = Integer.parseInt(strOfferId);
        } catch (NumberFormatException nfe) {
            throw new NumberFormatException("Id must be an integer!");
        }
        if (offerRepo.findById(offerId).isEmpty()) {
            throw new NullPointerException("No offer with that id exists!");
        }
        offer = offerRepo.getById(offerId);
        if (!offer.getReceivingUser().equals(userPOJO)) {
            throw new SecurityException("You do not own that offer!");
        }
        if (offer.getCurrentState() != Offers.CurrentState.Pending) {
            throw new IllegalArgumentException("That offer has already been accepted or declined!");
        } else {
            offer.setCurrentState(Offers.CurrentState.Rejected);
        }
        for (Link link : generateOfferLinksWithGameList(offer.getId(), offer.getOfferingUser().getId(), offer.getReceivingUser().getId(), offer.getOfferedVideoGames(), offer.getRequestedVideoGames())) {
            offer.add(link); //puts all the generated links into the game
        }
        bll.sendEmail("Your offer has been declined!", offer.getOfferingUser().getEmail(), "Simon's API: Offer Update");
        bll.sendEmail("You declined an offer!", offer.getReceivingUser().getEmail(), "Simon's API: Declined offer");
        return offer;
    }

    private ArrayList<Link> generateOfferLinksWithGameList(int offerId, int offerUserId, int receiveUserId, List<VideoGamePOJO> offerList, List<VideoGamePOJO> receiveList) {
        ArrayList<Link> links = new ArrayList<>();
        links.add(Link.of((String.format("http://localhost:8080/offerses/%s", offerId)), "self"));
        links.add(Link.of((String.format("http://localhost:8080/offerses/%s", offerId)), "offer"));
        for (VideoGamePOJO offerGame : offerList) {
            links.add(Link.of((String.format("http://localhost:8080/videoGamePOJOes/%s", offerGame.getId())), "offeredVideoGames"));
        }
        for (VideoGamePOJO receiveGame : receiveList) {
            links.add(Link.of((String.format("http://localhost:8080/videoGamePOJOes/%s", receiveGame.getId())), "offeredVideoGames"));
        }
        links.add(Link.of((String.format("http://localhost:8080/userPOJOes/%s", offerUserId)), "offeringUser"));
        links.add(Link.of((String.format("http://localhost:8080/userPOJOes/%s", receiveUserId)), "receivingUser"));
        return links;
    }

    @GetMapping(path="/userVer/getOffersIn")
    public ArrayList<Link> getOffersIn(@RequestHeader(value = "Authorization") String authorizationHeader, @RequestParam/*(required = false)*/ String filter) {
//        if (filter.trim().equals("") || filter == null) filter = "all";
        ArrayList<Link> linksOut = new ArrayList<>();
        UserPOJO userPOJO = userRepo.getByEmail(decodeAuth(authorizationHeader)[0]);
        switch (filter.toLowerCase(Locale.ROOT).trim()) {
            case "pending":
                for (Offers offer : userPOJO.getOfferListOut()) {
                    if (offer.getCurrentState().equals(Offers.CurrentState.Pending)) {
                        linksOut.add(Link.of((String.format("http://localhost:8080/offerses/%s", offer.getId())), "pendingOffer"));
                    }
                }
                break;
            case "accepted":
                System.err.println("here");
                for (Offers offer : userPOJO.getOfferListOut()) {
                    if (offer.getCurrentState().equals(Offers.CurrentState.Accepted)) {
                        linksOut.add(Link.of((String.format("http://localhost:8080/offerses/%s", offer.getId())), "acceptedOffer"));
                    }
                }
                break;
            case "rejected":
                for (Offers offer : userPOJO.getOfferListOut()) {
                    if (offer.getCurrentState().equals(Offers.CurrentState.Rejected)) {
                        linksOut.add(Link.of((String.format("http://localhost:8080/offerses/%s", offer.getId())), "rejectedOffer"));
                    }
                }
                break;
            default:
                for (Offers offer : userPOJO.getOfferListOut()) {
                    if (offer.getCurrentState().equals(Offers.CurrentState.Pending)) {
                        linksOut.add(Link.of((String.format("http://localhost:8080/offerses/%s", offer.getId())), "pendingOffer"));
                    }else if(offer.getCurrentState().equals(Offers.CurrentState.Accepted)){
                        linksOut.add(Link.of((String.format("http://localhost:8080/offerses/%s", offer.getId())), "acceptedOffer"));
                    }else{
                        linksOut.add(Link.of((String.format("http://localhost:8080/offerses/%s", offer.getId())), "declinedOffer"));
                    }
                }
                break;
        }
        return removeDuplicates(linksOut);
    }

    @GetMapping(path="/userVer/getOffersOut")
    public ArrayList<Link> getOffersOut(@RequestHeader(value = "Authorization") String authorizationHeader, @RequestParam/*(required = false)*/ String filter) {
//        if (filter.trim().equals("") || filter == null) filter = "all";
        ArrayList<Link> linksOut = new ArrayList<>();
        UserPOJO userPOJO = userRepo.getByEmail(decodeAuth(authorizationHeader)[0]);
        switch (filter.toLowerCase(Locale.ROOT).trim()) {
            case "pending":
                for (Offers offer : userPOJO.getOfferListIn()) {
                    if (offer.getCurrentState().equals(Offers.CurrentState.Pending)) {
                        linksOut.add(Link.of((String.format("http://localhost:8080/offerses/%s", offer.getId())), "pendingOffer"));
                    }
                }
                break;
            case "accepted":
                System.err.println(filter);
                for (Offers offer : userPOJO.getOfferListIn()) {
                    if (offer.getCurrentState().equals(Offers.CurrentState.Accepted)) {
                        linksOut.add(Link.of((String.format("http://localhost:8080/offerses/%s", offer.getId())), "acceptedOffer"));
                    }
                }
                break;
            case "rejected":
                for (Offers offer : userPOJO.getOfferListIn()) {
                    if (offer.getCurrentState().equals(Offers.CurrentState.Rejected)) {
                        linksOut.add(Link.of((String.format("http://localhost:8080/offerses/%s", offer.getId())), "rejectedOffer"));
                    }
                }
                break;
            default:
                System.err.println(filter);
                for (Offers offer : userPOJO.getOfferListIn()) {
                    if (offer.getCurrentState().equals(Offers.CurrentState.Pending)) {
                        linksOut.add(Link.of((String.format("http://localhost:8080/offerses/%s", offer.getId())), "pendingOffer"));
                    }else if(offer.getCurrentState().equals(Offers.CurrentState.Accepted)){
                        linksOut.add(Link.of((String.format("http://localhost:8080/offerses/%s", offer.getId())), "acceptedOffer"));
                    }else{
                        linksOut.add(Link.of((String.format("http://localhost:8080/offerses/%s", offer.getId())), "declinedOffer"));
                    }
                }
                break;
        }
        return removeDuplicates(linksOut);
    }

    public <T> ArrayList<T> removeDuplicates(ArrayList<T> list)
    {

        // Create a new LinkedHashSet
        Set<T> set = new LinkedHashSet<>();

        // Add the elements to set
        set.addAll(list);

        // Clear the list
        list.clear();

        // add the elements of set
        // with no duplicates to the list
        list.addAll(set);

        // return the list
        return list;
    }
}
