package ru.hse.avk_spingbot.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import ru.hse.avk_spingbot.entity.User;
public interface UserRepository extends CrudRepository<User, Long> {
}
