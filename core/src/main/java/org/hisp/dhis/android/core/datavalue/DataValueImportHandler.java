package org.hisp.dhis.android.core.datavalue;

import android.support.annotation.NonNull;

import org.hisp.dhis.android.core.common.State;
import org.hisp.dhis.android.core.imports.ImportConflict;
import org.hisp.dhis.android.core.imports.ImportSummary;

import java.util.List;

public class DataValueImportHandler {

    DataValueSetStore dataValueSetStore;

    public DataValueImportHandler(DataValueSetStore dataValueSetStore) {
        this.dataValueSetStore = dataValueSetStore;
    }

    public void handleImportSummary(@NonNull DataValueSet dataValueSet,
                                    @NonNull ImportSummary importSummary) {

        if (importSummary == null) {
            return;
        }

        for (DataValue dataValue : dataValueSet.dataValues) {

            if (isConflictive(dataValue, importSummary)) {
                dataValueSetStore.setState(dataValue.dataElement(), State.ERROR);
                continue;
            }

            dataValueSetStore.setState(dataValue.dataElement(), State.SYNCED);

        }
    }

    private boolean isConflictive(@NonNull DataValue dataValue,
                                  @NonNull ImportSummary importSummary) {

        if (importSummary.importConflicts() == null) {
            return false;
        }


        List<ImportConflict> importConflicts = importSummary.importConflicts();

        for (ImportConflict importConflict : importConflicts) {
            if (dataValue.dataElement().equals(importConflict.object())) {
                return true;
            }
        }

        return false;
    }
}
