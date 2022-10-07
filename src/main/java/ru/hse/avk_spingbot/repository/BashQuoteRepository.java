package ru.hse.avk_spingbot.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import ru.hse.avk_spingbot.entity.BashQuote;

public interface BashQuoteRepository extends CrudRepository<BashQuote, Long> {

    @Query(nativeQuery = true, value = "select t_quote_text from bash_quots order by random() limit 1")
    String findRandomOne();
}
