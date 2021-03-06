package de.hhu.stups.plues.dataeditor.ui.entities;

import org.springframework.lang.Nullable;

import java.io.Serializable;
import java.util.Objects;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

@SuppressWarnings("unused")
@Entity
@Table(name = "levels")
public class Level extends ModelEntity implements Serializable {

  private static final long serialVersionUID = -2571508967373463723L;

  @Id
  @Column(name = "id")
  private int id;

  private String art;

  private String name;

  private String tm;

  private Integer max = 0;

  private Integer min = 0;

  @Column(name = "min_credit_points")
  private Integer minCreditPoints;

  @Column(name = "max_credit_points")
  private Integer maxCreditPoints;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "parent_id")
  private Level parent;

  @OneToMany(mappedBy = "parent", fetch = FetchType.LAZY)
  private Set<Level> children;

  @OneToMany(mappedBy = "level", fetch = FetchType.LAZY)
  private Set<ModuleLevel> moduleLevels;

  @OneToMany(fetch = FetchType.LAZY)
  @JoinTable(name = "module_levels",
        joinColumns = @JoinColumn(name = "level_id", referencedColumnName = "id"),
        inverseJoinColumns = @JoinColumn(name = "module_id", referencedColumnName = "id"))
  private Set<Module> modules;

  @Nullable
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinTable(name = "course_levels",
        inverseJoinColumns = @JoinColumn(name = "course_id",
              referencedColumnName = "id"),
        joinColumns = @JoinColumn(name = "level_id",
              referencedColumnName = "id"))
  private Course course;

  public Level() {
    // Default constructor is required by hibernate
  }

  /**
   * Get the minimum number of modules required for level. Returns -1 if the level
   * is not cardinality based.
   * @return int the minimum number of modules for level or -1.
   */
  public int getMin() {
    if (this.min == null) {
      return -1;
    }
    return this.min;
  }

  /**
   * Get the maximum number of modules required for level. Returns -1 if the level
   * is not cardinality based.
   * @return int the maximum number of modules for level or -1.
   */
  public int getMax() {
    if (this.max == null) {
      return -1;
    }
    return this.max;
  }

  /**
   * Get the maximum number of credit points required for level. Returns -1 if the level
   * is not credit point based.
   * @return int the maximum number of modules for level or -1.
   */
  public int getMaxCreditPoints() {
    if (this.maxCreditPoints == null) {
      return -1;
    }
    return this.maxCreditPoints;
  }

  public void setMaxCreditPoints(int max) {
    this.maxCreditPoints = max;
  }

  /**
   * Get the minimum number of credit points required for level. Returns -1 if the level
   * is not credit point based.
   * @return int the minimum number of modules for level or -1.
   */
  public int getMinCreditPoints() {
    if (this.minCreditPoints == null) {
      return -1;
    }
    return this.minCreditPoints;
  }

  public void setMinCreditPoints(int min) {
    this.minCreditPoints = min;
  }

  public int getId() {
    return id;
  }

  public void setId(final int id) {
    this.id = id;
  }

  public String getArt() {
    return art;
  }

  public void setArt(final String art) {
    this.art = art;
  }

  public String getName() {
    return name;
  }

  public void setName(final String name) {
    this.name = name;
  }

  public String getTm() {
    return tm;
  }

  public void setTm(final String tm) {
    this.tm = tm;
  }

  public Level getParent() {
    return parent;
  }

  public void setParent(final Level parent) {
    this.parent = parent;
  }

  public Course getCourse() {
    return course;
  }

  public void setCourse(final Course course) {
    this.course = course;
  }

  public Set<Level> getChildren() {
    return children;
  }

  public void setChildren(Set<Level> children) {
    this.children = children;
  }

  public Set<ModuleLevel> getModuleLevels() {
    return moduleLevels;
  }

  public Set<Module> getModules() {
    return modules;
  }

  public void setModules(Set<Module> modules) {
    this.modules = modules;
  }

  @Override
  public boolean equals(final Object other) {
    if (this == other) {
      return true;
    }
    if (other == null || getClass() != other.getClass()) {
      return false;
    }
    final Level level = (Level) other;
    return Objects.equals(id, level.id)
          && Objects.equals(art, level.art)
          && Objects.equals(name, level.name)
          && Objects.equals(tm, level.tm)
          && Objects.equals(max, level.max)
          && Objects.equals(min, level.min)
          && Objects.equals(minCreditPoints, level.minCreditPoints)
          && Objects.equals(maxCreditPoints, level.maxCreditPoints)
          && super.equals(level);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, art, name, tm, max, min, minCreditPoints,
          maxCreditPoints, super.hashCode());
  }
}