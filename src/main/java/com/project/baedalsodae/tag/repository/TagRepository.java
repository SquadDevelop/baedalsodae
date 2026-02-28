package com.project.baedalsodae.tag.repository;

import com.project.baedalsodae.tag.entity.Tag;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TagRepository extends JpaRepository<Tag, UUID> {

  Optional<Tag> findByName(String name);

  List<Tag> findAllByNameIn(Collection<String> names);
}
