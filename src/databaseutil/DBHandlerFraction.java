package databaseutil;

import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHandlerFraction extends SQLiteOpenHelper {
	public static final String DATABASE_NAME = "MyDBName.db";
	public static final String FRACTION_TABLE_NAME = "Fraction";
	public static final String ORDER_ID = "order_id";
	public static final String PERSON_NAME = "person_name";
	public static final String ITEM_NAME = "item_name";
	public static final String UNITS = "units";
	public static final String F_UP = "up";
	public static final String F_DOWN = "down";

	   public DBHandlerFraction(Context context)
	   {
	      super(context, DATABASE_NAME , null, 1);
	      
	   }

	   @Override
	   public void onCreate(SQLiteDatabase db) {
	      // TODO Auto-generated method stub
	      db.execSQL(
	      "create table Fraction " +
	      "(order_id integer, person_name text,item_name text,units integer, up integer, down integer)"
	      );
	   }

	   @Override
	   public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
	      // TODO Auto-generated method stub
			  db.execSQL("DROP TABLE IF EXISTS "+FRACTION_TABLE_NAME);
	      onCreate(db);
	   }

	   public boolean insertData (int order_id, String person_name, String item_name, int units, int fup, int fdown)
	   {
	      SQLiteDatabase db = this.getWritableDatabase();
	      ContentValues contentValues = new ContentValues();

	      contentValues.put(ORDER_ID, order_id);
	      contentValues.put(PERSON_NAME, person_name);
	      contentValues.put(ITEM_NAME, item_name);
	      contentValues.put(UNITS, units);
	      contentValues.put(F_UP, fup);
	      contentValues.put(F_DOWN, fdown);
	      
	      db.insert(FRACTION_TABLE_NAME, null, contentValues);
	      return true;
	   }
	   
	   public Cursor getData(int order_id){
	      SQLiteDatabase db = this.getReadableDatabase();
	      Cursor res =  db.rawQuery( "select * from "+FRACTION_TABLE_NAME+" where id="+order_id+"", null );
	      return res;
	   }
	   public int numberOfRows(){
	      SQLiteDatabase db = this.getReadableDatabase();
	      int numRows = (int) DatabaseUtils.queryNumEntries(db, FRACTION_TABLE_NAME);
	      return numRows;
	   }
	   
	   
	   public boolean updateData (Integer order_id, String person_name, String item_name, int units, int fup, int fdown)
	   {
	      SQLiteDatabase db = this.getWritableDatabase();
	      ContentValues contentValues = new ContentValues();
	      
	      contentValues.put(ITEM_NAME, item_name);
	      contentValues.put(UNITS, units);
	      contentValues.put(F_UP, fup);
	      contentValues.put(F_DOWN, fdown);
	      
	      db.update(FRACTION_TABLE_NAME, contentValues, "order_id = ? AND person_name= ? ", new String[] { Integer.toString(order_id),person_name } );
	      return true;
	   }


	   public Integer deleteData (Integer order_id)
	   {
	      SQLiteDatabase db = this.getWritableDatabase();
	      return db.delete(FRACTION_TABLE_NAME, 
	      "order_id = ? ", 
	      new String[] { Integer.toString(order_id) });
	   }
	   
	   public ArrayList<FractionRowDS> getAllPersons(int order_id)
	   {
	      ArrayList<FractionRowDS> array_list=new ArrayList<FractionRowDS>();
	      FractionRowDS row=new FractionRowDS();

	      SQLiteDatabase db = this.getReadableDatabase();
	      Cursor res =  db.rawQuery( "select * from "+FRACTION_TABLE_NAME+" where order_id="+order_id+"", null );
	      res.moveToFirst();
	      while(res.isAfterLast() == false){
	    	  row=new FractionRowDS();
	    	  
	    	  row.order_id = order_id;
	    	  row.person_name = res.getString(res.getColumnIndex(PERSON_NAME));
	    	  row.item_name = res.getString(res.getColumnIndex(ITEM_NAME));	    	  	
	    	  row.units = res.getInt(res.getColumnIndex(UNITS));
	    	  row.f_up = res.getInt(res.getColumnIndex(F_UP));
	    	  row.f_down = res.getInt(res.getColumnIndex(F_DOWN));
	    	  	
	    	  array_list.add(row);
	         res.moveToNext();
	      }
	   return array_list;
	   }
	   
	   public boolean isAnyOnePaid(int order_id)
	   {
	      SQLiteDatabase db = this.getReadableDatabase();
	      Cursor res =  db.rawQuery( "select * from "+FRACTION_TABLE_NAME+" where order_id="+order_id+"", null );
	      res.moveToFirst();
	      boolean anyonepaid=false;
	      while(res.isAfterLast() == false){
	            
	            res.moveToNext();
	      }
	      
	      return anyonepaid;
	   }
}
