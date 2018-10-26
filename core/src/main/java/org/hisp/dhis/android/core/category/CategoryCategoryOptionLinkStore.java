package org.hisp.dhis.android.core.category;


import android.database.Cursor;
import android.database.sqlite.SQLiteStatement;
import android.support.annotation.NonNull;

import org.hisp.dhis.android.core.arch.db.binders.StatementBinder;
import org.hisp.dhis.android.core.common.CursorModelFactory;
import org.hisp.dhis.android.core.common.LinkModelStore;
import org.hisp.dhis.android.core.common.StoreFactory;
import org.hisp.dhis.android.core.arch.db.implementations.DatabaseAdapter;

import static org.hisp.dhis.android.core.utils.StoreUtils.sqLiteBind;

final class CategoryCategoryOptionLinkStore {

    private CategoryCategoryOptionLinkStore() {}

    private static final StatementBinder<CategoryCategoryOptionLinkModel> BINDER
            = new StatementBinder<CategoryCategoryOptionLinkModel>() {
        @Override
        public void bindToStatement(@NonNull CategoryCategoryOptionLinkModel o,
                                    @NonNull SQLiteStatement sqLiteStatement) {
            sqLiteBind(sqLiteStatement, 1, o.category());
            sqLiteBind(sqLiteStatement, 2, o.option());
            sqLiteBind(sqLiteStatement, 3, o.sortOrder());
        }
    };

    private static final CursorModelFactory<CategoryCategoryOptionLinkModel> FACTORY
            = new CursorModelFactory<CategoryCategoryOptionLinkModel>() {
        @Override
        public CategoryCategoryOptionLinkModel fromCursor(Cursor cursor) {
            return CategoryCategoryOptionLinkModel.create(cursor);
        }
    };

    public static LinkModelStore<CategoryCategoryOptionLinkModel> create(DatabaseAdapter databaseAdapter) {
        return StoreFactory.linkModelStore(databaseAdapter, CategoryCategoryOptionLinkModel.TABLE,
                new CategoryCategoryOptionLinkModel.Columns(),
                CategoryCategoryOptionLinkModel.Columns.CATEGORY, BINDER, FACTORY);
    }
}
