package com.web.vehiclerouting.optaplanner.domain.timewindowed.solver;

import org.apache.commons.lang.ObjectUtils;
import org.optaplanner.core.impl.domain.variable.listener.PlanningVariableListener;
import org.optaplanner.core.impl.score.director.ScoreDirector;
import com.web.vehiclerouting.optaplanner.domain.Customer;
import com.web.vehiclerouting.optaplanner.domain.Standstill;
import com.web.vehiclerouting.optaplanner.domain.timewindowed.TimeWindowedCustomer;

// TODO When this class is added only for TimeWindowedCustomer, use TimeWindowedCustomer instead of Customer
public class ArrivalTimeUpdatingVariableListener implements PlanningVariableListener<Customer> {

    public void beforeEntityAdded(ScoreDirector scoreDirector, Customer customer) {
        // Do nothing
    }

    public void afterEntityAdded(ScoreDirector scoreDirector, Customer customer) {
        if (customer instanceof TimeWindowedCustomer) {
            updateVehicle(scoreDirector, (TimeWindowedCustomer) customer);
        }
    }

    public void beforeVariableChanged(ScoreDirector scoreDirector, Customer customer) {
        // Do nothing
    }

    public void afterVariableChanged(ScoreDirector scoreDirector, Customer customer) {
        if (customer instanceof TimeWindowedCustomer) {
            updateVehicle(scoreDirector, (TimeWindowedCustomer) customer);
        }
    }

    public void beforeEntityRemoved(ScoreDirector scoreDirector, Customer customer) {
        // Do nothing
    }

    public void afterEntityRemoved(ScoreDirector scoreDirector, Customer customer) {
        // Do nothing
    }

    protected void updateVehicle(ScoreDirector scoreDirector, TimeWindowedCustomer sourceCustomer) {
        Standstill previousStandstill = sourceCustomer.getPreviousStandstill();
        Integer milliDepartureTime = (previousStandstill instanceof TimeWindowedCustomer)
                ? ((TimeWindowedCustomer) previousStandstill).getDepartureTime() : null;
        TimeWindowedCustomer shadowCustomer = sourceCustomer;
        Integer milliArrivalTime = calculateMilliArrivalTime(shadowCustomer, milliDepartureTime);
        while (shadowCustomer != null && ObjectUtils.notEqual(shadowCustomer.getMilliArrivalTime(), milliArrivalTime)) {
            scoreDirector.beforeVariableChanged(shadowCustomer, "milliArrivalTime");
            shadowCustomer.setMilliArrivalTime(milliArrivalTime);
            scoreDirector.afterVariableChanged(shadowCustomer, "milliArrivalTime");
            milliDepartureTime = shadowCustomer.getDepartureTime();
            shadowCustomer = shadowCustomer.getNextCustomer();
            milliArrivalTime = calculateMilliArrivalTime(shadowCustomer, milliDepartureTime);
        }
    }

    private Integer calculateMilliArrivalTime(TimeWindowedCustomer customer, Integer previousMilliDepartureTime) {
        if (customer == null) {
            return null;
        }
        if (previousMilliDepartureTime == null) {
            // PreviousStandstill is the Vehicle, so we leave from the Depot at the best suitable time
            return Math.max(customer.getMilliReadyTime(), customer.getMilliDistanceToPreviousStandstill());
        }
        return previousMilliDepartureTime + customer.getMilliDistanceToPreviousStandstill();
    }

}
