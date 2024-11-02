
package com.meetme.entities;

import jakarta.persistence.*;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Entity
@Data
public class Calendar {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToMany(mappedBy = "calendar", cascade = CascadeType.ALL)
    private List<Meeting> meetings = new ArrayList<>();

}
