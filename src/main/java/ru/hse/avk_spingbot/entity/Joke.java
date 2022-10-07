package ru.hse.avk_spingbot.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Getter
@Setter
@Entity(name = "jokes")
public class Joke {
    @Id
    private Long id;

    private String joke;


}
