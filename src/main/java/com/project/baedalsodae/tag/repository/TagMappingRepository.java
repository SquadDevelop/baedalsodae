package com.project.baedalsodae.tag.repository;

import com.project.baedalsodae.tag.entity.TagMapping;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TagMappingRepository extends JpaRepository<TagMapping, UUID> {}
