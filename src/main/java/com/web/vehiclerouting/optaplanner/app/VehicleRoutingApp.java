/*
 * Copyright 2012 JBoss Inc
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

package com.web.vehiclerouting.optaplanner.app;

import org.optaplanner.core.api.solver.Solver;
import org.optaplanner.core.config.solver.XmlSolverFactory;
import com.web.vehiclerouting.optaplanner.common.app.CommonApp;
import com.web.vehiclerouting.optaplanner.common.persistence.AbstractSolutionImporter;
import com.web.vehiclerouting.optaplanner.common.persistence.SolutionDao;
import com.web.vehiclerouting.optaplanner.common.swingui.SolutionPanel;
import com.web.vehiclerouting.optaplanner.persistence.VehicleRoutingDao;
import com.web.vehiclerouting.optaplanner.persistence.VehicleRoutingImporter;
import com.web.vehiclerouting.optaplanner.swingui.VehicleRoutingPanel;

public class VehicleRoutingApp extends CommonApp {

    public static final String SOLVER_CONFIG
            = "/com/vehiclerouting/solver/vehicleRoutingSolverConfig.xml";

    public static void main(String[] args) {
        fixateLookAndFeel();
        new VehicleRoutingApp().init();
    }

    public VehicleRoutingApp() {
        super("Vehicle routing",
                "Official competition name: Capacitated vehicle routing problem (CVRP), " +
                        "optionally with time windows (CVRPTW)\n\n" +
                        "Pick up all items of all customers with a few vehicles.\n\n" +
                        "Find the shortest route possible.\n" +
                        "Do not overload the capacity of the vehicles.\n" +
                        "Arrive within the time window of each customer.",
                VehicleRoutingPanel.LOGO_PATH);
    }

    @Override
    protected Solver createSolver() {
        XmlSolverFactory solverFactory = new XmlSolverFactory();
        solverFactory.configure(SOLVER_CONFIG);
        return solverFactory.buildSolver();
    }

    @Override
    protected SolutionPanel createSolutionPanel() {
        return new VehicleRoutingPanel();
    }

    @Override
    protected SolutionDao createSolutionDao() {
        return new VehicleRoutingDao();
    }

    @Override
    protected AbstractSolutionImporter createSolutionImporter() {
        return new VehicleRoutingImporter();
    }

}
