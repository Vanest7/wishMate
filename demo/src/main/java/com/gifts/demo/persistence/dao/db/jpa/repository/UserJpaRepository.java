package com.gifts.demo.persistence.dao.db.jpa.repository;

import com.gifts.demo.persistence.dao.db.jpa.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserJpaRepository
        extends JpaRepository<UserEntity, Integer> {

    Optional<UserEntity> findByMail(String mail);

    @Query("""
            select u from UserEntity u
            where lower(u.userName) like lower(concat('%', :pattern, '%')) escape '\\'
               or lower(u.handle) like lower(concat('%', :pattern, '%')) escape '\\'
            order by
                case when lower(u.handle) = lower(:query) then 0 else 1 end,
                lower(u.userName),
                u.id
            """)
    List<UserEntity> search(
            @Param("query") String query,
            @Param("pattern") String pattern,
            org.springframework.data.domain.Pageable pageable
    );

    boolean existsByMail(String mail);

    boolean existsByHandleIgnoreCase(String handle);
}