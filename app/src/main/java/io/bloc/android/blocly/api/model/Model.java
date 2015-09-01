package io.bloc.android.blocly.api.model;

/**
 * Created by Mike on 8/30/2015.
 */
public abstract class Model {

    private final long rowId;

    public Model(long rowId) {
        this.rowId = rowId;
    }

    public long getRowId() {
        return rowId;
    }
    //#56 Created a Model class for retaining the row identifier.
}
