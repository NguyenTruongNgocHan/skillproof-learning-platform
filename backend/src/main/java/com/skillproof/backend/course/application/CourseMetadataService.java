package com.skillproof.backend.course.application;

import com.skillproof.backend.common.exception.*;
import com.skillproof.backend.course.contract.CourseStructureQuery;
import com.skillproof.backend.course.infrastructure.*;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CourseMetadataService {
  public record Metadata(
      String domainTag, String difficulty, List<String> skills, String prerequisiteText) {}

  private final CourseContextRepository versions;
  private final CourseStructureQuery structure;

  public CourseMetadataService(CourseContextRepository versions, CourseStructureQuery structure) {
    this.versions = versions;
    this.structure = structure;
  }

  private CourseVersionEntity version(UUID id) {
    return versions
        .findById(id)
        .orElseThrow(() -> new NotFoundException("VERSION_NOT_FOUND", "Version not found"));
  }

  private Metadata view(CourseVersionEntity v) {
    return new Metadata(
        v.getDomainTag(), v.getDifficulty(), v.getSkills(), v.getPrerequisiteText());
  }

  @Transactional(readOnly = true)
  public Metadata get(UUID actor, UUID id) {
    structure.requireVersionAuthor(actor, id);
    return view(version(id));
  }

  @Transactional
  public Metadata save(UUID actor, UUID id, Metadata input) {
    structure.requireDraftAuthor(actor, id);
    if (input.domainTag() == null
        || !input.domainTag().matches("[a-z0-9]+(?:-[a-z0-9]+)*")
        || input.domainTag().length() > 80
        || input.difficulty() == null
        || !Set.of("UNSPECIFIED", "BEGINNER", "INTERMEDIATE", "ADVANCED")
            .contains(input.difficulty())
        || input.skills() == null
        || input.skills().size() > 20
        || input.skills().stream().anyMatch(x -> x == null || x.isBlank() || x.length() > 100)
        || input.prerequisiteText() == null
        || input.prerequisiteText().length() > 1000)
      throw new BadRequestException(
          "COURSE_METADATA_INVALID",
          "Provide a domain tag, valid difficulty, up to 20 skills and prerequisite text up to 1000"
              + " characters");
    var skills = new LinkedHashMap<String, String>();
    input.skills().forEach(x -> skills.putIfAbsent(x.trim().toLowerCase(Locale.ROOT), x.trim()));
    var v = version(id);
    v.configureMetadata(
        input.domainTag(),
        input.difficulty(),
        new ArrayList<>(skills.values()),
        input.prerequisiteText().trim());
    return view(v);
  }
}
