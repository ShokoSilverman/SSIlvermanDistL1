package com.example.ssilvermandistl1.Models;

import com.example.ssilvermandistl1.Repositories.UserRepository;
import lombok.Data;
import net.minidev.json.annotate.JsonIgnore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.RepresentationModel;

import javax.persistence.*;

@Data
@Entity
public class VideoGamePOJO extends RepresentationModel<UserPOJO> {

    public enum GameCondition{MINT, GOOD, FAIR, POOR}

    @GeneratedValue(strategy = GenerationType.AUTO)
    @Id
    private int id;
    private String name;
    private String publisher;
    private int yearPublished;
    private String systemUsed;
    private GameCondition gameCondition;
    private int previousOwners;
    @ManyToOne
    @JsonIgnore
    private UserPOJO gameOwner;

    public VideoGamePOJO(){}

}
