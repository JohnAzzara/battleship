package core;

import db.CellArrayAttributeConverter;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** Grid is the cells marked as rows and cols. Adds ship functionality. */
@Entity
@Table(name = "grid")
public class Grid implements Bounding {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "gridId")
    private long id;

    /** The cells in a given horizontal line */
    @Column(name = "rows")
    private int rows;

    /** The cells in a given vertical line */
    @Column(name = "cols")
    private int cols;

    /** The individual cell in a group of cells */
    @Convert(converter = CellArrayAttributeConverter.class)
    @Column(name = "cells", columnDefinition = "TEXT")
    private Cell[][] cells;

    /** Holds the list of different ships */
    @ManyToOne
    @JoinColumn(name = "ships", referencedColumnName = "id")
    private ShipList shipList;

    public Grid() {
        this.rows = 0;
        this.cols = 0;
    }

    /**
     * creates the grid given the 3 parameters
     *
     * @param rows are cells on a vertical line
     * @param cols are cells on a horizontal line
     * @param shipList is a list of different ships
     */
    public Grid(final int rows, final int cols, final List<Ship> shipList) {

        this.rows = rows;
        this.cols = cols;

        cells = new Cell[rows][cols];

        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                cells[row][col] = new Cell();
            }
        }

        this.shipList = new ShipList(shipList);
    }

    /** sets the row and col */
    public Grid(final int rows, final int cols) {
        this(rows, cols, new ArrayList<>());
    }

    Cell getCell(Coord coordinate) {
        int row = coordinate.row - 1;
        int col = coordinate.col - 1;
        return cells[row][col];
    }

    /**
     * gets the status of the cell
     *
     * @return whether the cell has been hit
     */
    public CellStatus getStatus(final Coord coordinate) {
        final Cell cell = getCell(coordinate);
        for (final Ship ship : shipList.getShips()) {
            if (ship.containsCoord(coordinate)) {
                return cell.hasBeenShot() ? CellStatus.SHIP_HIT : CellStatus.SHIP_UNREVEALED;
            }
        }
        return cell.hasBeenShot() ? CellStatus.EMPTY : CellStatus.UNKNOWN;
    }

    /**
     * the number of rows in the grid
     *
     * @return the number of rows
     */
    public int numRows() {
        return rows;
    }

    /**
     * the number of cols in the grid
     *
     * @return the number of cols
     */
    public int numCols() {
        return cols;
    }

    /** adds ship to list */
    public void addShip(final Ship ship) {
        shipList.addShip(ship);
    }

    /**
     * checks if all the ships are sunk
     *
     * @return boolean answer for check
     */
    public boolean allShipsAreSunk() {
        return shipList.all(
                ship ->
                        ship.getCoordList().stream()
                                .allMatch(
                                        coord ->
                                                this.getStatus(coord).equals(CellStatus.SHIP_HIT)));
    }

    /**
     * gets list of ships
     *
     * @return the list of ships
     */
    public List<Ship> getShipList() {
        return shipList.getShips();
    }

    public ShipList getShipListObject() {
        return shipList;
    }

    /**
     * checks if ship is sunk
     *
     * @param ship is a ship in list of ships
     * @return boolean value for ship status
     */
    public boolean getSunkShipAt(final Ship ship) {
        for (final Coord coord : ship.getCoordList()) {
            if (!this.getStatus(coord).equals(CellStatus.SHIP_HIT)) {
                return false;
            }
        }
        return true;
    }

    public Optional<Ship> getSunkShipAt(Coord coordinate) {
        return shipList.getShipAt(coordinate, this).filter(ship -> ship.all(this::isShipHit));
    }

    /** changes status of given cell to shoot */
    public void shoot(final Coord coordinate) {
        final Cell shipCell = getCell(coordinate);
        final CellStatus targetStatus = getStatus(coordinate);
        if (!targetStatus.equals(CellStatus.SHIP_HIT)) {
            shipCell.setAsShot();
        }
    }

    public long getId() {
        return id;
    }

    public boolean isShipHit(Coord coordinate) {
        return (getStatus(coordinate).equals(CellStatus.SHIP_HIT));
    }

    /**
     * Checks if the two grids are the same
     *
     * @param other the grid it is comparing to
     * @return false if the grids are not the same true if the grids are the same
     */
    public boolean isSameAs(Grid other) {
        if (this.numRows() != other.numRows() || this.numCols() != other.numCols()) {
            return false;
        }
        for (int row = 0; row < this.numRows(); row++) {
            for (int col = 0; col < this.numCols(); col++) {
                if (!this.cells[row][col].equals(other.cells[row][col])) {
                    return false;
                }
            }
        }
        return true;
    }
}
