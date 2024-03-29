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

package com.web.vehiclerouting.optaplanner.swingui;

import java.awt.Graphics;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import javax.swing.JPanel;

import com.web.vehiclerouting.optaplanner.common.swingui.latitudelongitude.LatitudeLongitudeTranslator;
import com.web.vehiclerouting.optaplanner.domain.VehicleRoutingSolution;

public class VehicleRoutingWorldPanel extends JPanel {
    private final VehicleRoutingPanel vehicleRoutingPanel;

    private VehicleRoutingSchedulePainter schedulePainter = new VehicleRoutingSchedulePainter();

    public VehicleRoutingWorldPanel(VehicleRoutingPanel vehicleRoutingPanel) {
        this.vehicleRoutingPanel = vehicleRoutingPanel;
        schedulePainter = new VehicleRoutingSchedulePainter();
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                // TODO Not thread-safe during solving
                VehicleRoutingSolution schedule = VehicleRoutingWorldPanel.this.vehicleRoutingPanel.getSchedule();
                if (schedule != null) {
                    resetPanel(schedule);
                }
            }
        });
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                LatitudeLongitudeTranslator translator = schedulePainter.getTranslator();
                if (translator != null) {
                    double longitude = translator.translateXToLongitude(e.getX());
                    double latitude = translator.translateYToLatitude(e.getY());
                    VehicleRoutingWorldPanel.this.vehicleRoutingPanel.insertLocationAndCustomer(longitude, latitude);
                }
            }
        });
    }

    public void resetPanel(VehicleRoutingSolution schedule) {
        schedulePainter.reset(schedule, getSize(), this);
        repaint();
    }

    public void updatePanel(VehicleRoutingSolution schedule) {
        resetPanel(schedule);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        BufferedImage canvas = schedulePainter.getCanvas();
        if (canvas != null) {
            g.drawImage(canvas, 0, 0, this);
        }
    }

}
