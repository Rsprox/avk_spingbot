package ru.hse.avk_spingbot.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import ru.hse.avk_spingbot.entity.Announcement;

import java.util.List;
public interface AnnouncementRepository extends CrudRepository<Announcement, Long> {
    List<Announcement> findAllByIsActive(Boolean isActive);
}
