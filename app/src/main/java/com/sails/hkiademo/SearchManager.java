package com.sails.hkiademo;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;

import com.sails.engine.LocationRegion;
import com.sails.engine.SAILS;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Richard on 2014/1/16.
 */
public class SearchManager {
    private final Context context;
    private MyDatabaseHelper dbHelper;
    private SAILS mSails;
    private SQLiteDatabase database;
    private List<LocationRegion> locationRegionList=new ArrayList<LocationRegion>();
    private FrameLayout frameLayout;
    private boolean show=false;
    private EditText editTextSearch;
    private int textLength=0;
    private ListView listViewSearch;
    private ExpandableListView eLV;
    private ExpandableAdapter eAdapter;
    public boolean isReady=false;
    public final static String LR_TABLE ="lr"; // name of table

    public final static String LR_ID ="_id"; // id value for employee
    public final static String LR_NAME="name";  // name of employee
    public final static String LR_DESC="desc";  // name of employee


    private FunctionManager functionManager=null;
    private List<FunctionManager.FunctionElement> functionElementList=null;
    private int numberElementInRow;
    private FrameLayout frameLayoutFunctionManager=null;
    private FunctionManager.OnFunctionClickListener onFunctionClickListener=null;
    OnLocationRegionClick onLocationRegionClick=null;
    public interface OnLocationRegionClick {
        void onClick(LocationRegion lr);
    }

    public void setOnLocationRegionClick(OnLocationRegionClick onLocationRegionClick) {
        this.onLocationRegionClick = onLocationRegionClick;
    }

    /**
     *
     * @param context
     */
    public SearchManager(Context context,SAILS sails,FrameLayout frameLayout){
        this.context=context;
        dbHelper = new MyDatabaseHelper(context);
//        database = dbHelper.getWritableDatabase();
//        removeAll();
        mSails=sails;
        this.frameLayout=frameLayout;

    }
    void refreshAllExpandableItemsByFloor() {

        //1st stage groups
        List<Map<String, String>> groups = new ArrayList<Map<String, String>>();
        //2nd stage groups
        List<List<Map<String, LocationRegion>>> childs = new ArrayList<List<Map<String, LocationRegion>>>();
        for (String mS : mSails.getFloorNameList()) {
            Map<String, String> group_item = new HashMap<String, String>();
            group_item.put("group", mSails.getFloorDescList().get(mSails.getFloorNameList().indexOf(mS)));
            List<Map<String, LocationRegion>> child_items = new ArrayList<Map<String, LocationRegion>>();
            for (LocationRegion mlr : mSails.getLocationRegionList(mS)) {
                if (mlr.getName() == null || mlr.getName().length() == 0)
                    continue;

                Map<String, LocationRegion> childData = new HashMap<String, LocationRegion>();
                childData.put("child", mlr);
                child_items.add(childData);
            }
            if(child_items.size()!=0) {
                groups.add(group_item);
                childs.add(child_items);
            }
        }

        eAdapter = new ExpandableAdapter(context, groups, childs);
        eLV.setAdapter(eAdapter);
    }
    public void clear() {
        if(frameLayout!=null)
            frameLayout.removeAllViews();
        functionManager=null;
    }
    public void setFunctionElementList(List<FunctionManager.FunctionElement> functionElementList, int numberElementInRow) {
        this.functionElementList=functionElementList;
        this.numberElementInRow=numberElementInRow;

    }
    private void generateFunctionManager(List<FunctionManager.FunctionElement> functionElementList, int numberElementInRow) {
        this.functionElementList=functionElementList;
        this.numberElementInRow=numberElementInRow;
        if(frameLayoutFunctionManager!=null) {
            functionManager = new FunctionManager(context,frameLayoutFunctionManager,functionElementList,numberElementInRow);
            functionManager.setOnFunctionClickListener(onFunctionClickListener);
            functionManager.openFunctionView();
        }
    }
    public FunctionManager getFunctionManager() {
        return functionManager;
    }
    public void openView() {
        if(frameLayout.getChildCount()==0) {
            View searchView = View.inflate(context, R.layout.function_search, null);
            frameLayout.addView(searchView);
            editTextSearch=(EditText)frameLayout.findViewById(R.id.editTextSearch);
            ImageView iv=(ImageView)frameLayout.findViewById(R.id.imageViewSearchCancel);
            iv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(editTextSearch.getText().length()==0)
                        closeView();
                    else {
                        editTextSearch.setText("");
                    }
                }
            });
            listViewSearch=(ListView)frameLayout.findViewById(R.id.listViewSearch);
            eLV=(ExpandableListView)frameLayout.findViewById(R.id.expandableListView);
            frameLayoutFunctionManager = new FrameLayout(context);
            eLV.addHeaderView(frameLayoutFunctionManager);
            refreshAllExpandableItemsByFloor();
            eLV.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
                @Override
                public boolean onChildClick(ExpandableListView expandableListView, View view, int i, int i2, long l) {
                    final LocationRegion lr = eAdapter.childs.get(i).get(i2).get("child");
                    SearchManager.this.closeView();
                    if(onLocationRegionClick!=null) {
                        onLocationRegionClick.onClick(lr);
                    }
                    return false;
                }
            });
        }

        editTextSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
                if(charSequence.length()==0) {
                    eLV.setVisibility(View.VISIBLE);
                    eLV.smoothScrollToPosition(0);

                } else {
                    eLV.setVisibility(View.INVISIBLE);

                }
                refreshListView();

            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        if(functionElementList!=null&&functionManager==null) {
            generateFunctionManager(functionElementList,numberElementInRow);
            functionManager.openFunctionView();
        }
        for(int i=0;i<eLV.getAdapter().getCount();i++) {
            eLV.collapseGroup(i);

        }
        frameLayout.setVisibility(View.VISIBLE);
        eLV.smoothScrollToPosition(0);
        show=true;
    }

    private void refreshListView() {

        Cursor cursor=getLocationRegionMatches(editTextSearch.getText().toString(), new String[]{LR_ID,LR_NAME,LR_DESC});
        int count=0;
        if(cursor!=null) {
            count=cursor.getCount();
            if(editTextSearch.getText().toString().length()==0)
                count=0;
        }
        LocationRegion[] locationRegions=new LocationRegion[count];
        if(count!=0) {
            int i=0;
            while(cursor.moveToNext()) {
                locationRegions[i]=getLocationRegionById(cursor.getInt(0));
                i++;
            }
        }
        final LocationRegionListAdapter locationRegionListAdapter=new LocationRegionListAdapter(context,locationRegions);
        listViewSearch.setAdapter(locationRegionListAdapter);
        listViewSearch.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                LocationRegion lr=locationRegionListAdapter.getItem(i);
                SearchManager.this.closeView();
                if(onLocationRegionClick!=null)
                    onLocationRegionClick.onClick(lr);
            }
        });
    }

    public boolean isViewShow() {
        return show;
    }
    public void closeView() {
        frameLayout.setVisibility(View.INVISIBLE);
        show=false;
        InputMethodManager imm = (InputMethodManager)context.getSystemService(
                Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(editTextSearch.getWindowToken(), 0);
    }
    LocationRegion getLocationRegionById(int id) {
        LocationRegion lr=new LocationRegion();
        lr.id=id;
        int index=Collections.binarySearch(locationRegionList,lr,new LocationRegionIdComparator());
        if(index>=0)
            return locationRegionList.get(index);
        return null;
    }

    public void setOnFunctionClickListener(FunctionManager.OnFunctionClickListener onFunctionClickListener) {
        this.onFunctionClickListener=onFunctionClickListener;
    }

    class LocationRegionIdComparator implements Comparator<LocationRegion> {
        @Override
        public int compare(LocationRegion a, LocationRegion b) {
            return (int)(a.id-b.id);
        }
    }
    void createLocationRegionDB() {
        int id=1;
        removeAll();
        database = dbHelper.getWritableDatabase();
        locationRegionList.clear();
        for(String floor:mSails.getFloorNameList()) {
            for(LocationRegion lr:mSails.getLocationRegionList(floor)) {
                lr.id=id;
                int index=Collections.binarySearch(locationRegionList,lr,new LocationRegionIdComparator());
                if(index<0)
                    locationRegionList.add(-index-1,lr);
                createLocationRegion(Integer.toString(id), lr.getName(),"");
                id++;
            }

        }
    }
    public long createLocationRegion(String id, String name, String desc){
        ContentValues values = new ContentValues();
        values.put(LR_ID, id);
        values.put(LR_NAME, name);
        values.put(LR_DESC, desc);
        return database.insert(LR_TABLE, null, values);
    }
    public void removeAll()
    {
        // db.delete(String tableName, String whereClause, String[] whereArgs);
        // If whereClause is null, it will delete all rows.
        SQLiteDatabase db = dbHelper.getWritableDatabase(); // helper is object extends SQLiteOpenHelper
        try {
        db.delete(LR_TABLE, null, null);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
    //    public selectRecords() {
//        String[] cols = new String[] {LR_ID, EMP_NAME};
//        Cursor mCursor = database.query(true, LR_TABLE,cols,null
//                , null, null, null, null, null);
//        if (mCursor != null) {
//            mCursor.moveToFirst();
//
//
//        }
//        return mCursor; // iterate to get each value.

    /**
     * Returns a Cursor over all words that match the given query
     *
     * @param query The string to search for
     * @param columns The columns to include, if null then all are included
     * @return Cursor over all words that match, or null if none found.
     */
    public Cursor getLocationRegionMatches(String query, String[] columns) {
        String selection = LR_NAME + " LIKE ?";
        String[] selectionArgs = new String[] {"%"+query+"%"};

        return query(selection, selectionArgs, columns);

        /* This builds a query that looks like:
         *     SELECT <columns> FROM <table> WHERE <KEY_WORD> MATCH 'query*'
         * which is an FTS3 search for the query text (plus a wildcard) inside the word column.
         *
         * - "rowid" is the unique id for all rows but we need this value for the "_id" column in
         *    order for the Adapters to work, so the columns need to make "_id" an alias for "rowid"
         * - "rowid" also needs to be used by the SUGGEST_COLUMN_INTENT_DATA alias in order
         *   for suggestions to carry the proper intent data.
         *   These aliases are defined in the DictionaryProvider when queries are made.
         * - This can be revised to also search the definition text with FTS3 by changing
         *   the selection clause to use FTS_VIRTUAL_TABLE instead of KEY_WORD (to search across
         *   the entire table, but sorting the relevance could be difficult.
         */
    }

    /**
     * Performs a database query.
     * @param selection The selection clause
     * @param selectionArgs Selection arguments for "?" components in the selection
     * @param columns The columns to return
     * @return A Cursor over all rows matching the query
     */
    private Cursor query(String selection, String[] selectionArgs, String[] columns) {
        /* The SQLiteBuilder provides a map for all possible columns requested to
         * actual columns in the database, creating a simple column alias mechanism
         * by which the ContentProvider does not need to know the real column names
         */
//        SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
//        builder.setTables(FTS_VIRTUAL_TABLE);
//        builder.setProjectionMap(mColumnMap);
        SQLiteDatabase database=dbHelper.getWritableDatabase();
        Cursor cursor = database.query(LR_TABLE,columns,selection,selectionArgs,null,null,null);
//        Cursor cursor = builder.query(dbHelper.getReadableDatabase(),
//                columns, selection, selectionArgs, null, null, null);

        if (cursor == null) {
            return null;
        }
//        else if (!cursor.moveToFirst()) {
//            cursor.close();
//            return null;
//        }
        return cursor;
    }

    public class MyDatabaseHelper extends SQLiteOpenHelper {

        private static final String DATABASE_NAME = "poi";

        private static final int DATABASE_VERSION = 3;

        // Database creation sql statement
        private static final String FTS_TABLE_CREATE =
                "CREATE VIRTUAL TABLE " + LR_TABLE +
                        " USING fts3 (" +
                        LR_ID + ", " +
                        LR_NAME + ", "+LR_DESC+");";
        private static final String DATABASE_CREATE = "create table "+LR_TABLE +
                "( _id integer primary key,name text not null);";

        public MyDatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        // Method is called during creation of the database
        @Override
        public void onCreate(SQLiteDatabase database) {
            database.execSQL(FTS_TABLE_CREATE);

        }

        // Method is called during an upgrade of the database,
        @Override
        public void onUpgrade(SQLiteDatabase database, int oldVersion,
                              int newVersion) {
            Log.w(SearchManager.class.getName(),
                    "Upgrading database from version " + oldVersion + " to "
                            + newVersion + ", which will destroy all old data");
            database.execSQL("DROP TABLE IF EXISTS MyEmployees");
            onCreate(database);
        }
    }
}
