package ru.hse.avk_spingbot.repository;

import org.springframework.data.repository.CrudRepository;
import ru.hse.avk_spingbot.entity.User;
public interface UserRepository extends CrudRepository<User, Long> {
}
