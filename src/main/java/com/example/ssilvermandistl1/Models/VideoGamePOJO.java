package com.example.ssilvermandistl1.Models;

import com.example.ssilvermandistl1.Repositories.UserRepository;
import com.example.ssilvermandistl1.Views.JSONViews;
import com.fasterxml.jackson.annotation.JsonIgnoreType;
import com.fasterxml.jackson.annotation.JsonView;
import lombok.Data;
import net.minidev.json.annotate.JsonIgnore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.RepresentationModel;

import javax.persistence.*;

@Data
@Entity
public class VideoGamePOJO extends RepresentationModel<UserPOJO> {

    public enum GameCondition{MINT, GOOD, FAIR, POOR}

    @JsonView(JSONViews.OfferView.class)
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Id
    private int id;
    @JsonView(JSONViews.OfferView.class)
    private String name;
    @JsonView(JSONViews.OfferView.class)
    private String publisher;
    @JsonView(JSONViews.OfferView.class)
    private int yearPublished;
    @JsonView(JSONViews.OfferView.class)
    private String systemUsed;
    @JsonView(JSONViews.OfferView.class)
    private GameCondition gameCondition;
    @JsonView(JSONViews.OfferView.class)
    private int previousOwners;
    @ManyToOne
    @JsonIgnore
    private UserPOJO gameOwner;

    public VideoGamePOJO(){}

}
