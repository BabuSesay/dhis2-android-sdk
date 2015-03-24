/*
 *  Copyright (c) 2015, University of Oslo
 *  * All rights reserved.
 *  *
 *  * Redistribution and use in source and binary forms, with or without
 *  * modification, are permitted provided that the following conditions are met:
 *  * Redistributions of source code must retain the above copyright notice, this
 *  * list of conditions and the following disclaimer.
 *  *
 *  * Redistributions in binary form must reproduce the above copyright notice,
 *  * this list of conditions and the following disclaimer in the documentation
 *  * and/or other materials provided with the distribution.
 *  * Neither the name of the HISP project nor the names of its contributors may
 *  * be used to endorse or promote products derived from this software without
 *  * specific prior written permission.
 *  *
 *  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 *  * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 *  * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *  * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 *  * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 *  * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 *  * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 *  * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 *  * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 */

package org.hisp.dhis2.android.sdk.persistence.models;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.structure.BaseModel;

import org.hisp.dhis2.android.sdk.controllers.Dhis2;
import org.hisp.dhis2.android.sdk.controllers.datavalues.DataValueController;

import java.util.List;
import java.util.UUID;

/**
 * @author Simen Skogly Russnes on 04.03.15.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Table
public class Enrollment extends BaseModel{

    public static final String ACTIVE = "ACTIVE";
    public static final String COMPLETED = "COMPLETED";
    public static final String CANCELLED = "CANCELLED"; //aka TERMINATED

    @JsonAnySetter
    public void handleUnknown(String key, Object value) {}

    @JsonIgnore
    @Column
    public boolean fromServer = true;

    @JsonIgnore
    @Column(columnType = Column.PRIMARY_KEY)
    public String enrollment;

    @JsonProperty("enrollment")
    public void setEnrollment(String enrollment) {
        this.enrollment = enrollment;
    }

    /**
     * Should only be used by Jackson so that event is included only if its non-local generated
     * Use Event.event instead to access it.
     */
    @JsonProperty("enrollment")
    public String getEnrollment() {
        String randomUUID = Dhis2.QUEUED + UUID.randomUUID().toString();
        if(enrollment.length() == randomUUID.length())
            return null;
        else return enrollment;
    }

    @JsonProperty("trackedEntityInstance")
    @Column
    public String trackedEntityInstance;

    @JsonProperty("program")
    @Column
    public String program;

    @JsonProperty("dateOfEnrollment")
    @Column
    public String dateOfEnrollment;

    @JsonProperty("dateOfIncident")
    @Column
    public String dateOfIncident;

    @JsonProperty("followup")
    @Column
    public boolean followup;

    @JsonProperty("status")
    @Column
    public String status;

    @JsonIgnore
    List<Event> events;

    /**
     * gets a list of events for this enrollment
     * @param reLoad true if you want to re-load from database. False if just use what's already
     *               loaded ( faster )
     * @return
     */
    public List<Event> getEvents(boolean reLoad) {
        if(events == null || reLoad) events = DataValueController.getEventsByEnrollment(enrollment);
        return events;
    }

    public void setEvents(List<Event> events) {
        this.events = events;
    }

    @Override
    public void save(boolean async) {
        super.save(async);
        if(events!=null) {
            for(Event event: events) {
                event.save(async);
            }
        }
    }

}
