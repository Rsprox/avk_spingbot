package ru.hse.avk_spingbot.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.Id;

@Getter
@Setter
@Entity(name = "bash_quots")
public class BashQuote {
    @Id
    private Long quote_id;

    private String t_quote_text;
}
