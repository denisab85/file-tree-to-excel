package com.denisab85.filetreetoexcel;

import org.apache.commons.io.filefilter.WildcardFileFilter;

import java.io.FileFilter;
import java.nio.file.Path;
import java.util.stream.Stream;

public class WildcardFilter {

    private final String[] filters;

    public WildcardFilter(String[] filters) {
        if (filters == null || filters.length == 0) {
            throw new IllegalArgumentException("Filters can not be null or empty.");
        }
        this.filters = filters;
    }

    private Stream<Path> process(Stream<Path> files, boolean include) {
        FileFilter combinedFilter = null;
        for (String mask : filters) {
            FileFilter newFilter = new WildcardFileFilter(mask);
            if (combinedFilter == null) {
                combinedFilter = new WildcardFileFilter(mask);
            } else {
                FileFilter oldFilter = combinedFilter;
                combinedFilter = f -> oldFilter.accept(f) || newFilter.accept(f);
            }
        }
        FileFilter finalCombinedFilter = combinedFilter;
        return files.filter(f -> f.toFile().isDirectory() || finalCombinedFilter.accept(f.toFile()) == include);
    }

    public Stream<Path> include(Stream<Path> files) {
        return process(files, true);
    }

    public Stream<Path> exclude(Stream<Path> files) {
        return process(files, false);
    }

}
