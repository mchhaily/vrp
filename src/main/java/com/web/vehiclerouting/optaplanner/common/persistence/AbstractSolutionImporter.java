/*
 * Copyright 2010 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.web.vehiclerouting.optaplanner.common.persistence;

import java.io.File;
import java.io.FileFilter;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.optaplanner.core.impl.solution.Solution;
import com.web.vehiclerouting.optaplanner.common.app.LoggingMain;
import com.web.vehiclerouting.optaplanner.common.business.ProblemFileComparator;

public abstract class AbstractSolutionImporter extends LoggingMain {

    protected static final String DEFAULT_OUTPUT_FILE_SUFFIX = "xml";

    protected final SolutionDao solutionDao;
    protected final File inputDir;
    protected final File outputDir;

    public AbstractSolutionImporter(SolutionDao solutionDao) {
        this.solutionDao = solutionDao;
        inputDir = new File(solutionDao.getDataDir(), "import");
        if (!inputDir.exists()) {
            throw new IllegalStateException("The directory inputDir (" + inputDir.getAbsolutePath()
                    + ") does not exist.");
        }
        outputDir = new File(solutionDao.getDataDir(), "unsolved");
    }

    public AbstractSolutionImporter(boolean withoutDao) {
        if (!withoutDao) {
            throw new IllegalArgumentException("The parameter withoutDao (" + withoutDao + ") must be true.");
        }
        solutionDao = null;
        inputDir = null;
        outputDir = null;
    }

    public File getInputDir() {
        return inputDir;
    }

    public File getOutputDir() {
        return outputDir;
    }

    public abstract String getInputFileSuffix();

    protected String getOutputFileSuffix() {
        return DEFAULT_OUTPUT_FILE_SUFFIX;
    }

    public void convertAll() {
        File[] inputFiles = inputDir.listFiles();
        Arrays.sort(inputFiles, new ProblemFileComparator());
        for (File inputFile : inputFiles) {
            if (acceptInputFile(inputFile) && acceptInputFileDuringBulkConvert(inputFile)) {
                String inputFileName = inputFile.getName();
                String outputFileName = inputFileName.substring(0,
                        inputFileName.length() - getInputFileSuffix().length())
                        + getOutputFileSuffix();
                File outputFile = new File(outputDir, outputFileName);
                convert(inputFile, outputFile);
            }
        }
    }

    public void convert(String inputFileName, String outputFileName) {
        File inputFile = new File(inputDir, inputFileName);
        if (!inputFile.exists()) {
            throw new IllegalStateException("The file inputFile (" + inputFile.getAbsolutePath()
                    + ") does not exist.");
        }
        File outputFile = new File(outputDir, outputFileName);
        outputFile.getParentFile().mkdirs();
        convert(inputFile, outputFile);
    }

    protected void convert(File inputFile, File outputFile) {
        Solution solution = readSolution(inputFile);
        solutionDao.writeSolution(solution, outputFile);
    }

    public boolean acceptInputFile(File inputFile) {
        return inputFile.getName().endsWith("." + getInputFileSuffix());
    }

    /**
     * Some files are too big to be serialized to XML or take too long.
     * @param inputFile never null
     * @return true if accepted
     */
    public boolean acceptInputFileDuringBulkConvert(File inputFile) {
        return true;
    }

    public abstract Solution readSolution(File inputFile);

    public abstract class InputBuilder {

    }

    public static String getFlooredPossibleSolutionSize(BigInteger possibleSolutionSize) {
        if (possibleSolutionSize.compareTo(BigInteger.valueOf(1000L)) < 0) {
            return possibleSolutionSize.toString();
        }
        // TODO this is slow for machinereassingment's biggest dataset
        return "10^" + (possibleSolutionSize.toString().length() - 1);
    }

}
