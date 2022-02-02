package com.denisab85.filetreetoexcel;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.nio.file.Path;

@Command(name = "FileTreeToExcel.jar", description = "Parameters:")
public class CommandLineArguments {

    @Option(names = {"-d", "--directory"}, description = "Path to the root folder.", arity = "0..1", defaultValue = ".")
    public Path directory;

    @Option(names = {"-o", "--outputFile"}, description = "Path to the Excel file to save the file structure.", required = true)
    public Path outputFile;

    @Option(names = {"-i", "--include"}, description = "File mask to include files. This parameter can be used " +
            "multiple times to supply multiple include masks: -i *.txt -i *.doc", arity = "1..*", defaultValue = "")
    public String[] include;

    @Option(names = {"-e", "--exclude"}, description = "File mask to exclude files. This parameter can be used " +
            "multiple times to supply multiple exclude masks: -e *.txt -e *.doc", arity = "1..*", defaultValue = "")
    public String[] exclude;

}
