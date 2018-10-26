package org.hisp.dhis.android.core.event;

import android.support.annotation.NonNull;
import android.util.Log;

import org.hisp.dhis.android.core.common.State;
import org.hisp.dhis.android.core.arch.db.implementations.DatabaseAdapter;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityDataValueHandler;

import java.util.Collection;

import static org.hisp.dhis.android.core.utils.Utils.isDeleted;

public class EventHandler {
    private final EventStore eventStore;
    private final TrackedEntityDataValueHandler trackedEntityDataValueHandler;

    EventHandler(EventStore eventStore,
            TrackedEntityDataValueHandler trackedEntityDataValueHandler) {
        this.eventStore = eventStore;
        this.trackedEntityDataValueHandler = trackedEntityDataValueHandler;
    }

    public void handleMany(@NonNull Collection<Event> events) {
        for (Event event : events) {
            handle(event);
        }
    }

    private boolean isValid(@NonNull Event event) {
        return event.eventDate()!=null && event.organisationUnit()!=null;
    }

    public void handle(@NonNull Event event) {
        if (event == null) {
            return;
        }

        if (isDeleted(event)) {
            eventStore.delete(event.uid());
        } else if (isValid(event)) {
            String latitude = null;
            String longitude = null;

            if (event.coordinates() != null) {
                latitude = event.coordinates().latitude().toString();
                longitude = event.coordinates().longitude().toString();
            }


            int updatedRow = eventStore.update(event.uid(), event.enrollmentUid(), event.created(), event.lastUpdated(),
                    event.createdAtClient(), event.lastUpdatedAtClient(),
                    event.status(), latitude, longitude, event.program(), event.programStage(),
                    event.organisationUnit(), event.eventDate(), event.completedDate(),
                    event.dueDate(), State.SYNCED, event.attributeOptionCombo(),
                    event.trackedEntityInstance(), event.uid());

            if (updatedRow <= 0) {
                eventStore.insert(event.uid(), event.enrollmentUid(), event.created(), event.lastUpdated(),
                        event.createdAtClient(), event.lastUpdatedAtClient(),
                        event.status(), latitude, longitude, event.program(), event.programStage(),
                        event.organisationUnit(), event.eventDate(), event.completedDate(),
                        event.dueDate(), State.SYNCED, event.attributeOptionCombo(),
                        event.trackedEntityInstance());
            }

            trackedEntityDataValueHandler.handle(event.uid(),
                    event.trackedEntityDataValues());
        } else {
            Log.d(this.getClass().getSimpleName(), event.uid() + " with no org. unit or event date");
        }
    }

    public static EventHandler create(DatabaseAdapter databaseAdapter) {
        return new EventHandler(
                new EventStoreImpl(databaseAdapter),
                TrackedEntityDataValueHandler.create(databaseAdapter)
        );
    }
}
