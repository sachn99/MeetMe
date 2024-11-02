
package com.meetme.entities;


import lombok.Data;
import org.springframework.data.annotation.Id;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
public class Meeting {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private LocalDateTime startTime;
    private LocalDateTime endTime;

    @ManyToMany
    private List<User> participants = new ArrayList<>();

}
