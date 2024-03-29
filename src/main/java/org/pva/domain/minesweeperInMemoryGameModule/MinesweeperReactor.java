package org.pva.domain.minesweeperInMemoryGameModule;

import org.pva.domain.abstraction.dto.InputDataDto;
import org.pva.domain.abstraction.dto.OutputDataDto;
import org.pva.domain.abstraction.dto.ResultDataDto;
import org.pva.domain.abstraction.reactor.Reactor;
import org.pva.domain.abstraction.robot.Robot;
import org.pva.domain.minesweeperInMemoryGameModule.cell.Cell;
import org.pva.domain.minesweeperInMemoryGameModule.cell.FreeCell;
import org.pva.domain.minesweeperInMemoryGameModule.cell.MarkedCell;
import org.pva.domain.minesweeperInMemoryGameModule.cell.MinedCell;
import org.pva.domain.minesweeperInMemoryGameModule.dto.*;
import org.pva.domain.minesweeperInMemoryGameModule.field.MinedField;

public class MinesweeperReactor extends Reactor {

    private MinedField virtualField;

    private MinedField visibleField;

    public MinesweeperReactor(MinesweeperStartData startData) {
        this.virtualField = startData.getVirtualField();

        this.visibleField = MinedField.generateUnknownMinedField(this.virtualField.getRowNumber(),
                this.virtualField.getColNumber(),
                this.virtualField.getMineNumber());
    }

    @Override
    public InputDataDto getInputData() {
        return new MinesweeperInputData(visibleField);
    }

    @Override
    public void makeAction(OutputDataDto outputData) {

        Integer row = ((MinesweeperOutputData) outputData).getRow();
        Integer col = ((MinesweeperOutputData) outputData).getCol();
        Boolean markAsMinedCell = ((MinesweeperOutputData) outputData).isMarkedAsMinedCell();

        Cell cell = virtualField.getCell(row, col);
        Cell openCell = cell;

        if (cell instanceof MinedCell && markAsMinedCell) {
            openCell = new MarkedCell(row, col);
        }

        visibleField.setCell(row, col, openCell);

        if (cell instanceof FreeCell && ((FreeCell) cell).getNumberClosestMines().equals(0)) {
            openSurroundCells(row, col);
        }
    }

    @Override
    public ResultDataDto analyseCurrentState(InputDataDto inputData) {
        Integer numberDiscoveredMines = 0;
        MinedField minedField = ((MinesweeperInputData) inputData).getVisibleField();
        for (int i = 0; i < minedField.getRowNumber(); i++) {
            for (int j = 0; j < minedField.getColNumber(); j++) {
                if (minedField.getCell(i, j) instanceof MinedCell)
                    return new MinesweeperResultData(GameState.GAME_OVER);
                if (minedField.getCell(i, j) instanceof MarkedCell)
                    numberDiscoveredMines++;
            }
        }

        if (numberDiscoveredMines.equals(virtualField.getMineNumber()))
            return new MinesweeperResultData(GameState.ROBOT_WINS);

        return new MinesweeperResultData(GameState.CONTINUE_GAME);
    }

    @Override
    public void start() {
        Robot robot = new MinesweeperRobot(getInputData());
        // ***
        while (true) {
            OutputDataDto outputData = robot.makeDataForAction();

            makeAction(outputData);

            MinesweeperResultData resultData = (MinesweeperResultData) analyseCurrentState(getInputData());

            robot.refreshMemory(new MinesweeperInputData(visibleField));
            MinedField.printField(visibleField);

            if (resultData.getGameState().equals(GameState.GAME_OVER)) {
                System.out.println("Game over!");
                break;
            }

            if (resultData.getGameState().equals(GameState.ROBOT_WINS)) {
                System.out.println("Robot wins!");
                break;
            }
        }
        // ***

    }

    private void openSurroundCells(Integer row, Integer col) {
        Cell cell;
        if (row > 0 && col > 0) {
            if (visibleField.getCell(row - 1, col - 1) == null) {
                cell = virtualField.getCell( row - 1, col - 1);
                visibleField.setCell(row - 1, col - 1, cell);
                if (cell instanceof FreeCell && ((FreeCell) cell).getNumberClosestMines().equals(0))
                    openSurroundCells(row - 1, col - 1);
            }
        }

        if (row > 0) {
            if (visibleField.getCell(row - 1, col) == null) {
                cell = virtualField.getCell( row - 1, col);
                visibleField.setCell(row - 1, col, virtualField.getCell( row - 1, col));
                if (cell instanceof FreeCell && ((FreeCell) cell).getNumberClosestMines().equals(0))
                    openSurroundCells(row - 1, col);
            }
        }

        if (row > 0 && col < visibleField.getColNumber() - 1) {
            if (visibleField.getCell(row - 1, col + 1) == null) {
                cell = virtualField.getCell( row - 1, col + 1);
                visibleField.setCell(row - 1, col + 1, virtualField.getCell(row - 1, col + 1));
                if (cell instanceof FreeCell && ((FreeCell) cell).getNumberClosestMines().equals(0))
                    openSurroundCells(row - 1, col + 1);
            }
        }
        if (col < visibleField.getColNumber() - 1) {
            if (visibleField.getCell(row, col +1) == null) {
                cell = virtualField.getCell(row, col + 1);
                visibleField.setCell(row, col + 1, virtualField.getCell(row, col + 1));
                if (cell instanceof FreeCell && ((FreeCell) cell).getNumberClosestMines().equals(0))
                    openSurroundCells(row, col + 1);
            }
        }

        if (row < visibleField.getRowNumber() - 1 && col < visibleField.getColNumber() - 1) {
            if (visibleField.getCell(row + 1, col + 1) == null) {
                cell = virtualField.getCell(row + 1, col + 1);
                visibleField.setCell(row + 1, col + 1, virtualField.getCell(row + 1, col + 1));
                if (cell instanceof FreeCell && ((FreeCell) cell).getNumberClosestMines().equals(0))
                    openSurroundCells(row + 1, col + 1);
            }
        }

        if (row < visibleField.getRowNumber() - 1) {
            if (visibleField.getCell(row + 1, col) == null) {
                cell = virtualField.getCell(row + 1, col);
                visibleField.setCell(row + 1, col, virtualField.getCell(row + 1, col));
                if (cell instanceof FreeCell && ((FreeCell) cell).getNumberClosestMines().equals(0))
                    openSurroundCells(row + 1, col);
            }
        }

        if (row < visibleField.getRowNumber() - 1 && col > 0) {
            if (visibleField.getCell(row + 1, col - 1) == null) {
                cell = virtualField.getCell(row + 1, col - 1);
                visibleField.setCell(row + 1, col - 1, virtualField.getCell(row + 1, col - 1));
                if (cell instanceof FreeCell && ((FreeCell) cell).getNumberClosestMines().equals(0))
                    openSurroundCells(row + 1, col - 1);
            }
        }

        if (col > 0) {
            if (visibleField.getCell(row, col - 1) == null) {
                cell = virtualField.getCell(row, col - 1);
                visibleField.setCell(row, col - 1, virtualField.getCell(row, col - 1));
                if (cell instanceof FreeCell && ((FreeCell) cell).getNumberClosestMines().equals(0))
                    openSurroundCells(row, col - 1);
            }
        }
    }

}
