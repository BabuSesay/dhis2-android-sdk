package org.hisp.dhis.android.sdk.program;

import org.hisp.dhis.java.sdk.models.dataelement.DataElement;
import org.hisp.dhis.java.sdk.models.program.ProgramStage;
import org.hisp.dhis.java.sdk.models.program.ProgramStageDataElement;
import org.hisp.dhis.java.sdk.models.program.ProgramStageSection;

import java.util.List;

import rx.Observable;

public interface IProgramStageDataElementScope {

    Observable<ProgramStageDataElement> get(long id);

    Observable<List<ProgramStageDataElement>> list();

    Observable<List<ProgramStageDataElement>> list(ProgramStage programStage);

    Observable<ProgramStageDataElement> list(ProgramStage programStage, DataElement dataElement);

    Observable<List<ProgramStageDataElement>> list(ProgramStageSection programStageSection);

    Observable<Boolean> save(ProgramStageDataElement object);

    Observable<Boolean> remove(ProgramStageDataElement object);

}
