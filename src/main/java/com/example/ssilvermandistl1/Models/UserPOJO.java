package com.example.ssilvermandistl1.Models;

import com.example.ssilvermandistl1.Controllers.BLL;
import com.example.ssilvermandistl1.Repositories.VideoGameRepository;
import com.example.ssilvermandistl1.Views.JSONViews;
import com.fasterxml.jackson.annotation.JsonIgnoreType;
import com.fasterxml.jackson.annotation.JsonView;
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


    @JsonView(JSONViews.OfferView.class)
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Id
    private int id;
    @JsonView(JSONViews.OfferView.class)
    private String name;
    @JsonView(JSONViews.OfferView.class)
    private String email;
    @JsonView(JSONViews.OfferView.class)
    private String streetAddress;
    @JsonView(JSONViews.OfferView.class)
    private String password;
    @JsonView(JSONViews.OfferView.class)
    @JsonIgnore
    @OneToMany(mappedBy = "gameOwner")
    private List<VideoGamePOJO> gameList = new ArrayList<>();
//    @JsonView(JSONViews.OfferView.class)
    @JsonIgnore
    @OneToMany(mappedBy = "offeringUser")
    private List<Offers> offerListIn = new ArrayList<>();
//    @JsonView(JSONViews.OfferView.class)
    @JsonIgnore
    @OneToMany(mappedBy = "receivingUser")
    private List<Offers> offerListOut = new ArrayList<>();

    public void addGame(VideoGamePOJO game){
        this.gameList.add(game);
    }

    public void addOfferIn(Offers offer){
        this.offerListIn.add(offer);
    }

    public void addOfferOut(Offers offer){
        this.offerListOut.add(offer);
    }
}