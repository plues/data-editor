package de.hhu.stups.plues.dataeditor.ui.components.dataedits;

import de.hhu.stups.plues.dataeditor.ui.components.LabeledTextField;
import de.hhu.stups.plues.dataeditor.ui.database.DataService;
import de.hhu.stups.plues.dataeditor.ui.database.events.DataChangeEvent;
import de.hhu.stups.plues.dataeditor.ui.database.events.DataChangeType;
import de.hhu.stups.plues.dataeditor.ui.entities.CourseDegree;
import de.hhu.stups.plues.dataeditor.ui.entities.CourseKzfa;
import de.hhu.stups.plues.dataeditor.ui.entities.CourseWrapper;
import de.hhu.stups.plues.dataeditor.ui.entities.EntityType;
import de.hhu.stups.plues.dataeditor.ui.entities.EntityWrapper;
import de.hhu.stups.plues.dataeditor.ui.layout.Inflater;
import javafx.beans.InvalidationListener;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.RadioButton;
import javafx.scene.input.MouseButton;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.GridPane;
import javafx.util.StringConverter;
import org.fxmisc.easybind.EasyBind;

import java.net.URL;
import java.util.ResourceBundle;

public class CourseEdit extends GridPane implements Initializable {

  private final CourseWrapper courseWrapper;
  private final DataService dataService;
  private final BooleanProperty dataChangedProperty;
  private final EntityListViewContextMenu entityListViewContextMenu;

  private ResourceBundle resources;

  @FXML
  @SuppressWarnings("unused")
  private ComboBox<CourseDegree> cbCourseDegree;
  @FXML
  @SuppressWarnings("unused")
  private LabeledTextField txtFullName;
  @FXML
  @SuppressWarnings("unused")
  private LabeledTextField txtShortName;
  @FXML
  @SuppressWarnings("unused")
  private LabeledTextField txtPVersion;
  @FXML
  @SuppressWarnings("unused")
  private LabeledTextField txtCreditPoints;
  @FXML
  @SuppressWarnings("unused")
  private RadioButton rbMajorCourse;
  @FXML
  @SuppressWarnings("unused")
  private RadioButton rbMinorCourse;
  @FXML
  @SuppressWarnings("unused")
  private Button btPersistChanges;
  @FXML
  @SuppressWarnings("unused")
  private Label lbMajorsOrMinors;
  @FXML
  @SuppressWarnings("unused")
  private ListView<CourseWrapper> listViewMajorsOrMinors;

  /**
   * Inject the {@link DataService}.
   */
  CourseEdit(final Inflater inflater,
             final DataService dataService,
             final CourseWrapper courseWrapper) {
    this.dataService = dataService;
    this.courseWrapper = courseWrapper;
    this.dataChangedProperty = new SimpleBooleanProperty(false);
    this.entityListViewContextMenu = new EntityListViewContextMenu();
    inflater.inflate("components/dataedits/course_edit", this, this, "course_edit");
  }

  @Override
  public void initialize(final URL location, final ResourceBundle resources) {
    this.resources = resources;
    btPersistChanges.disableProperty().bind(dataChangedProperty.not());
    initializeCbDegree();
    initializeInputFields();
    setDataListener();
    loadCourseData();
    dataService.dataChangeEventSource().subscribe(this::updateData);

    setListViewContextMenu();
    setListViewDragListeners();
  }

  private void updateData(final DataChangeEvent dataChangeEvent) {
    final EntityWrapper changedEntity = dataChangeEvent.getChangedEntity();
    if (dataChangeEvent.getDataChangeType().equals(DataChangeType.DELETE_ENTITY)
        && changedEntity.getEntityType().equals(EntityType.COURSE)) {
      final CourseWrapper changedCourseWrapper = (CourseWrapper) changedEntity;
      listViewMajorsOrMinors.getItems().remove(changedCourseWrapper);
      if (rbMajorCourse.isSelected()) {
        courseWrapper.minorCourseWrapperProperty().get().remove(changedCourseWrapper);
      } else {
        courseWrapper.majorCourseWrapperProperty().get().remove(changedCourseWrapper);
      }
    }
  }

  private void setListViewContextMenu() {
    this.entityListViewContextMenu.setParent(listViewMajorsOrMinors);

    listViewMajorsOrMinors.getItems().addListener((InvalidationListener) observable ->
          dataChangedProperty.set(true));
    listViewMajorsOrMinors.setOnMouseClicked(event -> {
      entityListViewContextMenu.hide();
      final CourseWrapper selectedItem =
            listViewMajorsOrMinors.getSelectionModel().getSelectedItem();
      if (selectedItem != null && MouseButton.SECONDARY.equals(event.getButton())) {
        entityListViewContextMenu.show(listViewMajorsOrMinors,
              event.getScreenX(), event.getScreenY());
      }
    });
  }

  private void setListViewDragListeners() {
    dataService.draggedEntityProperty().addListener((observable, oldValue, newValue) ->
        listViewMajorsOrMinors.requestFocus());
    listViewMajorsOrMinors.setOnDragOver(event -> {
      event.acceptTransferModes(TransferMode.COPY);
      event.consume();
    });
    listViewMajorsOrMinors.setOnDragDropped(event -> {
      event.setDropCompleted(true);
      final EntityWrapper draggedWrapper = dataService.draggedEntityProperty().get();
      if (EntityType.COURSE.equals(draggedWrapper.getEntityType())
          && (((CourseWrapper) draggedWrapper).getCourse().isMinor() == rbMajorCourse.isSelected())
          && !listViewMajorsOrMinors.getItems().contains(draggedWrapper)) {
        listViewMajorsOrMinors.getItems().add(((CourseWrapper) draggedWrapper));
        dataChangedProperty.set(true);
      }
      event.consume();
    });
  }

  private void updateDataChanged() {
    EasyBind.subscribe(txtFullName.textProperty(), s -> dataChangedProperty.set(true));
    EasyBind.subscribe(txtShortName.textProperty(), s -> dataChangedProperty.set(true));
    EasyBind.subscribe(txtPVersion.textProperty(), s -> dataChangedProperty.set(true));
    EasyBind.subscribe(txtCreditPoints.textProperty(), s -> dataChangedProperty.set(true));
    EasyBind.subscribe(listViewMajorsOrMinors.itemsProperty(), s -> dataChangedProperty.set(true));
    EasyBind.subscribe(rbMajorCourse.selectedProperty(), s -> dataChangedProperty.set(true));
    EasyBind.subscribe(rbMinorCourse.selectedProperty(), s -> dataChangedProperty.set(true));
    EasyBind.subscribe(cbCourseDegree.getSelectionModel().selectedIndexProperty(),
        s -> dataChangedProperty.set(true));
  }

  /**
   * Update data if the wrapper has changed.
   */
  private void setDataListener() {
    EasyBind.subscribe(courseWrapper.longNameProperty(), s -> setFullName());
    EasyBind.subscribe(courseWrapper.shortNameProperty(), s -> setShortName());
    EasyBind.subscribe(courseWrapper.creditPointsProperty(), number -> setCreditPoints());
    EasyBind.subscribe(courseWrapper.poProperty(), number -> setPversion());
    EasyBind.subscribe(courseWrapper.courseProperty(), course -> selectMajorOrMinor());
    EasyBind.subscribe(courseWrapper.degreeProperty(), course -> selectCourseDegree());
    EasyBind.subscribe(rbMajorCourse.selectedProperty(), aBoolean -> loadMajorsOrMinors());
    EasyBind.subscribe(rbMinorCourse.selectedProperty(), aBoolean -> loadMajorsOrMinors());
    updateDataChanged();
  }

  private void initializeInputFields() {
    lbMajorsOrMinors.textProperty().bind(Bindings.when(rbMajorCourse.selectedProperty())
          .then(resources.getString("minors")).otherwise(resources.getString("majors")));
    txtFullName.setLabelText(resources.getString("course"));
    txtShortName.setLabelText(resources.getString("stg"));
    txtCreditPoints.setLabelText(resources.getString("credits"));
    txtPVersion.setLabelText(resources.getString("pversion"));
    listViewMajorsOrMinors.setCellFactory(
        param -> new ListCell<CourseWrapper>() {
          @Override
          protected void updateItem(final CourseWrapper item, final boolean empty) {
            super.updateItem(item, empty);
            if (item != null && !empty) {
              setText(item.toString());
            } else {
              setText("");
              setGraphic(null);
            }
          }
        });
  }

  private void selectCourseDegree() {
    cbCourseDegree.getSelectionModel().select(courseWrapper.getDegree());
  }

  private void loadMajorsOrMinors() {
    if (courseWrapper == null) {
      return;
    }
    listViewMajorsOrMinors.getItems().clear();
    if (rbMajorCourse.isSelected()) {
      listViewMajorsOrMinors.getItems()
          .addAll(courseWrapper.getMinorCourseWrappers());
    } else {
      listViewMajorsOrMinors.getItems()
          .addAll(courseWrapper.getMajorCourseWrappers());
    }
  }

  private void loadCourseData() {
    setFullName();
    setShortName();
    setCreditPoints();
    setPversion();
    selectMajorOrMinor();
    dataChangedProperty.set(false);
  }

  private void selectMajorOrMinor() {
    if (courseWrapper.getCourse().isMajor()) {
      rbMajorCourse.setSelected(true);
    } else {
      rbMinorCourse.setSelected(true);
    }
  }

  private void setPversion() {
    txtPVersion.setText(String.valueOf(courseWrapper.poProperty().get()));

  }

  private void setCreditPoints() {
    txtCreditPoints.setText(String.valueOf(courseWrapper.creditPointsProperty().get()));
  }

  private void setShortName() {
    txtShortName.setText(courseWrapper.getShortName());
  }

  private void setFullName() {
    txtFullName.setText(courseWrapper.getLongName());
  }



  private String createCourseKey(final CourseWrapper courseWrapper,
                                 final LabeledTextField txtShortName) {
    return courseWrapper.getDegree().toString() + "-"
        + txtShortName.textProperty().getValue().toUpperCase() + "-"
        + CourseKzfa.toString(courseWrapper.getKzfa()) + "-"
        + courseWrapper.getPo();
  }

  private void initializeCbDegree() {
    cbCourseDegree.setConverter(new StringConverter<CourseDegree>() {
      @Override
      public String toString(final CourseDegree courseDegree) {
        return resources.getString(courseDegree.toString().toLowerCase());
      }

      @Override
      public CourseDegree fromString(final String string) {
        return null;
      }
    });
    cbCourseDegree.getSelectionModel().selectFirst();
  }

  /**
   * Push the {@link #courseWrapper} to the {@link #dataService} and set
   * {@link #dataChangedProperty} to false.
   */
  @FXML
  @SuppressWarnings("unused")
  public void persistChanges() {
    if (txtShortName.textProperty().getValue() == null
          || txtFullName.textProperty().get() == null) {
      new Alert(Alert.AlertType.ERROR,
            resources.getString("nameError"), ButtonType.OK).showAndWait();
      return;
    }
    if (cbCourseDegree.getValue() == null) {
      new Alert(Alert.AlertType.ERROR,
            resources.getString("degreeError"), ButtonType.OK).showAndWait();
      return;
    }
    int creditPoints;
    try {
      creditPoints = Integer.parseInt(
            txtCreditPoints.textProperty().getValue());
    } catch (NumberFormatException exception) {
      new Alert(Alert.AlertType.ERROR,
            resources.getString("creditsError"), ButtonType.OK).showAndWait();
      return;
    }
    try {
      courseWrapper.setPo(Integer.parseInt(txtPVersion.textProperty().getValue()));
    } catch (NumberFormatException exception) {
      new Alert(Alert.AlertType.ERROR,
            resources.getString("poError"), ButtonType.OK).showAndWait();
      return;
    }
    courseWrapper.setCreditPoints(creditPoints);
    courseWrapper.setDegree(cbCourseDegree.getValue());
    courseWrapper.setLongName(txtFullName.textProperty().getValue());
    courseWrapper.setShortName(txtShortName.textProperty().getValue());
    courseWrapper.getMajorCourseWrappers().clear();
    courseWrapper.getMinorCourseWrappers().clear();
    if (rbMajorCourse.isSelected()) {
      courseWrapper.setKzfa(CourseKzfa.getKzfaFromString("H"));
      listViewMajorsOrMinors.getItems().forEach(item -> {
        courseWrapper.getMinorCourseWrappers().add(item);
        item.getMinorCourseWrappers().add(courseWrapper);
      });
    } else {
      courseWrapper.setKzfa(CourseKzfa.getKzfaFromString("N"));
      listViewMajorsOrMinors.getItems().forEach(item -> {
        courseWrapper.getMajorCourseWrappers().add(item);
        item.getMajorCourseWrappers().add(courseWrapper);
      });
    }

    courseWrapper.setKey(createCourseKey(courseWrapper, txtShortName));

    dataService.dataChangeEventSource().push(
          new DataChangeEvent(DataChangeType.STORE_ENTITY, courseWrapper));

    dataChangedProperty.set(false);
  }
}
