package it.hijack.scheduler.jfx.agenda;

import it.hijack.scheduler.Activity;
import it.hijack.scheduler.Assignment;
import it.hijack.scheduler.Worker;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.Cursor;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import com.sun.javafx.scene.control.skin.SkinBase;

public class MyAgendaSkin extends SkinBase<MyAgenda, MyAgendaBehavior> {

	private final int gridColumns = 7;
	private final int gridRows = 12;
	private final int cellWidth = 100;
	private final int cellHeight = 30;

	private MyAgenda control;
	private boolean isDirty;

	private Pane dragPane;
	private Rectangle dashedRectangle;
	private double dashedRectangleStartX;
	private double dashedRectangleStartY;

	public MyAgendaSkin(MyAgenda control) {
		super(control, new MyAgendaBehavior(control));
		this.control = control;
		drawControl();
		this.isDirty = false;
	}

	@Override
	protected void layoutChildren() {
		System.out.println("layoutChildren()");
		if (!isDirty)
			return;

		isDirty = false;

		super.layoutChildren();
	}
	
	public void refresh() {
		reset();
		construct();
	}
	
	private void reset() {
		dragPane.getChildren().removeAll(stacks);
		stacks.clear();
	}

	private void construct() {
		Set<Assignment> assignments = getSkinnable().getTimetable().getAssignments();
		for(Assignment assignment : assignments) {
			drawRectangle(assignment);
		}
	}
	
	List<Region> stacks = new ArrayList<>();
	
	private void drawRectangle(Assignment assignment) {
		Rectangle rectangle = new Rectangle();
		rectangle.setWidth(cellWidth-2);
		
		int totalHeight = assignment.getTotalHours() * cellHeight - 2;
		rectangle.setHeight(totalHeight);
		
		rectangle.setFill(assignment.getWorker().getColor());
		rectangle.setArcHeight(6);
		rectangle.setArcWidth(6);
		rectangle.setStroke(null);

		StackPane stack = new StackPane();
		stack.setLayoutX(0 + 1);
		int startingHourIndex = assignment.getStartHour() - 8;
		stack.setLayoutY(startingHourIndex * cellHeight + 1);
		
		Label lbl = new Label(assignment.getActivity().getName() + " - " + assignment.getWorker().getName());
		stack.getChildren().add(rectangle);
		stack.getChildren().add(lbl);
		dragPane.getChildren().add(stack);
		
		stacks.add(stack);
	}
	
	private void drawControl() {
		System.out.println("drawControl()");

		Pane container = new Pane();
		dragPane = new Pane();

		BorderPane borderPane = new BorderPane();

		container.prefWidthProperty().bind(widthProperty());
		container.prefHeightProperty().bind(heightProperty());

		Region hoursColumn = createHoursColumn();
		Region weekHeader = createWeekHeader(hoursColumn);
		Region grid = createGrid(gridColumns, gridRows);
		grid.setStyle("-fx-border-color: GRAY");
		dragPane.getChildren().add(grid);

		borderPane.setTop(weekHeader);
		borderPane.setLeft(hoursColumn);
		borderPane.setCenter(dragPane);

		construct();

		container.getChildren().addAll(borderPane);

		addSelectionCapability();

		getChildren().clear();
		getChildren().addAll(container);
	}

	private Region createGrid(int columns, int rows) {
		GridPane grid = new GridPane();
		grid.setStyle("-fx-background-color: #DDD");

		for (int r = 0; r < rows; r++) {
			grid.getRowConstraints().add(new RowConstraints(cellHeight));
		}
		for (int c = 0; c < columns; c++) {
			grid.getColumnConstraints().add(new ColumnConstraints(cellWidth));
		}

		for (int c = 0; c < columns; c++) {
			for (int r = 0; r < rows; r++) {
				Rectangle cell = new Rectangle(cellWidth - 1, cellHeight - 1, Color.ALICEBLUE);
				grid.add(cell, c, r);
				GridPane.setHalignment(cell, HPos.RIGHT);
				GridPane.setValignment(cell, VPos.BOTTOM);
			}
		}

		grid.setGridLinesVisible(false);
		return grid;
	}

	private void addSelectionCapability() {
		dragPane.setOnMousePressed(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent evt) {
				// System.out.println("mouse pressed");

				double verticalTicks = evt.getY() / cellHeight;
				double flooredVerticalTicks = Math.floor(verticalTicks);

				double horizontalTicks = evt.getX() / cellWidth;
				double flooredHorizontalTicks = Math.floor(horizontalTicks);

				dashedRectangleStartX = flooredHorizontalTicks * cellWidth;
				dashedRectangleStartY = flooredVerticalTicks * cellHeight;

				System.out.println("ORIGIN: " + dashedRectangleStartY);

				dashedRectangle = new Rectangle(dashedRectangleStartX + 1, dashedRectangleStartY + 1, cellWidth - 2,
						cellHeight - 2);
				dashedRectangle.setFill(Color.ORANGE);
				dashedRectangle.setArcHeight(6);
				dashedRectangle.setArcWidth(6);
				dashedRectangle.setStroke(null);
				dragPane.getChildren().add(dashedRectangle);
			}
		});

		dragPane.setOnMouseDragged(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent evt) {
				// System.out.println("mouse dragged");
				// setCursor(Cursor.V_RESIZE);

				System.out.println(evt.getX() + ", " + evt.getY());
				if (evt.getY() <= dashedRectangleStartY) {
					System.out.println("PRIMA");
					return;
				}

				if (evt.getY() >= (cellHeight * gridRows)) {
					System.out.println("Oltre");
					return;
				}

				double deltaY = evt.getY() - dashedRectangleStartY;
				double ticks = deltaY / cellHeight;
				double ceiledTicks = Math.ceil(ticks);
				double rectangleHeight = ceiledTicks * cellHeight - 2;
				dashedRectangle.setHeight(rectangleHeight);
			}
		});

		dragPane.setOnMouseReleased(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				// System.out.println("mouse released");
				
				int startingHour = (int) Math.ceil(dashedRectangleStartY / cellHeight) + 8;
				int cells = (int) Math.ceil(dashedRectangle.getHeight() / cellHeight);
				
				setCursor(Cursor.DEFAULT);
				dragPane.getChildren().remove(dashedRectangle);
				dashedRectangle = null;
				event.consume();
				
				getSkinnable().getTimetable().assign(new Activity("test")).to(new Worker("john")).from(startingHour).to(startingHour + cells);
				refresh();
			}
		});
	}

	private Region createWeekHeader(Region hoursColumn) {
		HBox header = new HBox(0);
		Rectangle rect = new Rectangle();
		rect.widthProperty().bind(hoursColumn.widthProperty());

		header.getChildren().add(rect);

		for (WeekDays day : WeekDays.values()) {
			Label label = createWeekDayLabel(day.name());
			header.getChildren().add(label);
		}

		return header;
	}

	private Label createWeekDayLabel(String text) {
		Label label = new Label(text);
		label.setMinWidth(cellWidth);
		return label;
	}

	private Region createHoursColumn() {
		VBox hours = new VBox();
		for (int h = 0; h < gridRows; h++) {
			Label label = createHourLabel(h + 8);
			hours.getChildren().add(label);
		}
		hours.setSpacing(0);
		return hours;
	}

	private Label createHourLabel(int hour) {
		String text = new DecimalFormat("00").format(hour);
		Label label = new Label(text + ":00");
		label.setMinHeight(cellHeight);
		return label;
	}

	@Override
	public MyAgenda getSkinnable() {
		return control;
	}

}
