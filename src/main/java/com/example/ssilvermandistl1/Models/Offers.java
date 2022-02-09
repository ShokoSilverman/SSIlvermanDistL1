package com.example.ssilvermandistl1.Models;

import com.example.ssilvermandistl1.Views.JSONViews;
import com.fasterxml.jackson.annotation.JsonIgnoreType;
import com.fasterxml.jackson.annotation.JsonView;
import lombok.Data;
import net.minidev.json.annotate.JsonIgnore;
import org.springframework.hateoas.RepresentationModel;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;


@Data
@Entity
public class Offers extends RepresentationModel<Offers> {
    public enum CurrentState{Pending, Accepted, Rejected}

    @JsonView(JSONViews.OfferView.class)
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;

    //*** User requesting for trade
    @JsonView(JSONViews.OfferView.class)
    @ManyToOne()
    @JsonIgnore
    private UserPOJO offeringUser;

    @JsonView(JSONViews.OfferView.class)
    @JsonIgnore
    @ManyToMany()
    @JoinTable(name = "offered_videoGames", joinColumns = @JoinColumn(name = "videoGame_id"),inverseJoinColumns = @JoinColumn(name = "offer_id"))
    private List<VideoGamePOJO> offeredVideoGames = new ArrayList<>();

    //*** User being asked for a trade
    @JsonView(JSONViews.OfferView.class)
    @ManyToOne()
    @JsonIgnore()
    private UserPOJO receivingUser;

    @JsonView(JSONViews.OfferView.class)
    @JsonIgnore
    @ManyToMany()
    @JoinTable(name = "requested_videoGames", joinColumns = @JoinColumn(name = "videoGame_id"),inverseJoinColumns = @JoinColumn(name = "offer_id"))
    private List<VideoGamePOJO> requestedVideoGames = new ArrayList<>();

    @JsonView(JSONViews.OfferView.class)
    @Column(nullable = false)
    private CurrentState currentState;


    @JsonView(JSONViews.OfferView.class)
    @Column
    private String dateOfferMade;

//    public void checkState(CurrentState currentState) {
//        if(currentState == CurrentState.Accepted) {
//            System.out.println("Hello");
//        }
//    }

    public Offers() {
    }

    public Offers(UserPOJO offeringUser, List<VideoGamePOJO> offeredVideoGames, UserPOJO receivingUser, List<VideoGamePOJO> requestedVideoGames) {
        this.offeringUser = offeringUser;
        this.offeredVideoGames = offeredVideoGames;
        this.receivingUser = receivingUser;
        this.requestedVideoGames = requestedVideoGames;
        this.currentState = CurrentState.Pending;
        this.dateOfferMade = DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm:ss").format(LocalDateTime.now());;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @JsonIgnore
    public UserPOJO getOfferingUser() {
        return offeringUser;
    }

    public void setOfferingUser(UserPOJO offeringUser) {
        this.offeringUser = offeringUser;
    }
    @JsonIgnore
    public List<VideoGamePOJO> getOfferedVideoGames() {
        return offeredVideoGames;
    }

    public void setOfferedVideoGames(List<VideoGamePOJO> offeredVideoGames) {
        this.offeredVideoGames = offeredVideoGames;
    }
    @JsonIgnore
    public UserPOJO getReceivingUser() {
        return receivingUser;
    }

    public void setReceivingUser(UserPOJO receivingUser) {
        this.receivingUser = receivingUser;
    }
    @JsonIgnore
    public List<VideoGamePOJO> getRequestedVideoGames() {
        return requestedVideoGames;
    }

    public void setRequestedVideoGames(List<VideoGamePOJO> requestedVideoGames) {
        this.requestedVideoGames = requestedVideoGames;
    }

    public CurrentState getCurrentState() {
        return currentState;
    }

    public void setCurrentState(CurrentState currentState) {
        this.currentState = currentState;
    }

    public String getDateOfferMade() {
        return dateOfferMade;
    }

    public void setDateOfferMade(String dateOfferMade) {
        this.dateOfferMade = dateOfferMade;
    }
}
