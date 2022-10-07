package ru.hse.avk_spingbot.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import ru.hse.avk_spingbot.entity.Joke;

public interface JokeRepository extends CrudRepository<Joke, Long> {

    @Query(nativeQuery = true, value = "select joke from jokes order by random() limit 1")
    String findRandomOne();

}
