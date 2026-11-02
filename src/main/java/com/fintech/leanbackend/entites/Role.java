package com.fintech.leanbackend.entites;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "roles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Role extends Base{
    private String name;

    @ManyToMany(mappedBy = "roles")
    private List<Users> users = new ArrayList<>();
    public static Role of (String name){
        if(name == null || name.isBlank()){
            throw new IllegalArgumentException("Role name must not be blank");
        }
        Role r = new Role();
        r.name = name.toUpperCase();
        return  r;
    }
}
