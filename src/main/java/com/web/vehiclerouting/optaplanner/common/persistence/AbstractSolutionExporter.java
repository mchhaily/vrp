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
import java.util.Arrays;

import org.optaplanner.core.impl.solution.Solution;
import com.web.vehiclerouting.optaplanner.common.app.LoggingMain;
import com.web.vehiclerouting.optaplanner.common.business.ProblemFileComparator;

public abstract class AbstractSolutionExporter extends LoggingMain {

    private static final String DEFAULT_INPUT_FILE_SUFFIX = "xml";
    protected SolutionDao solutionDao;

    public AbstractSolutionExporter(SolutionDao solutionDao) {
        this.solutionDao = solutionDao;
    }

    protected File getInputDir() {
        return new File(solutionDao.getDataDir(), "solved");
    }

    protected File getOutputDir() {
        return new File(solutionDao.getDataDir(), "export");
    }

    protected String getInputFileSuffix() {
        return DEFAULT_INPUT_FILE_SUFFIX;
    }

    protected abstract String getOutputFileSuffix();

    public void convertAll() {
        File inputDir = getInputDir();
        if (!inputDir.exists()) {
            throw new IllegalStateException("The directory inputDir (" + inputDir.getAbsolutePath()
                    + ") does not exist.");
        }
        File outputDir = getOutputDir();
        outputDir.mkdirs();
        File[] inputFiles = inputDir.listFiles();
        Arrays.sort(inputFiles, new ProblemFileComparator());
        for (File inputFile : inputFiles) {
            String inputFileName = inputFile.getName();
            if (inputFileName.endsWith("." + getInputFileSuffix())) {
                Solution solution = solutionDao.readSolution(inputFile);
                String outputFileName = inputFileName.substring(0,
                        inputFileName.length() - getInputFileSuffix().length())
                        + getOutputFileSuffix();
                File outputFile = new File(outputDir, outputFileName);
                writeSolution(solution, outputFile);
            }
        }
    }

    public abstract void writeSolution(Solution solution, File outputFile);

    public abstract class OutputBuilder {

    }

}
