package com.evaluation.backend.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "AUTHENTIFICATION")
public class Authentification implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_CONNECTION")
    private Long id;

    @NotNull
    @Column(name = "ROLE")
    private String role;

    @NotNull
    @Column(name = "LOGIN_CONNECTION")
    private String email;

    @Column(name = "PSEUDO_CONNECTION")
    private String pseudoConnection;

    @Column(name = "MOT_PASSE")
    private String motPasse;

    @ManyToOne
    @JoinColumn(name = "NO_ETUDIANT")
    private Etudiant etudiant;

    @ManyToOne
    @JoinColumn(name = "NO_ENSEIGNANT")
    private Enseignant enseignant;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role));
    }

    @Override
    public String getPassword() {
        return motPasse;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() { return true; }

    @Override
    public boolean isAccountNonLocked() { return true; }

    @Override
    public boolean isCredentialsNonExpired() { return true; }

    @Override
    public boolean isEnabled() { return true; }
}