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
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

@Data
@Entity
public class UserPOJO extends RepresentationModel<UserPOJO> implements UserDetails {


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
    @OneToMany(mappedBy = "gameOwner", fetch = FetchType.EAGER)
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

    public void removeGame(VideoGamePOJO game){
        this.gameList.remove(game);
    }

    public void addOfferIn(Offers offer){
        this.offerListIn.add(offer);
    }

    public void addOfferOut(Offers offer){
        this.offerListOut.add(offer);
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        List<GrantedAuthority> grantedAuthorityList = new ArrayList<>();
        grantedAuthorityList.add(new SimpleGrantedAuthority("USER"));
        return grantedAuthorityList;

    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String getUsername() {
        return this.email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}