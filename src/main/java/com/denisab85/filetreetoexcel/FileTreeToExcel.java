package com.denisab85.filetreetoexcel;

import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;

import java.awt.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Stack;
import java.util.stream.Stream;

import static java.lang.String.format;

public class FileTreeToExcel {

    private final Logger log = LoggerFactory.getLogger(FileTreeToExcel.class);

    private final Stack<RowLevel> rowLevels = new Stack<>();

    private final CommandLineArguments params;

    private Sheet sheet;

    public FileTreeToExcel(CommandLineArguments params) {
        this.params = params;
    }

    public static void main(String[] args) {
        CommandLineArguments params = null;
        try {
            params = CommandLine.populateCommand(new CommandLineArguments(), args);
        } catch (Exception e) {
            CommandLine.usage(new CommandLineArguments(), System.out);
            System.exit(1);
        }
        FileTreeToExcel fileTreeToExcel = new FileTreeToExcel(params);
        try {
            fileTreeToExcel.process();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Stream<Path> listFiles(Path path) throws IOException {
        Stream<Path> result = Files.list(path).sorted();
        if (this.params.include != null && this.params.include.length > 0) {
            result = new WildcardFilter(this.params.include).include(result);
        }
        if (this.params.exclude != null && this.params.exclude.length > 0) {
            result = new WildcardFilter(this.params.exclude).exclude(result);
        }
        return result;
    }

    private void process() throws IOException {
        File directory = params.directory.toFile();
        if (!directory.isDirectory()) {
            throw new RuntimeException(directory + " is not a file. Please provide a path to a directory.");
        }
        File excel = params.outputFile.toFile();
        if (excel.exists() && !excel.delete()) {
            throw new RuntimeException("Path already exists and couldn't be deleted: '" + excel + "'.");
        }
        try (Workbook workbook = new XSSFWorkbook();
             OutputStream fileOut = new FileOutputStream(excel)) {
            sheet = workbook.createSheet(directory.getName());
            sheet.setRowSumsBelow(false);
            listFiles(params.directory).forEach(p -> processPath(p, 0));
            makeGroups(new RowLevel(sheet.getLastRowNum() + 1, 0));
            workbook.write(fileOut);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Desktop.getDesktop().open(excel);
    }

    private void processPath(Path path, final int level) {
        Row row = sheet.createRow(sheet.getLastRowNum() + 1);
        row.createCell(level, CellType.STRING).setCellValue(path.toFile().getName());
        RowLevel current = new RowLevel(row.getRowNum(), level);
        // Level jumps => mark the beginning of another row group
        if (rowLevels.empty() || level > rowLevels.peek().level) {
            rowLevels.push(current);
        }
        // Level drops => implement new row groups all the way down to the current level in the workbook
        else {
            makeGroups(current);
        }
        if (path.toFile().isDirectory()) {
            try {
                listFiles(path).forEach(p -> processPath(p, level + 1));
            } catch (IOException e) {
                e.printStackTrace();
                System.exit(3);
            }
        }
    }

    private void makeGroups(RowLevel current) {
        while (current.level < rowLevels.peek().level) {
            RowLevel previous = rowLevels.pop();
            sheet.groupRow(previous.row, current.row - 1);
            log.error(format("Creating row group: %d..%d", previous.row, current.row - 1));
        }
    }

    private static final class RowLevel {

        final int row;

        final int level;

        private RowLevel(int row, int level) {
            this.row = row;
            this.level = level;
        }

        @Override
        public String toString() {
            return format("%s {row=%d, level=%d}", getClass().getSimpleName(), row, level);
        }

    }

}
