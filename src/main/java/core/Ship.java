package core;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import ui.CoordToStringAttributeConverter;

/** Makes a ship object for the {@link Grid} class */
@Entity
public class Ship {

    /** List of coords for ship's position */
    @Transient private List<Coord> coordList;

    /** Identifier for Ship */
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    /** The starting coordinate of the ship */
    // @OneToOne
    // @JoinColumn(name = "startcoordinate_id")
    @Convert(converter = CoordToStringAttributeConverter.class)
    private Coord startcoordinate;

    /** the size of the ships */
    @Column private int size;

    /** the directions its facing like horizontal and vertical */
    @Column private Direction direction;

    /** Name of the ship e.g. Battleship */
    @Column(nullable = false)
    private String name;

    /** Enum to represent ship placement direction on the grid */
    public enum Direction {
        HORIZONTAL,
        VERTICAL
    }

    public Long getId() {
        return id;
    }

    public Coord getStartcoordinate() {
        return startcoordinate;
    }

    /** Length of the Ship object */

    /** The coordinates of the whole ship */

    /**
     * Constructs a new ship with the following:
     *
     * @param coordinate
     * @param size
     * @param direction
     * @param name
     */
    public Ship(
            final Coord coordinate, final int size, final Direction direction, final String name) {
        this.startcoordinate = coordinate;
        this.size = size;
        this.direction = direction;
        this.name = name;
        this.coordList = genCoordList();
    }

    /** Default constructor required by JPA */
    public Ship() {}

    public int getSize() {
        return size;
    }

    /**
     * @return row index of ship starting coord
     */
    public int getStartRow() {
        return startcoordinate.row;
    }

    /**
     * @return starting column of ship
     */
    public int getStartCol() {
        return startcoordinate.col;
    }

    /**
     * @return the ships direction
     */
    public Direction getDirection() {
        return direction;
    }

    /**
     * @return the ships name
     */
    public String getName() {
        return name;
    }

    /**
     * checks if a ship occupies a given coord
     *
     * @param coord the coordinate to check
     * @return true only if the ship contains the coord
     */
    public boolean containsCoord(final Coord coord) {
        return any(coord::isEqual);
    }

    private List<Coord> genCoordList() {
        final List<Coord> coordList = new ArrayList<>();
        if (direction == Direction.HORIZONTAL) {
            for (int i = 0; i < this.size; i++) {
                coordList.add(startcoordinate.shiftBy(0, i));
            }
        } else {
            for (int i = 0; i < this.size; i++) {
                coordList.add(startcoordinate.shiftBy(i, 0));
            }
        }
        return coordList;
    }

    public List<Coord> getCoordList() {
        return coordList;
    }

    /**
     * Checks if this ship overlaps with another
     *
     * @param other ship to check against
     * @return true if overlap, false if otherwise
     */
    public boolean isOverlapping(final Ship other) {
        return other.any(this::containsCoord);
    }

    /**
     * checks if ship is on grid
     *
     * @return true if ship is on grid, false if not
     */
    public boolean isWithinBounds(final Bounding bounding) {
        for (final Coord coord : this.coordList) {
            if (coord.row < 1 || coord.row > bounding.numRows()) return false;
            if (coord.col < 1 || coord.col > bounding.numCols()) return false;
        }
        return true;
    }

    /** Checks if coordinates in the ships coordinate match given predicate */
    public boolean any(final Predicate<Coord> predicate) {
        for (final Coord coord : getCoordList()) {
            if (predicate.test(coord)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if all coords in ship coord list match predicate
     *
     * @param predicate the condition to test the coord against
     * @return true if they satisfy, false if otherwise
     */
    public boolean all(final Predicate<Coord> predicate) {
        for (final Coord coord : getCoordList()) {
            if (!predicate.test(coord)) {
                return false;
            }
        }
        return true;
    }
}
