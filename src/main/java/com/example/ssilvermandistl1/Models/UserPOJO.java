package com.example.ssilvermandistl1.Models;

import com.example.ssilvermandistl1.Controllers.BLL;
import com.example.ssilvermandistl1.Repositories.VideoGameRepository;
import lombok.Data;
import net.minidev.json.annotate.JsonIgnore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.RepresentationModel;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

@Data
@Entity
public class UserPOJO extends RepresentationModel<UserPOJO> {


    @GeneratedValue(strategy = GenerationType.AUTO)
    @Id
    private int id;
    private String name;
    private String email;
    private String streetAddress;
    private String password;
    @JsonIgnore
    @OneToMany(mappedBy = "gameOwner")
    private List<VideoGamePOJO> gameList = new ArrayList<>();
    @JsonIgnore
    @OneToMany(mappedBy = "offeringUser")
    private List<Offers> offerListIn = new ArrayList<>();
    @JsonIgnore
    @OneToMany(mappedBy = "receivingUser")
    private List<Offers> offerListOut = new ArrayList<>();

    public void addGame(VideoGamePOJO game){
        this.gameList.add(game);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getStreetAddress() {
        return streetAddress;
    }

    public void setStreetAddress(String streetAddress) {
        this.streetAddress = streetAddress;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @JsonIgnore
    public List<VideoGamePOJO> getGameList() {
        return gameList;
    }

    public void setGameList(List<VideoGamePOJO> gameList) {
        this.gameList = gameList;
    }
    @JsonIgnore
    public List<Offers> getOfferListIn() {
        return offerListIn;
    }

    public void setOfferListIn(List<Offers> offerListIn) {
        this.offerListIn = offerListIn;
    }
    @JsonIgnore
    public List<Offers> getOfferListOut() {
        return offerListOut;
    }

    public void setOfferListOut(List<Offers> offerListOut) {
        this.offerListOut = offerListOut;
    }
}